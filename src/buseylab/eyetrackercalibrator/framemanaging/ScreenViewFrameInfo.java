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
 * ScreenViewFrameInfo.java
 *
 * Created on September 18, 2007, 12:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package buseylab.eyetrackercalibrator.framemanaging;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author rakavipa
 */
public class ScreenViewFrameInfo implements FrameInfo {

    private static final long serialVersionUID = 3518418690120514360L;
    private String sourceFileName = null;
    private Point[] corners = null;
    private Point[] correctedCorners = null;
    private Point[] markedPoints = null;
    public double[] similarities = null;
    public volatile double illumination = 0d;
    private volatile double scale = 1d;

    /** Creates a new instance of ScreenViewFrameInfo */
    public ScreenViewFrameInfo() {
        // Init corners
        corners = new Point[4];
        correctedCorners = new Point[4];
        Arrays.fill(corners, null);
        Arrays.fill(correctedCorners, null);
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    /**
     * Get scaled coordinate of the corner (the scale factor is set through setScale)
     */
    public Point2D getTopLeft() {
        return getScaledCornerPoint(TOPLEFT);
    }

    /**
     * set unscaled coordinate of the corner
     */
    public void setTopLeft(Point topLeft) {
        this.corners[TOPLEFT] = topLeft;
    }

    /**
     * Get scaled coordinate of the corner (the scale factor is set through setScale)
     */
    public Point2D getTopRight() {
        return getScaledCornerPoint(TOPRIGHT);
    }

    /**
     * set unscaled coordinate of the corner
     */
    public void setTopRight(Point topRight) {
        this.corners[TOPRIGHT] = topRight;
    }

    /**
     * Get scaled coordinate of the corner (the scale factor is set through setScale)
     */
    public Point2D getBottomLeft() {
        return getScaledCornerPoint(BOTTOMLEFT);
    }

    /**
     * set unscaled coordinate of the corner
     */
    public void setBottomLeft(Point bottomLeft) {
        this.corners[BOTTOMLEFT] = bottomLeft;
    }

    /**
     * Get scaled coordinate of the corner (the scale factor is set through setScale)
     */
    public Point2D getBottomRight() {
        return getScaledCornerPoint(BOTTOMRIGHT);
    }

    private Point2D.Double getScaledCornerPoint(int cornerLocation) {
        Point2D.Double p = new Point2D.Double();
        if (corners[cornerLocation] != null) {
            p.setLocation(corners[cornerLocation].x * scale, corners[cornerLocation].y * scale);
            return p;
        } else {
            return null;
        }
    }

    public void setBottomRight(Point bottomRight) {
        this.corners[BOTTOMRIGHT] = bottomRight;
    }

    public FrameInfo getInstance() {
        return new ScreenViewFrameInfo();
    }

    /**
     * This does not add Image source file name to the instance. The format is
     * <topleft X> <topleft Y> <similarity>
     * <topright X> <topright Y> <similarity>
     * <bottomright X> <bottomright Y> <similarity>
     * <bottomleft X> <bottomleft Y> <similarity>
     */
    public FrameInfo getInstance(File inputFile, File frameFile, FrameInfo oldInstance) {
        ScreenViewFrameInfo n = null;
        if (inputFile.exists()) {
            // Create new instance
            n = new ScreenViewFrameInfo();
            BufferedReader in;
            try {
                in = new BufferedReader(new FileReader(inputFile));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                return null;
            }
            String line;
            try {
                line = in.readLine();
                
                // Make sure that we are not getting the black file
                if(line == null){
                    return null;
                }
                line = line.trim();
                if(line == null){
                    return null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
            Point[] p = new Point[4];
            double[] loadedSimilarities = new double[4];
            int lineCount;

            // Read four lines
            for (lineCount = 0; lineCount < 4 && line != null; lineCount++) {
                // Process a line
                String[] coorString = line.split("\\p{Space}+");
                Double x = Double.valueOf(coorString[0]);
                Double y = Double.valueOf(coorString[1]);
                // Get similarity if exists
                if (coorString.length >= 3) {
                    double sim = Double.parseDouble(coorString[2]);
                    loadedSimilarities[lineCount] = sim;
                }

                // Set parameter
                p[lineCount] = new Point();
                p[lineCount].setLocation(x.doubleValue(), y.doubleValue());

                // Read more line
                try {
                    line = in.readLine();
                    if (line != null) {
                        line = line.trim();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
            // Sanity check
            if (lineCount < 4) {
                // Not complete reading terminate
                return null;
            } else {
                // Set variables
                for (int i = 0; i < p.length; i++) {
                    n.corners[i] = p[i];
                }

                n.illumination = 0d;

//                for (int i = 0; i < p.length; i++) {
//                    if(this.corners[i] != null && p[i] != null){
//                        similarities[i] = this.corners[i].distance(p[i]);
//                    }else{
//                        similarities[i] = -1d;
//                    }
//                }
                n.similarities = loadedSimilarities;

//                // This is for computation of different from previous points
//                // Set variables
//                for (int i = 0; i < p.length; i++) {
//                    this.corners[i] = p[i];
//                }
            }
        }

        if (oldInstance != null) {
            if (n == null) {
                n = new ScreenViewFrameInfo();
            }
            // Copy old info to new info
            n.markedPoints = ((ScreenViewFrameInfo) oldInstance).markedPoints;
        }

        return n;
    }
    public static final int TOPLEFT = 0;
    public static final int TOPRIGHT = 1;
    public static final int BOTTOMLEFT = 3;
    public static final int BOTTOMRIGHT = 2;

    @Override
    public String toString() {
        String retValue;
        retValue = super.toString();
        return retValue + " Source file name:" + sourceFileName +
                ", Corners " + corners[TOPLEFT].toString() + "," + corners[TOPRIGHT].toString() +
                "," + corners[BOTTOMLEFT].toString() + "," + corners[BOTTOMRIGHT].toString();
    }

    public Point[] getMarkedPoints() {
        return markedPoints;
    }

    public Point[] getScaledMarkedPoints() {
        Point[] scaled = null;
        if (markedPoints != null) {
            scaled = markedPoints.clone();
            for (int i = 0; i < scaled.length; i++) {
                Point point = scaled[i];
                point.x *= scale;
                point.y *= scale;
            }

        }
        return scaled;
    }

    public void setMarkedPoints(Point[] markedPoints) {
        this.markedPoints = markedPoints;
    }

    /**
     * Get unscaled corners
     * @return 
     */
    public Point[] getCorners() {
        return corners;
    }

    public Point[] getCorrectedCorners() {
        return correctedCorners;
    }

    public void setCorrectedCorners(Point2D topLeft, Point2D topRight, Point2D bottomLeft, Point2D bottomRight) {
        Point point = new Point();
        Point2D temp = null;
        boolean hasPoint = true;
        for (int i = 0; i < correctedCorners.length; i++) {
            switch (i) {
                case TOPLEFT:
                    temp = topLeft;
                    break;
                case TOPRIGHT:
                    temp = topRight;
                    break;
                case BOTTOMLEFT:
                    temp = bottomLeft;
                    break;
                case BOTTOMRIGHT:
                    temp = bottomRight;
                    break;
                default:
                    temp = null;
            }

            if (temp != null) {
                point.setLocation(temp);
                correctedCorners[i] = (Point) point.clone();
            } else {
                correctedCorners[i] = null;
            }
        }
    }

    void setScale(double scale) {
        this.scale = scale;
    }

    public double getScale() {
        return scale;
    }
}
