/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eyetrackercalibrator.gui.util;

import org.jfree.chart.plot.IntervalMarker;

/**
 * Base class for all classes that used to mark frames
 * @author ruj
 */
public class FrameMarker {

    public int startEyeFrame = 0;
    public int stopEyeFrame = 0;
    public int startSceneFrame = 0;
    public int stopSceneFrame = 0;
    protected IntervalMarker intervalMarker = null;
    /** Used for adjusting start and stop */
    private int referenceFrame = 0;
    private int referenceEyeFrame = 0;
    private int referenceSceneFrame = 0;

    @Override
    public boolean equals(Object obj) {
        FrameMarker o = (FrameMarker) obj;
        return (o.startEyeFrame == startEyeFrame &&
                o.stopEyeFrame == stopEyeFrame &&
                o.startSceneFrame == startSceneFrame &&
                o.stopSceneFrame == stopSceneFrame);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.startEyeFrame;
        hash = 53 * hash + this.stopEyeFrame;
        hash = 53 * hash + this.startSceneFrame;
        hash = 53 * hash + this.stopSceneFrame;
        return hash;
    }

    /**
     * Method for marking end frame and set the interval marker accordingly.
     * The method takes care of making sure that starting frame is smaller
     * than the end frame.  The method only works if you use it after
     * calling setStartFrame.
     */
    public void setEndFrame(int currentFrame, int eyeFrame, int sceneFrame) {
        int endRef = 0;
        int startRef = 0;

        if (currentFrame >= this.referenceFrame) {
            /** Normal start and end case */
            startRef = this.referenceFrame;
            endRef = currentFrame;
            this.startEyeFrame = this.referenceEyeFrame;
            this.stopEyeFrame = eyeFrame;
            this.startSceneFrame = this.referenceSceneFrame;
            this.stopSceneFrame = sceneFrame;
        } else {
            startRef = currentFrame;
            endRef = this.referenceFrame;
            this.startEyeFrame = eyeFrame;
            this.stopEyeFrame = this.referenceEyeFrame;
            this.startSceneFrame = sceneFrame;
            this.stopSceneFrame = this.referenceSceneFrame;
        }

        if (this.intervalMarker != null) {
            this.intervalMarker.setEndValue(endRef);
            this.intervalMarker.setStartValue(startRef);
        }
    }

    public void setStartFrame(int currentFrame, int eyeFrame, int sceneFrame) {
        this.startEyeFrame = eyeFrame;
        this.startSceneFrame = sceneFrame;
        if (this.intervalMarker != null) {
            this.intervalMarker.setStartValue(currentFrame);
        }

        this.referenceFrame = currentFrame;
        this.referenceEyeFrame = eyeFrame;
        this.referenceSceneFrame = sceneFrame;
    }

    public IntervalMarker getIntervalMarker() {
        return intervalMarker;
    }

    public void setIntervalMarker(IntervalMarker intervalMarker) {
        this.intervalMarker = intervalMarker;
        intervalMarker.setAlpha(0.2f);
    }

}
