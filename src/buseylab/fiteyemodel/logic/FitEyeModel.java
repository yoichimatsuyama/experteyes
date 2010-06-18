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
package buseylab.fiteyemodel.logic;

import ij.ImagePlus;
import buseylab.fiteyemodel.util.TerminationListener;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import org.spaceroots.mantissa.optimization.ConvergenceChecker;
import org.spaceroots.mantissa.optimization.CostException;
import org.spaceroots.mantissa.optimization.CostFunction;
import org.spaceroots.mantissa.optimization.NelderMead;
import org.spaceroots.mantissa.optimization.PointCostPair;
import buseylab.fiteyemodel.util.ParameterList;
import buseylab.fiteyemodel.util.Parameters;

public class FitEyeModel implements Runnable {

    public static final int INDEX_PUPIL_TOP_LEFT_X = 0;
    public static final int INDEX_PUPIL_TOP_LEFT_Y = 1;
    public static final int INDEX_PUPIL_BOTTOM_RIGHT_X = 2;
    public static final int INDEX_PUPIL_BOTTOM_RIGHT_Y = 3;
    public static final int INDEX_CR_TOP_LEFT_X = 4;
    public static final int INDEX_CR_TOP_LEFT_Y = 5;
    public static final int INDEX_CR_BOTTOM_RIGHT_X = 6;
    public static final int INDEX_CR_BOTTOM_RIGHT_Y = 7;

    /**
     *
     * @author dwyatte
     *
     * TO DO:
     * find a way to write out goodness of fit
     *
     *///Class to evaluate the difference between 2 images by sum squared error
//extends canvas because it draws the result
    /******************************************************************************
     *
     *
     *
     *****************************************************************************///class to drive the minimization
    class ImgDiffErr implements CostFunction, ConvergenceChecker {

        public boolean isCRCircle = false;
        public boolean isAlive = true;
        private FittingListener listener;
        // pupil/cr search space
        Rectangle searchRect;
        Rectangle originalSearchRect;
        // the eye model, actual eye image, and a pointer to which image to draw
        BufferedImage eyeModel, eyeImg, imgToDraw;
        Graphics eyeModelGraphics;
        Graphics2D eyeModelGraphics2D;
        // gray values
        int pupilGray, crGray, backgroundGray;
        // pixels for both of our images.
        // eyeModelPixels is in RGB packed bytes (but is a grayscale image so we can just mask off a channel)
        // eyeImgPixels is in grayscale values
        int[] eyeModelPixels, eyeImgPixels;
        // ellipses for the pupil and cr
        RotatedEllipse2D pupil, cr;

        public RotatedEllipse2D getCr() {
            return cr;
        }

        public void setCr(RotatedEllipse2D cr) {
            this.cr = cr;
        }

        public RotatedEllipse2D getPupil() {
            return pupil;
        }

        public void setPupil(RotatedEllipse2D pupil) {
            this.pupil = pupil;
        }
        // convergence threshold: difference between SSE and SSE of last iterations
        double diffThresh = 110;
        // sum of squared error for 2 images
        double SSE, diff;
        double lastSSE = diffThresh;    // SSE of last iteration
        // current parameters
        double[] params;
        // initializes model to an eye Image

