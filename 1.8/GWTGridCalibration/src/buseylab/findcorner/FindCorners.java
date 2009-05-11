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
package buseylab.findcorner;

import buseylab.gwtgrid.GWTGrid;
import buseylab.gwtgrid.ImageUtils;
import buseylab.gwtgrid.MatlabUtils;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FindCorners implements Runnable {

    public interface CompletionListener {

        /**
         * The method is called when a find corner is completed
         * The parameter passed in is the pointer to the thread
         * which is completed
         */
        public void completed(FindCorners whoIsComplete);
    }
    File sceneFile, hintsFile;
    double[][][] freqKernels;
    double[][] magnitudeResps;
    double[][] phaseResps;
    int size;
    protected Dimension hintSceneDim;
    protected File gwtGridFile;
    protected File outputFile;
    double[] similarities = new double[4];
    Point[] corners = new Point[4];
    Point[] cornerHints = new Point[4];
    BufferedImage img;
    long totalTime = 0;
    int xHint, yHint;
    boolean alive = true;
    private CompletionListener listener;

    // we are going to pass freqKernels in instead of reading it from gwtgrids.dat
    // to avoid going reading large objects from disk
    public FindCorners(File sceneFile, File hintsFile, Dimension hintSceneDim,
            double[][] magnitudeResps, double[][] phaseResps, double[][][] freqKernels,
            File outputFile, CompletionListener listener) {
        this.sceneFile = sceneFile;
        this.hintSceneDim = hintSceneDim;
        this.hintsFile = hintsFile;
        this.freqKernels = freqKernels;
        this.outputFile = outputFile;
        this.listener = listener;

        this.magnitudeResps = magnitudeResps;
        this.phaseResps = phaseResps;
        size = (int) (Math.sqrt((double) freqKernels[0][0].length / 2.0));
    }

    public long returnTotalTime() {
        return totalTime;
    }

    public void run() {
        try {
            long startTime = System.currentTimeMillis();

            img = ImageUtils.loadImage(sceneFile);
            int[] buffer = new int[size * size];
            BufferedReader br = new BufferedReader(new FileReader(hintsFile));

            // Translate corner hints coor to scene coor
            double scale = (double) img.getWidth() / (double) this.hintSceneDim.width;

            // Create buffered space
            double[][][][] magnitudeRespsBuffer = GWTGrid.createRespsSpace(
                    freqKernels.length, freqKernels[0].length, size);
            double[][][][] phaseRespsBuffer  = GWTGrid.createRespsSpace(
                    freqKernels.length, freqKernels[0].length, size);

            for (int corner = 0; corner < 4 && alive; corner++) {
                String[] hints = br.readLine().split("\\s");
                xHint = Integer.parseInt(hints[0]);
                yHint = Integer.parseInt(hints[1]);
                // store the hint
                cornerHints[corner] = new Point(xHint, yHint);
                // make sure screen is not off frame
                if (xHint > -1 && yHint > -1) {
                    int xHintTranslated = (int) (xHint * scale);
                    int yHintTranslated = (int) (yHint * scale);

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

                    double[] pixels = ImageUtils.RGBtoGrayDouble(img.getRGB(UpperLeftX, UpperLeftY, size, size, buffer, 0, size));//ImageUtils.getPixels(img, UpperLeftX, UpperLeftY, size, size));
                    GWTGrid gwt = new GWTGrid(pixels, size, freqKernels,magnitudeRespsBuffer, phaseRespsBuffer);

                    // write out image for debugging
//					BufferedImage tmp = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
//					tmp.setRGB(0, 0, size, size, ImageUtils.grayDoubleToRGB(pixels), 0, size);
//					ImageIO.write(tmp, "jpg", new File("test" + corner + ".jpg"));

                    double maxSim = 0;
                    Point maxSimIdx = null;
                    for (int i = 0; i < size && alive; i++) {
                        for (int j = 0; j < size && alive; j++) {
                            // compare img1's mag/phase @ xclick/yclick to every location on img2
                            double similarity = MatlabUtils.compareTwoJets(
                                    magnitudeResps[corner], gwt.getMagnitudeResp(i, j),
                                    phaseResps[corner], gwt.getPhaseResp(i, j));
                            if (similarity > maxSim) {
                                maxSim = similarity;
                                maxSimIdx = new Point(i, j);
                            }
                        }
                    }
                    // Get rid of things to save mem
                    gwt = null;
                    pixels = null;

                    // Do not go further if we are killed
                    if (this.alive) {
                        // similarity never exceeded zero. negative numbers are impossible, so it must have been NaN
                        if (maxSim > 0) {// all is well
                            // retranslate point from gabor frame to real picture
                            Point maxSimIdxTranslated = new Point(UpperLeftX + maxSimIdx.x, UpperLeftY + maxSimIdx.y);
                            similarities[corner] = maxSim;
                            corners[corner] = maxSimIdxTranslated;
                            System.out.println("maxSim=" + maxSim + " @ " + maxSimIdxTranslated.x + "," + maxSimIdxTranslated.y);
                        } else {
                            corners[corner] = new Point(-1, -1);
                            similarities[corner] = .5;
                            System.out.println("Couldn't compute similarities. Assuming -1,-1");
                        }
                    }
                } // corner hints had -1's
                else {
                    corners[corner] = new Point(-1, -1);
                    similarities[corner] = .5;
                    System.out.println("Corners off screen. Assuming -1,-1");
                }
            }
            // save the corners if not killed
            if (alive) {
                saveCorners();
            }
            totalTime = System.currentTimeMillis() - startTime;

            // Clean up
            br.close();
            img = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get rid of things we don't need


        // Signal completion if any body is listenting
        if (this.listener != null) {
            this.listener.completed(this);
        }
    }

    public void saveCorners() throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.outputFile));
        for (int corner = 0; corner < corners.length; corner++) {
            Point curCorner = corners[corner];
            if (curCorner != null) {
                String output = curCorner.x + "\t" + curCorner.y + "\t" + similarities[corner];
                bw.write(output);
                bw.newLine();
            }
        }
        bw.close();
    }

    /*
     * Convenience methods for drawing corners 
     */
    public Point[] getCorners() {
        return corners;
    }

    public File getImage() {
        return this.sceneFile;
    }

    public Point[] getHints() {
        return cornerHints;
    }

    /** Once killed the class cannot be run again */
    public void kill() {
        this.alive = false;
    }
}
