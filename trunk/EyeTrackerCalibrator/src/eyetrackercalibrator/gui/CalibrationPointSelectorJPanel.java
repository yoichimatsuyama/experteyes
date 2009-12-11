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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

/**
 *
 * @author  eeglab
 */
public class CalibrationPointSelectorJPanel extends javax.swing.JPanel {

    JLabel[] label = new JLabel[25];
    JToggleButton[] toggle = new JToggleButton[25];

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
        toggle[	0] = jToggleButton1;
        toggle[	1] = jToggleButton2;
        toggle[	2] = jToggleButton3;
        toggle[	3] = jToggleButton4;
        toggle[	4] = jToggleButton5;
        toggle[	5] = jToggleButton6;
        toggle[	6] = jToggleButton7;
        toggle[	7] = jToggleButton8;
        toggle[	8] = jToggleButton9;
        toggle[	9] = jToggleButton10;
        toggle[	10] = jToggleButton11;
        toggle[	11] = jToggleButton12;
        toggle[	12] = jToggleButton13;
        toggle[	13] = jToggleButton14;
        toggle[	14] = jToggleButton15;
        toggle[	15] = jToggleButton16;
        toggle[	16] = jToggleButton17;
        toggle[	17] = jToggleButton18;
        toggle[	18] = jToggleButton19;
        toggle[	19] = jToggleButton20;
        toggle[	20] = jToggleButton21;
        toggle[	21] = jToggleButton22;
        toggle[	22] = jToggleButton23;
        toggle[	23] = jToggleButton24;
        toggle[	24] = jToggleButton25;

