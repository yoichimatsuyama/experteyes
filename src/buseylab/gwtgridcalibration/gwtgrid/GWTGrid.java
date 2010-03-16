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
package buseylab.gwtgridcalibration.gwtgrid;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import javax.imageio.ImageIO;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

public class GWTGrid {

    int size, numScales, numOrientations;
    double[][][] freqKernels;
    DoubleFFT_2D fft;
    double[][][][] magnitudeResps;
    double[][][][] phaseResps;

    public GWTGrid(double[] pixels, int size, double[][][] freqKernels){
        init(freqKernels,size, pixels, null, null);
    }

    /**
     * construct a gwt grid from an image using 4D freq kernels
     * first dimension: scale
     * second dimension: orientation
     * third dimension: x (Bundled in third dim of array)
     * fourth dimension: y (Bundled in third dim of array)
     * @param magnitudeRespsBuffer Buffer with the first 2 D as freqKernels and the last two dim are equal to size.  If null it will be created.
     * @param phaseRespsBuffer Buffer with the first 2 D as freqKernels and the last two dim are equal to size.  If null it will be created.
     */
    public GWTGrid(double[] pixels, int size, double[][][] freqKernels,
            double[][][][] magnitudeRespsBuffer, double[][][][] phaseRespsBuffer) {

        // get phase and magnitude responses using fft shifted image
        init(freqKernels,size, pixels, magnitudeRespsBuffer, phaseRespsBuffer);
    }
    // setup the freq kernels

    public static double[][][] genFreqKernel(int size, int numScales, int numOrientations, double sigma) {
        double[][][] freqKernels = new double[numScales][numOrientations][size * size * 2];

        int xyResL = size;
        int xHalfResL = (int) ((double) size / 2.0);
        int yHalfResL = xHalfResL;
        double kxFactor = 2.0 * Math.PI / (double) xyResL;
        double kyFactor = 2.0 * Math.PI / (double) xyResL;

//		[tx,ty] = meshgrid(-xHalfResL:xHalfResL-1,-yHalfResL:yHalfResL-1);
//		tx = kxFactor*tx;
//		ty = kyFactor*(-ty);

        for (int scale = 0; scale < numScales; scale++) {
            double k0 = (Math.PI / 2.0) * Math.pow((1.0 / Math.sqrt(2.0)), scale);
            for (int orientation = 0; orientation < numOrientations; orientation++) {
                int curX = 0;
                double[][] freqKernel = new double[size][size];
                double kA = Math.PI * (double) orientation / (double) numOrientations;
                double k0X = k0 * Math.cos(kA);
                double k0Y = k0 * Math.sin(kA);
                for (int x = -xHalfResL; x <= xHalfResL - 1; x++) {
                    int curY = 0;
                    for (int y = -yHalfResL; y <= yHalfResL - 1; y++) {
                        double tx = kxFactor * x;
                        double ty = kyFactor * (-y);
                        //              2*pi*(exp(-(Sigma/k0)^2/2*((k0X-tx).^2+(k0Y-ty).^2))-exp(-(Sigma/k0)^2/2*(k0^2+tx.^2+ty.^2)));
                        double newv = 2.0 * Math.PI *
                                (Math.exp(-(sigma * sigma / (k0 * k0)) * 0.5 * (((k0X - tx) * (k0X - tx)) + ((k0Y - ty) * (k0Y - ty)))) -
                                Math.exp(-(sigma * sigma / (k0 * k0)) * 0.5 * ((k0 * k0) + (tx * tx) + (ty * ty))));
                        freqKernel[curX][curY] = newv;
                        curY++;
                    }
                    curX++;
                }
                // fft shift freqkernels
                freqKernel = ImgMod30.shiftOrigin(freqKernel);

                // put freqKernel into complex representation
                // first make freqKernel 1D. then interleave with imag 0s
                double[] zeros = new double[size * size];
                Arrays.fill(zeros, 0.0);

                double[] freqKernel1D = new double[size * size];
                int freqKernel1DIdx = 0;
                for (int tmpY = 0; tmpY < size; tmpY++) {
                    for (int tmpX = 0; tmpX < size; tmpX++) {
                        freqKernel1D[freqKernel1DIdx++] = freqKernel[tmpX][tmpY];
                    }
                }

                double[] complexFreqKernel = MatlabUtils.interleave(freqKernel1D, zeros);

                // copy freqKernel into freqKernels
                for (int tmpX = 0; tmpX < size * size * 2; tmpX++) {
                    freqKernels[scale][orientation][tmpX] = complexFreqKernel[tmpX];
                }
            }
        }
        return freqKernels;
    }

    public static double[][][][] createRespsSpace(int numScales, int numOrientations, int size) {
        return new double[numScales][numOrientations][size][size];
    }

