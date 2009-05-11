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
 * GraphTabPanel.java
 *
 * Created on October 8, 2007, 4:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package eyetrackercalibrator.gui;

import eyetrackercalibrator.framemanaging.CornerSimilarityXYDataSet;
import eyetrackercalibrator.framemanaging.CorniaReflectXYDataSet;
import eyetrackercalibrator.framemanaging.FrameManager;
import eyetrackercalibrator.framemanaging.PupilXYDataset;
import org.jfree.data.xy.DefaultXYDataset;

/**
 *
 * @author rakavipa
 */
public class FrameInfoGraphTabPanel extends GraphTabPanel {

    private CornerSimilarityXYDataSet cornerSimilarityXYDataSet = null;
    private PupilXYDataset pupilXYDataset = null;
    private CorniaReflectXYDataSet corniaXYDataset = null;
    // Edit tab order and label here
    // For legend name.  Edit appropriate XYDataset class
    private final static int INDEX_PUPIL_GRAPH_PANEL = 0;
    private final static int INDEX_REFLECTION_GRAPH_PANEL = 1;
    private final static int INDEX_CORNER_SIM_GRAPH_PANEL = 2;
    private final static String[] graphName = {
        "Pupil Loction",
        "Cornia Reflection",
        "Corner Similarity"
    };
    private int eyeOffset = 0;
    private int screenOffset = 0;

    /**
     * Creates a new instance of GraphTabPanel
     * To populate the panel with information, setFrameManager must be called
     */
    public FrameInfoGraphTabPanel() {
        super(graphName);
    }

    @Override
    protected void createAllCharts() {
        // Create pupil graph panel
        this.graphPanel[INDEX_PUPIL_GRAPH_PANEL] = createChartPanel(null, 500, 525);
        this.graphPanel[INDEX_REFLECTION_GRAPH_PANEL] = createChartPanel(null, 500, 525);
        this.graphPanel[INDEX_CORNER_SIM_GRAPH_PANEL] = createChartPanel(null, 500, 1);
    }

    /**
     */
    public void setEyeFrameManager(FrameManager eyeFrameManager) {
        if (eyeFrameManager != null) {
            // Populate pupil graph
            this.pupilXYDataset = new PupilXYDataset(eyeFrameManager);
            this.pupilXYDataset.setOffset(eyeOffset);
            setDataSet(pupilXYDataset, graphPanel[INDEX_PUPIL_GRAPH_PANEL]);
            // Populate cornia reflection graph
            this.corniaXYDataset = new CorniaReflectXYDataSet(eyeFrameManager);
            this.corniaXYDataset.setOffset(eyeOffset);
            setDataSet(corniaXYDataset, graphPanel[INDEX_REFLECTION_GRAPH_PANEL]);
        } else {
            // Otherwise clear out all data
            DefaultXYDataset emptyDataSet = new DefaultXYDataset();
            setDataSet(emptyDataSet, graphPanel[INDEX_PUPIL_GRAPH_PANEL]);
            setDataSet(emptyDataSet, graphPanel[INDEX_REFLECTION_GRAPH_PANEL]);
        }
    }

    public void setScreenFrameManager(FrameManager screenFrameManager) {
        if (screenFrameManager != null) {
            // Populate corner similarity graph
            this.cornerSimilarityXYDataSet =
                    new CornerSimilarityXYDataSet(screenFrameManager, 510);
            this.cornerSimilarityXYDataSet.setOffset(screenOffset);
            setDataSet(
                    cornerSimilarityXYDataSet,
                    graphPanel[INDEX_CORNER_SIM_GRAPH_PANEL]);

        } else {
            // Otherwise clear out all data
            DefaultXYDataset emptyDataSet = new DefaultXYDataset();
            setDataSet(emptyDataSet, graphPanel[INDEX_CORNER_SIM_GRAPH_PANEL]);
        }
    }

    public void setOffset(int eyeOffset, int screenOffset) {
        this.eyeOffset = eyeOffset;
        this.screenOffset = screenOffset;

        if (this.cornerSimilarityXYDataSet != null) {
            this.cornerSimilarityXYDataSet.setOffset(screenOffset);
        }

        if (this.corniaXYDataset != null) {
            this.corniaXYDataset.setOffset(eyeOffset);
        }

        if (this.pupilXYDataset != null) {
            this.pupilXYDataset.setOffset(eyeOffset);
        }
    }
}
