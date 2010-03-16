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
package buseylab.eyetrackercalibrator.math;

import buseylab.eyetrackercalibrator.framemanaging.InformationDatabase;

/**
 *
 * @author eeglab
 */
public class EstimateTrialMarking {

    private double lowGroup = 0d;
    private double middleGroup = 0d;
    private double highGroup = 0d;

    /**
     * 
     * @param informationDatabase Cannot be null
     */
    public EstimateTrialMarking(InformationDatabase informationDatabase) {
        this.informationDatabase = informationDatabase;
    }
    private InformationDatabase informationDatabase;

    /**
     * Return 2 mean value of two groups.  The estimation user k-Mean with 2 
     * kernal.  The first one starts at the smallest value in the database from
     * the given range (start to end inclusive).  The second one starts at the
     * average between the largest value and the smallest value.
     * @param start Starting position in the information database
     * @param end Ending position in the information database
     * @return return the center of two resulting kernal.  The result is sorted
     * in ascending order (low, high)
     */
    public double[] estimateGroup(
            int start, int end) {
        double temp;

        lowGroup = Math.abs(getDiff(start));
        highGroup = lowGroup;
        double oldLowGroup = lowGroup;
//        double oldMiddleGroup;
        double oldHighGroup = highGroup;

        // Get the core for low group from min and high group from max
        for (int i = start + 1; i <= end; i++) {
            temp = Math.abs(getDiff(i));
            lowGroup = Math.min(lowGroup, temp);
            highGroup = Math.max(highGroup, temp);
        }

        highGroup = (lowGroup + highGroup) / 2;
//        middleGroup = (lowGroup + highGroup) / 2;
        
        double changes;
        int round = 0;
        do {
            oldLowGroup = lowGroup;
//            oldMiddleGroup = middleGroup;
            oldHighGroup = highGroup;

            // Classify 
            double dLow, dHigh, dMid = 0;
            double sumLow = 0d;
//            double sumMid = 0d;
            double sumHign = 0d;
            double totalLow = 0d;
            double totalMid = 0d;
            double totalHigh = 0d;
            for (int i = start; i <= end; i++) {
                temp = Math.abs(getDiff(i));
                dLow = Math.abs(temp - lowGroup);
//                dMid = Math.abs(temp - middleGroup);
                dHigh = Math.abs(temp - highGroup);
                if (dLow <= dHigh){//dMid) {
                    // If equal goes to low by default
                    sumLow += temp;
                    totalLow++;
 //               } else if(dMid <= dHigh){
 //                   sumMid += temp;
 //                   totalMid++;
                }else{
                    sumHign += temp;
                    totalHigh++;
                }
            }

            // Compute new mean of each group
            lowGroup = sumLow / totalLow;
//            middleGroup = sumMid / totalMid;
            highGroup = sumHign / totalHigh;

            // compute changes
            dLow = oldLowGroup - lowGroup;
//            dMid = oldMiddleGroup - middleGroup;
            dHigh = oldHighGroup - highGroup;
            changes = Math.max(dLow * dLow, Math.max(dMid * dMid,dHigh * dHigh));

            round++;
        } while (changes > 0.001 && round < 10000);

        double[] result = new double[3];
        result[0] = lowGroup;
//        result[1] = middleGroup;
        result[2] = highGroup;
        return result;
    }

    /**
     * Return v(pos) - v(pos-1) 
     * @param pos
     * @return 0 if v(pos) or v(pos - 1) is not available. Otherwise returning
     * v(pos) - v(pos-1)
     */
    public double getDiff(int pos) {
        Double c = informationDatabase.getInfo(pos);
        Double p = informationDatabase.getInfo(pos - 1);
        if (c != null && p != null) {
            return c - p;
        } else {
            return 0d;
        }
    }

    /**
     * @param pos
     * @return true when v(pos) is closer to high group than low group mean
     */
    public boolean isHighGroup(int pos) {
        double v = Math.abs(getDiff(pos));
        return (/*Math.abs(v - lowGroup) > Math.abs(v - middleGroup) ||*/ Math.abs(v - lowGroup) > Math.abs(v - highGroup));
    }
}
