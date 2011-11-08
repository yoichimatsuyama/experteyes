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
 * CalibratingViewJDialog.java
 *
 * Created on October 29, 2007, 12:05 PM
 */
package eyetrackercalibrator.gui;

import eyetrackercalibrator.math.CalibrateEyeGazeListener;
import eyetrackercalibrator.math.Computation;
import eyetrackercalibrator.math.DegreeErrorComputer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import javax.media.jai.JAI;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;

/**
 * For viewing calibrating result as it runs
 * @author  ruj
 */
public class CalibratingViewJDialog
        extends javax.swing.JDialog {

    /** Position for primary calibration */
    public static int PRIMARY = 0;
    /** Position for secondary calibration */
    public static int SECONDARY = 1;
    /** Position for test only */
    public static int TEST = 2;
    /** Total support position */
    public static int TOTAL_CALIBRATION_TYPE = 2;
    /** Total info include all calibration and test (so must be > TOTAL_CALIBRATION_TYPE)*/
    public static int TOTAL_INFO_TYPE = TOTAL_CALIBRATION_TYPE + 1;
    final static int MAX_PROGRESS = 1000000;
    private double progressStep = 1d;
    private double[] progress;
    private int totalStages = 0;
    double[][][] coeff = new double[TOTAL_CALIBRATION_TYPE][][];
    private Point2D.Double[][] eyeVector = new Point2D.Double[TOTAL_INFO_TYPE][];
    private Point[][] correctPoints;
    private Point[][] estimatedPoints = new Point[TOTAL_CALIBRATION_TYPE][];
    private Point[][] estimatedTestPoints = new Point[TOTAL_CALIBRATION_TYPE][];
    private CalibrateEyeGazeListener[] listener = null;
    private int snapShotWidth = 0;
    private int snapShotHeight = 0;
    private DegreeErrorComputer degreeErrorComputer = null;
    private Point[][] combinedTestPoints = new Point[TOTAL_CALIBRATION_TYPE][0];
    private double minEyeVectorX = Double.POSITIVE_INFINITY;
    private double minEyeVectorY = Double.POSITIVE_INFINITY;
    private double maxEyeVectorX = Double.NEGATIVE_INFINITY;
    private double maxEyeVectorY = Double.NEGATIVE_INFINITY;
    // Parameters for displaying the calibration estimation grid
    private Point[][] estimationGridPoints = new Point[TOTAL_CALIBRATION_TYPE][];
    private final static Color ESTIMATION_GRID_COLOR = Color.cyan;

    private void handleShowEstimationGrid() {
        this.eyeVectorSpacingTextField.setEnabled(this.showEstimatinoGridCheckBox.isSelected());
        if (this.showEstimatinoGridCheckBox.isSelected()) {
            this.primaryMarkableJLabel.setMarkedPoints(this.estimatedPoints[0], MarkableJLabel.MarkColor.CYAN, false);
            this.secondaryMarkableJLabel.setMarkedPoints(this.estimatedPoints[1], MarkableJLabel.MarkColor.CYAN, false);
        } else {
            this.primaryMarkableJLabel.setMarkedPoints(null, MarkableJLabel.MarkColor.CYAN, false);
            this.secondaryMarkableJLabel.setMarkedPoints(null, MarkableJLabel.MarkColor.CYAN, false);
        }
        repaint();
    }

    /** Listener for progress */
    private class MyCalibrateEyeGazeListener implements CalibrateEyeGazeListener {

        int calibrationType;
        double stageProgress;
        MarkableJLabel display;
        private DecimalFormat formatter;

        public MyCalibrateEyeGazeListener(MarkableJLabel diaplay,
                int calibrationType, double stageProgress) {
            this.calibrationType = calibrationType;
            this.display = diaplay;
            formatter = new DecimalFormat("0.000");
        }

        @Override
        public void update(double[][] c, double cost) {
            coeff[this.calibrationType] = c;
            Point2D accuracy = estimatingPoints(this.calibrationType);
            this.display.setText("<html><pre>Calibration Accuracy: "
                    + formatter.format(accuracy.getX())
                    + " degrees of visual angle\n    Overall Accuracy: "
                    + formatter.format(accuracy.getY()) + " degrees of visual angle</pre></html>");
            // Advance progress
            progress[this.calibrationType] += progressStep;

            // Set progress
            double totalProgress = 0;
            for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
                totalProgress += progress[i];
            }
            progressBar.setValue((int) totalProgress);
            this.display.repaint();
            progressBar.repaint();
        }

        @Override
        public void completeStage(int stage) {
            progress[this.calibrationType] = (double) stage * this.stageProgress;
            double totalProgress = 0;
            for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
                totalProgress += progress[i];
            }
            progressBar.setValue((int) totalProgress);

            // Create estimation grid points
            createEstimationGrid();

            getParent().repaint();
        }
    }

    /** Creates new form CalibratingViewJDialog */
    public CalibratingViewJDialog(java.awt.Frame parent, boolean modal) {

        super(parent, modal);

        progress = new double[TOTAL_CALIBRATION_TYPE];
        double stageProgress = (double) MAX_PROGRESS / (double) this.totalStages;

        initComponents();

        // For displaying the accuracy text
        primaryMarkableJLabel.setVerticalTextPosition(JLabel.TOP);
        primaryMarkableJLabel.setHorizontalTextPosition(JLabel.LEFT);
        secondaryMarkableJLabel.setVerticalTextPosition(JLabel.TOP);
        secondaryMarkableJLabel.setHorizontalTextPosition(JLabel.LEFT);

        listener = new CalibrateEyeGazeListener[2];
        listener[PRIMARY] = new MyCalibrateEyeGazeListener(
                primaryMarkableJLabel, PRIMARY, stageProgress);
        listener[SECONDARY] = new MyCalibrateEyeGazeListener(
                secondaryMarkableJLabel, SECONDARY, stageProgress);

        progressBar.setMaximum(MAX_PROGRESS);
    }

    public void setTotalProgress(long max) {
        progressStep = (double) CalibratingViewJDialog.MAX_PROGRESS / (double) max;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textFieldEmptyPositiveDoubleInputVerifier1 = new eyetrackercalibrator.gui.util.TextFieldEmptyPositiveDoubleInputVerifier();
        jPanel2 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        snapShotButton = new javax.swing.JButton();
        showDegreeErrorCheckBox = new javax.swing.JCheckBox();
        progressBar = new javax.swing.JProgressBar();
        showEstimatinoGridCheckBox = new javax.swing.JCheckBox();
        eyeVectorSpacingTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        primaryMarkableJLabel = new eyetrackercalibrator.gui.MarkableJLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        secondaryMarkableJLabel = new eyetrackercalibrator.gui.MarkableJLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        snapShotButton.setText("Take A Snap Shot");
        snapShotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapShotButtonActionPerformed(evt);
            }
        });

        showDegreeErrorCheckBox.setSelected(true);
        showDegreeErrorCheckBox.setText("Show average degree error");
        showDegreeErrorCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showDegreeErrorCheckBoxActionPerformed(evt);
            }
        });

        showEstimatinoGridCheckBox.setText("Show estimation grid");
        showEstimatinoGridCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showEstimatinoGridCheckBoxActionPerformed(evt);
            }
        });

        eyeVectorSpacingTextField.setText("10");
        eyeVectorSpacingTextField.setEnabled(false);
        eyeVectorSpacingTextField.setInputVerifier(textFieldEmptyPositiveDoubleInputVerifier1);
        eyeVectorSpacingTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eyeVectorSpacingTextFieldActionPerformed(evt);
            }
        });

        jLabel1.setText("Eyevector spacing");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(showDegreeErrorCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showEstimatinoGridCheckBox)
                .add(12, 12, 12)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(eyeVectorSpacingTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 371, Short.MAX_VALUE)
                .add(snapShotButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(closeButton))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1112, Short.MAX_VALUE)
                .add(18, 18, 18))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(snapShotButton)
                    .add(closeButton)
                    .add(showDegreeErrorCheckBox)
                    .add(showEstimatinoGridCheckBox)
                    .add(eyeVectorSpacingTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jScrollPane1.setBackground(new java.awt.Color(0, 0, 0));

        primaryMarkableJLabel.setBackground(new java.awt.Color(0, 0, 0));
        primaryMarkableJLabel.setForeground(new java.awt.Color(255, 255, 255));
        primaryMarkableJLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        primaryMarkableJLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        primaryMarkableJLabel.setOpaque(true);
        jScrollPane1.setViewportView(primaryMarkableJLabel);

        jTabbedPane1.addTab("Primary Calibration", jScrollPane1);

        jScrollPane2.setBackground(new java.awt.Color(0, 0, 0));

        secondaryMarkableJLabel.setBackground(new java.awt.Color(0, 0, 0));
        secondaryMarkableJLabel.setForeground(new java.awt.Color(255, 255, 255));
        secondaryMarkableJLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        secondaryMarkableJLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        secondaryMarkableJLabel.setOpaque(true);
        jScrollPane2.setViewportView(secondaryMarkableJLabel);

        jTabbedPane1.addTab("Secondary Calibration", jScrollPane2);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1144, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.dispose();
}//GEN-LAST:event_closeButtonActionPerformed

    private void snapShotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapShotButtonActionPerformed

        // Ask user for location
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isFile()) {
                    // Get extension
                    String name = f.getName();
                    return name.endsWith(".tiff") || name.endsWith(".TIFF");
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "TIFF file";
            }
        });
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File outputFile = chooser.getSelectedFile();
            // Check if extension is there
            String name = outputFile.getAbsolutePath();
            if (name.endsWith(".tiff") || name.endsWith(".TIFF")) {
                // Does nothing
            } else {
                // Append file name
                name = name + ".tiff";
            }
            outputFile = new File(name);

            // Create image
            BufferedImage image = new BufferedImage(
                    this.snapShotWidth, this.snapShotHeight,
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            switch (this.jTabbedPane1.getSelectedIndex()) {
                case 1:
                    // Save secondary
                    this.secondaryMarkableJLabel.paint(g);
                    break;
                default:
                    // Save primary
                    this.primaryMarkableJLabel.paint(g);
            }

            //Save image
            JAI.create("filestore", image, outputFile.getAbsolutePath(),
                    "TIFF", null);
        }

    }//GEN-LAST:event_snapShotButtonActionPerformed

    private void showDegreeErrorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showDegreeErrorCheckBoxActionPerformed
        if (showDegreeErrorCheckBox.isSelected()) {
            // change forground color to hide text
            primaryMarkableJLabel.setForeground(Color.WHITE);
            secondaryMarkableJLabel.setForeground(Color.WHITE);
        } else {
            primaryMarkableJLabel.setForeground(Color.BLACK);
            secondaryMarkableJLabel.setForeground(Color.BLACK);
        }
        repaint();
}//GEN-LAST:event_showDegreeErrorCheckBoxActionPerformed

    private void eyeVectorSpacingTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eyeVectorSpacingTextFieldActionPerformed