        public void initModel(BufferedImage eyeImg) {
            this.eyeImg = eyeImg;

            // Create usable search rec
            this.searchRect = new Rectangle(this.originalSearchRect);
            this.searchRect.width = Math.min(
                    this.eyeImg.getWidth() - this.searchRect.x,
                    this.searchRect.width);
            this.searchRect.height = Math.min(
                    this.eyeImg.getHeight() - this.searchRect.y,
                    this.searchRect.height);

            eyeImgPixels = ImageUtils.RGBtoGray(ImageUtils.getPixels(eyeImg,
                    (int) searchRect.getX(), (int) searchRect.getY(),
                    (int) searchRect.getWidth(), (int) searchRect.getHeight()));
            // create eye model
            eyeModel = new BufferedImage(eyeImg.getWidth(), eyeImg.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            eyeModelGraphics = eyeModel.getGraphics();
            eyeModelGraphics2D = (Graphics2D) eyeModelGraphics;
        }

        /*
         * evaluation function if isCircle is true the cost only care for 7 elements
         * in parames instead of all 8
         */
        @Override
        public double cost(double[] params) throws CostException {
            // save params
            this.params = params;
            /* at each iteration
             * fill background with gray bg color
             * draw pupil with pupil gray and params
             * draw cr with cr gray and params
             * then get pixels for eyeModel and eyeImage and sum squared differences
             * return sum as error
             */
if (1==1){
            if (this.isCRCircle) {
            } else {
                //check for corneal reflection reversals
                if (params[INDEX_CR_BOTTOM_RIGHT_Y] < params[INDEX_CR_TOP_LEFT_Y]) {
                    // System.out.print("Corneal reflection y values are reversed\n");
//reverse values
                    double temp = params[INDEX_CR_BOTTOM_RIGHT_Y];
                    params[INDEX_CR_BOTTOM_RIGHT_Y] = params[INDEX_CR_TOP_LEFT_Y];
                    params[INDEX_CR_TOP_LEFT_Y] = temp;
                }
            }
            //check for corneal reflection reversals
            if (params[INDEX_CR_BOTTOM_RIGHT_X] < params[INDEX_CR_TOP_LEFT_X]) {
                //System.out.print("Corneal reflection x values are reversed\n");
//reverse values
                double temp = params[INDEX_CR_BOTTOM_RIGHT_X];
                params[INDEX_CR_BOTTOM_RIGHT_X] = params[INDEX_CR_TOP_LEFT_X];
                params[INDEX_CR_TOP_LEFT_X] = temp;
            }
}
            eyeModelGraphics.setColor(new Color(backgroundGray, backgroundGray, backgroundGray));
            eyeModelGraphics.fillRect(0, 0, eyeModel.getWidth(), eyeModel.getHeight());


            eyeModelGraphics2D.setColor(new Color(pupilGray, pupilGray, pupilGray));
            pupil.setFrameFromDiagonal(params[INDEX_PUPIL_TOP_LEFT_X],
                    params[INDEX_PUPIL_TOP_LEFT_Y],
                    params[INDEX_PUPIL_BOTTOM_RIGHT_X],
                    params[INDEX_PUPIL_BOTTOM_RIGHT_Y]);

            if (pupil.getAngle() != 0) {
                AffineTransform oldTransform = eyeModelGraphics2D.getTransform();

                eyeModelGraphics2D.setTransform(AffineTransform.getRotateInstance(
                        pupil.getAngle(), pupil.getCenterX(), pupil.getCenterY()));
                eyeModelGraphics2D.fill(pupil);

                eyeModelGraphics2D.setTransform(oldTransform);
            } else {
                eyeModelGraphics2D.fill(pupil);
            }

            eyeModelGraphics2D.setColor(new Color(crGray, crGray, crGray));

            if (crGray > 0) {
                if (this.isCRCircle) {
                    cr.setFrameFromDiagonal(params[INDEX_CR_TOP_LEFT_X],
                            params[INDEX_CR_TOP_LEFT_Y],
                            params[INDEX_CR_BOTTOM_RIGHT_X],
                            params[INDEX_CR_TOP_LEFT_Y]
                            + params[INDEX_CR_BOTTOM_RIGHT_X]
                            - params[INDEX_CR_TOP_LEFT_X]);
                } else {
                    cr.setFrameFromDiagonal(params[INDEX_CR_TOP_LEFT_X],
                            params[INDEX_CR_TOP_LEFT_Y],
                            params[INDEX_CR_BOTTOM_RIGHT_X],
                            params[INDEX_CR_BOTTOM_RIGHT_Y]);
                }
            } else {
                cr.setFrame(0, 0, 0, 0);
            }

            // turn this off when cr is empty
            if (crGray > 0) {
                eyeModelGraphics2D.fill(cr);
            }

            eyeModelPixels = ImageUtils.RGBtoGray(ImageUtils.getPixels(
                    eyeModel, (int) searchRect.getX(), (int) searchRect.getY(),
                    (int) searchRect.getWidth(), (int) searchRect.getHeight()));
            lastSSE = SSE;
            SSE = 0;
            for (int i = 0; i < eyeImgPixels.length; i++) {
                SSE += (eyeModelPixels[i] - eyeImgPixels[i]) * (eyeModelPixels[i] - eyeImgPixels[i]);
            }

            // If there is a listener tell it about current fit
            if (this.listener != null) {
                this.listener.setFit(cr, pupil);
            }

            return SSE;
        }

        // getters/setters
        public void setPupilGray(int pupilGray) {
            this.pupilGray = pupilGray;
        }

        public void setCRGray(int crGray) {
            this.crGray = crGray;
        }

        public void setBackgroundGray(int backgroundGray) {
            this.backgroundGray = backgroundGray;
        }

        public void setSearchRect(Rectangle searchRect) {
            this.originalSearchRect = searchRect;
        }

        public BufferedImage getEyeModel() {
            return eyeModel;
        }

        public double[] getParams() {
            return params;
        }

        /* ignore the simplex points and instead look at the difference between
         * current sse and lastsse to check for convergence
         */
        @Override
        public boolean converged(PointCostPair[] simplex) {
            if (this.isAlive) {
                diff = Math.abs(lastSSE - SSE);

                if (diff < diffThresh) {
                    return true;
                } else {
                    return false;
                }
            } else {
                // Stop the program by forcing to sudden converge
                return true;
            }
        }

        // returns SSE
        public double getSSE() {
            return SSE;
        }

        public void setListener(FittingListener listener) {
            this.listener = listener;
        }
    }
    // gradient descent step size
    final static int STEP_SIZE = 6;
    final static int CR_STEP_SIZE = 6;
    // Create instace of class holding function to be minimized
    ImgDiffErr funct = new ImgDiffErr();    // where to save gaze data
    final static String GAZE_ROOT = "Gaze";    // filenames
    File imageFile;
    File outputFile;
    private GradientCorrection gradientCorrection;
//    /** Flag for determining whether we consider CR as a circle and not an elisp*/
//    public boolean isCRCircle = true;

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
    private Parameters parameters;
    private ParameterList parameterList = null;
    private TerminationListener terminationListener = null;

