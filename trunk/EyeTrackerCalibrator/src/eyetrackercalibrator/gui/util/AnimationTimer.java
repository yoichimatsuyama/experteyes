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
 * AnimationTimer.java
 *
 * Created on September 26, 2007, 10:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package eyetrackercalibrator.gui.util;

import eyetrackercalibrator.framemanaging.EyeViewFrameInfo;
import eyetrackercalibrator.framemanaging.FrameManager;
import eyetrackercalibrator.framemanaging.FrameSynchronizor;
import eyetrackercalibrator.framemanaging.ScreenFrameManager;
import eyetrackercalibrator.framemanaging.ScreenViewFrameInfo;
import eyetrackercalibrator.math.EyeGazeComputing;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.Timer;

/**
 *
 * @author ruj
 */
public class AnimationTimer {

    protected Timer timer = null;
    protected FrameManager eyeFrameManager = null;
    protected ScreenFrameManager screenFrameManager = null;
    protected eyetrackercalibrator.gui.DisplayJPanel displayJPanel;
    protected eyetrackercalibrator.gui.FrameScrollingJPanel eyeFrameScrollingJPanel;
    protected eyetrackercalibrator.gui.FrameScrollingJPanel screenFrameScrollingJPanel;
    Point[] eyePoints = new Point[2];
    protected boolean isRepaint = false;
    protected EyeGazeComputing eyeGazeComputing = null;
    protected FrameSynchronizor frameSynchronizor = new FrameSynchronizor();

    /**
     * Creates a new instance of AnimationTimer
     * You need to call set method for eyeFrameManager, screenFrameManager
     * displayJPanel, eyeFrameScrollingJPanel, screenFrameScrollingJPanel before
     * starting!
     */
    public AnimationTimer() {
        // Go for default at 200 msec / frame
        timer = new Timer(200, new TimerListener());
        timer.setRepeats(true);

        eyePoints[0] = new Point();
        eyePoints[1] = new Point();
    }

    /** For starting animation */
    public void start() {
        timer.start();
    }

    /** For stoping animation */
    public void stop() {
        timer.stop();
    }

    /**
     * This is a method for setting frame rate.  Both panel get the same
     * frame rate
     */
    public void setFrameChangeDelay(int delay) {
        timer.setDelay(delay);
    }

    public EyeGazeComputing getEyeGazeComputing() {
        return eyeGazeComputing;
    }

    public void setEyeGazeComputing(EyeGazeComputing eyeGazeComputing) {
        this.eyeGazeComputing = eyeGazeComputing;
    }

    /** This class hadle updating of frames */
    protected class TimerListener implements ActionListener {

        long lastTime = System.currentTimeMillis();
        int eyeCurrentFrame = 0;
        int screenCurrentFrame = 0;
        Point2D.Double eyeVec = null;

