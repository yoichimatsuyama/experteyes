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
package eyetrackercalibrator.math;

import java.awt.geom.Point2D;
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;

/**
 * This class uses QR decomposition to solve eye gaze  cefficient.
 * User must submit at least 6 samples for estimation
 *
 * There are 12 constants to be found as shown in equations below
 *
 * S_x = c_0 + c_1 x + c_2 y + c_3 x y + c_4 x x + c_5 y y
 * S_y = c_6 + c_7 x + c_8 y + c_9 x y + c_10 x x + c_11 y y
 *
 * S_x is x position of eyegaze point on screen
 * S_y is y position of eyegaze point on screen
 *
 * @author SQ
 */
public class CalibrateEyeGazeQR implements CalibrateEyeGaze {

    private CalibrateEyeGazeListener listener;

    @Override
    public double[][] calibrate(Point2D[] eyeVector, Point2D[] calibratePoints) {
        final int TOTAL_COEFF = 6;

        // For sanity
        final int totalSamples = Math.min(eyeVector.length, calibratePoints.length);

        /**
         * Create A matrix in A c = v.  A is total sample x 6 matrix
         * Each row of A represent (1, x, y, xy, xx, yy) from each calibration point sample.
         * c is a vector represent all constants we want to find. (c_0 - c_5)
         * There are two set that we used c_x and c_y
         * v is the vector represents calibration points (x_0, x_1, ...) for x
         * and (y_0, y_1, ...) for y
         */
        double[][] A = new double[totalSamples][TOTAL_COEFF];
        for (int i = 0; i < totalSamples; i++) {
            double x = eyeVector[i].getX();
            double y = eyeVector[i].getY();
            A[i][0] = 1;
            A[i][1] = x;
            A[i][2] = y;
            A[i][3] = x * y;
            A[i][4] = x * x;
            A[i][5] = y * y;
        }
        RealMatrixImpl matrixA = new RealMatrixImpl(A);

        /** Create v */
        double[] v_x = new double[totalSamples];
        double[] v_y = new double[totalSamples];
        for (int i = 0; i < totalSamples; i++) {
            v_x[i] = calibratePoints[i].getX();
            v_y[i] = calibratePoints[i].getY();
        }
        RealMatrixImpl vectorVx = new RealMatrixImpl(v_x);
        RealMatrixImpl vectorVy = new RealMatrixImpl(v_y);

        /** Perform QR decomposition */
        QRDecompositionImpl qRDecomposition = new QRDecompositionImpl(matrixA);

        /** Find (Q^T)V */
        RealMatrix qT = qRDecomposition.getQ().transpose();
        RealMatrix qTVx = qT.multiply(vectorVx).getSubMatrix(0, TOTAL_COEFF-1, 0, 0);
        RealMatrix qTVy = qT.multiply(vectorVy).getSubMatrix(0, TOTAL_COEFF-1, 0, 0);

        /** Solve system Rc = (Q^T)v*/
        RealMatrix r = qRDecomposition.getR().getSubMatrix(
                0, TOTAL_COEFF - 1, 0, TOTAL_COEFF - 1);

        RealMatrix c_x = r.solve(qTVx);
        RealMatrix c_y = r.solve(qTVy);

        double[][] result = new double[2][];
        result[0] = c_x.getColumn(0);
        result[1] = c_y.getColumn(0);

        if (this.listener != null) {
            this.listener.update(result, 0);
            this.listener.completeStage(1);
        }

        return result;
    }

    @Override
    public long getTotalProgress() {
        return 1;
    }

    @Override
    public int getTotalStages() {
        return 1;
    }

    @Override
    public void setCalibrateEyeGazeListener(CalibrateEyeGazeListener listener) {
        this.listener = listener;
    }
}
