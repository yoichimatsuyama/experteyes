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
package logic;

import java.awt.image.BufferedImage;
import java.io.File;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import javax.imageio.ImageIO;


/**
 *
 * @author dwyatte
 * 
 * This class takes an array of image files and does some operations on THE ENTIRE SET such as:
 *  averaging
 *  running min
 *  running max
 * 
 *  not appropriate for thresholding
 * 
 * The class is threaded so that it can be done in the background
 */
public class ThreadedImageProcessor implements Runnable {
    // don't process every frame
    int FRAME_SKIP = 30;
    // all of the image files
    File[] imgFiles;
    // pixels with ops applied
    int[] pixels, maxImgPixels, minImgPixels, avgImgPixels;
    // images composed of said pixels
    BufferedImage img, maxImg, minImg, avgImg;

    private boolean alive;
    private ThreadedImageProcessorListener listener;

    // support for progress bar
    public ThreadedImageProcessor(ThreadedImageProcessorListener listener) {
        this.listener = listener;
        maxImg = minImg = avgImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    }

    public void initialize(File[] imgFiles) {
        // set up fields
        this.imgFiles = imgFiles;
    }

    /** Initialize has to be called before running or nothing will happen */
    public void run() {

        this.alive = true;

        try {
            if (imgFiles == null) {
                return;
            }

            // Save old picture in case of quitting
            BufferedImage oldMinImg = minImg;
            BufferedImage oldMaxImg = maxImg;
            BufferedImage oldAvgImg = avgImg;

            // get the initial img and pixels
            img = ImageUtils.loadRGBImage(imgFiles[0]);

            minImg = new BufferedImage(img.getWidth(), img.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            maxImg = new BufferedImage(img.getWidth(), img.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            avgImg = new BufferedImage(img.getWidth(), img.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            pixels = ImageUtils.RGBtoGray(ImageUtils.getPixels(img));
            minImgPixels = ImageUtils.RGBtoGray(pixels);
            maxImgPixels = ImageUtils.RGBtoGray(pixels);
            avgImgPixels = ImageUtils.RGBtoGray(pixels);

            // now do this for all files
            for (int i = 0; i < imgFiles.length && alive; i += FRAME_SKIP) {
                img = ImageUtils.loadRGBImage(imgFiles[i]);
                pixels = ImageUtils.RGBtoGray(ImageUtils.getPixels(img));
                for (int j = 0; j < pixels.length; j++) {
                    minImgPixels[j] = Math.min(minImgPixels[j], pixels[j]);
                    maxImgPixels[j] = Math.max(maxImgPixels[j], pixels[j]);
                }

                listener.progress(i);
                
            }

            // average min and max eyes
            for (int i = 0; i < avgImgPixels.length && alive; i++) {
                avgImgPixels[i] = (int) ((minImgPixels[i] + maxImgPixels[i]) / 2.0);
            }

            if (this.alive) {

                // set buffered images' pixels
                minImg.setRGB(0, 0, minImg.getWidth(), minImg.getHeight(),
                        ImageUtils.grayToRGB(minImgPixels), 0, minImg.getWidth());
                maxImg.setRGB(0, 0, maxImg.getWidth(), maxImg.getHeight(),
                        ImageUtils.grayToRGB(maxImgPixels), 0, maxImg.getWidth());
                avgImg.setRGB(0, 0, avgImg.getWidth(), avgImg.getHeight(),
                        ImageUtils.grayToRGB(avgImgPixels), 0, avgImg.getWidth());

                // top off progress bar
                listener.progress(imgFiles.length);
                listener.complete();

            } else {
                // Quit gracefully by rolling back all changes
                minImg = oldMinImg;
                maxImg = oldMaxImg;
                avgImg = oldAvgImg;

                // top off progress bar and signal completion
                listener.progress(imgFiles.length);
                listener.complete();
            }

            // Get rid of file array
            this.imgFiles = null;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public BufferedImage getMinImg() {
        return minImg;
    }

    public BufferedImage getMaxImg() {
        return maxImg;
    }

    public BufferedImage getAvgImg() {
        return avgImg;
    }

    public void load(File directory) {
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("resources/MinMaxAvgProcessor");

        File inputFile = new File(directory, bundle.getString("minImageFile"));
        if (inputFile.exists()) {
            minImg = ImageUtils.loadRGBImage(inputFile);
        }
        inputFile = new File(directory, bundle.getString("maxImageFile"));
        if (inputFile.exists()) {
            maxImg = ImageUtils.loadRGBImage(inputFile);
        }
        inputFile = new File(directory, bundle.getString("avgImageFile"));
        if (inputFile.exists()) {
            avgImg = ImageUtils.loadRGBImage(inputFile);
        }
    }

    public void save(File directory) throws IOException {
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("resources/MinMaxAvgProcessor");

        File outputfile = new File(directory, bundle.getString("minImageFile"));
        ImageIO.write(minImg, "jpg", outputfile);
        outputfile = new File(directory, bundle.getString("maxImageFile"));
        ImageIO.write(maxImg, "jpg", outputfile);
        outputfile = new File(directory, bundle.getString("avgImageFile"));
        ImageIO.write(avgImg, "jpg", outputfile);

    }

    /** This will kill the image procssing thread */
    public void kill() {
        this.alive = false;
    }
}
