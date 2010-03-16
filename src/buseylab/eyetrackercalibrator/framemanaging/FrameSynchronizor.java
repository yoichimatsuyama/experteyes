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
package buseylab.eyetrackercalibrator.framemanaging;

/**
 * This class helps synchronize eye frames and scene frames.  Once synch, the
 * total number of frames usually increases.  Therefore it is recommended that
 * getTotalFrame is used to get the new total frames.
 * @author ruj
 */
public class FrameSynchronizor {

    int currentBlock = 0;
    int currentFrame = 0;
    private int totalFrame;

    class SyncBlock {

        /** start frame number */
        public int start = 1;
        public int end = Integer.MAX_VALUE;
        /* Eye frame number */
        public int startEyeFrame = 1;
        public int endEyeFrame = Integer.MAX_VALUE;
        /* Strrting scene frame number */
        public int startSceneFrame = 1;
        public int endSceneFrame = Integer.MAX_VALUE;
    };
    SyncBlock[] syncBlocks;

    public FrameSynchronizor() {
        clearSyncPoints(1, 1);
    }

    private void clearSyncPoints(int totalEyeFrame, int totalSceneFrame) {
        this.syncBlocks = new SyncBlock[1];
        this.syncBlocks[0] = new SyncBlock();
        this.syncBlocks[0].endEyeFrame = totalEyeFrame;
        this.syncBlocks[0].endSceneFrame = totalSceneFrame;
        this.totalFrame = Math.max(totalEyeFrame, totalSceneFrame);
        this.currentBlock = 0;
    }

    /** Return total number of frames */
    public int getTotalFrame() {
        return this.totalFrame;
    }

    /**
     * Setting synchronization points.  Empty or null input will make the
     * system assume that the stream is already sync.
     *
     * The points must not overlap.  If they do, the the output behavior is
     * unknown.
     *
     * @param points if null or of zero size.  All synchronizations are clear by
     * having only one sync point.
     * @param totalEyeFrame if less than 1 then the value will be set to 1.
     * @param totalSceneFrame if less than 1 then the value will be set to 1.
     */
    public void setSynchronizationPoints(SynchronizationPoint[] points,
            int totalEyeFrame, int totalSceneFrame) {
        if (points == null || points.length == 0) {
            // Just clear the sync point
            clearSyncPoints(totalEyeFrame, totalSceneFrame);
        } else {
            int total = points.length;
            // Populate accordingly
            this.syncBlocks = new SyncBlock[total];

            // Populate the first one
            this.syncBlocks[0] = new SyncBlock();

            // Compute offset just for the first block
            if (points[0].eyeFrame > points[0].sceneFrame) {
                this.syncBlocks[0].startEyeFrame = points[0].eyeFrame - points[0].sceneFrame + 1;
            } else {
                this.syncBlocks[0].startSceneFrame = points[0].sceneFrame - points[0].eyeFrame + 1;
            }

            int length = 0;

            // Compute offset for all following blocks
            for (int i = 1; i < points.length; i++) {
                this.syncBlocks[i] = new SyncBlock();

                // Compute the end points for the previous block
                this.syncBlocks[i - 1].endEyeFrame = points[i].eyeFrame - 1;
                this.syncBlocks[i - 1].endSceneFrame = points[i].sceneFrame - 1;

                length = this.syncBlocks[i - 1].endEyeFrame - this.syncBlocks[i - 1].startEyeFrame;
                length = Math.max(length,
                        this.syncBlocks[i - 1].endSceneFrame - this.syncBlocks[i - 1].startSceneFrame);
                this.syncBlocks[i - 1].end = this.syncBlocks[i - 1].start + length;

                // Set the current block values
                this.syncBlocks[i].startEyeFrame = points[i].eyeFrame;
                this.syncBlocks[i].startSceneFrame = points[i].sceneFrame;
                this.syncBlocks[i].start = this.syncBlocks[i - 1].end + 1;
            }

            // Compute the total frames from last block
            SyncBlock s = this.syncBlocks[this.syncBlocks.length - 1];
            s.endEyeFrame = totalEyeFrame;
            s.endSceneFrame = totalSceneFrame;
            length = s.endEyeFrame - s.startEyeFrame;
            length = Math.max(length, s.endSceneFrame - s.startSceneFrame);
            s.end = s.start + length;
            this.totalFrame = s.end;
        }
    }

