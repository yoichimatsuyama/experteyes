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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.media.jai.JAI;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
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

    /** Creates new form CalibratingViewJDialog */
    public CalibratingViewJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        progress = new double[TOTAL_CALIBRATION_TYPE];
        final double stageProgress = (double) MAX_PROGRESS / (double) this.totalStages;

        listener = new CalibrateEyeGazeListener[2];
        listener[PRIMARY] = new CalibrateEyeGazeListener() {

            public void completeStage(int stage) {
                progress[PRIMARY] = (double) stage * stageProgress;
                double totalProgress = 0;
                for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
                    totalProgress += progress[i];
                }
                progressBar.setValue((int) totalProgress);
                getParent().repaint();
            }

            public void update(double[][] v, double cost) {
                coeff[PRIMARY] = v;
                estimatingPoints(PRIMARY);

                // Advance progress
                progress[PRIMARY] += progressStep;

                // Set progress
                double totalProgress = 0;
                for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
                    totalProgress += progress[i];
                }
                progressBar.setValue((int) totalProgress);
                primaryMarkableJLabel.repaint();
                progressBar.repaint();
            }
        };

        listener[SECONDARY] = new CalibrateEyeGazeListener() {

            public void completeStage(int stage) {
                progress[SECONDARY] = (double) stage * stageProgress;
                double totalProgress = 0;
                for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
                    totalProgress += progress[i];
                }
                progressBar.setValue((int) totalProgress);
                getParent().repaint();
            }

            public void update(double[][] v, double cost) {
                coeff[SECONDARY] = v;
                estimatingPoints(SECONDARY);

                // Advance progress
                progress[SECONDARY] += progressStep;

                // Set progress
                double totalProgress = 0;
                for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
                    totalProgress += progress[i];
                }
                progressBar.setValue((int) totalProgress);
                secondaryMarkableJLabel.repaint();
                progressBar.repaint();
            }
        };

        initComponents();
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

        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        primaryMarkableJLabel = new eyetrackercalibrator.gui.MarkableJLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        secondaryMarkableJLabel = new eyetrackercalibrator.gui.MarkableJLabel();
        jPanel2 = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        closeButton = new javax.swing.JButton();
        snapShotButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jScrollPane1.setBackground(new java.awt.Color(0, 0, 0));

        primaryMarkableJLabel.setBackground(new java.awt.Color(0, 0, 0));
        primaryMarkableJLabel.setOpaque(true);
        jScrollPane1.setViewportView(primaryMarkableJLabel);

        jTabbedPane1.addTab("Primary Calibration", jScrollPane1);

        jScrollPane2.setBackground(new java.awt.Color(0, 0, 0));

        secondaryMarkableJLabel.setBackground(new java.awt.Color(0, 0, 0));
        secondaryMarkableJLabel.setOpaque(true);
        jScrollPane2.setViewportView(secondaryMarkableJLabel);

        jTabbedPane1.addTab("Secondary Calibration", jScrollPane2);

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

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(snapShotButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(closeButton))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(snapShotButton)
                .add(closeButton))
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
    }

    public Point[][] getCorrectPoints() {
        return correctPoints;
    }

    /**
     * Make sure that all correctPoints contains has number of row equals to
     * TOTAL_CALIBRATION_TYPE.  No null is accepted
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
            }else{
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
        Point[][] combinedTestPoints = new Point[TOTAL_CALIBRATION_TYPE][];
        for (int i = 0; i < TOTAL_CALIBRATION_TYPE; i++) {
            int total = 0;
            for (int j = 0; j < TOTAL_CALIBRATION_TYPE; j++) {
                if (j != i) {
                    total += correctPoints[j].length;
                }
            }

            total += testPoints.length;
            combinedTestPoints[i] = new Point[total];
            int m = 0;
            for (int j = 0; j < TOTAL_CALIBRATION_TYPE; j++) {
                if (j != i) {
                    for (int k = 0; k < correctPoints[j].length; k++) {
                        combinedTestPoints[i][m] = this.correctPoints[j][k];
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
            primaryMarkableJLabel.setMarkedPoints(this.correctPoints[PRIMARY], MarkableJLabel.MarkColor.GREEN);
            primaryMarkableJLabel.setMarkedPoints(this.estimatedPoints[PRIMARY], MarkableJLabel.MarkColor.RED);
            primaryMarkableJLabel.setMarkedPoints(combinedTestPoints[PRIMARY], MarkableJLabel.MarkColor.BLUE);
            primaryMarkableJLabel.setMarkedPoints(this.estimatedTestPoints[PRIMARY], MarkableJLabel.MarkColor.YELLOW);
        }
        if (this.correctPoints[SECONDARY] != null) {
            secondaryMarkableJLabel.setMarkedPoints(this.correctPoints[SECONDARY], MarkableJLabel.MarkColor.GREEN);
            secondaryMarkableJLabel.setMarkedPoints(this.estimatedPoints[SECONDARY], MarkableJLabel.MarkColor.RED);
            secondaryMarkableJLabel.setMarkedPoints(combinedTestPoints[SECONDARY], MarkableJLabel.MarkColor.BLUE);
            secondaryMarkableJLabel.setMarkedPoints(this.estimatedTestPoints[SECONDARY], MarkableJLabel.MarkColor.YELLOW);
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
     *  Compute point estimation
     */
    private void estimatingPoints(int pos) {
        // Compute calibration point
        if (eyeVector[pos] != null) {
            for (int i = 0; i < eyeVector[pos].length; i++) {
                Point2D.Double point = Computation.computeEyeGazePoint(
                        eyeVector[pos][i].x, eyeVector[pos][i].y, coeff[pos]);
                estimatedPoints[pos][i].setLocation(point);
            }
        }

        int m = 0;
        // Compute test points
        for (int i = 0; i < TOTAL_INFO_TYPE; i++) {
            if (i != pos && eyeVector[i] != null) {
                for (int j = 0; j < eyeVector[i].length; j++) {
                    Point2D.Double point = Computation.computeEyeGazePoint(
                            eyeVector[i][j].x, eyeVector[i][j].y, coeff[pos]);
                    estimatedTestPoints[pos][m].setLocation(point);
                    m++;
                }
            }
        }
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private eyetrackercalibrator.gui.MarkableJLabel primaryMarkableJLabel;
    private javax.swing.JProgressBar progressBar;
    private eyetrackercalibrator.gui.MarkableJLabel secondaryMarkableJLabel;
    private javax.swing.JButton snapShotButton;
    // End of variables declaration//GEN-END:variables
}
