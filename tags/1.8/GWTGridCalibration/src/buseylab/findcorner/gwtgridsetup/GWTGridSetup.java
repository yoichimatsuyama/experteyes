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
package buseylab.findcorner.gwtgridsetup;

import buseylab.gwtgrid.GWTGrid;
import buseylab.gwtgrid.ImageUtils;
import buseylab.util.PictureFilenameFilter;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
 * TO DO: Add progress bar when saving at the end since it is a costly operation
 */

/*
 * This class opens a frame with a scene image and lets the user
 * code the initial four corners. 
 * 
 * The frame/image has a zoom feature for accuracy
 */
public class GWTGridSetup extends Canvas implements ChangeListener {
    // we expect the following directories in our project root
    String LARGE_SCENE_ROOT = "LargeCleanedScene/";
    File[] largeScenes;
    JFrame mainFrame = new JFrame();
    Container mainPane = mainFrame.getContentPane();
    JFrame zoomFrame;
    OldZoomJPanel zoomPanel;
    JSlider frameNumSlider;
    BufferedImage scene;
    long framenum = 0;
    int scaledWidth = 720;
    int scaledHeight = 480;
    Point[] corners = {null, null, null, null};
    // enumeration of corners as indices into corners array
    final int TL = 0;
    final int TR = 1;
    final int BR = 2;
    final int BL = 3;
    int whichCorner = TL;

    public GWTGridSetup() {
        try {
            largeScenes = new File(LARGE_SCENE_ROOT).listFiles(new PictureFilenameFilter());
            // set up canvas
            scene = ImageIO.read(largeScenes[(int) framenum]);
            setSize(scene.getWidth(), scene.getHeight());


            // add components to frame
            mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.X_AXIS));
            mainPane.add(this);
            JPanel rightPanel = setupRightPanel();
            mainPane.add(rightPanel);
            mainFrame.pack();
            mainFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JPanel setupRightPanel() {
        JLabel frameNumLabel = new JLabel("Frame Number");
        frameNumSlider = new JSlider(0, largeScenes.length, 0);
        frameNumSlider.addChangeListener(this);

        // make buttons and button group
        JPanel cornerPanel = new JPanel();
        cornerPanel.setLayout(new GridLayout(2, 2, 0, 0));
        // select top left button and set whichCorner to TL
        JRadioButton TLButton = new JRadioButton("Top Left", true);
        whichCorner = TL;
        TLButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                whichCorner = TL;
            }
        });
        JRadioButton TRButton = new JRadioButton("Top Right");
        TRButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                whichCorner = TR;
            }
        });
        JRadioButton BLButton = new JRadioButton("Bot Left");
        BLButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                whichCorner = BL;
            }
        });
        JRadioButton BRButton = new JRadioButton("Bot Right");
        BRButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                whichCorner = BR;
            }
        });
        ButtonGroup bg = new ButtonGroup();
        bg.add(TLButton);
        bg.add(TRButton);
        bg.add(BLButton);
        bg.add(BRButton);
        cornerPanel.add(TLButton);
        cornerPanel.add(TRButton);
        cornerPanel.add(BLButton);
        cornerPanel.add(BRButton);

        // add a button so that we can zoom in on the image
        JButton zoom = new JButton("Zoom");
        zoom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    zoomPanel = new OldZoomJPanel();
                    zoomPanel.addDoneButtonActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            doneZoomingActionPerformed(e);
                        }
                    });
                    zoomFrame = new JFrame();
                    Container zoomPane = zoomFrame.getContentPane();

                    zoomPanel.setImage(scene);
                    zoomPane.add(zoomPanel);
                    zoomFrame.pack();
                    zoomFrame.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // add a button that kills the frame and starts the gwtgrid process
        JButton start = new JButton("Save GWTGrid");
        start.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();
                saveGWTGrids();
            }
        });
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(zoom);
        bottom.add(start);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(frameNumLabel);
        rightPanel.add(frameNumSlider);
        rightPanel.add(cornerPanel);
        rightPanel.add(bottom);

        return rightPanel;
    }

    private void doneZoomingActionPerformed(ActionEvent e) {
        // Get selected point from zooming screen
        Point2D point = zoomPanel.getSelectedPoint();
        if (point != null) {
            // add point to corners
            corners[whichCorner] = new Point((int) point.getX(), (int) point.getY());
            repaint();
        }
        // Swtich back to calibration
        zoomPanel.setImage(null);
        zoomFrame.dispose();
    }

    public void stateChanged(ChangeEvent ce) {
        try {
            JSlider sourceSlider = (JSlider) ce.getSource();
            if (sourceSlider.equals(frameNumSlider)) {
                // update frame and repaint
                framenum = sourceSlider.getValue();
                scene = ImageIO.read(largeScenes[(int) framenum]);
                // nuke corners
                corners[TL] = null;
                corners[TR] = null;
                corners[BL] = null;
                corners[BR] = null;
                repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paint(Graphics g) {
        g.drawImage(scene, 0, 0, null);
        g.setColor(Color.GREEN);
        // draw corners if available
        for (int i = 0; i < corners.length; i++) {
            Point curCorner = corners[i];
            if (curCorner != null) {
                // draw a +
                g.drawLine(curCorner.x - 2, curCorner.y, curCorner.x + 2, curCorner.y);
                g.drawLine(curCorner.x, curCorner.y - 2, curCorner.x, curCorner.y + 2);
            }
        }
    }

    public void saveGWTGrids() {
        try {
            // any changes made here also need to be reflected in FindCornersMain
            double sigma = 1.0 * Math.PI;
            int numOrientations = 8;
            int numScales = 8;
            int size = 256;

            double[][][] freqKernels = GWTGrid.genFreqKernel(size, numScales, numOrientations, sigma);
            // translate the corners
            Point[] translatedClicks = new Point[4];
            // store in gwtgrids
            GWTGrid[] gwtgrids = new GWTGrid[4];

            for (int corner = 0; corner < 4; corner++) {
                // get image subsection
                double[] pixels = ImageUtils.RGBtoGrayDouble(ImageUtils.getPixels(scene, corners[corner].x - (int) (size / 2.0), corners[corner].y - (int) (size / 2.0), size, size));
                // translate xClick, yClick these into 128 x 128 space
                int xClickTranslated = (int) (size / 2.0);
                int yClickTranslated = (int) (size / 2.0);
                translatedClicks[corner] = new Point(xClickTranslated, yClickTranslated);
                gwtgrids[corner] = new GWTGrid(pixels, size, freqKernels);

            // write out image for debugging purposes
//				BufferedImage tmp = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
//				tmp.setRGB(0, 0, size, size, ImageUtils.grayDoubleToRGB(pixels), 0, size);
//				ImageIO.write(tmp, "jpg", new File("train" + corner + ".jpg"));
            }

            // any changes down here need to reflected in FindCorners were we read in gwtgrids.dat
            FileOutputStream fos = new FileOutputStream("gwtgrids.dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            for (int corner = 0; corner < 4; corner++) {
                oos.writeObject(gwtgrids[corner].getMagnitudeResp(translatedClicks[corner].x, translatedClicks[corner].y));
                oos.writeObject(gwtgrids[corner].getPhaseResp(translatedClicks[corner].x, translatedClicks[corner].y));
            }
            // write out the encoded file name
            oos.writeObject(new String(largeScenes[(int) framenum].getName()));
            oos.close();
            fos.close();

            mainFrame.dispose();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new GWTGridSetup();
    }
}
