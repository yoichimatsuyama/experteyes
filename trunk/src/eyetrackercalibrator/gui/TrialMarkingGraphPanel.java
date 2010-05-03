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
package eyetrackercalibrator.gui;

import eyetrackercalibrator.framemanaging.CorniaReflectXYDataSet;
import eyetrackercalibrator.framemanaging.FrameManager;
import eyetrackercalibrator.framemanaging.FrameSynchronizor;
import eyetrackercalibrator.framemanaging.IlluminationXYDataSet;
import eyetrackercalibrator.framemanaging.InformationDatabase;
import eyetrackercalibrator.framemanaging.PupilXYDataset;
import org.jfree.data.xy.DefaultXYDataset;

/**
 *
 * @author SQ
 */
public class TrialMarkingGraphPanel extends GraphTabPanel {

    protected IlluminationXYDataSet illuminationDataSet = null;
    protected PupilXYDataset pupilXYDataset = null;
    protected CorniaReflectXYDataSet corniaXYDataset = null;
    protected final static String[] graphName = {
        "Illumination",
        "Pupil Loction",
        "Cornia Reflection",
    };
    protected final static int INDEX_ILLUMINATION_GRAPH_PANEL = 0;
    protected final static int INDEX_PUPIL_GRAPH_PANEL = 1;
    protected final static int INDEX_REFLECTION_GRAPH_PANEL = 2;
    IlluminationXYDataSet dataSet = null;
    protected FrameSynchronizor frameSynchronizor = null;

    public TrialMarkingGraphPanel() {
        super(graphName);
    }

    @Override
    protected void createAllCharts() {
        // Create pupil graph panel
        this.graphPanel[INDEX_ILLUMINATION_GRAPH_PANEL] = createChartPanel(null, 500, 525);
        this.graphPanel[INDEX_PUPIL_GRAPH_PANEL] = createChartPanel(null, 500, 525);
        this.graphPanel[INDEX_REFLECTION_GRAPH_PANEL] = createChartPanel(null, 500, 525);
    }

    /**
     */
    public void setEyeFrameManager(FrameManager eyeFrameManager) {
        if (eyeFrameManager != null) {
            // Populate pupil graph
            this.pupilXYDataset = new PupilXYDataset(eyeFrameManager);
            this.pupilXYDataset.setFrameSynchronizor(this.frameSynchronizor);
            setDataSet(pupilXYDataset, graphPanel[INDEX_PUPIL_GRAPH_PANEL]);
            // Populate cornia reflection graph
            this.corniaXYDataset = new CorniaReflectXYDataSet(eyeFrameManager);
            this.corniaXYDataset.setFrameSynchronizor(this.frameSynchronizor);
            setDataSet(corniaXYDataset, graphPanel[INDEX_REFLECTION_GRAPH_PANEL]);
        } else {
            // Otherwise clear out all data
            DefaultXYDataset emptyDataSet = new DefaultXYDataset();
            setDataSet(emptyDataSet, graphPanel[INDEX_PUPIL_GRAPH_PANEL]);
            setDataSet(emptyDataSet, graphPanel[INDEX_REFLECTION_GRAPH_PANEL]);
        }
    }

    public void setFrameSynchronizor(FrameSynchronizor frameSynchronizor) {
        this.frameSynchronizor = frameSynchronizor;

        if (this.corniaXYDataset != null) {
            this.corniaXYDataset.setFrameSynchronizor(this.frameSynchronizor);
        }

        if (this.pupilXYDataset != null) {
            this.pupilXYDataset.setFrameSynchronizor(this.frameSynchronizor);
        }

        if (this.illuminationDataSet != null) {
            this.illuminationDataSet.setFrameSynchronizor(frameSynchronizor);
        }
    }

    /** Setting data set for the illumination graph
     * @param infoDatabase
     */
    public void setIlluminationDataSet(InformationDatabase infoDatabase) {
        this.illuminationDataSet = new IlluminationXYDataSet(infoDatabase);
        this.illuminationDataSet.setFrameSynchronizor(this.frameSynchronizor);
        setDataSet(illuminationDataSet, this.graphPanel[INDEX_ILLUMINATION_GRAPH_PANEL]);
    }
}
