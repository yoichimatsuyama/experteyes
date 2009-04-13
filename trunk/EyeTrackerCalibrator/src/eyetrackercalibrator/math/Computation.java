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
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;

/**
 *
 * @author rakavipa
 */
public class Computation {

    /** Creates a new instance of Computation */
    public Computation() {
    }

    /**
     * Compute average calibration angle error of the all calibration points
     * You must make sure that
     * @param calibrationPoints
     * @param eyeGaze
     * @param screendDimensionPixel 
     * @param screenWidthCM
     * @param distanceFromScreenCM
     * @param screenHeightCM
     * @return nonnegative average error.  -1 if there is any error.
     * Possible cause for an error are:
     * <ul>
     * <li>Providing none positive values to any of screendDimensionPixel, distanceFromScreenCM, screenWidthCM and screenHeightCM</li>
     * <li>The length of the array are not equal.</li>
     * </ul>
     */
    public static double ComputeEyeCalibrationErrorAngle(
            Point2D calibrationPoint, Point2D eyeGazePoint,
            Dimension screendDimensionPixel, double distanceFromSceneCM,
            double sceneWidthCM, double sceneHeightCM) {
        // Sanity check
        if (calibrationPoint == null || eyeGazePoint == null ||
                screendDimensionPixel.height <= 0 ||
                screendDimensionPixel.width <= 0 ||
                distanceFromSceneCM <= 0 ||
                sceneWidthCM <= 0 || sceneHeightCM <= 0) {
            return -1;
        }

        // First find cm length per pixel in scene
        double cmPerPixWidth = sceneWidthCM / screendDimensionPixel.getWidth();
        double cmPerPixHeight = sceneHeightCM / screendDimensionPixel.getHeight();

        // Compute error in cm
        double widthError = (eyeGazePoint.getX() - calibrationPoint.getX()) * cmPerPixWidth;
        double heightError = (eyeGazePoint.getY() - calibrationPoint.getY()) * cmPerPixHeight;
        double distanceError = Math.sqrt(widthError * widthError + heightError * heightError);

        /**
         *        /| Projected point
         *       / |
         *      /  |
         *     /a  |
         * eye ---- Correct point
         *
         *  Trying to estimate the angle a.
         */
        return Math.toDegrees(Math.atan2(distanceError, distanceFromSceneCM));
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
        if (trueScreenDimension == null || pos == null || topLeft == null ||
                topRight == null || bottomLeft == null || bottomRight == null) {
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
        A[3][1] = ((x[1] - x[2] + x[3] - x[4]) * (y[4] - y[3]) - (y[1] - y[2] + y[3] - y[4]) * (x[4] - x[3])) /
                ((x[2] - x[3]) * (y[4] - y[3]) - (x[4] - x[3]) * (y[2] - y[3]));

        devider = ((x[2] - x[3]) * (y[4] - y[3]) - (x[4] - x[3]) * (y[2] - y[3]));
        if (devider == 0) {
            return null;
        }
        A[3][2] = ((y[1] - y[2] + y[3] - y[4]) * (x[2] - x[3]) - (x[1] - x[2] + x[3] - x[4]) * (y[2] - y[3])) /
                ((x[2] - x[3]) * (y[4] - y[3]) - (x[4] - x[3]) * (y[2] - y[3]));
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
     * Use QR decomposition to solve for least square solution to finding
     * constant for screen position equation.
     * The equations are
     *    x = c_1 + c_2 * x_c + c_3 * y_c + c_4 * x_c * y_c
     *    y = c_5 + c_6 * x_c + c_6 * y_c + c_8 * x_c * y_c
     * Source: John C. Russ, ImageProcessing Handbook 5th Ed, P255-256, 2007
     */
    public static Point2D ComputeScreenPositionQR(
            Dimension2D trueScreenDimension,
            Dimension2D viewScreenDimension,
            Point2D pos,
            Point2D topLeft, Point2D topRight,
            Point2D bottomLeft, Point2D bottomRight) {
        double xMove = viewScreenDimension.getWidth() / 2d;
        double yMove = viewScreenDimension.getHeight() / 2d;

        /**
         * Create A matrix in A c = v.  A is 4x4 matrix
         * Each row of A represent (1, x, y, xy) from each observed corner.
         * c is a vector represent all constants we want to find. 
         * There are two set here cx (c_1 - c_4) and c_y (c_5-c_8)
         * v is the vector represents monitor corners there are also two sets
         * correcponding to constants v_x (h,h,0,0) and v_y (0,0,w,w)
         */
        double[][] A = new double[4][4];
        Point2D[] corners = {topLeft, topRight, bottomLeft, bottomRight};
        for (int i = 0; i < 4; i++) {
            // Translate to center
            double x = corners[i].getX() - xMove;
            double y = corners[i].getY() - yMove;
            A[i][0] = 1;
            A[i][1] = x;
            A[i][2] = y;
            A[i][3] = x * y;
        }
        RealMatrixImpl matrixA = new RealMatrixImpl(A);

        /** Create v_x and v_y */
        double w = trueScreenDimension.getWidth();
        double h = trueScreenDimension.getHeight();
        double[] v_x = {0, w, 0, w};
        double[] v_y = {0, 0, h, h};
        RealMatrixImpl vectorVx = new RealMatrixImpl(v_x);
        RealMatrixImpl vectorVy = new RealMatrixImpl(v_y);

        /** Perform QR decomposition */
        QRDecompositionImpl qRDecomposition = new QRDecompositionImpl(matrixA);

        /** Find (Q^T)V */
        RealMatrix qT = qRDecomposition.getQ().transpose();
        RealMatrix qTVx = qT.multiply(vectorVx);
        RealMatrix qTVy = qT.multiply(vectorVy);

        /** Solve system Rc = (Q^T)v*/
        RealMatrix r = qRDecomposition.getR();
        double[] c_x = r.solve(qTVx).getColumn(0);
        double[] c_y = r.solve(qTVy).getColumn(0);

        double x = pos.getX() - xMove;
        double y = pos.getY() - yMove;
        Point2D.Double result = new Point2D.Double(
                c_x[0] + c_x[1] * x + c_x[2] * y + c_x[3] * x * y,
                c_y[0] + c_y[1] * x + c_y[2] * y + c_y[3] * x * y);

        return result;
    }

    /**
     * Find a point in real screen given corners of skewed screen's corners.
     *
     * Screen coordinate is using top left = (0,0)
     *
     * @return if trueScreenDimension is null, the value returned is simply
     *         assume that real screen is 1x1
     */
    public static Point2D ComputeScreenPosition(
            Dimension2D trueScreenDimension, Point2D pos,
            Point2D topLeft, Point2D topRight,
            Point2D bottomLeft, Point2D bottomRight) {

        double[] r;

        /**
         * Solve for x ratio, r_x
         * x_up = r_x(topright.x - topleft.x) + topleft.x
         * y_up = r_x(topright.y - topleft.y) + topleft.y
         * x_down = r_x(bottomright.x - bottomleft.x) + bottomleft.x
         * y_down = r_x(bottomright.y - bottomleft.y) + bottomleft.y
         *
         * We have up, down and p on the same line so we have
         * x_p = a y_p + b  -- (1)
         * x_up = a y_up + b -- (2)
         * x_down = a y_down + b -- (3)
         *
         * Rearrange (1)
         * b = x_p - a y_p
         *
         * Substitute b in (2)
         *
         * x_up = a y_up + x_p - a y_p
         * x_up = a (y_up - y_p) + x_p -- (4)
         *
         * Subtrate (2) with (3)
         *
         * x_up - x_down = a (y_up - y_down)
         *
         * a = (x_up - x_down)/(y_up - y_down)
         *
         * Substitute a and b in (4)
         *
         * x_up = (x_up - x_down)/(y_up - y_down) (y_up - y_p) + x_p
         * (x_up - x_p)/(x_up - x_down) = (y_up - y_p)/(y_up - y_down)
         *
         * Substitute x_up, y_up, x_down and y_down we get
         *
         * (r_x(topright.x - topleft.x) + topleft.x - x_p)/
         * (r_x(topright.x - topleft.x) + topleft.x - r_x(bottomright.x - bottomleft.x) - bottomleft.x)
         *  =
         * (r_x(topright.y - topleft.y) + topleft.y - y_p)/
         * (r_x(topright.y - topleft.y) + topleft.y - r_x(bottomright.y - bottomleft.y) - bottomleft.y)
         *
         *  Rearrange
         * (r_x(topright.x - topleft.x) + topleft.x - x_p)/
         * (r_x(topright.x - topleft.x - bottomright.x + bottomleft.x) + topleft.x - bottomleft.x)
         *  =
         * (r_x(topright.y - topleft.y) + topleft.y - y_p)/
         * (r_x(topright.y - topleft.y - bottomright.y + bottomleft.y) + topleft.y - bottomleft.y)
         */
        r = solveRatio(
                topRight.getX() - topLeft.getX(),
                topLeft.getX() - pos.getX(),
                topRight.getX() - topLeft.getX() - bottomRight.getX() + bottomLeft.getX(),
                topLeft.getX() - bottomLeft.getX(),
                topRight.getY() - topLeft.getY(),
                topLeft.getY() - pos.getY(),
                topRight.getY() - topLeft.getY() - bottomRight.getY() + bottomLeft.getY(),
                topLeft.getY() - bottomLeft.getY());

        double r_x = selectSolution(r[0], r[1]);

        /**
         * Solve for y ratio, r_y
         * x_left = r_y(topleft.x - bottomleft.x) + bottomleft.x
         * y_left = r_y(topleft.y - bottomleft.y) + bottomleft.y
         * x_right = r_y(topright.x - bottomright.x) + bottomright.x
         * y_right = r_y(topright.y - bottomright.y) + bottomright.y
         *
         * We have left, right and p on the same line so we have
         * y_p = a x_p + b  -- (1)
         * y_left = a x_left + b -- (2)
         * y_right = a x_right + b -- (3)
         *
         * Rearrange (1)
         * b = y_p - a x_p
         *
         * Substitute b in (2)
         *
         * y_left = a x_left + y_p - a x_p
         * y_left = a (x_left - x_p) + y_p -- (4)
         *
         * Subtrate (2) with (3)
         *
         * y_left - y_right = a (x_left - x_right)
         *
         * a = (y_left - y_right)/(x_left - x_right)
         *
         * Substitute a and b in (4)
         *
         * y_left = (y_left - y_right)/(x_left - x_right) (x_left - x_p) + y_p
         * (y_left - y_p)/(y_left - y_right) = (x_left - x_p)/(x_left - x_right)
         *
         * Substitute x_left, y_left, x_right and y_right we get
         *
         * (r_y(topleft.y - bottomleft.y) + bottomleft.y - y_p) /
         * (r_y(topleft.y - bottomleft.y) + bottomleft.y - r_y(topright.y - bottomright.y) - bottomright.y)
         * =
         * (r_y(topleft.x - bottomleft.x) + bottomleft.x - x_p)/
         * (r_y(topleft.x - bottomleft.x) + bottomleft.x - r_y(topright.x - bottomright.x) - bottomright.x)
         *
         * (r_y(topleft.y - bottomleft.y) + bottomleft.y - y_p) /
         * (r_y(topleft.y - bottomleft.y - topright.y + bottomright.y) + bottomleft.y - bottomright.y)
         * =
         * (r_y(topleft.x - bottomleft.x) + bottomleft.x - x_p)/
         * (r_y(topleft.x - bottomleft.x - topright.x + bottomright.x) + bottomleft.x - bottomright.x)
         *
         */
        r = solveRatio(
                topLeft.getY() - bottomLeft.getY(),
                bottomLeft.getY() - pos.getY(),
                topLeft.getY() - bottomLeft.getY() - topRight.getY() + bottomRight.getY(),
                bottomLeft.getY() - bottomRight.getY(),
                topLeft.getX() - bottomLeft.getX(),
                bottomLeft.getX() - pos.getX(),
                topLeft.getX() - bottomLeft.getX() - topRight.getX() + bottomRight.getX(),
                bottomLeft.getX() - bottomRight.getX());

        double r_y = selectSolution(r[0], r[1]);

        if (trueScreenDimension == null) {
            trueScreenDimension = new Dimension(1, 1);
        }

        return new Point2D.Double(
                r_x * trueScreenDimension.getWidth(),
                r_y * trueScreenDimension.getHeight());
    }

    /**
     * Helper for ComputeScreenPosition.  It will select r which is from 0 to 1
     * first.  If both are valid the biggest one is selected.  Otherwise,
     * simply the biggest one will be selected
     */
    public static double selectSolution(double r1, double r2) {
        double r = Math.max(r1, r2);

        if (r > 1d) {
            if (r1 > 1d) {
                if (r2 >= 0d && r2 <= 1d) {
                    r = r2;
                }
            } else {
                // r1 is right. and r2 is bigger than 1
                r = r1;
            }
        }

        return r;
    }

    /**
     * Solve for r from the equation below
     *
     * (r c1 + c2)/(r c3 + c4) = (r c5 + c6)/(r c7 + c8)
     *
     * @return double[2] with each cell holding one of the solution
     *          Double.NaN is assigned if solutions are not in real space
     */
    public static double[] solveRatio(
            double c1, double c2, double c3, double c4,
            double c5, double c6, double c7, double c8) {
        return solvePolynomial(
                c1 * c7 - c3 * c5,
                c2 * c7 + c1 * c8 - c3 * c6 - c4 * c5,
                c2 * c8 - c4 * c6);
    }

    /**
     *  Solve ax^2 + bx + c
     *
     *  @return double[2] with each cell holding one of the solution
     *          Double.NaN is assigned if solutions are not in real space
     */
    public static double[] solvePolynomial(double a, double b, double c) {
        double[] sol = new double[2];

        double insqr = b * b - 4.0d * a * c;
        if (insqr < 0) {
            sol[0] = sol[1] = Double.NaN;
        } else if (a == 0d) {
            if (b == 0d) {
                sol[0] = sol[1] = Double.POSITIVE_INFINITY;
            } else {
                sol[0] = sol[1] = -1d * (c / b);
            }
        } else {

            double sqr = Math.sqrt(insqr);
            sol[0] = (-1d * b + sqr) / (2d * a);
            sol[1] = (-1d * b - sqr) / (2d * a);
        }

        return sol;
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

    /**
     * 
     * @param pupilX
     * @param pupilY
     * @param corniaReflectX
     * @param corniaReflectY
     * @return
     */
    public static Point2D.Double computeEyeVector(
            double pupilX, double pupilY,
            double corniaReflectX, double corniaReflectY) {
        return new Point2D.Double(
                pupilX - corniaReflectX,
                pupilY - corniaReflectY);
    }

    public static double computeTwoVariablePolynomial(double x, double y, double[] c) {
        return c[0] + c[1] * x + c[2] * y + c[3] * x * y + c[4] * x * x + c[5] * y * y;
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
