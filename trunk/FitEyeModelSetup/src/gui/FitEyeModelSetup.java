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
package gui;

import edu.cornell.chew.delaunay.DelaunayTriangulation;
import edu.cornell.chew.delaunay.Pnt;
import edu.cornell.chew.delaunay.Simplex;
import ij.ImagePlus;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;

/*
 * FitEyeModelSetup.java
 *
 * Created on March 17, 2008, 10:08 AM
 */
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import logic.FitEyeModel;
import logic.FittingListener;
import logic.ImageUtils;
import logic.NotHiddenPictureFilter;
import logic.PointToFrameByEstimatedPupilLocation;
import logic.RotatedEllipse2D;
import logic.ThreadedImageProcessor;
import logic.ThreadedImageProcessorListener;
import util.FitEyeModelRunner;
import util.ParameterList;
import util.ParameterList.Entry;
import util.Parameters;
import util.TerminationListener;

/**
 * @todo add tool tip to the slide bar
 * @author  dwyatte, ruj
 */
public class FitEyeModelSetup extends javax.swing.JFrame {

    public static int ESTIMATE_PUPIL_SAMPLING_RATE = 10;
    // path to the eye directory
    File eyeDir;
    // array containing all eye files
    File[] eyeFiles;
    int frameNum = 0;    // we need this to get our processed images   
    ThreadedImageProcessor imgProc;
    // For storing fit eye model system so that we can have auto update
    private FitEyeModelRunner fitEyeModelRunner;    // For managing the list panel
    DefaultListModel configListModel = new DefaultListModel();
    PointToFrameByEstimatedPupilLocation pointToFrameByEstimatedPupilLocation;
    BufferedImage voronoiBufferedImage = null;
    /** For computing voronoi */
    DelaunayTriangulation delaunayTriangulation = null;
    boolean isEyeFittingRunning = false;

    /** Creates new form FitEyeModelSetup */
    public FitEyeModelSetup() {

        initComponents();

        // Create transparent empty buffer for voronoi
        this.voronoiBufferedImage = new BufferedImage(
                this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        clearVoronoiInfo();
        drawAllVoronoi();

        // set up parent handles to subcomponents. they will need this to use
        // getPaintPanel/getImageProcessor to paint or setFrame
        searchSpacePanel1.setParent(this);
        thresholdPanel1.setParent(this);

        this.pointToFrameByEstimatedPupilLocation =
                new PointToFrameByEstimatedPupilLocation();
        this.pointToFrameByEstimatedPupilLocation.addLoadingListener(new ChangeListener() {

            private int progress = 0;

            public void stateChanged(ChangeEvent e) {
                progress++;
                loadingProgress.setMaximum(eyeFiles.length);
                loadingProgress.setValue(progress);
                loadingProgress.setString("Estimating pupil location: " +
                        progress + "/" + eyeFiles.length);
            }
        });

        this.colorSelectionPanel1.addDropperActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (colorSelectionPanel1.isDropperSelected()) {
                    paintPanel1.setColorCaptureListener(colorSelectionPanel1);
                    // Switch to hand icon
                    paintPanel1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    // Remove listener
                    paintPanel1.setColorCaptureListener(null);
                    // Switch to cross hair
                    paintPanel1.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            }
        });

