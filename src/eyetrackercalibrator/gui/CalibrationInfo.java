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

import eyetrackercalibrator.gui.util.FrameMarker;
import java.awt.Color;
import java.awt.geom.Point2D;
import org.jfree.chart.plot.IntervalMarker;

/**
 *
 * @author ruj
 */
public class CalibrationInfo extends FrameMarker{
    /** 
     * Contains a calibration point position of the first frame selected
     * by user
     */
    public Point2D selectedCalibrationPointPosition;
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
        this.startSceneFrame = startScreenFrame;
        this.stopSceneFrame = stopScreenFrame;
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
                show += "Drift Correction Point<br>";
                break;
            default:
                show += "Primary calibration<br>";
        }

        show += "From:" + startEyeFrame + ":" + startSceneFrame + "(eye:screen)<br>" +
                "To:" + stopEyeFrame + ":" + stopSceneFrame + "(eye:screen)<br>" +
                "First point: (" + selectedCalibrationPointPosition.getX() + "," +
                selectedCalibrationPointPosition.getY() + ")";
        if (isCalibrationPointPositionLocated) {
            show += "<br>Processed";
        } else {
            show += "<br>Not processed";
        }
        return show + "</html>";
    }

    public void setIntervalMarker(IntervalMarker intervalMarker) {
        super.setIntervalMarker(intervalMarker);

        intervalMarker.setPaint(Color.GREEN);
    }
}
