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
 * CornerCorrection.java
 *
 * Created on November 6, 2007, 9:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package buseylab.eyetrackercalibrator.math;

import buseylab.eyetrackercalibrator.framemanaging.FrameManager;
import buseylab.eyetrackercalibrator.framemanaging.ScreenViewFrameInfo;
import buseylab.eyetrackercalibrator.gui.ProgressJDialog;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.LinkedList;
import org.spaceroots.mantissa.optimization.CostException;
import org.spaceroots.mantissa.optimization.CostFunction;

/**
 * This is a tool to help correct the
 *
 * @author ruj
 */
public class CornerCorrection {
    
    /** Creates a new instance of CornerCorrection */
    public CornerCorrection() {
    }
    
    public class ErrorFrames{
        int from;
        int to;
    }
    
    /**
     * The class is trying to correct the misplaced corner
     * it will modify information in supplied FrameManager
     * It needs reference frame, which is a frame with correct
     * placement of a corner.  The correction will start from the reference
     * frame position onward
     *
     * @return LinkedList of error frames detected
     */
    static public LinkedList<ErrorFrames> identifyBadCorners(int referenceFrame,
            FrameManager screenViewFrameManager, double threshold,
            ProgressJDialog progressDialog){
        
        LinkedList<ErrorFrames> errorList = new LinkedList<ErrorFrames>();
        
        // Load information from reference frame
        ScreenViewFrameInfo info =
                (ScreenViewFrameInfo) screenViewFrameManager.getFrameInfo(referenceFrame);
        
        if(info == null){
            System.err.println("No reference frame information");
            return errorList;
        }
        
        // Set reference corners
        Point[] references = info.getCorners();
        
        if(references == null){
            System.err.println("No corner information in reference frame");
            return errorList;
        }
        
        // Get total frames
        int totalFrames = screenViewFrameManager.getTotalFrames();
        
        // Variables to be used in the loop
        Point[] corners = null;
        Point[] correction = new Point[4];
        double[] distance = new double[4];
        double[] deviation = new double[4];
        
        // Proceed through each frame and find bad corners
        for (int i = referenceFrame + 1; i < totalFrames; i++) {
            // Clear correction
            Arrays.fill(correction,null);
            
            // Load new frame information
            info = (ScreenViewFrameInfo) screenViewFrameManager.getFrameInfo(i);
            
            if(info != null){
                corners = info.getCorners();
                
                // Compute distance of each point from reference
                for (int j = 0; j < corners.length; j++) {
                    distance[j] = references[j].distance(corners[j]);
                }
                
                // Compute the diviation of each corner distance
                double mean = (distance[0] + distance[1] + distance[2] + distance[3])/4d;
                for (int j = 0; j < corners.length; j++) {
                    deviation[j] = mean - distance[j];
                    deviation[j] = deviation[j];// * deviation[j];
                    
                    // If deviation is bigger than threshold then mark it as bad
                    if(deviation[j] > threshold){
                        correction[j] = corners[j];
                    }else{
                        correction[j] = null;
                    }
                }
                
                if(info.similarities != null){
                    for (int j = 0; j < deviation.length; j++) {
                        info.similarities[j] = deviation[j];
                    }
                }
                
                // Set correction
                info.setCorrectedCorners(
                        correction[ScreenViewFrameInfo.TOPLEFT],
                        correction[ScreenViewFrameInfo.TOPRIGHT],
                        correction[ScreenViewFrameInfo.BOTTOMLEFT],
                        correction[ScreenViewFrameInfo.BOTTOMRIGHT]);
                
                // Update information
                screenViewFrameManager.setFrameInfo(i,info);
                
                // Set new reference
                references = corners;
            }
            
            if(progressDialog != null){
                progressDialog.setProgrss(i);
            }
        }
        
        if(progressDialog != null){
            progressDialog.setProgrss(totalFrames);
        }
        
        return null;
    }
    
