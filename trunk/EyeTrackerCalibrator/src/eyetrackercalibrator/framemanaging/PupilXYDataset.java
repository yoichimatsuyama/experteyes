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
 * PupilXYDataset.java
 *
 * Created on October 8, 2007, 1:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eyetrackercalibrator.framemanaging;

import org.jfree.data.general.DatasetGroup;

/**
 *
 * @author rakavipa
 */
public class PupilXYDataset extends TwoSeriesXYDataSet{
       
    /** 
     * Creates a new instance of PupilXYDataset 
     * it is necessary that the FrameManager returns EyeViewFrameInfo
     * @param  frameInfoManager FrameManager which returns EyeViewFrameInfo when
     *         getFrameInfo method is called
     */
    public PupilXYDataset(FrameManager frameInfoManager) {
        super(frameInfoManager);
    }
    
    /**
     * Return (x,y) coor of the pupil depending on series value
     * @param series 0 for X value of pupil 1 for Y value of pupil
     * @return -1 When information is not avilable
     */
    public double getYValue(int series, int item) {
        double result = -1d;
        
        // Get info
        EyeViewFrameInfo info =
                (EyeViewFrameInfo) frameInfoManager.getFrameInfo(
                this.frameSynchronizor.getEyeFrame(item));
        if(info != null){
            switch(series){
                case 0: // This is X value of pupil
                    result = info.getPupilX();
                    break;
                case 1: // This is Y value pf pupil
                    result = info.getPupilY();
            }
        }
        return result;
    }

    public DatasetGroup getGroup() {
        return new DatasetGroup("Pupil coor data group");
    }

    protected String getFirst_series_key() {
        return "x";
    }

    protected String getSecond_series_key() {
        return "y";
    }
    
}
