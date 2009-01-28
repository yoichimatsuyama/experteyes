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

import java.awt.Color;
import java.awt.geom.Point2D;
import org.jfree.chart.plot.IntervalMarker;

/**
 *
 * @author ruj
 */
public class CalibrationInfo {

    public int startEyeFrame = 0;
    public int stopEyeFrame = 0;
    public int startScreenFrame = 0;
    public int stopScreenFrame = 0;
    /** 
     * Contains a calibration point position of the first frame selected
     * by user
     */
    public Point2D selectedCalibrationPointPosition;
    private IntervalMarker intervalMarker = null;
    private int referenceFrame = 0;
    public boolean isCalibrationPointPositionLocated = false;

    public enum CalibrationType {

        Primary, Secondary, Testing
    }
    public CalibrationType calibrationType;

    public CalibrationInfo() {
    }

    public CalibrationInfo(
            int startEyeFrame, int stopEyeFrame, int startScreenFrame,
            int stopScreenFrame, Point2D selectedCalibrationPointPosition,
            boolean isCalibrationPointPositionLocated,
            CalibrationType calibrationType) {
        this.startEyeFrame = startEyeFrame;
        this.stopEyeFrame = stopEyeFrame;
        this.startScreenFrame = startScreenFrame;
        this.stopScreenFrame = stopScreenFrame;
        this.selectedCalibrationPointPosition = selectedCalibrationPointPosition;
        this.isCalibrationPointPositionLocated = isCalibrationPointPositionLocated;
        this.calibrationType = calibrationType;

    }

    @Override
    public String toString() {
        String show = "<html>";

        switch (this.calibrationType) {
            case Secondary:
                show += "Secondary calibration<br>";
                break;
            case Testing:
                show += "Testing point<br>";
                break;
            default:
                show += "Primary calibration<br>";
        }

        show += "From:" + startEyeFrame + ":" + startScreenFrame + "(eye:screen)<br>" +
                "To:" + stopEyeFrame + ":" + stopScreenFrame + "(eye:screen)<br>" +
                "First point: (" + selectedCalibrationPointPosition.getX() + "," +
                selectedCalibrationPointPosition.getY() + ")";
        if (isCalibrationPointPositionLocated) {
            show += "<br>Processed";
        } else {
            show += "<br>Not processed";
        }
        return show + "</html>";
    }

    @Override
    public boolean equals(Object obj) {
        CalibrationInfo o = (CalibrationInfo) obj;
        return (o.startEyeFrame == startEyeFrame &&
                o.stopEyeFrame == stopEyeFrame &&
                o.startScreenFrame == startScreenFrame &&
                o.stopScreenFrame == stopScreenFrame);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.startEyeFrame;
        hash = 53 * hash + this.stopEyeFrame;
        hash = 53 * hash + this.startScreenFrame;
        hash = 53 * hash + this.stopScreenFrame;
        return hash;
    }

    /** 
     * Method for marking end frame and set the interval marker accordingly.  
     * The method takes care of making sure that starting frame is smaller
     * than the end frame.  The method only works if you use it after
     * calling setStartFrame.
     */
    public void setEndFrame(int currentFrame, int eyeOffset, int screenOffset) {
        int endRef = 0;
        int startRef = 0;

        if (currentFrame >= referenceFrame) {
            startRef = referenceFrame;
            endRef = currentFrame;
        } else {
            startRef = currentFrame;
            endRef = referenceFrame;
        }

        stopEyeFrame = endRef + eyeOffset;
        stopScreenFrame = endRef + screenOffset;
        startEyeFrame = startRef + eyeOffset;
        startScreenFrame = startRef + screenOffset;
        if (intervalMarker != null) {
            intervalMarker.setEndValue(endRef);
            intervalMarker.setStartValue(startRef);
        }
    }

    public void setStartFrame(int currentFrame, int eyeOffset, int screenOffset) {
        startEyeFrame = currentFrame + eyeOffset;
        startScreenFrame = currentFrame + screenOffset;
        if (intervalMarker != null) {
            intervalMarker.setStartValue(currentFrame);
        }

        referenceFrame = currentFrame;
    }

    public IntervalMarker getIntervalMarker() {
        return intervalMarker;
    }

    public void setIntervalMarker(IntervalMarker intervalMarker) {
        this.intervalMarker = intervalMarker;

        intervalMarker.setPaint(Color.GREEN);

        intervalMarker.setAlpha(0.2f);
    }
}
