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
package eyetrackercalibrator.gui;

import buseylab.gwtgridcalibration.findcorner.FindCorners;
import buseylab.gwtgridcalibration.findcorner.FindCornersMainThreadedAsync;
import buseylab.gwtgridcalibration.findcorner.FindCornersMainThreadedAsync.ProgressListener;
import buseylab.gwtgridcalibration.findcorner.gwtgridsetup.FindCornerTrainingSetup;
import buseylab.cornerselector.CornerSelector;
import eyetrackercalibrator.ImageTools;
import eyetrackercalibrator.framemanaging.ScreenFrameManager;
import eyetrackercalibrator.gui.util.CompletionListener;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JOptionPane;

/**
 *
 * @author ruj
 */
public class DetectCornerRunner extends Thread {

    /** Constants for Gabor jet (GWT Grid)*/
    double sigma = 1.0 * Math.PI;
    int numOrientations = 8;
    int numScales = 8;
    int size = 256;
    //End constatnts------
    private boolean alive = true;
    private CompletionListener listener;
    private ScreenFrameManager screenFrameManager;
    private ErrorMarking[] ranges;
    private String largeSceneDir;
    private int frameRate = 1;
    private File cornerHintDir;
    private File cornerOutputDir;

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public DetectCornerRunner(ScreenFrameManager screenFrameManager,
            ErrorMarking[] ranges, String largeSceneDir, File cornerHintDir,
            File frameInfoDir, CompletionListener listener) {
        super("Detect Corner Thread");
        this.cornerHintDir = cornerHintDir;
        this.listener = listener;
        this.largeSceneDir = largeSceneDir;
        this.screenFrameManager = screenFrameManager;
        this.ranges = ranges;
        this.cornerOutputDir = frameInfoDir;
    }

