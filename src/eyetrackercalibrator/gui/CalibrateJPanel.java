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
 * NewJPanel.java
 *
 * Created on October 29, 2008, 1:35 PM
 */
package eyetrackercalibrator.gui;

import eyetrackercalibrator.ImageTools;
import eyetrackercalibrator.framemanaging.EyeViewFrameInfo;
import eyetrackercalibrator.framemanaging.FrameManager;
import eyetrackercalibrator.framemanaging.FrameSynchronizor;
import eyetrackercalibrator.framemanaging.ScreenFrameManager;
import eyetrackercalibrator.framemanaging.ScreenViewFrameInfo;
import eyetrackercalibrator.gui.CalibrationInfo.CalibrationType;
import eyetrackercalibrator.gui.util.AnimationTimer;
import eyetrackercalibrator.gui.util.CompletionListener;
import eyetrackercalibrator.gui.util.GUIUtil;
import eyetrackercalibrator.gui.util.IntervalMarkerManager;
import eyetrackercalibrator.math.CalibrateEyeGaze;
import eyetrackercalibrator.math.CalibrateEyeGazeQR;
import eyetrackercalibrator.math.Computation;
import eyetrackercalibrator.math.DegreeErrorComputer;
import eyetrackercalibrator.math.EyeGazeComputing;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.vecmath.Point3i;
import org.jdesktop.layout.GroupLayout;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;

/**
 *
 * @author  ruj
 */
public class CalibrateJPanel extends javax.swing.JPanel {

    public static final String[] FITTING_EQUATION_CHOICE = new String[]{"2nd degree", "3rd degree"};
    public static int TOTAL_CALIBRATION_X = 5;
    public static int TOTAL_CALIBRATION_Y = 5;
    protected static final String CALIBRATE_POINT_ELEMENT = "calibrate";
    protected static final String CALIBRATE_POINT_X_ATTRIBUTE = "x";
    protected static final String CALIBRATE_POINT_Y_ATTRIBUTE = "y";
    protected static final String EYEGAZECOEFF_ELEMENT = "eyegazecoeff";
    protected static final String SECONDARYEYEGAZECOEFF_ELEMENT = "secondaryeyegazecoeff";
    protected static final String COEFF_ATTRIBUTE = "c";
    protected static final String FITTING_EQUATION_ATTRIBUTE = "equation";
    protected static final String LINEAREYEGAZEINTERPOLATIONRANGE_ELEMENT = "lineareyegazeinterpolationrange";
    protected static final String LINEAREYEGAZEINTERPOLATIONRANGE_FROM_ATTRIBUTE = "from";
    protected static final String LINEAREYEGAZEINTERPOLATIONRANGE_TO_ATTRIBUTE = "to";
    protected static final String RANGE_ELEMENT = "range";
    protected static final String RANGE_ISCALIBRATIONPOINTPOSITIONLOCATED_ATTRIBUTE = "iscalibrationpointpositionlocated";
    protected static final String RANGE_STARTSCREENFRAME_ATTRIBUTE = "startscreenframe";
    protected static final String RANGE_POINTTYPE_ATTRIBUTE = "pointtype";
    protected static final String RANGE_SELECTEDX_ATTRIBUTE = "selectedx";
    protected static final String RANGE_SELECTEDY_ATTRIBUTE = "selectedy";
    protected static final String RANGE_STARTEYEFRAME_ATTRIBUTE = "starteyeframe";
    protected static final String RANGE_STOPEYEFRAME_ATTRIBUTE = "stopeyeframe";
    protected static final String RANGE_STOPSCREENFRAME_ATTRIBUTE = "stopscreenframe";
    protected static final String SCREENPOINT_ELEMENT = "screenpoint";
    protected static final String SCREENPOINT_FRAME_ATTRIBUTE = "frame";
    protected static final String SCREENPOINT_X_ATTRIBUTE = "x";
    protected static final String SCREENPOINT_Y_ATTRIBUTE = "y";
    protected static final String XCOEFF_ELEMENT = "xcoeff";
    protected static final String YCOEFF_ELEMENT = "ycoeff";
    double[][][] eyeGazeCoefficient = null;
    Point[] screenViewMark = null;
    Point2D.Double selectedPoint = null;
    // Set of calibration list set for each calibration point
    DefaultListModel[] calibrationSet = new DefaultListModel[TOTAL_CALIBRATION_X * TOTAL_CALIBRATION_Y];
    // Current calibration point
    int calibrationIndex = 0;
    FrameInfoGraphTabPanel graphTabPanel = null;
    private Dimension2D fullScreenDim = null;
    private AnimationTimer timer;
    private ZoomJPanel zoomPanel = null;
    boolean isZoomingScreen = true;
    IntervalMarkerManager intervalMarkerManager = null;
    /** 
     * Current calibration info.  This is needed since there are many
     * method dealing with current calibration info
     */
    private CalibrationInfo currentCalibrationInfo = null;
    private File projectLocation;
    private String fullScreenFrameDirectory;
    private String eyeScreenInfoDirectory;
    private boolean isRunningCalibrationPointPositionFinding = false;
    private boolean isRunningDetectEyeInfo = false;
    /**
     * Holding current thread running calibration point set up
     */
    private CalibrationPointPositionFinderRunner calibrationPointPositionFinderRunner = null;
    private CalibrationType currentCalibrationType = CalibrationInfo.CalibrationType.Primary;
    private DegreeErrorComputer degreeErrorComputer;
    private boolean warnuser = true;
    private double[][] primaryEyeCoeff;
    private DriftCorrectionInfo driftCorrectionInfo;
    public DefaultListModel allDriftCorrectionSets = null;