    protected void init(double[][][] freqKernels, int size, double[] pixels,
            double[][][][] magnitudeRespsBuffer, double[][][][] phaseRespsBuffer) {
        numScales = freqKernels.length;
        numOrientations = freqKernels[0].length;
        this.size = size;
        this.freqKernels = freqKernels;
        // fft shift the image
        fft = new DoubleFFT_2D(size, size);
        // interleave our vectors since that is what this implementation of fft needs
        double[] realVector = pixels;
        double[] imaginaryVector = new double[pixels.length];
        Arrays.fill(imaginaryVector, 0.0);
        double[] complexVector = MatlabUtils.interleave(realVector, imaginaryVector);
        fft.complexForward(complexVector);
        if (magnitudeRespsBuffer == null) {
            this.magnitudeResps = createRespsSpace(numScales, numOrientations, size);
        } else {
            this.magnitudeResps = magnitudeRespsBuffer;
        }
        if (phaseRespsBuffer == null) {
            this.phaseResps = createRespsSpace(numScales, numOrientations, size);
        } else {
            this.phaseResps = phaseRespsBuffer;
        }
        // get phase and magnitude responses using fft shifted image
        getPhaseAndMagResps(complexVector);
    }

    private void getPhaseAndMagResps(double[] imgFreq) {

        int allElements = size * size * 2;
        double[] product = new double[imgFreq.length];

        // for all orientations and scales, multiply imgFreq with freqKernel
        for (int scale = 0; scale < numScales; scale++) {
            for (int orientation = 0; orientation < numOrientations; orientation++) {
                MatlabUtils.complexMultiply(freqKernels[scale][orientation], imgFreq, product);
                // now inverse transform the product
                fft.complexInverse(product, true);

                // get rid of our complex numbers and compute magnitude (abs) and phase (angle) repsones
                int curX = 0;
                int curY = 0;
                for (int i = 0; i < allElements/*size * size * 2*/; i += 2) {
                    double realPart = product[i];
                    double imagPart = product[i + 1];

                    magnitudeResps[scale][orientation][curX][curY] = Math.sqrt(
                            realPart * realPart /*Math.pow(realPart, 2.0)*/ +
                            imagPart * imagPart /*Math.pow(imagPart, 2.0)*/);
                    // this is not a perfect translation. possible source of error
                    phaseResps[scale][orientation][curX][curY] = Math.atan2(imagPart, realPart) + Math.PI;
                    curX++;
                    if (curX == size) {
                        curX = 0;
                        curY++;
                    }
                }
            }
        }
    }

    // gets magnitude response for index x,y by stacking the scale/orientation responses
    public double[] getMagnitudeResp(int x, int y) {
        double[] resp = new double[numScales * numOrientations];

        int respIdx = 0;
        for (int scale = 0; scale < numScales; scale++) {
            for (int orientation = 0; orientation < numOrientations; orientation++) {
                resp[respIdx++] = magnitudeResps[scale][orientation][x][y];
            }
        }
        return resp;
    }

    // gets phase response for index x,y by stacking the scale/orientation responses
    public double[] getPhaseResp(int x, int y) {
        double[] resp = new double[numScales * numOrientations];

        int respIdx = 0;
        for (int scale = 0; scale < numScales; scale++) {
            for (int orientation = 0; orientation < numOrientations; orientation++) {
                resp[respIdx++] = phaseResps[scale][orientation][x][y];
            }
        }

        return resp;
    }

