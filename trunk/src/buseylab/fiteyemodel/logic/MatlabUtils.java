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
package buseylab.fiteyemodel.logic;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

public class MatlabUtils {
	
	// construct an array 0:len-1
	public static int[] arrayConstructor(int len) {
		int[] A = new int[len];
		for(int i = 0; i < len; i++) {
			A[i] = i;
		}
		return A;
	}
	
	// tile an an array numTimes
	public static int[] repeatingArray(int[] baseArray, int numTimes) {
		int[] newArray = new int[baseArray.length * numTimes];
		
		for(int i = 0; i < newArray.length; i++) {
			newArray[i] = baseArray[i % baseArray.length];
		}
		
		return newArray;
	}
	
	
	// shuffle an array
	public static int[] shuffle(int[] A) {
		for(int i = 0; i < A.length; i++) {
			int swapInd = (int)(Math.random()*(double)A.length);
			int tmp = A[i];
			A[i] = A[swapInd];
			A[swapInd] = tmp;
		}
		
		return A;
	}
	public static File[] shuffle(File[] A) {
		for(int i = 0; i < A.length; i++) {
			int swapInd = (int)(Math.random()*(double)A.length);
			File tmp = A[i];
			A[i] = A[swapInd];
			A[swapInd] = tmp;
		}
		
		return A;
	}
	
	// reshape an array into a wxh matrix
	public static int[][] reshape(int[] A, int w, int h) {
		int[][] A2D = new int[w][h];
		
		for(int i = 0; i < w; i++) {
			for(int j = 0; j < h; j++) {
				A2D[i][j] = A[j*w + i];
			}
		}
		
		return A2D;
	}
	
}