    public enum Corner {TOPLEFT,TOPRIGHT,BOTTOMLEFT,BOTTOMRIGHT};
    
    
    /**
     * Correct corner using reflexive approach (Not working)
     */
    public void correctCorner(int referenceFrame,
            FrameManager screenViewFrameManager, Corner corner){
        ScreenViewFrameInfo info =
                (ScreenViewFrameInfo) screenViewFrameManager.getFrameInfo(referenceFrame);
        
        if(info == null){
            System.err.println("No reference frame information");
            return;
        }
        
        // Set reference corners
        Point2D topLeft = info.getTopLeft();
        Point2D topRight = info.getTopRight();
        Point2D bottomLeft = info.getBottomLeft();
        Point2D bottomRight = info.getBottomRight();
        
        Point2D p = correctCornerRefletive(
                topLeft, topRight, bottomLeft, bottomRight);
        // Set correction
        info.setCorrectedCorners(
                null,
                null,
                p,
                null);
        
        // Update information
        screenViewFrameManager.setFrameInfo(referenceFrame,info);
    }
    
    public void correctCornerTranslate(int referenceFrame,
            FrameManager screenViewFrameManager, Corner corner){
        int w = 512;
        int h = 512;
        int tw = -w/2;
        int th = -h/2;
        
        ScreenViewFrameInfo info =
                (ScreenViewFrameInfo) screenViewFrameManager.getFrameInfo(referenceFrame);
        
        if(info == null){
            System.err.println("No reference frame information");
            return;
        }
        
        // Set reference corners
        //Point[] references = info.getCorners();
        Point2D topLeft = info.getTopLeft();
//new Point(references[ScreenViewFrameInfo.TOPLEFT].x,references[ScreenViewFrameInfo.TOPLEFT].y);
        //        h - references[ScreenViewFrameInfo.TOPLEFT].y);
        Point2D topRight = info.getTopRight();
//new Point(references[ScreenViewFrameInfo.TOPRIGHT].x,references[ScreenViewFrameInfo.TOPRIGHT].y);
        //        h - references[ScreenViewFrameInfo.TOPRIGHT].y);
        Point2D bottomLeft = info.getBottomLeft();//new Point(references[ScreenViewFrameInfo.BOTTOMLEFT].x,references[ScreenViewFrameInfo.BOTTOMLEFT].y);
        //        h- references[ScreenViewFrameInfo.BOTTOMLEFT].y);
        Point2D bottomRight = info.getBottomRight();//new Point(references[ScreenViewFrameInfo.BOTTOMRIGHT].x,references[ScreenViewFrameInfo.BOTTOMRIGHT].y);
        //        h- references[ScreenViewFrameInfo.BOTTOMRIGHT].y);
        //BOTTOMLeft.translate(tw,th);
        //BOTTOMRight.translate(tw,th);
        //bottomLeft.translate(tw,th);
        //bottomRight.translate(tw,th);
        
        Point2D p = correctCorner(
                topLeft, topRight, bottomLeft, bottomRight, 1680, 1050);
        //p.translate(-tw,-th);
        //p.setLocation(p.x,h-p.y);
        // Set correction
        info.setCorrectedCorners(
                null,
                null,
                p,
                null);
        
        // Update information
        screenViewFrameManager.setFrameInfo(referenceFrame,info);
    }
    
    /**
     *  This method tries to correct a SINGLE missing corner.  So there can
     *  be ONLY ONE corner missing.  Don't pass in more than one null parameter
     *  or the method will throw null point exception
     *
     *  The approach used is assume reflectveness of the corner
     *
     *  @param topLeft Top left corner. null will signal the corner is missing
     *  @param topRight Top right corner. null will signal the corner is missing
     *  @param bottomLeft bottom left corner. null will signal the corner is missing
     *  @param bottomRight bottom right corner. null will signal the corner is missing
     *  @return coordinate of the missing corner. null if fail
     */
    private Point2D correctCornerRefletive(
            Point2D topLeft, Point2D topRight, Point2D bottomLeft, Point2D bottomRight){
        // Lets just try to correct bottom left corner first
        
        // Find midpoint between BOTTOMLeft and bottom right
        
        Point2D.Double mid = new Point2D.Double(
                (topLeft.getX() + bottomRight.getX())/2d,
                (topLeft.getY() + bottomRight.getY())/2d);
        
        double dx = topRight.getX() - mid.x;
        double dy = topRight.getY() - mid.y;
        
        // Reflect the top right to bottom left
        Point2D.Double reflect = new Point2D.Double(
                mid.x - dx, mid.y - dy);
        
        Point result = new Point();
        
        result.setLocation(reflect);
        
        return result;
    }
    
