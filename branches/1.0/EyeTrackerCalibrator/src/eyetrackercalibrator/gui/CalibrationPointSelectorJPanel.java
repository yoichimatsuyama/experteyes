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
 * CalibrationPointSelectorJPanel.java
 *
 * Created on September 10, 2007, 12:27 PM
 */
package eyetrackercalibrator.gui;

import java.awt.Point;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

/**
 *
 * @author  eeglab
 */
public class CalibrationPointSelectorJPanel extends javax.swing.JPanel {

    JLabel[] label = new JLabel[25];
    JRadioButton[] radio = new JRadioButton[25];

    /** Creates new form CalibrationPointSelectorJPanel */
    public CalibrationPointSelectorJPanel() {
        initComponents();
        label[	0] = jLabel;
        label[	1] = jLabel1;
        label[	2] = jLabel2;
        label[	3] = jLabel3;
        label[	4] = jLabel4;
        label[	5] = jLabel5;
        label[	6] = jLabel6;
        label[	7] = jLabel7;
        label[	8] = jLabel8;
        label[	9] = jLabel9;
        label[	10] = jLabel10;
        label[	11] = jLabel11;
        label[	12] = jLabel12;
        label[	13] = jLabel13;
        label[	14] = jLabel14;
        label[	15] = jLabel15;
        label[	16] = jLabel16;
        label[	17] = jLabel17;
        label[	18] = jLabel18;
        label[	19] = jLabel19;
        label[	20] = jLabel20;
        label[	21] = jLabel21;
        label[	22] = jLabel22;
        label[	23] = jLabel23;
        label[	24] = jLabel24;
        radio[	0] = pointRadio;
        radio[	1] = pointRadio1;
        radio[	2] = pointRadio2;
        radio[	3] = pointRadio3;
        radio[	4] = pointRadio4;
        radio[	5] = pointRadio5;
        radio[	6] = pointRadio6;
        radio[	7] = pointRadio7;
        radio[	8] = pointRadio8;
        radio[	9] = pointRadio9;
        radio[	10] = pointRadio10;
        radio[	11] = pointRadio11;
        radio[	12] = pointRadio12;
        radio[	13] = pointRadio13;
        radio[	14] = pointRadio14;
        radio[	15] = pointRadio15;
        radio[	16] = pointRadio16;
        radio[	17] = pointRadio17;
        radio[	18] = pointRadio18;
        radio[	19] = pointRadio19;
        radio[	20] = pointRadio20;
        radio[	21] = pointRadio21;
        radio[	22] = pointRadio22;
        radio[	23] = pointRadio23;
        radio[	24] = pointRadio24;
    }

    /**
     *  Command code for each button is <row>_<column> from 1_1 ... 3_3 according
     *  to position of the button
     */
    public void addActionListener(ActionListener listener) {
        for (int i = 0; i < radio.length; i++) {
            radio[i].addActionListener(listener);
        }
    }
    
    /** Return selected index */
    public int getSelectedPointIndex(){
        for (int i = 0; i < radio.length; i++) {
            if(radio[i].isSelected()){
                return i;
            }
        }
        return -1;
    }
    
    public void setLabelText(int index, String text){
        if(index >= 0 && index < this.label.length){
            this.label[index].setText(text);
        }
    }
    
    /** @return null if index if out of bound */
    public String getLabelText(int index){
        if(index >= 0 && index < this.label.length){
            return this.label[index].getText();
        }
        return null;
    }
    
    /**
     * @return null when index is out of bound.  Otherwise return the location
     * of the index in the grid with topleft coor of (1,1)
     */
    public Point indexToCalibrationPoint(int index){
        if(index >= 0 && index < this.label.length){
            Point p = new Point(index % 5 + 1, 
                    (index - index % 5) / 5 + 1);
            return p;
        }
        return null;
    }
    