        this.colorSelectionPanel1.addAutoTestModelCheckBoxActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handleAutoTestModelCheckBoxAction();
            }
        });

        this.colorSelectionPanel1.addSliderChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                if (ColorSelectionPanel.SIGMA_SLIDER_NAME.equals(slider.getName()) ||
                        ColorSelectionPanel.SHARPENINGFACTOR_SLIDER_NAME.equals(slider.getName())) {
                    // SEt frame to trigger change
                    setFrame(frameNum);
                }
                triggerAutoFitEyeModelRecompute();
                setHighlight();
                paintPanel1.repaint();
            }
        });

        this.colorSelectionPanel1.addDetectPupilAngleChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                setFrame(frameNum);
                triggerAutoFitEyeModelRecompute();
                setHighlight();
                paintPanel1.repaint();
            }
        });

        this.colorSelectionPanel1.addHighlightCheckBoxActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setHighlight();
                paintPanel1.repaint();
            }
        });

        this.thresholdPanel1.addPupilThreshSliderStateChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                updateEstimateConfigutationPupilLocation();
            }
        });

        final Component parent = this;
        this.thresholdPanel1.addEstimatePupilLocationButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int decision = JOptionPane.showConfirmDialog(parent,
                        "<html><p>Plase make sure that your current search space covers all pupil locations, and\n" +
                        "pupil threshold is set properly.  Otherwise, estimations may not be correct.",
                        "Before you start", JOptionPane.OK_CANCEL_OPTION);

                if (decision == JOptionPane.OK_OPTION) {
                    estimatePupilLocations();
                }
            }
        });

        this.thresholdPanel1.addLoadPupilLocationButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                loadPupilLocations();
            }
        });

        this.paintPanel1.setSearchAreaChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                triggerAutoFitEyeModelRecompute();
            }
        });
    }

    private void estimatePupilLocations() {

        // Ask for a file to save to
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EstimatedPupilLocations.txt"));
        // Guess current dir
        if (this.eyeDir != null) {
            chooser.setCurrentDirectory(this.eyeDir.getParentFile());
        }
        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {

            final File saveFile = chooser.getSelectedFile();

            // Set up progress bar for this
            this.loadingProgress.setValue(0);
            this.loadingProgress.setString("Estimating pupil location: 0/" +
                    this.loadingProgress.getMaximum());

            // Sanity check
            if (eyeFiles == null) {
                return;
            }

            Thread t = new Thread(new Runnable() {

                public void run() {
                    pointToFrameByEstimatedPupilLocation.loadFrames(eyeFiles,
                            thresholdPanel1.getPupilThresh(),
                            paintPanel1.getSearchRect(), ESTIMATE_PUPIL_SAMPLING_RATE);
                    try {
                        pointToFrameByEstimatedPupilLocation.save(saveFile);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(FitEyeModelSetup.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            t.start();
        }
    }

    private void loadPupilLocations() {
        // Ask for a file to save to
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EstimatedPupilLocations.txt"));
        // Guess current dir
        if (this.eyeDir != null) {
            chooser.setCurrentDirectory(this.eyeDir.getParentFile());
        }
        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            try {
                File loadFile = chooser.getSelectedFile();
                this.pointToFrameByEstimatedPupilLocation.loadFromFile(loadFile);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error loading pupil location file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAutoTestModelCheckBoxAction() {
        if (this.colorSelectionPanel1.isAutoTestModelSelected()) {
            this.fitEyeModelRunner = new FitEyeModelRunner(
                    new FittingListener() {

                        public void setFit(RotatedEllipse2D crFit, RotatedEllipse2D pupilFit) {
                            paintPanel1.setCR(crFit);
                            paintPanel1.setPupil(pupilFit);
                            repaint();
                        }
                    });
            // Start one if pass sanity check
            if (eyeFiles != null && eyeFiles.length > frameNum) {
                this.fitEyeModelRunner.setParameters(eyeFiles[frameNum], createParameters());
                // Start the runner
                this.fitEyeModelRunner.start();
            }
        } else {
            // Kill and remove the runner
            if (this.fitEyeModelRunner != null) {
                this.fitEyeModelRunner.kill();
                try {
                    this.fitEyeModelRunner.join();
                } catch (InterruptedException ex) {
                }
                this.fitEyeModelRunner = null;
                paintPanel1.setCR(null);
                paintPanel1.setPupil(null);
            }
        }
    }

    private void triggerAutoFitEyeModelRecompute() {
        FitEyeModelRunner runner = this.fitEyeModelRunner;
        if (runner != null) {
            runner.setParameters(eyeFiles[frameNum], createParameters());
        }
    }

    /**
     * Create parameters by reading from all panels values.
     */
    private Parameters createParameters() {
        Rectangle searchRect = new Rectangle(paintPanel1.getSearchRect());

        Parameters parameters = new Parameters(
                thresholdPanel1.getCRThresh(), thresholdPanel1.getPupilThresh(),
                this.colorSelectionPanel1.getCRGrayValue(),
                this.colorSelectionPanel1.getPupilGrayValue(),
                this.colorSelectionPanel1.getBackgroundGrayValue(),
                searchRect,
                this.colorSelectionPanel1.getSigma(),
                this.colorSelectionPanel1.getSharpeningFactor(),
                this.colorSelectionPanel1.isDetectingPupilAngle());

        return parameters;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        searchSpacePanel1 = new gui.SearchSpacePanel();
        thresholdPanel1 = new gui.ThresholdPanel();
        colorSelectionPanel1 = new gui.ColorSelectionPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        commentTextPane = new javax.swing.JTextPane();
        frameSlider = new javax.swing.JSlider();
        frameLabel = new javax.swing.JLabel();
        frameTextField = new javax.swing.JTextField();
        eyeDirTextField = new javax.swing.JTextField();
        eyeDirLabel = new javax.swing.JLabel();
        loadingProgress = new javax.swing.JProgressBar();
        loadImageButton = new javax.swing.JButton();
        paintPanel1 = new gui.PaintPanel();
        saveSettingButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        loadSettingButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        configList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        runEyeModelFittingButton = new javax.swing.JButton();
        showVoronoiCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        jTabbedPane1.addTab("Search Space", searchSpacePanel1);
        jTabbedPane1.addTab("Thresholds", thresholdPanel1);
        jTabbedPane1.addTab("Eye Model Grey Level", colorSelectionPanel1);

        jScrollPane2.setViewportView(commentTextPane);

        jTabbedPane1.addTab("Comment", jScrollPane2);

        frameSlider.setMaximum(0);
        frameSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                frameSliderStateChanged(evt);
            }
        });
        frameSlider.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                frameSliderKeyPressed(evt);
            }
        });

        frameLabel.setText("Frame"); // NOI18N

        frameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frameTextFieldActionPerformed(evt);
            }
        });

        eyeDirTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eyeDirTextFieldActionPerformed(evt);
            }
        });

        eyeDirLabel.setText("Eye Directory"); // NOI18N

        loadingProgress.setString("");
        loadingProgress.setStringPainted(true);

        loadImageButton.setText("..."); // NOI18N
        loadImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadImageButtonActionPerformed(evt);
            }
        });

        paintPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                paintPanel1MouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout paintPanel1Layout = new org.jdesktop.layout.GroupLayout(paintPanel1);
        paintPanel1.setLayout(paintPanel1Layout);
        paintPanel1Layout.setHorizontalGroup(
            paintPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 570, Short.MAX_VALUE)
        );
        paintPanel1Layout.setVerticalGroup(
            paintPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 576, Short.MAX_VALUE)
        );

        saveSettingButton.setText("Save Settings"); // NOI18N
        saveSettingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSettingButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Image file name"); // NOI18N

        fileNameTextField.setEditable(false);

        loadSettingButton.setText("Load Setting"); // NOI18N
        loadSettingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSettingButtonActionPerformed(evt);
            }
        });

        configList.setModel(configListModel);
        configList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                configListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(configList);

        addButton.setText("+");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("-");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("resources/FitEyeModelSetup"); // NOI18N
        runEyeModelFittingButton.setText(bundle.getString("Run Eye Model Fitting button text")); // NOI18N
        runEyeModelFittingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runEyeModelFittingButtonActionPerformed(evt);
            }
        });

        showVoronoiCheckBox.setText(bundle.getString("Show config bound check box text")); // NOI18N
        showVoronoiCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showVoronoiCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(eyeDirLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(eyeDirTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(loadImageButton))
                    .add(paintPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(frameLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(frameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fileNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 474, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(frameSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 471, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(showVoronoiCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(deleteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 52, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(loadingProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 236, Short.MAX_VALUE)
                        .add(loadSettingButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(saveSettingButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(runEyeModelFittingButton)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {addButton, deleteButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(eyeDirLabel)
                        .add(eyeDirTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(loadImageButton))
                    .add(loadingProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(addButton)
                                    .add(deleteButton))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(showVoronoiCheckBox))
                            .add(layout.createSequentialGroup()
                                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(frameLabel)
                                    .add(frameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel1)
                                    .add(fileNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(frameSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(runEyeModelFittingButton)
                            .add(saveSettingButton)
                            .add(loadSettingButton)))
                    .add(paintPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        showVoronoiCheckBox.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void frameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frameTextFieldActionPerformed
        // grab the frame number and set the slider appropriately
        // the slider action performed will do the necessary updating        
        frameNum = Integer.parseInt(frameTextField.getText());

        // Cap value
        frameNum = Math.min(frameSlider.getMaximum(),
                Math.max(frameNum, frameSlider.getMinimum()));

        frameSlider.setValue(frameNum);
    }//GEN-LAST:event_frameTextFieldActionPerformed

    private void loadImageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadImageButtonActionPerformed
        initProject();
    }//GEN-LAST:event_loadImageButtonActionPerformed

    private void frameSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_frameSliderStateChanged
        // grab the value and load the image 
        frameNum = frameSlider.getValue();
        setFrame(frameNum);
        this.fileNameTextField.setText(eyeFiles[frameNum].getName());

        /**
         * Try setting gray level then if nothing is set then simply signal
         * a change.  We need this because selectGrayLevelFromDistance triggers
         * the change in colorSelection so there is no need to signal it twice
         * by calling colorSelectionHandleSliderStateChange
         */
        selectParametersFromDistance();

        triggerAutoFitEyeModelRecompute();

    }//GEN-LAST:event_frameSliderStateChanged

    private void saveSettingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSettingButtonActionPerformed
        saveSetting();
    }//GEN-LAST:event_saveSettingButtonActionPerformed

    /**
     * Save setting
     * @return null if cancel or unsuccessful. Otherwise return a saved file
     */
    private File saveSetting() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("FitEyeModel.xml"));
        // Guess current dir
        if (this.eyeDir != null) {
            chooser.setCurrentDirectory(this.eyeDir.getParentFile());
        }

        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {

            File saveFile = chooser.getSelectedFile();

            ParameterList parameterList = new ParameterList();

            if (configListModel.isEmpty()) {
                Parameters parameters = createParameters();
                parameterList.add(estimatePupilFromThreshold(this.frameNum),
                        this.eyeFiles[this.frameNum].getName(), parameters);
            } else {
                // If there is configuration then loop through each
                Enumeration elem = this.configListModel.elements();
                while (elem.hasMoreElements()) {
                    ConfigutationInfo info = (ConfigutationInfo) elem.nextElement();

                    Parameters parameters = createParameters();
                    parameters.backgroundGrayValue = info.background;
                    parameters.crGrayValue = info.cr;
                    parameters.pupilGrayValue = info.pupil;
                    parameters.searchArea = info.getSearchArea();
                    parameters.unsharpFactor = info.unsharpFactor;
                    parameters.unsharpRadious = info.unsharpRadious;
                    parameters.detectPupilAngle = info.isDetectingPupilAngle;

                    parameterList.add(info.point, info.frameFileName, parameters);
                }

            }
            // Add comment parameter list
            parameterList.setComment(this.commentTextPane.getText());
            try {
                // Save parameter list
                parameterList.save(saveFile);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex, "Error saving setting",
                        JOptionPane.ERROR_MESSAGE);
            }

            try {
                this.imgProc.save(chooser.getSelectedFile().getParentFile());
            } catch (IOException ex) {
                Logger.getLogger(FitEyeModelSetup.class.getName()).log(Level.SEVERE, null, ex);
            }

            return saveFile;
        }

        return null;
    }

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        // Give warning when threshold is dirty
        if (this.thresholdPanel1.isDirty()) {
            JOptionPane.showMessageDialog(this,
                    "Threshold values have been changed.  The changes \n" +
                    "may invalidate the estimated pupil locations.  \n" +
                    "You may need to restimate the locations or load \n" +
                    "another appropriate estimation.",
                    "Pupil locations may be invalid!", JOptionPane.WARNING_MESSAGE);
            this.thresholdPanel1.setDirty(false);
        }

        if (this.jTabbedPane1.getSelectedComponent().equals(this.colorSelectionPanel1)) {
            // Change paint to picture so that we can select color
            this.thresholdPanel1.setNoThreshold();
            // repaint image with new info
            setFrame(getFrame());

            this.paintPanel1.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            // Make sure that all droppers are off
            this.colorSelectionPanel1.unselectAllDropperButtons();
            if (this.jTabbedPane1.getSelectedComponent().equals(this.searchSpacePanel1)) {
                this.paintPanel1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } else {
                this.paintPanel1.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }

        }
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void loadSettingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSettingButtonActionPerformed
        // Switch to threshold tab.
        this.jTabbedPane1.setSelectedComponent(this.thresholdPanel1);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // Guess current dir
        if (this.eyeDir != null) {
            chooser.setCurrentDirectory(this.eyeDir.getParentFile());
        }

        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            ParameterList parameterList =
                    ParameterList.load(chooser.getSelectedFile());

            // Get comment
            this.commentTextPane.setText(parameterList.getComment());

            // Set up initial parameters
            Parameters parameters = parameterList.getFirstParameters();
            if (parameters != null) {
                // Set all parameters accordingly
                this.paintPanel1.setSearchRect(parameters.searchArea);
                this.searchSpacePanel1.setArea(parameters.searchArea.width,
                        parameters.searchArea.height);
                this.colorSelectionPanel1.setBackgroundGrayValue(parameters.backgroundGrayValue);
                this.colorSelectionPanel1.setPupilGrayValue(parameters.pupilGrayValue);
                this.colorSelectionPanel1.setCRGrayValue(parameters.crGrayValue);
                this.colorSelectionPanel1.setSharpeningFactor(parameters.unsharpFactor);
                this.colorSelectionPanel1.setSigma(parameters.unsharpRadious);

                this.thresholdPanel1.setCrThresh(parameters.crThreshold);
                this.thresholdPanel1.setPupilThresh(parameters.pupilThreshold);
            }

            // Clear current model
            this.configListModel.clear();

            for (Iterator<Entry> it = parameterList.iterator(); it.hasNext();) {
                Entry entry = it.next();

                ConfigutationInfo info = new ConfigutationInfo();
                info.frameFileName = entry.filename;
                info.background = entry.parameters.backgroundGrayValue;
                info.cr = entry.parameters.crGrayValue;
                info.pupil = entry.parameters.pupilGrayValue;
                info.unsharpFactor = entry.parameters.unsharpFactor;
                info.unsharpRadious = entry.parameters.unsharpRadious;
                info.isDetectingPupilAngle = entry.parameters.detectPupilAngle;

                info.setSearchArea(entry.parameters.searchArea);

                // Locate file number for for file name
                if (eyeFiles != null) {
                    File target = new File(eyeFiles[0].getParentFile(),
                            entry.filename);
                    int pos = Arrays.binarySearch(eyeFiles, target);
                    if (pos >= 0) {
                        // Found in list so we add the info to list
                        info.frameNum = pos;
                        this.configListModel.addElement(info);
                    }

                }
            }
            // Update the pupil position in configuration list
            updateEstimateConfigutationPupilLocation();

            // Make system switch to proper config
            selectParametersFromDistance();

            // Load min,max,avg pics is any
            this.imgProc.load(chooser.getSelectedFile().getParentFile());

            repaint();

        }
    }//GEN-LAST:event_loadSettingButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        // Remove entries from list
        Object[] selected = configList.getSelectedValues();

        for (int i = 0; i <
                selected.length; i++) {
            configListModel.removeElement(selected[i]);
        }

        reloadVoronoiSources();
        drawAllVoronoi();

        repaint();
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Construct the config
        ConfigutationInfo grayLevelInfo = new ConfigutationInfo();
        grayLevelInfo.background =
                this.colorSelectionPanel1.getBackgroundGrayValue();
        grayLevelInfo.cr = this.colorSelectionPanel1.getCRGrayValue();
        grayLevelInfo.pupil = this.colorSelectionPanel1.getPupilGrayValue();
        grayLevelInfo.frameFileName = this.fileNameTextField.getText();
        grayLevelInfo.unsharpFactor = this.colorSelectionPanel1.getSharpeningFactor();
        grayLevelInfo.unsharpRadious = this.colorSelectionPanel1.getSigma();
        grayLevelInfo.isDetectingPupilAngle = this.colorSelectionPanel1.isDetectingPupilAngle();

        grayLevelInfo.frameNum = this.frameNum;
        grayLevelInfo.point = estimatePupilFromThreshold(this.frameNum);
        // Need to use set method here to make sure that the value saved will not change
        grayLevelInfo.setSearchArea(paintPanel1.searchRect);

        if (grayLevelInfo.point != null) {
            // Search if already have the info in the list
            int index = this.configListModel.indexOf(grayLevelInfo);

            if (index < 0) {
                // Add new
                this.configListModel.addElement(grayLevelInfo);
                // Add a voronoi site
                addVoronoiSource(grayLevelInfo.point);
            } else {
                // Replace old one
                ConfigutationInfo oldInfo =
                        (ConfigutationInfo) this.configListModel.set(index, grayLevelInfo);

                // Check if the point changes (Make sure we add info first)
                if (!oldInfo.point.equals(grayLevelInfo.point)) {
                    // Reload all voronoi info if the point changes
                    reloadVoronoiSources();
                }

            }

            drawAllVoronoi();
            repaint();

        } else {
            // Give warning that threshold is not set properly
            JOptionPane.showMessageDialog(this,
                    "Cannot estimate pupil location.  Please make adjustment to pupil threshold.",
                    "Cannot add color config", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void configListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_configListMouseClicked
        // Get selected gray level
        ConfigutationInfo grayLevelInfo = (ConfigutationInfo) configList.getSelectedValue();
        if (grayLevelInfo != null) {
            // Set current parameters to the selected one
            frameSlider.setValue(grayLevelInfo.frameNum);
            colorSelectionPanel1.setBackgroundGrayValue(grayLevelInfo.background);
            colorSelectionPanel1.setCRGrayValue(grayLevelInfo.cr);
            colorSelectionPanel1.setPupilGrayValue(grayLevelInfo.pupil);
            colorSelectionPanel1.setDetectPupilAngle(grayLevelInfo.isDetectingPupilAngle);
        }
    }//GEN-LAST:event_configListMouseClicked

    private void runEyeModelFittingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runEyeModelFittingButtonActionPerformed

        if (this.isEyeFittingRunning) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Would you like to terminate eye model fitting? " +
                    "Termination may take sometime to complete.",
                    "Abort Eye Model Fitting",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // Stop the running through the flag
                this.isEyeFittingRunning = false;
            }

        } else {

            // Save current setting
            final File saveFile = saveSetting();
            if (saveFile != null) {
                final RunEyeModelFittingJDialog d = new RunEyeModelFittingJDialog(this, true);
                d.setTitle("Eye Model Running Parameters");
                d.setMaximumCPU(Runtime.getRuntime().availableProcessors());
                d.setOutputDir(new File(saveFile.getParent(), "Gaze"));
                d.addRunConfirmedListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        final java.util.ResourceBundle bundle =
                                java.util.ResourceBundle.getBundle("resources/FitEyeModelSetup");

                        // Set the flag that eye fitting is running
                        isEyeFittingRunning =
                                true;

                        // Change text to "stop running"
                        runEyeModelFittingButton.setText(
                                bundle.getString("Stop Eye Model Fitting button text"));

                        Thread t = new Thread(new Runnable() {

                            public void run() {
                                runEyeModelFitting(new File(d.getOutputDir()),
                                        d.getNumberCPU(), saveFile, d.isRedo());

                                // Change the flag that eye fitting is running
                                isEyeFittingRunning =
                                        false;

                                // Change text to normal "Run" button
                                runEyeModelFittingButton.setText(
                                        bundle.getString("Run Eye Model Fitting button text"));

                            }
                        });

                        t.start();
                        d.dispose();
                    }
                });

                d.setVisible(true);
            }

        }
    }//GEN-LAST:event_runEyeModelFittingButtonActionPerformed

    private void paintPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paintPanel1MouseClicked
        // Do not move frame when dropper is activated
        if (!colorSelectionPanel1.isDropperSelected()) {
            int frame = this.pointToFrameByEstimatedPupilLocation.getNearestFrame(
                    this.frameNum, evt.getPoint());

            if (frame >= 0) {
                this.frameSlider.setValue(frame);
            }

        }
    }//GEN-LAST:event_paintPanel1MouseClicked

    private void frameSliderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_frameSliderKeyPressed
        int step = 1;
        // Set up modifier
        switch (evt.getModifiers()) {
            case KeyEvent.SHIFT_MASK:
                step = 1000;
                break;

            case KeyEvent.CTRL_MASK:
                step = 100;
                break;

            case KeyEvent.ALT_MASK:
                step = 10;
                break;

            case KeyEvent.META_MASK:
                step = 5;
                break;

            default:

                return;
        }
        // Move according to arrow button
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                this.frameSlider.setValue(this.frameSlider.getValue() - step);
                break;

            case KeyEvent.VK_RIGHT:
                this.frameSlider.setValue(this.frameSlider.getValue() + step);
                break;

        }
    }//GEN-LAST:event_frameSliderKeyPressed

    private void showVoronoiCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showVoronoiCheckBoxActionPerformed
        if (this.showVoronoiCheckBox.isSelected()) {
            // Add voronoi to picture
            this.paintPanel1.addHighlight(this.voronoiBufferedImage);
        } else {
            this.paintPanel1.removeHighlight(this.voronoiBufferedImage);
        }

        repaint();
    }//GEN-LAST:event_showVoronoiCheckBoxActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        saveSetting();
    }//GEN-LAST:event_formWindowClosing

    private void eyeDirTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eyeDirTextFieldActionPerformed
        initProject();
    }//GEN-LAST:event_eyeDirTextFieldActionPerformed

    private void initProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Clear current frame by pupil location lookup
        this.pointToFrameByEstimatedPupilLocation.clear();

        // Get starting dir from the text box
        if (this.eyeDirTextField.getText().length() > 0) {
            chooser.setCurrentDirectory(new File(this.eyeDirTextField.getText()));
        }

        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // list the eye files and set the text field to reflect their directory
            eyeDir = chooser.getSelectedFile();
            eyeDirTextField.setText(eyeDir.getAbsolutePath());

            if (!eyeDir.exists()) {
                // Give warning when file not exists
                JOptionPane.showMessageDialog(this,
                        eyeDirTextField.getText() + " directory does not exists, please select a directory",
                        "Error accessing the direcory", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // list the eye files and set the text field to reflect their directory
            eyeFiles = eyeDir.listFiles(new NotHiddenPictureFilter());

            // Set correct total images
            this.frameSlider.setMaximum(eyeFiles.length - 1);

            imgProc = new ThreadedImageProcessor(new ThreadedImageProcessorListener() {

                public void progress(int progress) {
                    loadingProgress.setMaximum(eyeFiles.length);
                    loadingProgress.setValue(progress);
                    loadingProgress.setString("Computing min, max & avg " + progress + "/" + eyeFiles.length);
                    repaint();
                }

                public void complete() {
                    loadingProgress.setString("Computing min, max & avg is completed");
                    searchSpacePanel1.enableComputeMinMaxAvg();
                    repaint();
                }
            });
        }


    }

    /** This method start min max avg image computation */
    public void startMinMaxAverageImageComputation() {
        // Get starting dir from the text box
        try {
            // start processing images
            imgProc.initialize(eyeFiles);
            Thread imgProcThread = new Thread(imgProc);
            imgProcThread.start();

            // load the first image for display
            frameNum = 0;
            frameSlider.setValue(frameNum);
            this.fileNameTextField.setText(eyeFiles[0].getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** This method stop min max avg image computation */
    public void stopMinMaxAverageImageComputation() {
        imgProc.kill();
    }

    /**
     * Warning!! This is a blocking method.  It will compute FitEyeModel for all
     * files
     * @param redo If true will redo all frames. Otherwise if output file exists
     * no model fitting will be performed.
     */
    private void runEyeModelFitting(File outputDir, int numCPUs, File saveFile,
            boolean redo) {
        // Cap number of cpu to the job available
        numCPUs = Math.min(eyeFiles.length, numCPUs);
        final Semaphore sem = new Semaphore(0);
        ParameterList parameters = ParameterList.load(saveFile);
        // Clear out loading progress value
        this.loadingProgress.setValue(0);

        TerminationListener terminationListener = new TerminationListener() {

            synchronized public void complete() {
                sem.release();
            }
        };

        int curEyeFile = 0;
        // spawn numCPUs-1 FitEyeModel threads
        for (int i = 0; i < numCPUs && this.isEyeFittingRunning; i++) {
            FitEyeModel fem = null;

            fem = new FitEyeModel(
                    this.eyeFiles[curEyeFile], outputDir.getAbsolutePath(), parameters);
            fem.setTerminationListener(terminationListener);

            Thread threads = new Thread(fem, "Eye fitting " + curEyeFile);
            threads.start();
            curEyeFile++;
        }
        // monitor threads, and regenerate dead ones
        while (curEyeFile < eyeFiles.length && this.isEyeFittingRunning) {
            try {
                // Check if there is available thread
                sem.acquire();
            } catch (InterruptedException ex) {
                // Abort when interrupted
                return;
            }
            FitEyeModel fem = null;
            boolean toRun = true;
            if (!redo) {
                // Check if output file already exists
                toRun = !FitEyeModel.isOutputFileExisting(
                        this.eyeFiles[curEyeFile], outputDir);
            }

            if (toRun) {
                fem = new FitEyeModel(
                        this.eyeFiles[curEyeFile], outputDir.getAbsolutePath(), parameters);
                fem.setTerminationListener(terminationListener);

                Thread threads = new Thread(fem, "Eye fitting " + curEyeFile);
                threads.start();
            } else {
                sem.release();
            }

            // Increment the counter
            curEyeFile++;

            loadingProgress.setMaximum(eyeFiles.length);
            loadingProgress.setValue(curEyeFile);
            loadingProgress.setString("Fitting eye model: " + curEyeFile + "/" + eyeFiles.length);

        }
        // wait for remaining threads to die before exiting
        for (int i = 0; i < numCPUs; i++) {
            try {
                sem.acquire();
            } catch (InterruptedException ex) {
                // Abort when interrupted
                return;
            }
        }
        this.loadingProgress.setString("Fitting eye model is completed.");
    }

    // subcomponenets need to know what to paint to
    public PaintPanel getPaintPanel() {
        return paintPanel1;
    }

    // searchSpacePanel needs to use the ImageProcessor to get min/max/avg images
    public ThreadedImageProcessor getImageProcessor() {
        return imgProc;
    }

    public int getFrame() {
        return frameNum;
    }

    // this should get called whenever the frame changes from the frame slider
    // gets called from slider statechanged as well as from other classes
    public void setFrame(int newFrameNumber) {
        // Sanity check and cap
        if (eyeFiles != null) {
            newFrameNumber = Math.min(eyeFiles.length - 1, newFrameNumber);
            newFrameNumber = Math.max(0, newFrameNumber);

            this.frameNum = newFrameNumber;
            // first disable any modifiers from search space panels
            searchSpacePanel1.setDefaultButton();
            frameTextField.setText(new Integer(newFrameNumber).toString());

            try {
                BufferedImage paintedImg = null;

                // Load image
                paintedImg = ImageUtils.loadImage(eyeFiles[newFrameNumber]);

                if (this.colorSelectionPanel1.getSigma() > 0) {
                    // Avoid avoid loading image from image plus directly since
                    // it cannot load some tiff compression

                    // Limit sharpen to the search space area to increase the speed
                    Rectangle r = this.paintPanel1.searchRect;
                    BufferedImage img = paintedImg.getSubimage(r.x, r.y, r.width, r.height);

                    ImagePlus imagePlus = new ImagePlus("", img);

                    ImageUtils.unsharpMask(imagePlus.getProcessor(),
                            this.colorSelectionPanel1.getSigma(),
                            this.colorSelectionPanel1.getSharpeningFactor());


                    paintedImg.getGraphics().drawImage(
                            ImageUtils.toBufferedImage(imagePlus.getImage()),
                            r.x, r.y, null);
                }
                // user should be able to scroll through sequence with threshold on,
                // so check to see what kind of threshold is set in the thresh panel
                switch (thresholdPanel1.getThresholdType()) {
                    case NO_THRESH_TYPE:
                        paintPanel1.setImage(paintedImg);
                        setHighlight();
                        break;
                    case CR_THRESH_TYPE:
                        // find the cr
                        RotatedEllipse2D foundCR = FitEyeModel.findCR(paintedImg,
                                this.paintPanel1.searchRect,
                                this.thresholdPanel1.getCRThresh());

                        // draw into the image
                        paintFoundEllisp(foundCR, paintedImg,
                                this.thresholdPanel1.getCRThresh());

                        break;
                    case PUPIL_THRESH_TYPE:

                        RotatedEllipse2D foundPupil = FitEyeModel.findPupil(paintedImg,
                                this.paintPanel1.searchRect,
                                this.thresholdPanel1.getPupilThresh(),
                                false);

                        // draw into the image
                        paintFoundEllisp(foundPupil, paintedImg,
                                this.thresholdPanel1.getPupilThresh());
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method help with setting highlight
     */
    private void setHighlight() {
        paintPanel1.clearHighlight();
        if (this.showVoronoiCheckBox.isSelected()) {
            paintPanel1.addHighlight(this.voronoiBufferedImage);
        }
        BufferedImage img = paintPanel1.getImage();
        if (img != null) {
            int[] pixels = logic.ImageUtils.RGBtoGray(ImageUtils.getPixels(img));
            if (this.colorSelectionPanel1.isCRColorHeighlighted()) {
                logic.ImageUtils.grayToRGB(pixels);

                int limit = (this.colorSelectionPanel1.getCRGrayValue() +
                        this.colorSelectionPanel1.getBackgroundGrayValue()) / 2;

                int[] highPix = ImageUtils.threshold(pixels, limit, 255,
                        ImageUtils.createARGB(125, 255, 0, 0));

                BufferedImage highLight = new BufferedImage(img.getWidth(),
                        img.getHeight(), BufferedImage.TYPE_INT_ARGB);
                highLight.setRGB(0, 0, img.getWidth(), img.getHeight(), highPix,
                        0, img.getWidth());
                this.paintPanel1.addHighlight(highLight);
            }

            if (this.colorSelectionPanel1.isPupilColorHeighlighted()) {
                logic.ImageUtils.grayToRGB(pixels);

                int limit = (this.colorSelectionPanel1.getPupilGrayValue() +
                        this.colorSelectionPanel1.getBackgroundGrayValue()) / 2;

                int[] highPix = ImageUtils.threshold(pixels, 0, limit,
                        ImageUtils.createARGB(125, 0, 255, 0));

                BufferedImage highLight = new BufferedImage(img.getWidth(),
                        img.getHeight(), BufferedImage.TYPE_INT_ARGB);

                highLight.setRGB(0, 0, img.getWidth(), img.getHeight(), highPix,
                        0, img.getWidth());

                this.paintPanel1.addHighlight(highLight);
            }
        }
    }

    /**
     * Internal helper for painting found pupil or cr thresholding result
     */
    private void paintFoundEllisp(RotatedEllipse2D found,
            BufferedImage paintedImg, int threshold) {
        Graphics paintedImgGraphics;
        int[] paintedImgPixels = logic.ImageUtils.RGBtoGray(
                logic.ImageUtils.getPixels(paintedImg));
        paintedImgPixels = logic.ImageUtils.threshold(
                paintedImgPixels, threshold);
        paintedImg.setRGB(0, 0, paintedImg.getWidth(), paintedImg.getHeight(),
                logic.ImageUtils.grayToRGB(paintedImgPixels), 0,
                paintedImg.getWidth());
        // now draw the CR
        paintedImgGraphics = paintedImg.getGraphics();
        paintedImgGraphics.setColor(Color.RED);

        Graphics2D g2d = (Graphics2D) paintedImgGraphics;

        AffineTransform oldTransform = g2d.getTransform();

        g2d.setTransform(AffineTransform.getRotateInstance(
                found.getAngle(), found.getCenterX(), found.getCenterY()));

        g2d.draw(found);

        g2d.drawLine((int) found.getX(), (int) found.getCenterY(),
                (int) found.getMaxX(), (int) found.getCenterY());

        g2d.setTransform(oldTransform);

        paintPanel1.setImage(paintedImg);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new FitEyeModelSetup().setVisible(true);
            }
        });
    }

    /**
     * @return null when there is error with file
     */
    private Point2D.Double estimatePupilFromThreshold(int frameNum) {
        Point2D.Double pupil = null;

        BufferedImage paintedImg = ImageUtils.loadImage(eyeFiles[frameNum]);

        if(paintedImg != null){

        Rectangle searchArea = null;

        if (!this.configListModel.isEmpty()) {
            // Try getting search rect from the first stored config
            ConfigutationInfo info =
                    (ConfigutationInfo) this.configListModel.firstElement();
            searchArea = info.getSearchArea();
        } else {
            searchArea = this.paintPanel1.getSearchRect();
        }
        // Get pupil estimate
        Ellipse2D foundPupil = FitEyeModel.findPupil(paintedImg,
                searchArea, this.thresholdPanel1.getPupilThresh(),
                false);

        pupil = new Point2D.Double(foundPupil.getCenterX(),
                foundPupil.getCenterY());
        }
        
        return pupil;
    }

    private void updateEstimateConfigutationPupilLocation() {
        for (Enumeration elem = this.configListModel.elements();
                elem.hasMoreElements();) {
            ConfigutationInfo info = (ConfigutationInfo) elem.nextElement();
            Double point = estimatePupilFromThreshold(info.frameNum);
            if (point != null) {
                info.point = point;
            } else {
                // Give warning that threshold is not set properly
                JOptionPane.showMessageDialog(this,
                        "Cannot estimate pupil location for one of the configuration.  Please make adjustment to pupil threshold.",
                        "Configuration point becomes invalid", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        reloadVoronoiSources();
        drawAllVoronoi();
    }

    /**
     * This method uses threshold to approximate current pupil place. Then
     * compute distance from the current spot to all gray level configurations.
     * The panel is then set to the configuration which is closest to it.
     * Currently the configuration includes gray levels and search area
     * @return true when there is a change else false
     */
    private boolean selectParametersFromDistance() {

        // Check if we have gray level configuration
        if (!this.configListModel.isEmpty()) {
            // Estimate pupil location from Threshold
            Point2D.Double pupil = estimatePupilFromThreshold(this.frameNum);

            if (pupil != null) {
                // Find the configuration which is closest to us
                Enumeration elem = this.configListModel.elements();
                // Get the first config
                ConfigutationInfo currentConfig =
                        (ConfigutationInfo) elem.nextElement();
                double distance = pupil.distance(currentConfig.point);

                // Keep finding a closer one
                while (elem.hasMoreElements()) {
                    ConfigutationInfo info = (ConfigutationInfo) elem.nextElement();

                    double newDistance = pupil.distance(info.point);

                    if (newDistance < distance) {
                        // Take a new closer one
                        distance = newDistance;
                        currentConfig = info;
                    }
                }

                // Set the current gray level to whatever we get
                this.colorSelectionPanel1.setBackgroundGrayValue(
                        currentConfig.background);
                this.colorSelectionPanel1.setCRGrayValue(currentConfig.cr);
                this.colorSelectionPanel1.setPupilGrayValue(currentConfig.pupil);

                // Set unsharpen
                this.colorSelectionPanel1.setSigma(currentConfig.unsharpRadious);
                this.colorSelectionPanel1.setSharpeningFactor(currentConfig.unsharpFactor);

                // Set search space
                this.paintPanel1.setSearchRect(currentConfig.getSearchArea());
                this.searchSpacePanel1.setArea(
                        currentConfig.getSearchArea().width,
                        currentConfig.getSearchArea().height);

                // Set whether we have to use angle detection
                this.colorSelectionPanel1.setDetectPupilAngle(
                        currentConfig.isDetectingPupilAngle);

                //Mark the config that we pick
                this.configList.setSelectedValue(currentConfig, true);
                return true;
            }
        }

        return false;
    }

    /** Clear all voronoi information */
    private void clearVoronoiInfo() {
        // Init voronoid computation
        int size = Math.max(this.paintPanel1.getWidth(),
                this.paintPanel1.getHeight()) * 3;
        Simplex<Pnt> initialTriangle = new Simplex<Pnt>(
                new Pnt(-size, -size),
                new Pnt(size, -size),
                new Pnt(0, size));

        this.delaunayTriangulation = new DelaunayTriangulation(initialTriangle);
    }

    private void drawAllVoronoi() {
        Graphics2D g = this.voronoiBufferedImage.createGraphics();

        // Clear out old picture
        g.setBackground(new Color(255, 255, 255, 0));
        g.clearRect(0, 0, this.voronoiBufferedImage.getWidth(),
                this.voronoiBufferedImage.getWidth());

        g.setColor(Color.CYAN);
        // Loop through all the edges of the DT (each is done twice)
        for (Simplex<Pnt> triangle : this.delaunayTriangulation) {
            for (Simplex<Pnt> other : this.delaunayTriangulation.neighbors(triangle)) {
                Pnt p = Pnt.circumcenter(triangle.toArray(new Pnt[0]));
                Pnt q = Pnt.circumcenter(other.toArray(new Pnt[0]));
                int px = (int) p.coord(0);
                int py = (int) p.coord(1);
                int qx = (int) q.coord(0);
                int qy = (int) q.coord(1);
                g.drawLine(px, py, qx, qy);
            }
        }
        g.dispose();
    }

    private void addVoronoiSource(Point2D p) {
        Pnt site = new Pnt(p.getX(), p.getY());
        this.delaunayTriangulation.delaunayPlace(site);
    }

    private void reloadVoronoiSources() {
        clearVoronoiInfo();

        for (Enumeration elem = this.configListModel.elements();
                elem.hasMoreElements();) {
            ConfigutationInfo info = (ConfigutationInfo) elem.nextElement();
            addVoronoiSource(info.point);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private gui.ColorSelectionPanel colorSelectionPanel1;
    private javax.swing.JTextPane commentTextPane;
    private javax.swing.JList configList;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel eyeDirLabel;
    private javax.swing.JTextField eyeDirTextField;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JLabel frameLabel;
    private javax.swing.JSlider frameSlider;
    private javax.swing.JTextField frameTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton loadImageButton;
    private javax.swing.JButton loadSettingButton;
    private javax.swing.JProgressBar loadingProgress;
    private gui.PaintPanel paintPanel1;
    private javax.swing.JButton runEyeModelFittingButton;
    private javax.swing.JButton saveSettingButton;
    private gui.SearchSpacePanel searchSpacePanel1;
    private javax.swing.JCheckBox showVoronoiCheckBox;
    private gui.ThresholdPanel thresholdPanel1;
    // End of variables declaration//GEN-END:variables
}