    /**
     *  This method tries to correct a SINGLE missing corner.  So there can
     *  be ONLY ONE corner missing.  Don't pass in more than one null parameter
     *  or the method will throw null point exception
     *
     *  This is trying to solve it with view angle approach
     *
     *  @param topLeft Top left corner. null will signal the corner is missing
     *  @param topRight Top right corner. null will signal the corner is missing
     *  @param bottomLeft bottom left corner. null will signal the corner is missing
     *  @param bottomRight bottom right corner. null will signal the corner is missing
     *  @return coordinate of the missing corner. null if fail
     */
    private Point2D correctCorner(
            Point2D topLeft, Point2D topRight, Point2D bottomLeft, Point2D bottomRight,
            double w, double h){
        // Lets just try to correct bottom left corner first
        
        // Find midpoint between BOTTOMLeft and bottom right
        
        Point2D.Double mid = new Point2D.Double(
                (topLeft.getX() + bottomRight.getX())/2d,
                (topLeft.getY() + bottomRight.getY())/2d);
        
        double dx = topRight.getX() - mid.x;
        double dy = topRight.getY() - mid.y;
        
        // Reflect the top right to bottom left
        Point2D.Double reflect = new Point2D.Double(
                mid.x - dx, mid.y - dy);
        
        Point2D result = new Point2D.Double();
        
        result.setLocation(reflect);
        
        // Fitting P_z
        // Prepare for NelderMead
//        NelderMead nelderMead = new NelderMead();
//
//        // Create checker for termination
//        ConvergenceChecker checker =  new ConvergenceChecker() {
//            public boolean converged(PointCostPair[] pointCostPair) {
//                if(pointCostPair[0].cost < 0.001){
//                    System.out.println("Try " + pointCostPair[0].cost);
//                    return true;
//                }
//                return false;
//            }
//        };
//
//        CornerCostFunction costF = new CornerCostFunction(
//                BOTTOMLeft,BOTTOMRight,bottomRight, w, h);
//
//        PointCostPair pointCostPair = null;
//
////        double cost = Double.POSITIVE_INFINITY;
////        double P_z2 = 0;
////        for (int i = 0; i > 1000; i--) {
////            double newCost = costF.cost(i);
////            if(newCost < cost){
////                cost = newCost;
////                P_z2 = i;
////            }
////        }
//        double[] v1 = {0};
//        double[] v2 = {10};
//
//        try {
//            // Try minimization with
//            pointCostPair = nelderMead.minimizes(
//                    costF, 1000, checker, v1, v2, 5, null);
//        } catch (NoConvergenceException ex) {
//            ex.printStackTrace();
//        } catch (CostException ex) {
//            ex.printStackTrace();
//        }
//
//        double P_z2 = pointCostPair.point[0];
//
//        // Compute the missing corner
//        double x_1, y_1, x_2, y_2, x_3, y_3;
//        x_1 = BOTTOMLeft.x;
//        y_1 = BOTTOMLeft.y;
//        x_2 = BOTTOMRight.x;
//        y_2 = BOTTOMRight.y;
//        x_3 = bottomRight.x;
//        y_3 = bottomRight.y;
//        double P_x2 = x_2 * P_z2;
//        double P_y2 = y_2 * P_z2;
//
//        // Compute possible P_z1
//        double[] P_z1 = Computation.solvePolynomial(
//                (x_1 * x_1) + (y_1 * y_1) + 1,
//                -2d * (x_1 * x_2 + y_1 * y_2 + 1) * P_z2,
//                ( (x_2 * x_2) + (y_2 * y_2) + 1 ) * P_z2 * P_z2 - w * w
//                );
//
//        // Compute possible P_z3
//        double[] P_z3 = Computation.solvePolynomial(
//                (x_3 * x_3) + (y_3 * y_3) + 1,
//                -2d * (x_3 * x_2 + y_3 * y_2 + 1) * P_z2,
//                ( (x_2 * x_2) + (y_2 * y_2) + 1 ) * P_z2 * P_z2 - h * h
//                );
//
//        int goodPoint1 = 0;
//        int goodPoint3 = 0;
//        double oldError = 1d;
//        double error = 1d;
//        int k = 0;
//        for (int i = 0; i < P_z1.length; i++) {
//            for (int j = 0; j < P_z3.length; j++) {
//                // Check for validity of data
//                if(!(
//                        Double.isNaN(P_z1[i]) ||
//                        Double.isInfinite(P_z1[i]) ||
//                        Double.isNaN(P_z1[i]) ||
//                        Double.isInfinite(P_z1[i]) )){
//                    // Compute error
//                    double P_x1 = x_1 * P_z1[i];
//                    double P_y1 = y_1 * P_z1[i];
//                    double P_x3 = x_3 * P_z3[j];
//                    double P_y3 = y_3 * P_z3[j];
//
//                    error = (P_x1 - P_x2) * (P_x3 - P_x2) +
//                            (P_y1 - P_y2) * (P_y3 - P_y2) +
//                            (P_z1[i] - P_z2) * (P_z3[j] - P_z2);
//
//                    // Update when we get a better error
//                    if(Math.abs(error) < Math.abs(oldError)){
//                        oldError = error;
//                        goodPoint1 = i;
//                        goodPoint3 = j;
//                    }
//
//                    k++;
//                }
//            }
//        }
//
//        // Estimate corner
//        double P_x1 = x_1 * P_z1[goodPoint1];
//        double P_y1 = y_1 * P_z1[goodPoint1];
//        double P_x3 = x_3 * P_z3[goodPoint3];
//        double P_y3 = y_3 * P_z3[goodPoint3];
//
//        // Compute missing point
//        double P_x4 = ( (P_x1 - P_x2) + (P_x3 - P_x2) + P_x2 );
//        double P_y4 = ( (P_y1 - P_y2) + (P_y3 - P_y2) + P_y2 );
//        double P_z4 = ( (P_z1[goodPoint1] - P_z2) + (P_z3[goodPoint3] - P_z2) + P_z2 );
//
//        Point result = new Point();
//        result.setLocation((P_x4/P_z4),(P_y4/P_z4));
        
        return result;
    }
    
