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
import java.io.File;
import java.util.Vector;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author ruj
 */
public abstract class TrialFileHandler extends FileFilter {

    public TrialFileHandler(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }
    public String extension;
    public String description;

    @Override
    public boolean accept(File f) {
        String ext = Utils.getExtension(f);

        return ext != null &&
                (ext.equalsIgnoreCase(extension) ||
                ext.equalsIgnoreCase(extension.concat(".txt")));
    }

    @Override
    public String getDescription() {
        return "(" + extension + ") " + description;
    }

    /**
     * Parsing input file and output the trial object
     * @param inputFile
     * @return null if there is error in format or file opening
     */
    public abstract Trial parse(File inputFile);

    /**
     * Process information using all provided informaion to create an array 
     * (returned in the form of vector) of trial marker
     * @param infoDatabase 
     * @param trial 
     * @param firstTrialStartFrame 
     * @param lastTrialStartFrame 
     * @param eyeOffset 
     * @param screenOffset 
     * @param intervalMarkerManager 
     * @return
     */
    public abstract Vector<TrialMarker> estimateTrials(
            InformationDatabase infoDatabase, Trial trial,
            int firstTrialStartFrame, int lastTrialStartFrame,
            FrameSynchronizor frameSynchronizor,
            IntervalMarkerManager intervalMarkerManager);

    /**
     *  This method estimate trials from Illumination computed
     * 
     * @param informationDatabase Database of Illumination computed
     * @param trials array of trials.  The content is ecpected to change after
     * going through this method
     */
    public abstract void estimateTrialMarking(
            InformationDatabase informationDatabase, TrialMarker[] trials,
            FrameSynchronizor frameSynchronizor);
}
