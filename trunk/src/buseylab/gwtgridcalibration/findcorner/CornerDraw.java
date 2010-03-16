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
package buseylab.gwtgridcalibration.findcorner;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

/*
 * Quick and dirty hack to visualize results on the fly. Needs the scene image,
 * found corners, and corner hints.
 * 
 * Has flakey support to scale the image frame and the drawn results with it
 */
public class CornerDraw extends Canvas {

    JFrame f = new JFrame();
    Container contentPane = f.getContentPane();
    Point[] corners;
    Point[] hints;
    BufferedImage img;
    // search space size. defined elsewhere so make sure to reflect changes
    int SIZE = 256;
    // for drawing
    int MARKER_SIZE = 6;
    // for normalizing
    int smallSceneWidth = 512;
    int smallSceneHeight = 342;

    public CornerDraw(BufferedImage img, Point[] corners, Point[] hints) {
        this.img = img;
        this.corners = corners;
        this.hints = hints;

        setSize(img.getWidth(), img.getHeight());
        contentPane.add(this);
        f.pack();
        f.setVisible(true);
        repaint();
    }

    public void setImage(BufferedImage img) {
        this.img = img;
        repaint();
    }

    public void setCorners(Point[] corners) {
        this.corners = corners;
        repaint();
    }

    public void setHints(Point[] hints) {
        this.hints = hints;
        repaint();
    }

    public void paint(Graphics g) {
        // check for frame resize and resize this accordingly
        setSize(f.getWidth(), f.getHeight());

        // our img is grayscale. make a new one that is rgb to draw into
        BufferedImage RGBimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D RGBimgGraphics = (Graphics2D) RGBimg.getGraphics();
        RGBimgGraphics.drawImage(img, 0, 0, null);

        // draw corners if available
        for (int i = 0; i < corners.length; i++) {
            BasicStroke oldStroke = (BasicStroke) RGBimgGraphics.getStroke();
            RGBimgGraphics.setColor(Color.GREEN);
            Point curCorner = corners[i];
            Point curHint = hints[i];
            if (curCorner != null) {
                // draw horiz/vert
                RGBimgGraphics.drawLine(curCorner.x - MARKER_SIZE, curCorner.y, curCorner.x + MARKER_SIZE, curCorner.y);
                RGBimgGraphics.drawLine(curCorner.x, curCorner.y - MARKER_SIZE, curCorner.x, curCorner.y + MARKER_SIZE);
                // draw diags
                RGBimgGraphics.drawLine(curCorner.x - MARKER_SIZE, curCorner.y - MARKER_SIZE, curCorner.x + MARKER_SIZE, curCorner.y + MARKER_SIZE);
                RGBimgGraphics.drawLine(curCorner.x - MARKER_SIZE, curCorner.y + MARKER_SIZE, curCorner.x + MARKER_SIZE, curCorner.y - MARKER_SIZE);
                // draw rectangular hint space
                RGBimgGraphics.setColor(Color.RED);
                RGBimgGraphics.setStroke(oldStroke);
                oldStroke = (BasicStroke) RGBimgGraphics.getStroke();
                // fat line width
                RGBimgGraphics.setStroke(new BasicStroke(3));
                int scaledHintX = (int) (curHint.x / (double) smallSceneWidth * img.getWidth());
                int scaledHintY = (int) (curHint.y / (double) smallSceneWidth * img.getWidth());
                RGBimgGraphics.drawRect((int) (scaledHintX - SIZE / 2.0), (int) (scaledHintY - SIZE / 2.0), SIZE, SIZE);
                RGBimgGraphics.setStroke(oldStroke);
            }
        }

        // draw scaled image
        g.drawImage(RGBimg, 0, 0, f.getWidth(), f.getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);


    }
}
