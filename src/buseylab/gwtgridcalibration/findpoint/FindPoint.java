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

import buseylab.gwtgridcalibration.gwtgrid.GWTGrid;
import buseylab.gwtgridcalibration.gwtgrid.ImageUtils;
import buseylab.gwtgridcalibration.gwtgrid.MatlabUtils;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class FindPoint implements Runnable {

    File sceneFile, hintsFile;
    double[][][] freqKernels;
    double[] magnitudeResps;
    double[] phaseResps;
    int size;
    double similarity;
    Point point, hint;
    BufferedImage img;
    long totalTime = 0;
    int xHint, yHint;
    String projectRoot;
    private FindPointListener listener = null;

    /**
     *  we are going to pass freqKernels in instead of 
     * reading it from gwtgrids.dat to avoid going reading large objects from disk
     * @param projectRoot String of path end with File.seperator
     * @param sceneFile
     * @param hint
     * @param freqKernels
     * @param listener Null is permitted
     */
    public FindPoint(String projectRoot, File sceneFile, Point hint,
            double[][][] freqKernels, FindPointListener listener) {
        this.projectRoot = projectRoot;
        this.sceneFile = sceneFile;
        this.hint = hint;
        this.freqKernels = freqKernels;
        this.listener = listener;

        magnitudeResps = new double[freqKernels.length * freqKernels[0].length];
        phaseResps = new double[freqKernels.length * freqKernels[0].length];
        size = (int) (Math.sqrt((double) freqKernels[0][0].length / 2.0));
    }

    public long returnTotalTime() {
        return totalTime;
    }

    public void run() {
        try {
            long startTime = System.currentTimeMillis();
            loadGWTGrids();
            img = ImageUtils.loadImage(sceneFile);
            xHint = hint.x;
            yHint = hint.y;
            // make sure screen is not off frame
            if (xHint > -1 && yHint > -1) {

                // we are working with large images now, so there is no need to scale
                int xHintTranslated = xHint;
                int yHintTranslated = yHint;

                // make sure we don't move off the screen at top left
                int UpperLeftX = Math.max(xHintTranslated - (int) (size / 2.0), 0);
                int UpperLeftY = Math.max(yHintTranslated - (int) (size / 2.0), 0);

                // make sure we don't move off the screen at bottom right
                if (UpperLeftX + size >= img.getWidth()) {
                    UpperLeftX = img.getWidth() - size - 1;
                }
                if (UpperLeftY + size >= img.getHeight()) {
                    UpperLeftY = img.getHeight() - size - 1;
                }

                double[] pixels = ImageUtils.RGBtoGrayDouble(ImageUtils.getPixels(img, UpperLeftX, UpperLeftY, size, size));
                GWTGrid gwt = new GWTGrid(pixels, size, freqKernels);

                // write out image for debugging
//                System.out.println("debugging");
//				BufferedImage tmp = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
//				tmp.setRGB(0, 0, size, size, ImageUtils.grayDoubleToRGB(pixels), 0, size);
//				ImageIO.write(tmp, "jpg", new File(projectRoot + "test.jpg"));

                double maxSim = 0;
                Point maxSimIdx = null;
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        // compare img1's mag/phase @ xclick/yclick to every location on img2
                        similarity = MatlabUtils.compareTwoJets(magnitudeResps, gwt.getMagnitudeResp(i, j),
                                phaseResps, gwt.getPhaseResp(i, j));
                        if (similarity > maxSim) {
                            maxSim = similarity;
                            maxSimIdx = new Point(i, j);
                        }
                    }
                }
                // similarity never exceeded zero. negative numbers are impossible, so it must have been NaN
                if (maxSim == 0) {
                    point = new Point(-1, -1);
                    similarity = .5;
                    System.out.println("Couldn't compute similarities. Assuming -1,-1");
                } // all is well
                else {
                    // retranslate point from 128 x 128 to 1440x990
                    Point maxSimIdxTranslated = new Point(UpperLeftX + maxSimIdx.x, UpperLeftY + maxSimIdx.y);
                    similarity = maxSim;
                    point = maxSimIdxTranslated;
                    System.out.println("maxSim=" + maxSim + " @ " + maxSimIdxTranslated.x + "," + maxSimIdxTranslated.y);
                }
            } // corner hints had -1's
            else {
                point = new Point(-1, -1);
                similarity = .5;
                System.out.println("Point off screen. Assuming -1,-1");
            }
            totalTime = System.currentTimeMillis() - startTime;

        } catch (Exception e) {
            e.printStackTrace();
            point = null;
        }
        // Signal completion before terminates
        if (listener != null) {
            listener.completed(this);
        }
    }

    public void loadGWTGrids() throws Exception {
        FileInputStream fis = new FileInputStream(new File(projectRoot,"GWTPoint.dat"));
        ObjectInputStream ois = new ObjectInputStream(fis);
        magnitudeResps = (double[]) ois.readObject();
        phaseResps = (double[]) ois.readObject();
        ois.close();
        fis.close();
    }


    /*
     * Convenience methods for drawing corners 
     */
    public Point getPoint() {
        return point;
    }

    public BufferedImage getImage() {
        return img;
    }

    public Point getHint() {
        return hint;
    }

    public File getSceneFile() {
        return sceneFile;
    }
}