// Recompute the grid and update
        createEstimationGrid();

        handleShowEstimationGrid();
    }//GEN-LAST:event_eyeVectorSpacingTextFieldActionPerformed

    private void showEstimatinoGridCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showEstimatinoGridCheckBoxActionPerformed

        handleShowEstimationGrid();
    }//GEN-LAST:event_showEstimatinoGridCheckBoxActionPerformed

    /**
     * @param pos Position defined by the constant (PRIMARY, SECONDARY, TEST)
     */
    public Point2D.Double[] getEyeVector(int pos) {
        if (pos >= 0 && pos < TOTAL_INFO_TYPE) {
            return eyeVector[pos];
        } else {
            return null;
        }
    }

    /**
     * @param pos Position defined by the constant (PRIMARY, SECONDARY, TEST)
     */
    public void setEyeVector(Point2D.Double[] eyeVector, int pos) {
        if (pos >= 0 && pos < TOTAL_INFO_TYPE) {
            this.eyeVector[pos] = eyeVector;
        }

        // Compute mesh from calibration
        // Determine the bound of mesh
        // Get minimum x
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < eyeVector.length; i++) {
            Point2D.Double v = eyeVector[i];
            minX = Math.min(minX, v.x);
            maxX = Math.max(maxX, v.x);
            minY = Math.min(minY, v.y);
            maxY = Math.max(maxY, v.y);
        }

        this.minEyeVectorX = Math.min(this.minEyeVectorX, minX);
        this.minEyeVectorY = Math.min(this.minEyeVectorY, minY);
        this.maxEyeVectorX = Math.max(this.maxEyeVectorX, maxX);
        this.maxEyeVectorY = Math.max(this.maxEyeVectorY, maxY);
    }

    public Point[][] getCorrectPoints() {
        return correctPoints;
    }

    /**
     * Make sure that all correctPoints contains has number of row equals to
     * TOTAL_CALIBRATION_TYPE.  No null is accepted
     * The points have to be in the same order as the gaze point for degree error
     * to be computed correctly
     */
    public void setCorrectPoints(Point2D[][] correctPoints, Point2D[] testPoints) {
        this.correctPoints = new Point[TOTAL_INFO_TYPE][];

        /*
         * Store all correct points for each calibration type. The last one
         * is reserved for test points
         */
        for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
            if (correctPoints.length > i && correctPoints[i] != null) {
                this.correctPoints[i] = new Point[correctPoints[i].length];
                for (int j = 0; j < correctPoints[i].length; j++) {
                    Point p = new Point();
                    p.setLocation(correctPoints[i][j]);
                    this.correctPoints[i][j] = p;
                }
            } else {
                // Give zero length if nothing exists in the input
                this.correctPoints[i] = new Point[0];
            }
        }
        this.correctPoints[TEST] = new Point[testPoints.length];
        for (int i = 0; i < testPoints.length; i++) {
            Point p = new Point();
            p.setLocation(testPoints[i]);
            this.correctPoints[TEST][i] = p;
        }

        // Create combind test point
        this.combinedTestPoints = new Point[TOTAL_CALIBRATION_TYPE][];
        for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
            int total = 0;
            for (int j = 0; j < TOTAL_CALIBRATION_TYPE; j++) {
                if (j != i) {
                    total += correctPoints[j].length;
                }
            }

            total += testPoints.length;
            this.combinedTestPoints[i] = new Point[total];
            int m = 0;
            for (int j = 0; j < TOTAL_CALIBRATION_TYPE; j++) {
                if (j != i) {
                    for (int k = 0; k < correctPoints[j].length; k++) {
                        this.combinedTestPoints[i][m] = this.correctPoints[j][k];
                        m++;
                    }
                }
            }
        }

        // Set estimate points
        this.estimatedPoints = new Point[TOTAL_CALIBRATION_TYPE][];
        for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
            this.estimatedPoints[i] = new Point[correctPoints[i].length];
            for (int j = 0; j < estimatedPoints[i].length; j++) {
                this.estimatedPoints[i][j] = new Point(10, 10);
            }
        }

        // Set estimate test points
        this.estimatedTestPoints = new Point[TOTAL_CALIBRATION_TYPE][];
        for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
            int total = 0;
            for (int j = 0; j < TOTAL_INFO_TYPE; j++) {
                if (j != i) {
                    total += this.correctPoints[j].length;
                }
            }
            this.estimatedTestPoints[i] = new Point[total];
            for (int j = 0; j < total; j++) {
                this.estimatedTestPoints[i][j] = new Point(10, 10);
            }
        }



        // Set display
        if (this.correctPoints[PRIMARY] != null) {
            primaryMarkableJLabel.setMarkedPoints(this.correctPoints[PRIMARY], MarkableJLabel.MarkColor.GREEN, false);
            primaryMarkableJLabel.setMarkedPoints(this.estimatedPoints[PRIMARY], MarkableJLabel.MarkColor.RED, false);
            primaryMarkableJLabel.setMarkedPoints(this.correctPoints[SECONDARY], MarkableJLabel.MarkColor.YELLOW, false);
            primaryMarkableJLabel.setMarkedPoints(this.correctPoints[TEST], MarkableJLabel.MarkColor.BLUE, false);
            primaryMarkableJLabel.setMarkedPoints(this.estimatedTestPoints[PRIMARY], MarkableJLabel.MarkColor.WHITE, false);
        }
        if (this.correctPoints[SECONDARY] != null) {
            secondaryMarkableJLabel.setMarkedPoints(this.correctPoints[PRIMARY], MarkableJLabel.MarkColor.GREEN, false);
            secondaryMarkableJLabel.setMarkedPoints(this.correctPoints[SECONDARY], MarkableJLabel.MarkColor.YELLOW, false);
            secondaryMarkableJLabel.setMarkedPoints(this.estimatedPoints[SECONDARY], MarkableJLabel.MarkColor.RED, false);
            secondaryMarkableJLabel.setMarkedPoints(this.correctPoints[TEST], MarkableJLabel.MarkColor.BLUE, false);
            secondaryMarkableJLabel.setMarkedPoints(this.estimatedTestPoints[SECONDARY], MarkableJLabel.MarkColor.WHITE, false);
        }

        // Repaint
        repaint();
    }

    /**
     * Set x coefficients.  No copy is made for input
     *
     * @param pos Position defined by the constant (PRIMARY, SECONDARY)
     *
     */
    public void setXCoefficients(double[] c, int pos) {
        coeff[pos][0] = c;
    }

    /**
     * Set y coefficients.  No copy is made for input
     *
     * @param pos Position defined by the constant (PRIMARY, SECONDARY)
     *
     */
    public void setYCoefficients(double[] c, int pos) {
        coeff[pos][1] = c;
    }

    /**
     * @param pos Position defined by the constant (PRIMARY, SECONDARY)
     */
    public CalibrateEyeGazeListener getCoeffListener(int pos) {
        return listener[pos];
    }

    /**
     * Compute point estimation
     * @return Point2D where x is accuracy for calibration and y is overall calibration -1 indicates that it cannot be computed
     */
    private Point2D estimatingPoints(int pos) {
        double totalCalibrateDegreeError = 0;
        double totalTestDegreeError = 0;

        // Compute calibration point
        if (eyeVector[pos] != null) {
            for (int i = 0; i < eyeVector[pos].length; i++) {
                Point2D.Double point = Computation.computeEyeGazePoint(
                        eyeVector[pos][i].x, eyeVector[pos][i].y, coeff[pos]);
                estimatedPoints[pos][i].setLocation(point);
                if (this.degreeErrorComputer != null) {
                    totalCalibrateDegreeError += this.degreeErrorComputer.degreeError(
                            this.correctPoints[pos][i], point);
                }
            }
        }

        int m = 0;
        // Compute test points
        for (int i = 0; i < TOTAL_INFO_TYPE; i++) {
            if (i != pos && eyeVector[i] != null) {
                for (int j = 0; j < eyeVector[i].length; j++) {
                    Point2D.Double point = Computation.computeEyeGazePoint(
                            eyeVector[i][j].x, eyeVector[i][j].y, coeff[pos]);

                    /*
                     * could correct for drift using the last drift
                     */
                   /* if (allDriftCorrectionSets != null)
                    {
                    point = applyDriftCorrection(allDriftCorrectionSets, currentFrame, point);
                    }
                    * 
                    */
                    estimatedTestPoints[pos][m].setLocation(point);
                    if (this.degreeErrorComputer != null && this.combinedTestPoints[pos][m] != null) {
                        totalTestDegreeError += this.degreeErrorComputer.degreeError(
                                this.combinedTestPoints[pos][m], point);
                    }
                    m++;
                }
            }
        }

        // Compute calibration accuracy
        int totalPoints = 0;
        Point2D.Double result = new Point2D.Double(-1, -1);
        if (this.correctPoints[pos] != null) {
            totalPoints = this.correctPoints[pos].length;
        }

        if (totalPoints > 0) {
            result.x = totalCalibrateDegreeError / (double) totalPoints;
        }

        // Compute overall accuracy
        if (this.combinedTestPoints[pos] != null) {
            totalPoints += this.combinedTestPoints[pos].length;
        }
        if (totalPoints > 0) {
            result.y = (totalCalibrateDegreeError + totalTestDegreeError) / (double) totalPoints;
        }

        return result;
    }

    public int getTotalStages() {
        return totalStages;
    }

    public void setTotalStages(int totalStages) {
        this.totalStages = totalStages;
    }

    public void setDisplayArea(int width, int height) {
        this.snapShotWidth = width;
        this.snapShotHeight = height;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        primaryMarkableJLabel.setIcon(new ImageIcon(image));
        pack();
    }

    /**  If degreeErrorComputer is set to null, no */
    public void setDegreeErrorComputer(DegreeErrorComputer degreeErrorComputer) {
        this.degreeErrorComputer = degreeErrorComputer;
    }

    private void createEstimationGrid() {
        // Get spacing
        double spacing = 0;
        try {
            spacing = Double.parseDouble(this.eyeVectorSpacingTextField.getText());
        } catch (NumberFormatException numberFormatException) {
            return;
        }

        // Sanity check
        if (spacing > 0) {

            // Compute how many points we need
            int totalPoints = (int) ((this.maxEyeVectorX - this.minEyeVectorX) / spacing + 1)
                    * (int) ((this.maxEyeVectorY - this.minEyeVectorY) / spacing + 1);

            for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
                // If we don't have calibration coeff yet, there is no need to compute
                if (this.coeff != null && this.coeff[i] != null) {

                    // Check and see if we have enough array
                    if (this.estimatedPoints[i] == null || this.estimatedPoints[i].length != totalPoints) {
                        this.estimatedPoints[i] = new Point[totalPoints];
                    }// Else we already have enough space. No need to do anything

                    int count = 0;
                    for (double x = this.minEyeVectorX; x <= this.maxEyeVectorX; x += spacing) {
                        for (double y = this.minEyeVectorY; y <= this.maxEyeVectorY; y += spacing) {
                            if (this.estimatedPoints[i][count] == null) {
                                this.estimatedPoints[i][count] = new Point();
                            }

                            this.estimatedPoints[i][count].setLocation(Computation.computeEyeGazePoint(x, y, this.coeff[i]));

                            count++;

                        }
                    }
                }
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JTextField eyeVectorSpacingTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private eyetrackercalibrator.gui.MarkableJLabel primaryMarkableJLabel;
    private javax.swing.JProgressBar progressBar;
    private eyetrackercalibrator.gui.MarkableJLabel secondaryMarkableJLabel;
    private javax.swing.JCheckBox showDegreeErrorCheckBox;
    private javax.swing.JCheckBox showEstimatinoGridCheckBox;
    private javax.swing.JButton snapShotButton;
    private eyetrackercalibrator.gui.util.TextFieldEmptyPositiveDoubleInputVerifier textFieldEmptyPositiveDoubleInputVerifier1;
    // End of variables declaration//GEN-END:variables
}
