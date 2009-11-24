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
 * CornerSelector.java
 *
 * Created on November 5, 2008, 3:31 PM
 */
package cornerselector;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * For giving where to search for monitor's corners in scenes.  The class provides
 * GUI for users to give hints for each scene.  The scenes will be shown in
 * sampled manner over all scenes as specify by frame rate (1 means show all scene)
 * If the user closes the GUI by clicking on close window or Cancle button,
 * termination state (getTerminationCause) will be TerminationCause.CANCEL.
 * Otherwise it will be TerminationCause.DONE.
 *
 * By default if setCornerHintsDirectory is not called a program will create
 * a temporary corner-hint directory at the same location as the configuration
 * file specified by the user. The temporary directory can be known through
 * getCornerHintsDirectory after user save the configuration.
 * @author  ruj
 */
public class CornerSelector extends javax.swing.JFrame {

    public enum TerminationCause {

        /** The user completed the hint */
        COMPLETED,
        /** The user canceled the operation */
        CANCEL
    }

    class FrameRateInputVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent input) {
            JTextField textField = (JTextField) input;
            String s = textField.getText();
            try {
                int v = Integer.parseInt(s.trim());
                if (v < 1) {
                    return false;
                }
            } catch (NumberFormatException numberFormatException) {
                return false;
            }

            return true;
        }
    }
    private TerminationCause terminationCause = TerminationCause.CANCEL;
    public static final int NOT_CODED = -666;    // enumeration of directions for handleEvent
    /** Top Left Index */
    public static final int TL = 0;
    /** Top Right Index */
    public static final int TR = 1;
    /** Bottom Right Index */
    public static final int BR = 2;
    /** Bottom Leftt Index */
    public static final int BL = 3;
    /** Default config file name */
    protected ImageLoadingThread imageLoadingThread;
    protected File[] files;
    protected Point[][] corners;
    protected File cornerHintsDirectory = null;

    public File getCornerHintsDirectory() {
        return cornerHintsDirectory;
    }

    /**
     * @return the terminationCause
     */
    public TerminationCause getTerminationCause() {
        return terminationCause;
    }

    /** 
     * Creates new form CornerSelector
     * @param fileList Array of files to be shown in GUI.
     * @param cornerHintsDirectory If null, no old configuration will be loaded and output dir will be asked when saved
     * @param frameRate Frame rate e.g. 10 means shows every 10th frames
     */
    public CornerSelector(File[] fileList, File cornerHintsDirectory, int frameRate) {
        setLocationByPlatform(true);
        initComponents();

        this.cornerHintsDirectory = cornerHintsDirectory;

        this.imageLoadingThread = new ImageLoadingThread(this.cornerSelectLabel);
        this.imageLoadingThread.start();

        this.frameScrollingJPanel.setTotalFrame(fileList.length);
        this.frameScrollingJPanel.setFrameRate(frameRate);

        files = fileList;
        if (fileList == null) {
            throw new RuntimeException("File list cannot be null");
        }

        // Create empty corners by default
        createCorners(fileList.length);
        // Loading pervious data if any
        loadCorners(frameRate);
        this.reviewToggleButton.doClick();

        this.imageLoadingThread.setFileToOpen(fileList[0],
                corners[TL][0], corners[TR][0], corners[BL][0], corners[BR][0]);

        /** Add change listener to scroller */
        this.frameScrollingJPanel.addPropertyChangeListener(
                FrameScrollingJPanel.CURRENT_FRAME_CHANGE,
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        Integer nf = (Integer) evt.getNewValue();
                        Integer of = (Integer) evt.getOldValue();
                        if (nf != null || of != null) {
                            frameChangeHandler(of - 1, nf - 1);
                        }
                    }
                });
    }

    /** 
     * Set the size of the corner detection.  The box size will be computed by
     * (2 * size + 1)
     * @param size Positive integer to be used to compute the size.
     */
    public void setCornerBoxSize(int size) {
        this.cornerSelectLabel.setSelectSize(size);
    }

    public int getCornerBoxSize() {
        return this.cornerSelectLabel.getSelectSize();
    }

    public void setFirstFrameNumber(int firstFrameNumber) {
        this.frameScrollingJPanel.setFirstFrameNumber(firstFrameNumber);
        // This current frame is relative to the file.  So 1 = firstFrameNumber
        this.frameScrollingJPanel.setCurrentFrame(1);
    }

    protected void frameChangeHandler(int oldFrame, int newFrame) {
        if (newFrame >= 0 && newFrame < files.length) {
            this.imageLoadingThread.setFileToOpen(files[newFrame],
                    corners[TL][newFrame], corners[TR][newFrame],
                    corners[BL][newFrame], corners[BR][newFrame]);
        }
        if (oldFrame >= 0 && oldFrame < files.length) {
            Point p = this.cornerSelectLabel.getMousePosition();
            if (!this.reviewToggleButton.isSelected() && p != null) {
                int cornerPos = 0;
                if (this.topleftRadioButton.isSelected()) {
                    cornerPos = TL;
                }
                if (this.topRightRadioButton.isSelected()) {
                    cornerPos = TR;
                }
                if (this.bottomRightRadioButton.isSelected()) {
                    cornerPos = BR;
                }
                if (this.bottomLeftRadioButton.isSelected()) {
                    cornerPos = BL;
                }

                /** Change all from this frame to the end of sampling */
                int skipRange = this.frameScrollingJPanel.getFrameRate();
                for (int i = 0; i < skipRange && (oldFrame + i) < files.length; i++) {
                    corners[cornerPos][oldFrame + i] = p;
                }
            }
        }
    }

    private void save() {
        // Ask user where to save it if not specified earlier.
        if (this.cornerHintsDirectory == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                // Set saving dir
                this.cornerHintsDirectory = chooser.getSelectedFile();
            } else {
                // Do nothing
                return;
            }
        }

        // Create dir if not exists
        if (!this.cornerHintsDirectory.exists()) {
            if (!this.cornerHintsDirectory.mkdirs()) {
                // Cannot make dir so send out an error
                JOptionPane.showMessageDialog(this,
                        "Cannot create the directory below to store corner hints:\n" +
                        this.cornerHintsDirectory.getAbsolutePath(), "Error Creating Directory",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (!this.cornerHintsDirectory.isDirectory()) {
            JOptionPane.showMessageDialog(this,
                    this.cornerHintsDirectory.getAbsolutePath() +
                    " is not a directory.", "Error Accessing Directory",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // now write out every corner file as a text file
        Point[] lastGoodCorners = new Point[4];
        // Init the corner
        lastGoodCorners[TL] = corners[TL][0];
        lastGoodCorners[TR] = corners[TR][0];
        lastGoodCorners[BR] = corners[BR][0];
        lastGoodCorners[BL] = corners[BL][0];

        // Show loading dialog
        ProgressJDialog progressJDialog = new ProgressJDialog(null, false);
        progressJDialog.setTitle("Saving Corner Hints");
        progressJDialog.setMaxValue(files.length);
        progressJDialog.setVisible(true);
        progressJDialog.requestFocusInWindow();
        for (int i = 0; i < this.files.length; i++) {

            progressJDialog.setProgrss(i);
            progressJDialog.repaint();

            // instead, if corner is NOT_CODED, write out last good values
            if (corners[TL][i].x > NOT_CODED || corners[TR][i].x > NOT_CODED ||
                    corners[BL][i].x > NOT_CODED || corners[BR][i].x > NOT_CODED) {
                lastGoodCorners[TL] = corners[TL][i];
                lastGoodCorners[TR] = corners[TR][i];
                lastGoodCorners[BR] = corners[BR][i];
                lastGoodCorners[BL] = corners[BL][i];
            }

            if (this.files[i] != null) {
                String filename = toHintFileName(this.files[i].getName());
                File outFile = new File(this.cornerHintsDirectory, filename);
                BufferedWriter bw;

                // Write out last good corners
                String TLString = lastGoodCorners[TL].x + "\t" + lastGoodCorners[TL].y;
                String TRString = lastGoodCorners[TR].x + "\t" + lastGoodCorners[TR].y;
                String BRString = lastGoodCorners[BR].x + "\t" + lastGoodCorners[BR].y;
                String BLString = lastGoodCorners[BL].x + "\t" + lastGoodCorners[BL].y;
                try {
                    bw = new BufferedWriter(new FileWriter(outFile));
                    bw.write(TLString);
                    bw.newLine();
                    bw.write(TRString);
                    bw.newLine();
                    bw.write(BRString);
                    bw.newLine();
                    bw.write(BLString);
                    bw.close();
                } catch (IOException ex) {
                    Logger.getLogger(CornerSelector.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(this,
                            ex.getMessage(), "Error Saving Corner Hints",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        progressJDialog.dispose();

    }

    protected String toHintFileName(String filename) {
        filename = filename.substring(0, filename.lastIndexOf('.'));
        filename = filename + ".txt";

        return filename;
    }

    protected void loadCorners(int frameRate) {
        // Load nothing if there is no corner hints dir
        if (this.cornerHintsDirectory != null && this.cornerHintsDirectory.exists()) {
            // For each file (skipped by frame rate) check the hint file
            File hintFile = null;
            // Show loading dialog
            ProgressJDialog progressJDialog = new ProgressJDialog(null, false);
            progressJDialog.setTitle("Loading Corner Hints");
            progressJDialog.setMaxValue(files.length);
            progressJDialog.setVisible(true);
            progressJDialog.requestFocusInWindow();
            for (int i = 0; i < files.length; i += frameRate) {
                progressJDialog.setProgrss(i);
                progressJDialog.repaint();
                // Check the hint file
                if (files[i] != null) {
                    hintFile = new File(this.cornerHintsDirectory,
                            toHintFileName(files[i].getName()));
                    if (hintFile.exists()) {
                        try {
                            // Get the file content
                            RandomAccessFile in = new RandomAccessFile(hintFile, "r");
                            // Read contents
                            int j = 0;
                            String line = in.readLine();
                            while (line != null && j < 4) {
                                String[] token = line.split("\t");
                                if (token.length > 1) { // Sanity check
                                    Point p = null;
                                    switch (j) {
                                        case 0:
                                            p = this.corners[TL][i];
                                            break;
                                        case 1:
                                            p = this.corners[TR][i];
                                            break;
                                        case 2:
                                            p = this.corners[BR][i];
                                            break;
                                        case 3:
                                            p = this.corners[BL][i];
                                            break;
                                    }
                                    try {
                                        p.x = Integer.parseInt(token[0]);
                                        p.y = Integer.parseInt(token[1]);
                                    } catch (NumberFormatException numberFormatException) {
                                        p.x = NOT_CODED;
                                        p.y = NOT_CODED;
                                    }
                                }

                                // Next line
                                line = in.readLine();
                                j++;
                            }
                            in.close();
                        } catch (IOException ex) {
                            // We should not get this
                            Logger.getLogger(CornerSelector.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }// Do not load anything id the files doesnot exists
                }// Do not load anything id the files doesnot exists
            }
            progressJDialog.dispose();
        }// Else just don't load anything
    }

    protected void createCorners(int length) {
        corners = new Point[4][length];
        for (int i = 0; i < corners.length; i++) {
            for (int j = 0; j < corners[i].length; j++) {
                corners[i][j] = new Point(NOT_CODED, NOT_CODED);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cornerGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        frameRateTextField = new javax.swing.JTextField();
        frameScrollingJPanel = new cornerselector.FrameScrollingJPanel();
        jPanel2 = new javax.swing.JPanel();
        topleftRadioButton = new javax.swing.JRadioButton();
        topRightRadioButton = new javax.swing.JRadioButton();
        bottomLeftRadioButton = new javax.swing.JRadioButton();
        bottomRightRadioButton = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        reviewToggleButton = new javax.swing.JToggleButton();
        saveButton = new javax.swing.JButton();
        doneButton = new javax.swing.JButton();
        cancleButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        cornerSelectLabel = new cornerselector.CornerSelectLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Giving Hints For Corner Detection");
        setName("frame"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Frame Rate");
        jPanel4.add(jLabel1, new java.awt.GridBagConstraints());

        frameRateTextField.setText("1");
        frameRateTextField.setInputVerifier(new FrameRateInputVerifier());
        frameRateTextField.setMinimumSize(new java.awt.Dimension(60, 28));
        frameRateTextField.setPreferredSize(new java.awt.Dimension(60, 28));
        frameRateTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frameRateTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel4.add(frameRateTextField, gridBagConstraints);

        frameScrollingJPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                frameScrollingJPanelKeyTyped(evt);
            }
        });

        jPanel2.setLayout(new java.awt.GridLayout(2, 2));

        cornerGroup.add(topleftRadioButton);
        topleftRadioButton.setSelected(true);
        topleftRadioButton.setText("Top Left");
        jPanel2.add(topleftRadioButton);

        cornerGroup.add(topRightRadioButton);
        topRightRadioButton.setText("Top Right");
        jPanel2.add(topRightRadioButton);

        cornerGroup.add(bottomLeftRadioButton);
        bottomLeftRadioButton.setText("Bottom Left");
        jPanel2.add(bottomLeftRadioButton);

        cornerGroup.add(bottomRightRadioButton);
        bottomRightRadioButton.setText("Bottom Right");
        jPanel2.add(bottomRightRadioButton);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        reviewToggleButton.setText("Review");
        reviewToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reviewToggleButtonActionPerformed(evt);
            }
        });
        jPanel3.add(reviewToggleButton, new java.awt.GridBagConstraints());

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        jPanel3.add(saveButton, new java.awt.GridBagConstraints());

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });
        jPanel3.add(doneButton, new java.awt.GridBagConstraints());

        cancleButton.setText("Cancel");
        cancleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancleButtonActionPerformed(evt);
            }
        });
        jPanel3.add(cancleButton, new java.awt.GridBagConstraints());

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(frameScrollingJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 838, Short.MAX_VALUE)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(40, 40, 40)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 102, Short.MAX_VALUE)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(frameScrollingJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        cornerSelectLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        cornerSelectLabel.setText("N/A");
        cornerSelectLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        cornerSelectLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                cornerSelectLabelMouseMoved(evt);
            }
        });
        cornerSelectLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cornerSelectLabelMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cornerSelectLabelMouseEntered(evt);
            }
        });
        jScrollPane1.setViewportView(cornerSelectLabel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 838, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void cornerSelectLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cornerSelectLabelMouseExited
    this.cornerSelectLabel.setShowMouseCornerSelect(false);
    this.cornerSelectLabel.repaint();
}//GEN-LAST:event_cornerSelectLabelMouseExited

