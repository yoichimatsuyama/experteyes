/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buseylab.fiteyemodel.logic;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 *
 * @author ruj
 */
public class AdditionComposite implements Composite{

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new CompositeContext() {

            @Override
            public void dispose() {
                
            }

            @Override
            public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
                int height = Math.min(src.getHeight(),dstOut.getHeight());
                int width = Math.min(src.getWidth(),dstOut.getWidth());
                int[] srcPix = src.getPixels(0, 0, width, height, (int[])null);
                int[] desPix = dstIn.getPixels(0, 0, width, height, (int[]) null);
                for (int i = 0; i < desPix.length; i++) {
                   srcPix[i] += desPix[i];
                   if(srcPix[i] > 255){
                       srcPix[i] = 255;
                   }
                }
                dstOut.setPixels(0, 0, width, height, srcPix);
            }
        };
    }

}
