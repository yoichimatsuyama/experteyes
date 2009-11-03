/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eyetrackercalibrator.framemanaging;

import org.jfree.data.xy.XYDataset;

/**
 * This is an abstract class for implementing XYDataset with synchronization
 * with global frame number
 * @author ruj
 */
public abstract class SyncXYDataSet implements XYDataset{
    protected FrameSynchronizor frameSynchronizor;

    public FrameSynchronizor getFrameSynchronizor() {
        return frameSynchronizor;
    }

    public void setFrameSynchronizor(FrameSynchronizor frameSynchronizor) {
        this.frameSynchronizor = frameSynchronizor;
    }
}