    @Override
    public void run() {
        // Check output dir existance
        if (!this.cornerOutputDir.exists()) {
            // Try making it
            if (!this.cornerOutputDir.mkdirs()) {
                // Fail to create, inform user and quit
                JOptionPane.showMessageDialog(null, "Cannot create directory : " +
                        this.cornerOutputDir.getAbsolutePath(),
                        "Error Creating Output Dir", JOptionPane.ERROR_MESSAGE);
                if (this.listener != null) {
                    this.listener.fullCompletion();
                }
                return;
            }
        }

        // Compute all files in range
        int totalFrame = 0;
        for (int i = 0; i < this.ranges.length; i++) {
            totalFrame = totalFrame + this.ranges[i].stopSceneFrame -
                    this.ranges[i].startSceneFrame + 1;
        }

        // Create array of files
        File[] largeSceneFiles = new File[totalFrame];
        File[] smallSceneFiles = new File[totalFrame];
        int k = 0;
        for (int i = 0; i < this.ranges.length; i++) {
            for (int j = this.ranges[i].startSceneFrame;
                    j <= this.ranges[i].stopSceneFrame; j++) {
                String fileName = this.screenFrameManager.getFrameFileName(j);

                if (fileName != null) {
                    largeSceneFiles[k] = new File(this.largeSceneDir, fileName);
                    smallSceneFiles[k] =
                            new File(this.screenFrameManager.getFrameDirectory(),
                            fileName);
                }else{
                    largeSceneFiles[k] = null;
                    smallSceneFiles[k] = null;
                }

                k++;
            }
        }

        // Get the scale to compute how big the box should be
        double scale = this.screenFrameManager.getScreenInfoScalefactor();

        final Semaphore sem = new Semaphore(0);

        // Start corner selector to get hints
        CornerSelector cornerSelector =
                new CornerSelector(smallSceneFiles, this.cornerHintDir, this.frameRate);
        cornerSelector.setCornerBoxSize((int) (this.size / 2 * scale));

        // Register to corner selector to detect its termination
        cornerSelector.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                // Signal termination
                sem.release();
            }
        });

        // Start corner selector
        cornerSelector.setVisible(true);

        try {
            // Wait for cornerSelector to terminate
            sem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(DetectCornerRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Check status if we should proceed further
        switch (cornerSelector.getTerminationCause()) {
            case CANCEL:
                // User hits cancel
                if (this.listener != null) {
                    this.listener.fullCompletion();
                }
                return;
        }

        // Get rid of cornerSelector to save memory
        cornerSelector = null;


        /** Create a temp GWTGrid file to store training data */
        File gwtGridFile;
        try {
            gwtGridFile = File.createTempFile("GWTGrid", ".dat");
        } catch (IOException ex) {
            Logger.getLogger(DetectCornerRunner.class.getName()).log(Level.SEVERE, null, ex);
            // Fail so return
            if (this.listener != null) {
                this.listener.fullCompletion();
            }
            return;
        }


        FindCornerTrainingSetup cornerTrainingSetup = new FindCornerTrainingSetup(
                smallSceneFiles, largeSceneFiles, gwtGridFile);

        /** Set up training parameters */
        cornerTrainingSetup.setGWTNumOrientations(numOrientations);
        cornerTrainingSetup.setGWTNumScales(numScales);
        cornerTrainingSetup.setGWTSigma(sigma);
        cornerTrainingSetup.setGWTSize(size);

        cornerTrainingSetup.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                //Signal termination
                sem.release();
            }
        });

        cornerTrainingSetup.setVisible(true);

        try {
            // Wait for user decision
            sem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(DetectCornerRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Check status if we should proceed further
        switch (cornerTrainingSetup.getTerminationCause()) {
            case CANCEL:
                // User hits cancel
                if (this.listener != null) {
                    this.listener.fullCompletion();
                }
                // Erase traing file
                gwtGridFile.delete();
                return;
        }

        // Clear object to save memory
        cornerTrainingSetup = null;

        // Ask how many CPU to use
        int availableProcessor = Runtime.getRuntime().availableProcessors();
        if (availableProcessor > 1) {
            Integer[] cpuChoices = new Integer[availableProcessor];
            for (int i = 0; i < cpuChoices.length; i++) {
                cpuChoices[i] = i + 1;
            }
            // Ask the user how many they want to use
            Integer answer = (Integer) JOptionPane.showInputDialog(null,
                    "Please select how many CPU to be used for detecting corners",
                    "How Many CPU to use", JOptionPane.QUESTION_MESSAGE,
                    (Icon) null, cpuChoices, availableProcessor);
            if (answer == null) {
                // Just quit
                // Erase traing file
                gwtGridFile.delete();
                // Fully complete
                if (this.listener != null) {
                    this.listener.fullCompletion();
                }
                return;
            } else {
                availableProcessor = answer;
            }
        }

        // Get first small scene size
        BufferedImage smallScene = ImageTools.loadImage(smallSceneFiles[0]);
        if (smallScene == null) {
            // Give warning and quit
        } else {
            // Get scene dimension
            final FindCornersMainThreadedAsync findCornersMainThreadedAsync =
                    new FindCornersMainThreadedAsync(largeSceneFiles,
                    new Dimension(smallScene.getWidth(), smallScene.getHeight()),
                    gwtGridFile, this.cornerHintDir, this.cornerOutputDir, null);

            final FindCornerProgressJDialog progressJDialog =
                    new FindCornerProgressJDialog(null, false);
            
            progressJDialog.addWindowListener(new java.awt.event.WindowAdapter() {

                @Override
                public void windowClosed(java.awt.event.WindowEvent evt) {
                    // Kill Find Corner
                    findCornersMainThreadedAsync.kill();
                }
            });
            progressJDialog.setMaximum(smallSceneFiles.length);
            ProgressListener fincornerProgressListener = new ProgressListener() {

                public synchronized void progress(int totalCompleted, FindCorners fc) {
                    progressJDialog.setImage(ImageTools.loadImage(fc.getImage()));

                    Point[] points = fc.getCorners();
                    progressJDialog.setCorners(
                            points[CornerSelector.TL], points[CornerSelector.TR],
                            points[CornerSelector.BL], points[CornerSelector.BR]);
                    progressJDialog.setProgress(totalCompleted);
                }

                public void completed() {
                    sem.release();
                }
            };
            findCornersMainThreadedAsync.setListener(fincornerProgressListener);

            findCornersMainThreadedAsync.setNumThreads(availableProcessor);
            // Run corner detection here
            Thread t = new Thread(findCornersMainThreadedAsync, "Find Corner Main Thread");
            t.start();
            progressJDialog.setVisible(true);
            progressJDialog.requestFocusInWindow();

            try {
                // Wait completion
                sem.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(DetectCornerRunner.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Dispose of progress
            progressJDialog.dispose();
        }

        // Erase traing file
        gwtGridFile.delete();

        //TAB changed- announce that we are complete before loading is finished
        // Fully complete
        if (this.listener != null) {
            this.listener.fullCompletion();
        }

        // Reload frames
        for (int i = 0; i < this.ranges.length; i++) {
            this.screenFrameManager.loadFrames(
                    this.screenFrameManager.getFrameDirectory(),
                    this.cornerOutputDir.getAbsolutePath(),
                    this.ranges[i].startSceneFrame - 1,
                    this.ranges[i].stopSceneFrame - this.ranges[i].startSceneFrame + 1,
                    true);
        }

       
    }

    public void kill() {
        this.alive = false;
    }
}
