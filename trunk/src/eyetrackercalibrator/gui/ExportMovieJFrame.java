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
 * ExportMovieJFrame.java
 * @todo add a check box to eliminate frames that are not in pair
 * Created on March 18, 2008, 11:10 AM
 */
package eyetrackercalibrator.gui;

import eyetrackercalibrator.framemanaging.FrameManager;
import eyetrackercalibrator.framemanaging.FrameSynchronizor;
import eyetrackercalibrator.framemanaging.MovieFrameExporter;
import eyetrackercalibrator.framemanaging.ScreenFrameManager;
import eyetrackercalibrator.math.EyeGazeComputing;
import eyetrackercalibrator.util.FFMPEGHandler;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author  ruj
 */
public class ExportMovieJFrame extends javax.swing.JFrame implements PropertyChangeListener {

    public static final String[] SCENE_SOURCE_CHOICE = new String[]{"Small Scene", "Full Scene"};
    private static double CORNER_FRAME_SCALE = 0.3;
    protected static final int TOTAL_TEST_FRAMES = 10;
    MovieFrameExporter movieFrameExporter = null;
    int start;
    int totalProcess;
    int lastFrame;
    private String fullScreenDir;
    boolean isExporting = false;

    /** Creates new form ExportMovieJFrame */
    public ExportMovieJFrame(File currentDir, int exportWidth, int exportHeight,
            EyeGazeComputing eyeGazeComputing, FrameSynchronizor frameSynchronizor,
            FrameManager eyeFrameManager, ScreenFrameManager screenFrameManager,
            String fullScreenDir) {

        initComponents();

        // Set up initial range t export
        int totalExport = frameSynchronizor.getTotalFrame();
        this.lastFrame = totalExport;
        this.toTextField.setText(String.valueOf(totalExport));
        this.start = 1;
        this.totalProcess = totalExport + 1;

        this.progressBar.setStringPainted(true);
        File defaultPath = new File(currentDir.getParentFile(), "MovieFrames");
        this.exportLocationTextField.setText(defaultPath.getAbsolutePath());

        repaint();

        this.fullScreenDir = fullScreenDir;

        // Set up exporter
        this.movieFrameExporter = new MovieFrameExporter(
                eyeGazeComputing, frameSynchronizor,
                eyeFrameManager, screenFrameManager, null,
                Integer.parseInt(this.frameRateTextField.getText()), this);
    }

    protected boolean startMovieExporting(int to, int from) throws HeadlessException {
        File exportDirectory = new File(this.exportLocationTextField.getText());
        // Set up start and end
        this.start = Math.min(to, from);
        this.totalProcess = Math.abs(to - from) + 1;
        this.progressBar.setMaximum(this.totalProcess);

        // Set up calcel button
        this.cancelButton.setText("Cancel");

        // Get number of average frame with sanity check
        String avgStr = this.gazeAverageTextField.getText();
        int averageFrames = 1;
        if (avgStr != null) {
            int v = 1;
            try {
                v = Integer.parseInt(avgStr);
            } catch (NumberFormatException numberFormatException) {
            }
            averageFrames = Math.max(1, v);
            this.gazeAverageTextField.setText(String.valueOf(averageFrames));
            repaint();
        }
boolean useMedianForAverage = false;
useMedianForAverage = useMedian.isSelected();
        if (!(eyeOnlyCheckBox.isSelected() || screenOnlyCheckBox.isSelected()
                || eyeInCornerCheckBox.isSelected() || screenInCornerCheckBox.isSelected()
                || sideBySideCheckBox.isSelected())) {
            // None is selected so warn the user and do nothing
            // Show warning dialog
            JOptionPane.showMessageDialog(this, "Please select at least one export type.",
                    "No Export Type Is Selected", JOptionPane.ERROR_MESSAGE);
            return true;
        }
        boolean createMovieFile = true;
        boolean deleteMoviePictureFile = true;
        if (movieAndFramesRadioButton.isSelected()) {
            deleteMoviePictureFile = false;
        } else if (framesOnlyRadioButton.isSelected()) {
            deleteMoviePictureFile = false;
            createMovieFile = false;
        }
        // Check if ffmpeg is needed
        if (this.movieOnlyRadioButton.isSelected()
                || this.movieAndFramesRadioButton.isSelected()) {
            File ffmpegFile = FFMPEGHandler.getFFMPEGExecutable(this);
            if (ffmpegFile == null) {
                return true;
            }
            this.movieFrameExporter.setFfmpegExecutable(ffmpegFile);
        }
        this.startButton.setEnabled(false);
        this.testButton.setEnabled(false);
        String currentFullScreenDir = null;
        if (this.sceneSourceComboBox.getSelectedIndex() == 1) {            if (this.fullScreenDir.trim().length() > 1) {
                currentFullScreenDir = this.fullScreenDir;
            }
        }

        this.isExporting = true;
        this.movieFrameExporter.exportThread(exportDirectory, currentFullScreenDir,
                Double.parseDouble(this.imageInCornetScaleTextField.getText()) / 100d,
                Double.parseDouble(this.mainImageScaleTextField.getText()) / 100d,
                from, to,
                this.drawCornerCheckBox.isSelected(), this.eyeOnlyCheckBox.isSelected(),
                this.screenOnlyCheckBox.isSelected(), this.sideBySideCheckBox.isSelected(),
                this.eyeInCornerCheckBox.isSelected(), this.screenInCornerCheckBox.isSelected(),
                createMovieFile, deleteMoviePictureFile, averageFrames, useMedianForAverage);
        
        return false;
    }

