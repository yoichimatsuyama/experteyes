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
 * 
 * @todo implement scaling 
 */
package eyetrackercalibrator.framemanaging;

import eyetrackercalibrator.GlobalConstants;
import eyetrackercalibrator.gui.util.StreamGobbler;
import eyetrackercalibrator.math.Computation;
import eyetrackercalibrator.math.EyeGazeComputing;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.JAI;

/**
 * This class export entire play to series of jpg files
 * @todo change from jpg export to tiff
 * @todo add frame average to eye gaze.
 * @author SQ
 */
public class MovieFrameExporter {

    /**
     * File name constants
     */
    public String EYE_ONLY_FILE_NAME = "eyeOnly";
    public String SCREEN_ONLY_FILE_NAME = "screenOnly";
    public String SIDE_BY_SIDE_FILE_NAME = "sideBySide";
    public String EYE_IN_CORNER_FILE_NAME = "eyeInCorner";
    public String SCREEN_IN_CORNER_FILE_NAME = "screenInCorner";
    private boolean alive;
    int pointMarkLength = 2;
    int cornerMarkLength = 10;
    PropertyChangeListener listener;
    double mainImageScale;
    EyeGazeComputing eyeGazeComputing;
    FrameManager eyeFrameManager;
    ScreenFrameManager screenFrameManager;
    FrameSynchronizor frameSynchronizor;
    private File ffmpegExecutable;
    private ReentrantLock processLock = new ReentrantLock();
    Process process = null;
    private float frameRate;
    private boolean useFullSizeScene;

    /**
     * 
     * @param width
     * @param height
     * @param smallWidth
     * @param smallHeight
     * @param eyeCoeff
     * @param eyeOffset
     * @param screenOffset
     * @param eyeFrameManager
     * @param screenFrameManager
     * @param listener
     */
    public MovieFrameExporter(
            EyeGazeComputing eyeGazeComputing, FrameSynchronizor frameSynchronizor,
            FrameManager eyeFrameManager, ScreenFrameManager screenFrameManager,
            File ffmpegExecutable, float frameRate, PropertyChangeListener listener) {
        this.eyeGazeComputing = eyeGazeComputing;
        this.eyeFrameManager = eyeFrameManager;
        this.screenFrameManager = screenFrameManager;
        this.ffmpegExecutable = ffmpegExecutable;
        this.frameRate = frameRate;
        this.frameSynchronizor = frameSynchronizor;

        this.listener = listener;
    }

    public File getFfmpegExecutable() {
        return ffmpegExecutable;
    }

    public void setFfmpegExecutable(File ffmpegExecutable) {
        this.ffmpegExecutable = ffmpegExecutable;
    }

