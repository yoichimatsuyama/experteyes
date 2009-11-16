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
 * ScreenEstimationJPanel.java
 *
 * Created on November 5, 2007, 9:46 AM
 */
package eyetrackercalibrator.gui;

import eyetrackercalibrator.framemanaging.FrameManager;
import eyetrackercalibrator.framemanaging.FrameSynchronizor;
import eyetrackercalibrator.framemanaging.ScreenFrameManager;
import eyetrackercalibrator.gui.util.AnimationTimer;
import eyetrackercalibrator.gui.util.CompletionListener;
import eyetrackercalibrator.gui.util.IntervalMarkerManager;
import eyetrackercalibrator.math.CornerCorrection;
import eyetrackercalibrator.math.CornerCorrection.Corner;
import eyetrackercalibrator.math.EyeGazeComputing;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
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
 * For cleaning data.
 * This class shoulde be used like this
 *
 * 1. load information
 * 2. Set eye frame manager and screen frame manager (optional)
 * 3. start()
 *
 * @author  ruj
 */
public class CleanDataJPanel extends javax.swing.JPanel {

    private AnimationTimer timer;
    FrameInfoGraphTabPanel graphTabPanel = null;
    ErrorMarking errorMarking = null;
    DefaultListModel errorSet = new DefaultListModel();
    IntervalMarkerManager intervalMarkerManager = null;
    String fullScreenFrameDirectory = ".";
    private String cornerHintDir;
    private String screenInfoDir;
    private DefaultValueAppliedListener defaultValueAppliedListener = null;

