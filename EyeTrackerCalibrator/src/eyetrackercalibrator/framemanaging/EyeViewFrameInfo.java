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
 * EyeViewFrameInfo.java
 *
 * Created on September 17, 2007, 7:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package eyetrackercalibrator.framemanaging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author rakavipa
 */
public class EyeViewFrameInfo implements FrameInfo {

    private static final long serialVersionUID = 4547267706325691722L;
    private String sourceFileName = null;
    private double corniaX = 0;
    private double corniaY = 0;
    private double reflectX = 0;
    private double reflectY = 0;
    private double[] reflectFit = null;
    private double[] corniaFit = null;
    private double pupilAngle = 0;

    /** Creates a new instance of EyeViewFrameInfo */
    public EyeViewFrameInfo() {
    }

    public FrameInfo getInstance() {
        return new EyeViewFrameInfo();
    }

    /**
     * File format is
     * <filename> <pupil X position> <pupil Y position> <reflect X position> <reflect Y position>
     * <pupil fit top left x> <pupil fit top left y> <pupil fit bottom right x> <pupil fit bottom right y> [optional: <cornia reflect fit top left x> <cornia reflect fit top left y> <cornia reflect fit bottom right x> <cornia reflect fit bottom right y>
     * <fit error> <pupil fit angle> <cornia reflect fit angle>
     */
    public FrameInfo getInstance(File inputFile, File frameFile, FrameInfo oldInstance) {
        EyeViewFrameInfo info = null;
        if (inputFile.exists()) {
            // Create new instance
            info = new EyeViewFrameInfo();
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
                if (line == null) {
                    return null;
                }
                line = line.trim();
                if (line == null) {
                    return null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
            String[] coorString = line.split("\\p{Space}+");

            // Parse pupil
            if (coorString.length > 2) {
                info.corniaX = Double.parseDouble(coorString[1]);
                info.corniaY = Double.parseDouble(coorString[2]);
            }

            // Parse reflection
            coorString = line.split("\\p{Space}+");
            if (coorString.length > 4) {
                info.reflectX = Double.parseDouble(coorString[3]);
                info.reflectY = Double.parseDouble(coorString[4]);
            }

            // Read in cornia and reflect fit
            try {
                line = in.readLine();
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }

            if (line != null) {
                // Split for value
                coorString = line.trim().split("\\p{Space}+");

                // check for cornia set
                if (coorString.length >= 4) {
                    info.setCorniaFit(new double[4]);
                    info.corniaFit[0] = Double.parseDouble(coorString[0]);
                    info.corniaFit[1] = Double.parseDouble(coorString[1]);
                    info.corniaFit[2] = Double.parseDouble(coorString[2]) - info.corniaFit[0];
                    info.corniaFit[3] = Double.parseDouble(coorString[3]) - info.corniaFit[1];
                }
                // check for reflect set
                if (coorString.length >= 7) {
                    info.setReflectFit(new double[4]);
                    info.reflectFit[0] = Double.parseDouble(coorString[4]);
                    info.reflectFit[1] = Double.parseDouble(coorString[5]);
                    info.reflectFit[2] = Double.parseDouble(coorString[6]) - info.reflectFit[0];
                    if (coorString.length >= 8) {
                        info.reflectFit[3] = Double.parseDouble(coorString[7]) - info.reflectFit[1];
                    } else {
                        info.reflectFit[3] = info.reflectFit[2];
                    }
                }
            }

            line = null;
            try {
                line = in.readLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (line != null) {
                // Split for value
                coorString = line.trim().split("\\p{Space}+");

                if (coorString.length >= 2) {
                    info.pupilAngle = Double.parseDouble(coorString[1]);
                }
            }
        }
        return info;
    }

    public double getPupilX() {
        return corniaX;
    }

    public void setCorniaX(double corniaX) {
        this.corniaX = corniaX;
    }

    public double getPupilY() {
        return corniaY;
    }

    public void setCorniaY(double corniaY) {
        this.corniaY = corniaY;
    }

    public double getReflectX() {
        return reflectX;
    }

    public void setReflectX(double reflectX) {
        this.reflectX = reflectX;
    }

    public double getReflectY() {
        return reflectY;
    }

    public void setReflectY(double reflectY) {
        this.reflectY = reflectY;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    @Override
    public String toString() {
        String retValue;

        retValue = "" + getPupilX() + " " + getPupilY();
        return retValue;
    }

    /**
     * Set bounding box of eye reflection fit (coordinate is top left screen = 0,0)
     * {top left x, top left y, bottom right x, bottom right y}
     */
    public double[] getReflectFit() {
        return reflectFit;
    }

    /**
     * Set bounding box of eye reflection fit (coordinate is top left screen = 0,0)
     * {top left x, top left y, bottom right x, bottom right y}
     */
    public void setReflectFit(double[] reflectFit) {
        this.reflectFit = reflectFit;
    }

    /**
     * get bounding box of eye cornia fit (coordinate is top left screen = 0,0)
     * {top left x, top left y, bottom right x, bottom right y}
     */
    public double[] getCorniaFit() {
        return corniaFit;
    }

    /**
     * Set bounding box of eye cornia fit (coordinate is top left screen = 0,0)
     * {top left x, top left y, bottom right x, bottom right y}
     */
    public void setCorniaFit(double[] corniaFit) {
        this.corniaFit = corniaFit;
    }

    public double getPupilAngle() {
        return pupilAngle;
    }

    public void setPupilAngle(double pupilAngle) {
        this.pupilAngle = pupilAngle;
    }
}
