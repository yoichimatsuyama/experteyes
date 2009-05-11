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

import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.ParameterList;
import util.TerminationListener;

public class FitEyeModelMain {

    /*
     * this method monitors the thread array for completion.
     * returns false if any thread is alive.
     * returns true when they are all dead
     */
    public static boolean allDead(Thread[] threads) {
        for (int i = 0; i < threads.length; i++) {
            if (threads[i].isAlive()) {
                return false;
            }
        }
        return true;
    }

    /*
     * returns the first index of a dead thread or -1 if all are alive
     */
    public static int firstDeadIndex(Thread[] threads) {
        for (int i = 0; i < threads.length; i++) {
            if (!threads[i].isAlive()) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        if (!(args.length == 3 || args.length == 4)) {
            printUsage();
            return;
        }

        // args[0] is the directory we want to analyze
        String eyeRoot = args[0];
        // Check if the firectory exists
        File dir = new File(eyeRoot);
        if (!dir.exists()) {
            System.out.println(eyeRoot + " directory does not exists!");
            return;
        }
        File[] eyeFiles = dir.listFiles(new NotHiddenPictureFilter());
        int curEyeFile = 0;

        // args[1] is the number of CPUs we want to utilize
        int numCPUs;
        try {
            numCPUs = Integer.parseInt(args[1]);

        } catch (NumberFormatException numberFormatException) {
            printUsage();
            return;
        }

        //Thread[] threads = new Thread[numCPUs - 1];

        // args[2] is the parameter file
        ParameterList parameters = ParameterList.load(new File(args[2]));
        if (parameters == null) {
            System.out.println("Cannot find " + args[2]);
            return;
        }

        // Cap number of cpu to the job available
        numCPUs = Math.min(eyeFiles.length, numCPUs);
        final Semaphore sem = new Semaphore(0);
        // spawn numCPUs-1 FitEyeModel threads
        for (int i = 0; i < numCPUs; i++) {
            FitEyeModel fem = null;
            if (args.length == 4) {
                fem = new FitEyeModel(
                        eyeFiles[curEyeFile], new File(args[3]), parameters);
            } else {
                fem = new FitEyeModel(
                        eyeFiles[curEyeFile], parameters);
            }
            fem.setTerminationListener(new TerminationListener() {

                public void complete() {
                    sem.release();
                }
            });
            Thread threads = new Thread(fem, "Eye fitting " + curEyeFile);
            threads.start();
            curEyeFile++;
        }
        // monitor threads, and regenerate dead ones
        while (curEyeFile < eyeFiles.length) {
            try {
                // Check if there is available thread
                sem.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(FitEyeModelMain.class.getName()).log(Level.SEVERE, null, ex);
                // Abort when interrupted
                return;
            }
            
            FitEyeModel fem = null;
            if (args.length == 4) {
                fem = new FitEyeModel(
                        eyeFiles[curEyeFile], new File(args[3]), parameters);
            } else {
                fem = new FitEyeModel(
                        eyeFiles[curEyeFile], parameters);
            }
            
            fem.setTerminationListener(new TerminationListener() {

                public void complete() {
                    sem.release();
                }
                });
            Thread threads = new Thread(fem, "Eye fitting " + curEyeFile);
            threads.start();
            curEyeFile++;
        }
        // wait for remaining threads to die before exiting
        for (int i = 0; i < numCPUs; i++) {
            try {
                sem.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(FitEyeModelMain.class.getName()).log(Level.SEVERE, null, ex);
                // Abort when interrupted
                return;
            }
        }
    }

    public static void printUsage() {
        System.out.println("Usage: [Eye frame directory] [# of cpu to use] [parameter file] [optional: outputdir]");
    }
}
