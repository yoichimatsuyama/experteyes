/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buseylab.fiteyemodel.gui;

import buseylab.fiteyemodel.logic.ThreadedImageProcessor;
import java.awt.image.BufferedImage;

/**
 *
 * @author ruj
 */
public interface FitEyeModelSystemInterface {

    int getFrame();

    ThreadedImageProcessor getImageProcessor();

    InteractivePanel getInteractivePanel();

    /**
     * This method set current eye directory and force the program to load the frames
     */
    void setEyeDirectory(String eyePath);

    void setFrame(int newFrameNumber);

    /**
     * This method start min max avg image computation
     */
    void startMinMaxAverageImageComputation();

    /**
     * This method stop min max avg image computation
     */
    void stopMinMaxAverageImageComputation();

    /* This method force image to be show.  If null, then normal image showing
     * operation of the system should be used
     */
    void setImage(BufferedImage img);

    void setSearchSpaceSize(int width, int height);
}
