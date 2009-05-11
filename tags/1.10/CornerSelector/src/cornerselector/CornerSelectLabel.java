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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cornerselector;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import javax.swing.Icon;
import javax.swing.JLabel;

/**
 *
 * @author ruj
 */
public class CornerSelectLabel extends JLabel {

    boolean showMouseCornerSelect = true;
//    /** Mouse location.  If off screen e.g. (-1,-1) no Mouse corner select is shown */
//    Point mousePosition = new Point(-1, -1);
    Point[] cornerSelected = new Point[4];
    int selectSize = 10;
    protected static final int TOP_LEFT = 0;
    protected static final int TOP_RIGHT = 1;
    protected static final int BOTTOM_LEFT = 2;
    protected static final int BOTTOM_RIGHT = 3;

    public void setSelectedCorner(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight) {
        cornerSelected[TOP_LEFT] = topLeft;
        cornerSelected[TOP_RIGHT] = topRight;
        cornerSelected[BOTTOM_LEFT] = bottomLeft;
        cornerSelected[BOTTOM_RIGHT] = bottomRight;
    }

    public int getSelectSize() {
        return selectSize;
    }

    /** 
     * The size must not be positive number.  The size is the number of pixels
     * extended from the mouse location for example, 0 means a point  and 10 means
     * a box of 21x21.
     */
    public void setSelectSize(int selectSize) {
        this.selectSize = selectSize;
    }

    public boolean isShowMouseCornerSelect() {
        return showMouseCornerSelect;
    }

    public void setShowMouseCornerSelect(boolean showMouseCornerSelect) {
        this.showMouseCornerSelect = showMouseCornerSelect;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        int boxSize = selectSize * 2 + 1;

        Point mousePosition = this.getMousePosition();

        // Show point
        if (showMouseCornerSelect && mousePosition != null) {
            g.setColor(Color.YELLOW);
            g.drawRect(
                    mousePosition.x - selectSize, mousePosition.y - selectSize,
                    boxSize, boxSize);
        }

        g.setXORMode(Color.PINK);

        // Draw corners present
        g.setColor(Color.GREEN);
        for (int i = 0; i < cornerSelected.length; i++) {
            Point point = cornerSelected[i];
            if (point != null) {
                g.drawRect(
                        point.x - selectSize, point.y - selectSize,
                        boxSize, boxSize);
                // Draw a line to indicate corner type
                switch (i) {
                    case TOP_LEFT:
                        g.drawLine(
                                point.x + selectSize, point.y - selectSize,
                                point.x - selectSize, point.y + selectSize);
                        g.drawLine(
                                point.x, point.y,
                                point.x - selectSize, point.y - selectSize);
                        break;
                    case TOP_RIGHT:
                        g.drawLine(
                                point.x - selectSize, point.y - selectSize,
                                point.x + selectSize, point.y + selectSize);
                        g.drawLine(
                                point.x, point.y,
                                point.x + selectSize, point.y - selectSize);
                        break;
                    case BOTTOM_LEFT:
                        g.drawLine(
                                point.x - selectSize, point.y - selectSize,
                                point.x + selectSize, point.y + selectSize);
                        g.drawLine(
                                point.x, point.y,
                                point.x - selectSize, point.y + selectSize);
                        break;
                    case BOTTOM_RIGHT:
                        g.drawLine(
                                point.x + selectSize, point.y - selectSize,
                                point.x - selectSize, point.y + selectSize);
                        g.drawLine(
                                point.x, point.y,
                                point.x + selectSize, point.y + selectSize);
                        break;
                }
            }
        }
    }

    private void init() {
    }

    public CornerSelectLabel() {
        init();
    }

    public CornerSelectLabel(Icon image) {
        super(image);
        init();
    }

    public CornerSelectLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
        init();
    }

    public CornerSelectLabel(String text) {
        super(text);
        init();
    }

    public CornerSelectLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        init();
    }

    public CornerSelectLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        init();
    }
}
