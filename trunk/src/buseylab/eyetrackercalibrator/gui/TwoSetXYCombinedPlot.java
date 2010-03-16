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
 * TwoSetXYCombinedPlot.java
 *
 * Created on October 3, 2007, 9:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package buseylab.eyetrackercalibrator.gui;

import java.util.List;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author ruj
 */
public class TwoSetXYCombinedPlot {
    
    JFreeChart chart = null;
    XYPlot upperPlot = null;
    XYPlot lowerPlot = null;
    
    /** Creates a new instance of TwoSetXYCombinedPlot */
    public TwoSetXYCombinedPlot(XYDataset upperDataSet, XYDataset lowerDataSet) {
        // Create chart
        this.chart = createChart(upperDataSet,lowerDataSet);
    }
    
    private JFreeChart createChart(XYDataset upperDataSet, XYDataset lowerDataSet){
        // Sanity check
        if(upperDataSet == null){
            upperDataSet = new DefaultXYDataset();
        }
        if(lowerDataSet == null){
            lowerDataSet = new DefaultXYDataset();
        }
        // Set up upper chart plot
        NumberAxis yAxis = new NumberAxis("Value");
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        // No x Axis since it's going to be managed by combinedPlot
        XYPlot upperPlot = new XYPlot(upperDataSet,null,yAxis,renderer);
        
        // Set up lower chart plot
        yAxis = new NumberAxis("Value");
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        renderer = new XYLineAndShapeRenderer();
        // No x Axis since it's going to be managed by combinedPlot
        XYPlot lowerPlot = new XYPlot(lowerDataSet,null,yAxis,renderer);
        
        // Combine two plot and provide joint X axis
        NumberAxis xAxis = new NumberAxis("Frame");
        // Config xAxis
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(xAxis);
        // Set up the plot
        // Careful about an order here since it will effect algorithm in 
        // setDataSet, getUpperDataSet, getLowerDataSet method
        combinedPlot.add(upperPlot);
        combinedPlot.add(lowerPlot);
        
        JFreeChart jfreechart = new JFreeChart(combinedPlot); // To be implement
        return jfreechart;
    }
    
    public JPanel getPanel(){
        if(this.chart != null){
            return new ChartPanel(chart);
        }else{
            return null;
        }
    }
    
    private XYDataset getUpperDataSet() {
        CombinedDomainXYPlot plot = (CombinedDomainXYPlot) chart.getPlot();
        List<XYPlot> plotList = (List<XYPlot>) plot.getSubplots();
        return plotList.get(0).getDataset();
    }    
    
    public XYDataset getLowerDataSet() {
        CombinedDomainXYPlot plot = (CombinedDomainXYPlot) chart.getPlot();
        List<XYPlot> plotList = (List<XYPlot>) plot.getSubplots();
        return plotList.get(1).getDataset();
    }
    
    public void setDataSet(XYDataset upperDataSet, XYDataset lowerDataSet) {
        CombinedDomainXYPlot plot = (CombinedDomainXYPlot) chart.getPlot();
        List<XYPlot> plotList = (List<XYPlot>) plot.getSubplots();
        plotList.get(0).setDataset(upperDataSet);
        plotList.get(1).setDataset(lowerDataSet);
    }
}