        public void actionPerformed(ActionEvent e) {
            // Holding real frame number in db
            int frameDBLocation;

            long time = System.currentTimeMillis();
            int next = 0;
            // The frame needs to be read first here to prevent out of synch
            // when play
            int eyeCurrent = eyeFrameScrollingJPanel.getCurrentFrame();
            int screenCurrent = screenFrameScrollingJPanel.getCurrentFrame();

            boolean isChange = false;

            // Get and clear repaint flag
            boolean isRepaintRequested = getAndClearRepaint();

            // Advance eye frame
            next = computeNextFrame(
                    eyeCurrent,
                    eyeFrameScrollingJPanel.getFrameRate(),
                    lastTime, time);
            if (next != eyeCurrentFrame || isRepaintRequested) {
                eyeCurrentFrame = next;
                // Set next frame
                eyeFrameScrollingJPanel.setCurrentFrame(next);
                // Set display We get it from panel instead of using next because
                // The panel will make sure we get a valid value.
                frameDBLocation = frameSynchronizor.getEyeFrame(
                        eyeFrameScrollingJPanel.getCurrentFrame());
                BufferedImage image = eyeFrameManager.getFrame(frameDBLocation);
                double scale = setDisplayedImage(image,
                        displayJPanel.getEyeViewDimension(), true);

                // Set pupil and cornia reflection marking rendering
                EyeViewFrameInfo info = (EyeViewFrameInfo) eyeFrameManager.getFrameInfo(new Integer(frameDBLocation));
                if (info != null) {
                    eyePoints[0].x = (int) (info.getPupilX() * scale);
                    eyePoints[0].y = (int) (info.getPupilY() * scale);
                    eyePoints[1].x = (int) (info.getCorneaReflectX() * scale);
                    eyePoints[1].y = (int) (info.getCorneaReflectY() * scale);

                    displayJPanel.setEyeMarkedPoints(eyePoints);
                    // Compute eye vector when eye gaze computing is available.
                    //  It usually is during syncing that the computing is not available
                    if(eyeGazeComputing != null){
                        eyeVec = eyeGazeComputing.getEyeVector(info);
                    }

                    double[] box = info.getPupilFit();
                    if (box != null) {
                        for (int i = 0; i < box.length; i++) {
                            box[i] *= scale;
                        }
                    }
                    displayJPanel.setPupilFit(box, info.getPupilAngle());
                    box = info.getCorneaReflectFit();
                    if (box != null) {
                        for (int i = 0; i < box.length; i++) {
                            box[i] *= scale;
                        }
                    }
                    displayJPanel.setReflectFit(box);

                } else {
                    displayJPanel.setEyeMarkedPoints(null);
                    displayJPanel.setPupilFit(null, 0);
                    displayJPanel.setReflectFit(null);
                }

                isChange = true;
            }

            // Advance screen frame
            next = computeNextFrame(
                    screenCurrent,
                    screenFrameScrollingJPanel.getFrameRate(),
                    lastTime, time);
            if (next != screenCurrentFrame || isRepaintRequested) {
                screenCurrentFrame = next;
                // Set next frame
                screenFrameScrollingJPanel.setCurrentFrame(next);
                //We get it from panel instead of using next because
                // The panel will make sure we get a valid value.
                frameDBLocation = frameSynchronizor.getSceneFrame(
                        screenFrameScrollingJPanel.getCurrentFrame());

                // Set display
                BufferedImage image =
                        screenFrameManager.getFrame(new Integer(frameDBLocation));

                double scale = setDisplayedImage(image,
                        displayJPanel.getScreenViewDimension(), false);

                isChange = true;
                // Set scale
                double originalScale = screenFrameManager.getScreenInfoScalefactor();
                screenFrameManager.setScreenInfoScalefactor(scale * originalScale);

                // Set corner marking
                ScreenViewFrameInfo info =
                        (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(new Integer(frameDBLocation));
                if (info != null) {
                    // Set corners
                    Point topLeft = null;
                    Point topRight = null;
                    Point bottomLeft = null;
                    Point bottomRight = null;
                    if (info.getTopLeft() != null) {
                        topLeft = new Point();
                        topLeft.setLocation(info.getTopLeft());
                    }
                    if (info.getTopRight() != null) {
                        topRight = new Point();
                        topRight.setLocation(info.getTopRight());
                    }
                    if (info.getBottomLeft() != null) {
                        bottomLeft = new Point();
                        bottomLeft.setLocation(info.getBottomLeft());
                    }
                    if (info.getBottomRight() != null) {
                        bottomRight = new Point();
                        bottomRight.setLocation(info.getBottomRight());
                    }
                    displayJPanel.setScreenCorners(
                            topLeft, topRight, bottomLeft, bottomRight);
                    // Set corrected corners
                    Point[] corners = info.getCorrectedCorners();
                    displayJPanel.setScreenCorrectedCorners(
                            corners[ScreenViewFrameInfo.TOPLEFT],
                            corners[ScreenViewFrameInfo.TOPRIGHT],
                            corners[ScreenViewFrameInfo.BOTTOMLEFT],
                            corners[ScreenViewFrameInfo.BOTTOMRIGHT]);
                    // Set marked point
                    displayJPanel.setScreenMarkedPoints(info.getScaledMarkedPoints());
                } else {
                    // Clear current
                    displayJPanel.clearScreenCorners();
                }

                // Eye Gaze point has to be set here because the screen
                // will remove all points when setting a new picture to
                // display
                if (eyeVec != null && eyeGazeComputing != null) {
                    Point2D p = eyeGazeComputing.computeEyeGaze(
                            eyeCurrentFrame, eyeVec.x, eyeVec.y);
                    if (p != null) {
                        displayJPanel.setEyeGaze(p.getX() * scale, p.getY() * scale);
                    } else {
                        displayJPanel.setEyeGaze(-666, -666);
                    }
                }

                // Return scale to original
                screenFrameManager.setScreenInfoScalefactor(originalScale);
            }
            // Advance time
            if (isChange) {
                lastTime = time;
            }
        }

        /**
         * The function does not cap the new frame.  It is assumed there will
         * other control to cap the last frame. e.g. The function can give 1000
         * as next frame while we only have 100 frames available
         */
        protected int computeNextFrame(int currentFrame, int frameRate,
                long lastTime, long currentTime) {
            // Compute time elasp
            long timePass = currentTime - lastTime;

            // Compute next frame
            return (int) (timePass * frameRate / 1000 + currentFrame);
        }

        protected double setDisplayedImage(BufferedImage image, Dimension displayDimension,
                boolean isEyeScreen) {
            double scale = 1d;
            ImageIcon icon = null;
            if (image != null) {
                // Scale image
                int widthDiff = image.getWidth() - displayDimension.width;
                int heightDiff = image.getHeight() - displayDimension.height;
                Image scaledImage = null;
                if (widthDiff > heightDiff) {
                    // We should scale by width
                    scale = (double) displayDimension.width / (double) image.getWidth();
                    scaledImage = image.getScaledInstance(
                            displayDimension.width, -1, Image.SCALE_FAST);
                } else {
                    // We should scale by height
                    scale = (double) displayDimension.height / (double) image.getHeight();
                    scaledImage = image.getScaledInstance(
                            -1, displayDimension.height, Image.SCALE_FAST);
                }
                icon = new ImageIcon(scaledImage);
            }
            if (isEyeScreen) {
                displayJPanel.setEyeViewImage(icon);
            } else {
                displayJPanel.setScreenViewImage(icon);
            }
            return scale;
        }
    }

