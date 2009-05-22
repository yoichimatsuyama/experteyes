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
package buseylab.findcorner;

import buseylab.gwtgrid.GWTGrid;
import buseylab.gwtgrid.ImageUtils;
import buseylab.util.FileComparator;
import buseylab.util.PictureFilenameFilter;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;

/*
 * This class is our main driver class. It expecs a 1:1 mapping between corner hints and scene files
 * It generates NUM_CPUS-1 worker threads and 1 monitor thread.
 */
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FindCornersMainThreadedAsync implements Runnable, FindCorners.CompletionListener {

    public interface ProgressListener {

        /** @param fc is FindCorner object that can provide a way to visualize result */
        public void progress(int totalCompleted, FindCorners fc);

        /**
         * The method is called when the task is completed
         */
        public void completed();
    }
    protected File[] sceneFiles;
    protected Dimension hintSceneDim;
    protected File gwtGridFile;
    protected File cornerHintDir;
    protected File cornerOutputDir;
    protected boolean isPrintingOut = false;
    protected int currentScene;
    protected int totalCompleted = 0;
    protected ProgressListener listener = null;
    protected boolean alive = true;
    protected int numThreads = 1;
    protected LinkedList<FindCorners> findCornersesList =
            new LinkedList<FindCorners>();
    /** Lock for preventing race condition in killing and spawning new thread */
    protected ReentrantLock threadLock = new ReentrantLock();
    /** For good termination behavior */
    private Semaphore completionSemaphore = null;
    /**
     * any changes made to the following fields also need to
     * reflected in GWTGridSetup saveGWTGrids
     */
    double[][][] freqKernels = null;
    private double[][] magnitudeResps;
    private double[][] phaseResps;
    protected double sigma = 1.0 * Math.PI;
    protected int numOrientations = 8;
    protected int numScales = 8;
    protected int size = 256;

    /** 
     * Find corners using 
     * @param sceneFiles Files to find corners.
     * @param hintSceneDim Dimension of the hint scene file.  Assume that all
     * pictures that are used to creat hints have the same dimension
     * @param gwtGridFile  A file generated from GWTGridSetup.
     */
    public FindCornersMainThreadedAsync(File[] sceneFiles, Dimension hintSceneDim,
            File gwtGridFile, File cornerHintDir, File cornerOutputDir,
            ProgressListener listener) {
        this.sceneFiles = sceneFiles;
        this.hintSceneDim = hintSceneDim;
        this.gwtGridFile = gwtGridFile;
        this.cornerHintDir = cornerHintDir;
        this.cornerOutputDir = cornerOutputDir;
        this.listener = listener;
    }

    public ProgressListener getListener() {
        return listener;
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

    public void run() {
        this.freqKernels = GWTGrid.genFreqKernel(this.size,
                this.numScales, this.numOrientations,
                this.sigma);


        try {
            this.magnitudeResps = new double[4][];
            this.phaseResps = new double[4][];

            FileInputStream fis = new FileInputStream(this.gwtGridFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            for (int corner = 0; corner < 4; corner++) {
                this.magnitudeResps[corner] = (double[]) ois.readObject();
                this.phaseResps[corner] = (double[]) ois.readObject();
            }
            ois.close();
            fis.close();

            this.currentScene = getIndexOfFirstFrame(this.sceneFiles);
            // Check if we can find the first frame
            if (this.currentScene < 0) {
                // Signal completion and return
                this.listener.completed();
                return;
            }

            // Here to make sure that any change to this.numThreads won't effect
            // termination condition
            int totalThread = this.numThreads;

            /** Mechanic for waiting for all threads to finish (+1 because 0 is still block) */
            this.completionSemaphore = new Semaphore(-totalThread + 1);

            /**
             * spawn initial threads
             * Lock here so that there is no race condition between starting
             * threads and the completed thread
             */
            this.threadLock.lock();
            for (int i = 0; alive &&
                    i < totalThread && this.currentScene < this.sceneFiles.length;
                    i++) {
                if (!assignSceneProcessing(this.currentScene)) {
                    i--;
                }
                this.currentScene++;
            }
            this.threadLock.unlock();

            // Wait for all to finish before cleaning up
            this.completionSemaphore.acquire();


        } catch (Exception e) {
            e.printStackTrace();
        }

        // Signal completion and return
        this.listener.completed();
    }

    /**
     * This method assigning the scene to the FinCorner thread
     * @return false if assignment fails or true otherwise.
     */
    private boolean assignSceneProcessing(int sceneNumber) {
        // Sanity check
        if (sceneNumber >= this.sceneFiles.length) {
            return false;
        }
        File curSceneFile = this.sceneFiles[sceneNumber];

        if (curSceneFile == null) {
            Logger.getLogger(FindCornersMainThreadedAsync.class.getName()).log(
                    Level.SEVERE, "Skip because of missing file name number " + sceneNumber + 1);

            return false;
        }

        String curSceneFilename = curSceneFile.getName();
        String curSceneFilenameWithoutExt = curSceneFilename.substring(
                0, curSceneFilename.lastIndexOf('.'));
        String curHintFilename = curSceneFilenameWithoutExt + ".txt";
        String curCornerFilename = curSceneFilenameWithoutExt + ".txt";
        File curHintFile = new File(this.cornerHintDir, curHintFilename);
        File curCornerFile = new File(this.cornerOutputDir, curCornerFilename);
        // check to make sure hint file and scene file exists
        if (curHintFile.exists() && curSceneFile.exists()) {
            FindCorners fc = new FindCorners(
                    curSceneFile, curHintFile, this.hintSceneDim,
                    this.magnitudeResps, this.phaseResps, this.freqKernels,
                    curCornerFile, this);
            // Add to queue
            this.findCornersesList.add(fc);
            // Start running
            Thread t = new Thread(fc, "Find Corner Thread " + sceneNumber);
            t.start();
            return true;
        } else {
            String error = "Skip " + curCornerFilename + "because of missing ";
            if (!curHintFile.exists()) {
                error += " the hintfile";
            }
            if (!curSceneFile.exists()) {
                if (!curHintFile.exists()) {
                    error += " and ";
                }
                error += " the scenefile";
            }
            Logger.getLogger(FindCornersMainThreadedAsync.class.getName()).log(
                    Level.SEVERE, error);

            return false;
        }
    }


    /*
     * returns the first index of a dead thread or -1 if all are alive
     */
    public static int firstDeadIndex(Thread[] threads) {
        for (int i = 0; i <
                threads.length; i++) {
            if (!threads[i].isAlive()) {
                return i;
            }

        }
        return -1;
    }

// return the frame number for a filename
    public static long parseFrameno(File f) {
        String filename = f.getName();
        String framenoStr = filename.substring(filename.indexOf('_') + 1, filename.indexOf('.'));
        return Long.parseLong(framenoStr);
    }
//CAUTION- THIS ASSUMES THAT THE FILES ARE IN ALPHABETICAL ORDER- SHOULD SORT FIRST.

    public int getIndexOfFirstFrame(File[] files) throws Exception {
        FileInputStream fis = new FileInputStream(this.gwtGridFile);
        ObjectInputStream ois = new ObjectInputStream(fis);

        for (int corner = 0; corner <
                4; corner++) {
            // don't care about these. get them off the file stack
            ois.readObject();
            ois.readObject();
        }

        String firstFilename = (String) ois.readObject();
        ois.close();
        fis.close();
        // search for this filename in the files array
        for (int i = 0; i <
                files.length; i++) {
            if (files[i].getName().equals(firstFilename)) {
                return i;
            }

        }

        // not found
        return -1;
    }

    /** This is kept for legacy compatibility purpose */
    public static void main(String[] args) {

        String CORNER_HINTS_ROOT = "CornerHints";
        String LARGE_SCENE_ROOT = "LargeCleanedScene";
        String CORNER_OUT_ROOT = "Corners";

        // Check output dir
        File cornerDir = new File(CORNER_OUT_ROOT);
        if (!cornerDir.exists()) {
            cornerDir.mkdirs();
        }

        File[] scenes = new File(LARGE_SCENE_ROOT).listFiles(new PictureFilenameFilter());
        //CAUTION- WE SHOULD SORT scenes HERE IN CASE THEY ARE NOT IN ALPHABETICAL ORDER
        Arrays.sort(scenes, new FileComparator());

        FindCornersMainThreadedAsync findCornersMainThreadedAsync =
                new FindCornersMainThreadedAsync(scenes,
                new Dimension(512, 512),
                new File("gwtgrids.dat"),
                new File(CORNER_HINTS_ROOT),
                new File(CORNER_OUT_ROOT),
                new ProgressListener() {

                    int numberDone = 0;
                    CornerDraw cd = null;

                    public synchronized void progress(int totalCompleted, FindCorners fc) {
                        // Show corner drawing
                        if (fc != null) {
                            if (cd == null) {
                                cd = new CornerDraw(ImageUtils.loadImage(fc.getImage()),
                                        fc.getCorners(),
                                        fc.getHints());
                            } else {
                                cd.setImage(ImageUtils.loadImage(fc.getImage()));
                                cd.setCorners(fc.getCorners());
                                cd.setHints(fc.getHints());
                            }

                        }
                        numberDone++;

                    }

                    public void completed() {
                        // Do nothing
                    }
                });

        // args[1] is the number of CPUs we want to utilize
        int numCPUs = Integer.parseInt(args[0]);
        findCornersMainThreadedAsync.setNumThreads(numCPUs);

        Thread t = new Thread(findCornersMainThreadedAsync);
        t.start();
        try {
            // Wait for computation to finish
            t.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(FindCornersMainThreadedAsync.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getNumThreads() {
        return numThreads;
    }

    /**
     * Set total number of threads to be used when finding corners.
     * The number will be cap to max of total available CPUs and min of 1.
     * The number of CPUs must be set before start running (calling
     * start() or run() method).  Any change afterward is ignored.
     * @return the valued actually set
     */
    public int setNumThreads(int numThreads) {
        // Cap to sanity and possible number of CPU available
        numThreads = Math.min(Runtime.getRuntime().availableProcessors(),
                Math.max(1, numThreads));
        this.numThreads = numThreads;
        return numThreads;
    }

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    public int getNumOrientations() {
        return numOrientations;
    }

    public void setNumOrientations(int numOrientations) {
        this.numOrientations = numOrientations;
    }

    public int getNumScales() {
        return numScales;
    }

    public void setNumScales(int numScales) {
        this.numScales = numScales;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isIsPrintingOut() {
        return isPrintingOut;
    }

    public void setPrintingOut(boolean isPrintingOut) {
        this.isPrintingOut = isPrintingOut;
    }

    /**
     * This method is called when a FindCorners thread finished
     * execution
     */
    public void completed(FindCorners whoIsComplete) {
        this.threadLock.lock();
        this.totalCompleted++;
        if (whoIsComplete != null) {
            // Remove ourself
            this.findCornersesList.remove(whoIsComplete);
        }

        // Schedule a find corner this must be in lock since it adds
        // a FindCorner to the queue
        // While asignment turn sour keep moving on
        while (alive && this.currentScene < this.sceneFiles.length &&
                !assignSceneProcessing(this.currentScene)) {
            this.currentScene++;
            this.totalCompleted++;
        }
        this.currentScene++;

        if (!alive || this.currentScene >= this.sceneFiles.length) {
            // Signal completion when there is nothing more
            this.completionSemaphore.release();
        }

        this.threadLock.unlock();
        if (whoIsComplete != null && this.isPrintingOut) {
            System.out.println("Time for one scene = " +
                    whoIsComplete.returnTotalTime() / 1000.0 +
                    ". TotalTime = " +
                    whoIsComplete.returnTotalTime() / 1000.0 *
                    this.sceneFiles.length / 60.0 / 60.0 /
                    (double) this.numThreads +
                    " hours. Time left = " +
                    whoIsComplete.returnTotalTime() / 1000.0 *
                    (this.sceneFiles.length - this.totalCompleted) /
                    60.0 / 60.0 / (double) this.numThreads);
        }

        if (this.listener != null) {
            this.listener.progress(totalCompleted, whoIsComplete);
        }

    }

    /**
     * Signal the thread to stop
     */
    public void kill() {
        this.threadLock.lock();
        this.alive = false;
        // Clear all queue
        while (!findCornersesList.isEmpty()) {
            FindCorners fc = findCornersesList.removeFirst();
            fc.kill();
        }

        this.threadLock.unlock();
    }
}
