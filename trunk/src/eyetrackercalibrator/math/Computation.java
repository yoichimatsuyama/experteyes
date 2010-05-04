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
 * Computation.java
 *
 * Created on October 13, 2007, 1:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package eyetrackercalibrator.math;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;

/**
 *
 * @author rakavipa
 */
public class Computation {

    /** Creates a new instance of Computation */
    public Computation() {
    }

    /**
     * Use projective translation to find real screen position.  This assume that
     * barrel correction is already run.
     * @param trueScreenDimension
     * @return null if there is an error
     */
    public static Point2D ComputeScreenPositionProjective(
            Dimension2D trueScreenDimension,
            Point2D pos,
            Point2D topLeft, Point2D topRight,
            Point2D bottomLeft, Point2D bottomRight) {
        // Sanity check
        if (trueScreenDimension == null || pos == null || topLeft == null
                || topRight == null || bottomLeft == null || bottomRight == null) {
            return null;
        }

        /**
         * Create Transformation matrix A for p' = Ap. where p is what we want
         * and p' is what we find from .
         *
         * First we find transformation matrix from unit square to what we
         * ovserved from the scene.  Let p1,p2,p3 and p4 be observed corner
         * coordinates from the scene arranged as shown below.
         *
         *  p4 --- p3
         *   |      |
         *  p1 --- p2
         *
         *  The transformation follows the equation below
         *
         *  h*x'   a_11 a_12 a13   x
         *  h*y' = a_21 a_22 a23 . y
         *   h     a_31 a_32  1    1
         *
         *  where x,y is the original position and x',y' is the translated position
         *
         *  We have 4 set of x',y' which are our corners.  To make things easier
         *  original x,y can just be unit square
         *
         *  Solving for A we have
         *  a_31 = [(x1-x2+x3-x4)*(y4-y3)-(y1-y2+y3-y4)*(x4-x3)]/[(x2-x3)*(y4-y3)-(x4-x3)*(y2-y3)]
         *  a_32 = [(y1-y2+y3-y4)*(x2-x3)-(x1-x2+x3-x4)*(y2-y3)]/[(x2-x3)*(y4-y3)-(x4-x3)*(y2-y3)]
         *  a_11 = x2-x1+a_31*x2, a_12 = x4-x1+a_32*x4, a_13=x1
         *  a_21 = y2-y1+a_31*y2, a_22 = y4-y1+a_32*y4, a_13=y1
         *
         * Reference: Wilhelm Burger and Mark J. Burge, Digital Image Processing An Algorithmic Introduction Using Java, Springer, 2008, P380-385
         */
        double[] x = {0, bottomLeft.getX(), bottomRight.getX(),
            topRight.getX(), topLeft.getX()}; // 0 is added so that we can use 1-4 for index
        double[] y = {0, bottomLeft.getY(), bottomRight.getY(),
            topRight.getY(), topLeft.getY()}; // 0 is added so that we can use 1-4 for index
        double[][] A = new double[4][4]; // Just so that we can use index of 1-3 for easy checking

        // Check devider
        double devider = ((x[2] - x[3]) * (y[4] - y[3]) - (x[4] - x[3]) * (y[2] - y[3]));
        if (devider == 0) {
            return null;
        }
        A[3][1] = ((x[1] - x[2] + x[3] - x[4]) * (y[4] - y[3]) - (y[1] - y[2] + y[3] - y[4]) * (x[4] - x[3]))
                / ((x[2] - x[3]) * (y[4] - y[3]) - (x[4] - x[3]) * (y[2] - y[3]));

        devider = ((x[2] - x[3]) * (y[4] - y[3]) - (x[4] - x[3]) * (y[2] - y[3]));
        if (devider == 0) {
            return null;
        }
        A[3][2] = ((y[1] - y[2] + y[3] - y[4]) * (x[2] - x[3]) - (x[1] - x[2] + x[3] - x[4]) * (y[2] - y[3]))
                / ((x[2] - x[3]) * (y[4] - y[3]) - (x[4] - x[3]) * (y[2] - y[3]));
        A[1][1] = x[2] - x[1] + A[3][1] * x[2];
        A[2][1] = y[2] - y[1] + A[3][1] * y[2];
        A[1][2] = x[4] - x[1] + A[3][2] * x[4];
        A[2][2] = y[4] - y[1] + A[3][2] * y[4];
        A[1][3] = x[1];
        A[2][3] = y[1];
        A[3][3] = 1;

        /** Compute A^(-1) to inverse the transformation by using A_adj
         * Compute ajacency matrix A_adj x Given x
         */
        double unitX = (A[2][2] * A[3][3] - A[2][3] * A[3][2]) * pos.getX() + (A[1][3] * A[3][2] - A[1][2] * A[3][3]) * pos.getY() + (A[1][2] * A[2][3] - A[1][3] * A[2][2]);
        double unitY = (A[2][3] * A[3][1] - A[2][1] * A[3][3]) * pos.getX() + (A[1][1] * A[3][3] - A[1][3] * A[3][1]) * pos.getY() + (A[1][3] * A[2][1] - A[1][1] * A[2][3]);
        double h = (A[2][1] * A[3][2] - A[2][2] * A[3][1]) * pos.getX() + (A[1][2] * A[3][1] - A[1][1] * A[3][2]) * pos.getY() + (A[1][1] * A[2][2] - A[1][2] * A[2][1]);

        // Sanity check
        if (h == 0) {
            return null;
        } else {
            return new Point2D.Double(
                    trueScreenDimension.getWidth() * unitX / h,
                    trueScreenDimension.getHeight() * unitY / h);
        }
    }

