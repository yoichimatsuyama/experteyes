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
package buseylab.gwtgridcalibration.findpoint;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/*
 * Quick and dirty hack to visualize results on the fly. Needs the scene image,
 * found corners, and corner hints.
 * 
 * Has flakey support to scale the image frame and the drawn results with it
 */
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

public class ProgressDraw extends JLabel {

    JFrame f = new JFrame();
    Container contentPane = f.getContentPane();
    // 0 - 100 % progress bar
    JProgressBar progressBar = new JProgressBar(0, 100);
    Point point;
    Point hint;
    BufferedImage img;
    // search space size. defined elsewhere so make sure to reflect changes
    int SIZE = 256;
    // for drawing
    int MARKER_SIZE = 6;
    // for normalizing
    int smallSceneWidth = 512;
    int smallSceneHeight = 342;

    public ProgressDraw(BufferedImage img, Point point, Point hint) {
        super();
        this.img = img;
        this.point = point;
        this.hint = hint;

        initComponents();
        f.setVisible(true);
    }

    public void setImage(BufferedImage img) {
        setIcon(new ImageIcon(img));
        this.img = img;
        repaint();
    }

    public void setPoint(Point point) {
        this.point = point;
        repaint();
    }

    public void setHint(Point hint) {
        this.hint = hint;
        repaint();
    }

    public void setProgress(String progressString, int progress, int maximum) {
        progressBar.setString(progressString);
        progressBar.setMaximum(maximum);
        progressBar.setValue(progress);
        progressBar.repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (img != null) {
            g.setColor(Color.GREEN);
            if (point != null) {
                // draw horiz/vert
                g.drawLine(point.x - MARKER_SIZE, point.y, point.x + MARKER_SIZE, point.y);
                g.drawLine(point.x, point.y - MARKER_SIZE, point.x, point.y + MARKER_SIZE);
                // draw diags
                g.drawLine(point.x - MARKER_SIZE, point.y - MARKER_SIZE, point.x + MARKER_SIZE, point.y + MARKER_SIZE);
                g.drawLine(point.x - MARKER_SIZE, point.y + MARKER_SIZE, point.x + MARKER_SIZE, point.y - MARKER_SIZE);
                // draw rectangular hint space
                g.setColor(Color.RED);
                g.drawRect((int) (hint.x - SIZE / 2.0), (int) (hint.y - SIZE / 2.0), SIZE, SIZE);
            }
        }

    }

    private void initComponents() {

        this.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        this.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        progressBar = new javax.swing.JProgressBar();

        f.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        jScrollPane1.setViewportView(this);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(f.getContentPane());
        f.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE).add(progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()));

        f.pack();
    }

    /**
     * This method lets us kill this class when it is not needed anymore
     */
    public void die() {
        img = null;
        f.dispose();
    }
}
