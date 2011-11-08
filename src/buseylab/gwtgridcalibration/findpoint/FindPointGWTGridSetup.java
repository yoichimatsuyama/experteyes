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
package buseylab.gwtgridcalibration.findpoint;


import buseylab.gwtgridcalibration.gwtgrid.GWTGrid;
import buseylab.gwtgridcalibration.gwtgrid.ImageUtils;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/*
 * This class allows Ruj to interface with the GWT Grid training mechanism.
 * 
 * Usage:
 * FindPointGWTGridSetup gwtgridsetup = new FindPointGWTGridSetup();
 * gwtgridsetup.train(point, image);
 * gwtgridsetup.save(outputFile);
 * 
 * project root is a string that describes the base of the subject
 *  i.e. /Volumes/Eyebook4/Evansville/RickEvan/ **** TRAILING SLASH IS NECSESARY
 * point is the point that the user clicked
 * image is the buffered image that the user clicked on
 * 
 */
import javax.imageio.ImageIO;

public class FindPointGWTGridSetup {
    // any changes made here also need to be reflected in FindCornersMain
    double sigma = 1.0 * Math.PI;
    int numOrientations = 8;
    int numScales = 8;
    int size = 128;
    Point translatedClick;
    GWTGrid gwtgrids;
    

    public FindPointGWTGridSetup() {
    }

    public void train(Point p, BufferedImage scene) {
        double[][][] freqKernels = GWTGrid.genFreqKernel(size, numScales, numOrientations, sigma);

        // get image subsection
        double[] pixels = ImageUtils.RGBtoGrayDouble(ImageUtils.getPixels(scene, p.x - (int) (size / 2.0), p.y - (int) (size / 2.0), size, size));
        // translate xClick, yClick these into 128 x 128 space
        int xClickTranslated = (int) (size / 2.0);
        int yClickTranslated = (int) (size / 2.0);
        translatedClick = new Point(xClickTranslated, yClickTranslated);
        gwtgrids = new GWTGrid(pixels, size, freqKernels);

    }

    public void save(File outputGWTGridFile) {
        try {
            // any changes down here need to reflected in FindCalPoint where we read in GWTCalPoint
            FileOutputStream fos = new FileOutputStream(outputGWTGridFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(gwtgrids.getMagnitudeResp(translatedClick.x, translatedClick.y));
            oos.writeObject(gwtgrids.getPhaseResp(translatedClick.x, translatedClick.y));
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            String projectRoot = "/Volumes/Eyebook4/Evansville/RickEvan/";
            Point p = new Point(84, 105);
            BufferedImage img = ImageIO.read(new File("/Volumes/Eyebook4/Evansville/RickEvan/SmallCleanedScene/scene_01450.jpg"));

            FindPointGWTGridSetup gwtgridsetup = new FindPointGWTGridSetup();
            gwtgridsetup.train(p, img);
            gwtgridsetup.save(new File(projectRoot,"GWTPoint.dat"));
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
