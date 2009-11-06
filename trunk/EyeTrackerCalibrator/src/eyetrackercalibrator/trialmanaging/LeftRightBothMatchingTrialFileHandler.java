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
import eyetrackercalibrator.math.EstimateTrialMarking;
import eyetrackercalibrator.trialmanaging.Trial.TrialInfo;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.plot.IntervalMarker;

/**
 *
 * @author eeglab
 */
public class LeftRightBothMatchingTrialFileHandler extends TrialFileHandler {

    public LeftRightBothMatchingTrialFileHandler() {
        super(".dat2", "Left Right Both matching experiment");
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

        int lineNumber = 1;
        int offset = 0;

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(inputFile));

            // Read in each line
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                line = line.trim();
                if (line.length() > 0) {// Skip blank line
                    String[] tokens = line.split("[\\p{javaWhitespace}:]+");
                    // Special case for every third line
                    if (lineNumber != 0) {
                        offset = 0;
                    } else {
                        offset = 1;
                    }
                    if (tokens.length >= 3 + offset) { // Sanity check
                        TrialInfo info = new TrialInfo();
                        info.name = tokens[0];
                        if (tokens.length == 3 + offset) {
                            info.startTime = Long.parseLong(tokens[1 + offset]);
                        } else {
                            info.startTime = Long.parseLong(tokens[2 + offset]);
                        }
                        trialInfos.add(info);
                    } else {
                        // We have a breach in format! Abort
                        return null;
                    }

                    // Increment line number
                    lineNumber = (lineNumber + 1) % 3;
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

    public Vector<TrialMarker> estimateTrials(InformationDatabase infoDatabase,
            Trial trial, int firstTrialStartFrame, int lastTrialStartFrame,
            FrameSynchronizor frameSynchronizor, IntervalMarkerManager intervalMarkerManager) {

        int trialType = 0;
        Vector<TrialMarker> trialMarkers = new Vector<TrialMarker>();

        double totalFrames = (double) (lastTrialStartFrame - firstTrialStartFrame);
        long endTime = trial.trialInfos.get(trial.trialInfos.size() - 1).startTime;
        long startTime = trial.trialInfos.get(0).startTime;
        double totalTime = (double) (endTime - startTime);

        // Compute effective frame rate
        double frameRate = totalFrames / totalTime;
        for (Iterator<TrialInfo> it = trial.trialInfos.iterator(); it.hasNext();) {
            TrialInfo info = it.next();
            TrialMarker marker = new TrialMarker();
            marker.label = info.name;
            if (intervalMarkerManager != null) {
                IntervalMarker intervalMarker = intervalMarkerManager.getNewIntervalMarker();
                marker.setIntervalMarker(intervalMarker);
                intervalMarker.setPaint(trialColor[trialType]);
            }

            int startFrame = (int) ((info.startTime - startTime) * frameRate) + firstTrialStartFrame;
            int endFrame = (int) ((info.startTime - startTime + trialLength[trialType]) * frameRate) + firstTrialStartFrame;

            marker.setStartFrame(startFrame,
                    frameSynchronizor.getEyeFrame(startFrame),
                    frameSynchronizor.getSceneFrame(startFrame));
            marker.setEndFrame(endFrame,
                    frameSynchronizor.getEyeFrame(endFrame),
                    frameSynchronizor.getSceneFrame(endFrame));

            trialMarkers.add(marker);

            trialType = (trialType + 1) % 3;
        }


        return trialMarkers;
    }
    /** Trial length of 5,5,10 seconds*/
    public static double[] trialLength = {5 * 1000, 5 * 1000, 10 * 1000};
    public static Color[] trialColor = {Color.GREEN, Color.ORANGE, Color.GREEN};

    @Override
    public void estimateTrialMarking(InformationDatabase informationDatabase,
            TrialMarker[] trials, FrameSynchronizor frameSynchronizor) {
        int firstFrame = trials[0].startSceneFrame;
        int lastFrame = trials[trials.length - 1].startSceneFrame;

        // Compute kernal
        EstimateTrialMarking est =
                new EstimateTrialMarking(informationDatabase);
        double[] k = est.estimateGroup(
                firstFrame, lastFrame);

        // Reestimate the trial
        for (int i = 0; i < trials.length - 1 && (i + 2) < trials.length; i = i + 3) {

            /* We are working on a trial set of 3 trials.
             * |-Sec 1-|-Sec 2-|-Sec 3-|-Sec 1-|-Sec 2-|-Sec 3-|
             * |----------Set j--------|------- Set k -------|          
             */

            // Set bounddary to middle of current trial set to middle of next set of trials
            int set_j_Sec_1_Bound = (trials[i].stopSceneFrame + trials[i].startSceneFrame) / 2;
            int set_j_Sec_2_Bound = (trials[i + 1].stopSceneFrame + trials[i + 1].startSceneFrame) / 2;
            int set_j_Sec_3_Bound = (trials[i + 2].stopSceneFrame + trials[i + 2].startSceneFrame) / 2;

            // Find boundary between Sec 2 and Sec 3
            // Try estimating group locally
            k = est.estimateGroup(
                    set_j_Sec_2_Bound,
                    set_j_Sec_3_Bound);


            // Set start frame to prevent bug
            trials[i + 1].setStartFrame(trials[i + 1].startSceneFrame - screenViewOffset,
                    eyeViewOffset, screenViewOffset);

            // Search from left bound to right bound
            int pos = set_j_Sec_2_Bound;

            while (pos <= set_j_Sec_3_Bound && !(est.isHighGroup(pos))) { //&& est.getDiff(pos) < 0)) {
                pos++;
            }

            // Set new trial end if the bound is not reached otherwise no setting
            if (pos < set_j_Sec_3_Bound) {
                trials[i + 1].setEndFrame(pos - screenViewOffset, eyeViewOffset, screenViewOffset);
            } else {
                // Change the color to mark no change
                trials[i + 1].getIntervalMarker().setPaint(Color.BLUE);
            }

            // Search from right bound to left bound
            pos = set_j_Sec_3_Bound;

            // Search further for beginning of another trial
            while (pos >= set_j_Sec_2_Bound && !(est.isHighGroup(pos))) {// && est.getDiff(pos) < 0)) {
                pos--;
            }

            // Set new trial start if the bound is not reached
            if (pos > set_j_Sec_2_Bound) {
                trials[i + 2].setStartFrame(pos - screenViewOffset, eyeViewOffset, screenViewOffset);
            } else {
                trials[i + 2].setStartFrame(trials[i + 2].startSceneFrame - screenViewOffset,
                        eyeViewOffset, screenViewOffset);
                trials[i + 2].getIntervalMarker().setPaint(Color.BLUE);
            }

            // Set start point to prevent bug
            trials[i].setStartFrame(trials[i].startSceneFrame - screenViewOffset,
                    eyeViewOffset, screenViewOffset);
            //--------------
            // Find boundary between Section 1 and 2

            // Try estimating group locally
            k = est.estimateGroup(
                    set_j_Sec_1_Bound,
                    set_j_Sec_2_Bound);


            // Compute bound
            int default_1_2_Bound = (trials[i].startSceneFrame + trials[i + 1].stopSceneFrame) / 2 - -screenViewOffset;


            // Search from left bound to right bound
            pos = set_j_Sec_1_Bound;
            while (pos < set_j_Sec_2_Bound && !(est.isHighGroup(pos))) {// && est.getDiff(pos) < 0)) {
                pos++;
            }
            // Set new boundary if the bound is not reached
            if (pos < set_j_Sec_2_Bound) {
                trials[i].setEndFrame(pos - screenViewOffset, eyeViewOffset, screenViewOffset);
            } else {
                trials[i].setEndFrame(default_1_2_Bound,
                        eyeViewOffset, screenViewOffset);
                trials[i].getIntervalMarker().setPaint(Color.BLUE);
            }

            // Search from left bound to right bound
            pos = set_j_Sec_2_Bound;
            while (pos > set_j_Sec_1_Bound && !(est.isHighGroup(pos))) {// && est.getDiff(pos) < 0)) {
                pos--;
            }
            // Set new boundary if the bound is not reached
            if (pos > set_j_Sec_1_Bound) {
                trials[i + 1].setStartFrame(pos - screenViewOffset, eyeViewOffset, screenViewOffset);
            } else {
                trials[i + 1].setStartFrame(default_1_2_Bound,
                        eyeViewOffset, screenViewOffset);
                trials[i + 1].getIntervalMarker().setPaint(Color.BLUE);
            }
            //--------------

            // Does nothing more if we reaches the end
            if (i + 3 < trials.length) {
                // Try estimating locally
                k = est.estimateGroup(
                        set_j_Sec_2_Bound,
                        trials[i + 3].startSceneFrame);


                // If not then estimate the boundary between two set
                int set_k_Sec_1_Bound = (trials[i + 3].stopSceneFrame + trials[i + 3].startSceneFrame) / 2;

                // Search from left bound to right bound
                pos = set_j_Sec_3_Bound;

                while (pos <= set_k_Sec_1_Bound && !(est.isHighGroup(pos) && est.getDiff(pos) > 0)) {
                    pos++;
                }

                // Set new trial end if the bound is not reached otherwise no setting
                if (pos < set_k_Sec_1_Bound) {
                    trials[i + 2].setEndFrame(pos - screenViewOffset, eyeViewOffset, screenViewOffset);
                } else {
                    // Change the color to mark no change
                    trials[i + 2].getIntervalMarker().setPaint(Color.BLUE);
                }

                // Try estimating locally
                k = est.estimateGroup(
                        (trials[i + 2].stopSceneFrame + trials[i + 3].startEyeFrame) / 2,
                        set_k_Sec_1_Bound);

                // Search from right bound to left bound
                pos = set_k_Sec_1_Bound;

                // Search further for beginning of another trial
                while (pos >= set_j_Sec_3_Bound && !(est.isHighGroup(pos) && est.getDiff(pos) < 0)) {
                    pos--;
                }

                // Set new trial start if the bound is not reached
                if (pos > set_j_Sec_3_Bound) {
                    trials[i + 3].setStartFrame(pos - screenViewOffset, eyeViewOffset, screenViewOffset);
                } else {
                    trials[i + 3].getIntervalMarker().setPaint(Color.BLUE);
                }
            }
        }
    }
}
