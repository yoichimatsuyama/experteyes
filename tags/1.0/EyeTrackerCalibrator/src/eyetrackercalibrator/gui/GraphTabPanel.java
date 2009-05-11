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

package eyetrackercalibrator.gui;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.jdesktop.layout.GroupLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author SQ
 */
public class GraphTabPanel extends JPanel {
    /** Array of chart panel */
    protected ChartPanel[] graphPanel;
    
    public JTabbedPane jTabbedPane1;
    // Hold current position of the graph crosshair    
    private double currentPosition = 0d;

    /**
     * Setting a graph tab panel
     * @param graphPanelNames Array containing name of all graph.
     * Total graph tab will be created to be equal to the length of the
     * array.  If the element is blank the name for that 
     * graph will just be empty.
     */
    public GraphTabPanel(String[] graphPanelNames) {
        this.graphPanel = new ChartPanel[graphPanelNames.length];
        initComponents(graphPanelNames);
    }

    public void addChartProgressListener(ChartProgressListener listener) {
        for (int i = 0; i < graphPanel.length; i++) {
            graphPanel[i].getChart().addProgressListener(listener);
        }
    }

    public void addMarker(Marker marker) {
        for (int i = 0; i < this.graphPanel.length; i++) {
            XYPlot plot = (XYPlot) this.graphPanel[i].getChart().getPlot();
            plot.addDomainMarker(marker, Layer.BACKGROUND);
        }
    }

    public void clearMarkers() {
        for (int i = 0; i < this.graphPanel.length; i++) {
            XYPlot plot = (XYPlot) this.graphPanel[i].getChart().getPlot();
            plot.clearDomainMarkers();
        }
    }

    protected ChartPanel createChartPanel(XYDataset dataSet, double xrange, double yrange) {
        if (dataSet == null) {
            dataSet = new DefaultXYDataset();
        }
        NumberAxis yAxis = new NumberAxis("Value");
        NumberAxis xAxis = new NumberAxis("Frame");
        // Set x axis range
        xAxis.setAutoRange(false);
        xAxis.setLowerBound(0.0);
        xAxis.setUpperBound(xrange);
        yAxis.setAutoRange(true);
        yAxis.setLowerBound(0.0);
        yAxis.setUpperBound(yrange);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        // Remove point marker
        renderer.setShapesVisible(false);
        XYPlot plot = new XYPlot(dataSet, xAxis, yAxis, renderer);
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(false);
        plot.setRangeCrosshairLockedOnData(false);
        JFreeChart jfreechart = new JFreeChart(plot);
        // Move legend to the right
        jfreechart.getLegend().setPosition(RectangleEdge.RIGHT);
        ChartPanel panel = new ChartPanel(jfreechart);
        // Prevent zooming
        panel.setDomainZoomable(false);
        panel.setRangeZoomable(false);

        return panel;
    }

    protected XYDataset getDataSet(ChartPanel chartPanel) {
        XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
        return plot.getDataset();
    }

    /**
     * Over write this method if ChartPanel are to be created differently
     * otherwise it will create a chart of range 0 to 500 in both x and y
     */
    protected void createAllCharts(){
        for (int i = 0; i < graphPanel.length; i++) {
            graphPanel[i] = createChartPanel(null, 500, 100);
        }
    }
    
    protected void initComponents(String[] graphName) {
        this.jTabbedPane1 = new JTabbedPane();
        this.jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.TOP);

        createAllCharts();
        
        // Add all panels in different tab
        for (int i = 0; i < this.graphPanel.length; i++) {
            this.jTabbedPane1.addTab(graphName[i], this.graphPanel[i]);
        }

        GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, 100,
            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
            Integer.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, 100,
            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }

    public void setFixVerticalSize(int size){
        GroupLayout layout = (GroupLayout) getLayout();
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
            size,
            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }

    public void setCurrentCrossHairPosition(double xPosition) {
        xPosition = Math.max(0.0, xPosition);
        for (int i = 0; i < graphPanel.length; i++) {
            XYPlot plot = (XYPlot) graphPanel[i].getChart().getPlot();

            // Check direction of movement if we need to shift the graph or not
            double dif = xPosition - this.currentPosition;
            if (dif > 0) {
                plot.setDomainCrosshairValue(xPosition);
                Range range = plot.getDomainAxis().getRange();
                double length = range.getLength();
                if (xPosition > length * 0.75 + range.getLowerBound()) {
                    plot.getDomainAxis().setRangeAboutValue(xPosition, length);
                }
            } else if (dif < 0) {
                // Update position
                plot.setDomainCrosshairValue(xPosition);
                Range range = plot.getDomainAxis().getRange();
                double length = range.getLength();
                if (xPosition < length * 0.25 + range.getLowerBound()) {
                    // Shift graph to have a crosshair at center
                    plot.getDomainAxis().setRangeAboutValue(xPosition, length);
                }
            }
        }

        // Set new current position
        currentPosition = xPosition;
    }

    protected void setDataSet(XYDataset dataset, ChartPanel chartPanel) {
        XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
        plot.setDataset(dataset);
    }

}
