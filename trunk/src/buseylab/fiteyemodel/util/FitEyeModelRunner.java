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
package buseylab.fiteyemodel.util;

import java.io.File;
import java.util.concurrent.Semaphore;
import buseylab.fiteyemodel.logic.FitEyeModel;
import buseylab.fiteyemodel.logic.FittingListener;
import buseylab.fiteyemodel.logic.GradientCorrection;

/**
 *
 * @author SQ
 */
public class FitEyeModelRunner extends Thread {

    Semaphore sem;
    FitEyeModel fitEyeModel = null;
    boolean alive = true;
    FittingListener listener;

    public FitEyeModelRunner(FittingListener listener) {
        super("Fit eye model runner");
        this.sem = new Semaphore(0);
        this.listener = listener;
    }

    @Override
    public void run() {
        do {
            FitEyeModel currentModel = getNewFitEyeModel();
            if (currentModel != null) {
                try {
                    Thread t = new Thread(currentModel);
                    t.start();
                    t.join();
                } catch (InterruptedException ex) {
                }
            }
            try {
                this.sem.acquire();
            } catch (InterruptedException discard) {
            }
        } while (this.alive);
    }

    synchronized private FitEyeModel getNewFitEyeModel() {
        FitEyeModel model = this.fitEyeModel;
        // Drain all permits
        this.sem.drainPermits();
        return model;
    }

    /**
     * Set parameters before running.
     * @param gradientCorrection null if the is no need for gradient correcting.
     * If not null, the object is cloned inside the method.
     */
    synchronized public void setParameters(File imageFile, Parameters parameters, 
            GradientCorrection gradientCorrection) {
        // Kill old fit eye model
        if (this.fitEyeModel != null) {
            this.fitEyeModel.kill();
        }
        // Create new fit eye model
        this.fitEyeModel = new FitEyeModel(imageFile, null, parameters, gradientCorrection);
        this.fitEyeModel.setFittingListener(this.listener);
        // Signal semaphore
        this.sem.release();
    }

    synchronized public void kill() {
        // Kill old fit eye model
        if (this.fitEyeModel != null) {
            this.fitEyeModel.kill();
        }
        // Kill this runner
        this.alive = false;

        // Signal semaphore
        this.sem.release();
    }
}
