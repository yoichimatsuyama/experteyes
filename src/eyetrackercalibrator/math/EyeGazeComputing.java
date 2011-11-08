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
package eyetrackercalibrator.math;

import eyetrackercalibrator.framemanaging.EyeViewFrameInfo;
import eyetrackercalibrator.gui.CalibrateJPanel;
import eyetrackercalibrator.gui.DriftCorrectionInfo;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import javax.swing.DefaultListModel;

/**
 *
 * @author ruj
 */
public class EyeGazeComputing {

    private int endFrame = 0;
    private int startFrame = 0;
    private double linearInterpolationFactor = 0d;
    private double[][] primaryEyeCoeff;
    private double[][] secondaryEyeCoeff;
    private boolean usingCorneaReflect = true;
    private DefaultListModel allDriftCorrectionSets;

    public boolean isUsingCorneaReflect() {
        return usingCorneaReflect;
    }

    public void setUsingCorneaReflect(boolean usingCorneaReflect) {
        this.usingCorneaReflect = usingCorneaReflect;
    }

    public void setAllDriftCorrectionSets(DefaultListModel allDriftCorrectionSets) {
        this.allDriftCorrectionSets = allDriftCorrectionSets;
    }

    public enum ComputingApproach {

        /** Compute using primary eye coefficient */
        PRIMARY,
        /** Compute using secondary eye coefficient */
        SECONDARY,
        /** Compute using linear estimation from primaty to secondary according
         * the current frame.
         */
        LINEAR
    }
    private ComputingApproach computingApproach = EyeGazeComputing.ComputingApproach.PRIMARY;

    public double[][] getPrimaryEyeCoeff() {
        return primaryEyeCoeff;
    }

    public void setPrimaryEyeCoeff(double[][] primaryEyeCoeff) {
        this.primaryEyeCoeff = primaryEyeCoeff;
    }

    public double[][] getSecondaryEyeCoeff() {
        return secondaryEyeCoeff;
    }

    public void setSecondaryEyeCoeff(double[][] secondaryEyeCoeff) {
        this.secondaryEyeCoeff = secondaryEyeCoeff;
    }

    public ComputingApproach getComputingApproach() {
        return computingApproach;
    }

    public void setComputingApproach(ComputingApproach computingApproach) {
        this.computingApproach = computingApproach;
    }

    public void setLinearComputingParameters(int startFrame, int endFrame) {
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        // Sanity check
        if (startFrame != endFrame) {
            this.linearInterpolationFactor = 1d / (double) (endFrame - startFrame);
        } else {
            this.linearInterpolationFactor = 0;
        }
    }

    public int getLinearStartFrame() {
        return this.startFrame;
    }

    public int getLinearLastFrame() {
        return this.endFrame;
    }

    /**
     * Compute eyegaze using the method set by setComputingApproach
     * param x x component of an eye vector
     * param y y component of an eye vector
     */
    public Point2D computeEyeGaze(int currentFrame, double x, double y) {
        Point2D point = computeEyeGaze(currentFrame, x, y,
                this.computingApproach);

        return point;
    }

    private Point2D applyDriftCorrection(DefaultListModel allDriftCorrectionSets, int eyeFrameNumber, Point2D scenePoint) {
        //search through drift correction sets, find the one with the closest eye frame
        //that is SMALLER than the current eyeFrameNumber. Get correction and apply it
        int MinDistance = Integer.MAX_VALUE;
        Point2D.Double driftCorrectionToApply = new Point2D.Double(0, 0);
        for (Enumeration en = allDriftCorrectionSets.elements();
                en.hasMoreElements();) {
            // Add calibration points
            DriftCorrectionInfo driftCorrectionInfo = (DriftCorrectionInfo) en.nextElement();
            int thisDistance = eyeFrameNumber - driftCorrectionInfo.startEyeFrame;
            if (thisDistance >= 0) {
                if (thisDistance < MinDistance) {
                    MinDistance = thisDistance;
                }
                driftCorrectionToApply.setLocation(driftCorrectionInfo.GetCumulativeError());
            }
        }
        Point2D driftCorrectedPoint = new Point2D.Double(scenePoint.getX() - driftCorrectionToApply.getX(), scenePoint.getY() - driftCorrectionToApply.getY());
        return (driftCorrectedPoint);
    }

    /**
     * Compute eyegaze using the method specified by parameters
     * @param x x component of an eye vector
     * @param y y component of an eye vector
     * @param currentFrame Current frame number used by calibration.
     * Make sure the
     */
    public Point2D computeEyeGaze(int currentFrame, double x, double y,
            ComputingApproach approach) {
        Point2D point = null;
        switch (approach) {
            case SECONDARY:
                if (this.secondaryEyeCoeff != null) {
                    // Compute eye gaze point
                    point = Computation.computeEyeGazePoint(x, y, this.secondaryEyeCoeff);
                }
                break;
            case LINEAR:
                if (this.secondaryEyeCoeff != null && this.primaryEyeCoeff != null) {
                    Point2D startPoint = Computation.computeEyeGazePoint(x, y, this.primaryEyeCoeff);
                    Point2D endPoint = Computation.computeEyeGazePoint(x, y, this.secondaryEyeCoeff);
                    if (currentFrame <= startFrame) {
                        point = startPoint;
                    } else if (currentFrame >= endFrame) {
                        point = endPoint;
                    } else {
                        // Compute linear estimation
                        double frameAdvanced = currentFrame - startFrame;

                        double px = startPoint.getX()
                                + (endPoint.getX() - startPoint.getX()) * frameAdvanced * this.linearInterpolationFactor;

                        double py = startPoint.getY()
                                + (endPoint.getY() - startPoint.getY()) * frameAdvanced * this.linearInterpolationFactor;

                        point = new Point2D.Double(px, py);
                    }
                }
                break;
            default: // Default at primary
                if (this.primaryEyeCoeff != null) {
                    // Compute eye gaze point
                    point = Computation.computeEyeGazePoint(x, y, this.primaryEyeCoeff);

                    //first check to see if there are any drift correction points
                    if (allDriftCorrectionSets != null)
                    {
                    point = applyDriftCorrection(allDriftCorrectionSets, currentFrame, point);
                    }
                    
                }
        }

        return point;
    }

    /** 
     * Compute eye vector from pupil location and Cornia reflection. Make sure that
     * info is not null.
     */
    public Point2D.Double getEyeVector(EyeViewFrameInfo info) {
        if (this.usingCorneaReflect) {
            return new Point2D.Double(info.getPupilX() - info.getCorneaReflectX(),
                    info.getPupilY() - info.getCorneaReflectY());
        } else {
            return new Point2D.Double(info.getPupilX(), info.getPupilY());
        }
    }
}
