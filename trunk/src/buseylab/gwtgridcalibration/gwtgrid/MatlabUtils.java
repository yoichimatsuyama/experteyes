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
package buseylab.gwtgridcalibration.gwtgrid;

import java.io.File;

public class MatlabUtils {

    // construct an array 0:len-1
    public static int[] arrayConstructor(int len) {
        int[] A = new int[len];
        for (int i = 0; i < len; i++) {
            A[i] = i;
        }
        return A;
    }

    // tile an an array numTimes
    public static int[] repeatingArray(int[] baseArray, int numTimes) {
        int[] newArray = new int[baseArray.length * numTimes];

        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = baseArray[i % baseArray.length];
        }

        return newArray;
    }

    // shuffle an array
    public static int[] shuffle(int[] A) {
        for (int i = 0; i < A.length; i++) {
            int swapInd = (int) (Math.random() * (double) A.length);
            int tmp = A[i];
            A[i] = A[swapInd];
            A[swapInd] = tmp;
        }

        return A;
    }

    public static File[] shuffle(File[] A) {
        for (int i = 0; i < A.length; i++) {
            int swapInd = (int) (Math.random() * (double) A.length);
            File tmp = A[i];
            A[i] = A[swapInd];
            A[swapInd] = tmp;
        }

        return A;
    }

    // reshape an array into a wxh matrix
    public static int[][] reshape(int[] A, int w, int h) {
        int[][] A2D = new int[w][h];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                A2D[i][j] = A[j * w + i];
            }
        }

        return A2D;
    }

    // reshape an array into a wxh matrix
    public static double[][] reshape(double[] A, int w, int h) {
        double[][] A2D = new double[w][h];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                A2D[i][j] = A[j * w + i];
            }
        }

        return A2D;
    }

    // not a matlab translation, but creates a new vector by interleaving the two that are passed in
    public static double[] interleave(double[] v1, double[] v2) {
        double[] newV = new double[v1.length + v2.length];

        int oldVIdx = 0;
        for (int i = 0; i < newV.length; i += 2) {
            newV[i] = v1[oldVIdx];
            newV[i + 1] = v2[oldVIdx];
            oldVIdx++;
        }

        return newV;
    }

    // this method does complex multiplication across 2 vectors, that are organized
    // in the manner v = [real1 imag1 real2 imag2 ... ...]
    public static double[] complexMultiply(double[] complexV1, double[] complexV2) {
        //return new Complex(x*w.real()-y*w.imag(),x*w.imag()+y*w.real());
        double[] productVec = new double[complexV1.length];

        complexMultiply(complexV1, complexV2, productVec);

        return productVec;
    }

    // this method does complex multiplication across 2 vectors, that are organized
    // in the manner v = [real1 imag1 real2 imag2 ... ...]
    public static void complexMultiply(double[] complexV1, double[] complexV2, double[] results) {
        double[] productVec = results;

        double real1;
        double real2;
        double imag1;
        double imag2;
        double realProd;
        double imagProd;
        for (int i = 0; i < complexV1.length - 1; i += 2) {
            // get the real numbers
            real1 = complexV1[i];
            real2 = complexV2[i];
            // get the imaginary numbers
            imag1 = complexV1[i + 1];
            imag2 = complexV2[i + 1];

            realProd = real1 * real2 - imag1 * imag2;
            imagProd = real1 * imag2 + imag1 * real2;
            // do the math
            productVec[i] = realProd;
            productVec[i + 1] = imagProd;
        }
    }

    // translation of similarity function
    public static double compareTwoJets(
            double[] magnitudeResps1, double[] magnitudeResps2,
            double[] phaseResps1, double[] phaseResps2) {
        double numerator = 0;
        double sumMagnitudeResps1 = 0;
        double sumMagnitudeResps2 = 0;
        double denominator = 0;

        // product of mag resps
        // diff of phase resps
        // squared mag resps
        double magnitudeProds;
        double phaseDiffs;
        double resps1;
        double resps2;
        for (int i = 0; i < magnitudeResps1.length; i++) {
            resps1 = magnitudeResps1[i];
            resps2 = magnitudeResps2[i];

            magnitudeProds = resps1 * resps2;
            phaseDiffs = phaseResps1[i] - phaseResps2[i];

            // for the numerator, sum of magnitudes * cos of phases
            // for the denominator, sum the magnitude resps
            numerator += magnitudeProds * Math.cos(phaseDiffs);
            sumMagnitudeResps1 += resps1 * resps1;
            sumMagnitudeResps2 += resps2 * resps2;
        }

        denominator = sumMagnitudeResps1 * sumMagnitudeResps2;

        return numerator / Math.sqrt(denominator);
    }
}
