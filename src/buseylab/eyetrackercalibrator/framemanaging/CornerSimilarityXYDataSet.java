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
 * CornerSimilarityXYDataSet.java
 *
 * Created on October 10, 2007, 11:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package buseylab.eyetrackercalibrator.framemanaging;

import java.util.Arrays;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 *
 * @author ruj
 */
public class CornerSimilarityXYDataSet extends SyncXYDataSet{
    FrameManager frameInfoManager = null;
    private double[][] buffer;
    private int firstItem = 0;
    private int bufferSize = 0;
    
    /**
     * Creates a new instance of CornerSimilarityXYDataSet
     * it is necessary that the FrameManager returns EyeViewFrameInfo
     * @param  frameInfoManager FrameManager which returns ScreenViewFrameInfo when
     *         getFrameInfo method is called
     * @param  Set the buffer size to greater to total domain you want
     *         to display
     */
    public CornerSimilarityXYDataSet(FrameManager frameInfoManager, int bufferSize) {
        this.frameInfoManager = frameInfoManager;
        
        buffer = new double[4][];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = new double[bufferSize];
            // Filled with default value
            Arrays.fill(buffer[i],-1);
        }
        
        this.bufferSize = bufferSize;
    }
    
    /**
     * Return (x,y) coor of the similarity depending on series value
     * @item if frame number starting from 1
     * @return -1 When information is not avilable
     */
    public double getYValue(int series, int item) {
        if(item < firstItem || item >= firstItem + bufferSize){
            // buffer
            rebuffer(item+1);
        }
        
        if(item <= 0){
            return -1;
        }else{
            int pos = (item+1) % bufferSize;
            if(buffer[series][pos] >= 0){
                return buffer[series][pos];
            }else{
                // Try reloading information
                rebuffer(item+1);
                return buffer[series][pos];
            }
        }
    }
    
    // Load information to the buffer to cover the pos point
    // Assume this.firstItem is always greater than 0 and pos starts from 1
    private void rebuffer(int pos){
        int arrayPos = 0; // Real buffer location
        int startPos = 0; // The starting item to load
        int changes = 0; // Amount to load
        
        // Setting filling parameters ( arrayPos, startPos )
        if(pos < firstItem){
            // Find amount to load in and cap it at loading everything
            changes = Math.min(firstItem - pos, bufferSize);
            
            arrayPos = pos % bufferSize;
            
            startPos = pos;
            
            // Move buffer position
            firstItem = pos;
        }else if(pos >= firstItem + bufferSize){
            // Find amount to load in and cap it at loading everything
            changes = Math.min(pos - bufferSize - firstItem + 1, bufferSize);
            
            arrayPos = (pos - changes + 1) % bufferSize;
            
            // Frind start filling point
            startPos = pos - changes + 1;
            
            // Move buffer position
            firstItem = firstItem + changes;
        }else if(pos <= this.frameSynchronizor.getTotalFrame()){
            // Just reload the information at the position if we have it
            changes = 1;
            arrayPos = pos % bufferSize;
            startPos = pos;
        }
        
        // Fill in the gap
        for (int i = 0; i < changes; i++) {
            // Get info
            ScreenViewFrameInfo info =
                    (ScreenViewFrameInfo) frameInfoManager.getFrameInfo(
                    this.frameSynchronizor.getSceneFrame(startPos+i));
            if(info == null || info.similarities == null){
                // Set information to unavailable
                for (int j = 0; j < buffer.length; j++) {
                    buffer[j][arrayPos] = -1;
                }
            }else{
                // Copy in information
                for (int j = 0; j < buffer.length; j++) {
                    buffer[j][arrayPos] = info.similarities[j];
                }
            }
            // Advance the position
            arrayPos = (arrayPos + 1) % bufferSize;
        }
    }
    
    public DatasetGroup getGroup() {
        return new DatasetGroup("Corner Similarity");
    }
    
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }
    
    public int getItemCount(int i) {
        return frameInfoManager.getTotalFrames();
    }
    
    public Number getX(int series, int item) {
        return new Integer(item+1);
    }
    
    public double getXValue(int series, int item) {
        return (double) (item+1);
    }
    
    public Number getY(int series, int item) {
        return new Double(getYValue(series,item));
    }
    
    /**
     * We have 4 series for each corner
     */
    public int getSeriesCount() {
        return 4;
    }
    
    public Comparable getSeriesKey(int series) {
        switch(series){
            case ScreenViewFrameInfo.BOTTOMLEFT:
                return "Bottom left";
                
            case ScreenViewFrameInfo.BOTTOMRIGHT:
                return "Bottom right";
                
            case ScreenViewFrameInfo.TOPLEFT:
                return "Top left";
                
            case ScreenViewFrameInfo.TOPRIGHT:
                return "Top right";
                
            default:
                return null;
        }
    }
    
    public int indexOf(Comparable seriesKey) {
        if(seriesKey.equals("Bottom left")){
            return ScreenViewFrameInfo.BOTTOMLEFT;
        }else if(seriesKey.equals("Bottom right")){
            return ScreenViewFrameInfo.BOTTOMRIGHT;
        }else if(seriesKey.equals("Top left")){
            return ScreenViewFrameInfo.TOPLEFT;
        }else if(seriesKey.equals("Top right")){
            return ScreenViewFrameInfo.TOPRIGHT;
        }
        
        return -1; // By default not found
    }
    
    public void addChangeListener(DatasetChangeListener datasetChangeListener) {
    }
    
    public void removeChangeListener(DatasetChangeListener datasetChangeListener) {
    }
    
    public void setGroup(DatasetGroup datasetGroup) {
    }
    
}