    private class CornerCostFunction implements CostFunction{
        double x_1, y_1, x_2, y_2, x_3, y_3, w, h;
        public CornerCostFunction(
                Point point1, Point point2, Point point3,
                double width, double height){
            w = width;
            h = height;
            x_1 = point1.x;
            y_1 = point1.y;
            x_2 = point2.x;
            y_2 = point2.y;
            x_3 = point3.x;
            y_3 = point3.y;
            
        }
        
        public double cost(double[] d) throws CostException {
            return cost(d[0]);
        }
        
        public double cost(double d){
            double totalcost = 0d;
            double P_z2 = d;
            double P_x2 = x_2 * P_z2;
            double P_y2 = y_2 * P_z2;
            
            System.err.print(d);
            
            // Compute possible P_z1
            double[] P_z1 = Computation.solvePolynomial(
                    (x_1 * x_1) + (y_1 * y_1) + 1d,
                    -2d * (x_1 * x_2 + y_1 * y_2 + 1d) * P_z2,
                    ( (x_2 * x_2) + (y_2 * y_2) + 1d ) * P_z2 * P_z2 - w * w
                    );
            
            // Compute possible P_z3
            double[] P_z3 = Computation.solvePolynomial(
                    (x_3 * x_3) + (y_3 * y_3) + 1,
                    -2d * (x_3 * x_2 + y_3 * y_2 + 1) * P_z2,
                    ( (x_2 * x_2) + (y_2 * y_2) + 1 ) * P_z2 * P_z2 - h * h
                    );
            
            totalcost = Double.MAX_VALUE;
            double error = Double.MAX_VALUE;;
            int k = 0;
            for (int i = 0; i < P_z1.length; i++) {
                for (int j = 0; j < P_z3.length; j++) {
                    // Check for validity of data
                    if(!(
                            Double.isNaN(P_z1[i]) ||
                            Double.isInfinite(P_z1[i]) ||
                            Double.isNaN(P_z1[i]) ||
                            Double.isInfinite(P_z1[i]) )){
                        // Compute error
                        double P_x1 = x_1 * P_z1[i];
                        double P_y1 = y_1 * P_z1[i];
                        double P_x3 = x_3 * P_z3[j];
                        double P_y3 = y_3 * P_z3[j];
                        
                        error = (P_x1 - P_x2) * (P_x3 - P_x2) +
                                (P_y1 - P_y2) * (P_y3 - P_y2) +
                                (P_z1[i] - P_z2) * (P_z3[j] - P_z2);
                        
                        // Update when we get a better error
                        if(Math.abs(error) < totalcost){
                            totalcost = Math.abs(error);
                        }
                        
                        k++;
                    }
                }
            }
            
            System.err.println(":"+totalcost);
            return totalcost;
        }
    }
}