private void cornerSelectLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cornerSelectLabelMouseEntered
    this.cornerSelectLabel.setShowMouseCornerSelect(true &&
            !this.reviewToggleButton.isSelected());
    this.cornerSelectLabel.repaint();
    this.frameScrollingJPanel.requestFocus();
}//GEN-LAST:event_cornerSelectLabelMouseEntered

private void cornerSelectLabelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cornerSelectLabelMouseMoved
    this.cornerSelectLabel.repaint();
}//GEN-LAST:event_cornerSelectLabelMouseMoved

private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    this.imageLoadingThread.kill();
}//GEN-LAST:event_formWindowClosed

private void frameScrollingJPanelKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_frameScrollingJPanelKeyTyped
    switch (evt.getKeyChar()) {
        case ' ':
            // Toggle review
            this.reviewToggleButton.doClick();
            break;
    }
}//GEN-LAST:event_frameScrollingJPanelKeyTyped

private void reviewToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reviewToggleButtonActionPerformed
    this.cornerSelectLabel.setShowMouseCornerSelect(
            !this.reviewToggleButton.isSelected());
}//GEN-LAST:event_reviewToggleButtonActionPerformed

private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
    // Ask if this should be saved
    int n = JOptionPane.showConfirmDialog(
            this,
            "Would you like to save?",
            "Completing Corner Hint Selection",
            JOptionPane.YES_NO_CANCEL_OPTION);
    switch (n) {
        case JOptionPane.CANCEL_OPTION:
            // Cancel closeing
            return;
        case JOptionPane.YES_OPTION:
            // Save before exit
            Thread t = new Thread(new Runnable() {

                public void run() {
                    save();
                }
            });
            t.start();
    }
    this.terminationCause = TerminationCause.COMPLETED;
    // Dispose at the end
    dispose();
}//GEN-LAST:event_doneButtonActionPerformed

