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

package buseylab.cornerselector;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 * This thread load the given picture into given label
 * @author ruj
 */
public class ImageLoadingThread extends Thread{

    /** Semaphore to synchronize image loading */
    Semaphore sem = new Semaphore(0);
    boolean alive = true;
    File fileToOpen = null;
    CornerSelectLabel label;
    Point topLeft, topRight, bottomLeft, bottomRight;
    
    public ImageLoadingThread(String name, CornerSelectLabel label) {
        super(name);
        this.label = label;
    }

    public ImageLoadingThread(CornerSelectLabel label) {
        super("Image Loading Thread");
        this.label = label;
    }

    @Override
    public void run() {
        File currentFile = null;
        while(alive){
            try {
                sem.acquire();
                if (currentFile != this.fileToOpen) {
                    // Open a file
                    currentFile = this.fileToOpen;
                    BufferedImage image = ImageTools.loadImage(currentFile);
                    if(image != null){
                        this.label.setIcon(new ImageIcon(image));
                        this.label.setSelectedCorner(topLeft, topRight, bottomLeft, bottomRight);
                        this.label.setText(null);
                    }else{
                        this.label.setIcon(null);
                        this.label.setText("N/A");
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ImageLoadingThread.class.getName()).log(Level.SEVERE, null, ex);
                alive = false;
            }
        }
        this.label = null;
    }
    
    public void setFileToOpen(File fileToOpen, Point topLeft, Point topRight, Point bottomLeft, Point bottomRight){
        if(alive){
            this.fileToOpen = fileToOpen;
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomLeft = bottomLeft;
            this.bottomRight = bottomRight;
            sem.release();
        }
    }
    
    /** Properly stop the thread */
    public void kill(){
        alive = false;
        sem.release();
    }
}
