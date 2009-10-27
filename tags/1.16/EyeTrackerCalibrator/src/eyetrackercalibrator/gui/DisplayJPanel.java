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
 * DisplayJPanel.java
 *
 * Created on September 10, 2007, 9:40 AM
 */
package eyetrackercalibrator.gui;

import eyetrackercalibrator.gui.util.RotatedEllipse2D;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import javax.swing.ImageIcon;

/**
 * Note: When you use this class make sure that minimum size and preferrred size are the same or strange behavior will happen
 * @author  eeglab
 */
public class DisplayJPanel extends javax.swing.JPanel {

    public Point[] eyeGaze = null;
    private double gazeScaleFactor = 1;
    private double[][] gazeCoefficient = null;

    // Dummy Blank Image of 1 pixel
    // private ImageIcon noImageIcon = new ImageIcon(new BufferedImage(1,1,1));
    /** Creates new form DisplayJPanel */
    public DisplayJPanel() {
        initComponents();
        eyeGaze = new Point[1];
        eyeGaze[0] = new Point();
    }

    /**
     * Set new icon will erase all marks
     * @param imageIcon 
     */
    public void setEyeViewImage(ImageIcon imageIcon) {
        if (imageIcon == null) {
            // Set text to show not available
            eyeViewLabel.setText("N/A");
        } else {
            // Else clear old text
            eyeViewLabel.setText(null);
        }
        eyeViewLabel.setIcon(imageIcon);
        eyeViewLabel.clearCorners();
        eyeViewLabel.clearMarks();
    }

    /**
     * Set new icon will erase all marks
     * @param imageIcon 
     */
    public void setScreenViewImage(ImageIcon imageIcon) {
        if (imageIcon == null) {
            // Set text to show not available
            screenViewLabel.setText("N/A");
        } else {
            // Else clear old text
            screenViewLabel.setText(null);
        }
        screenViewLabel.setIcon(imageIcon);

        screenViewLabel.clearCorners();
        screenViewLabel.clearMarks();
    }

    public void clearEyeMarks() {
        eyeViewLabel.clearMarks();
    }

    public void setScreenCorners(Point topleft, Point topright, Point bottomleft, Point bottomright) {
        screenViewLabel.setCorners(topleft, topright, bottomleft, bottomright, MarkableJLabel.CornerColor.GREEN);
    }

    public void setScreenCorrectedCorners(Point topleft, Point topright, Point bottomleft, Point bottomright) {
        screenViewLabel.setCorners(topleft, topright, bottomleft, bottomright, MarkableJLabel.CornerColor.RED);
    }

    public void setEyeMarkedPoints(Point[] markedPoints) {
        eyeViewLabel.setMarkedPoints(markedPoints, MarkableJLabel.MarkColor.GREEN);
    }

    public void setScreenMarkedPoints(Point[] markedPoints) {
        screenViewLabel.setMarkedPoints(markedPoints, MarkableJLabel.MarkColor.GREEN);
    }

    public void clearScreenMarks() {
        screenViewLabel.clearMarks();
    }

    public void clearScreenCorners() {
        screenViewLabel.clearCorners();
    }

    public void addScreenViewMouseListener(MouseAdapter mouseAdapter) {
        screenViewLabel.addMouseListener(mouseAdapter);
    }

    public void addEyeViewMouseListener(MouseAdapter mouseAdapter) {
        eyeViewLabel.addMouseListener(mouseAdapter);
    }

    public void setEyeGaze(double x, double y) {
        eyeGaze[0].setLocation(x * gazeScaleFactor, y * gazeScaleFactor);
        // Put a mark on it.
        screenViewLabel.setMarkedPoints(eyeGaze, MarkableJLabel.MarkColor.RED);
    }

    public void setPupilFit(double[] boundingBox, double angle) {
        RotatedEllipse2D ellisp = null;

        if (boundingBox != null) {
            ellisp = new RotatedEllipse2D(
                    (int) boundingBox[0], (int) boundingBox[1],
                    (int) boundingBox[2], (int) boundingBox[3],
                    angle);
        }

        eyeViewLabel.setGreenEllisp(ellisp);
    }

    public void setReflectFit(double[] boundingBox) {
        RotatedEllipse2D ellisp = null;

        if (boundingBox != null) {
            ellisp = new RotatedEllipse2D(
                    (int) boundingBox[0], (int) boundingBox[1],
                    (int) boundingBox[2], (int) boundingBox[3],
                    0);
        }

        eyeViewLabel.setRedEllisp(ellisp);
    }

    public Dimension getScreenViewDimension() {
        Dimension d = screenViewPanel.getSize();
        d.width -= 12;
        d.height -= 26;
        return d;
    }

    public Dimension getEyeViewDimension() {
        Dimension d = eyeViewPanelPanel.getSize();
        d.width -= 12;
        d.height -= 26;
        return d;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        eyeViewPanelPanel = new javax.swing.JPanel();
        eyeViewLabel = new eyetrackercalibrator.gui.MarkableJLabel();
        screenViewPanel = new javax.swing.JPanel();
        screenViewLabel = new eyetrackercalibrator.gui.MarkableJLabel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        eyeViewPanelPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Eye View"));
        eyeViewPanelPanel.setPreferredSize(new java.awt.Dimension(100, 100));

        eyeViewLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        eyeViewLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        eyeViewLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        eyeViewLabel.setMaximumSize(null);
        eyeViewLabel.setMinimumSize(null);
        eyeViewLabel.setPreferredSize(null);
        eyeViewLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        org.jdesktop.layout.GroupLayout eyeViewPanelPanelLayout = new org.jdesktop.layout.GroupLayout(eyeViewPanelPanel);
        eyeViewPanelPanel.setLayout(eyeViewPanelPanelLayout);
        eyeViewPanelPanelLayout.setHorizontalGroup(
            eyeViewPanelPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(eyeViewLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
        );
        eyeViewPanelPanelLayout.setVerticalGroup(
            eyeViewPanelPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(eyeViewLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
        );

        add(eyeViewPanelPanel);

        screenViewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Scene View", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        screenViewPanel.setPreferredSize(new java.awt.Dimension(100, 100));

        screenViewLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        screenViewLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        screenViewLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        screenViewLabel.setMaximumSize(null);
        screenViewLabel.setMinimumSize(null);
        screenViewLabel.setPreferredSize(null);
        screenViewLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        org.jdesktop.layout.GroupLayout screenViewPanelLayout = new org.jdesktop.layout.GroupLayout(screenViewPanel);
        screenViewPanel.setLayout(screenViewPanelLayout);
        screenViewPanelLayout.setHorizontalGroup(
            screenViewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(screenViewLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
        );
        screenViewPanelLayout.setVerticalGroup(
            screenViewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(screenViewLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
        );

        add(screenViewPanel);
        screenViewPanel.getAccessibleContext().setAccessibleName("Scene View");
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private eyetrackercalibrator.gui.MarkableJLabel eyeViewLabel;
    private javax.swing.JPanel eyeViewPanelPanel;
    private eyetrackercalibrator.gui.MarkableJLabel screenViewLabel;
    private javax.swing.JPanel screenViewPanel;
    // End of variables declaration//GEN-END:variables
    public double getGazeScaleFactor() {
        return gazeScaleFactor;
    }

    /**
     * @param gazeScaleFactor Scale factor to scale result of eye gaze coeff into
     * 512 x 512 space
     */
    public void setGazeScaleFactor(double gazeScaleFactor) {
        this.gazeScaleFactor = gazeScaleFactor;
    }
}