    /** Creates new form ScreenEstimationJPanel */
    public CleanDataJPanel() {
        initComponents();

        // Graph tab panel has to be added outside since group layout will prevent
        // JGraphPanel from displaying properly
        GroupLayout layout = (GroupLayout) getLayout();
        graphTabPanel = new FrameInfoGraphTabPanel();
        graphTabPanel.setFixVerticalSize(222);
        layout.replace(graphHolder, graphTabPanel);

        // Add listener to mouse click on graph
        graphTabPanel.addChartProgressListener(new ChartProgressListener() {

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

    public void setFullScreenFrameDirectory(String fullScreenFrameDirectory) {
        this.fullScreenFrameDirectory = fullScreenFrameDirectory;
    }

    public void setCornerHintDir(String cornerHintDir) {
        this.cornerHintDir = cornerHintDir;
    }

    public void setScreenInfoDir(String screenInfoDir) {
        this.screenInfoDir = screenInfoDir;
    }

    public interface DefaultValueAppliedListener {

        void useDefaultCornerHintDir(String dir);

        void useDefaultScreenInfoDir(String dir);

        void useDefaultFullScreenFrameDir(String dir);
    }

    public void setDefaultValueAppliedListener(DefaultValueAppliedListener listener) {
        this.defaultValueAppliedListener = listener;
    }

    protected void handleDetectCornerButton() throws HeadlessException {

        Object[] errors = errorList.getSelectedValues();
        if (errors.length < 1) {
            // Shows warning if nothing is selected
            JOptionPane.showMessageDialog(this, "Please select a marked region from the list before starting corner detection.", "No marked region is selected", JOptionPane.ERROR_MESSAGE);
            return;
        }


        ErrorMarking[] ranges = new ErrorMarking[errors.length];
        for (int i = 0; i < ranges.length; i++) {
            ranges[i] = (ErrorMarking) errors[i];
        }

        String message = "";
        boolean needAttention = false;

        // Sanity check the output dir
        String preApprovedscreenInfoDir = this.screenInfoDir;
        if (preApprovedscreenInfoDir == null || preApprovedscreenInfoDir.length() < 1) {
            needAttention = true;

            // Create default screen info dir
            File dir = new File(this.timer.getScreenFrameManager().getFrameDirectory());
            dir = new File(dir.getParent(), "Corners");

            preApprovedscreenInfoDir = dir.getAbsolutePath();

            message = "Scene (corner) information will be stored in \n   " +
                    preApprovedscreenInfoDir + "\n\n";

        }

        String preApprovedCornerHintDir = this.cornerHintDir;
        if (preApprovedCornerHintDir == null || preApprovedCornerHintDir.length() < 1) {
            needAttention = true;

            // Create default corner hints info dir
            File dir = new File(preApprovedscreenInfoDir);
            dir = new File(dir.getParent(), "CornerHints");

            preApprovedCornerHintDir = dir.getAbsolutePath();

            message += "Corner hints will be stored in \n   " +
                    preApprovedCornerHintDir + "\n\n";

        }

        // Sanity check the large scene dir
        String preApprovedFullScreenFrameDirectory = this.fullScreenFrameDirectory;
        if (preApprovedFullScreenFrameDirectory == null ||
                preApprovedFullScreenFrameDirectory.length() < 1) {
            needAttention = true;

            // Create default full scene dir
            preApprovedFullScreenFrameDirectory = this.timer.getScreenFrameManager().getFrameDirectory();

            message += "Scene full size location is not specify, the scene view location will be used instead.\n\n";
        }
        File fullViewFile = new File(preApprovedFullScreenFrameDirectory);
        if (!fullViewFile.exists()) {
            needAttention = true;

            // Create default full scene dir
            preApprovedFullScreenFrameDirectory = this.timer.getScreenFrameManager().getFrameDirectory();

            message += "Scene full size location does not exists, the scene view location will be used instead.\n\n";
        }

        if (needAttention) {
            message += "If you wish to use other values, please return to project panel and change the values accordingly.";

            /**  Show approve message */
            int reply = JOptionPane.showConfirmDialog(this, message,
                    "Default Values Applied", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            if (reply != JOptionPane.OK_OPTION) {
                return;
            } else {
                /** Set values */
                this.cornerHintDir = preApprovedCornerHintDir;
                this.screenInfoDir = preApprovedscreenInfoDir;
                this.fullScreenFrameDirectory = preApprovedFullScreenFrameDirectory;

                if (this.defaultValueAppliedListener != null) {
                    this.defaultValueAppliedListener.useDefaultCornerHintDir(preApprovedCornerHintDir);
                    this.defaultValueAppliedListener.useDefaultFullScreenFrameDir(preApprovedFullScreenFrameDirectory);
                    this.defaultValueAppliedListener.useDefaultScreenInfoDir(preApprovedscreenInfoDir);
                }
            }
        }

        File screenInfoDirFile = new File(this.screenInfoDir);

        // Disable button to prevent double clicking
        this.detectCornerButton.setEnabled(false);

        DetectCornerRunner detectCornerRunner = new DetectCornerRunner(
                this.timer.getScreenFrameManager(), ranges,
                this.fullScreenFrameDirectory,
                new File(this.cornerHintDir),
                screenInfoDirFile,
                new CompletionListener() {

                    public void fullCompletion() {
                        // We are done so reenable the button
                        detectCornerButton.setEnabled(true);
                    }
                });
        detectCornerRunner.start();
    }

    /**
     * Handle when current frame is changed in Frame scrolling manager
     */
    private void frameChangeHandler(PropertyChangeEvent evt) {
        Integer frame = (Integer) evt.getNewValue();
        // Set the graph to point to correct frame
        graphTabPanel.setCurrentCrossHairPosition(frame.doubleValue());

        // Move marker if there is one
        if (this.errorMarking != null) {
            this.errorMarking.setEndFrame(frame,
                    this.timer.getFrameSynchronizor().getEyeFrame(frame),
                    this.timer.getFrameSynchronizor().getSceneFrame(frame));
        }
    }

    /** For starting animation */
    public void start() {
        timer.start();
    }

    /** For stoping animation */
    public void stop() {
        timer.stop();
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

        // Set total frame for frame scrolling
        setTotalFrame(timer.getScreenFrameManager(), eyeFrameManager);

        // Register framemanager to graph display
        graphTabPanel.setEyeFrameManager(eyeFrameManager);

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

        // Register framemanager to graph display
        graphTabPanel.setScreenFrameManager(screenFrameManager);

        // Reset viewing position
        frameScrollingJPanel.setCurrentFrame(1);
    }

    public void setEyeGazeComputing(EyeGazeComputing eyeGazeComputing) {
        this.timer.setEyeGazeComputing(eyeGazeComputing);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        errorList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        detectCornerButton = new javax.swing.JButton();
        markToggleButton = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        eyeVectorToggleButton = new javax.swing.JToggleButton();
        topLeftToggleButton = new javax.swing.JToggleButton();
        topRightToggleButton = new javax.swing.JToggleButton();
        bottomRightToggleButton = new javax.swing.JToggleButton();
        bottomLeftToggleButton = new javax.swing.JToggleButton();
        unrecoverableToggleButton = new javax.swing.JToggleButton();
        removeButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();
        displayJPanel = new eyetrackercalibrator.gui.DisplayJPanel();
        frameScrollingJPanel = new eyetrackercalibrator.gui.FrameScrollingJPanel();
        graphHolder = new javax.swing.JPanel();

        errorList.setModel(errorSet);
        errorList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                errorListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(errorList);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Error Correcting"));
        jPanel2.setLayout(new java.awt.CardLayout());

        detectCornerButton.setText("Detect Corners");
        detectCornerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detectCornerButtonActionPerformed(evt);
            }
        });
        jPanel2.add(detectCornerButton, "card2");

        markToggleButton.setText("Start marking");
        markToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markToggleButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Error Types"));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        eyeVectorToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/eyetrackercalibrator/gui/resources/eye.png"))); // NOI18N
        eyeVectorToggleButton.setToolTipText("Eye detection error");
        eyeVectorToggleButton.setMargin(new java.awt.Insets(7, 7, 7, 7));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(eyeVectorToggleButton, gridBagConstraints);

        topLeftToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/eyetrackercalibrator/gui/resources/topleft.png"))); // NOI18N
        topLeftToggleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        topLeftToggleButton.setMargin(new java.awt.Insets(7, 7, 7, 7));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel1.add(topLeftToggleButton, gridBagConstraints);

        topRightToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/eyetrackercalibrator/gui/resources/topright.png"))); // NOI18N
        topRightToggleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        topRightToggleButton.setMargin(new java.awt.Insets(7, 7, 7, 7));
        jPanel1.add(topRightToggleButton, new java.awt.GridBagConstraints());

        bottomRightToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/eyetrackercalibrator/gui/resources/bottomright.png"))); // NOI18N
        bottomRightToggleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        bottomRightToggleButton.setMargin(new java.awt.Insets(7, 7, 7, 7));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanel1.add(bottomRightToggleButton, gridBagConstraints);

        bottomLeftToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/eyetrackercalibrator/gui/resources/bottomleft.png"))); // NOI18N
        bottomLeftToggleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        bottomLeftToggleButton.setMargin(new java.awt.Insets(7, 7, 7, 7));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel1.add(bottomLeftToggleButton, gridBagConstraints);

        unrecoverableToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/eyetrackercalibrator/gui/resources/trash.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(unrecoverableToggleButton, gridBagConstraints);

        removeButton.setText("Delete");
        removeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        backButton.setText("Back");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(backButton))
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(markToggleButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 96, Short.MAX_VALUE)
                .add(removeButton))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(markToggleButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(backButton))
        );

        displayJPanel.setMinimumSize(new java.awt.Dimension(200, 100));

        graphHolder.setBackground(new java.awt.Color(0, 153, 0));

        org.jdesktop.layout.GroupLayout graphHolderLayout = new org.jdesktop.layout.GroupLayout(graphHolder);
        graphHolder.setLayout(graphHolderLayout);
        graphHolderLayout.setHorizontalGroup(
            graphHolderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 635, Short.MAX_VALUE)
        );
        graphHolderLayout.setVerticalGroup(
            graphHolderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 222, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(displayJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, frameScrollingJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, graphHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(displayJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(frameScrollingJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(graphHolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    private void errorListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_errorListMouseClicked
        // Any click to select and enable corner correction
        // Enable corner correction when some thing is slected
        int[] select = errorList.getSelectedIndices();

        // Two click to move to the starting frame
        // Check if it is double click or not
        if (evt.getClickCount() >= 2) {
            // Get info and move to the frame
            int singleSelect = errorList.getSelectedIndex();
            ErrorMarking mark = (ErrorMarking) errorSet.get(singleSelect);
            frameScrollingJPanel.setCurrentFrame(
                    this.timer.getFrameSynchronizor().eyeFrameToSyncFrame(mark.startEyeFrame));
        }
    }//GEN-LAST:event_errorListMouseClicked

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Remove entry from list
        int[] selected = errorList.getSelectedIndices();
        ErrorMarking mark = null;
        for (int i = 0; i < selected.length; i++) {
            mark = (ErrorMarking) errorSet.remove(selected[i]);
            // Remove marker from graph
            intervalMarkerManager.removeIntervalMarker(mark.getIntervalMarker());
        }

//        // Turn off the corner correction
//        correctCornerButton.setEnabled(false);
    }//GEN-LAST:event_removeButtonActionPerformed

    private void markToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markToggleButtonActionPerformed
        // Check and warn if nothing is selected
        if (!(topLeftToggleButton.isSelected() ||
                topRightToggleButton.isSelected() ||
                bottomLeftToggleButton.isSelected() ||
                bottomRightToggleButton.isSelected() ||
                eyeVectorToggleButton.isSelected() ||
                unrecoverableToggleButton.isSelected())) {
            JOptionPane.showMessageDialog(this,
                    "You must select at least one error type",
                    "Unable to mark error",
                    JOptionPane.ERROR_MESSAGE);
            markToggleButton.setSelected(false);
        } else {

            // Start marking
            JToggleButton button = (JToggleButton) evt.getSource();
            if (button.isSelected()) {
                // Set text to stop marking 
                markToggleButton.setText("Stop marking");

                // Disable error button
                setErrorButtonsEnable(false);
                // Start recording information
                startRecording();
            } else {
                // Set text to start marking 
                markToggleButton.setText("Start marking");

                // Enable button
                setErrorButtonsEnable(true);
                // Save information
                stopRecording();
            }
        }
    }//GEN-LAST:event_markToggleButtonActionPerformed

private void detectCornerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detectCornerButtonActionPerformed

    handleDetectCornerButton();

}//GEN-LAST:event_detectCornerButtonActionPerformed

    private void correctCorner(ProgressJDialog progressDialog) {
        CornerCorrection cornerCorrection = new CornerCorrection();

        // Get all select errors
        Object[] errorArray = errorList.getSelectedValues();

        int completed = 0;

        for (int i = 0; i < errorArray.length; i++) {
            ErrorMarking error = (ErrorMarking) errorArray[i];

            Corner corner = Corner.BOTTOMLEFT;
            // Get a corner to correct and show warning if there are more than
            // one bad corner
            int count = 0;
            if (error.bottomleft) {
                count++;
                corner = Corner.BOTTOMLEFT;
            }
            if (error.bottomright) {
                count++;
                corner = Corner.BOTTOMRIGHT;
            }
            if (error.topleft) {
                count++;
                corner = Corner.TOPLEFT;
            }
            if (error.topright) {
                count++;
                corner = Corner.TOPRIGHT;
            }
            if (count != 1) {
                // Spit warning if there are too many or too few
                // Open dialog to warn user
                if (count > 1) {
                    JOptionPane.showMessageDialog(this,
                            "There are more than one incorrect corner in frame " +
                            +error.startSceneFrame + " to " + error.stopSceneFrame +
                            ". They cannot be corrected",
                            "Unable to correct corners",
                            JOptionPane.ERROR_MESSAGE);
                }

                completed += error.stopSceneFrame = error.startSceneFrame + 1;
                progressDialog.setProgrss(completed);
            } else {
                // Iterate correction through frame range
                for (int j = error.startSceneFrame; j <= error.stopSceneFrame; j++) {

                    cornerCorrection.correctCorner(j, timer.getScreenFrameManager(), corner);

                    completed++;
                    progressDialog.setProgrss(completed);
                }
            }
        }
    }

    private void setErrorButtonsEnable(boolean v) {
        eyeVectorToggleButton.setEnabled(v);
        topLeftToggleButton.setEnabled(v);
        topRightToggleButton.setEnabled(v);
        bottomLeftToggleButton.setEnabled(v);
        bottomRightToggleButton.setEnabled(v);
        unrecoverableToggleButton.setEnabled(v);
    }

    private void startRecording() {
        // Create error marking
        ErrorMarking mark = new ErrorMarking();

        // Setting marking
        mark.topleft = topLeftToggleButton.isSelected();
        mark.topright = topRightToggleButton.isSelected();
        mark.bottomleft = bottomLeftToggleButton.isSelected();
        mark.bottomright = bottomRightToggleButton.isSelected();
        mark.eye = eyeVectorToggleButton.isSelected();
        mark.unrecoverable = unrecoverableToggleButton.isSelected();

        // Setting marker
        IntervalMarker intervalMarker = intervalMarkerManager.getNewIntervalMarker();

        mark.setIntervalMarker(intervalMarker);

        // Setting frame
        int currentFrame = frameScrollingJPanel.getCurrentFrame();
        mark.setStartFrame(currentFrame,
                this.timer.getFrameSynchronizor().getEyeFrame(currentFrame),
                this.timer.getFrameSynchronizor().getSceneFrame(currentFrame));

        mark.setEndFrame(currentFrame,
                this.timer.getFrameSynchronizor().getEyeFrame(currentFrame),
                this.timer.getFrameSynchronizor().getSceneFrame(currentFrame));

        this.errorMarking = mark;
    }

    private void stopRecording() {
        // Adding to the error list
        errorSet.addElement(errorMarking);

        // Clear current mark
        this.errorMarking = null;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JToggleButton bottomLeftToggleButton;
    private javax.swing.JToggleButton bottomRightToggleButton;
    private javax.swing.JButton detectCornerButton;
    private eyetrackercalibrator.gui.DisplayJPanel displayJPanel;
    private javax.swing.JList errorList;
    private javax.swing.JToggleButton eyeVectorToggleButton;
    private eyetrackercalibrator.gui.FrameScrollingJPanel frameScrollingJPanel;
    private javax.swing.JPanel graphHolder;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToggleButton markToggleButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JToggleButton topLeftToggleButton;
    private javax.swing.JToggleButton topRightToggleButton;
    private javax.swing.JToggleButton unrecoverableToggleButton;
    // End of variables declaration//GEN-END:variables

    public void loadErrors(File errorFile) {
        // Clear old marking
        errorSet.clear();
        intervalMarkerManager.clearIntervalMarker();

        // Load from a file
        if (errorFile != null) {
            // Load from file
            SAXBuilder builder = new SAXBuilder();
            Element root = null;
            try {
                Document doc = builder.build(errorFile);
                root = doc.getRootElement();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            // Loop through all element
            List list = root.getChildren();
            for (Iterator it = list.iterator(); it.hasNext();) {
                Element elem = (Element) it.next();

                ErrorMarking error = new ErrorMarking(elem);

                // Add marker to the graph
                IntervalMarker intervalMarker = intervalMarkerManager.getNewIntervalMarker();
                int frame = error.startEyeFrame;
                if (frame < 1) {
                    frame = error.startSceneFrame;
                }
                frame = error.stopEyeFrame;
                if (frame < 1) {
                    frame = error.stopSceneFrame;
                }
                intervalMarker.setEndValue(
                        this.timer.getFrameSynchronizor().eyeFrameToSyncFrame(frame));

                error.setIntervalMarker(intervalMarker);

                // Add marker to the list
                errorSet.addElement(error);
            }
        }
    }

    public void saveErrors(File errorFile) {
        Element root = new Element("root");

//        Enumeration enumeration = errorSet.elements();
//        while (enumeration.hasMoreElements()) {
//            ErrorMarking error = (ErrorMarking) enumeration.nextElement();
//
//            root.addContent(error.toElement());
//        }
        LinkedList<ErrorMarking> list = getCompressedErrorMarkingSet();
        for (Iterator<ErrorMarking> it = list.iterator(); it.hasNext();) {
            ErrorMarking error = it.next();
            root.addContent(error.toElement());
        }

        // Write out to file as xml
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(new Document(root), new FileWriter(errorFile));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void clear() {
        errorSet.clear();
        intervalMarkerManager.clearIntervalMarker();
    }

    public LinkedList<ErrorMarking> getCompressedErrorMarkingSet() {

        LinkedList<ErrorMarking> compressedList = new LinkedList<ErrorMarking>();
        LinkedList<ErrorMarking> tempList = new LinkedList<ErrorMarking>();

        // Go through the set to break it up
        Enumeration enumeration = errorSet.elements();
        while (enumeration.hasMoreElements()) {
            ErrorMarking error = (ErrorMarking) enumeration.nextElement();
            error = (ErrorMarking) error.clone();

            // Check if the error mark is overlap with the previous mark
            boolean notCompleted = true;
            while (!compressedList.isEmpty() && notCompleted) {
                ErrorMarking compressdMark = compressedList.removeLast();

                if (error.startEyeFrame > compressdMark.stopEyeFrame) {
                    // This is a new mark just add it
                    compressedList.add(compressdMark);
                    compressedList.add(error);
                    notCompleted = false;
                } else if (error.stopEyeFrame < compressdMark.startEyeFrame) {
                    // The current mark goes before the current compressed mark
                    // so there is no need to merge

                    // Put compress mark in the temp list
                    tempList.addFirst(compressdMark);
                } else {
                    // There is an overlap try merging

                    // Check if this is the same error type
                    if (error.getErrorCode() == compressdMark.getErrorCode()) {
                        // Simply merge them
                        error.startEyeFrame = Math.min(
                                error.startEyeFrame, compressdMark.startEyeFrame);
                        error.startSceneFrame = Math.min(
                                error.startSceneFrame, compressdMark.startSceneFrame);
                        error.stopEyeFrame = Math.max(
                                error.stopEyeFrame, compressdMark.stopEyeFrame);
                        error.stopSceneFrame = Math.max(
                                error.stopSceneFrame, compressdMark.stopSceneFrame);
                    } else {
                        // Split the tail of uncompressed part if any
                        if (error.stopEyeFrame > compressdMark.stopEyeFrame) {
                            ErrorMarking tail = (ErrorMarking) error.clone();
                            tail.startEyeFrame = compressdMark.stopEyeFrame + 1;
                            tail.startSceneFrame = compressdMark.stopSceneFrame + 1;
                            // Add tail part to temp list
                            tempList.addFirst(tail);
                        }
                        // Split the tail of compressed part if any
                        if (error.stopEyeFrame < compressdMark.stopEyeFrame) {
                            ErrorMarking tail = (ErrorMarking) compressdMark.clone();
                            tail.startEyeFrame = error.stopEyeFrame + 1;
                            tail.startSceneFrame = error.stopSceneFrame + 1;
                            // Add tail part to temp list
                            tempList.addFirst(tail);
                        }

                        // Merge the overlapping part
                        ErrorMarking overlap = (ErrorMarking) compressdMark.clone();
                        overlap.parseErrorCode(
                                error.getErrorCode() |
                                compressdMark.getErrorCode());
                        overlap.startEyeFrame = Math.max(
                                error.startEyeFrame, compressdMark.startEyeFrame);
                        overlap.startSceneFrame = Math.max(
                                error.startSceneFrame, compressdMark.startSceneFrame);
                        overlap.stopEyeFrame = Math.min(
                                error.stopEyeFrame, compressdMark.stopEyeFrame);
                        overlap.stopSceneFrame = Math.min(
                                error.stopSceneFrame, compressdMark.stopSceneFrame);
                        tempList.addFirst(overlap);

                        // Split the head part if any
                        if (error.startEyeFrame < compressdMark.startEyeFrame) {
                            // If the head part if of the error
                            error.stopEyeFrame = compressdMark.startEyeFrame - 1;
                            error.stopSceneFrame = compressdMark.startSceneFrame - 1;
                        } else {
                            if (error.startEyeFrame > compressdMark.startEyeFrame) {
                                // if the head part is of the compressed error
                                compressdMark.stopEyeFrame = error.startEyeFrame - 1;
                                compressdMark.stopSceneFrame = error.startSceneFrame - 1;
                                tempList.addFirst(compressdMark);
                            }
                            // Done
                            notCompleted = false;
                        }
                    }
                }
            }

            if (notCompleted) {
                // Simply add it
                compressedList.addFirst(error);
            }

            // Add temp list to the compress list
            compressedList.addAll(tempList);
            tempList.clear();
        }

        return compressedList;
    }
}
