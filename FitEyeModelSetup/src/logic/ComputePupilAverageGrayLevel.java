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
package logic;

import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import util.Parameters;

/**
 *
 * @author SQ
 */
public class ComputePupilAverageGrayLevel {

    static public class PupilInfo {

        @Override
        public String toString() {
            return x + "\t" + y + "\t" + gray;
        }

        public PupilInfo(int x, int y, double gray) {
            this.x = x;
            this.y = y;
            this.gray = gray;
        }
        public int x;
        public int y;
        public double gray;
    }

    static public PupilInfo compute(File input, Parameters parameters){
        // Get image from file
        BufferedImage paintedImg = ImageUtils.loadRGBImage(input);

        // Get pupil estimate
        Ellipse2D foundPupil = FitEyeModel.findPupil(paintedImg,
                parameters.searchArea, parameters.pupilThreshold,
                parameters.detectPupilAngle);

        int[] paintedImgPixels = logic.ImageUtils.RGBtoGray(
                logic.ImageUtils.getPixels(paintedImg));

        int width = (int) foundPupil.getX() + (int) foundPupil.getWidth();
        int height = (int) foundPupil.getY() + (int) foundPupil.getHeight();
        int pos = ((int) foundPupil.getX()) +
                paintedImg.getWidth() * ((int) foundPupil.getY());
        int count = 0;
        double total = 0;

        for (int y = (int) foundPupil.getY(); y < width; y++) {
            for (int x = (int) foundPupil.getX(); x < height; x++) {
                // Check if this is in ellisp
                if (foundPupil.contains(x, y)) {
                    total += paintedImgPixels[pos];
                    count++;
                }
                pos++;
            }
            pos += paintedImg.getWidth();
        }

        if (count > 0) {
            return new PupilInfo((int) foundPupil.getX(), (int) foundPupil.getY(),
                    total / count);
        } else {
            return null;
        }
    }
}