    public static void main(String[] args) {
        double sigma = 1.0 * Math.PI;
        int numOrientations = 8;
        int numScales = 5;

        try {
//			// read in images
//			BufferedImage img = ImageIO.read(new File("corner1.jpg"));
//			BufferedImage img2 = ImageIO.read(new File("corner2.jpg"));
//			int size = img.getWidth();

//			long startTime = System.currentTimeMillis();
//			// generate frequency kernel
//			double[][][] freqKernels = GWTGrid.genFreqKernel(size, numScales, numOrientations, sigma);

//			// create gwt grids
//			GWTGrid gwt = new GWTGrid(img, freqKernels);
//			GWTGrid gwt2 = new GWTGrid(img2, freqKernels);

//			// simulated click
//			int xClick = 64;
//			int yClick = 58;

//			double maxSim = 0;
//			Point maxSimIdx = null; 
//			for(int i = 0; i < img2.getWidth(); i++) {
//			for(int j = 0; j < img2.getHeight(); j++) {
//			// compare img1's mag/phase @ xclick/yclick to every location on img2
//			double similarity = MatlabUtils.compareTwoJets(gwt.getMagnitudeResp(xClick, yClick), gwt2.getMagnitudeResp(i,j), 
//			gwt.getPhaseResp(xClick, yClick), gwt2.getPhaseResp(i,j));
//			if(similarity > maxSim) {
//			maxSim = similarity;
//			maxSimIdx = new Point(i, j);
//			}
//			}
//			}
//			long elapsedTime = System.currentTimeMillis() - startTime;
//			System.out.println("maxSim=" + maxSim + ", maxSimIdx=" + maxSimIdx.x + "," + maxSimIdx.y);
//			System.out.println(elapsedTime);


            /*
             * First, make GWT grid for each corner clicked
             * 		GWT grid is 128x128 
             * Then get filter responses at mouse click coordinates using getMagnitudeResp/getPhaseResp
             * 
             * 
             * Now for each image:
             * 		look at CornerHints/scene_xxxxx.txt
             * 			(format is TL, TR, BR, BL)
             * 		get 128x128 window centered at each corner from corner hints file
             * 			if window would go off screen,
             * 				translate window so that the upper left part of window is on the corner
             * 		do similarity comparison
             */


            // read in training image and train on four corners
            int size = 256;
            double[][][] freqKernels = GWTGrid.genFreqKernel(size, numScales, numOrientations, sigma);
            BufferedImage img = ImageIO.read(new File("Corners/scene_02191.jpg"));
            // TL, TR, BR, BL
            Point[] clicks = {new Point(246, 302), new Point(1185, 302), new Point(1188, 849), new Point(234, 826)};
            Point[] translatedClicks = new Point[clicks.length];
            // store in gwtgrids
            GWTGrid[] gwtgrids = new GWTGrid[4];
            for (int corner = 0; corner < 4; corner++) {
                // get image subsection
                double[] pixels = ImageUtils.RGBtoGrayDouble(ImageUtils.getPixels(img, clicks[corner].x - (int) (size / 2.0), clicks[corner].y - (int) (size / 2.0), size, size));
                // translate xClick, yClick these into 128 x 128 space
                int xClickTranslated = (int) (size / 2.0);
                int yClickTranslated = (int) (size / 2.0);
                translatedClicks[corner] = new Point(xClickTranslated, yClickTranslated);
                gwtgrids[corner] = new GWTGrid(pixels, size, freqKernels, null, null);
            }


            long startTime = System.currentTimeMillis();
            BufferedImage img2 = ImageIO.read(new File("Corners/scene_13171.jpg"));
            BufferedReader br = new BufferedReader(new FileReader("Corners/scene_13171.txt"));
            for (int corner = 0; corner < 4; corner++) {
                GWTGrid gwt = gwtgrids[corner];
                String[] hints = br.readLine().split("\\s");
                int xHint = Integer.parseInt(hints[0]);
                int yHint = Integer.parseInt(hints[1]);
                // corner hints are 512 x 342; translate to 1440 x 960
                int xHintTranslated = (int) (xHint / 512.0 * img2.getWidth());
                int yHintTranslated = (int) (yHint / 512.0 * img2.getWidth());

                // make sure we don't move off the screen at top left
                int UpperLeftX = Math.max(xHintTranslated - (int) (size / 2.0), 0);
                int UpperLeftY = Math.max(yHintTranslated - (int) (size / 2.0), 0);

                // make sure we don't move off the screen at bottom right
                if (UpperLeftX + size >= img2.getWidth()) {
                    UpperLeftX = img2.getWidth() - size - 1;
                }
                if (UpperLeftY + size >= img2.getHeight()) {
                    UpperLeftY = img2.getHeight() - size - 1;
                }

                double[] pixels2 = ImageUtils.RGBtoGrayDouble(ImageUtils.getPixels(img2, UpperLeftX, UpperLeftY, size, size));
                GWTGrid gwt2 = new GWTGrid(pixels2, size, freqKernels, null, null);


                double maxSim = 0;
                Point maxSimIdx = null;
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        // compare img1's mag/phase @ xclick/yclick to every location on img2
                        double similarity = MatlabUtils.compareTwoJets(gwt.getMagnitudeResp(translatedClicks[corner].x, translatedClicks[corner].y), gwt2.getMagnitudeResp(i, j),
                                gwt.getPhaseResp(translatedClicks[corner].x, translatedClicks[corner].y), gwt2.getPhaseResp(i, j));
                        if (similarity > maxSim) {
                            maxSim = similarity;
                            maxSimIdx = new Point(i, j);
                        }
                    }
                }
                // retranslate point from 128 x 128 to 1440x990
                Point maxSimIdxTranslated = new Point(UpperLeftX + maxSimIdx.x, UpperLeftY + maxSimIdx.y);
                System.out.println("maxSim=" + maxSim + ", maxSimIdx=" + maxSimIdxTranslated.x + "," + maxSimIdxTranslated.y);
            }
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Elapsed time=" + elapsedTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
