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

import eyetrackercalibrator.framemanaging.InformationDatabase;
import eyetrackercalibrator.framemanaging.ScreenFrameManager;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author SQ
 */
public class ComputeIlluminationRangeThread extends Thread {

    ScreenFrameManager screenFrameManager;
    int startFrame;
    int endFrame;
    InformationDatabase informationDatabase;
    boolean alive = true;
    private PropertyChangeListener listener;

    public ComputeIlluminationRangeThread(
            ScreenFrameManager screenFrameManager,
            InformationDatabase informationDatabase,
            int startFrame, int endFrame, PropertyChangeListener listener) {
        super("Compute Illumination from " + startFrame);
        this.screenFrameManager = screenFrameManager;
        this.informationDatabase = informationDatabase;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.listener = listener;
    }

    @Override
    public void run() {
        BufferedImage image;// = screenFrameManager.getFrame(1);
        // Space fo storing the pic data
        double[] pixels;// = new double[image.getHeight() * image.getWidth()];

        for (int i = startFrame; i <= endFrame && alive; i++) {
        //for (int i = 7019; i <= endFrame && alive; i++) {
            //        String name = screenFrameManager.getFrameFileName(i);
            image = screenFrameManager.getFrame(i);

            double illumination = 0d;

            if (image != null) {
                // Convert to gray scale
                image = toGray(image);

                // Get pixeles data
                pixels = image.getData().getPixels(0, 0,
                        image.getWidth(), image.getHeight(),
                        (double[]) null);

                // Sum up to get total value
                for (int j = 0; j < pixels.length; j++) {
                    illumination += pixels[j];
                }
                illumination = illumination / pixels.length;
                 if (pixels.length == 0)
            {
                System.out.println("Pixel length is zero");
            }
            }
            // Put data to database
           // System.out.println("Illumination = " + illumination);
           

            informationDatabase.putInfo(i, illumination);

            if (this.listener != null) {
                this.listener.propertyChange(new PropertyChangeEvent(
                        this, "Frame completed", i - 1, i));
            }
        }
    }

    public BufferedImage toGray(BufferedImage bi) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);

        return op.filter(bi, null);
    }

    public void kill() {
        alive = false;
    }
}