private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
    Thread t = new Thread(new Runnable() {

        public void run() {
            save();
        }
    });
    t.start();
}//GEN-LAST:event_saveButtonActionPerformed

private void cancleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancleButtonActionPerformed
    // Dispose at the end
    dispose();
}//GEN-LAST:event_cancleButtonActionPerformed

private void frameRateTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frameRateTextFieldActionPerformed
    this.frameScrollingJPanel.setFrameRate(Integer.parseInt(this.frameRateTextField.getText().trim()));
}//GEN-LAST:event_frameRateTextFieldActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        JFileChooser c = new JFileChooser();
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        c.showOpenDialog(null);
        final File[] files = c.getSelectedFile().listFiles();
        Arrays.sort(files, new Comparator<File>() {

            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                CornerSelector c = new CornerSelector(files, null, 10);
                c.setFirstFrameNumber(1);
                c.setCornerBoxSize(45);
                c.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton bottomLeftRadioButton;
    private javax.swing.JRadioButton bottomRightRadioButton;
    private javax.swing.JButton cancleButton;
    private javax.swing.ButtonGroup cornerGroup;
    private cornerselector.CornerSelectLabel cornerSelectLabel;
    private javax.swing.JButton doneButton;
    private javax.swing.JTextField frameRateTextField;
    private cornerselector.FrameScrollingJPanel frameScrollingJPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToggleButton reviewToggleButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JRadioButton topRightRadioButton;
    private javax.swing.JRadioButton topleftRadioButton;
    // End of variables declaration//GEN-END:variables
}
