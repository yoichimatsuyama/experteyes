/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package buseylab.fiteyemodel.logic;

import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 *
 * @author ruj
 */
public class GradientCorrection {

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end.setLocation(end);
    }

    public BufferedImage getGradientMask() {
        return gradientMask;
    }

    public void setGradientMask(BufferedImage gradientMask) {
        this.gradientMask = gradientMask;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLightAdding() {
        return lightAdding;
    }

    /** Accept 0-255 level */
    public void setLightAdding(int lightAdding) {
        this.lightAdding = Math.min(255,Math.max(0,lightAdding));
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start.setLocation(start);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    // Set parameters here
    int lightAdding = 0;
    // Gradient mask
    BufferedImage gradientMask = null;
    
    int width, height;
    Point start = new Point(0, 0);
    Point end = new Point(1, 1);

    /** Make sure you call this once you change a parameter to update your gradient mask or you will be sorry */
    public void updateGradientMask() {
        this.gradientMask = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = (Graphics2D) this.gradientMask.getGraphics();

        GradientPaint gradientPaint = new GradientPaint(this.start, new Color(lightAdding, lightAdding, lightAdding, 255), this.end, new Color(0, 0, 0, 255));

        g2d.setPaint(gradientPaint);
        g2d.fill(new Rectangle(0, 0, this.width, this.height));
    }

    // This method draw Image with corrected gradient on the given graphic
    public void correctGradient(Graphics g) {
        if (this.gradientMask != null) {
//            BufferedImage destination = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
//            Graphics2D g2d = destination.createGraphics();
//
//            g2d.setComposite(AlphaComposite.Clear);
//
//            g2d.drawImage(source, 0, 0, null);
//
//            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, lightAdding));
//            g2d.drawImage(gradientMask, 0, 0, null);
//
//            g2d.dispose();
//
//            g2d = (Graphics2D) g;
//            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
//            g2d.drawImage(destination, 0, 0, null);
            
//            g.drawImage(source, 0, 0, null);
//            g.drawImage(gradientMask, 0, 0, null);

            Graphics2D g2d = (Graphics2D) g;

            // Save old composite
            Composite oldComposite = g2d.getComposite();

            // Set our additive compisite rule
            g2d.setComposite(new AdditionComposite());

            // Draw our gradient mask
            g2d.drawImage(gradientMask, 0, 0, null);
            
            // Restore old composite
            g2d.setComposite(oldComposite);
            g2d.dispose();
        }
    }
}
