/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GradientPanel.java
 *
 * Created on Mar 26, 2010, 3:27:09 PM
 */

package buseylab.fiteyemodel.gui;

/**
 *
 * @author ruj
 */
public class GradientPanel extends javax.swing.JPanel {

    /** Creates new form GradientPanel */
    public GradientPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cornerButtonGroup = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        enableCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        widthLabel = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        heightSlider = new javax.swing.JSlider();
        widthSlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        brightnessIncreaseSlider = new javax.swing.JSlider();
        jPanel3 = new javax.swing.JPanel();
        bottomRightRadioButton = new javax.swing.JRadioButton();
        topLeftRadioButton = new javax.swing.JRadioButton();
        bottomLeftRadioButton = new javax.swing.JRadioButton();
        topRightRadioButton = new javax.swing.JRadioButton();

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        setBackground(new java.awt.Color(255, 255, 255));
        setOpaque(false);

        enableCheckBox.setText("Enable Gradient Correction");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setOpaque(false);

        widthLabel.setText("Width"); // NOI18N

        heightLabel.setText("Height"); // NOI18N

        heightSlider.setMaximum(512);
        heightSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                heightSliderStateChanged(evt);
            }
        });

        widthSlider.setMaximum(512);
        widthSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                widthSliderStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, heightLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, widthLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(heightSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                    .add(widthSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(widthSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(widthLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(heightLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(heightSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel1.setText("Brightness Increase");

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Darkest Corner:"));
        jPanel3.setOpaque(false);

        cornerButtonGroup.add(bottomRightRadioButton);
        bottomRightRadioButton.setText("Bottom Right");
        bottomRightRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottomRightRadioButtonActionPerformed(evt);
            }
        });

        cornerButtonGroup.add(topLeftRadioButton);
        topLeftRadioButton.setText("Top Left");
        topLeftRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topLeftRadioButtonActionPerformed(evt);
            }
        });

        cornerButtonGroup.add(bottomLeftRadioButton);
        bottomLeftRadioButton.setText("Bottom Left");
        bottomLeftRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottomLeftRadioButtonActionPerformed(evt);
            }
        });

        cornerButtonGroup.add(topRightRadioButton);
        topRightRadioButton.setText("Top Right");
        topRightRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topRightRadioButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, topLeftRadioButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, bottomLeftRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(bottomRightRadioButton)
                    .add(topRightRadioButton)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(topLeftRadioButton)
                    .add(topRightRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bottomLeftRadioButton)
                    .add(bottomRightRadioButton)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(enableCheckBox)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(brightnessIncreaseSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(enableCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(brightnessIncreaseSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void widthSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_widthSliderStateChanged
//        // set the rect to it's old location with new width/height
//        Rectangle oldSearchRect = parent.getInteractivePanel().getSearchRect();
//        Rectangle newSearchRect = new Rectangle((int) oldSearchRect.getX(), (int) oldSearchRect.getY(), widthSlider.getValue(), heightSlider.getValue());
//        parent.getInteractivePanel().setSearchRect(newSearchRect);
}//GEN-LAST:event_widthSliderStateChanged

    private void heightSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_heightSliderStateChanged
        // set the rect to it's old location with new width/height
//        Rectangle oldSearchRect = parent.getInteractivePanel().getSearchRect();
//        Rectangle newSearchRect = new Rectangle((int) oldSearchRect.getX(), (int) oldSearchRect.getY(), widthSlider.getValue(), heightSlider.getValue());
//        parent.getInteractivePanel().setSearchRect(newSearchRect);
}//GEN-LAST:event_heightSliderStateChanged

    private void topLeftRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topLeftRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_topLeftRadioButtonActionPerformed

    private void topRightRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topRightRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_topRightRadioButtonActionPerformed

    private void bottomLeftRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottomLeftRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bottomLeftRadioButtonActionPerformed

    private void bottomRightRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottomRightRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bottomRightRadioButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton bottomLeftRadioButton;
    private javax.swing.JRadioButton bottomRightRadioButton;
    private javax.swing.JSlider brightnessIncreaseSlider;
    private javax.swing.ButtonGroup cornerButtonGroup;
    private javax.swing.JCheckBox enableCheckBox;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JSlider heightSlider;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton topLeftRadioButton;
    private javax.swing.JRadioButton topRightRadioButton;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JSlider widthSlider;
    // End of variables declaration//GEN-END:variables

}