    /**
     * Convert point coordinate to array index
     * @param x Range from 1 to 5
     * @param y Range from 1 to 5
     * @return -1 if point is out of bound
     */
    public int pointToIndex(int x, int y){
        int index = (x - 1) + (y - 1) * 5;
        if(index >= 0 && index < this.label.length){
            return index;
        }
        return -1;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pointSelectButtonGroup = new javax.swing.ButtonGroup();
        pointRadio = new javax.swing.JRadioButton();
        jLabel = new javax.swing.JLabel();
        pointRadio1 = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        pointRadio2 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        pointRadio3 = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        pointRadio4 = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        pointRadio5 = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        pointRadio6 = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        pointRadio7 = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        pointRadio8 = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        pointRadio9 = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        pointRadio10 = new javax.swing.JRadioButton();
        jLabel10 = new javax.swing.JLabel();
        pointRadio11 = new javax.swing.JRadioButton();
        jLabel11 = new javax.swing.JLabel();
        pointRadio12 = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        pointRadio13 = new javax.swing.JRadioButton();
        jLabel13 = new javax.swing.JLabel();
        pointRadio14 = new javax.swing.JRadioButton();
        jLabel14 = new javax.swing.JLabel();
        pointRadio15 = new javax.swing.JRadioButton();
        jLabel15 = new javax.swing.JLabel();
        pointRadio16 = new javax.swing.JRadioButton();
        jLabel16 = new javax.swing.JLabel();
        pointRadio17 = new javax.swing.JRadioButton();
        jLabel17 = new javax.swing.JLabel();
        pointRadio18 = new javax.swing.JRadioButton();
        jLabel18 = new javax.swing.JLabel();
        pointRadio19 = new javax.swing.JRadioButton();
        jLabel19 = new javax.swing.JLabel();
        pointRadio20 = new javax.swing.JRadioButton();
        jLabel20 = new javax.swing.JLabel();
        pointRadio21 = new javax.swing.JRadioButton();
        jLabel21 = new javax.swing.JLabel();
        pointRadio22 = new javax.swing.JRadioButton();
        jLabel22 = new javax.swing.JLabel();
        pointRadio23 = new javax.swing.JRadioButton();
        jLabel23 = new javax.swing.JLabel();
        pointRadio24 = new javax.swing.JRadioButton();
        jLabel24 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Calibration Point"));

        pointSelectButtonGroup.add(pointRadio);
        pointRadio.setSelected(true);
        pointRadio.setActionCommand("1_1");
        pointRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel.setText("0");
        jLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio1);
        pointRadio1.setActionCommand("1_1");
        pointRadio1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("0");
        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel1.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio2);
        pointRadio2.setActionCommand("1_1");
        pointRadio2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("0");
        jLabel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel2.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio3);
        pointRadio3.setActionCommand("1_1");
        pointRadio3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio3.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("0");
        jLabel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel3.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio4);
        pointRadio4.setActionCommand("1_1");
        pointRadio4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio4.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("0");
        jLabel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel4.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio5);
        pointRadio5.setActionCommand("1_1");
        pointRadio5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio5.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("0");
        jLabel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel5.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio6);
        pointRadio6.setActionCommand("1_1");
        pointRadio6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio6.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("0");
        jLabel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel6.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio7);
        pointRadio7.setActionCommand("1_1");
        pointRadio7.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio7.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("0");
        jLabel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel7.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio8);
        pointRadio8.setActionCommand("1_1");
        pointRadio8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio8.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("0");
        jLabel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel8.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio9);
        pointRadio9.setActionCommand("1_1");
        pointRadio9.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio9.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("0");
        jLabel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel9.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio10);
        pointRadio10.setActionCommand("1_1");
        pointRadio10.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio10.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("0");
        jLabel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel10.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio11);
        pointRadio11.setActionCommand("1_1");
        pointRadio11.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio11.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("0");
        jLabel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel11.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio12);
        pointRadio12.setActionCommand("1_1");
        pointRadio12.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio12.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("0");
        jLabel12.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel12.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio13);
        pointRadio13.setActionCommand("1_1");
        pointRadio13.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio13.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("0");
        jLabel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel13.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio14);
        pointRadio14.setActionCommand("1_1");
        pointRadio14.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio14.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("0");
        jLabel14.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel14.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio15);
        pointRadio15.setActionCommand("1_1");
        pointRadio15.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio15.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("0");
        jLabel15.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel15.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio16);
        pointRadio16.setActionCommand("1_1");
        pointRadio16.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio16.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("0");
        jLabel16.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel16.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio17);
        pointRadio17.setActionCommand("1_1");
        pointRadio17.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio17.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("0");
        jLabel17.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel17.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio18);
        pointRadio18.setActionCommand("1_1");
        pointRadio18.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio18.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("0");
        jLabel18.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel18.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio19);
        pointRadio19.setActionCommand("1_1");
        pointRadio19.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio19.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("0");
        jLabel19.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel19.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio20);
        pointRadio20.setActionCommand("1_1");
        pointRadio20.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio20.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("0");
        jLabel20.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel20.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio21);
        pointRadio21.setActionCommand("1_1");
        pointRadio21.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio21.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("0");
        jLabel21.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel21.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio22);
        pointRadio22.setActionCommand("1_1");
        pointRadio22.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio22.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("0");
        jLabel22.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel22.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio23);
        pointRadio23.setActionCommand("1_1");
        pointRadio23.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio23.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("0");
        jLabel23.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel23.setMaximumSize(new java.awt.Dimension(15, 15));

        pointSelectButtonGroup.add(pointRadio24);
        pointRadio24.setActionCommand("1_1");
        pointRadio24.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointRadio24.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setText("0");
        jLabel24.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel24.setMaximumSize(new java.awt.Dimension(15, 15));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(pointRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createSequentialGroup()
                .add(pointRadio5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio8)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio9)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createSequentialGroup()
                .add(pointRadio10)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio11)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio12)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio13)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio14)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createSequentialGroup()
                .add(pointRadio15)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio16)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio17)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio18)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio19)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createSequentialGroup()
                .add(pointRadio20)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio21)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio22)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio23)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pointRadio24)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio15, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio16, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio17, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio18, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio19, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio20, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio21, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio22, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio23, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pointRadio24, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
        );

        getAccessibleContext().setAccessibleName("Select Calibration Point");
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JRadioButton pointRadio;
    private javax.swing.JRadioButton pointRadio1;
    private javax.swing.JRadioButton pointRadio10;
    private javax.swing.JRadioButton pointRadio11;
    private javax.swing.JRadioButton pointRadio12;
    private javax.swing.JRadioButton pointRadio13;
    private javax.swing.JRadioButton pointRadio14;
    private javax.swing.JRadioButton pointRadio15;
    private javax.swing.JRadioButton pointRadio16;
    private javax.swing.JRadioButton pointRadio17;
    private javax.swing.JRadioButton pointRadio18;
    private javax.swing.JRadioButton pointRadio19;
    private javax.swing.JRadioButton pointRadio2;
    private javax.swing.JRadioButton pointRadio20;
    private javax.swing.JRadioButton pointRadio21;
    private javax.swing.JRadioButton pointRadio22;
    private javax.swing.JRadioButton pointRadio23;
    private javax.swing.JRadioButton pointRadio24;
    private javax.swing.JRadioButton pointRadio3;
    private javax.swing.JRadioButton pointRadio4;
    private javax.swing.JRadioButton pointRadio5;
    private javax.swing.JRadioButton pointRadio6;
    private javax.swing.JRadioButton pointRadio7;
    private javax.swing.JRadioButton pointRadio8;
    private javax.swing.JRadioButton pointRadio9;
    private javax.swing.ButtonGroup pointSelectButtonGroup;
    // End of variables declaration//GEN-END:variables
}
