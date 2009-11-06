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
 * IlluminationXYDataSet.java
 *
 * Created on November 29, 2007, 11:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eyetrackercalibrator.framemanaging;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 *
 * @author ruj
 */
public class IlluminationXYDataSet extends SyncXYDataSet{
    InformationDatabase informationDatabase = null;
    protected int offset = 0;
    private int lastItem = 0;
    
    /** Creates a new instance of IlluminationXYDataSet
     * @param infoDatabase 
     */
    public IlluminationXYDataSet(InformationDatabase infoDatabase) {
        this.informationDatabase = infoDatabase;
    }
    
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }
    
    /* Return the last item */
    public int getItemCount(int i) {
        return lastItem;
    }
    
    /**
     * This X represent a frame 
     */
    public Number getX(int series, int item) {
        return new Integer(item);
    }
    
    /**
     * This X represent a frame 
     */
    public double getXValue(int series, int item) {
        return (double) (item);
    }
    
    /**
     * Return (x,y) coor of the pupil depending on series value
     * @param series 0 for X value of pupil 1 for Y value of pupil
     */
    public Number getY(int series, int item) {
        return new Double(getYValue(series,item));
    }
    
    public double getYValue(int series, int item) {
        double result = 0d;
        
        // Get info
        Double info = 
                informationDatabase.getInfo(item + offset);
        if(info != null){
            if(series == 0){
                    result = info;
            }
        }
        return result;
    }
    
    public int getSeriesCount() {
        return 1;
    }
    
    public Comparable getSeriesKey(int series) {
        if(series == 0){
            return "Illumination";
        }else{
            return null;
        }
    }
    
    public int indexOf(Comparable seriesKey) {
        if(seriesKey.equals(getSeriesKey(0))){
            return 0;
        }else{
            return -1;
        }
    }
    
    public void addChangeListener(DatasetChangeListener datasetChangeListener) {
    }
    
    public void removeChangeListener(DatasetChangeListener datasetChangeListener) {
    }
    
    public DatasetGroup getGroup() {
        return new DatasetGroup("Illumination data group");
    }
    
    public void setGroup(DatasetGroup datasetGroup) {
    }
    
    public int getLastItem() {
        return lastItem;
    }

    public void setLastItem(int lastItem) {
        this.lastItem = lastItem + 1;
    }
}
