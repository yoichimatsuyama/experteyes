/*
 * Copyright (c) 2009 by Thomas Busey and Ruj Akavipat
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Experteyes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Thomas Busey and Ruj Akavipat ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Thomas Busey and Ruj Akavipat BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * TrialMarkingJPanel.java
 *
 * Created on November 27, 2007, 11:00 PM
 */
package eyetrackercalibrator.gui;

import eyetrackercalibrator.framemanaging.FrameManager;
import eyetrackercalibrator.framemanaging.FrameSynchronizor;
import eyetrackercalibrator.framemanaging.InformationDatabase;
import eyetrackercalibrator.framemanaging.ScreenFrameManager;
import eyetrackercalibrator.gui.util.AnimationTimer;
import eyetrackercalibrator.gui.util.GUIUtil;
import eyetrackercalibrator.gui.util.IntervalMarkerManager;
import eyetrackercalibrator.math.EyeGazeComputing;
import eyetrackercalibrator.trialmanaging.BothMatchingTrialFileHandler;
import eyetrackercalibrator.trialmanaging.LeftRightBothMatchingTrialFileHandler;
import eyetrackercalibrator.trialmanaging.ListATrialFileHandler;
import eyetrackercalibrator.trialmanaging.PresentationFileHandler;
import eyetrackercalibrator.trialmanaging.Trial;
import eyetrackercalibrator.trialmanaging.TrialFileHandler;
import eyetrackercalibrator.trialmanaging.TrialMarker;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import org.jdesktop.layout.GroupLayout;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;

/**
 * @author  SQ
 */
public class TrialMarkingJPanel extends javax.swing.JPanel {

    private AnimationTimer timer;
    TrialMarker trialMarking = null;
    DefaultListModel trialSet = new DefaultListModel();
    LinkedList<IntervalMarker> freeIntervalMarkersList = new LinkedList<IntervalMarker>();
    private TrialMarkingGraphPanel graphTabPanel;
    IntervalMarkerManager intervalMarkerManager = null;
    public static String TRIAL_HANDLER_ELEMENT_NAME = "TrialHandlerClass";
    private InformationDatabase informationDatabase;
    private String fullScreenFrameDirectory;
    private IntervalMarker fullTrialMarker = null;
    private boolean isGettingEndOfFullTrial = false;
    private File projectLocation;
    private String trialInfoHandlerClassName = null;

