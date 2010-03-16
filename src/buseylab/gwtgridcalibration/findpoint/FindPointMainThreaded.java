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
import java.awt.Point;
import java.io.File;

/*
 * This class allows Ruj to interface with the point finding code 
 * 
 * Usage:
 * 
 * new FindjPointMainThreaded(projectRoot, hint, files);
 * project root is a string that describes the base of the subject
 *  i.e. /Volumes/Eyebook4/Evansville/RickEvan/ **** TRAILING SLASH IS NECSESARY
 * hint is the clicked point in small image coordinates (512 x 342)
 * files is a range of files
 * nThreads is the number of threads that we should use to complete the task
 * CompletionListener cl is Ruj's creation that gets activated when we are done
 */
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;

public class FindPointMainThreaded implements Runnable, FindPointListener {
    // any changes made here also need to reflected in GWTGridSetup saveGWTGrids
    double sigma = 1.0 * Math.PI;
    int numOrientations = 8;
    int numScales = 8;
    int size = 256;
    double[][][] freqKernels = GWTGrid.genFreqKernel(size, numScales, numOrientations, sigma);
    // maintain list of points to pass back to ruj
    Point[] points;
    // progress draw-er
    ProgressDraw pd = null;
    private String projectRoot;
    private Point hint;
    private File[] files;
    private int nThreads;
    private CompletionListener completionListener;
    private Semaphore semaphore = null;
    private int curFile = 0;
    private boolean isAlive = true;

    /**
     * returns the index of file in the array files, -1 if not found
     */
    public int fileIndex(File[] files, File file) {
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(file.getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Call this to cancel the execution of this class
     */
    synchronized public void cancel() {
        isAlive = false;
    }

    public FindPointMainThreaded(String projectRoot, Point hint, File[] files,
            int nThreads, CompletionListener completionListener) {
        this.projectRoot = projectRoot;
        this.hint = hint;
        this.files = files;
        this.nThreads = nThreads;
        this.completionListener = completionListener;
    }

    public void run() {
        
        Thread[] threads = new Thread[nThreads];
        points = new Point[files.length];
        int threadCount = 0;

        try {
            curFile = 0;
            // spawn initial threads
            for (int i = 0; i < nThreads && i < files.length; i++) {
                threads[i] = new Thread(new FindPoint(projectRoot, files[curFile++],
                        hint, freqKernels, this), 
                        "Find Point Thread" + (curFile - 1));
                threadCount++;
            }

            // Draw a blank page
            pd = new ProgressDraw(ImageIO.read(files[0]), new Point(0, 0), hint);

            // Set up semaphore to wait until all file is completed
            semaphore = new Semaphore(-(threadCount-1));//-(files.length - 1));

            // A hack here to make sure that we are safe from race condition
            for (int i = 0; i < nThreads && i < files.length; i++) {
                threads[i].start();
            }

            // Wait for job completion
            semaphore.acquire();

            // call the completed method
            pd.die();
            completionListener.completed(points);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** This method is for listening to completion of a FindPoint thread */
    synchronized public boolean completed(FindPoint invoker) {
        // monitor threads and replace as necessary
        if (isAlive) {
            int pointIdx = fileIndex(files, invoker.getSceneFile());

            points[pointIdx] = invoker.getPoint();

            pd.setImage(invoker.getImage());
            pd.setPoint(invoker.getPoint());
            pd.setHint(invoker.getHint());
            pd.setProgress(null, curFile, files.length);

            if (curFile < files.length && isAlive) {
                // start a new thread in its place if there is something left to do
                FindPoint findPoint = new FindPoint(
                        projectRoot, files[curFile++], hint, freqKernels, this);
                Thread t = new Thread(findPoint, "Find Point Thread" + (curFile - 1));
                t.start();
            }else{
                semaphore.release();
            }
        }else{
            semaphore.release();
        }
        return true;
    }
}