    public FrameManager getEyeFrameManager() {
        return eyeFrameManager;
    }

    public void setEyeFrameManager(FrameManager eyeFrameManager) {
        this.eyeFrameManager = eyeFrameManager;
    }

    public ScreenFrameManager getScreenFrameManager() {
        return screenFrameManager;
    }

    public void setScreenFrameManager(ScreenFrameManager screenFrameManager) {
        this.screenFrameManager = screenFrameManager;
    }

    public eyetrackercalibrator.gui.DisplayJPanel getDisplayJPanel() {
        return displayJPanel;
    }

    public void setDisplayJPanel(eyetrackercalibrator.gui.DisplayJPanel displayJPanel) {
        this.displayJPanel = displayJPanel;
    }

    public eyetrackercalibrator.gui.FrameScrollingJPanel getEyeFrameScrollingJPanel() {
        return eyeFrameScrollingJPanel;
    }

    public void setEyeFrameScrollingJPanel(eyetrackercalibrator.gui.FrameScrollingJPanel eyeFrameScrollingJPanel) {
        this.eyeFrameScrollingJPanel = eyeFrameScrollingJPanel;
    }

    public eyetrackercalibrator.gui.FrameScrollingJPanel getScreenFrameScrollingJPanel() {
        return screenFrameScrollingJPanel;
    }

    public void setScreenFrameScrollingJPanel(eyetrackercalibrator.gui.FrameScrollingJPanel screenFrameScrollingJPanel) {
        this.screenFrameScrollingJPanel = screenFrameScrollingJPanel;
    }

    public FrameSynchronizor getFrameSynchronizor() {
        return frameSynchronizor;
    }

    public void setFrameSynchronizor(FrameSynchronizor frameSynchronizor) {
        this.frameSynchronizor = frameSynchronizor;
    }

    synchronized public void repaint() {
        isRepaint = true;
    }

    synchronized protected boolean getAndClearRepaint() {
        boolean b = isRepaint;
        isRepaint = false;
        return b;
    }
}