    /** 
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outputButtonGroup = new javax.swing.ButtonGroup();
        eyetrackercalibrator.gui.util.TextFieldPosIntInputVerifier textFieldPosIntInputVerifier = new eyetrackercalibrator.gui.util.TextFieldPosIntInputVerifier();
        textFieldPosFloatInputVerifier1 = new eyetrackercalibrator.gui.util.TextFieldPosFloatInputVerifier();
        jLabel11 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        exportLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        eyeOnlyCheckBox = new javax.swing.JCheckBox();
        screenOnlyCheckBox = new javax.swing.JCheckBox();
        sideBySideCheckBox = new javax.swing.JCheckBox();
        screenInCornerCheckBox = new javax.swing.JCheckBox();
        eyeInCornerCheckBox = new javax.swing.JCheckBox();
        progressBar = new javax.swing.JProgressBar();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        fromTextField = new javax.swing.JTextField();
        toTextField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        frameRateTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        movieOnlyRadioButton = new javax.swing.JRadioButton();
        movieAndFramesRadioButton = new javax.swing.JRadioButton();
        framesOnlyRadioButton = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        testButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        gazeAverageTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        useMedian = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        drawCornerCheckBox = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        mainImageScaleTextField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel6 = new javax.swing.JPanel();
        imageInCornetScaleTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        sceneSourceComboBox = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();

        jLabel11.setText("jLabel11");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Exporting Movie Frames");
        setResizable(false);

        jLabel1.setText("Export Location:");

        exportLocationTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportLocationTextFieldActionPerformed(evt);
            }
        });

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Export Types"));

        eyeOnlyCheckBox.setText("Eye only");

        screenOnlyCheckBox.setText("Scene only");
        screenOnlyCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                screenOnlyCheckBoxStateChanged(evt);
            }
        });

        sideBySideCheckBox.setSelected(true);
        sideBySideCheckBox.setText("Eye and scene side by side");
        sideBySideCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sideBySideCheckBoxStateChanged(evt);
            }
        });

        screenInCornerCheckBox.setText("Scene in the corner of eye");
        screenInCornerCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                screenInCornerCheckBoxStateChanged(evt);
            }
        });

        eyeInCornerCheckBox.setText("Eye in the corner of scene");
        eyeInCornerCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                eyeInCornerCheckBoxStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(eyeOnlyCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(screenOnlyCheckBox)
                .add(18, 18, 18)
                .add(sideBySideCheckBox))
            .add(jPanel1Layout.createSequentialGroup()
                .add(eyeInCornerCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(screenInCornerCheckBox))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(eyeOnlyCheckBox)
                    .add(screenOnlyCheckBox)
                    .add(sideBySideCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(eyeInCornerCheckBox)
                    .add(screenInCornerCheckBox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setText("From:");

        jLabel3.setText("To:");

        fromTextField.setText("1");
        fromTextField.setInputVerifier(textFieldPosIntInputVerifier);

        toTextField.setText("1");
        toTextField.setInputVerifier(textFieldPosIntInputVerifier);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Output Options"));

        jLabel7.setText("Frame rate:");
        jPanel2.add(jLabel7);

        frameRateTextField.setText("30");
        frameRateTextField.setInputVerifier(textFieldPosFloatInputVerifier1);
        frameRateTextField.setPreferredSize(new java.awt.Dimension(60, 28));
        jPanel2.add(frameRateTextField);

        jLabel6.setText("fps");
        jPanel2.add(jLabel6);

        outputButtonGroup.add(movieOnlyRadioButton);
        movieOnlyRadioButton.setSelected(true);
        movieOnlyRadioButton.setText("Movie only");
        jPanel2.add(movieOnlyRadioButton);

        outputButtonGroup.add(movieAndFramesRadioButton);
        movieAndFramesRadioButton.setText("Movie and frame files");
        jPanel2.add(movieAndFramesRadioButton);

        outputButtonGroup.add(framesOnlyRadioButton);
        framesOnlyRadioButton.setText("Frame files only");
        jPanel2.add(framesOnlyRadioButton);

        testButton.setText("Test");
        testButton.setToolTipText("Try exporting the first 10 frames.");
        testButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testButtonActionPerformed(evt);
            }
        });
        jPanel3.add(testButton);

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        jPanel3.add(startButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel3.add(cancelButton);

        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel4.setText("Running average across");

        gazeAverageTextField.setText("3");
        gazeAverageTextField.setInputVerifier(textFieldPosIntInputVerifier);
        gazeAverageTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gazeAverageTextFieldActionPerformed(evt);
            }
        });

        jLabel5.setText("frames");

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jLabel4)
                .add(6, 6, 6)
                .add(gazeAverageTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel5))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel4)
                .add(gazeAverageTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel5))
        );

        jPanel5.add(jPanel4);

        useMedian.setText("use median");
        jPanel5.add(useMedian);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(6, 25));
        jPanel5.add(jSeparator1);

        drawCornerCheckBox.setText("Draw screen corners in scenes");
        jPanel5.add(drawCornerCheckBox);

        mainImageScaleTextField.setText("100");
        mainImageScaleTextField.setInputVerifier(textFieldPosFloatInputVerifier1);

        jLabel12.setText("%");

        jLabel13.setText("Main image scale");

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(jLabel13)
                .add(2, 2, 2)
                .add(mainImageScaleTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                .add(1, 1, 1)
                .add(jLabel12))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel13)
                .add(mainImageScaleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel12))
        );

        jPanel5.add(jPanel8);

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator3.setPreferredSize(new java.awt.Dimension(6, 25));
        jPanel5.add(jSeparator3);

        imageInCornetScaleTextField.setText("25");
        imageInCornetScaleTextField.setEnabled(false);
        imageInCornetScaleTextField.setInputVerifier(textFieldPosFloatInputVerifier1);
        imageInCornetScaleTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageInCornetScaleTextFieldActionPerformed(evt);
            }
        });

        jLabel8.setLabelFor(imageInCornetScaleTextField);
        jLabel8.setText("Corner image scale");

        jLabel9.setText("% of the main image");
        jLabel9.setToolTipText("This scaling overwrite %scale of scene source when the scene is in the corner.");

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(jLabel8)
                .add(2, 2, 2)
                .add(imageInCornetScaleTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                .add(2, 2, 2)
                .add(jLabel9))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel8)
                .add(imageInCornetScaleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel9))
        );

        jPanel5.add(jPanel6);

        sceneSourceComboBox.setModel(new javax.swing.DefaultComboBoxModel(SCENE_SOURCE_CHOICE));

        jLabel10.setText("Scene Source");

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jLabel10)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sceneSourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(sceneSourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel10))
        );

        jPanel5.add(jPanel7);

        jLabel14.setText("Scene Frames:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel5, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(exportLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel2)
                                    .add(jLabel3))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(toTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                                    .add(fromTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)))
                            .add(layout.createSequentialGroup()
                                .add(32, 32, 32)
                                .add(jLabel14)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(exportLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .add(9, 9, 9)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel14)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(fromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel3)
                            .add(toTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (this.movieFrameExporter != null && this.isExporting) {
            this.movieFrameExporter.cancel();
        }

        if (!this.isExporting) {
            this.dispose();
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed

        // Check if it has focus
        if (!this.startButton.isFocusOwner()) {
            return;
        }

        // Get to and from
        int from = 0, to = 0;
        // There is no need to catch error here coz we do input validation at the textfield
        from = Integer.parseInt(this.fromTextField.getText());
        to = Integer.parseInt(this.toTextField.getText());

        if (to<=from)
        {
            JOptionPane.showMessageDialog(this, "Last frame should be greater than first frame.",
                    "Last frame is too small", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (to>lastFrame)
        {
            JOptionPane.showMessageDialog(this, "Make the last frame smaller than " + lastFrame,
                    "Last frame is too large", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (startMovieExporting(to, from)) {
            return;
        }
    }//GEN-LAST:event_startButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        // Set text box with directory that user chose.
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (this.exportLocationTextField.getText().length() > 1) {
            // Get current selection from text field
            fileChooser.setSelectedFile(new File(exportLocationTextField.getText()));
        } else {
            // Otherwise open to latest location if there is nothing in the field text
            fileChooser.setSelectedFile(new File(System.getProperty("user.home")));
        }

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.exportLocationTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void gazeAverageTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gazeAverageTextFieldActionPerformed
        // Check content
        String input = gazeAverageTextField.getText();
        // Try parsing it
        int value = 3;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException numberFormatException) {
            // This is bad so falls to default
            gazeAverageTextField.setText(String.valueOf(value));
            return;
        }
        // Cap value
        value = Math.max(1, value);

        // Reset output
        gazeAverageTextField.setText(String.valueOf(value));
}//GEN-LAST:event_gazeAverageTextFieldActionPerformed

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButtonActionPerformed
        // Check if it has focus
        if (!this.testButton.isFocusOwner()) {
            return;
        }

        // Get to and from and cap from to the either the last frame possible or the (n-1)th after the first frame
        int from = 0, to = 0;
        // There is no need to catch error here coz we do input validation at the textfield
        from = Integer.parseInt(this.fromTextField.getText());
        to = Math.min(Integer.parseInt(this.toTextField.getText()), from + TOTAL_TEST_FRAMES - 1);
        if (startMovieExporting(to, from)) {
            return;
        }
    }//GEN-LAST:event_testButtonActionPerformed

    private void imageInCornetScaleTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageInCornetScaleTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_imageInCornetScaleTextFieldActionPerformed

    private void screenOnlyCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_screenOnlyCheckBoxStateChanged
        updateSceneSourceScalingEnable();
    }//GEN-LAST:event_screenOnlyCheckBoxStateChanged

    private void sideBySideCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sideBySideCheckBoxStateChanged
        updateSceneSourceScalingEnable();
    }//GEN-LAST:event_sideBySideCheckBoxStateChanged

    private void screenInCornerCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_screenInCornerCheckBoxStateChanged
        updateCornerScaleEnable();
        updateSceneSourceScalingEnable();
    }//GEN-LAST:event_screenInCornerCheckBoxStateChanged

    private void eyeInCornerCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_eyeInCornerCheckBoxStateChanged
        updateCornerScaleEnable();
        updateSceneSourceScalingEnable();
    }//GEN-LAST:event_eyeInCornerCheckBoxStateChanged

    private void exportLocationTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportLocationTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_exportLocationTextFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox drawCornerCheckBox;
    private javax.swing.JTextField exportLocationTextField;
    private javax.swing.JCheckBox eyeInCornerCheckBox;
    private javax.swing.JCheckBox eyeOnlyCheckBox;
    private javax.swing.JTextField frameRateTextField;
    private javax.swing.JRadioButton framesOnlyRadioButton;
    private javax.swing.JTextField fromTextField;
    private javax.swing.JTextField gazeAverageTextField;
    private javax.swing.JTextField imageInCornetScaleTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextField mainImageScaleTextField;
    private javax.swing.JRadioButton movieAndFramesRadioButton;
    private javax.swing.JRadioButton movieOnlyRadioButton;
    private javax.swing.ButtonGroup outputButtonGroup;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JComboBox sceneSourceComboBox;
    private javax.swing.JCheckBox screenInCornerCheckBox;
    private javax.swing.JCheckBox screenOnlyCheckBox;
    private javax.swing.JCheckBox sideBySideCheckBox;
    private javax.swing.JButton startButton;
    private javax.swing.JButton testButton;
    private eyetrackercalibrator.gui.util.TextFieldPosFloatInputVerifier textFieldPosFloatInputVerifier1;
    private javax.swing.JTextField toTextField;
    private javax.swing.JCheckBox useMedian;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        int completed = (Integer) evt.getNewValue();

        if (completed > 0) {
            completed = completed - start + 1;

            if (completed >= this.totalProcess && evt.getPropertyName().startsWith("Completed")) {
                this.progressBar.setString(evt.getPropertyName());
                this.cancelButton.setText("Close");
                this.startButton.setEnabled(true);
                this.testButton.setEnabled(true);
                this.isExporting = false;
            } else {
                this.progressBar.setString(evt.getPropertyName() + " " + completed + " of " + this.totalProcess);
            }
            this.progressBar.setValue(completed);

        } else if (completed < 0) {
            this.progressBar.setString(evt.getPropertyName());
            this.progressBar.setValue(this.totalProcess - 1);
        }

    }

    protected void updateCornerScaleEnable() {
        this.imageInCornetScaleTextField.setEnabled(
                this.eyeInCornerCheckBox.isSelected()
                || this.screenInCornerCheckBox.isSelected());
    }

    protected void updateSceneSourceScalingEnable() {
        boolean enable = (this.screenOnlyCheckBox.isSelected()
                || this.screenInCornerCheckBox.isSelected()
                || this.eyeInCornerCheckBox.isSelected()
                || this.sideBySideCheckBox.isSelected());

        this.mainImageScaleTextField.setEditable(enable);
        this.sceneSourceComboBox.setEnabled(enable);
    }
}