    /**
     * @output Output file. Null to supress output
     *
     */
    public FitEyeModel(File imageFile, File output, Parameters parameters,
            GradientCorrection gradientCorrection) {
        // set the filename and wait for run()
        if (gradientCorrection != null) {
            this.gradientCorrection = gradientCorrection.clone();
        } else {
            this.gradientCorrection = null;
        }

        this.imageFile = imageFile;
        this.parameters = parameters;
        this.outputFile = output;
    }

    /**
     * Fit eye model create the output file using file name of the image file and
     * placed it in the specified output directory
     * @outputDir output directory.  Null to supress output
     */
    public FitEyeModel(File imageFile, String outputDir, ParameterList parameterList,
            GradientCorrection gradientCorrection) {
        // set the filename and wait for run()
        // set the filename and wait for run()
        if (gradientCorrection != null) {
            this.gradientCorrection = gradientCorrection.clone();
        } else {
            this.gradientCorrection = null;
        }
        this.imageFile = imageFile;
        this.parameterList = parameterList;
        this.parameters = parameterList.getFirstParameters();
        if (outputDir != null) {
            this.outputFile = createOutputFile(imageFile, new File(outputDir));
        } else {
            this.outputFile = null;
        }
    }

    /**
     * Create output file.
     * @param input ImageFile
     * @param gazeDir Output directory
     */
    public static boolean isOutputFileExisting(File input, File gazeDir) {
        if (input == null || gazeDir == null) {
            return false;
        }
        if (!gazeDir.exists()) {
            return false;
        }

        File output = createOutputFileNoCheck(input, gazeDir);

        return output.exists();
    }

    private File createOutputFile(File input, File gazeDir) {
        if (!gazeDir.exists()) {
            gazeDir.mkdirs();
        }
        return createOutputFileNoCheck(input, gazeDir);
    }

    private static File createOutputFileNoCheck(File input, File gazeDir) {
        String inputName = input.getName();
        int cutOff = inputName.lastIndexOf('.');
        String outputName;
        if (cutOff < 0) {
            outputName = inputName;
        } else {
            outputName = inputName.substring(0, cutOff);
        }
        File output = new File(gazeDir, outputName + ".txt");
        return output;
    }

    /** Set listener to current fitting */
    public void setFittingListener(FittingListener listener) {
        this.funct.setListener(listener);
    }

    public void setTerminationListener(TerminationListener listener) {
        this.terminationListener = listener;
    }

