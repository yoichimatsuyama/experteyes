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
package eyetrackercalibrator.trialmanaging;

import eyetrackercalibrator.framemanaging.FrameSynchronizor;
import eyetrackercalibrator.framemanaging.InformationDatabase;
import eyetrackercalibrator.gui.util.IntervalMarkerManager;
import eyetrackercalibrator.trialmanaging.Trial.TrialInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eeglab
 */
public class ListATrialFileHandler extends TrialFileHandler {

    public ListATrialFileHandler() {
        super(".dat3", "List A experiment");
    }

    /**
     * 
     * @param inputFile
     * @return null if there is error in format or file opening
     */
    public Trial parse(File inputFile) {
        Vector<TrialInfo> trialInfos = new Vector<TrialInfo>();
        Trial result = new Trial();
        result.trialInfos = trialInfos;

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(inputFile));

            // Read in each line
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                line = line.trim();
                if (line.length() > 0) {
                    String[] tokens = line.split("[\\p{javaWhitespace}:]+");
                    if (tokens.length >= 3) { // Sanity check
                        TrialInfo info = new TrialInfo();
                        info.name = tokens[0];
                       
                            info.startTime = Long.parseLong(tokens[3]);
                        //info.endTime = Long.parseLong(tokens[4]);
                        trialInfos.add(info);
                    } else {
                        // We have a breach in format! Abort
                        return null;
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(BothMatchingTrialFileHandler.class.getName()).log(Level.SEVERE, null, ex);
            result = null;
        } finally {
            try {
                in.close();
            } catch (IOException discard) {
            }
        }
        return result;
    }
    public static double trialLength = 20 * 1000; // 20 seconds time

    public Vector<TrialMarker> estimateTrials(InformationDatabase infoDatabase,
            Trial trial, int firstTrialStartFrame, int lastTrialStartFrame,
            FrameSynchronizor frameSynchronizor, IntervalMarkerManager intervalMarkerManager) {

        Vector<TrialMarker> trialMarkers = new Vector<TrialMarker>();

        double totalFrames = (double) (lastTrialStartFrame - firstTrialStartFrame);
        long endTime = trial.trialInfos.get(trial.trialInfos.size() - 1).startTime;
        long startTime = trial.trialInfos.get(0).startTime;
        double totalTime = (double) (endTime - startTime);

        // Compute effective frame rate
        double frameRate = totalFrames / totalTime;

        // Loop for each trial
        for (Iterator<TrialInfo> it = trial.trialInfos.iterator(); it.hasNext();) {
            TrialInfo info = it.next();

            TrialMarker marker = new TrialMarker();
            marker.label = info.name;
            if (intervalMarkerManager != null) {
                marker.setIntervalMarker(intervalMarkerManager.getNewIntervalMarker());
            }
            int startFrame = (int) ((info.startTime - startTime) * frameRate) + firstTrialStartFrame;
            int endFrame = (int) ((info.startTime - startTime + trialLength) * frameRate) + firstTrialStartFrame;

            marker.setStartFrame(startFrame, frameSynchronizor.getEyeFrame(startFrame),
                    frameSynchronizor.getSceneFrame(startFrame));
            marker.setEndFrame(endFrame, frameSynchronizor.getEyeFrame(endFrame),
                    frameSynchronizor.getSceneFrame(endFrame));

            trialMarkers.add(marker);
        }


        return trialMarkers;
    }

    @Override
    public void estimateTrialMarking(InformationDatabase informationDatabase,
            TrialMarker[] trials, FrameSynchronizor frameSynchronizor) {
//        int firstFrame = trials[0].startSceneFrame;
//        int lastFrame = trials[trials.length - 1].startSceneFrame;
//
//        // Compute kernal
//        EstimateTrialMarking est =
//                new EstimateTrialMarking(informationDatabase);
//
//        // Reestimate the trial
//        for (int i = 0; i < trials.length - 1; i++) {
//            // Set bounddary to middle of current trial to middle of next trial
//            int leftBound = (trials[i].stopSceneFrame + trials[i].startSceneFrame) / 2;
//            int rightBound = (trials[i + 1].stopSceneFrame + trials[i + 1].startSceneFrame) / 2;
//
//            double[] k = est.estimateGroup(leftBound, rightBound);
//
//            // Set start frame to to prevent bug
//            int frame = frameSynchronizor.sceneFrameToSyncFrame(trials[i].startSceneFrame);
//            if(frame < 1){
//                frame = frameSynchronizor.sceneFrameToSyncFrame(trials[i].startEyeFrame);
//            }
//            trials[i].setStartFrame(frame,frameSynchronizor.getEyeFrame(frame),
//                    frameSynchronizor.getSceneFrame(frame));
//
//            // Search from left bound to right bound
//            int pos = leftBound;
//
//            while (pos <= rightBound && !(est.isHighGroup(pos) && est.getDiff(pos) > 0)) {
//                pos++;
//            }
//
//            // Set new trial end if the bound is not reached otherwise no setting
//            if (pos < rightBound) {
//                trials[i].setEndFrame(pos - screenViewOffset, eyeViewOffset, screenViewOffset);
//            // Change the color to mark the change
//            } else {
//                trials[i].getIntervalMarker().setPaint(Color.BLUE);
//            }
//
//            // Search from right bound to left bound
//            pos = rightBound;
//
//            // Search further for beginning of another trial
//            while (pos >= leftBound && !(est.isHighGroup(pos) && est.getDiff(pos) < 0)) {
//                pos--;
//            }
//
//            // Set new trial start if the bound is not reached
//            if (pos > leftBound) {
//                trials[i + 1].setStartFrame(pos - screenViewOffset, eyeViewOffset, screenViewOffset);
//            } else {
//                trials[i + 1].getIntervalMarker().setPaint(Color.BLUE);
//            }
//        }
    }


}
