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
package logic;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * 
 * @author ruj
 */
public class PointToFrameByEstimatedPupilLocation {

    private HashMap<Point, LinkedList<Info>> gridToInfoList =
            new HashMap<Point, LinkedList<Info>>();
    private double gridSize = 50;
    ChangeListener listener = null;

    public double getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    private class Info {

        public Info(Double p, int frameNumber) {
            this.p = p;
            this.frameNumber = frameNumber;
        }
        public Point2D.Double p;
        public int frameNumber;
    }
    final private static int FRAME_NUM_POSITON = 0;
    final private static int X_POSITON = 1;
    final private static int Y_POSITON = 2;

    public void loadFromFile(File inputFile) throws IOException {
        // Clear old values
        this.gridToInfoList.clear();

        RandomAccessFile in = new RandomAccessFile(inputFile, "r");
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            String[] token = line.split("\t");
            Point2D.Double p = new Point2D.Double(
                    java.lang.Double.parseDouble(token[X_POSITON]),
                    java.lang.Double.parseDouble(token[Y_POSITON]));
            Point gridP = toGridPoint(p);
            Info info = new Info(p, Integer.parseInt(token[FRAME_NUM_POSITON]));
            LinkedList<Info> list = this.gridToInfoList.get(gridP);
            if (list == null) {
                list = new LinkedList<Info>();
                this.gridToInfoList.put(gridP, list);
            }
            list.add(info);
        }
        in.close();
    }

    public void save(File outputFile) throws FileNotFoundException {
        PrintStream out = new PrintStream(outputFile);
        for (Iterator<LinkedList<Info>> allListIt =
                this.gridToInfoList.values().iterator();
                allListIt.hasNext();) {

            for (Iterator<Info> it = allListIt.next().iterator(); it.hasNext();) {
                Info info = it.next();
                out.println(info.frameNumber + "\t" + info.p.x + "\t" + info.p.y);
            }
        }

        out.close();
    }

    private Point toGridPoint(Point2D originalPoint) {
        Point p = new Point();
        p.setLocation(originalPoint.getX() / this.gridSize,
                originalPoint.getY() / this.gridSize);
        return p;
    }

    /**
     * Estimates fram pupil location on given frame array.
     * @param frames input frames to be processed
     * @param pupilThreshold pupil threshold to be used across all frames
     * @param searchRect Area to search for pupil
     * @param frameSampling Sampling rate.  1 mean compute all fram n mean compute
     * every n frames. The frame will be looked at when 
     * frames array position % frameSampling = 0
     */
    public void loadFrames(File[] frames, int pupilThreshold,
            Rectangle searchRect, int frameSampling){
        
        double[] size = new double[frames.length];
        Point2D.Double[] pupil = new Double[frames.length];

        // Make sure that we don't get negative
        frameSampling = Math.max(1, frameSampling);

        double sumPupilSize = 0d;
        double totalForAverageCount = 0d;
        for (int i = 0; i < frames.length; i++) {
            if (i % frameSampling == 0) {
                BufferedImage paintedImg = ImageUtils.loadRGBImage(frames[i]);

                // Get pupil estimate
                Ellipse2D foundPupil = FitEyeModel.findPupil(paintedImg,
                        searchRect, pupilThreshold, false);

                if (foundPupil != null) {
                    pupil[i] = new Point2D.Double(foundPupil.getCenterX(),
                            foundPupil.getCenterY());
                    size[i] = foundPupil.getWidth() * foundPupil.getHeight();
                    sumPupilSize += size[i];
                    totalForAverageCount++;
                } else {
                    pupil[i] = null;
                    size[i] = -1;
                }
            }
            if (this.listener != null) {
                this.listener.stateChanged(new ChangeEvent(this));
            }
        }
        // Try filtering if any
        if (totalForAverageCount > 0) {
            boolean[] filter = filterBlink(size,
                    sumPupilSize / totalForAverageCount, frameSampling);
            for (int i = 0; i < filter.length; i++) {
                if (i % frameSampling == 0 && filter[i] && pupil[i] != null) {
                    // Add to lookup
                    Point p = toGridPoint(pupil[i]);

                    LinkedList<Info> list = gridToInfoList.get(p);
                    if (list == null) {
                        list = new LinkedList<Info>();
                        gridToInfoList.put(p, list);
                    }
                    list.add(new Info(pupil[i], i));
                }
            }
        }
    }

    /**
     * This method uses 2 mean clustering to help classify 
     */
    private boolean[] filterBlink(double size[], double sizeSample, int samplingRate) {
        boolean[] filter = new boolean[size.length];
        double pupilAvgSize = sizeSample;
        double blinkAvgSize = Math.sqrt(sizeSample);
        double changes = 1d;
        int round = 1;
        do {
            double sumPupilSize = 0d;
            double totalPupil = 0d;
            double sumBlinkSize = 0d;
            double totalBlink = 0d;
            // Loop computing group
            for (int i = 0; i < size.length; i++) {
                if (i % samplingRate == 0 && size[i] >= 0) {
                    filter[i] = Math.abs(size[i] - pupilAvgSize) <=
                            Math.abs(size[i] - blinkAvgSize);
                    if (filter[i]) {
                        // THis size belong to pupil
                        sumPupilSize += size[i];
                        totalPupil++;
                    } else {
                        sumBlinkSize += size[i];
                        totalBlink++;
                    }
                }
            }
            // Adjust new pupil;
            if (totalPupil > 0) {
                double newPupilAvgSize = sumPupilSize / totalPupil;
                changes = Math.abs(pupilAvgSize - newPupilAvgSize);
                pupilAvgSize = newPupilAvgSize;
            }

            if (totalBlink > 0) {
                double newBlinkAvgSize = sumBlinkSize / totalBlink;
                changes = Math.min(changes,
                        Math.abs(blinkAvgSize - newBlinkAvgSize));
                blinkAvgSize = newBlinkAvgSize;
            }
        } while (changes > 0.001 && round < 10000);
        return filter;
    }

    /**
     * Get nearest frame by looking at the closest pupil estimation point.
     * The tie is broken with the distance in frame number.
     * @return frame -1 when no frame can be found
     */
    public int getNearestFrame(int currentFrame, Point2D currentPoint) {
        int frame = -1;

        // Find what grid this point belong to
        Point select = toGridPoint(currentPoint);

        Info closest = null;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                Point p = new Point(select.x + x, select.y + y);

                LinkedList<Info> list = gridToInfoList.get(p);

                if (list != null && !list.isEmpty()) {

                    if (closest == null) {
                        closest = list.getFirst();
                    }

                    double distance = currentPoint.distance(closest.p);

                    for (Iterator<Info> it = list.iterator(); it.hasNext();) {
                        Info info = it.next();

                        double newDistance = currentPoint.distance(info.p);

                        if (newDistance < distance) {
                            closest = info;
                        } else if (newDistance == distance) {
                            if (Math.abs(info.frameNumber - currentFrame) <
                                    Math.abs(closest.frameNumber - currentFrame)) {
                                closest = info;
                            }
                        }
                    }


                }

            }
        }
        if (closest != null) {
            frame = closest.frameNumber;
        }
        return frame;
    }

    public void setLoadingListener(ChangeListener listener) {
        this.listener = listener;
    }

    public void clear() {
        this.gridToInfoList.clear();
    }
}