    private synchronized SyncBlock getCurrentBlock(int currentFrame) {
        if (this.currentFrame == currentFrame) {
            // Does nothing
            return this.syncBlocks[this.currentBlock];
        } else {
            this.currentFrame = currentFrame;
        }

        SyncBlock block = this.syncBlocks[this.currentBlock];
        /* Check if the current frame is in the current block */
        if (currentFrame > block.end) {
            // Search to the right
            for (int i = this.currentBlock + 1; i < this.syncBlocks.length; i++) {
                SyncBlock syncBlock = syncBlocks[i];
                if (currentFrame >= syncBlock.start && currentFrame <= syncBlock.end) {
                    // Stop searching
                    this.currentBlock = i;
                    return this.syncBlocks[this.currentBlock];
                }
            }
            // Stop searching
            this.currentBlock = this.syncBlocks.length - 1;
            return this.syncBlocks[this.currentBlock];
        } else if (currentFrame < block.start) {
            // Search to the left
            for (int i = this.currentBlock - 1; i > 0; i--) {
                SyncBlock syncBlock = syncBlocks[i];
                if (currentFrame >= syncBlock.start && currentFrame <= syncBlock.end) {
                    // Stop searching
                    this.currentBlock = i;
                    return this.syncBlocks[this.currentBlock];
                }
            }
            // Stop searching
            this.currentBlock = 0;
            return this.syncBlocks[this.currentBlock];
        }
        // No need to change block
        return this.syncBlocks[this.currentBlock];
    }

    /** Get eye frame number according to previously set current frame */
    private int getEyeFrame(int currentFrame, SyncBlock block) {
        // Look up from the current block
        int frame = currentFrame - block.start + block.startEyeFrame;

        if (frame <= block.endEyeFrame) {
            return frame;
        } else {
            return -1;
        }
    }

    /**
     * Get eye frame number according to the current frame and also set the current
     * frame as a new current frame
     */
    public int getEyeFrame(int currentFrame) {
        return getEyeFrame(currentFrame,getCurrentBlock(currentFrame));
    }

    /** Get scene frame number according to previously set current frame */
    private int getSceneFrame(int currentFrame, SyncBlock block) {
        //SyncBlock block = this.syncBlocks[this.currentBlock];

        // Look up from the current block
        int frame = currentFrame - block.start + block.startSceneFrame;

        if (frame <= block.endSceneFrame) {
            return frame;
        } else {
            return -1;
        }
    }

    /**
     * Get scene frame number according to the current frame and also set the current
     * frame as a new current frame
     */
    public int getSceneFrame(int currentFrame) {
        return getSceneFrame(currentFrame, getCurrentBlock(currentFrame));
    }

    /**
     * Finding a sync frame number given an eye frame number.  The function is
     * not optimized so it will go through sync points linearly.
     */
    public int eyeFrameToSyncFrame(int eyeFrame) {
        /** Sanity check */
        if (eyeFrame < 1) {
            return -1;
        }

        for (int i = 0; i < syncBlocks.length; i++) {
            SyncBlock syncBlock = syncBlocks[i];
            if (syncBlock.startEyeFrame <= eyeFrame && syncBlock.endEyeFrame >= eyeFrame) {
                return eyeFrame - syncBlock.startEyeFrame + syncBlock.start;
            }
        }
        // This should not be reached
        return -1;
    }

    /**
     * Finding a sync frame number given an scene frame number.  The function is
     * not optimized so it will go through sync points linearly.
     */
    public int sceneFrameToSyncFrame(int sceneFrame) {
        /** Sanity check */
        if (sceneFrame < 1) {
            return -1;
        }

        for (int i = 0; i < syncBlocks.length; i++) {
            SyncBlock syncBlock = syncBlocks[i];
            if (syncBlock.startSceneFrame <= sceneFrame && syncBlock.endSceneFrame >= sceneFrame) {
                return sceneFrame - syncBlock.startSceneFrame + syncBlock.start;
            }
        }
        // This should not be reached
        return -1;
    }
}
