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
 * TwoSeriesXYDataSet.java
 *
 * Created on October 10, 2007, 11:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package buseylab.eyetrackercalibrator.framemanaging;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 *
 * @author ruj
 */
public abstract class TwoSeriesXYDataSet extends SyncXYDataSet{
    
    protected FrameManager frameInfoManager = null;
    
    /** Creates a new instance of TwoSeriesXYDataSet */
    public TwoSeriesXYDataSet(FrameManager frameInfoManager) {
        this.frameInfoManager = frameInfoManager;
    }
    
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }
    
    public int getItemCount(int series) {
        return frameInfoManager.getTotalFrames();
    }
    
    /**
     * This X represent a frame of pupil
     */
    public Number getX(int series, int item) {
        return new Integer(item);
    }
    
    /**
     * This X represent a frame of pupil
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
    
    /**
     * Return (x,y) coor of the pupil depending on series value.  Implementation
     * should take in account the offset value
     * @param series 0 for X value of pupil 1 for Y value of pupil
     * @return -1 When information is not avilable
     */
    public abstract double getYValue(int series, int item);
    
    public int getSeriesCount() {
        return 2;
    }
       
    public Comparable getSeriesKey(int series) {
        switch(series){
            case 0: // This is X value of pupil
                return getFirst_series_key();
                
            case 1: // This is Y value pf pupil
                return getSecond_series_key();
                
            default:
                return null;
        }
    }
    
    public int indexOf(Comparable seriesKey) {
        if(seriesKey.equals(getFirst_series_key())){
            return 0;
        }else if(seriesKey.equals(getSecond_series_key())){
            return 1;
        }
        
        return -1; // By default not found
    }
    
    public void addChangeListener(DatasetChangeListener listener) {
    }
    
    public void removeChangeListener(DatasetChangeListener listener) {
    }
    
    public abstract DatasetGroup getGroup();
    
    public void setGroup(DatasetGroup group) {
        // Does nothing
    }
    
    protected abstract String getFirst_series_key();

    protected abstract String getSecond_series_key();       
}
