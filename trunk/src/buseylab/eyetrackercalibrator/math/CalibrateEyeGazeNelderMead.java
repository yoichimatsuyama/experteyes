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
 * CalibrateEyeGaze.java
 *
 * Created on October 24, 2007, 9:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package buseylab.eyetrackercalibrator.math;

import java.awt.geom.Point2D;
import java.util.Arrays;
import org.spaceroots.mantissa.optimization.ConvergenceChecker;
import org.spaceroots.mantissa.optimization.CostException;
import org.spaceroots.mantissa.optimization.CostFunction;
import org.spaceroots.mantissa.optimization.NelderMead;
import org.spaceroots.mantissa.optimization.NoConvergenceException;
import org.spaceroots.mantissa.optimization.PointCostPair;

/**
 * This class finds constants to be used to compute eye gaze point on screen.
 * There are 12 constants to be found as shown in equations below
 *
 * S_x = c_0 + c_1 x + c_2 y + c_3 x y + c_4 x x + c_5 y y
 * S_y = c_6 + c_7 x + c_8 y + c_9 x y + c_10 x x + c_11 y y
 *
 * S_x is x position of eyegaze point on screen
 * S_y is y position of eyegaze point on screen
 *
 * The class uses Nelder-Mead Search for a Minimum to locate the constant
 *
 * @author ruj
 */
public class CalibrateEyeGazeNelderMead implements CalibrateEyeGaze{
    CalibrateEyeGazeListener listener = null;
    double[][] results = new double[2][];
    
    final static int MAX_EVALUTATION = 10000;
    /** Total number optimization restart (including the first time)*/
    final static int TOTAL_RESTART = 10;
    
    /** Creates a new instance of CalibrateEyeGaze */
    public CalibrateEyeGazeNelderMead() {
    }
    
    public void setCalibrateEyeGazeListener(CalibrateEyeGazeListener listener){
        this.listener = listener;
    }
    
    /**
     * Eye vector and calibrate point must be the same size. This operation
     * is not thread safe.
     * @param eyeVector
     * @param calibratePoints
     * @return 2 array of 6 constants to be used for computing eye gaze points
     */
    public double[][] calibrate(
            Point2D[] eyeVector,
            Point2D[] calibratePoints){
        
        // Init results
        results[0] = new double[6];
        results[1] = results[0];
        Arrays.fill(results[0],0,results[0].length,0);
        
        // Put all in array for fast access
        double[] eye_x = new double[eyeVector.length];
        double[] eye_y = new double[eyeVector.length];
        double[] calibrate_x = new double[eyeVector.length];
        double[] calibrate_y = new double[eyeVector.length];
        
        for (int i = 0; i < calibratePoints.length; i++) {
            eye_x[i] = eyeVector[i].getX();
            eye_y[i] = eyeVector[i].getY();
            calibrate_x[i] = calibratePoints[i].getX();
            calibrate_y[i] = calibratePoints[i].getY();
        }
        
        // Prepare for NelderMead
        NelderMead nelderMead = new NelderMead();
        
        // Create checker for termination
        ConvergenceChecker checker1 =  new ConvergenceChecker() {
            public boolean converged(PointCostPair[] pointCostPair) {
                if(listener != null){
                    results[0] = pointCostPair[0].point;
                    listener.update(results, pointCostPair[0].cost);
                }
                
                if(pointCostPair[0].cost < 0.0001){
                    System.out.println("Try " + pointCostPair[0].cost);
                    return true;
                }
                return false;
            }
        };
        
        // Create checker for termination
        ConvergenceChecker checker2 =  new ConvergenceChecker() {
            public boolean converged(PointCostPair[] pointCostPair) {
                if(listener != null){
                    results[1] = pointCostPair[0].point;
                    listener.update(results, pointCostPair[0].cost);
                }
                
                if(pointCostPair[0].cost < 0.0001){
                    System.out.println("Try " + pointCostPair[0].cost);
                    return true;
                }
                return false;
            }
        };
        
        PointCostPair pointCostPair = null;
        
        TwoVariableCostFunction costF =
                new TwoVariableCostFunction(eye_x, eye_y, calibrate_x);
        
        // Create starting vector
        /** @todo change the way we init simplex vector for minimization */
        double[] v1 = {256,256,256,256,256,256};
        double[] v2 = {-256,-256,-256,-256,-256,-256};
        try {
            // Try minimization with
            pointCostPair = nelderMead.minimizes(
                    costF, MAX_EVALUTATION, checker1, v1, v2, TOTAL_RESTART, null);
        } catch (NoConvergenceException ex) {
            ex.printStackTrace();
        } catch (CostException ex) {
            ex.printStackTrace();
        }
        
        if(pointCostPair != null){
            results[0] = pointCostPair.point;
            // Clear old result
            pointCostPair = null;
        }
        
        /**
         * Signal completion of X coefficients
         */
        if(listener != null){
            listener.completeStage(1);
        }
        
        costF = new TwoVariableCostFunction(eye_x, eye_y, calibrate_y);
        
        try {
            // Try minimization with
            pointCostPair = nelderMead.minimizes(
                    costF, MAX_EVALUTATION, checker2, v1, v2, TOTAL_RESTART, null);
        } catch (NoConvergenceException ex) {
            ex.printStackTrace();
        } catch (CostException ex) {
            ex.printStackTrace();
        }
        
        if(pointCostPair != null){
            results[1] = pointCostPair.point;
            // Clear old result
            pointCostPair = null;
        }
        
        /**
         * Signal completion of Y coefficients
         */
        if(listener != null){
            listener.completeStage(2);
        }
        
        return results;
    }
    
    public int getTotalStages(){
        return 2;
    }
    
    public long getTotalProgress(){
        return (long)TOTAL_RESTART * (long)MAX_EVALUTATION;
    }
    
    private class TwoVariableCostFunction implements CostFunction{
        private double[] x;
        private double[] y;
        private double[] v;
        public TwoVariableCostFunction(
                double[] in_x, double[] in_y, double[] in_v){
            x = new double[in_x.length];
            y = new double[in_y.length];
            v = new double[in_v.length];
            
            System.arraycopy(in_x,0,x,0,x.length);
            System.arraycopy(in_y,0,y,0,x.length);
            System.arraycopy(in_v,0,v,0,x.length);
        }
        
        public double cost(double[] d) throws CostException {
            double totalcost = 0d;
            double cost = 0d;
            for (int i = 0; i < v.length; i++) {
                cost = v[i] - Computation.computeTwoVariablePolynomial(
                        x[i],y[i],d);
                totalcost += (cost*cost);
            }
            
            return totalcost;
        }
    }
}