    /**
     *  Compute eyegaze spot from eye vector (obtained from cornia reflection
     *  and pupil center location).  The functions for computation is
     *
     *  S_x = c_0_0 + c_0_1 x + c_0_2 y + c_0_3 x y + c_0_4 x x + c_0_5 y y
     *  S_y = c_1_0 + c_1_1 x + c_1_2 y + c_1_3 x y + c_1_4 x x + c_1_5 y y
     *
     *  S_x is x position of eyegaze point on screen
     *  S_y is y position of eyegaze point on screen
     *
     *  @param x is X component of eye vector
     *  @param y is Y component of eye vector
     *  @param c[][] Two constant vector each with size of 6.  
     *               If the size is less than 6 program
     *                 will throw array exceeding exception
     *  @return (S_x,S_y)
     */
    public static Point2D.Double computeEyeGazePoint(double x, double y, double[][] c) {
        return new Point2D.Double(
                computeTwoVariablePolynomial(x, y, c[0]),
                computeTwoVariablePolynomial(x, y, c[1]));
    }

    /** Compute Degree 2 or 3 polynomial when possible */
    public static double computeTwoVariablePolynomial(double x, double y, double[] c) {
        double v = c[0] + c[1] * x + c[2] * y + c[3] * x * y + c[4] * x * x + c[5] * y * y;
        if (c.length >= 10) {
            v += c[6] * x * y * y + c[7] * y * x * x + c[8] * x * x * x + c[9] * y * y * y;
        }

        return v;
    }

    /**
     * Counting number of digit. The method can only count to 10
     */
    public static int countIntegerDigit(int x) {
        if (x < 10000) {
            if (x < 100) {
                if (x < 10) {
                    return 1;
                } else {
                    return 2;
                }
            } else {
                if (x < 1000) {
                    return 3;
                } else {
                    return 4;
                }
            }
        } else {
            if (x < 1000000) {
                if (x < 100000) {
                    return 5;
                } else {
                    return 6;
                }
            } else {
                if (x < 100000000) {
                    if (x < 10000000) {
                        return 7;
                    } else {
                        return 8;
                    }
                } else {
                    if (x < 1000000000) {
                        return 9;
                    } else {
                        return 10;
                    }
                }
            }
        }
    }

    public static void main(String args[]) {
        Point2D correct = new Point2D.Double(1008, 1024 - 877);
        //Point2D correct = new Point2D.Double(1954-1280,1024-112);
        System.out.println(correct);
        System.out.println(ComputeScreenPositionProjective(new Dimension(1280, 1024),
                //new Dimension(2048, 1536),
                new Point2D.Double(421, 129.5)//1044, 893)//900,448)
                , new Point2D.Double(74, 38)//325, 164)/*topLeft*/
                , new Point2D.Double(436, 22)//1317, 142)//topRight
                , new Point2D.Double(97, 226)//291, 952)//bottomLeft
                , new Point2D.Double(428, 227)//1272, 1043)/*bottomRight*/
                ));
    }
}