    /**
     * None blocking version of export.  The thread spawned can be stopped through
     * cancel method call.
     */
    public void exportThread(final File exportDirectory, final String fullSceneDir,
            final double cornerImageScale, final double mainImageScale,
            final int start, final int end,
            final boolean withCorners, final boolean eyeOnly, final boolean screenOnly,
            final boolean sideBySide, final boolean eyeInCorner, final boolean screenInCorner,
            final boolean createMovieFile, final boolean deleteMoviePictureFile,
            final int averageFrames) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                export(exportDirectory, fullSceneDir,
                        cornerImageScale, mainImageScale,
                        start, end, withCorners, eyeOnly,
                        screenOnly, sideBySide, eyeInCorner, screenInCorner,
                        createMovieFile, deleteMoviePictureFile, averageFrames);
            }
        });
        t.start();
    }

    public void export(File exportDirectory, String fullSceneDir,
            double cornerImageScale, double mainImageScale,
            int start, int end,
            boolean withCorners, boolean eyeOnly, boolean sceneOnly,
            boolean sideBySide, boolean eyeInCorner, boolean sceneInCorner,
            boolean createMovieFile, boolean deleteMoviePictureFile,
            int averageFrames) {

        // Reset average eye gaze.  This is necessary or we will get the wrong gaze data.
        resetAverageEyeGaze();

        // Make sure fullSceneDir is in correct form
        if (fullSceneDir != null) {
            File dir = new File(fullSceneDir);
            fullSceneDir = dir.getAbsolutePath() + File.separator;
        }

        this.alive = true;
        Point gazePoint = new Point();
        double scaleFactor = screenFrameManager.getScreenInfoScalefactor();

        Point eyeDefaultSize = new Point(1, 1);
        Point sceneDefaultSize = new Point(1, 1);

        File eyeOnlyDir = null;
        File screenOnlyDir = null;
        File sideBySideDir = null;
        File eyeInCornerDir = null;
        File screenInCornerDir = null;

        // Determine number of digit need for file name
        int digit = Computation.countIntegerDigit(end);

        DecimalFormat numberFormat = new DecimalFormat("####");
        numberFormat.setMinimumIntegerDigits(digit);
        numberFormat.setMaximumIntegerDigits(digit);

        //Example
        //String sb = (nf.format((long) 12)) // Prepare all directories accordingly

        if (!exportDirectory.exists()) {
            exportDirectory.mkdirs();
        }
        if (eyeOnly) {
            eyeOnlyDir = createSubDir("EyeOnly", exportDirectory);
        }
        if (sceneOnly) {
            screenOnlyDir = createSubDir("ScreenOnly", exportDirectory);
        }
        if (sideBySide) {
            sideBySideDir = createSubDir("SideBySide", exportDirectory);
        }
        if (eyeInCorner) {
            eyeInCornerDir = createSubDir("EyeInCorner", exportDirectory);
        }
        if (sceneInCorner) {
            screenInCornerDir = createSubDir("ScreenInCorner", exportDirectory);
        }

        /* Try to determine eye and scene default size by grapping from the first
         * image that we can find
         */
        // Do eye if we need eye
        if (sideBySide || eyeInCorner || eyeOnly) {
            for (int i = start; i <= end; i++) {
                // Get eye frame
                BufferedImage image = eyeFrameManager.getFrame(
                        this.frameSynchronizor.getEyeFrame(i));
                if (image != null) {
                    eyeDefaultSize.x = image.getWidth();
                    eyeDefaultSize.y = image.getHeight();
                    // Terminate loop if the size is reasonable
                    if (eyeDefaultSize.x > 0 && eyeDefaultSize.y > 0) {
                        i = end;
                    }
                }
            }
            // Sanity sheck
            sceneDefaultSize.x = Math.max(1, sceneDefaultSize.x);
            sceneDefaultSize.y = Math.max(1, sceneDefaultSize.y);
        }
        // Do scene if we need scene
        if (sideBySide || eyeInCorner || sceneInCorner || sceneOnly) {
            for (int i = start; i <= end; i++) {
                // Get eye frame
                BufferedImage image;
                if (fullSceneDir != null) {
                    image = screenFrameManager.getFrame(fullSceneDir,
                            this.frameSynchronizor.getSceneFrame(i));
                } else {
                    image = screenFrameManager.getFrame(
                            this.frameSynchronizor.getSceneFrame(i));
                }
                if (image != null) {
                    sceneDefaultSize.x = image.getWidth();
                    sceneDefaultSize.y = image.getHeight();
                    // Terminate loop if the size is reasonable
                    if (sceneDefaultSize.x > 0 && sceneDefaultSize.y > 0) {
                        i = end;
                    }
                }
            }
            // Sanity sheck
            sceneDefaultSize.x = Math.max(1, sceneDefaultSize.x);
            sceneDefaultSize.y = Math.max(1, sceneDefaultSize.y);
        }


        // loop for all frames
        for (int i = start; i <= end && alive; i++) {

            // Create file name
            String fileName = numberFormat.format(i - start + 1) + ".tiff";

            // Get eye frame
            BufferedImage eyeImage = renderEyeImage(
                    this.frameSynchronizor.getEyeFrame(i), eyeFrameManager,
                    eyeDefaultSize, mainImageScale);

            // Get average eye gaze
            Point2D.Double point = getNextAverageEyeGaze(
                    start, averageFrames, scaleFactor);
            if (point != null) {
                gazePoint.setLocation(point);
            } else {
                gazePoint.setLocation(GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE);
            }

            // Get screen frame
            BufferedImage screenImage = renderScreenImage(
                    this.frameSynchronizor.getSceneFrame(i),
                    screenFrameManager, withCorners, gazePoint, sceneDefaultSize,
                    mainImageScale, fullSceneDir);

            // Writing out information
            if (eyeOnly) {
                writeImage(eyeImage,
                        new File(eyeOnlyDir, fileName));
            }

            // Writing out information
            if (sceneOnly) {
                writeImage(screenImage,
                        new File(screenOnlyDir, fileName));
            }

            if (sideBySide) {
                writeImage(renderSideBySideImage(eyeImage, screenImage),
                        new File(sideBySideDir, fileName));
            }

            if (eyeInCorner) {
                writeImage(renderInCornerImage(eyeImage, screenImage, cornerImageScale),
                        new File(eyeInCornerDir, fileName));
            }

            if (sceneInCorner) {
                writeImage(renderInCornerImage(screenImage, eyeImage, cornerImageScale),
                        new File(screenInCornerDir, fileName));
            }

            // Update property
            if (this.listener != null) {
                this.listener.propertyChange(new PropertyChangeEvent(this,
                        "Creating Images", i - 1, i));
            }
        }

        // Run ffmpeg
        // Check if FFMPEG exists
        boolean movieCreatedSuccessfully = true;
        if (createMovieFile && this.ffmpegExecutable != null && this.ffmpegExecutable.exists()) {

            movieCreatedSuccessfully = movieCreatedSuccessfully
                    && createMovie(eyeOnly, "Creating eye only movie", EYE_ONLY_FILE_NAME, digit, eyeOnlyDir);

            movieCreatedSuccessfully = movieCreatedSuccessfully
                    && createMovie(sceneOnly, "Creating screen only movie", SCREEN_ONLY_FILE_NAME, digit, screenOnlyDir);

            movieCreatedSuccessfully = movieCreatedSuccessfully
                    && createMovie(sideBySide, "Creating side by side movie", SIDE_BY_SIDE_FILE_NAME, digit, sideBySideDir);

            movieCreatedSuccessfully = movieCreatedSuccessfully
                    && createMovie(eyeInCorner, "Creating eye in the corner movie", EYE_IN_CORNER_FILE_NAME, digit, eyeInCornerDir);

            movieCreatedSuccessfully = movieCreatedSuccessfully
                    && createMovie(sceneInCorner, "Creating screen in the corner movie",
                    SCREEN_IN_CORNER_FILE_NAME, digit, screenInCornerDir);

            this.processLock.lock();
            this.process = null;
            this.processLock.unlock();
        }


        // Handle temp files accordingly
        for (int i = start; i <= end; i++) {
            // Create file name
            String fileName = numberFormat.format(i - start + 1) + ".tiff";

            String newFileName = numberFormat.format(i) + ".tiff";

            // Handle file
            File file = null;
            if (eyeOnly) {
                file = new File(eyeOnlyDir, fileName);
                if (deleteMoviePictureFile) {
                    file.delete();
                } else {
                    //Remove old file if any
                    File outFile = new File(eyeOnlyDir,
                            EYE_ONLY_FILE_NAME + newFileName);
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    file.renameTo(outFile);
                }
            }

            // Writing out information
            if (sceneOnly) {
                file = new File(screenOnlyDir, fileName);
                if (deleteMoviePictureFile) {
                    file.delete();
                } else {
                    //Remove old file if any
                    File outFile = new File(screenOnlyDir,
                            SCREEN_ONLY_FILE_NAME + newFileName);
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    file.renameTo(outFile);
                }
            }

            if (sideBySide) {
                file = new File(sideBySideDir, fileName);
                if (deleteMoviePictureFile) {
                    file.delete();
                } else {
                    //Remove old file if any
                    File outFile = new File(sideBySideDir,
                            SIDE_BY_SIDE_FILE_NAME + newFileName);
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    file.renameTo(outFile);
                }
            }

            if (eyeInCorner) {
                file = new File(eyeInCornerDir, fileName);
                if (deleteMoviePictureFile) {
                    file.delete();
                } else {
                    //Remove old file if any
                    File outFile = new File(eyeInCornerDir,
                            EYE_IN_CORNER_FILE_NAME + newFileName);
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    file.renameTo(outFile);
                }
            }

            if (sceneInCorner) {
                file = new File(screenInCornerDir, fileName);
                if (deleteMoviePictureFile) {
                    file.delete();
                } else {
                    //Remove old file if any
                    File outFile = new File(screenInCornerDir,
                            SCREEN_IN_CORNER_FILE_NAME + newFileName);
                    if (outFile.exists()) {
                        outFile.delete();
                    }
                    file.renameTo(outFile);
                }
            }

            // Update property
            if (this.listener != null) {
                this.listener.propertyChange(new PropertyChangeEvent(this,
                        "Clean Up Temp Files", i - 1, i));
            }
        }

        // Update property
        if (this.listener != null) {
            if (movieCreatedSuccessfully) {
                this.listener.propertyChange(new PropertyChangeEvent(this,
                        "Completed successfully", end, end));
            } else {
                this.listener.propertyChange(new PropertyChangeEvent(this,
                        "Completed with errors increating movies", end, end));
            }
            //open folder for person
              try {
                        // Show results
                        Desktop.getDesktop().open(exportDirectory);
                    } catch (IOException ex) {
                        //Logger.getLogger(ImportMovieJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }catch(Exception ex){
                        // Ignore every other exception.. This should take care of Missing Desktop in 1.5
                    }
        }
    }

    protected boolean createMovie(boolean eyeOnly, String propertyChangeString, String inputFilePrefix, int digit, File outputdir) {
        if (eyeOnly && alive) {
            // Update property
            if (this.listener != null) {
                this.listener.propertyChange(new PropertyChangeEvent(this, propertyChangeString, -1, -1));
            }
            ProcessBuilder processBuilder = new ProcessBuilder(constructFFMPEGCommandList(inputFilePrefix, digit));
            processBuilder = processBuilder.directory(outputdir);
            this.processLock.lock();
            try {
                process = processBuilder.start();
                StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "Output");
                StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "Error");
                outputGobbler.start();
                errorGobbler.start();
            } catch (Exception ex) {
                Logger.getLogger(MovieFrameExporter.class.getName()).log(Level.SEVERE, null, ex);
                this.processLock.unlock();
                if (this.listener != null) {
                    this.listener.propertyChange(new PropertyChangeEvent(this, "Error: " + propertyChangeString, -1, -1));
                }
                return false;
            }
            this.processLock.unlock();

            // Wait for process
            try {
                int exitCode = this.process.waitFor();
                if (exitCode != 0) {
                    return false;
                }
            } catch (Exception ex) {
                Logger.getLogger(MovieFrameExporter.class.getName()).log(Level.SEVERE, null, ex);
                if (this.listener != null) {
                    this.listener.propertyChange(new PropertyChangeEvent(this, "Error: " + propertyChangeString, -1, -1));
                }

                return false;
            }
        }
        return true;
    }

    private LinkedList<String> constructFFMPEGCommandList(String name, int totalDigitInFileName) {
        LinkedList<String> list = new LinkedList<String>();

        list.add(this.ffmpegExecutable.getAbsolutePath());
        list.add("-sameq");
        list.add("-r");
        list.add(String.valueOf(this.frameRate));
        list.add("-i");
        list.add("%0" + totalDigitInFileName + "d.tiff");
        list.add("-y");
        list.add(name.trim() + ".mov");

        return list;
    }

    /**
     * Create subdirectory 
     * @return null when not successful or create is false
     */
    private File createSubDir(String subdirName, File parentDir) {
        File dir = null;
        // Prepare subdirectories
        dir = new File(parentDir, subdirName);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                return null;
            }
        }
        return dir;
    }
    /**
     * This method get the average eye gaze using medien from current point.
     * @param start the frame of the first eye gaze to be returned
     * @return null if not applicable
     */
    LinkedList<Point.Double> gazeList = null;
    int gazeAverageRangeNextFrame = 0;

    private void resetAverageEyeGaze(){
        this.gazeList = null;
        this.gazeAverageRangeNextFrame = 0;
    }

    private Point.Double getNextAverageEyeGaze(
            int start, int range, double scaleFactor) {
        EyeViewFrameInfo info = null;
        Point2D.Double eyeGaze = null;
        // If this is the first time populate the list
        if (gazeList == null) {
            this.gazeList = new LinkedList<Point.Double>();
            // Go back half the range
            int firstFrame = start - range / 2;
            // Populate the list from start to completion of range
            for (int i = 0; i < range; i++) {
                int eyeFrameNum = this.frameSynchronizor.getEyeFrame(i + firstFrame);
                info = (EyeViewFrameInfo) this.eyeFrameManager.getFrameInfo(
                        eyeFrameNum);

                if (info != null) {
                    // Compute scaled eye gaze
                    eyeGaze = computeEyeGaze(scaleFactor, eyeFrameNum, info);
                    this.gazeList.add(eyeGaze);
                } else {
                    this.gazeList.add(null);
                }
            }
            this.gazeAverageRangeNextFrame = firstFrame + range;
        } else {
            // Fetch the next one
            info = (EyeViewFrameInfo) this.eyeFrameManager.getFrameInfo(
                    this.gazeAverageRangeNextFrame);
            if (info != null) {
                // Compute scaled eye gaze
                eyeGaze = computeEyeGaze(scaleFactor,
                        this.gazeAverageRangeNextFrame, info);
                this.gazeList.addLast(eyeGaze);
            } else {
                this.gazeList.addLast(null);
            }

            // Pop the old one from the list if there is any
            if (!this.gazeList.isEmpty()) {
                this.gazeList.removeFirst();
            }

            this.gazeAverageRangeNextFrame++;
        }

        Point.Double[] pointArray = this.gazeList.toArray(new Point.Double[0]);
        if (pointArray.length > 0) {
            // Find medien of x and y seperately when one exists
            double[] x = new double[pointArray.length];
            double[] y = new double[pointArray.length];
            int totalMissing = 0;
            for (int i = 0; i < pointArray.length; i++) {
                if (pointArray[i] != null) {
                    x[i] = pointArray[i].x;
                    y[i] = pointArray[i].y;
                } else {
                    x[i] = -1;
                    y[i] = -1;
                    totalMissing++;
                }
            }
            Arrays.sort(x);
            Arrays.sort(y);
            int dataLength = pointArray.length - totalMissing;

            int halfPoint = Math.max(dataLength / 2 - 1, 0) + totalMissing;
            if (dataLength > 1 && dataLength % 2 != 0) {
                eyeGaze = new Point2D.Double(
                        (x[halfPoint] + x[halfPoint + 1]) / 2,
                        (y[halfPoint] + y[halfPoint + 1]) / 2);
            } else {
                try
                {eyeGaze = new Point2D.Double(x[halfPoint], y[halfPoint]);}//Need to do error checking on this
                catch (java.lang.ArrayIndexOutOfBoundsException e)
                {eyeGaze = null;}
            }
        } else {
            eyeGaze = null;
        }

        return eyeGaze;
    }

    private Point.Double computeEyeGaze(double scaleFactor, int eyeFrame,
            EyeViewFrameInfo info) {
        // Compute and add the gaze point in
        Point.Double eyeVector = this.eyeGazeComputing.getEyeVector(info);

        // Compute eye gaze point
        Double eyeGaze = new Point2D.Double(GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE);
        Point2D p = this.eyeGazeComputing.computeEyeGaze(eyeFrame,
                eyeVector.x, eyeVector.y);
        if (p != null) {
            eyeGaze.setLocation(p);
        }

        // Scale eyeGaze
        eyeGaze.setLocation(eyeGaze.x * scaleFactor, eyeGaze.y * scaleFactor);
        return eyeGaze;
    }

    private void writeImage(BufferedImage image, File outputFile) {
        // Store the image in the TIFF format.
        JAI.create("filestore", image, outputFile.getAbsolutePath(), "TIFF", null);
    }

    /**
     * For stoping threading system.
     */
    public void cancel() {
        this.alive = false;
        processLock.lock();
        if (this.process != null) {
            // Terminate the ffmpeg
            this.process.destroy();
        }
        processLock.unlock();
    }

    /**
     * Render eye image of the given eye frame
     * @param eyeVector Cannot be null.  The content will be replaced by the 
     *        eyeVector computed by the method for the current frame.  
     *        (GlobalConstants.ERROR_VALUE,GlobalConstants.ERROR_VALUE) is given when eye vector is unavailable.
     */
    private BufferedImage renderEyeImage(
            int i, FrameManager eyeFrameManager, Point eyeDefaultSize, double scale) {

        BufferedImage eyeImage = null;

        Graphics2D g = null;

        BufferedImage image = eyeFrameManager.getFrame(i);
        if (image != null) {
            // Scale image
            int newWidth = (int) (scale * (double) image.getWidth());
            Image scaledImage = image.getScaledInstance(
                    newWidth, -1, Image.SCALE_FAST);

            // Put picture in
            eyeImage = new BufferedImage(
                    scaledImage.getWidth(null), scaledImage.getHeight(null),
                    BufferedImage.TYPE_INT_RGB);

            g = eyeImage.createGraphics();
            g.drawImage(scaledImage, 0, 0, null);
        } else {
            eyeImage = new BufferedImage(
                    eyeDefaultSize.x, eyeDefaultSize.y,
                    BufferedImage.TYPE_INT_RGB);
            g = eyeImage.createGraphics();
            // Fill with black
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, eyeDefaultSize.x, eyeDefaultSize.y);
        }

        EyeViewFrameInfo info =
                (EyeViewFrameInfo) eyeFrameManager.getFrameInfo(i);
        if (info != null) {
            Point[] eyePoints = new Point[2];
            eyePoints[0] = new Point();
            eyePoints[1] = new Point();
            eyePoints[0].setLocation(info.getPupilX() * scale, info.getPupilY() * scale);
            eyePoints[1].setLocation(info.getCorneaReflectX() * scale, info.getCorneaReflectY() * scale);

            Point[] ellisp = makeBoundingBox(info.getPupilFit(), scale);
            if (ellisp != null) {
                g.setColor(Color.GREEN);
                if (info.getPupilAngle() > 0) {
                    Graphics2D g2d = (Graphics2D) g;
                    AffineTransform oldTransform = g2d.getTransform();
                    AffineTransform aff = AffineTransform.getTranslateInstance(
                            oldTransform.getTranslateX(), oldTransform.getTranslateY());
                    Ellipse2D.Double e = new Ellipse2D.Double(
                            ellisp[0].x, ellisp[0].y, ellisp[1].x, ellisp[1].y);
                    aff.rotate(info.getPupilAngle(), e.getCenterX(), e.getCenterY());
                    g2d.setTransform(aff);
                    g2d.draw(e);
                    g2d.setTransform(oldTransform);
                } else {
                    g.drawOval(ellisp[0].x, ellisp[0].y, ellisp[1].x, ellisp[1].y);
                }
            }

            ellisp = makeBoundingBox(info.getCorneaReflectFit(), scale);
            if (ellisp != null) {
                g.setColor(Color.RED);
                g.drawOval(ellisp[0].x, ellisp[0].y, ellisp[1].x, ellisp[1].y);
            }

            drawMarks(g, Color.GREEN, eyePoints);
        }

        return eyeImage;
    }

    private BufferedImage renderScreenImage(
            int i, ScreenFrameManager screenFrameManager, boolean withCorners,
            Point gazePosition, Point sceneDefaultSize, double scale, String fullSceneDir) {

        BufferedImage screenImage = null;

        Graphics2D g = null;

        BufferedImage image;
        if (fullSceneDir != null) {
            image = screenFrameManager.getFrame(fullSceneDir, i);
        } else {
            image = screenFrameManager.getFrame(i);
        }


        if (image != null) {
            // Scale image
            int newWidth = (int) (scale * (double) image.getWidth());
            Image scaledImage = image.getScaledInstance(
                    newWidth, -1, Image.SCALE_FAST);

            // Put picture in
            screenImage = new BufferedImage(
                    scaledImage.getWidth(null), scaledImage.getHeight(null),
                    BufferedImage.TYPE_INT_RGB);

            g = screenImage.createGraphics();
            g.drawImage(scaledImage, 0, 0, null);
        } else {
            screenImage = new BufferedImage(
                    sceneDefaultSize.x, sceneDefaultSize.y,
                    BufferedImage.TYPE_INT_RGB);
            g = screenImage.createGraphics();
            // Fill with black
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, sceneDefaultSize.x, sceneDefaultSize.y);
        }

        Point[] point = new Point[1];
        // Draw gaze point when available
        if (gazePosition != null) {
            // Scale gaze point accordingly
            point[0] = new Point((int) (gazePosition.x * scale),
                    (int) (gazePosition.y * scale));
            //drawMarks(g, Color.RED, point);
            drawReverseMarks(g, Color.yellow, point, screenImage.getWidth(),
                    screenImage.getHeight());
        }

        if (withCorners) {
            ScreenViewFrameInfo info =
                    (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(i);
            if (info != null) {
                Point[] corners = new Point[4];
                for (int j = 0; j < corners.length; j++) {
                    corners[j] = new Point(GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE);
                }

                if (info.getTopLeft() != null) {
                    corners[TOPLEFT].setLocation(info.getTopLeft());
                }
                if (info.getTopRight() != null) {
                    corners[TOPRIGHT].setLocation(info.getTopRight());
                }
                if (info.getBottomLeft() != null) {
                    corners[BOTTOMLEFT].setLocation(info.getBottomLeft());
                }
                if (info.getBottomRight() != null) {
                    corners[BOTTOMRIGHT].setLocation(info.getBottomRight());
                }
                drawCorners(g, Color.GREEN, corners, scale);
            }
        }

        return screenImage;
    }

    private void drawMarks(Graphics2D g, Color color, Point[] points) {
        g.setColor(color);
        if (points != null) {
            for (int i = 0; i < points.length; i++) {
                g.drawLine(
                        points[i].x - pointMarkLength,
                        points[i].y,
                        points[i].x + pointMarkLength,
                        points[i].y);
                g.drawLine(
                        points[i].x,
                        points[i].y - pointMarkLength,
                        points[i].x,
                        points[i].y + pointMarkLength);
            }
        }
    }

    private void drawReverseMarks(Graphics2D g, Color color, Point[] points, int spaceWidth, int spaceHeight) {
        g.setColor(color);
        if (points != null) {
            for (int i = 0; i < points.length; i++) {
                g.drawLine(
                        0,
                        points[i].y,
                        points[i].x - pointMarkLength - 1,
                        points[i].y);
                g.drawLine(
                        points[i].x + pointMarkLength + 1,
                        points[i].y,
                        spaceWidth,
                        points[i].y);
                g.drawLine(
                        points[i].x,
                        0,
                        points[i].x,
                        points[i].y - pointMarkLength - 1);
                g.drawLine(
                        points[i].x,
                        points[i].y + pointMarkLength + 1,
                        points[i].x,
                        spaceHeight);
            }
        }
    }
    static final int TOPLEFT = 0;
    static final int BOTTOMLEFT = 1;
    static final int BOTTOMRIGHT = 2;
    static final int TOPRIGHT = 3;

    private void drawCorners(Graphics2D g, Color color, Point[] points, double scale) {
        // Set color
        g.setColor(color);

        // Paint corners when possible
        if (points[TOPLEFT] != null) {
            g.drawLine(
                    scaleing(points[TOPLEFT].x, scale),
                    scaleing(points[TOPLEFT].y, scale),
                    scaleing(points[TOPLEFT].x, scale),
                    scaleing(points[TOPLEFT].y + cornerMarkLength, scale));
            g.drawLine(
                    scaleing(points[TOPLEFT].x, scale),
                    scaleing(points[TOPLEFT].y, scale),
                    scaleing(points[TOPLEFT].x + cornerMarkLength, scale),
                    scaleing(points[TOPLEFT].y, scale));
        }
        if (points[TOPRIGHT] != null) {
            g.drawLine(
                    scaleing(points[TOPRIGHT].x, scale),
                    scaleing(points[TOPRIGHT].y, scale),
                    scaleing(points[TOPRIGHT].x, scale),
                    scaleing(points[TOPRIGHT].y + cornerMarkLength, scale));
            g.drawLine(
                    scaleing(points[TOPRIGHT].x, scale),
                    scaleing(points[TOPRIGHT].y, scale),
                    scaleing(points[TOPRIGHT].x - cornerMarkLength, scale),
                    scaleing(points[TOPRIGHT].y, scale));
        }
        if (points[BOTTOMRIGHT] != null) {
            g.drawLine(
                    scaleing(points[BOTTOMRIGHT].x, scale),
                    scaleing(points[BOTTOMRIGHT].y, scale),
                    scaleing(points[BOTTOMRIGHT].x, scale),
                    scaleing(points[BOTTOMRIGHT].y - cornerMarkLength, scale));
            g.drawLine(
                    scaleing(points[BOTTOMRIGHT].x, scale),
                    scaleing(points[BOTTOMRIGHT].y, scale),
                    scaleing(points[BOTTOMRIGHT].x - cornerMarkLength, scale),
                    scaleing(points[BOTTOMRIGHT].y, scale));
        }
        if (points[BOTTOMLEFT] != null) {
            g.drawLine(
                    scaleing(points[BOTTOMLEFT].x, scale),
                    scaleing(points[BOTTOMLEFT].y, scale),
                    scaleing(points[BOTTOMLEFT].x, scale),
                    scaleing(points[BOTTOMLEFT].y - cornerMarkLength, scale));
            g.drawLine(
                    scaleing(points[BOTTOMLEFT].x, scale),
                    scaleing(points[BOTTOMLEFT].y, scale),
                    scaleing(points[BOTTOMLEFT].x + cornerMarkLength, scale),
                    scaleing(points[BOTTOMLEFT].y, scale));
        }
    }

    private int scaleing(int v, double scale) {
        return (int) ((double) v * scale);
    }

    private Point[] makeBoundingBox(double[] cornerPoints, double scale) {
        Point[] boundingBox = null;

        if (cornerPoints != null) {
            // Scale corners points
            for (int i = 0; i < cornerPoints.length; i++) {
                cornerPoints[i] *= scale;
            }
            boundingBox = new Point[2];
            boundingBox[0] = new Point((int) cornerPoints[0], (int) cornerPoints[1]);
            boundingBox[1] = new Point((int) cornerPoints[2], (int) cornerPoints[3]);
        }

        return boundingBox;
    }

    private BufferedImage renderSideBySideImage(
            BufferedImage eyeImage, BufferedImage screenImage
            ){//double largerImageScale) {
        BufferedImage image = new BufferedImage(
                eyeImage.getWidth() + screenImage.getWidth(),
                Math.max(eyeImage.getHeight(), screenImage.getHeight()),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = image.createGraphics();

        // Draw eyeImage
        g.drawImage(eyeImage, null, 0, 0);
        g.drawImage(screenImage, null, eyeImage.getWidth(), 0);

        return image;
    }

    private BufferedImage renderInCornerImage(
            BufferedImage cornerImage, BufferedImage mainImage, double cornerImageScale) {

        BufferedImage image = new BufferedImage(
                mainImage.getWidth(), mainImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = image.createGraphics();

        g.drawImage(mainImage, null, 0, 0);

        Image newCornerImage = null;

        // Scale smaller image to fit into bounding box of scaled big image
        int newHeight = (int) ((double) cornerImage.getHeight() * (double) mainImage.getWidth()
                * cornerImageScale / (double) cornerImage.getWidth());

        int mainScaledHeight = (int) ((double) mainImage.getHeight()
                * cornerImageScale);

        if (newHeight > mainScaledHeight) {
            // Height is out of bound after scaling so have to scale by height
            newCornerImage = cornerImage.getScaledInstance(-1,mainScaledHeight, BufferedImage.SCALE_DEFAULT);
        } else {
            // Just scale
            newCornerImage = cornerImage.getScaledInstance(-1,newHeight, BufferedImage.SCALE_DEFAULT);
        }

        g.drawImage(newCornerImage, 0, 0, null);

        // Draw a box around corner image
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, newCornerImage.getWidth(null), newCornerImage.getHeight(null));

        return image;
    }
}