        ActionListener myListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // Disable all other tobble points
                e.getSource();
            }
        }
    }

    /**
     *  Command code for each button is <row>_<column> from 1_1 ... 3_3 according
     *  to position of the button
     */
    public void addActionListener(ActionListener listener) {
        for (int i = 0; i < toggle.length; i++) {
            toggle[i].addActionListener(listener);
        }
    }
    
    /** Return selected index */
    public int getSelectedPointIndex(){
        for (int i = 0; i < toggle.length; i++) {
            if(toggle[i].isSelected()){
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
        pointTypeViewGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        buttonPanel1 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jToggleButton11 = new javax.swing.JToggleButton();
        jToggleButton16 = new javax.swing.JToggleButton();
        jToggleButton21 = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        buttonPanel2 = new javax.swing.JPanel();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton7 = new javax.swing.JToggleButton();
        jToggleButton12 = new javax.swing.JToggleButton();
        jToggleButton17 = new javax.swing.JToggleButton();
        jToggleButton22 = new javax.swing.JToggleButton();
        buttonPanel3 = new javax.swing.JPanel();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton8 = new javax.swing.JToggleButton();
        jToggleButton13 = new javax.swing.JToggleButton();
        jToggleButton18 = new javax.swing.JToggleButton();
        jToggleButton23 = new javax.swing.JToggleButton();
        buttonPanel4 = new javax.swing.JPanel();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton9 = new javax.swing.JToggleButton();
        jToggleButton14 = new javax.swing.JToggleButton();
        jToggleButton19 = new javax.swing.JToggleButton();
        jToggleButton24 = new javax.swing.JToggleButton();
        buttonPanel5 = new javax.swing.JPanel();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton10 = new javax.swing.JToggleButton();
        jToggleButton15 = new javax.swing.JToggleButton();
        jToggleButton20 = new javax.swing.JToggleButton();
        jToggleButton25 = new javax.swing.JToggleButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        primaryShowRadioButton2 = new javax.swing.JRadioButton();
        primaryShowRadioButton = new javax.swing.JRadioButton();
        jLabel25 = new javax.swing.JLabel();
        primaryShowRadioButton1 = new javax.swing.JRadioButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Calibration Point"));

        buttonPanel1.setLayout(new java.awt.GridLayout(6, 1));

        jToggleButton1.setText("M");
        jToggleButton1.setToolTipText("Start marking trial");
        buttonPanel1.add(jToggleButton1);

        jToggleButton6.setText("M");
        buttonPanel1.add(jToggleButton6);

        jToggleButton11.setText("M");
        buttonPanel1.add(jToggleButton11);

        jToggleButton16.setText("M");
        buttonPanel1.add(jToggleButton16);

        jToggleButton21.setText("M");
        buttonPanel1.add(jToggleButton21);

        jPanel3.setMinimumSize(new java.awt.Dimension(20, 100));
        jPanel3.setPreferredSize(new java.awt.Dimension(20, 161));
        jPanel3.setLayout(new java.awt.GridLayout(6, 1));

        jLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel.setText("0");
        jLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel3.add(jLabel);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("0");
        jLabel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel5.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel3.add(jLabel5);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("0");
        jLabel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel10.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel3.add(jLabel10);

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("0");
        jLabel15.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel15.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel3.add(jLabel15);

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("0");
        jLabel20.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel20.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel3.add(jLabel20);

        buttonPanel2.setLayout(new java.awt.GridLayout(6, 1));

        jToggleButton2.setText("M");
        buttonPanel2.add(jToggleButton2);

        jToggleButton7.setText("M");
        buttonPanel2.add(jToggleButton7);

        jToggleButton12.setText("M");
        buttonPanel2.add(jToggleButton12);

        jToggleButton17.setText("M");
        buttonPanel2.add(jToggleButton17);

        jToggleButton22.setText("M");
        buttonPanel2.add(jToggleButton22);

        buttonPanel3.setLayout(new java.awt.GridLayout(6, 1));

        jToggleButton3.setText("M");
        buttonPanel3.add(jToggleButton3);

        jToggleButton8.setText("M");
        buttonPanel3.add(jToggleButton8);

        jToggleButton13.setText("M");
        buttonPanel3.add(jToggleButton13);

        jToggleButton18.setText("M");
        buttonPanel3.add(jToggleButton18);

        jToggleButton23.setText("M");
        buttonPanel3.add(jToggleButton23);

        buttonPanel4.setLayout(new java.awt.GridLayout(6, 1));

        jToggleButton4.setText("M");
        buttonPanel4.add(jToggleButton4);

        jToggleButton9.setText("M");
        buttonPanel4.add(jToggleButton9);

        jToggleButton14.setText("M");
        buttonPanel4.add(jToggleButton14);

        jToggleButton19.setText("M");
        buttonPanel4.add(jToggleButton19);

        jToggleButton24.setText("M");
        buttonPanel4.add(jToggleButton24);

        buttonPanel5.setLayout(new java.awt.GridLayout(6, 1));

        jToggleButton5.setText("M");
        buttonPanel5.add(jToggleButton5);

        jToggleButton10.setText("M");
        buttonPanel5.add(jToggleButton10);

        jToggleButton15.setText("M");
        buttonPanel5.add(jToggleButton15);

        jToggleButton20.setText("M");
        buttonPanel5.add(jToggleButton20);

        jToggleButton25.setText("M");
        buttonPanel5.add(jToggleButton25);

        jPanel8.setMinimumSize(new java.awt.Dimension(20, 100));
        jPanel8.setPreferredSize(new java.awt.Dimension(20, 100));
        jPanel8.setLayout(new java.awt.GridLayout(6, 1));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("0");
        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel1.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel8.add(jLabel1);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("0");
        jLabel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel6.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel8.add(jLabel6);

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("0");
        jLabel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel11.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel8.add(jLabel11);

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("0");
        jLabel16.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel16.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel8.add(jLabel16);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("0");
        jLabel21.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel21.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel8.add(jLabel21);

        jPanel9.setMinimumSize(new java.awt.Dimension(20, 120));
        jPanel9.setPreferredSize(new java.awt.Dimension(20, 120));
        jPanel9.setLayout(new java.awt.GridLayout(6, 1));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("0");
        jLabel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel2.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel9.add(jLabel2);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("0");
        jLabel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel7.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel9.add(jLabel7);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("0");
        jLabel12.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel12.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel9.add(jLabel12);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("0");
        jLabel17.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel17.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel9.add(jLabel17);

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("0");
        jLabel22.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel22.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel9.add(jLabel22);

        jPanel10.setMinimumSize(new java.awt.Dimension(20, 120));
        jPanel10.setPreferredSize(new java.awt.Dimension(20, 120));
        jPanel10.setLayout(new java.awt.GridLayout(6, 1));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("0");
        jLabel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel3.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel10.add(jLabel3);

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("0");
        jLabel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel8.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel10.add(jLabel8);

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("0");
        jLabel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel13.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel10.add(jLabel13);

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("0");
        jLabel18.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel18.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel10.add(jLabel18);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("0");
        jLabel23.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel23.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel10.add(jLabel23);

        jPanel11.setMinimumSize(new java.awt.Dimension(20, 120));
        jPanel11.setPreferredSize(new java.awt.Dimension(20, 120));
        jPanel11.setLayout(new java.awt.GridLayout(6, 1));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("0");
        jLabel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel4.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel11.add(jLabel4);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("0");
        jLabel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel9.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel11.add(jLabel9);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("0");
        jLabel14.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel14.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel11.add(jLabel14);

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("0");
        jLabel19.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel19.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel11.add(jLabel19);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setText("0");
        jLabel24.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel24.setMaximumSize(new java.awt.Dimension(15, 15));
        jPanel11.add(jLabel24);

        pointTypeViewGroup.add(primaryShowRadioButton2);
        primaryShowRadioButton2.setText("Test Points");

        pointTypeViewGroup.add(primaryShowRadioButton);
        primaryShowRadioButton.setSelected(true);
        primaryShowRadioButton.setText("Primary Points");

        jLabel25.setText("Show :");

        pointTypeViewGroup.add(primaryShowRadioButton1);
        primaryShowRadioButton1.setText("Secondary Points");

        org.jdesktop.layout.GroupLayout jPanel12Layout = new org.jdesktop.layout.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel25)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(primaryShowRadioButton)
                .add(21, 21, 21)
                .add(primaryShowRadioButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(primaryShowRadioButton2)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel12Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel25)
                    .add(primaryShowRadioButton)
                    .add(primaryShowRadioButton1)
                    .add(primaryShowRadioButton2))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(buttonPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(6, 6, 6)
                .add(jPanel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
            .add(jPanel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, buttonPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 132, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, buttonPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 132, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, buttonPanel2, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, buttonPanel3, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, buttonPanel4, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        getAccessibleContext().setAccessibleName("Select Calibration Point");
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel1;
    private javax.swing.JPanel buttonPanel2;
    private javax.swing.JPanel buttonPanel3;
    private javax.swing.JPanel buttonPanel4;
    private javax.swing.JPanel buttonPanel5;
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
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton10;
    private javax.swing.JToggleButton jToggleButton11;
    private javax.swing.JToggleButton jToggleButton12;
    private javax.swing.JToggleButton jToggleButton13;
    private javax.swing.JToggleButton jToggleButton14;
    private javax.swing.JToggleButton jToggleButton15;
    private javax.swing.JToggleButton jToggleButton16;
    private javax.swing.JToggleButton jToggleButton17;
    private javax.swing.JToggleButton jToggleButton18;
    private javax.swing.JToggleButton jToggleButton19;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton20;
    private javax.swing.JToggleButton jToggleButton21;
    private javax.swing.JToggleButton jToggleButton22;
    private javax.swing.JToggleButton jToggleButton23;
    private javax.swing.JToggleButton jToggleButton24;
    private javax.swing.JToggleButton jToggleButton25;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JToggleButton jToggleButton8;
    private javax.swing.JToggleButton jToggleButton9;
    private javax.swing.ButtonGroup pointSelectButtonGroup;
    private javax.swing.ButtonGroup pointTypeViewGroup;
    private javax.swing.JRadioButton primaryShowRadioButton;
    private javax.swing.JRadioButton primaryShowRadioButton1;
    private javax.swing.JRadioButton primaryShowRadioButton2;
    // End of variables declaration//GEN-END:variables
}
