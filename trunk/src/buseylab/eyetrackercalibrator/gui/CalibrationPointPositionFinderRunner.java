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
package buseylab.eyetrackercalibrator.gui;

import buseylab.gwtgridcalibration.findpoint.FindPointGWTGridSetup;
import buseylab.eyetrackercalibrator.framemanaging.FrameManager;
import buseylab.eyetrackercalibrator.framemanaging.ScreenViewFrameInfo;
import buseylab.gwtgridcalibration.findpoint.FindPointMainThreaded;
import buseylab.eyetrackercalibrator.ImageTools;
import buseylab.eyetrackercalibrator.gui.util.CompletionListener;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.DefaultListModel;

/**
 *
 * @author ruj
 */
public class CalibrationPointPositionFinderRunner extends Thread {

    boolean alive = true;
    private File projectLocation = null;
    private DefaultListModel[] calibrationSet = null;
    private FrameManager screenFrameManager = null;
    private String fullScreenFrameDirectory = null;
    private Point[] calibrationPointFound = null;
    private CompletionListener listener = null;
    private ProgressJDialog progressBar = null;
    private FindPointMainThreaded findPointMainThreaded = null;
    /** A lock to prevent race condition on variable */
    private ReentrantLock lock = new ReentrantLock();

    public CalibrationPointPositionFinderRunner(
            File projectLocation, DefaultListModel[] calibrationSet,
            FrameManager screenFrameManager, String fullScreenFrameDirectory,
            CompletionListener listener) {
        super("Calibration Point Position Finder Runner");
        this.alive = true;
        this.projectLocation = projectLocation;
        this.calibrationSet = calibrationSet;
        this.screenFrameManager = screenFrameManager;
        this.fullScreenFrameDirectory = fullScreenFrameDirectory;
        this.listener = listener;
    }

    @Override
    public synchronized void start() {
        alive = true;
        super.start();
    }

    @Override
    public void run() {
        // For each calibration set
        for (int i = 0; i < calibrationSet.length; i++) {
            // For each calibration range.  Also stop if is killed
            for (Enumeration en = calibrationSet[i].elements();
                    en.hasMoreElements() && alive;) {
                CalibrationInfo info = (CalibrationInfo) en.nextElement();

                // Skip the one which is already processed
                if (!info.isCalibrationPointPositionLocated) {
                    BufferedImage screen = null;

                    FindPointGWTGridSetup gwtGridSetup = new FindPointGWTGridSetup();

                    // Prepare location guide
                    Point hint = new Point();
                    hint.setLocation(info.selectedCalibrationPointPosition);

                    // Load full screen view image for zoom panel
                    // Get first frame
                    String fileName = screenFrameManager.getFrameFileName(
                            info.startSceneFrame);
                    if (fileName != null) {
                        screen = ImageTools.loadImage(
                                new File(this.fullScreenFrameDirectory, fileName));
                    }else{
                        screen = null;
                    }

                    if (screen != null) {
                        gwtGridSetup.train(hint, screen);
                        // @todo Make this more dynamic
                        gwtGridSetup.save(new File(projectLocation, "GWTPoint.dat"));

                        // Create array of all frame location to be procesed
                        File[] screenFileArray =
                                new File[info.stopSceneFrame - info.startSceneFrame + 1];

                        for (int j = 0; j < screenFileArray.length; j++) {
                            fileName = screenFrameManager.getFrameFileName(
                                    info.startSceneFrame + j);

                            screenFileArray[j] = new File(
                                    fullScreenFrameDirectory, fileName);
                        }

                        // Clear old results
                        this.calibrationPointFound = null;
                        lock.lock();
                        if (alive) { // Good point to kill after time consuming process
                            this.findPointMainThreaded =
                                    new FindPointMainThreaded(
                                    projectLocation.getAbsolutePath() + File.separator,
                                    hint, screenFileArray,
                                    Runtime.getRuntime().availableProcessors(),
                                    new buseylab.gwtgridcalibration.findpoint.CompletionListener() {

                                        public boolean completed(Point[] points) {
                                            return rangeProcessCompletedHandler(points);
                                        }
                                    }); // Remove current calibration info
                        }
                        lock.unlock();
                        if (alive) { // Another good point to kill after time consuming process
                            this.findPointMainThreaded.run();
                        }
                        // Don't skip this since it also do cleaning up
                        processCalibrationPointFound(info);
                    }
                }
            }
        }
        listener.fullCompletion();
    }

    /** 
     * Handle completion of calibration point location finding of each 
     * calibration range
     */
    private boolean rangeProcessCompletedHandler(Point[] points) {
        this.calibrationPointFound = points;
        return true;
    }

    /**
     * Kill current running of this thread 
     */
    public void kill() {
        lock.lock();
        alive = false;
        if (this.findPointMainThreaded != null) {
            this.findPointMainThreaded.cancel();
        }
        lock.unlock();
    }

    /** 
     * Helper method to make sure we don't get kill off during write out or
     * writing out incomplete results
     */
    private void processCalibrationPointFound(CalibrationInfo info) {
        // Process calibration points found
        if (this.calibrationPointFound != null) {// && alive) {
            // Clear out the point finding thread
            this.findPointMainThreaded = null;

            if (alive) { // If killed, don't write out results
                for (int j = 0; j < this.calibrationPointFound.length; j++) {
                    ScreenViewFrameInfo screenInfo =
                            (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(
                            info.startSceneFrame + j);
                    if (screenInfo == null) {
                        screenInfo = new ScreenViewFrameInfo();
                    }
                    Point[] screenFocus = screenInfo.getMarkedPoints();
                    if (screenFocus == null) {
                        // If there is no previous point, just create a new one
                        screenFocus = new Point[1];
                        screenFocus[0] = new Point();
                    }
                    // Set focus location
                    screenFocus[0].setLocation(this.calibrationPointFound[j]);
                    screenInfo.setMarkedPoints(screenFocus);

                    // Put back to database
                    screenFrameManager.setFrameInfo(
                            info.startSceneFrame + j, screenInfo);
                }

                // Set completion
                info.isCalibrationPointPositionLocated = true;
            }
        }
    }

    /**
     * The content of the calibrationSet will be modified.
     * @param calibrationSet Array of DefaultListModel to be processed
     */
    public void setCalibrationSet(DefaultListModel[] calibrationSet) {
        this.calibrationSet = calibrationSet;
    }

    public void setProjectLocation(File projectLocation) {
        this.projectLocation = projectLocation;
    }

    public void setScreenFrameManager(FrameManager screenFrameManager) {
        this.screenFrameManager = screenFrameManager;
    }

    public void setFullScreenFrameDirectory(String fullScreenFrameDirectory) {
        this.fullScreenFrameDirectory = fullScreenFrameDirectory;
    }

    public void setListener(CompletionListener listener) {
        this.listener = listener;
    }

    public void setProgressBar(ProgressJDialog progressBar) {
        this.progressBar = progressBar;
    }
}
