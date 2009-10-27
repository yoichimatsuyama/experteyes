/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eyetrackercalibrator.math;

import java.awt.Dimension;
import java.awt.geom.Point2D;

/**
 * This class helps compute degree error e given a correct point and a
 * projected point
 * <pre>
 *        /| Projected point
 *       / |
 *      /  |
 *     /e  |
 * eye ---- Correct point
 * </pre>
 * @author ruj
 */
public class DegreeErrorComputer {

    private double cmPerPixWidth;
    private double cmPerPixHeight;

    public double getDistanceFromSceneCM() {
        return distanceFromSceneCM;
    }

    public void setDistanceFromSceneCM(double distanceFromSceneCM) {
        this.distanceFromSceneCM = distanceFromSceneCM;
        computeConstants();
    }

    public double getSceneHeightCM() {
        return sceneHeightCM;
    }

    public void setSceneHeightCM(double sceneHeightCM) {
        this.sceneHeightCM = sceneHeightCM;
        computeConstants();
    }

    public double getSceneWidthCM() {
        return sceneWidthCM;
    }

    public void setSceneWidthCM(double sceneWidthCM) {
        this.sceneWidthCM = sceneWidthCM;
        computeConstants();
    }

    public Dimension getScreendDimensionPixel() {
        return sceneDimensionPixel;
    }

    public void setScreendDimensionPixel(Dimension screendDimensionPixel) {
        this.sceneDimensionPixel = screendDimensionPixel;
        computeConstants();
    }

    /**
     * To use this class you have to provide some real measurement.  Given
     * a scene from a scene camera, what is the distance from a camera to
     * the scene and its the width and height of the scene captured by the
     * camera
     */
    public DegreeErrorComputer(Dimension sceneDimensionPixel,
            double distanceFromSceneCM, double sceneWidthCM,
            double sceneHeightCM) {
        this.sceneDimensionPixel = sceneDimensionPixel;
        this.distanceFromSceneCM = distanceFromSceneCM;
        this.sceneWidthCM = sceneWidthCM;
        this.sceneHeightCM = sceneHeightCM;
        computeConstants();
    }
    Dimension sceneDimensionPixel;
    double distanceFromSceneCM;
    double sceneWidthCM;
    double sceneHeightCM;

    private void computeConstants() {
        // Sanity check
        if (sceneDimensionPixel.height <= 0 ||
                sceneDimensionPixel.width <= 0 ||
                distanceFromSceneCM <= 0 ||
                sceneWidthCM <= 0 || sceneHeightCM <= 0) {
            cmPerPixWidth = 0;
            cmPerPixHeight = 0;
        }

        // First find cm length per pixel in scene
        cmPerPixWidth = sceneWidthCM / sceneDimensionPixel.getWidth();
        cmPerPixHeight = sceneHeightCM / sceneDimensionPixel.getHeight();
    }

    /**
     * Compute a degree error e given a correct point and an eye gaze point
     * <pre>
     *        /| Projected point
     *       / |
     *      /  |
     *     /e  |
     * eye ---- Correct point
     * </pre>
     * @return degree error in degree unit
     */
    public double degreeError(Point2D correctPoint, Point2D eyeGazePoint) {
        // Compute error in cm
        double widthError = (eyeGazePoint.getX() - correctPoint.getX()) * cmPerPixWidth;
        double heightError = (eyeGazePoint.getY() - correctPoint.getY()) * cmPerPixHeight;
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
}