    /** Creates new form TrialMarkingJPanel */
    public TrialMarkingJPanel() {
        initComponents();

        // Graph tab panel has to be added outside since group layout will prevent
        // JGraphPanel from displaying properly
        GroupLayout layout = (GroupLayout) getLayout();
        graphTabPanel = new TrialMarkingGraphPanel();
        graphTabPanel.setFixVerticalSize(247);
        layout.replace(graphHolder, graphTabPanel);

        // Add listener to mouse click on graph
        graphTabPanel.addChartProgressListener(new ChartProgressListener() {

            @Override
            public void chartProgress(ChartProgressEvent chartProgressEvent) {
                if (chartProgressEvent.getType() == ChartProgressEvent.DRAWING_FINISHED) {
                    JFreeChart chart = chartProgressEvent.getChart();
                    if (chart != null) {
                        XYPlot plot = (XYPlot) chart.getPlot();
                        if (plot != null) {
                            graphTabMouseClickedHandle(plot.getDomainCrosshairValue());
                        }
                    }
                }
            }
        });

        // Add listener to frame scroll to detect frame change
        frameScrollingJPanel.addPropertyChangeListener(
                FrameScrollingJPanel.CURRENT_FRAME_CHANGE,
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        frameChangeHandler(evt);
                    }
                });

        // Set up animation timer
        timer = new AnimationTimer();
        timer.setDisplayJPanel(displayJPanel);
        timer.setEyeFrameScrollingJPanel(frameScrollingJPanel);
        timer.setScreenFrameScrollingJPanel(frameScrollingJPanel);

        intervalMarkerManager = new IntervalMarkerManager(graphTabPanel);
    }

    /**
     * Handle mouse click on graph be moving the screen to a frame selected
     */
    private void graphTabMouseClickedHandle(double x) {
        int frameNumber = (int) x;
        // Don't do anything if 0 or less
        if (frameNumber > 0) {
            // Set current frame number
            frameScrollingJPanel.setCurrentFrame(frameNumber);
        }
    }

    /**
     * Handle when current frame is changed in Frame scrolling manager
     */
    private void frameChangeHandler(PropertyChangeEvent evt) {
        Integer frame = (Integer) evt.getNewValue();
        // Set the graph to point to correct frame
        graphTabPanel.setCurrentCrossHairPosition(frame.doubleValue());

        // Move marker if there is one
        if (this.trialMarking != null) {
            this.trialMarking.setEndFrame(frame,
                    timer.getFrameSynchronizor().getEyeFrame(frame),
                    timer.getFrameSynchronizor().getSceneFrame(frame));
        }

        if (this.fullTrialMarker != null && isGettingEndOfFullTrial) {
            this.fullTrialMarker.setEndValue(
                    this.frameScrollingJPanel.getCurrentFrame());
        }
    }

    /** 
     * For starting animation assume that all parameter are set correctly
     * especially setProjectRoot
     */
    public void start() {
        timer.start();
    }

    /** For stoping animation */
    public void stop() {
        timer.stop();
    }

    public void setInformationDatabase(InformationDatabase informationDatabase) {
        this.informationDatabase = informationDatabase;
        graphTabPanel.setIlluminationDataSet(informationDatabase);
    }

    /**
     * Add actionlistener to "Back" commands
     */
    public void addActionListener(ActionListener listener) {
        backButton.addActionListener(listener);
    }

    /** 
     * Require setOffset to be called first to make the scrolling panel working
     * properly
     */
    public void setEyeFrameManager(FrameManager eyeFrameManager) {
        // Register framemanager to animation timer
        timer.setEyeFrameManager(eyeFrameManager);

        // Register framemanager to graph display
        graphTabPanel.setEyeFrameManager(eyeFrameManager);

        // Set total frame for frame scrolling
        setTotalFrame(timer.getScreenFrameManager(), eyeFrameManager);

        // Reset viewing position
        frameScrollingJPanel.setCurrentFrame(1);

    }

    /** 
     * Require setOffset to be called first to make the scrolling panel working
     * properly
     */
    public void setScreenFrameManager(ScreenFrameManager screenFrameManager) {
        timer.setScreenFrameManager(screenFrameManager);

        // Set total frame for frame scrolling
        setTotalFrame(timer.getEyeFrameManager(), screenFrameManager);

        // Reset viewing position
        frameScrollingJPanel.setCurrentFrame(1);
    }

    /** 
     * Helper for setting total frame for frame scrolling panel.  The method set
     * the total frame to be the maximum between total eye and total screen frames
     */
    private void setTotalFrame(
            FrameManager eyeFrameManager,
            FrameManager screenFrameManager) {
        if (eyeFrameManager != null) {
            if (screenFrameManager != null) {
                this.frameScrollingJPanel.setTotalFrame(Math.max(
                        eyeFrameManager.getTotalFrames(),
                        screenFrameManager.getTotalFrames()));
            } else {
                this.frameScrollingJPanel.setTotalFrame(
                        eyeFrameManager.getTotalFrames());
            }
        } else if (screenFrameManager != null) {
            this.frameScrollingJPanel.setTotalFrame(
                    screenFrameManager.getTotalFrames());
        } else {
            this.frameScrollingJPanel.setTotalFrame(1);
        }
    }

    /**  Set frame sync and total frames*/
    public void setFrameSynchronizor(FrameSynchronizor frameSynchronizor) {
        this.timer.setFrameSynchronizor(frameSynchronizor);
        this.graphTabPanel.setFrameSynchronizor(frameSynchronizor);
        this.frameScrollingJPanel.setTotalFrame(frameSynchronizor.getTotalFrame());
    }

    public void setEyeGazeScaleFactor(double scaleFactor) {
        // Set it to display
        displayJPanel.setGazeScaleFactor(scaleFactor);
    }

    public void setProjectRoot(File projectLocation) {
        this.projectLocation = projectLocation;

    }

    public void setFullScreenFrameDirectory(String fullScreenFrameDirectory) {
        this.fullScreenFrameDirectory = fullScreenFrameDirectory;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        markTrialPanel = new javax.swing.JPanel();
        trialMarkCancelButton = new javax.swing.JButton();
        markButton = new javax.swing.JButton();
        trialRangeMarkLabel = new javax.swing.JLabel();
        graphHolder = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        frameScrollingJPanel = new eyetrackercalibrator.gui.FrameScrollingJPanel();
        displayJPanel = new eyetrackercalibrator.gui.DisplayJPanel();
        bottomPanel = new javax.swing.JPanel();
        trialInfoPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        trialList = new javax.swing.JList();
        removeButton = new javax.swing.JButton();
        fixEndFrameButton = new javax.swing.JButton();
        fixStartFrameButton = new javax.swing.JButton();
        badTrialCheckBox = new javax.swing.JCheckBox();
        backButton = new javax.swing.JButton();
        operationPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        trialLabelTextField = new javax.swing.JTextField();
        markToggleButton = new javax.swing.JToggleButton();
        loadTrialPanel = new javax.swing.JPanel();
        loadTrialInfoButton = new javax.swing.JButton();
        estimateTrialButton = new javax.swing.JButton();

        markTrialPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        trialMarkCancelButton.setText("Cancel");
        trialMarkCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trialMarkCancelButtonActionPerformed(evt);
            }
        });

        markButton.setText("Mark");
        markButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markButtonActionPerformed(evt);
            }
        });

        trialRangeMarkLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        trialRangeMarkLabel.setText("Text");
        trialRangeMarkLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        org.jdesktop.layout.GroupLayout markTrialPanelLayout = new org.jdesktop.layout.GroupLayout(markTrialPanel);
        markTrialPanel.setLayout(markTrialPanelLayout);
        markTrialPanelLayout.setHorizontalGroup(
            markTrialPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(markTrialPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(markTrialPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(trialRangeMarkLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .add(markButton)
                    .add(trialMarkCancelButton))
                .addContainerGap())
        );
        markTrialPanelLayout.setVerticalGroup(
            markTrialPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, markTrialPanelLayout.createSequentialGroup()
                .add(trialRangeMarkLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(markButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 13, Short.MAX_VALUE)
                .add(trialMarkCancelButton)
                .addContainerGap())
        );

        graphHolder.setBackground(new java.awt.Color(51, 255, 0));
        graphHolder.setMaximumSize(new java.awt.Dimension(32767, 247));
        graphHolder.setPreferredSize(new java.awt.Dimension(862, 247));

        org.jdesktop.layout.GroupLayout graphHolderLayout = new org.jdesktop.layout.GroupLayout(graphHolder);
        graphHolder.setLayout(graphHolderLayout);
        graphHolderLayout.setHorizontalGroup(
            graphHolderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 693, Short.MAX_VALUE)
        );
        graphHolderLayout.setVerticalGroup(
            graphHolderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 247, Short.MAX_VALUE)
        );

        displayJPanel.setMinimumSize(new java.awt.Dimension(200, 100));

        org.jdesktop.layout.GroupLayout topPanelLayout = new org.jdesktop.layout.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(frameScrollingJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 693, Short.MAX_VALUE)
            .add(displayJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 693, Short.MAX_VALUE)
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, topPanelLayout.createSequentialGroup()
                .add(displayJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(frameScrollingJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        bottomPanel.setPreferredSize(new java.awt.Dimension(285, 264));

        trialInfoPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        trialList.setModel(trialSet);
        trialList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                trialListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(trialList);

        removeButton.setText("-");
        removeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        fixEndFrameButton.setText("Fix end frame");
        fixEndFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixEndFrameButtonActionPerformed(evt);
            }
        });

        fixStartFrameButton.setText("Fix start frame");
        fixStartFrameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixStartFrameButtonActionPerformed(evt);
            }
        });

        badTrialCheckBox.setText("Bad trial");
        badTrialCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                badTrialCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout trialInfoPanelLayout = new org.jdesktop.layout.GroupLayout(trialInfoPanel);
        trialInfoPanel.setLayout(trialInfoPanelLayout);
        trialInfoPanelLayout.setHorizontalGroup(
            trialInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(trialInfoPanelLayout.createSequentialGroup()
                .add(trialInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(fixEndFrameButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(fixStartFrameButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(badTrialCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
        );
        trialInfoPanelLayout.setVerticalGroup(
            trialInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, trialInfoPanelLayout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(trialInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fixStartFrameButton)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(badTrialCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fixEndFrameButton))
        );

        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Label:");

        markToggleButton.setText("Start marking a trial");
        markToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markToggleButtonActionPerformed(evt);
            }
        });

        loadTrialPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        loadTrialInfoButton.setText("Load trial info");
        loadTrialInfoButton.setActionCommand("Load Trial Marking Info");
        loadTrialInfoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadTrialInfoButtonActionPerformed(evt);
            }
        });
        loadTrialPanel.add(loadTrialInfoButton);

        estimateTrialButton.setText("Estimate trials");
        estimateTrialButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                estimateTrialButtonActionPerformed(evt);
            }
        });
        loadTrialPanel.add(estimateTrialButton);

        org.jdesktop.layout.GroupLayout operationPanelLayout = new org.jdesktop.layout.GroupLayout(operationPanel);
        operationPanel.setLayout(operationPanelLayout);
        operationPanelLayout.setHorizontalGroup(
            operationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(operationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(trialLabelTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .addContainerGap())
            .add(markToggleButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
            .add(loadTrialPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
        );
        operationPanelLayout.setVerticalGroup(
            operationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(operationPanelLayout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .add(operationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(trialLabelTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(markToggleButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loadTrialPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout bottomPanelLayout = new org.jdesktop.layout.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                .add(backButton)
                .add(bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, operationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, trialInfoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bottomPanelLayout.createSequentialGroup()
                .add(trialInfoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(operationPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(9, 9, 9)
                .add(backButton))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(graphHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 693, Short.MAX_VALUE)
                    .add(topPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bottomPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 292, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(topPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(graphHolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(bottomPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 673, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private void loadTrialInfoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadTrialInfoButtonActionPerformed
        // Stop user from leaving 
        GUIUtil.setEnableAllCompoenentsExcept(this, null, false);

        // Switch panel to allow marking
        GroupLayout layout = (GroupLayout) operationPanel.getLayout();
        layout.replace(loadTrialPanel, markTrialPanel);
        trialRangeMarkLabel.setText("<html>Mark starting point<br>of the first trial</html>");

        // Open dialog for user to mark the panel
        JOptionPane.showMessageDialog(null,
                "<html>Please select the starting point of the <b>FIRST</b> trial then click Mark button</html>");
}//GEN-LAST:event_loadTrialInfoButtonActionPerformed

    private void markToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markToggleButtonActionPerformed
        // Start marking
        JToggleButton button = (JToggleButton) evt.getSource();
        if (button.isSelected()) {
            // Set text to stop marking
            markToggleButton.setText("Stop marking a trial");

            // Disble trail label button
            trialLabelTextField.setEnabled(false);

            // Start recording information
            startRecording();
        } else {
            // Set text to stop marking
            markToggleButton.setText("Start marking a trial");

            // Enable trial label button
            trialLabelTextField.setEnabled(true);

            // Save information
            stopRecording();

            // Set trial name
            trialLabelTextField.setText("Trial " + (trialSet.getSize() + 1));
        }
    }//GEN-LAST:event_markToggleButtonActionPerformed

    private void startRecording() {

        // Unselect trial list
        this.trialList.clearSelection();

        // Create trial marking
        TrialMarker mark = new TrialMarker();

        // Set interval marker
        mark.setIntervalMarker(intervalMarkerManager.getNewIntervalMarker());

        // Setting marking
        mark.label = trialLabelTextField.getText();

        // Setting frame
        int frame = frameScrollingJPanel.getCurrentFrame();
        mark.setStartFrame(frame,
                timer.getFrameSynchronizor().getEyeFrame(frame),
                timer.getFrameSynchronizor().getSceneFrame(frame));

        mark.setEndFrame(frame,
                timer.getFrameSynchronizor().getEyeFrame(frame),
                timer.getFrameSynchronizor().getSceneFrame(frame));

        this.trialMarking = mark;
    }

    private void stopRecording() {
        // Adding to the error list
        trialSet.addElement(this.trialMarking);

        // Clear current mark
        this.trialMarking = null;
    }

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Remove entry from list
        Object[] selected = trialList.getSelectedValues();
        TrialMarker mark = null;
        for (int i = 0; i < selected.length; i++) {
            mark = (TrialMarker) selected[i];
            trialSet.removeElement(selected[i]);
            intervalMarkerManager.removeIntervalMarker(mark.getIntervalMarker());
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void trialListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_trialListMouseClicked

        // Get selected info
        TrialMarker mark = (TrialMarker) trialList.getSelectedValue();

        // More than one click move to marked frame
        // Check if it is double click or not
        if (evt.getClickCount() >= 2 && mark != null) {
            // Get info and move to the frame
            //int singleSelect = trialList.getSelectedIndex();
            //TrialMarker mark = (TrialMarker) trialSet.get(singleSelect);
            int frame = 0;
            if (evt.getClickCount() == 2) {
                // Two click to move to the starting frame
                frame = timer.getFrameSynchronizor().eyeFrameToSyncFrame(mark.startEyeFrame);
                /** Use scene frame when eye is not available*/
                if (frame < 1) {
                    frame = timer.getFrameSynchronizor().sceneFrameToSyncFrame(mark.startSceneFrame);
                }
            } else {
                // Three click to move to the end frame
                frame = timer.getFrameSynchronizor().eyeFrameToSyncFrame(mark.stopEyeFrame);
                if (frame < 1) {
                    frame = timer.getFrameSynchronizor().sceneFrameToSyncFrame(mark.stopSceneFrame);
                }
            }
            frameScrollingJPanel.setCurrentFrame(frame);
        }
        // Anyway change the bad trial mark accordingly
        if (mark != null) {
            this.badTrialCheckBox.setSelected(mark.isBadTrial);
        }
    }//GEN-LAST:event_trialListMouseClicked

    private void trialMarkCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trialMarkCancelButtonActionPerformed
        // Switch panel to back to loading
        GroupLayout layout = (GroupLayout) operationPanel.getLayout();
        try {
            layout.replace(markTrialPanel, loadTrialPanel);
        } catch (IllegalArgumentException discard) {
        }
        cancelTrialInfoLoading();
}//GEN-LAST:event_trialMarkCancelButtonActionPerformed

    private void cancelTrialInfoLoading() {
        // Set the flag
        this.isGettingEndOfFullTrial = false;

        if (this.fullTrialMarker != null) {
            intervalMarkerManager.removeIntervalMarker(this.fullTrialMarker);
            this.fullTrialMarker = null;
        }

        // Allow user to leave 
        GUIUtil.setEnableAllCompoenentsExcept(this, null, true);
    }

    private void markButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markButtonActionPerformed
        if (this.fullTrialMarker == null) {
            /** 
             * This is the case that the user just finish marking the starting
             * trial 
             */
            // Set up range marking
            this.fullTrialMarker = intervalMarkerManager.getNewIntervalMarker();
            this.fullTrialMarker.setPaint(Color.CYAN);
            this.fullTrialMarker.setAlpha(0.2f);

            // Set the starting point
            this.fullTrialMarker.setStartValue(
                    this.frameScrollingJPanel.getCurrentFrame());

            // Set end point to be the starting point
            this.fullTrialMarker.setEndValue(this.fullTrialMarker.getStartValue());

            // Set the flag
            this.isGettingEndOfFullTrial = true;

            trialRangeMarkLabel.setText("<html>Mark starting point<br>of the last trial</html>");
            JOptionPane.showMessageDialog(null,
                    "<html>Please select the starting point of the <b>LAST</b> trial then click Mark button</html>");
        } else {
            /**
             * This is the case that the user finish marking the final trial.
             */
            this.fullTrialMarker.setEndValue(
                    this.frameScrollingJPanel.getCurrentFrame());

            // Set the flag
            this.isGettingEndOfFullTrial = false;

            loadTrialInfo();
        }
}//GEN-LAST:event_markButtonActionPerformed

    private void estimateTrialButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_estimateTrialButtonActionPerformed
        if (trialSet.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "<html>You need to load the trial info / enter trial info <br>"
                    + "and compute illumination before estimation</html>");
        } else {
            // Get all trial info and sort it
            Object[] array = trialSet.toArray();
            TrialMarker[] trials = new TrialMarker[array.length];
            for (int i = 0; i < trials.length; i++) {
                trials[i] = (TrialMarker) array[i];
            }

            Arrays.sort(trials);

            // Load trial extimation class
            if (this.trialInfoHandlerClassName != null) {
                TrialFileHandler trialFileHandler = null;

                try {
                    Class cl = Class.forName(this.trialInfoHandlerClassName,
                            true, Thread.currentThread().getContextClassLoader());
                    trialFileHandler = (TrialFileHandler) cl.newInstance();
                } catch (InstantiationException ex) {
                    Logger.getLogger(TrialMarkingJPanel.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(TrialMarkingJPanel.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TrialMarkingJPanel.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }

                trialFileHandler.estimateTrialMarking(informationDatabase,
                        trials, timer.getFrameSynchronizor());
            }
        }
}//GEN-LAST:event_estimateTrialButtonActionPerformed

    private void fixStartFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixStartFrameButtonActionPerformed
        // Remove entry from list
        TrialMarker mark = (TrialMarker) trialList.getSelectedValue();
        if (mark != null) {
            int frame = frameScrollingJPanel.getCurrentFrame();
            mark.setStartFrame(frame,
                    timer.getFrameSynchronizor().getEyeFrame(frame),
                    timer.getFrameSynchronizor().getSceneFrame(frame));
        }
}//GEN-LAST:event_fixStartFrameButtonActionPerformed

    private void fixEndFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixEndFrameButtonActionPerformed
        // Remove entry from list
        TrialMarker mark = (TrialMarker) trialList.getSelectedValue();
        if (mark != null) {
            /** Compute frame from eye if not available then scene */
            int frame = frameScrollingJPanel.getCurrentFrame();
            mark.setEndFrame(frame,
                    timer.getFrameSynchronizor().getEyeFrame(frame),
                    timer.getFrameSynchronizor().getSceneFrame(frame));
        }
    }//GEN-LAST:event_fixEndFrameButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_backButtonActionPerformed

    private void badTrialCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_badTrialCheckBoxActionPerformed
        // Mark all selected trial accordingly
        Object[] selected = trialList.getSelectedValues();
        TrialMarker mark = null;
        for (int i = 0; i < selected.length; i++) {
            mark = (TrialMarker) selected[i];
            mark.isBadTrial = this.badTrialCheckBox.isSelected();
        }
    }//GEN-LAST:event_badTrialCheckBoxActionPerformed

    /** 
     * Load trial info making sure that only one of 
     * skip button or complete Illumination completion thread can access this
     */
    private synchronized void loadTrialInfo() {

        /**
         * Since noone is waiting for compute illu threads to die we proceed
         * to the next step
         */
        // Proceed to ask user to enter file name
        JOptionPane.showMessageDialog(null,
                "<html>Please select the trial data file</html>");

        // Ask user for a file
        // Choose new directory to put the project
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(this.projectLocation);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);

        fileChooser.addChoosableFileFilter(new BothMatchingTrialFileHandler());
        fileChooser.addChoosableFileFilter(new LeftRightBothMatchingTrialFileHandler());
        fileChooser.addChoosableFileFilter(new ListATrialFileHandler());
        fileChooser.addChoosableFileFilter(new PresentationFileHandler());

        Trial t = null;
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {

                // Clear previous trial marker
                trialSet.clear();
                intervalMarkerManager.clearIntervalMarker();

                // Get the file type the user select
                TrialFileHandler trialFileHandler =
                        (TrialFileHandler) fileChooser.getFileFilter();

                t = trialFileHandler.parse(file);
                Vector<TrialMarker> markers = trialFileHandler.estimateTrials(
                        informationDatabase, t,
                        (int) fullTrialMarker.getStartValue(),
                        (int) fullTrialMarker.getEndValue(),
                        this.timer.getFrameSynchronizor(),
                        intervalMarkerManager);


                for (Iterator<TrialMarker> it = markers.iterator(); it.hasNext();) {
                    trialSet.addElement(it.next());
                }
                repaint();

                // Store type of file loaded
                this.trialInfoHandlerClassName =
                        fileChooser.getFileFilter().getClass().getName();
            }
        }

        // Remove full trial marker
        if (this.fullTrialMarker != null) {
            intervalMarkerManager.removeIntervalMarker(this.fullTrialMarker);
            this.fullTrialMarker = null;
        }

        // Switch panel to back to loading
        GroupLayout layout = (GroupLayout) operationPanel.getLayout();
        try {
            layout.replace(markTrialPanel, loadTrialPanel);
        } catch (IllegalArgumentException discard) {
        }

        cancelTrialInfoLoading();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JCheckBox badTrialCheckBox;
    private javax.swing.JPanel bottomPanel;
    private eyetrackercalibrator.gui.DisplayJPanel displayJPanel;
    private javax.swing.JButton estimateTrialButton;
    private javax.swing.JButton fixEndFrameButton;
    private javax.swing.JButton fixStartFrameButton;
    private eyetrackercalibrator.gui.FrameScrollingJPanel frameScrollingJPanel;
    private javax.swing.JPanel graphHolder;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton loadTrialInfoButton;
    private javax.swing.JPanel loadTrialPanel;
    private javax.swing.JButton markButton;
    private javax.swing.JToggleButton markToggleButton;
    private javax.swing.JPanel markTrialPanel;
    private javax.swing.JPanel operationPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel topPanel;
    private javax.swing.JPanel trialInfoPanel;
    private javax.swing.JTextField trialLabelTextField;
    private javax.swing.JList trialList;
    private javax.swing.JButton trialMarkCancelButton;
    private javax.swing.JLabel trialRangeMarkLabel;
    // End of variables declaration//GEN-END:variables

    public void loadTrialMarks(File trialMarkFile) {
        // Clear old marking
        clear();
        IntervalMarker intervalMarker;

        trialInfoHandlerClassName = null;

        // Load from a file
        if (trialMarkFile != null) {
            // Load from file
            SAXBuilder builder = new SAXBuilder();
            Element root = null;
            try {
                Document doc = builder.build(trialMarkFile);
                root = doc.getRootElement();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            // Get trial handler name if extist
            Element classNameElement = root.getChild(TRIAL_HANDLER_ELEMENT_NAME);
            if (classNameElement != null) {
                trialInfoHandlerClassName = classNameElement.getAttributeValue("name");
            }

            // Loop through all trial element
            List list = root.getChildren(TrialMarker.ELEMENT_NAME);
            boolean changeColor = false;
            for (Iterator it = list.iterator(); it.hasNext();) {
                Element elem = (Element) it.next();

                TrialMarker trial = new TrialMarker(elem);

                // Add marker to the list
                trialSet.addElement(trial);

                // Set up interval marker
                intervalMarker = intervalMarkerManager.getNewIntervalMarker();

                int frame = this.timer.getFrameSynchronizor().sceneFrameToSyncFrame(trial.startSceneFrame);
                if (frame < 1) {
                    /* Use eye frame when scene frame is no good */
                    frame = this.timer.getFrameSynchronizor().sceneFrameToSyncFrame(trial.startEyeFrame);
                }
                intervalMarker.setStartValue(frame);

                frame = this.timer.getFrameSynchronizor().sceneFrameToSyncFrame(trial.stopSceneFrame);
                if (frame < 1) {
                    /* Use eye frame when scene frame is no good */
                    frame = this.timer.getFrameSynchronizor().sceneFrameToSyncFrame(trial.stopEyeFrame);
                }
                intervalMarker.setEndValue(frame);
                if (changeColor) {// Change color for even trial
                    intervalMarker.setPaint(Color.YELLOW);
                }

                changeColor = !changeColor;

                trial.setIntervalMarker(intervalMarker);
            }

            // Set auto increment trial name
            trialLabelTextField.setText("Trial " + (trialSet.getSize() + 1));
        }
    }

    public void saveTrialMarks(File trialMarkFile) {
        Element root = new Element("root");

        if (trialInfoHandlerClassName != null) {
            Element className = new Element(TRIAL_HANDLER_ELEMENT_NAME);
            className.setAttribute("name", trialInfoHandlerClassName);
            root.addContent(className);
        }

        TrialMarker[] trial = getTrials();
        for (int i = 0; i < trial.length; i++) {
            root.addContent(trial[i].toElement());
        }

        // Write out to file as xml
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(new Document(root), new FileWriter(trialMarkFile));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Clear all information
     */
    public void clear() {
        trialSet.clear();

        trialLabelTextField.setText("Trial 1");

        // Cancel trial info loading if any
        cancelTrialInfoLoading();

        // Cannot run before cancelTrialInfoLoading since cancelTrialInfo
        intervalMarkerManager.clearIntervalMarker();
    }

    /** Get trials sorted by the frame */
    public TrialMarker[] getTrials() {
        Object[] objs = trialSet.toArray();

        TrialMarker[] trials = new TrialMarker[objs.length];
        // Copy
        for (int i = 0; i < trials.length; i++) {
            trials[i] = (TrialMarker) objs[i];
        }

        Arrays.sort(trials, new Comparator<TrialMarker>() {

            public int compare(TrialMarker o1, TrialMarker o2) {
                int i = o1.startEyeFrame - o2.startEyeFrame;
                if (i == 0) {
                    i = o1.stopEyeFrame - o2.stopEyeFrame;
                }
                if (i == 0 && o1.label != null && o2.label != null) {
                    i = o1.label.compareTo(o2.label);
                }
                return i;
            }
        });

        return trials;
    }

    public void setEyeGazeComputing(EyeGazeComputing eyeGazeComputing) {
        this.timer.setEyeGazeComputing(eyeGazeComputing);
    }
}
