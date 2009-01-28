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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/*
 * EyeImagePanel.java
 *
 * Created on March 20, 2008, 10:12 AM
 */
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import logic.RotatedEllipse2D;

/**
 *
 * @author  dwyatte
 * 
 * This class draws images as they are presented via setImage
 * 
 * Also, it maintains a list of 4 drawing shapes -- 2 ellipses and 2 squares
 * that represent the cr/pupil and background/searchspace
 */
public class PaintPanel extends javax.swing.JPanel {

    final static int COLOR_SAMPLING_SIZE = 2;
    RotatedEllipse2D cr, pupil;
    //Rectangle backgroundRect;
    Rectangle searchRect;
    Shape shapeToMove;
    int BG_RECT_SIZE = 25;
    // the image to paint initialized to prevent null point execption
    BufferedImage img = null;
    // Highlight to paint
    LinkedList<BufferedImage> highlightImgList = new LinkedList<BufferedImage>();
    private Point mousePos;
    private ColorCaptureListener colorCaptureListener;
    private ChangeListener searchAreaChangeListener = null;

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // draw the image
        g.drawImage(img, 0, 0, null);

        // draw the highlight
        for (Iterator<BufferedImage> it = highlightImgList.iterator(); it.hasNext();) {
            BufferedImage highlightImg = it.next();
            g.drawImage(highlightImg, 0, 0, null);
        }

        // draw shapes
        g.setXORMode(Color.PINK);

        g.setColor(Color.GREEN);

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();
        if (cr != null) {
            // Somehow there is some translation in the old trandform so we have to observe it
            AffineTransform aff = AffineTransform.getTranslateInstance(
                    oldTransform.getTranslateX(),
                    oldTransform.getTranslateY());
            aff.rotate(cr.getAngle(),
                    cr.getCenterX(),
                    cr.getCenterY());
            g2d.setTransform(aff);
            
            g2d.draw(cr);
            g2d.drawLine((int) cr.getX(), (int) cr.getCenterY(), (int) cr.getMaxX(), (int) cr.getCenterY());
            g2d.setTransform(oldTransform);
        }

        if (pupil != null) {
            // Somehow there is some translation in the old trandform so we have to observe it
            AffineTransform aff = AffineTransform.getTranslateInstance(
                    oldTransform.getTranslateX(),
                    oldTransform.getTranslateY());
            aff.rotate(pupil.getAngle(),
                    pupil.getCenterX(),
                    pupil.getCenterY());
            
            g2d.setTransform(aff);
            g2d.draw(pupil);
            g2d.drawLine((int) pupil.getX(), (int) pupil.getCenterY(), 
                    (int) pupil.getMaxX(), (int) pupil.getCenterY());
            g2d.setTransform(oldTransform);
        }
        g2d.draw(searchRect);
        g2d.setColor(Color.YELLOW);
    }

    /** Creates new form EyeImagePanel */
    public PaintPanel() {
        initComponents();
        this.searchRect = new Rectangle(125, 25, 100, 100);
    }

    public Rectangle getSearchRect() {
        return searchRect;
    }

    public BufferedImage getImage() {
        return img;
    }

    public void addHighlight(BufferedImage img) {
        this.highlightImgList.add(img);
    }

    public void removeHighlight(BufferedImage img) {
        this.highlightImgList.remove(img);
    }

    public void clearHighlight() {
        this.highlightImgList.clear();
    }

    /**
     * shape/img setters
     */
    public void setSearchRect(Rectangle r) {
        searchRect = r;
        if (this.searchAreaChangeListener != null) {
            this.searchAreaChangeListener.stateChanged(new ChangeEvent(this));
        }
        repaint();
    }

    public void setPupil(RotatedEllipse2D e) {
        pupil = e;
    }

    public void setCR(RotatedEllipse2D e) {
        cr = e;
    }

    public void setImage(BufferedImage img) {
        this.img = img;
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 508, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 508, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        // move the selected shape; note that search rect has the lowest priority, since it is the biggest
        if (shapeToMove != null) {
            if (shapeToMove.equals(searchRect)) {
                this.searchRect.translate(
                        evt.getX() - this.mousePos.x,
                        evt.getY() - this.mousePos.y);
                setSearchRect(searchRect);
                shapeToMove = searchRect;
            }
            // Update mouse position
            this.mousePos = evt.getPoint();
        }
    }//GEN-LAST:event_formMouseDragged

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        // Remember the last place we click
        this.mousePos = evt.getPoint();
        // Remember the last 

        // select the shape; note that search rect has lowest priority because it is the biggest
        if (searchRect.contains(evt.getX(), evt.getY())) {
            shapeToMove = searchRect;
        } else {
            shapeToMove = null;
        }
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        // unselect the shape
        shapeToMove = null;
    }//GEN-LAST:event_formMouseReleased

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        if (this.colorCaptureListener != null && this.img != null) {
            // Capture gray color
            Point center = evt.getPoint();

            int topBound = Math.max(0, center.y - COLOR_SAMPLING_SIZE);
            int bottomBound = Math.min(img.getHeight(), center.y + COLOR_SAMPLING_SIZE);
            int leftBound = Math.max(0, center.x - COLOR_SAMPLING_SIZE);
            int rightBound = Math.min(img.getWidth(), center.x + COLOR_SAMPLING_SIZE);

            int totalPix = 0;
            int sumPix = 0;
            for (int x = leftBound; x <= rightBound; x++) {
                for (int y = topBound; y <= bottomBound; y++) {
                    sumPix += (img.getRGB(x, y) & 0xff);
                    totalPix++;
                }
            }

            this.colorCaptureListener.setColor(sumPix / totalPix);
        }
    }//GEN-LAST:event_formMouseClicked

    public void setColorCaptureListener(ColorCaptureListener listener) {
        this.colorCaptureListener = listener;
    }

    public void setSearchAreaChangeListener(ChangeListener listener) {
        this.searchAreaChangeListener = listener;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