    /** Creates new form NewJPanel */
    public CalibrateJPanel() {
        this.eyeGazeCoefficient = new double[2][][];

        // Create empty calibration storage
        for (int i = 0; i
                < calibrationSet.length; i++) {
            calibrationSet[i] = new DefaultListModel();
        }

        initComponents();

        this.selectedPoint = new Point2D.Double();
        this.screenViewMark = new Point[1];
        this.screenViewMark[0] = new Point();

        // Create zoom panel
        this.zoomPanel = new ZoomJPanel();
        this.zoomPanel.addDoneButtonActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doneZoomingActionPerformed(e);
            }
        });

        // Graph tab panel has to be added outside since group layout will prevent
        // JGraphPanel from displaying properly
        GroupLayout layout = (GroupLayout) getLayout();
        graphTabPanel = new FrameInfoGraphTabPanel();
        graphTabPanel.setFixVerticalSize(247);
        layout.replace(graphHolder, graphTabPanel);


        // Add listener to mouse click event on display
        displayJPanel.addEyeViewMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                eyeViewMouseClickHandle(e);
            }
        });
        displayJPanel.addScreenViewMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                screenViewMouseClickHandle(e);
            }
        });

        // Add listener to calibration point selector
        calibrationPointSelectorJPanel.addMarkingButtonActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handleCalibrationMarkingButtonActionPerformed(e);
            }
        });

        calibrationPointSelectorJPanel.addLabelMouseClickListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                calibrationPointSelectHandle(e);
            }
        });

        // Add listener to mouse click on graph
        graphTabPanel.addChartProgressListener(new ChartProgressListener() {

            @Override
            public void chartProgress(ChartProgressEvent chartProgressEvent) {
                if (chartProgressEvent.getType() == ChartProgressEvent.DRAWING_FINISHED) {
                    JFreeChart chart = chartProgressEvent.getChart();
                    if (chart != null) {
                        XYPlot plot = (XYPlot) chart.getPlot();
                        if (plot != null) {
                            graphTabMouseClickedHandle(plot.getDomainCrosshairValue());
                        }
                    }
                }
            }
        });

        // Add listener to frame scroll to detect frame change
        frameScrollingJPanel.addPropertyChangeListener(
                FrameScrollingJPanel.CURRENT_FRAME_CHANGE,
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        frameChangeHandler(evt);
                    }
                });

        // Set up animation timer
        timer = new AnimationTimer();
        timer.setDisplayJPanel(displayJPanel);
        timer.setEyeFrameScrollingJPanel(frameScrollingJPanel);
        timer.setScreenFrameScrollingJPanel(frameScrollingJPanel);

        intervalMarkerManager = new IntervalMarkerManager(graphTabPanel);
    }

    public void setProjectRoot(File projectLocation) {
        this.projectLocation = projectLocation;
    }

    public void setFullScreenFrameDirectory(String fullScreenFrameDirectory) {
        this.fullScreenFrameDirectory = fullScreenFrameDirectory;
    }

    public void setEyeScreenInfoDirectory(String eyeScreenInfoDirectory) {
        this.eyeScreenInfoDirectory = eyeScreenInfoDirectory;
    }

    private void doneZoomingActionPerformed(ActionEvent e) {
        if (isZoomingScreen) {
            // This zooming screen view
            // Get selected point from zooming screen
            final Point2D point = zoomPanel.getSelectedPoint();

            if (point == null) {
                // If no point is selected remind user to do that
                // Open dialog to warn user
                JOptionPane.showMessageDialog(zoomPanel,
                        "Please select a calibration by clicking on a calibration spot on the screen view.",
                        "No calibration point selected",
                        JOptionPane.ERROR_MESSAGE);

            } else {
                // Clear picture from zoom panel
                zoomPanel.setImage(null);
                // Swtich back to calibration
                Container container = zoomPanel.getParent();
                zoomPanel.setVisible(false);
                this.setVisible(true);
                container.remove(zoomPanel);
                container.add(this);
                repaint();

                // For testing lets just propagate the first frames coor to all
                final FrameManager screenFrameManager = timer.getScreenFrameManager();

                int startScreenFrame = this.currentCalibrationInfo.startSceneFrame;

                // Create array of screen view Files 
                ScreenViewFrameInfo screenInfo =
                        (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(startScreenFrame);

                if (screenInfo == null) {
                    screenInfo = new ScreenViewFrameInfo();
                }
                Point[] screenFocus = screenInfo.getMarkedPoints();
                if (screenFocus == null) {
                    // If there is no previous point, just create a new one
                    screenFocus = new Point[1];
                    screenFocus[0] = new Point();
                }
                // Set focus location
                screenFocus[0].setLocation(point);
                screenInfo.setMarkedPoints(screenFocus);

                // Put back to database
                screenFrameManager.setFrameInfo(startScreenFrame, screenInfo);

                final CalibrationInfo calibrationInfo = this.currentCalibrationInfo;

                // Set user selected point type
                calibrationInfo.calibrationType = this.currentCalibrationType;

                //Set user selected point
                calibrationInfo.selectedCalibrationPointPosition = point;

                // Add point to proper calibration
                calibrationSet[calibrationIndex].addElement(calibrationInfo);

                updateCalibrationStoredLabel(calibrationIndex);

                this.currentCalibrationInfo = null;

            }
        } else {
            // Deal with zooming something else (probably eye )
        }
    }

    /**
     * Handle when current frame is changed in Frame scrolling manager
     */
    private void frameChangeHandler(PropertyChangeEvent evt) {
        Integer frame = (Integer) evt.getNewValue();
        // Set the graph to point to correct frame
        graphTabPanel.setCurrentCrossHairPosition(frame.doubleValue());

        // Move marker if there is one
        if (this.currentCalibrationInfo != null) {
            this.currentCalibrationInfo.setEndFrame(
                    frame, timer.getFrameSynchronizor().getEyeFrame(frame),
                    timer.getFrameSynchronizor().getSceneFrame(frame));
        }
    }

    // Handle when calibration point selector is select
    private void calibrationPointSelectHandle(MouseEvent e) {
        // get selected calibration point index
        this.calibrationIndex =
                this.calibrationPointSelectorJPanel.getLabelIndex(e.getSource());

        // Switch to the list
        calibrateList.setModel(calibrationSet[this.calibrationIndex]);
    }

    private void eyeViewMouseClickHandle(MouseEvent e) {
    }

    private void handleCalibrationMarkingButtonActionPerformed(ActionEvent evt) {
        // Start marking
        JToggleButton button = (JToggleButton) evt.getSource();
        if (button.isSelected()) {
            // Set text to stop marking
            this.calibrationIndex =
                    this.calibrationPointSelectorJPanel.getMarkingButtonIndex(evt.getSource());
            // Set proper selection index
            this.calibrationPointSelectorJPanel.setSelectedLabel(this.calibrationIndex);
            // Switch to proper calubration list
            calibrateList.setModel(calibrationSet[this.calibrationIndex]);
            // Change type to the currently viewing type
            CalibrationPointSelectorJPanel.CalibrationType viewType =
                    this.calibrationPointSelectorJPanel.getCurrentView();

            switch (viewType) {
                case PRIMARY:
                    this.primaryCalibrationPointsRadioButton.setSelected(true);
                    this.currentCalibrationType = CalibrationType.Primary;
                    break;
                case SECONDARY:
                    this.secondaryCalibrationPointsRadioButton.setSelected(true);
                    this.currentCalibrationType = CalibrationType.Secondary;
                    break;
                case TEST:
                    this.testingPointsRadioButton.setSelected(true);
                    this.currentCalibrationType = CalibrationType.Testing;
                    break;
            }

            // Block other buttons
            calibrateButton.setEnabled(false);
            backButton.setEnabled(false);
            deleteButton.setEnabled(false);
            // Start recording information
            startRecording();
        } else {
            // Reenable other buttons
            calibrateButton.setEnabled(true);
            backButton.setEnabled(true);
            deleteButton.setEnabled(true);
            // Save information
            stopRecording();
        }
    }

    private void screenViewMouseClickHandle(MouseEvent e) {
    }

    /** For starting animation */
    public void start() {
        timer.start();
    }

    /**
     * Handle mouse click on graph by moving the screen to a frame selected
     */
    private void graphTabMouseClickedHandle(double x) {
        int frameNumber = (int) x;
        // Don't do anything if 0 or less
        if (frameNumber > 0) {
            // Set current frame number
            frameScrollingJPanel.setCurrentFrame(frameNumber);
        }
    }

    /**
     * Add actionlistener to process "Back" commands
     * @param listener 
     */
    public void addActionListener(ActionListener listener) {
        backButton.addActionListener(listener);
    }

    /**
     * Helper for updating number of calibration points selected.
     */
    private void updateCalibrationStoredLabel(int index) {
        // Compute current number
        int totalPrimeCal = 0;
        int totalSecondaryCal = 0;
        int totalTest = 0;

        for (Enumeration en = calibrationSet[index].elements();
                en.hasMoreElements();) {
            // Create element
            CalibrationInfo info = (CalibrationInfo) en.nextElement();

            int amount = info.stopSceneFrame - info.startSceneFrame + 1;

            switch (info.calibrationType) {
                case Testing:
                    totalTest += amount;
                    break;
                case Secondary:
                    totalSecondaryCal += amount;
                    break;
                default:
                    totalPrimeCal += amount;
            }
        }

        // Set label accordingly
        this.calibrationPointSelectorJPanel.setLabelText(
                index, CalibrationPointSelectorJPanel.CalibrationType.PRIMARY,
                String.valueOf(totalPrimeCal));
        this.calibrationPointSelectorJPanel.setLabelText(
                index, CalibrationPointSelectorJPanel.CalibrationType.SECONDARY,
                String.valueOf(totalSecondaryCal));
        this.calibrationPointSelectorJPanel.setLabelText(
                index, CalibrationPointSelectorJPanel.CalibrationType.TEST,
                String.valueOf(totalTest));
    }

    private void setCalibrationType(CalibrationInfo.CalibrationType type) {
        // Set current calibration type
        this.currentCalibrationType = type;

        // Propagate
        Object[] select = calibrateList.getSelectedValues();
        for (int i = 0; i < select.length; i++) {
            CalibrationInfo object = (CalibrationInfo) select[i];
            object.calibrationType = type;
        }

        // Set label accordingly
        updateCalibrationStoredLabel(this.calibrationIndex);
        //update to allow for drift correct
        updateDriftCorrectionOffsets();
        repaint();
    }

    private synchronized void enableButtons(Component excempt, boolean b) {
        // Disable all other component if all process are cleared
        if (!this.isRunningCalibrationPointPositionFinding
                && !this.isRunningDetectEyeInfo) {
            GUIUtil.setEnableAllCompoenentsExcept(controlPanel, excempt, b);
        }

    }

    /**
     * Helper for handling calibration point positions finding completion
     * Only deals with cosmatic changes to the CalibraJPanel.  For data change
     * CalibrationPointPositionFinderRunner class will be the handler
     */
    private void calibrationPointPositionFindingCompleteHandler() {
        // Reset the flag
        this.isRunningCalibrationPointPositionFinding = false;

        // Clear calibration finder runner
        this.calibrationPointPositionFinderRunner = null;

        // Re enable all buttons
        enableButtons(locateCalibrationPointsPositionsButton, true);

        // Change text to start
        locateCalibrationPointsPositionsButton.setText("Start");

        // Cause information panel to update display
        calibrateList.repaint();
         //update to allow for drift correct
        updateDriftCorrectionOffsets();

    }

    private void startRecording() {
        int currentFrame = frameScrollingJPanel.getCurrentFrame();

        // Create calibration info
        CalibrationInfo calibrationInfo = new CalibrationInfo();

        // Setting marker
        calibrationInfo.setIntervalMarker(
                intervalMarkerManager.getNewIntervalMarker());

        // Setting frame
        calibrationInfo.setStartFrame(currentFrame,
                this.timer.getFrameSynchronizor().getEyeFrame(currentFrame),
                this.timer.getFrameSynchronizor().getSceneFrame(currentFrame));

        calibrationInfo.setEndFrame(currentFrame,
                this.timer.getFrameSynchronizor().getEyeFrame(currentFrame),
                this.timer.getFrameSynchronizor().getSceneFrame(currentFrame));

        // Setting that this is new
        calibrationInfo.isCalibrationPointPositionLocated = false;

        // Remeber current calibration
        this.currentCalibrationInfo = calibrationInfo;
    }

    private void stopRecording() {
        // Defer adding calibration point to when zoom panel return
        // see doneZoomingActionPerformed method;
        //Launch zoom on the first frame of the range
        this.isZoomingScreen = true;

        // Load full screen view image for zoom panel
        String fileName = timer.getScreenFrameManager().getFrameFileName(
                this.currentCalibrationInfo.startSceneFrame);

        if (fileName != null) {
            File fullScreenImageFile = new File(this.fullScreenFrameDirectory, fileName);
            BufferedImage image = null;

            if (fullScreenImageFile.exists()) {
                image = ImageTools.loadImage(fullScreenImageFile);
            } else {
                // Default to normal image
                image = timer.getScreenFrameManager().getFrame(
                        this.currentCalibrationInfo.startSceneFrame);
            }

            zoomPanel.setImage(image);

            // Swtich to zoom screen
            zoomPanel.setVisible(true);
            this.setVisible(false);
            Container container = getParent();
            container.remove(this);
            container.add(zoomPanel);
            zoomPanel.repaint();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Screen image file for frame "
                    + this.currentCalibrationInfo.startSceneFrame
                    + " is missing", "Cannot mark calibration",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void stop() {
        timer.stop();
//        isCalibrationPointSelected = true;
    }

    /** 
     * Require setOffset to be called first to make the scrolling panel working
     * properly
     */
    public void setEyeFrameManager(FrameManager eyeFrameManager) {
        // Register framemanager to animation timer
        timer.setEyeFrameManager(eyeFrameManager);

        // Register framemanager to graph display
        graphTabPanel.setEyeFrameManager(eyeFrameManager);

        // Reset viewing position
        frameScrollingJPanel.setCurrentFrame(1);
    }

    /** 
     * Require setOffset to be called first to make the scrolling panel working
     * properly
     */
    public void setScreenFrameManager(ScreenFrameManager screenFrameManager) {
        timer.setScreenFrameManager(screenFrameManager);

        // Register framemanager to graph display
        graphTabPanel.setScreenFrameManager(screenFrameManager);

        // Reset viewing position
        frameScrollingJPanel.setCurrentFrame(1);
    }

    /**  Set frame sync and total frames*/
    public void setFrameSynchronizor(FrameSynchronizor frameSynchronizor) {
        this.timer.setFrameSynchronizor(frameSynchronizor);
        this.graphTabPanel.setFrameSynchronizor(frameSynchronizor);
        this.frameScrollingJPanel.setTotalFrame(frameSynchronizor.getTotalFrame());
    }

    /**
     * Load calibration points from file
     * @param calibrationFile If null, empty calibration will be created
     */
    public void loadCalibrationPoints(File calibrationFile) {
        // Clear previous info
        clearCalibrationInfo();

        if (calibrationFile != null) {
            ScreenFrameManager screenFrameManager = timer.getScreenFrameManager();

            // Load from file
            SAXBuilder builder = new SAXBuilder();
            Element root = null;
            try {
                Document doc = builder.build(calibrationFile);
                root =
                        doc.getRootElement();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            // Get saved eye gaze coefficient
            Element eyeGazeElement = root.getChild(EYEGAZECOEFF_ELEMENT);
            if (eyeGazeElement != null) {
                loadEyegazeCoeff(eyeGazeElement, 0);
            } else {
                eyeGazeCoefficient[0] = null;
            }
            // Set it to display
            timer.getEyeGazeComputing().setPrimaryEyeCoeff(eyeGazeCoefficient[0]);

            eyeGazeElement = root.getChild(SECONDARYEYEGAZECOEFF_ELEMENT);
            if (eyeGazeElement != null) {
                loadEyegazeCoeff(eyeGazeElement, 1);
            } else {
                eyeGazeCoefficient[1] = null;
            }
            // Set it to display
            timer.getEyeGazeComputing().setSecondaryEyeCoeff(eyeGazeCoefficient[1]);

            // Load linear interpolation range
            int from = 0;
            int to = 0;
            Element linearInterpolationRangeElement =
                    root.getChild(LINEAREYEGAZEINTERPOLATIONRANGE_ELEMENT);
            if (linearInterpolationRangeElement != null) {
                from = Integer.parseInt(
                        linearInterpolationRangeElement.getAttributeValue(LINEAREYEGAZEINTERPOLATIONRANGE_FROM_ATTRIBUTE));
                to = Integer.parseInt(
                        linearInterpolationRangeElement.getAttributeValue(LINEAREYEGAZEINTERPOLATIONRANGE_TO_ATTRIBUTE));
            }

            timer.getEyeGazeComputing().setLinearComputingParameters(from, to);

            // Get calibrate points
            for (Iterator it = root.getChildren(CALIBRATE_POINT_ELEMENT).iterator(); it.hasNext();) {
                Element calibration = (Element) it.next();
                int x = Integer.parseInt(calibration.getAttributeValue(CALIBRATE_POINT_X_ATTRIBUTE));
                int y = Integer.parseInt(calibration.getAttributeValue(CALIBRATE_POINT_Y_ATTRIBUTE));
                int pos = this.calibrationPointSelectorJPanel.pointToIndex(x, y);

                for (Iterator rangeIt = calibration.getChildren().iterator(); rangeIt.hasNext();) {
                    Element rangeElem = (Element) rangeIt.next();

                    int startEyeFrame = Integer.parseInt(
                            rangeElem.getAttributeValue(RANGE_STARTEYEFRAME_ATTRIBUTE));

                    int stopEyeFrame = Integer.parseInt(
                            rangeElem.getAttributeValue(RANGE_STOPEYEFRAME_ATTRIBUTE));

                    // Populate screenInfo database with screenFocus
                    for (Iterator pointIt = rangeElem.getChildren().iterator();
                            pointIt.hasNext();) {
                        Element pointElem = (Element) pointIt.next();

                        int frame = Integer.parseInt(
                                pointElem.getAttributeValue(SCREENPOINT_FRAME_ATTRIBUTE));

                        Point p = new Point();

                        ScreenViewFrameInfo screenInfo =
                                (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(frame);
                        if (screenInfo == null) {
                            screenInfo = new ScreenViewFrameInfo();
                        }

                        Point[] screenFocus = screenInfo.getMarkedPoints();
                        if (screenFocus == null) {
                            // If there is no previous point, just create a new one
                            screenFocus = new Point[1];
                            screenFocus[0] = new Point();
                        }
                        // Set focus location
                        screenFocus[0].setLocation(
                                Integer.parseInt(pointElem.getAttributeValue(SCREENPOINT_X_ATTRIBUTE)),
                                Integer.parseInt(pointElem.getAttributeValue(SCREENPOINT_Y_ATTRIBUTE)));
                        screenInfo.setMarkedPoints(screenFocus);

                        // Put back to database
                        screenFrameManager.setFrameInfo(frame, screenInfo);
                    }

                    Point2D.Double userSelectedCalibrationPosition = new Point2D.Double(
                            Double.parseDouble(rangeElem.getAttributeValue(RANGE_SELECTEDX_ATTRIBUTE)),
                            Double.parseDouble(rangeElem.getAttributeValue(RANGE_SELECTEDY_ATTRIBUTE)));

                    boolean iscalibrationpointpositionlocated =
                            Boolean.parseBoolean(
                            rangeElem.getAttributeValue(RANGE_ISCALIBRATIONPOINTPOSITIONLOCATED_ATTRIBUTE));

                    int startScreenFrame = Integer.parseInt(
                            rangeElem.getAttributeValue(RANGE_STARTSCREENFRAME_ATTRIBUTE));
                    int stopScreenFrame = Integer.parseInt(
                            rangeElem.getAttributeValue(RANGE_STOPSCREENFRAME_ATTRIBUTE));

                    String pointTypeString = rangeElem.getAttributeValue(RANGE_POINTTYPE_ATTRIBUTE);
                    CalibrationInfo.CalibrationType pointType =
                            CalibrationInfo.CalibrationType.Primary;
                    if (pointTypeString != null) {
                        pointType = CalibrationInfo.CalibrationType.valueOf(pointTypeString);
                    }

                    CalibrationInfo info = new CalibrationInfo(
                            startEyeFrame, stopEyeFrame,
                            startScreenFrame, stopScreenFrame,
                            userSelectedCalibrationPosition,
                            iscalibrationpointpositionlocated,
                            pointType);
                    // Add point to proper calibration
                    calibrationSet[pos].addElement(info);

                    // Set interval marking
                    IntervalMarker marker = intervalMarkerManager.getNewIntervalMarker();
                    info.setIntervalMarker(marker);
                    FrameSynchronizor synchronizor = this.timer.getFrameSynchronizor();
                    int frame = -1;
                    if (startEyeFrame >= 0) {
                        frame = synchronizor.eyeFrameToSyncFrame(startEyeFrame);
                    } else {
                        frame = synchronizor.sceneFrameToSyncFrame(startScreenFrame);
                    }
                    marker.setStartValue(frame);
                    if (stopEyeFrame >= 0) {
                        frame = synchronizor.eyeFrameToSyncFrame(stopEyeFrame);
                    } else {
                        frame = synchronizor.sceneFrameToSyncFrame(stopScreenFrame);
                    }
                    marker.setEndValue(frame);
                }
            }

            // Set stored calibration
            for (int i = 0; i < this.calibrationSet.length; i++) {
                updateCalibrationStoredLabel(i);

            }

            // Display current selection
            calibrateList.setModel(calibrationSet[this.calibrationIndex]);
        }
                updateDriftCorrectionOffsets();

    }

    private void loadEyegazeCoeff(Element eyeGazeElement, int i) {
        // Turn off warning temporary
        boolean currentSetting = this.warnuser;
        this.warnuser = false;

        String value = eyeGazeElement.getAttributeValue(FITTING_EQUATION_ATTRIBUTE);
        if (value != null && value.equals(FITTING_EQUATION_CHOICE[1])) {
            // This is 3rd degree
            this.fittingEquationChoiceComboBox.setSelectedIndex(1);
        } else {
            // default to 2 degree
            this.fittingEquationChoiceComboBox.setSelectedIndex(0);
        }

        Element xcoeffElement = eyeGazeElement.getChild(XCOEFF_ELEMENT);
        Element ycoeffElement = eyeGazeElement.getChild(YCOEFF_ELEMENT);
        this.eyeGazeCoefficient[i] = new double[2][];
        if (xcoeffElement != null) {

            List attributeList = xcoeffElement.getAttributes();

            double[] buffer = new double[attributeList.size()];
            for (int j = 0; j < buffer.length; j++) {
                buffer[j] = Double.parseDouble(xcoeffElement.getAttributeValue(COEFF_ATTRIBUTE + j));
            }

            this.eyeGazeCoefficient[i][0] = buffer;
        }

        if (ycoeffElement != null) {

            List attributeList = ycoeffElement.getAttributes();

            double[] buffer = new double[attributeList.size()];
            for (int j = 0; j < buffer.length; j++) {
                buffer[j] = Double.parseDouble(ycoeffElement.getAttributeValue(COEFF_ATTRIBUTE + j));
            }

            this.eyeGazeCoefficient[i][1] = buffer;
        }

        // Restore warning
        this.warnuser = currentSetting;
    }


    /*
     * Strategy: parse through all calibration sets and find the testing ones.
     * For each testing set, get the eye vector and project into the scene camera
     * to get the
     *
     */

    public DefaultListModel getAllDriftCorrectionSets(){
        return allDriftCorrectionSets;
    }

    private void updateDriftCorrectionOffsets() {

        //can't do anything if have no primary calibration

        this.primaryEyeCoeff = this.timer.getEyeGazeComputing().getPrimaryEyeCoeff();
        if (this.primaryEyeCoeff == null) {
            return;
        }

        //destroy old one if necessary
        allDriftCorrectionSets = new DefaultListModel();


        FrameManager eyeFrameManager = timer.getEyeFrameManager();
        FrameManager screenFrameManager = timer.getScreenFrameManager();

        for (int pos = 0; pos < this.calibrationSet.length; pos++) {
            // For each calibration add point
            for (Enumeration en = calibrationSet[pos].elements();
                    en.hasMoreElements();) {
                // Add calibration points
                CalibrationInfo info = (CalibrationInfo) en.nextElement();

                int screenFrame = info.startSceneFrame;
                int eyeFrame = info.startEyeFrame;

                Point2D.Double cumulativeError = new Point2D.Double(0, 0);
                int numPointsAccumulated = 0;

                while (screenFrame <= info.stopSceneFrame) {
                    // Get screen info
                    ScreenViewFrameInfo screenFrameInfo =
                            (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(screenFrame);

                    // Get corresponding eye vector
                    EyeViewFrameInfo eyeFrameInfo =
                            (EyeViewFrameInfo) eyeFrameManager.getFrameInfo(eyeFrame);

                    // Sanity check
                    if (screenFrameInfo != null && eyeFrameInfo != null
                            && screenFrameInfo.getMarkedPoints() != null) {
                        Point2D.Double calPoint = new Point2D.Double();
                        calPoint.setLocation(
                                screenFrameInfo.getMarkedPoints()[0]);

                        Point2D.Double eyeVecPoint = this.timer.getEyeGazeComputing().getEyeVector(eyeFrameInfo);

                        /*
                         * need to project into scene camera space using
                         * the PRIMARY calibration (and should only do this if we have one)
                         * make sure we don't actually correct for drift here!
                         */
                        Point2D.Double currentProjectedEyeGazePoint;

                        switch (info.calibrationType) {
                            case Testing:
                                //first get the (non drift corrected!) primary projection
                                this.primaryEyeCoeff = this.timer.getEyeGazeComputing().getPrimaryEyeCoeff();
                                if (this.primaryEyeCoeff != null) {
                                    // Compute eye gaze point
                                    currentProjectedEyeGazePoint = Computation.computeEyeGazePoint(eyeVecPoint.x, eyeVecPoint.y, this.primaryEyeCoeff);

                                    //should do some error checking beyond null
                                    if (currentProjectedEyeGazePoint != null) {
                                        double errorX = currentProjectedEyeGazePoint.x - calPoint.x;
                                        double errorY = currentProjectedEyeGazePoint.y - calPoint.y;
                                        cumulativeError.x = cumulativeError.x + errorX;
                                        cumulativeError.y = cumulativeError.y + errorY;
                                        numPointsAccumulated = numPointsAccumulated + 1;
                                    }


                                }
                                //next, compare it against the place where clicked in the scene view, in calPoint


                                // testList.add(calPoint);
                                // testEyeVecList.add(eyeVecPoint);
                                break;
                            case Secondary:
                                //don't do anything
                                break;
                            default:
                            //don't do anything
                        }
                    }

                    // Move to next frame in the range
                    screenFrame++;
                    eyeFrame++;
                }
                //now get average drift for this set of points
                if (numPointsAccumulated > 0) {
                    cumulativeError.x = cumulativeError.x / (double) numPointsAccumulated;
                    cumulativeError.y = cumulativeError.y / (double) numPointsAccumulated;
                    //now add the following variables to the list of drift correction points
                    /*
                     * info.startSceneFrame;
                     * info.startEyeFrame;
                     * cumulativeError
                     *
                     */
                    this.driftCorrectionInfo = new DriftCorrectionInfo(info.startEyeFrame, info.startSceneFrame, cumulativeError);

                    final DriftCorrectionInfo driftCorrectionSetInfo = this.driftCorrectionInfo;


                    //Set user selected point
                    //driftCorrectionSetInfo.cumulativeError = cumulativeError;

                    // Add point to proper calibration
                    allDriftCorrectionSets.addElement(driftCorrectionSetInfo);
                }
            }
        }
        //at this point we have accumulated all of the drift point sets
        //need to sort them 
                timer.getEyeGazeComputing().setAllDriftCorrectionSets(allDriftCorrectionSets);

    }


    /** 
     * Convert a screen frame number to a calibration point.
     * @return 0,0 if the frame is not a calibration frame. Otherwise return
     * the coordinate (top left is 1,1 and bottom right is TOTAL_CALIBRATION_X and TOTAL_CALIBRATION_X)
     */
    public class CalibationPoint {

        public Point location = new Point();
        public CalibrationType type = CalibrationType.Primary;
    }

    public CalibationPoint frameToCalibrationPoint(int screenFrameNumber) {
        CalibationPoint point = null;

        for (int x = 1; x <= TOTAL_CALIBRATION_X && point == null; x++) {
            for (int y = 1; y <= TOTAL_CALIBRATION_Y && point == null; y++) {
                int pos = this.calibrationPointSelectorJPanel.pointToIndex(x, y);
                for (Enumeration en = calibrationSet[pos].elements();
                        en.hasMoreElements();) {
                    // Create element
                    CalibrationInfo info = (CalibrationInfo) en.nextElement();
                    if (screenFrameNumber >= info.startSceneFrame
                            && screenFrameNumber <= info.stopSceneFrame) {

                        point = new CalibationPoint();
                        point.location.setLocation(x, y);
                        point.type = info.calibrationType;
                    }
                }
            }
        }
        return point;
    }

    /**
     * Save calibration points to file
     * The format is
     * <root>
     *   <calibration x='#' y='#' >
     *      <range starteyeframe='#' stopeyeframe='#' startscreenframe='#' stopscreenframe='#'>
     *         <screenpoint frame='#' x='#' y='#'/>
     *      </range>
     *      ...
     *   </calibration>
     * </root>
     * @param calibrationFile 
     */
    public void saveCalibrationPoints(File calibrationFile) {
        FrameManager screenFrameManager = timer.getScreenFrameManager();

        // Create a dom tree
        Element root = new Element("root");
        // Store eye gaze coefficient if any
        if (eyeGazeCoefficient[0] != null) {
            saveEyeCoeff(new Element(EYEGAZECOEFF_ELEMENT), root, eyeGazeCoefficient[0]);
        }
        if (eyeGazeCoefficient[1] != null) {
            saveEyeCoeff(new Element(SECONDARYEYEGAZECOEFF_ELEMENT), root, eyeGazeCoefficient[1]);
        }
        // Store linear interpolation range
        Element linearInterpolationRangeElement = new Element(LINEAREYEGAZEINTERPOLATIONRANGE_ELEMENT);
        linearInterpolationRangeElement.setAttribute(LINEAREYEGAZEINTERPOLATIONRANGE_FROM_ATTRIBUTE, String.valueOf(
                this.timer.getEyeGazeComputing().getLinearStartFrame()));
        linearInterpolationRangeElement.setAttribute(LINEAREYEGAZEINTERPOLATIONRANGE_TO_ATTRIBUTE, String.valueOf(
                this.timer.getEyeGazeComputing().getLinearLastFrame()));
        root.addContent(linearInterpolationRangeElement);

        Element calibrateElement = null;
        for (int x = 1; x <= TOTAL_CALIBRATION_X; x++) {
            for (int y = 1; y <= TOTAL_CALIBRATION_Y; y++) {
                calibrateElement = new Element(CALIBRATE_POINT_ELEMENT);
                calibrateElement.setAttribute(CALIBRATE_POINT_X_ATTRIBUTE, String.valueOf(x));
                calibrateElement.setAttribute(CALIBRATE_POINT_Y_ATTRIBUTE, String.valueOf(y));

                // For each calibration range add point
                int pos = this.calibrationPointSelectorJPanel.pointToIndex(x, y);
                for (Enumeration en = calibrationSet[pos].elements();
                        en.hasMoreElements();) {
                    // Create element
                    CalibrationInfo info = (CalibrationInfo) en.nextElement();
                    Element range = new Element(RANGE_ELEMENT);
                    range.setAttribute(RANGE_STARTEYEFRAME_ATTRIBUTE,
                            String.valueOf(info.startEyeFrame));
                    range.setAttribute(RANGE_STOPEYEFRAME_ATTRIBUTE,
                            String.valueOf(info.stopEyeFrame));
                    range.setAttribute(RANGE_STARTSCREENFRAME_ATTRIBUTE,
                            String.valueOf(info.startSceneFrame));
                    range.setAttribute(RANGE_STOPSCREENFRAME_ATTRIBUTE,
                            String.valueOf(info.stopSceneFrame));
                    range.setAttribute(RANGE_ISCALIBRATIONPOINTPOSITIONLOCATED_ATTRIBUTE,
                            String.valueOf(info.isCalibrationPointPositionLocated));
                    range.setAttribute(RANGE_POINTTYPE_ATTRIBUTE,
                            String.valueOf(info.calibrationType));
                    range.setAttribute(RANGE_SELECTEDX_ATTRIBUTE,
                            String.valueOf(info.selectedCalibrationPointPosition.getX()));
                    range.setAttribute(RANGE_SELECTEDY_ATTRIBUTE,
                            String.valueOf(info.selectedCalibrationPointPosition.getY()));
                    for (int i = info.startSceneFrame; i
                            <= info.stopSceneFrame; i++) {
                        ScreenViewFrameInfo screenInfo =
                                (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(i);
                        if (screenInfo != null) {
                            Point[] p = screenInfo.getMarkedPoints();
                            if (p != null) {
                                Element point = new Element(SCREENPOINT_ELEMENT);
                                point.setAttribute(SCREENPOINT_FRAME_ATTRIBUTE, String.valueOf(i));
                                point.setAttribute(SCREENPOINT_X_ATTRIBUTE, String.valueOf(p[0].x));
                                point.setAttribute(SCREENPOINT_Y_ATTRIBUTE, String.valueOf(p[0].y));
                                range.addContent(point);
                            }

                        }
                    }

                    // add to tree
                    calibrateElement.addContent(range);
                }
                // Add to tree
                root.addContent(calibrateElement);
            }

        }

        // Write out to file as xml
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(new Document(root), new FileWriter(calibrationFile));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void saveEyeCoeff(Element eyeGaze, Element root, double[][] coeff) {
        // Get current equation type
        int index = this.fittingEquationChoiceComboBox.getSelectedIndex();

        eyeGaze.setAttribute(FITTING_EQUATION_ATTRIBUTE, FITTING_EQUATION_CHOICE[index]);

        if (coeff[0] != null) {
            Element coeffElement = new Element(XCOEFF_ELEMENT);

            for (int i = 0; i < coeff[0].length; i++) {
                coeffElement.setAttribute(COEFF_ATTRIBUTE + i, String.valueOf(coeff[0][i]));
            }

            eyeGaze.addContent(coeffElement);
        }

        if (coeff[1] != null) {
            Element coeffElement = new Element(YCOEFF_ELEMENT);
            for (int i = 0; i < coeff[1].length; i++) {
                coeffElement.setAttribute(COEFF_ATTRIBUTE + i, String.valueOf(coeff[1][i]));
            }

            eyeGaze.addContent(coeffElement);
        }

        root.addContent(eyeGaze);
    }

    public Dimension2D getFullScreenTrueDim() {
        return fullScreenDim;
    }

    public void setFullScreenDim(Dimension2D fullScreenDim) {
        this.fullScreenDim = fullScreenDim;
    }

    /**
     * This function performs calibration and showing progress.
     */
    private void calibrate() {
        FrameManager eyeFrameManager = timer.getEyeFrameManager();
        FrameManager screenFrameManager = timer.getScreenFrameManager();

        // Get all calibration points and corresponding eye vectors
        LinkedList<Point2D.Double> primeCalList = new LinkedList<Point2D.Double>();
        LinkedList<Point2D.Double> secondaryCalList = new LinkedList<Point2D.Double>();
        LinkedList<Point2D.Double> primeEyeVecList = new LinkedList<Point2D.Double>();
        LinkedList<Point2D.Double> secondaryEyeVecList = new LinkedList<Point2D.Double>();
        LinkedList<Point2D.Double> testList = new LinkedList<Point2D.Double>();
        LinkedList<Point2D.Double> testEyeVecList = new LinkedList<Point2D.Double>();

        // Data to compute where linear interpolation starts and end
        int firstPrimaryFrame = eyeFrameManager.getTotalFrames();
        int lastPrimaryFrame = 0;
        int firstSecondaryFrame = firstPrimaryFrame;
        int lastSecondaryFrame = 0;

        for (int pos = 0; pos < this.calibrationSet.length; pos++) {
            // For each calibration add point
            for (Enumeration en = calibrationSet[pos].elements();
                    en.hasMoreElements();) {
                // Add calibration points
                CalibrationInfo info = (CalibrationInfo) en.nextElement();

                int screenFrame = info.startSceneFrame;
                int eyeFrame = info.startEyeFrame;
                while (screenFrame <= info.stopSceneFrame) {
                    // Get screen info
                    ScreenViewFrameInfo screenFrameInfo =
                            (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(screenFrame);

                    // Get corresponding eye vector
                    EyeViewFrameInfo eyeFrameInfo =
                            (EyeViewFrameInfo) eyeFrameManager.getFrameInfo(eyeFrame);

                    // Sanity check
                    if (screenFrameInfo != null && eyeFrameInfo != null
                            && screenFrameInfo.getMarkedPoints() != null) {
                        Point2D.Double calPoint = new Point2D.Double();
                        calPoint.setLocation(
                                screenFrameInfo.getMarkedPoints()[0]);

                        Point2D.Double eyeVecPoint = this.timer.getEyeGazeComputing().getEyeVector(eyeFrameInfo);

                        switch (info.calibrationType) {
                            case Testing:
                                testList.add(calPoint);
                                testEyeVecList.add(eyeVecPoint);
                                break;
                            case Secondary:
                                secondaryCalList.add(calPoint);
                                secondaryEyeVecList.add(eyeVecPoint);
                                firstSecondaryFrame = Math.min(firstSecondaryFrame, eyeFrame);
                                lastSecondaryFrame = Math.max(lastSecondaryFrame, eyeFrame);
                                break;
                            default:
                                primeCalList.add(calPoint);
                                primeEyeVecList.add(eyeVecPoint);
                                firstPrimaryFrame = Math.min(firstPrimaryFrame, eyeFrame);
                                lastPrimaryFrame = Math.max(lastPrimaryFrame, eyeFrame);
                        }
                    }

                    // Move to next frame in the range
                    screenFrame++;
                    eyeFrame++;
                }
            }
        }

        // Set linear interpolation range
        this.timer.getEyeGazeComputing().setLinearComputingParameters(
                (firstPrimaryFrame + lastPrimaryFrame) / 2,
                (lastSecondaryFrame + lastSecondaryFrame) / 2);

        // Convert to array for calibration
        final Point2D.Double[][] calArray = new Point2D.Double[2][];
        calArray[0] = primeCalList.toArray(new Point2D.Double[0]);
        calArray[1] = secondaryCalList.toArray(new Point2D.Double[0]);

        final Point2D.Double[] primeEyeVecArray =
                primeEyeVecList.toArray(new Point2D.Double[0]);
        final Point2D.Double[] secondaryEyeVecArray =
                secondaryEyeVecList.toArray(new Point2D.Double[0]);
        // Convert to array to testing results
        final Point2D.Double[] testArray = testList.toArray(new Point2D.Double[0]);
        final Point2D.Double[] testEyeVecArray = testEyeVecList.toArray(new Point2D.Double[0]);

        // Set up monitoring dialog and calibration system
        //final CalibratingViewPanel panel = new CalibratingViewPanel();
        final CalibratingViewJDialog panel =
                new CalibratingViewJDialog(new JFrame(), true);
        panel.setDegreeErrorComputer(this.degreeErrorComputer);

        final CalibrateEyeGaze primaryCalibrator;
        final CalibrateEyeGaze secondaryCalibrator;
        if (this.fittingEquationChoiceComboBox.getSelectedIndex() == 0) {
            // Do 2nd degree
            primaryCalibrator = new CalibrateEyeGazeQR(CalibrateEyeGazeQR.FittingEquation.SECOND_DEGREE_POLYNOMIAL);
            secondaryCalibrator = new CalibrateEyeGazeQR(CalibrateEyeGazeQR.FittingEquation.SECOND_DEGREE_POLYNOMIAL);
        } else {
            // Do third degree
            primaryCalibrator = new CalibrateEyeGazeQR(CalibrateEyeGazeQR.FittingEquation.THIRD_DEGREE_POLYNOMIAL);
            secondaryCalibrator = new CalibrateEyeGazeQR(CalibrateEyeGazeQR.FittingEquation.THIRD_DEGREE_POLYNOMIAL);
        }

        panel.setTotalProgress(
                primaryCalibrator.getTotalProgress()
                + secondaryCalibrator.getTotalProgress());
        panel.setTotalStages(
                primaryCalibrator.getTotalStages()
                + secondaryCalibrator.getTotalStages());

        /*
         * Could correct testArray with the last drift correction point
         */
 //if (allDriftCorrectionSets != null)
             //       {
                    //point = applyDriftCorrection(allDriftCorrectionSets, Integer.MAX_VALUE, point);
              //      }

        panel.setCorrectPoints(calArray, testArray);
        panel.setEyeVector(primeEyeVecArray, CalibratingViewJDialog.PRIMARY);
        panel.setEyeVector(secondaryEyeVecArray, CalibratingViewJDialog.SECONDARY);
        panel.setEyeVector(testEyeVecArray, CalibratingViewJDialog.TEST);

        primaryCalibrator.setCalibrateEyeGazeListener(
                panel.getCoeffListener(CalibratingViewJDialog.PRIMARY));
        secondaryCalibrator.setCalibrateEyeGazeListener(
                panel.getCoeffListener(CalibratingViewJDialog.SECONDARY));

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                // Start computation
                panel.setVisible(true);
            }
        });

        // Set panel size
        //int size = (int) (512 / displayJPanel.getGazeScaleFactor());
        if (this.fullScreenDim != null) {
            panel.setDisplayArea((int) this.fullScreenDim.getWidth(), (int) this.fullScreenDim.getWidth());
        } else {
            /** Default display size */
            panel.setDisplayArea(250, 250);
        }

        t.start();

        final Thread primaryCalibrationThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // Start computation
                try {
                    eyeGazeCoefficient[0] =
                            primaryCalibrator.calibrate(primeEyeVecArray, calArray[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Primary Calibration Thread");

        primaryCalibrationThread.start();

        final Thread secondaryCalibrationThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // Start computation
                try {
                    eyeGazeCoefficient[1] =
                            secondaryCalibrator.calibrate(secondaryEyeVecArray, calArray[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Secondary Calibration Thread");

        secondaryCalibrationThread.start();

        t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    primaryCalibrationThread.join();
                } catch (InterruptedException discard) {
                }
                try {
                    secondaryCalibrationThread.join();
                } catch (InterruptedException discard) {
                }

                // Pass eye gaze coeff to computation unit
                timer.getEyeGazeComputing().setPrimaryEyeCoeff(eyeGazeCoefficient[0]);
                timer.getEyeGazeComputing().setSecondaryEyeCoeff(eyeGazeCoefficient[1]);

                displayJPanel.setGazeScaleFactor(timer.getScreenFrameManager().getScreenInfoScalefactor());

                // Done calibrating allow button to be pressed
                calibrateButton.setEnabled(true);
                backButton.setEnabled(true);
            }
        });

        t.start();
    }

    public double[][] getEyeGazeCoefficient(int pos) {
        // Sanity check
        if (pos >= this.eyeGazeCoefficient.length) {
            return null;
        }
        return eyeGazeCoefficient[pos];
    }

    /** 
     * Clear all calibration information 
     */
    public void clearCalibrationInfo() {
        for (int i = 0; i
                < calibrationSet.length; i++) {
            calibrationSet[i].clear();
        }
        // Clear interval markers
        intervalMarkerManager.clearIntervalMarker();
        currentCalibrationInfo =
                null;
        for (int i = 0; i < eyeGazeCoefficient.length; i++) {
            eyeGazeCoefficient[i] = null;
        }
    }

    /**
     * @param gazeScaleFactor Scale factor to scale result of eye gaze coeff into
     * 512 x 512 space
     */
    public void setGazeScaleFactor(double gazeScaleFactor) {
        displayJPanel.setGazeScaleFactor(gazeScaleFactor);
    }

    public void setEyeGazeComputing(EyeGazeComputing eyeGazeComputing) {
        this.timer.setEyeGazeComputing(eyeGazeComputing);
    }

    public void setDegreeErrorComputer(DegreeErrorComputer degreeErrorComputer) {
        this.degreeErrorComputer = degreeErrorComputer;
    }

    public void setUsingCorneaReflect(boolean usingCorneaReflect) {
        this.useCorneaReflectionCheckBox.setSelected(usingCorneaReflect);
        this.timer.getEyeGazeComputing().setUsingCorneaReflect(usingCorneaReflect);
    }

    public void setAllCalibrationPointsUnprocessed() {
        for (int i = 0; i < this.calibrationSet.length; i++) {
            for (Enumeration en = calibrationSet[i].elements();
                    en.hasMoreElements();) {
                // Create element
                CalibrationInfo info = (CalibrationInfo) en.nextElement();
                info.isCalibrationPointPositionLocated = false;
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pointTypeButtonGroup = new javax.swing.ButtonGroup();
        graphHolder = new javax.swing.JPanel();
        bottomPanel = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();
        calibrateButton = new javax.swing.JButton();
        calibrationPointSelectorJPanel = new eyetrackercalibrator.gui.CalibrationPointSelectorJPanel();
        useCorneaReflectionCheckBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        fittingEquationChoiceComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        displayJPanel = new eyetrackercalibrator.gui.DisplayJPanel();
        controlPanel = new javax.swing.JPanel();
        deleteButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        calibrateList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        locateCalibrationPointsPositionsButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        testingPointsRadioButton = new javax.swing.JRadioButton();
        secondaryCalibrationPointsRadioButton = new javax.swing.JRadioButton();
        primaryCalibrationPointsRadioButton = new javax.swing.JRadioButton();
        frameScrollingJPanel = new eyetrackercalibrator.gui.FrameScrollingJPanel();

        graphHolder.setBackground(new java.awt.Color(0, 153, 0));
        graphHolder.setMaximumSize(null);

        org.jdesktop.layout.GroupLayout graphHolderLayout = new org.jdesktop.layout.GroupLayout(graphHolder);
        graphHolder.setLayout(graphHolderLayout);
        graphHolderLayout.setHorizontalGroup(
            graphHolderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 933, Short.MAX_VALUE)
        );
        graphHolderLayout.setVerticalGroup(
            graphHolderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 372, Short.MAX_VALUE)
        );

        backButton.setText("Back"); // NOI18N

        calibrateButton.setText("Calibrate"); // NOI18N
        calibrateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrateButtonActionPerformed(evt);
            }
        });

        useCorneaReflectionCheckBox.setSelected(true);
        useCorneaReflectionCheckBox.setText("Use Cornia Reflection");
        useCorneaReflectionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useCorneaReflectionCheckBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Fitting equation:");

        fittingEquationChoiceComboBox.setModel(new javax.swing.DefaultComboBoxModel(FITTING_EQUATION_CHOICE));
        fittingEquationChoiceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fittingEquationChoiceComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout bottomPanelLayout = new org.jdesktop.layout.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(calibrationPointSelectorJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, bottomPanelLayout.createSequentialGroup()
                .add(calibrateButton)
                .add(18, 18, 18)
                .add(useCorneaReflectionCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 178, Short.MAX_VALUE)
                .add(backButton))
            .add(bottomPanelLayout.createSequentialGroup()
                .add(9, 9, 9)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fittingEquationChoiceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(368, Short.MAX_VALUE))
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bottomPanelLayout.createSequentialGroup()
                .add(calibrationPointSelectorJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(fittingEquationChoiceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(backButton)
                    .add(useCorneaReflectionCheckBox)
                    .add(calibrateButton)))
        );

        displayJPanel.setMinimumSize(new java.awt.Dimension(200, 100));

        deleteButton.setText("-"); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        calibrateList.setModel(calibrationSet[0]);
        calibrateList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                calibrateListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(calibrateList);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jLabel1.setText("<html>Locate Calibration Points Positions:<html>"); // NOI18N
        jPanel2.add(jLabel1);

        locateCalibrationPointsPositionsButton.setText("Start"); // NOI18N
        locateCalibrationPointsPositionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locateCalibrationPointsPositionsButtonActionPerformed(evt);
            }
        });
        jPanel2.add(locateCalibrationPointsPositionsButton);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        pointTypeButtonGroup.add(testingPointsRadioButton);
        testingPointsRadioButton.setText("Drift Correction Points"); // NOI18N
        testingPointsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testingPointsRadioButtonActionPerformed(evt);
            }
        });

        pointTypeButtonGroup.add(secondaryCalibrationPointsRadioButton);
        secondaryCalibrationPointsRadioButton.setText("Secondary Calibration Points"); // NOI18N
        secondaryCalibrationPointsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secondaryCalibrationPointsRadioButtonActionPerformed(evt);
            }
        });

        pointTypeButtonGroup.add(primaryCalibrationPointsRadioButton);
        primaryCalibrationPointsRadioButton.setSelected(true);
        primaryCalibrationPointsRadioButton.setText("Primary Calibration Points"); // NOI18N
        primaryCalibrationPointsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                primaryCalibrationPointsRadioButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(secondaryCalibrationPointsRadioButton)
                    .add(primaryCalibrationPointsRadioButton)
                    .add(testingPointsRadioButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(primaryCalibrationPointsRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(secondaryCalibrationPointsRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(testingPointsRadioButton))
        );

        org.jdesktop.layout.GroupLayout controlPanelLayout = new org.jdesktop.layout.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
            .add(controlPanelLayout.createSequentialGroup()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 143, Short.MAX_VALUE)
                .add(deleteButton))
            .add(controlPanelLayout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, controlPanelLayout.createSequentialGroup()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(deleteButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(displayJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1030, Short.MAX_VALUE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(frameScrollingJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1020, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controlPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(displayJPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(frameScrollingJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(controlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(graphHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(bottomPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(graphHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(bottomPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

private void calibrateListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_calibrateListMouseClicked
    // Get info 
    int select = calibrateList.getSelectedIndex();
    CalibrationInfo info =
            (CalibrationInfo) calibrationSet[this.calibrationIndex].get(select);
    // Check if it is double click or not
    if (evt.getClickCount() >= 2) {
        //Move to the frame
        frameScrollingJPanel.setCurrentFrame(
                timer.getFrameSynchronizor().eyeFrameToSyncFrame(info.startEyeFrame));
    }
    // Change the check mark to reflect the value
    switch (info.calibrationType) {
        case Secondary:
            this.secondaryCalibrationPointsRadioButton.setSelected(true);
            break;
        case Testing:
            this.testingPointsRadioButton.setSelected(true);
            break;
        default:
            this.primaryCalibrationPointsRadioButton.setSelected(true);
    }
}//GEN-LAST:event_calibrateListMouseClicked

private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
    FrameManager screenFrameManager = timer.getScreenFrameManager();

    Object[] selected = calibrateList.getSelectedValues();
    CalibrationInfo info = null;
    for (int i = 0; i < selected.length; i++) {
        info = (CalibrationInfo) selected[i];
        calibrationSet[this.calibrationIndex].removeElement(selected[i]);
        // Remove marked point from each frame as well
        for (int j = info.startSceneFrame; j <= info.stopSceneFrame; j++) {

            ScreenViewFrameInfo frameInfo =
                    (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(j);
            if (frameInfo != null) { //sanity check just incase
                // Clear old content

                frameInfo.setMarkedPoints(null);
                // Put back
                screenFrameManager.setFrameInfo(j, frameInfo);
            }
        }

        // Remove intervalMarker
        intervalMarkerManager.removeIntervalMarker(info.getIntervalMarker());
    }

    // Set label accordingly
    updateCalibrationStoredLabel(this.calibrationIndex);
    //update to allow for drift correct
        updateDriftCorrectionOffsets();
}//GEN-LAST:event_deleteButtonActionPerformed

private void calibrateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibrateButtonActionPerformed

    boolean process = true;
    for (int i = 0; i < this.calibrationSet.length && process; i++) {
        for (Enumeration en = calibrationSet[i].elements();
                en.hasMoreElements() && process;) {
            // Create element
            CalibrationInfo info = (CalibrationInfo) en.nextElement();
            process = process && info.isCalibrationPointPositionLocated;
        }
    }

    // Check if we have unprocessed calibration points.  If not then we warn
    // the user before proceeding
    int result = JOptionPane.OK_OPTION;

    if (!process) {
        result = JOptionPane.showConfirmDialog(this,
                "You have some unprocessed calibration points.  The calibration results may be incorrect.  Select OK to proceed with calibration.",
                "Unprocessed Calibration Points Exist", JOptionPane.OK_CANCEL_OPTION);
    }
    if (result == JOptionPane.OK_OPTION) {
        // Done calibrating allow button to be pressed
        calibrateButton.setEnabled(false);
        backButton.setEnabled(false);

        calibrate();

        //update to allow for drift correct
        updateDriftCorrectionOffsets();
    }
}//GEN-LAST:event_calibrateButtonActionPerformed

private void locateCalibrationPointsPositionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locateCalibrationPointsPositionsButtonActionPerformed
    if (this.isRunningCalibrationPointPositionFinding) {
        this.calibrationPointPositionFinderRunner.kill();
    } else {
        // Check if we have to redo all set
        boolean isAllCompleted = true;
        for (int i = 0; i < calibrationSet.length && isAllCompleted; i++) {
            // For each calibration range.  Also stop if is killed
            for (Enumeration en = calibrationSet[i].elements();
                    en.hasMoreElements() && isAllCompleted;) {
                CalibrationInfo info = (CalibrationInfo) en.nextElement();
                isAllCompleted = isAllCompleted && info.isCalibrationPointPositionLocated;
            }
        }

        // If all complete ask user whether he/she wants to redo everything
        if (isAllCompleted) {
            int answer = JOptionPane.showOptionDialog(this,
                    "You already processed all calibration points.  "
                    + "Would you like to reprocess all of them?",
                    "All calibration points are processed",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, null, JOptionPane.NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                // Mark all point as unprocessed
                for (int i = 0; i < calibrationSet.length && isAllCompleted; i++) {
                    // For each calibration range.  Also stop if is killed
                    for (Enumeration en = calibrationSet[i].elements();
                            en.hasMoreElements() && isAllCompleted;) {
                        CalibrationInfo info = (CalibrationInfo) en.nextElement();
                        info.isCalibrationPointPositionLocated = false;
                    }
                }
                //update to allow for drift correct
                updateDriftCorrectionOffsets();
            } else {
                // Does nothing if the user say no
                return;
            }
        }

        // Set running flag
        this.isRunningCalibrationPointPositionFinding = true;

        /* Sanity check if fullscreen frame dir exists or not.  If not then
         * default to small scene dir.
         */
        String fullScreenFrameUsableDir = this.fullScreenFrameDirectory;
        if (fullScreenFrameUsableDir == null) {
            // Go by default
            fullScreenFrameUsableDir = this.timer.getScreenFrameManager().getFrameDirectory();
        } else {
            File test = new File(fullScreenFrameUsableDir);
            if (!test.exists()) {
                // Go by default
                fullScreenFrameUsableDir = this.timer.getScreenFrameManager().getFrameDirectory();
            }
        }


        // Create a thread for running calibration point location
        this.calibrationPointPositionFinderRunner =
                new CalibrationPointPositionFinderRunner(
                projectLocation, calibrationSet,
                timer.getScreenFrameManager(), fullScreenFrameUsableDir,
                new CompletionListener() {

                    @Override
                    public void fullCompletion() {
                        calibrationPointPositionFindingCompleteHandler();
                    }
                });

        this.calibrationPointPositionFinderRunner.start();

        // Change name to cancel
        locateCalibrationPointsPositionsButton.setText(
                "Cancel");
        // Disable all other component
        enableButtons(locateCalibrationPointsPositionsButton, false);
    }
}//GEN-LAST:event_locateCalibrationPointsPositionsButtonActionPerformed

private void testingPointsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testingPointsRadioButtonActionPerformed
    setCalibrationType(CalibrationInfo.CalibrationType.Testing);
}//GEN-LAST:event_testingPointsRadioButtonActionPerformed

private void secondaryCalibrationPointsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secondaryCalibrationPointsRadioButtonActionPerformed
    setCalibrationType(CalibrationInfo.CalibrationType.Secondary);
}//GEN-LAST:event_secondaryCalibrationPointsRadioButtonActionPerformed

private void primaryCalibrationPointsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primaryCalibrationPointsRadioButtonActionPerformed
    setCalibrationType(CalibrationInfo.CalibrationType.Primary);
}//GEN-LAST:event_primaryCalibrationPointsRadioButtonActionPerformed

private void useCorneaReflectionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useCorneaReflectionCheckBoxActionPerformed
    JOptionPane.showMessageDialog(this, "Changing this option makes the current calibration invalid.  You will need to recalibrate.", "Calibration Option Changes", JOptionPane.WARNING_MESSAGE);
    timer.getEyeGazeComputing().setUsingCorneaReflect(this.useCorneaReflectionCheckBox.isSelected());
}//GEN-LAST:event_useCorneaReflectionCheckBoxActionPerformed

private void fittingEquationChoiceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fittingEquationChoiceComboBoxActionPerformed
    //Warn if user wants to
    if (this.warnuser) {
        String[] options = {"OK", "Do not warn me again"};


        int v = JOptionPane.showOptionDialog(this,
                "Calibration fitting equaltion has changed. "
                + "Please click on calibration button for it to take effect.",
                "Calibration fitting equation changes",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                options, options[0]);
        if (v == JOptionPane.NO_OPTION) {
            this.warnuser = false;
        }
    }

}//GEN-LAST:event_fittingEquationChoiceComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton calibrateButton;
    private javax.swing.JList calibrateList;
    private eyetrackercalibrator.gui.CalibrationPointSelectorJPanel calibrationPointSelectorJPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton deleteButton;
    private eyetrackercalibrator.gui.DisplayJPanel displayJPanel;
    private javax.swing.JComboBox fittingEquationChoiceComboBox;
    private eyetrackercalibrator.gui.FrameScrollingJPanel frameScrollingJPanel;
    private javax.swing.JPanel graphHolder;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton locateCalibrationPointsPositionsButton;
    private javax.swing.ButtonGroup pointTypeButtonGroup;
    private javax.swing.JRadioButton primaryCalibrationPointsRadioButton;
    private javax.swing.JRadioButton secondaryCalibrationPointsRadioButton;
    private javax.swing.JRadioButton testingPointsRadioButton;
    private javax.swing.JCheckBox useCorneaReflectionCheckBox;
    // End of variables declaration//GEN-END:variables
}