    @Override
    public void run() {
        BufferedImage eyeImg;
        String eyeFilename;
        // for benchmarking
        long startTime, endTime, elapsedTime;

        // for outputting gaze data
        FileWriter output;

        NelderMead nm = new NelderMead();

        // these values are set from our/mark's estimates
        double pupilCenterX, pupilCenterY, crCenterX, crCenterY;

        try {
            // parse frame number
            eyeFilename = imageFile.getName();
            System.out.println("Analyzing " + eyeFilename);
            startTime = System.currentTimeMillis();

            // get eye image
            eyeImg = ImageUtils.loadRGBImage(imageFile);

            // Correct gradient if the corrector is provided
            if (this.gradientCorrection != null) {
                this.gradientCorrection.setWidth(eyeImg.getWidth());
                this.gradientCorrection.setHeight(eyeImg.getHeight());
                this.gradientCorrection.updateGradientMask();

                Graphics2D gd = eyeImg.createGraphics();
                this.gradientCorrection.correctGradient(gd);
                gd.dispose();
            }

            RotatedEllipse2D pupil = null;

            // Estimate initial spot for pupil
            pupil = findPupil(eyeImg, this.parameters.searchArea,
                    this.parameters.pupilThreshold, false);

            // Switch to proper paramter according to the estimate pupil location
            if (this.parameterList != null) {
                this.parameters = this.parameterList.get(new Point2D.Double(
                        pupil.getCenterX(), pupil.getCenterY()));
            }

            // Rediscover the spot when we get new parameter 
            pupil = findPupil(eyeImg, this.parameters.searchArea,
                    this.parameters.pupilThreshold,
                    this.parameters.detectPupilAngle);

            // This must be done after parameter look up
            RotatedEllipse2D cr = findCR(eyeImg, this.parameters.searchArea,
                    this.parameters.crThreshold);

            // Check if we have to do unsharpen
            if (this.parameters.unsharpRadious > 0) {
                // Do unsharpen first
                ImagePlus imagePlus = new ImagePlus("Unsharpen", eyeImg);

                ImageUtils.unsharpMask(imagePlus.getProcessor(),
                        this.parameters.unsharpRadious,
                        this.parameters.unsharpFactor);

                //imagePlus.get
                eyeImg = ImageUtils.toBufferedImage(imagePlus.getImage());
            }

            funct.setSearchRect(this.parameters.searchArea);
            funct.setCRGray(this.parameters.crGrayValue);
            funct.setPupilGray(this.parameters.pupilGrayValue);
            funct.setBackgroundGray(this.parameters.backgroundGrayValue);
            funct.setCr(cr);
            funct.setPupil(pupil);
            // All parameters must be set before running initModel
            funct.initModel(eyeImg);

            // set up starts and ends for each iteration
            double[] start;
            if (this.parameters.isCRCircle) {
                start = new double[7];
            } else {
                start = new double[8];
            }

            start[INDEX_PUPIL_TOP_LEFT_X] = pupil.getX();
            start[INDEX_PUPIL_TOP_LEFT_Y] = pupil.getY();
            start[INDEX_PUPIL_BOTTOM_RIGHT_X] = pupil.getMaxX();
            start[INDEX_PUPIL_BOTTOM_RIGHT_Y] = pupil.getMaxY();
            start[INDEX_CR_TOP_LEFT_X] = cr.getX();
            start[INDEX_CR_TOP_LEFT_Y] = cr.getY();
            start[INDEX_CR_BOTTOM_RIGHT_X] = cr.getMaxX();
            if (!this.parameters.isCRCircle) {
                start[INDEX_CR_BOTTOM_RIGHT_Y] = cr.getMaxY();
            }

            double[] end = new double[start.length];
            for (int j = 0; j < end.length; j++) {
                if (j < 5) {
                    end[j] = start[j] + STEP_SIZE;
                } else {
                    end[j] = start[j] + CR_STEP_SIZE;
                }
            }

            // Lets move back from starting point be step size as well
            for (int j = 0; j < start.length; j++) {
                if (j < 5) {
                    start[j] -= STEP_SIZE;
                } else {
                    start[j] -= CR_STEP_SIZE;
                }
            }


            // Set if we are considering CR as circle
            funct.isCRCircle = this.parameters.isCRCircle;

            // nelder-mead minimization
            nm.minimizes(funct, 1000, funct, start, end);

            // now that the minimization is complete, get the final parameters
            double[] params = funct.getParams();

            if (1==1)
            {
            if (this.parameters.isCRCircle) {
            } else {
                //check for corneal reflection reversals
                if (params[INDEX_CR_BOTTOM_RIGHT_Y] < params[INDEX_CR_TOP_LEFT_Y]) {
                    //System.out.print("Corneal reflection y values are reversed. Fixing...\n");
//reverse values
                    double temp = params[INDEX_CR_BOTTOM_RIGHT_Y];
                    params[INDEX_CR_BOTTOM_RIGHT_Y] = params[INDEX_CR_TOP_LEFT_Y];
                    params[INDEX_CR_TOP_LEFT_Y] = temp;
                }
            }
            //check for corneal reflection reversals
            if (params[INDEX_CR_BOTTOM_RIGHT_X] < params[INDEX_CR_TOP_LEFT_X]) {
                // System.out.print("Corneal reflection x values are reversed. Fixing...\n");
//reverse values
                double temp = params[INDEX_CR_BOTTOM_RIGHT_X];
                params[INDEX_CR_BOTTOM_RIGHT_X] = params[INDEX_CR_TOP_LEFT_X];
                params[INDEX_CR_TOP_LEFT_X] = temp;
            }
            }

            pupilCenterX = params[INDEX_PUPIL_TOP_LEFT_X]
                    + ((params[INDEX_PUPIL_BOTTOM_RIGHT_X] - params[INDEX_PUPIL_TOP_LEFT_X]) / 2.0);
            pupilCenterY = params[INDEX_PUPIL_TOP_LEFT_Y]
                    + ((params[INDEX_PUPIL_BOTTOM_RIGHT_Y] - params[INDEX_PUPIL_TOP_LEFT_Y]) / 2.0);
            crCenterX = params[INDEX_CR_TOP_LEFT_X]
                    + ((params[INDEX_CR_BOTTOM_RIGHT_X] - params[INDEX_CR_TOP_LEFT_X]) / 2.0);
            if (this.parameters.isCRCircle) {
                crCenterY = params[INDEX_CR_TOP_LEFT_Y]
                        + ((params[INDEX_CR_BOTTOM_RIGHT_X] - params[INDEX_CR_TOP_LEFT_X]) / 2.0);
            } else {
                crCenterY = params[INDEX_CR_TOP_LEFT_Y]
                        + ((params[INDEX_CR_BOTTOM_RIGHT_Y] - params[INDEX_CR_TOP_LEFT_Y]) / 2.0);
            }

            // Write out when there is an output file
            if (this.outputFile != null) {
                output = new FileWriter(this.outputFile);
                output.write(eyeFilename + "\t" + pupilCenterX + "\t" + pupilCenterY + "\t"
                        + crCenterX + "\t" + crCenterY + "\t" + "\n");
                /* also write out our final parameters
                 * TL = top left, BR = bottom right
                 * pupilTLX, pupilTLY, pupilBRX, pupilBRY, crTLX, crTLY, and crBRX
                 * crBRY is written off only when CR is not circle
                 */
                for (int i = 0; i < params.length; i++) {
                    output.write(params[i] + "\t");

                }
                // Special case for circle
                if (this.parameters.isCRCircle) {
                    double topRightY = params[INDEX_CR_TOP_LEFT_Y]
                            + params[INDEX_CR_BOTTOM_RIGHT_X]
                            - params[INDEX_CR_TOP_LEFT_X];

                    // Write height equal to width in case of the circle
                    output.write(topRightY + "\t");
                }
                // also write out our goodness of fit, pupil angle and cr angle
                output.write("\n" + funct.getSSE() + "\t"
                        + pupil.getAngle() + "\t" + cr.getAngle());
                output.close();
            }

            // write out benchmarking
            endTime = System.currentTimeMillis();
            elapsedTime = endTime - startTime;
            System.out.println("Elapsed time=" + (elapsedTime / 1000) + " secs");
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (this.terminationListener != null) {
            this.terminationListener.complete();
        }
    }

    static public RotatedEllipse2D findCR(BufferedImage image, Rectangle searchArea,
            int threshold) {
        return findThresholdEllisp(image, searchArea, threshold, false, false);


    }

    static public RotatedEllipse2D findPupil(BufferedImage image, Rectangle searchArea,
            int threshold, boolean isOriented) {
        return findThresholdEllisp(image, searchArea, threshold, true, isOriented);


    }

    /**
     * This method find a biggest black filled ellisp from input image by
     * chaning it to gray scale picture then threshold the grayscaled image.
     * The ellisp orientation is computed using the moment of the blob
     * @param image Input image
     * @param searchArea Specify the search area
     * @param threshold Threshold value
     * @param isInverted When true the gray scale will be inverted before being
     * @param isOriented When true the angle of ellisp will also be computed
     * thresholded
     */
    static private RotatedEllipse2D findThresholdEllisp(BufferedImage image,
            Rectangle searchArea, int threshold, boolean isInverted,
            boolean isOriented) {

        int[] eyeImgPixels = ImageUtils.RGBtoGray(ImageUtils.getPixels(image,
                searchArea.x,
                searchArea.y,
                searchArea.width,
                searchArea.height));


        int[] threshPixels = ImageUtils.threshold(eyeImgPixels,
                threshold);



        if (isInverted) {
            threshPixels = ImageUtils.invertPixels(threshPixels);


        } // label the image and get the labels
        //ImageLabel il = new ImageLabel(image.getWidth());
        ImageLabel il = new ImageLabel();


        int[] labeledEye = il.doLabel(threshPixels,
                searchArea.width,
                searchArea.height);


        int[] labelColors = il.getLabelColors();

        // labelColors has a zero in it for black. remove it


        int[] tmp = new int[labelColors.length - 1];


        int tmpIdx = 0;


        for (int i = 0; i
                < labelColors.length; i++) {
            if (labelColors[i] != 0) {
                tmp[tmpIdx++] = labelColors[i];


            }
        }

        labelColors = tmp;

        // if label Colors is empty, the whole image was threhsolded to black/white.
        // the frame is probably a blink; return 0,0


        if (labelColors.length == 0) {
            System.out.println("No components found");


            return new RotatedEllipse2D(0, 0, 0, 0, 0);


        } // now calculate the mass of each label
        int[] masses = new int[labelColors.length];


        for (int labelIdx = 0; labelIdx
                < labelColors.length; labelIdx++) {
            int curLabel = labelColors[labelIdx];


            int mass = 0;


            for (int imageIdx = 0; imageIdx
                    < labeledEye.length; imageIdx++) {
                if (labeledEye[imageIdx] == curLabel) {
                    mass++;


                }
            }
            masses[labelIdx] = mass;


        } // component with the largest mass is what we want
        int maxMass = 0;


        int maxMassInd = 0;


        for (int i = 0; i
                < masses.length; i++) {
            if (masses[i] > maxMass) {
                maxMass = masses[i];
                maxMassInd = i;


            }
        }
        // now get its corresponding label
        int crLabel = labelColors[maxMassInd];

        // get the x's and y's associated with the crLabel
        // minX and minY are the top left corner of the ellipse


        int minX = searchArea.width - 1;


        int minY = searchArea.height - 1;
        // maxX and maxY are the width and height


        int maxX = 0;


        int maxY = 0;


        int[][] labeledEye2D = buseylab.fiteyemodel.logic.MatlabUtils.reshape(
                labeledEye, searchArea.width, searchArea.height);



        double Sx = 0;


        double Sy = 0;


        double Sxx = 0;


        double Syy = 0;


        double Sxy = 0;


        double area = 0;



        for (int i = 0; i
                < labeledEye2D.length; i++) {
            for (int j = 0; j
                    < labeledEye2D[0].length; j++) {
                if (labeledEye2D[i][j] == crLabel) {
                    if (i > maxX) {
                        maxX = i;


                    }
                    if (j > maxY) {
                        maxY = j;


                    }
                    if (i < minX) {
                        minX = i;


                    }
                    if (j < minY) {
                        minY = j;


                    }

                    if (isOriented) {
                        // Set up for finding moment
                        Sx += i;
                        Sy += j;
                        Sxx += i * i;
                        Sxy += i * j;
                        Syy += j * j;
                        area++;

                    }


                }
            }
        }

        double angle = 0;


        if (isOriented) {
            // Compute moment angle
            double Mx = Sxx - Sx * Sx / area;


            double My = Syy - Sy * Sy / area;


            double Mxy = Sxy - Sx * Sy / area;
            angle = Math.PI / 2 - Math.atan((Mx - My + Math.sqrt((Mx - My) * (Mx - My) + 4 * Mxy * Mxy)) / (2 * Mxy));


        }

        int crX = minX;


        int crY = minY;


        int crWidth = maxX - minX;


        int crHeight = maxY - minY;

        RotatedEllipse2D ellipse = new RotatedEllipse2D(crX, crY, crWidth, crHeight, angle);

        // Translate coordinate back
        ellipse.x = ellipse.x + searchArea.x;
        ellipse.y = ellipse.y + searchArea.y;



        return ellipse;


    }

    public void kill() {
        this.funct.isAlive = false;

    }
}
