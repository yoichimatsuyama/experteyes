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
 * Main.java
 *
 * Created on September 19, 2007, 9:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package eyetrackercalibrator;

import eyetrackercalibrator.framemanaging.EyeViewFrameInfo;
import eyetrackercalibrator.framemanaging.FrameLoadingListener;
import eyetrackercalibrator.framemanaging.FrameManager;
import eyetrackercalibrator.framemanaging.FrameSynchronizor;
import eyetrackercalibrator.framemanaging.InformationDatabase;
import eyetrackercalibrator.framemanaging.ScreenFrameManager;
import eyetrackercalibrator.framemanaging.ScreenViewFrameInfo;
import eyetrackercalibrator.framemanaging.SynchronizationPoint;
import eyetrackercalibrator.gui.CalibrateJPanel;
import eyetrackercalibrator.gui.CalibrateJPanel.CalibationPoint;
import eyetrackercalibrator.gui.CleanDataJPanel;
import eyetrackercalibrator.gui.ErrorMarking;
import eyetrackercalibrator.gui.ExportMovieJFrame;
import eyetrackercalibrator.gui.ImportMovieJFrame;
import eyetrackercalibrator.gui.NewProjectJDialog;
import eyetrackercalibrator.gui.ProjectSelectPanel;
import eyetrackercalibrator.gui.SynchronizeJPanel;
import eyetrackercalibrator.gui.TrialMarkingJPanel;
import eyetrackercalibrator.math.Computation;
import eyetrackercalibrator.math.ComputeIlluminationRangeThread;
import eyetrackercalibrator.math.DegreeErrorComputer;
import eyetrackercalibrator.math.EyeGazeComputing;
import eyetrackercalibrator.math.EyeGazeComputing.ComputingApproach;
import eyetrackercalibrator.trialmanaging.TrialMarker;
import buseylab.fiteyemodel.gui.FitEyeModelSetup;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

/**
 * @author SQ
 */
public class Main extends javax.swing.JFrame {

    private CalibrateJPanel calibrateJPanel;
    private SynchronizeJPanel synchronizeJPanel;
    private ProjectSelectPanel projectSelectPanel;
    private CleanDataJPanel cleanDataJPanel;
    private TrialMarkingJPanel markTrialJPanel;
    private FrameManager eyeFrameManager = null;
    private ScreenFrameManager screenFrameManager = null;
    private FrameSynchronizor frameSynchronizor = new FrameSynchronizor();
    private EyeGazeComputing eyeGazeComputing = new EyeGazeComputing();
    private JMenuBar menuBar;
    private JMenu projectMenu;
    private JMenu viewSelectMenu;
    private JMenuItem new_MenuItem;
    private JMenuItem open_MenuItem;
    private JMenuItem save_MenuItem;
    private JMenuItem quit_MenuItem;
    private JRadioButtonMenuItem primaryMenuItem;
    private JRadioButtonMenuItem secondaryMenuItem;
    private JRadioButtonMenuItem linearMenuItem;
    private JMenu exportSelectMenu;
    private JMenuItem calibrationPointsExportMenuItem;
    private JMenu importSelectMenu;
    private JMenuItem eyeFrameImportMenuItem;
    private JMenuItem sceneFrameImportMenuItem;
    private JMenu toolsMenu;
    private JMenuItem fitEyeModelImportMenuItem;
    private File projectLocation = null;
    static final public String EYE_OFFSET = "eyeoffset";
    static final public String SCREEN_OFFSET = "screenoffset";
    static final public String USING_CORNEA_REFLECTION = "usigCorneaReflection";
    static final public String EYE_VIEW_DIRECTORY = "eyedirectory";
    static final public String EYE_INFO_DIRECTORY = "eyeinfodirectory";
    static final public String SCREEN_VIEW_DIRECTORY = "screendirectory";
    static final public String FULL_SCREEN_VIEW_DIRECTORY = "fullscreendirectory";
    static final public String SCREEN_INFO_DIRECTORY = "screeninfodirectory";
    static final public String EYE_LOAD_PROGRESS = "eyeloadprogress";
    static final public String SCREEN_LOAD_PROGRESS = "screenloadprogress";
    static final public String PROJECT_PROPERTY_FILE_NAME = "project.ini";
    static final public String CALIBRATION_FILE_NAME = "calibration.xml";
    static final public String SYNC_FILE_NAME = "synchronization.xml";
    static final public String ERROR_FILE_NAME = "errors.xml";
    static final public String TRIAL_FILE_NAME = "trial.xml";
    static final public String MONITOR_TRUE_WIDTH_PX = "monitortruewidth";
    static final public String MONITOR_TRUE_HEIGHT_PX = "monitortrueheight";
    static final public String MONITOR_TRUE_WIDTH_CM = "monitortruewidthcm";
    static final public String MONITOR_TRUE_HEIGHT_CM = "monitortrueheightcm";
    static final public String DISTANCE_FROM_MONITOR_CM = "distancefrommonitorcm";
    static final public String FULL_SCREEN_WIDTH = "fullscreenwidth";
    static final public String FULL_SCREEN_HEIGHT = "fullscreenheight";
    static final public String COMMENT = "comment";
    private static String DATABASE_NAME = "IlluminationDb";
    static final public String CORNERHINT_DIR = "CornerHints";
    private int DISPLAY_WIDTH = 512;
    private int DISPLAY_HEIGHT = 512;
    private InformationDatabase informationDatabase = null;
    private boolean isProjectOpen = false;
    static final String DEFAULT_EYE_FRAME_PATH = "eye";
    static final String DEFAULT_EYE_INFO_PATH = "Gaze";
    static final String DEFAULT_SCENE_FRAME_PATH = "scene";
    static final String DEFAULT_SCENE_INFO_PATH = "Corners";

    /** Creates a new instance of Main */
    public Main() {
        initComponents();
    }

    /**
     * Close project. 
     * @return false when user cancel project closing
     */
    private boolean closeProject() {
        int n = JOptionPane.showConfirmDialog(
                this,
                "Would you like to save before exiting?",
                "Exiting program",
                JOptionPane.YES_NO_CANCEL_OPTION);
        switch (n) {
            case JOptionPane.CANCEL_OPTION:
                // Cancel closeing
                return false;
            case JOptionPane.YES_OPTION:
                // Save before exit
                save();
        }
        // Don't stop before save since we still need database during saving
        // Atop all animation
        synchronizeJPanel.stop();
        calibrateJPanel.stop();
        cleanDataJPanel.stop();
        markTrialJPanel.stop();

        // Close old project if active
        if (eyeFrameManager != null) {
            eyeFrameManager.close();
        }
        if (screenFrameManager != null) {
            screenFrameManager.close();
        }
        if (informationDatabase != null) {
            informationDatabase.close();
        }

        return true;
    }

    private String createPath(String path, File projectLocation, String defaultValue) {
        String v = path;
        if (v == null) {
            File defaultPath = new File(projectLocation.getParentFile(), defaultValue);
            v = defaultPath.getAbsolutePath();
        }
        return v;
    }

    private void exportCalibrationPointInfo() {
        // Ask user where to put information
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            PrintWriter out = null;
            try {
                // Start exporting
                out = new PrintWriter(chooser.getSelectedFile());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, "Cannot open "
                        + chooser.getSelectedFile().getAbsolutePath() + " for writing.",
                        "File Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Dimension trueMonitorDimension = projectSelectPanel.getMonitorDimensionPX();
            Dimension sceneDimension = projectSelectPanel.getFullSceneDimensionPX();
            double distanceFromMeasuredScene = projectSelectPanel.getDistanceFromMeasuredScene();
            double sceneHeight = projectSelectPanel.getSceneHeightCM();
            double sceneWidth = projectSelectPanel.getSceneWidthCM();

            // Print header
            out.println(
                    "Frame\t"
                    + "Scene X\t"
                    + "Scene Y\t"
                    + "Top Left X\t"
                    + "Top Left Y\t"
                    + "Top Right X\t"
                    + "Top Right Y\t"
                    + "Bottom Left X\t"
                    + "Bottom Left Y\t"
                    + "Bottom Right X\t"
                    + "Bottom Right Y\t"
                    + "Eye Gaze X\t"
                    + "Eye Gaze Y\t"
                    + "Error Angle\t"
                    + "Eye Frame File\t"
                    + "Scene Frame File");

            int totalFrame = this.frameSynchronizor.getTotalFrame();

            // Prepare degree error computer
            DegreeErrorComputer dec = new DegreeErrorComputer(sceneDimension,
                    distanceFromMeasuredScene,
                    sceneWidth, sceneHeight);

            // Scan through each frame
            CalibationPoint calibrationPoint;
            for (int i = 1; i <= totalFrame; i++) {

                calibrationPoint =
                        calibrateJPanel.frameToCalibrationPoint(
                        this.frameSynchronizor.getSceneFrame(i));
                if (calibrationPoint != null) {
                    // Found calibration points so output info
                    ScreenViewFrameInfo info =
                            (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(
                            this.frameSynchronizor.getSceneFrame(i));

                    out.print(i);

                    Point[] point = info.getMarkedPoints();
                    Point scenePos = null;
                    if (point != null) {
                        scenePos = point[0];
                    }
                    printPointHelper(scenePos, out);

                    Point p = null;
                    Point[] corners = info.getCorners();
                    if (corners != null) {
                        printCorners(corners, p, out);

                        // Estimate true screen position
                        Point2D pos = Computation.ComputeScreenPositionProjective(
                                trueMonitorDimension,
                                scenePos,
                                corners[ScreenViewFrameInfo.TOPLEFT],
                                corners[ScreenViewFrameInfo.TOPRIGHT],
                                corners[ScreenViewFrameInfo.BOTTOMLEFT],
                                corners[ScreenViewFrameInfo.BOTTOMRIGHT]);
                        printPointHelper(pos, out);
                    } else {
                        // Print error
                        for (int j = 0; j < 10; i++) {
                            out.print("\t" + GlobalConstants.ERROR_VALUE);
                        }
                    }

                    // Get eyeGaze
                    EyeViewFrameInfo eyeInfo = null;
                    if (this.eyeFrameManager != null) {
                        eyeInfo = (EyeViewFrameInfo) this.eyeFrameManager.getFrameInfo(
                                frameSynchronizor.getEyeFrame(i));

                        if (eyeInfo != null) {
                            Point2D.Double gazeVec = this.eyeGazeComputing.getEyeVector(eyeInfo);
                            // Computer eye gaze point
                            Point2D gazePoint = this.eyeGazeComputing.computeEyeGaze(
                                    i, gazeVec.x, gazeVec.y);

                            if (scenePos != null) {
                                // Compute error angel
                                double errorAngle = dec.degreeError(
                                        scenePos, gazePoint);

                                if (errorAngle >= 0) {
                                    out.print("\t" + errorAngle);
                                } else {
                                    out.print("\t" + GlobalConstants.ERROR_VALUE);
                                }
                            } else {
                                out.print("\t" + GlobalConstants.ERROR_VALUE);
                            }
                        }
                    }
                    // Print files involved
                    if (eyeInfo != null) {
                        out.print("\t" + eyeInfo.getSourceFileName());
                    } else {
                        out.print("\t-");
                    }
                    if (info != null) {
                        out.print("\t" + info.getSourceFileName());
                    } else {
                        out.print("\t-");
                    }
                    out.println();
                }
            }
            out.close();
        }
    }

    private Point2D.Double exportEyeGazes(PrintWriter exportWriter, int currentFrame,
            Double vector, ComputingApproach approach, Dimension screenViewFullSize) {
        Double point = new Point2D.Double(GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE);

        Point2D p = this.eyeGazeComputing.computeEyeGaze(currentFrame, vector.x, vector.y, approach);
        if (p != null) {
            point.setLocation(p);
            // Check the range and mark is with GlobalConstants.ERROR_VALUE,GlobalConstants.ERROR_VALUE if out of screen
            if (point.x < 0 || point.y < 0
                    || point.x > screenViewFullSize.width
                    || point.y > screenViewFullSize.height) {
                point.setLocation(GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE);
            }
        }
        exportWriter.print(point.x + "\t" + point.y + "\t");

        return point;
    }

    private void exportMovies() {
        int eyeFrame, screenFrame;

        ExportMovieJFrame exportMovieJFrame = new ExportMovieJFrame(
                projectLocation, DISPLAY_WIDTH, DISPLAY_HEIGHT,
                this.eyeGazeComputing, this.frameSynchronizor,
                this.eyeFrameManager, this.screenFrameManager,
                this.projectSelectPanel.getFullScreenFrameDirectory());
        exportMovieJFrame.setLocationByPlatform(true);
        exportMovieJFrame.setVisible(true);
    }

    private void initComponents() {
        /** SEt windows behavior */
        setLocationByPlatform(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                formWindowClosed();
            }
        });

        /* Set menu bar */
        menuBar = new javax.swing.JMenuBar();
        projectMenu = new javax.swing.JMenu("Project");
        new_MenuItem = new JMenuItem("New");
        open_MenuItem = new JMenuItem("Open");
        save_MenuItem = new JMenuItem("Save");
        quit_MenuItem = new JMenuItem("Quit");

        viewSelectMenu = new JMenu("View");
        primaryMenuItem = new JRadioButtonMenuItem("Primary");
        primaryMenuItem.setSelected(true);
        secondaryMenuItem = new JRadioButtonMenuItem("Secondary");
        linearMenuItem = new JRadioButtonMenuItem("Linear Interpolation");
        ButtonGroup viewGroup = new ButtonGroup();
        viewGroup.add(primaryMenuItem);
        viewGroup.add(secondaryMenuItem);
        viewGroup.add(linearMenuItem);

        importSelectMenu = new JMenu("Import");
        eyeFrameImportMenuItem = new JMenuItem("Eye Frames");
        sceneFrameImportMenuItem = new JMenuItem("Scene Frames");

        exportSelectMenu = new JMenu("Export");
        calibrationPointsExportMenuItem = new JMenuItem("Calibration Points");

        toolsMenu = new JMenu("Tools");
        fitEyeModelImportMenuItem = new JMenuItem("Find Pupil Locations");

        new_MenuItem.setMnemonic(KeyEvent.VK_N);
        open_MenuItem.setMnemonic(KeyEvent.VK_O);
        save_MenuItem.setMnemonic(KeyEvent.VK_S);
        quit_MenuItem.setMnemonic(KeyEvent.VK_Q);

        ActionListener menuListener = new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuActionPerformed(evt);
            }
        };

        new_MenuItem.addActionListener(menuListener);
        open_MenuItem.addActionListener(menuListener);
        save_MenuItem.addActionListener(menuListener);
        quit_MenuItem.addActionListener(menuListener);

        menuListener = new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMenuActionPerformed(evt);
            }
        };

        primaryMenuItem.addActionListener(menuListener);
        secondaryMenuItem.addActionListener(menuListener);
        linearMenuItem.addActionListener(menuListener);

        menuListener = new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuActionPerformed(evt);
            }
        };

        calibrationPointsExportMenuItem.addActionListener(menuListener);

        menuListener = new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importMenuActionPerformed(evt);
            }
        };

        eyeFrameImportMenuItem.addActionListener(menuListener);
        sceneFrameImportMenuItem.addActionListener(menuListener);

        menuListener = new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fitEyeModelMenuActionPerformed(evt);
            }
        };

        fitEyeModelImportMenuItem.addActionListener(menuListener);

        projectMenu.add(new_MenuItem);
        projectMenu.add(open_MenuItem);
        projectMenu.add(save_MenuItem);
        projectMenu.add(quit_MenuItem);

        viewSelectMenu.add(primaryMenuItem);
        viewSelectMenu.add(secondaryMenuItem);
        viewSelectMenu.add(linearMenuItem);

        exportSelectMenu.add(calibrationPointsExportMenuItem);

        importSelectMenu.add(eyeFrameImportMenuItem);
        importSelectMenu.add(sceneFrameImportMenuItem);

        toolsMenu.add(fitEyeModelImportMenuItem);

        menuBar.add(projectMenu);
        menuBar.add(viewSelectMenu);
        menuBar.add(importSelectMenu);
        menuBar.add(toolsMenu);
        menuBar.add(exportSelectMenu);
        setJMenuBar(menuBar);

        /* Interaction panel */


        calibrateJPanel = new CalibrateJPanel();
        synchronizeJPanel = new SynchronizeJPanel();
        projectSelectPanel = new ProjectSelectPanel();
        cleanDataJPanel = new CleanDataJPanel();
        markTrialJPanel = new TrialMarkingJPanel();

        calibrateJPanel.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibrateJPanelActionPerformed(evt);
            }
        });
        synchronizeJPanel.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                synchronizeJPanelActionPerformed(evt);
            }
        });
        projectSelectPanel.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectSelectPanelActionPerformed(evt);
            }
        });
        cleanDataJPanel.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cleanDataJPanelActionPerformed(evt);
            }
        });

        markTrialJPanel.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markTrialJPanelActionPerformed(evt);
            }
        });

        // Give all an eye gaze computation
        calibrateJPanel.setEyeGazeComputing(this.eyeGazeComputing);
        cleanDataJPanel.setEyeGazeComputing(this.eyeGazeComputing);
        markTrialJPanel.setEyeGazeComputing(this.eyeGazeComputing);

        projectSelectPanel.setEnabled(false);
        add(projectSelectPanel);
        pack();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    protected void fitEyeModelMenuActionPerformed(ActionEvent evt) {
        // Sanity check whether we have a directory for eyes or not
        // Prompt user to enter the location if not there
        String importLocation = this.projectSelectPanel.getEyeFrameDirectory();
        if (importLocation == null || importLocation.trim().length() < 1) {
            JOptionPane.showMessageDialog(this,
                    "You need to specify eye frame directory in the project and populate them with movies frames first.",
                    "Missing Information", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] args = new String[1];
        args[0] = importLocation;

        // Start fit eye model.
        FitEyeModelSetup fems = new FitEyeModelSetup();
        fems.setVisible(true);
        fems.setEyeDirectory(importLocation);
    }

    /* Handle when "Back" are pressed in calibration panel*/
    protected void calibrateJPanelActionPerformed(java.awt.event.ActionEvent evt) {
        if ("Back".equals(evt.getActionCommand())) {
            // Stop animation
            calibrateJPanel.stop();

            // Go back to projectSelectPanel
            remove(calibrateJPanel);
            add(projectSelectPanel);
            pack();
        }
    }

    /** 
     * Handle when "Synchronize" and "Cancel" are pressed in synchronize panel
     * @param evt 
     */
    protected void synchronizeJPanelActionPerformed(java.awt.event.ActionEvent evt) {
        boolean switchBack = false;
        if ("Synchronize".equals(evt.getActionCommand())) {
            // Read in synchronize data and set all offset in all panel
            sync();

            switchBack = true;
        } else if ("Cancel".equals(evt.getActionCommand())) {
            switchBack = true;
        }
        if (switchBack) {
            // Stop animation
            synchronizeJPanel.stop();
            // Go back to projectSelectPanel
            remove(synchronizeJPanel);
            add(projectSelectPanel);
            pack();
        }
    }

    /** Handle when "Back" are pressed in clean data panel */
    protected void cleanDataJPanelActionPerformed(java.awt.event.ActionEvent evt) {
        if ("Back".equals(evt.getActionCommand())) {
            // Stop animation
            cleanDataJPanel.stop();

            // Go back to projectSelectPanel
            remove(cleanDataJPanel);
            add(projectSelectPanel);
            pack();
        }
    }

    /** Handle when "Back" are pressed in mark trials panel */
    protected void markTrialJPanelActionPerformed(java.awt.event.ActionEvent evt) {
        if ("Back".equals(evt.getActionCommand())) {
            // Stop animation
            markTrialJPanel.stop();

            // Go back to projectSelectPanel
            remove(markTrialJPanel);
            add(projectSelectPanel);
            pack();
        }
    }

    /** Helper for printing corners to output */
    private void printCorners(Point[] corners, Point p, PrintWriter out) {
        p = corners[ScreenViewFrameInfo.TOPLEFT];
        printPointHelper(p, out);
        p = corners[ScreenViewFrameInfo.TOPRIGHT];
        printPointHelper(p, out);
        p = corners[ScreenViewFrameInfo.BOTTOMLEFT];
        printPointHelper(p, out);
        p = corners[ScreenViewFrameInfo.BOTTOMRIGHT];
        printPointHelper(p, out);
    }

    private void printPointHelper(Point2D p, PrintWriter out) {
        // Show corners
        if (p != null) {
            out.print("\t" + p.getX() + "\t" + p.getY());
        } else {
            out.print("\t" + GlobalConstants.ERROR_VALUE + "\t" + GlobalConstants.ERROR_VALUE);
        }
    }

    /**
     * Handle when "Load Eye Image", "Reload Eye Information",
     * "Load Screen Image", "Reload Screen Information",
     * "Synchronize", "Calibrate", "Clean data" and "Mark trials" buttons
     * are pressed.
     */
    public void projectSelectPanelActionPerformed(java.awt.event.ActionEvent evt) {
        screenFrameManager.setFrameDirectory(projectSelectPanel.getScreenFrameDirectory());
        eyeFrameManager.setFrameDirectory(projectSelectPanel.getEyeFrameDirectory());

        if ("Synchronize".equals(evt.getActionCommand())) {
            // Set up synchronized panel

            // need this to set the stat for frame playing (total frame and what not)
            synchronizeJPanel.setEyeFrameManager(eyeFrameManager);
            synchronizeJPanel.setSceneFrameManager(screenFrameManager);

            // Go to synchronized panel
            remove(projectSelectPanel);
            add(synchronizeJPanel);
            pack();

            // Start playing synchronize panel
            synchronizeJPanel.start();

        } else if ("Load Eye Images".equals(evt.getActionCommand())) {
            projectSelectPanel.setEyeLoadButtonsEnable(false);

            final Thread eyeThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    eyeFrameManager.loadFrames(
                            projectSelectPanel.getEyeFrameDirectory(),
                            projectSelectPanel.getEyeInfoDirectory());
                }
            });
            eyeThread.start();
            // Create a thred to wait and reenable the button
            Thread waitThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        eyeThread.join();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    projectSelectPanel.setEyeLoadButtonsEnable(true);
                }
            });
            waitThread.start();

        } else if ("Load Screen Images".equals(evt.getActionCommand())) {
            projectSelectPanel.setScreenLoadButtonsEnable(false);

            final Thread screenThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    screenFrameManager.loadFrames(
                            projectSelectPanel.getScreenFrameDirectory(),
                            projectSelectPanel.getScreenInfoDirectory());
                }
            });
            screenThread.start();
            // Create a thred to wait and reenable the button
            Thread waitThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        screenThread.join();
                        updateFullScreenDimansion();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    projectSelectPanel.setScreenLoadButtonsEnable(true);
                }
            });
            waitThread.start();

        } else if ("Reload Eye Information".equals(evt.getActionCommand())) {
            projectSelectPanel.setEyeLoadButtonsEnable(false);
            final Thread eyeThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    eyeFrameManager.loadFrameInfo(
                            projectSelectPanel.getEyeFrameDirectory(),
                            projectSelectPanel.getEyeInfoDirectory());
                }
            });
            eyeThread.start();

            // Create a thred to wait and reenable the button
            Thread waitThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        eyeThread.join();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    projectSelectPanel.setEyeLoadButtonsEnable(true);
                }
            });
            waitThread.start();
        } else if ("Reload Screen Information".equals(evt.getActionCommand())) {

            projectSelectPanel.setScreenLoadButtonsEnable(false);

            final Thread screenThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    screenFrameManager.loadFrameInfo(
                            projectSelectPanel.getScreenFrameDirectory(),
                            projectSelectPanel.getScreenInfoDirectory());
                }
            });
            screenThread.start();
            // Create a thred to wait and reenable the button
            Thread waitThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        screenThread.join();
                        updateFullScreenDimansion();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    projectSelectPanel.setScreenLoadButtonsEnable(true);
                }
            });
            waitThread.start();

        } else if ("Compute Screen Illumination".equals(evt.getActionCommand())) {
            projectSelectPanel.setScreenLoadButtonsEnable(false);

            final FrameLoadingListener frameLoadingListener =
                    projectSelectPanel.getScreenFrameLoadingListener();
            final int totalFrame = screenFrameManager.getTotalFrames();
            final ComputeIlluminationRangeThread computeIlluminationRangeThread =
                    new ComputeIlluminationRangeThread(screenFrameManager,
                    informationDatabase, 1, totalFrame,
                    new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            Integer totalLoaded = (Integer) evt.getNewValue();
                            frameLoadingListener.update(totalLoaded + " of " + totalFrame,
                                    totalLoaded, 0, totalFrame);
                        }
                    });
            computeIlluminationRangeThread.start();

            // Create a thred to wait and reenable the button
            Thread waitThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        computeIlluminationRangeThread.join();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    projectSelectPanel.setScreenLoadButtonsEnable(true);
                }
            });
            waitThread.start();

        } else {
            // Screen scaling must be recomputed before any of the following operations
            screenFrameManager.setScreenInfoScalefactor(computeScalingFactor());

            if ("Calibrate".equals(evt.getActionCommand())) {
                // need this to set the stat for frame playing (total frame and what not)
                calibrateJPanel.setEyeFrameManager(eyeFrameManager);
                calibrateJPanel.setScreenFrameManager(screenFrameManager);
                calibrateJPanel.setProjectRoot(projectLocation);
                calibrateJPanel.setFullScreenFrameDirectory(
                        projectSelectPanel.getFullScreenFrameDirectory());
                calibrateJPanel.setFullScreenDim(projectSelectPanel.getFullSceneDimensionPX());
                calibrateJPanel.setGazeScaleFactor(
                        screenFrameManager.getScreenInfoScalefactor());
                calibrateJPanel.setEyeScreenInfoDirectory(
                        projectSelectPanel.getEyeInfoDirectory());
                calibrateJPanel.setDegreeErrorComputer(new DegreeErrorComputer(
                        projectSelectPanel.getFullSceneDimensionPX(),
                        projectSelectPanel.getDistanceFromMeasuredScene(),
                        projectSelectPanel.getSceneWidthCM(),
                        projectSelectPanel.getSceneHeightCM()));
                sync();
                calibrateJPanel.setFrameSynchronizor(this.frameSynchronizor);

                remove(projectSelectPanel);
                add(calibrateJPanel);
                pack();

                calibrateJPanel.start();

            } else if ("Clean Data".equals(evt.getActionCommand())) {

                // Set scaling factor
                cleanDataJPanel.setEyeGazeScaleFactor(
                        screenFrameManager.getScreenInfoScalefactor());

                // need this to set the stat for frame playing (total frame and what not)
                cleanDataJPanel.setDefaultValueAppliedListener(projectSelectPanel);
                cleanDataJPanel.setEyeFrameManager(eyeFrameManager);
                cleanDataJPanel.setScreenFrameManager(screenFrameManager);
                cleanDataJPanel.setFullScreenFrameDirectory(
                        projectSelectPanel.getFullScreenFrameDirectory());
                cleanDataJPanel.setCornerHintDir(projectSelectPanel.getCornerHintsDirectory());
                cleanDataJPanel.setScreenInfoDir(projectSelectPanel.getScreenInfoDirectory());
                sync();
                cleanDataJPanel.setFrameSynchronizor(this.frameSynchronizor);

                remove(projectSelectPanel);
                add(cleanDataJPanel);
                pack();

                cleanDataJPanel.start();
            } else if ("Mark Trials".equals(evt.getActionCommand())) {
//            @todo remove
                // Set calibrate panel offset
                markTrialJPanel.setEyeGazeScaleFactor(
                        screenFrameManager.getScreenInfoScalefactor());

                // need this to set the stat for frame playing (total frame and what not)
                markTrialJPanel.setEyeFrameManager(eyeFrameManager);
                markTrialJPanel.setScreenFrameManager(screenFrameManager);
                markTrialJPanel.setProjectRoot(projectLocation);
                markTrialJPanel.setFullScreenFrameDirectory(
                        projectSelectPanel.getFullScreenFrameDirectory());
                sync();
                markTrialJPanel.setFrameSynchronizor(this.frameSynchronizor);

                remove(projectSelectPanel);
                add(markTrialJPanel);
                pack();

                markTrialJPanel.start();
            } else if ("Export Data".equals(evt.getActionCommand())) {
                sync();
                exportData();
            } else if ("Export Movies".equals(evt.getActionCommand())) {
                sync();
                exportMovies();
            }
        }
    }

    private void menuActionPerformed(java.awt.event.ActionEvent evt) {
        JMenuItem item = (JMenuItem) evt.getSource();
        if ("New".equals(item.getText())) {
            performNewMenuAction();
        } else if ("Open".equals(item.getText())) {
            performOpenMenuAction();//pack();
        } else if ("Save".equals(item.getText())) {
            save();
        } else if ("Quit".equals(item.getText())) {
            formWindowClosed();
        }
    }

    private void sync() {
        // Read in synchronize data and set all offset in all panel
        this.frameSynchronizor.setSynchronizationPoints(
                this.synchronizeJPanel.getSynchronizationPoints(),
                this.eyeFrameManager.getTotalFrames(),
                this.screenFrameManager.getTotalFrames());
    }

    private void viewMenuActionPerformed(ActionEvent evt) {
        JMenuItem item = (JMenuItem) evt.getSource();
        if (primaryMenuItem.getText().equals(item.getText())) {
            this.eyeGazeComputing.setComputingApproach(
                    EyeGazeComputing.ComputingApproach.PRIMARY);
        } else if (secondaryMenuItem.getText().equals(item.getText())) {
            this.eyeGazeComputing.setComputingApproach(
                    EyeGazeComputing.ComputingApproach.SECONDARY);
        } else if (linearMenuItem.getText().equals(item.getText())) {
            this.eyeGazeComputing.setComputingApproach(
                    EyeGazeComputing.ComputingApproach.LINEAR);
        }
    }

    private void exportMenuActionPerformed(ActionEvent evt) {
        JMenuItem item = (JMenuItem) evt.getSource();
        if (calibrationPointsExportMenuItem.getText().equals(item.getText())) {
            // Export menu action
            exportCalibrationPointInfo();
        }
    }

    private void importMenuActionPerformed(ActionEvent evt) {

        String importLocation = null;
        String type = null;

        JMenuItem item = (JMenuItem) evt.getSource();
        if (eyeFrameImportMenuItem.getText().equals(item.getText())) {
            importLocation = this.projectSelectPanel.getEyeFrameDirectory();
            type = "eye";
        } else if (sceneFrameImportMenuItem.getText().equals(item.getText())) {
            importLocation = this.projectSelectPanel.getScreenFrameDirectory();
            type = "scene";
        }
        // Sanity check
        if (importLocation == null || importLocation.trim().length() < 1) {
            // Prompt user to enter the location
            JOptionPane.showMessageDialog(this,
                    "You need to specify " + type + " frame directory in the project.",
                    "Missing Information", JOptionPane.ERROR_MESSAGE);
            return;
        } else {

            ImportMovieJFrame importMovieJFrame = new ImportMovieJFrame(new File(importLocation));
            importMovieJFrame.setTitle("Importing " + type + " movie frames");
            importMovieJFrame.setImageFilePrefix(type + "_");
            importMovieJFrame.setVisible(true);

        }
    }

    /** Handel new project menu action */
    private void performNewMenuAction() {
        NewProjectJDialog dialog = new NewProjectJDialog(this, "Create new project", true);
        dialog.addActionListener(new ProjectCreateActionListener(dialog));
        dialog.setVisible(true);
    }

    /**Class to help with handling dialog*/
    private class ProjectCreateActionListener implements ActionListener {

        NewProjectJDialog dialog;

        ProjectCreateActionListener(NewProjectJDialog d) {
            dialog = d;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getActionCommand().equals("Create")) {
                // Check if the directory already exists
                File location = new File(dialog.getProjectLocation());
                if (!location.exists()) {
                    location.mkdirs();
                }
                frameSynchronizor.setSynchronizationPoints(null, 1, 1);
                createProject(location);
            } else {
                // Does nothing
            }
            dialog.setVisible(false);
            dialog.dispose();
            // Remove dialog
            dialog = null;
        }
    }

    private void createProject(File location) {
        // create project
        switch (openProject(location, true)) {
            case ERROR_OPENING_DATABASE:
                // Something is wrong tell user
                JOptionPane.showMessageDialog(this,
                        "<html>There is an error opening file databases. The "
                        + "project may already be open or the project is located "
                        + "in a network drive.</html>",
                        "Problem Creating Project", JOptionPane.ERROR_MESSAGE);
                break;
            case ERROR_OPENING_PROJECT_FILE:
                // Something is wrong tell user
                JOptionPane.showMessageDialog(this,
                        "<html>There is an error creating the project file. "
                        + "Please check file permissions.</html>",
                        "Problem Creating Project", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    /** Handel open project menu action */
    private void performOpenMenuAction() {
        // Choose new directory to put the project
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            boolean isNotCancel = true;

            // Stop everything from old project
            if (isProjectOpen) {
                isNotCancel = closeProject();
            }

            if (isNotCancel) {
                // Switch to project select view in disable mode
                projectSelectPanel.setEnabled(false);
                remove(projectSelectPanel);
                remove(synchronizeJPanel);
                remove(calibrateJPanel);
                remove(cleanDataJPanel);
                remove(markTrialJPanel);
                add(projectSelectPanel);
                pack();
                // Switch to selected project
                switch (openProject(fileChooser.getSelectedFile(), false)) {
                    case ERROR_OPENING_DATABASE:
                        // Something is wrong tell user
                        JOptionPane.showMessageDialog(this,
                                "<html>There is an error opening file databases. "
                                + "The project may already be open or the project is "
                                + "located in a network drive.</html>",
                                "Problem Opening Project", JOptionPane.ERROR_MESSAGE);
                        break;
                    case ERROR_OPENING_PROJECT_FILE:
                        // Something is wrong tell user
                        JOptionPane.showMessageDialog(this,
                                "<html>There is an error opening the project file. "
                                + "Please check file permission.</html>",
                                "Problem Opening Project", JOptionPane.ERROR_MESSAGE);
                        break;
                    case PROJECT_FILE_NOT_FOUND:
                        // Something is wrong tell user
                        JOptionPane.showMessageDialog(this,
                                "<html>This is not a project folder.  Please select "
                                + "a different folder.</html>",
                                "Problem Opening Project", JOptionPane.ERROR_MESSAGE);
                        break;
                }
            }
        }
    }

    /**
     * Open a project.
     * @param createNew If create is true the method create a project files when there is no project in the given directory
     * @return true when successful
     */
    private enum OpenProjectError {

        PROJECT_FILE_NOT_FOUND, ERROR_OPENING_PROJECT_FILE, ERROR_OPENING_DATABASE, NO_ERROR
    }

    private OpenProjectError openProject(File projectLocation, boolean create) {
        // Set project location
        this.projectLocation = projectLocation;


        // Load project properties if exists
        Properties p = new Properties();
        try {
            p.loadFromXML(new FileInputStream(new File(projectLocation, PROJECT_PROPERTY_FILE_NAME)));
        } catch (FileNotFoundException ex) {
            if (!create) {
                System.err.println("Cannot load project property file.");
                return OpenProjectError.PROJECT_FILE_NOT_FOUND;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            if (!create) {
                return OpenProjectError.ERROR_OPENING_PROJECT_FILE;
            }
        }

        this.isProjectOpen = true;

        //Apply default when new
        projectSelectPanel.setEyeFrameDirectory(
                createPath(p.getProperty(EYE_VIEW_DIRECTORY),
                projectLocation, DEFAULT_EYE_FRAME_PATH));
        projectSelectPanel.setEyeInfoDirectory(
                createPath(p.getProperty(EYE_INFO_DIRECTORY),
                projectLocation, DEFAULT_EYE_INFO_PATH));
        projectSelectPanel.setScreenFrameDirectory(
                createPath(p.getProperty(SCREEN_VIEW_DIRECTORY),
                projectLocation, DEFAULT_SCENE_FRAME_PATH));
        projectSelectPanel.setFullScreenFrameDirectory(
                p.getProperty(FULL_SCREEN_VIEW_DIRECTORY));
        projectSelectPanel.setScreenInfoDirectory(
                createPath(p.getProperty(SCREEN_INFO_DIRECTORY),
                projectLocation, DEFAULT_SCENE_INFO_PATH));
        projectSelectPanel.setMonitorDimensionPX(
                p.getProperty(MONITOR_TRUE_WIDTH_PX, ""),
                p.getProperty(MONITOR_TRUE_HEIGHT_PX, ""));
        projectSelectPanel.setFullSceneDimensionPX(
                p.getProperty(FULL_SCREEN_WIDTH, ""),
                p.getProperty(FULL_SCREEN_HEIGHT, ""));
        projectSelectPanel.setComment(p.getProperty(COMMENT, ""));
        projectSelectPanel.setDistanceFromMeasuredScene(
                p.getProperty(DISTANCE_FROM_MONITOR_CM, "0"));
        projectSelectPanel.setSceneHeightCM(p.getProperty(MONITOR_TRUE_HEIGHT_CM, "0"));
        projectSelectPanel.setSceneWidthCM(p.getProperty(MONITOR_TRUE_WIDTH_CM, "0"));

        // Trying to determine full screen dimension
        try {
            eyeFrameManager = new FrameManager(projectLocation.getAbsolutePath()
                    + File.separator + "EyeViewCacheDB", 512, 512, new EyeViewFrameInfo());
            // Open a project
            screenFrameManager = new ScreenFrameManager(projectLocation.getAbsolutePath()
                    + File.separator + "ScreenViewCacheDB", 512, 512, new ScreenViewFrameInfo());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error open project", JOptionPane.ERROR_MESSAGE);
            return OpenProjectError.ERROR_OPENING_DATABASE;
        }

        // Set up scaling factor of the screen info
        updateFullScreenDimansion();

        // Set up scaling factor of the screen info
        Dimension d = projectSelectPanel.getFullSceneDimensionPX();

        // Assign frame manager to all panel
        synchronizeJPanel.setEyeFrameManager(eyeFrameManager);
        synchronizeJPanel.setSceneFrameManager(screenFrameManager);
        calibrateJPanel.setEyeFrameManager(eyeFrameManager);
        calibrateJPanel.setScreenFrameManager(screenFrameManager);
        cleanDataJPanel.setEyeFrameManager(eyeFrameManager);
        cleanDataJPanel.setScreenFrameManager(screenFrameManager);
        markTrialJPanel.setEyeFrameManager(eyeFrameManager);
        markTrialJPanel.setScreenFrameManager(screenFrameManager);

        // Set up eye gaze coeff
        this.eyeGazeComputing.setPrimaryEyeCoeff(calibrateJPanel.getEyeGazeCoefficient(0));
        this.eyeGazeComputing.setSecondaryEyeCoeff(calibrateJPanel.getEyeGazeCoefficient(1));
        // Check if using cornea reflection
        this.calibrateJPanel.setUsingCorneaReflect(
                Boolean.parseBoolean(p.getProperty(USING_CORNEA_REFLECTION, "true")));


        // Load synch file if exists
        File syncFile = new File(projectLocation, SYNC_FILE_NAME);
        if (syncFile.exists()) {
            synchronizeJPanel.loadSynchronizationPoints(syncFile);
        } else {
            synchronizeJPanel.clear();
        }

        /**
         * check if sync file is loaded.  If not then try loading from project in case
         * of old project file.  This exists to provide backward compatibility */
        SynchronizationPoint[] sps = synchronizeJPanel.getSynchronizationPoints();
        if (sps == null || sps.length < 1) {
            sps = new SynchronizationPoint[1];
            sps[0] = new SynchronizationPoint(
                    Integer.parseInt(p.getProperty(EYE_OFFSET, "0")),
                    Integer.parseInt(p.getProperty(SCREEN_OFFSET, "0")));
            this.synchronizeJPanel.addSyncPoint(sps[0]);
        }
        /**--End backward compatimility support---*/
        /** Set frame sync accordingly.  This must be done before we load any other things */
        this.frameSynchronizor.setSynchronizationPoints(sps,
                eyeFrameManager.getTotalFrames(),
                screenFrameManager.getTotalFrames());

        // Link frame manager to update progress to project panel
        eyeFrameManager.setLoadingListener(projectSelectPanel.getEyeFrameLoadingListener());
        screenFrameManager.setLoadingListener(projectSelectPanel.getScreenFrameLoadingListener());

        this.calibrateJPanel.setFrameSynchronizor(frameSynchronizor);
        this.markTrialJPanel.setFrameSynchronizor(frameSynchronizor);
        this.cleanDataJPanel.setFrameSynchronizor(frameSynchronizor);

        // Load calibration file if exists
        File calibrationFile = new File(projectLocation, CALIBRATION_FILE_NAME);
        if (calibrationFile.exists()) {
            calibrateJPanel.loadCalibrationPoints(calibrationFile);
        } else {
            calibrateJPanel.clearCalibrationInfo();
        }
        // Set up calibration full screen dia
        calibrateJPanel.setFullScreenDim(d);

        // Load trial file if exists
        File trialMarkFile = new File(projectLocation, TRIAL_FILE_NAME);
        if (trialMarkFile.exists()) {
            markTrialJPanel.loadTrialMarks(trialMarkFile);
        } else {
            markTrialJPanel.clear();
        }

        // Load error file if exists
        File errorFile = new File(projectLocation, ERROR_FILE_NAME);
        if (errorFile.exists()) {
            cleanDataJPanel.loadErrors(errorFile);
        } else {
            cleanDataJPanel.clear();
        }

        // SEt project title
        this.setTitle(projectLocation.getName());

        // Open illumination database
        try {
            File databaseLocation = new File(projectLocation, DATABASE_NAME);
            this.informationDatabase = new InformationDatabase(databaseLocation.getAbsolutePath());
            // Give illumination database to mark trial panel
            markTrialJPanel.setInformationDatabase(informationDatabase);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        projectSelectPanel.setEnabled(true);

        pack();

        return OpenProjectError.NO_ERROR;
    }

    /**
     * Compute scaling factor
     */
    private double computeScalingFactor() {

        // Get fullscreen dimension
        Dimension d = projectSelectPanel.getFullSceneDimensionPX();
        // Get the first image file
        BufferedImage image = screenFrameManager.getFrame(1);

        // Only do anything when there is somthing to compute
        if (image == null || d == null) {
            return 1d;
        } else {
            return ((double) image.getWidth()) / ((double) d.width);
        }
    }

    private void formWindowClosed() {
        boolean isNotCanceled = true;
        if (isProjectOpen) {
            isNotCanceled = closeProject();
        }
        if (isNotCanceled) {
            System.exit(0);
        }
    }

    private void save() {
        // Get new screen dimension
        Dimension d = projectSelectPanel.getMonitorDimensionPX();
        calibrateJPanel.setFullScreenDim(d);

        // Save calibration points
        calibrateJPanel.saveCalibrationPoints(new File(projectLocation, CALIBRATION_FILE_NAME));

        // Save error information
        cleanDataJPanel.saveErrors(new File(projectLocation, ERROR_FILE_NAME));

        // Save trial mark information
        markTrialJPanel.saveTrialMarks(new File(projectLocation, TRIAL_FILE_NAME));

        // Save sync points information
        synchronizeJPanel.saveSynchronizationPoints(new File(projectLocation, SYNC_FILE_NAME));

        // Save project property
        Properties p = new Properties();
        p.setProperty(EYE_VIEW_DIRECTORY, projectSelectPanel.getEyeFrameDirectory());
        p.setProperty(EYE_INFO_DIRECTORY, projectSelectPanel.getEyeInfoDirectory());
        p.setProperty(SCREEN_VIEW_DIRECTORY, projectSelectPanel.getScreenFrameDirectory());
        p.setProperty(FULL_SCREEN_VIEW_DIRECTORY, projectSelectPanel.getFullScreenFrameDirectory());
        p.setProperty(SCREEN_INFO_DIRECTORY, projectSelectPanel.getScreenInfoDirectory());
        p.setProperty(COMMENT, projectSelectPanel.getComment());
        if (d != null) {
            p.setProperty(MONITOR_TRUE_HEIGHT_PX, String.valueOf(d.height));
            p.setProperty(MONITOR_TRUE_WIDTH_PX, String.valueOf(d.width));
        }
        p.setProperty(MONITOR_TRUE_HEIGHT_CM, String.valueOf(
                projectSelectPanel.getSceneHeightCM()));
        p.setProperty(MONITOR_TRUE_WIDTH_CM, String.valueOf(
                projectSelectPanel.getSceneWidthCM()));
        p.setProperty(DISTANCE_FROM_MONITOR_CM, String.valueOf(
                projectSelectPanel.getDistanceFromMeasuredScene()));
        p.setProperty(USING_CORNEA_REFLECTION, String.valueOf(
                this.eyeGazeComputing.isUsingCorneaReflect()));
        d = projectSelectPanel.getFullSceneDimensionPX();
        if (d != null) {
            p.setProperty(FULL_SCREEN_HEIGHT, String.valueOf(d.height));
            p.setProperty(FULL_SCREEN_WIDTH, String.valueOf(d.width));
        }
        try {
            p.storeToXML(new FileOutputStream(new File(this.projectLocation,
                    PROJECT_PROPERTY_FILE_NAME)), null);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Exporting information to files
     */
    private void exportData() {
        // Get screen dimension
        Dimension realMonitorDimension = projectSelectPanel.getMonitorDimensionPX();
        Dimension screenViewFullSize = projectSelectPanel.getFullSceneDimensionPX();

        // Sanity check and warning
        if (realMonitorDimension == null) {
            JOptionPane.showMessageDialog(this,
                    "<html>Monitor dimension is missing.  Data exported will "
                    + "not have projected monitor coordinates.</html>",
                    "Monitor dimension is missing.",
                    JOptionPane.WARNING_MESSAGE);
        }


        // Check for eye calibration vector
        double[][] gazeCoefficient = calibrateJPanel.getEyeGazeCoefficient(0);

        if (gazeCoefficient == null) {
            // Show error that there is no gaze coefficient
            JOptionPane.showMessageDialog(null,
                    "<html>Eye gaze has not been calibrated</html>",
                    "Error getting eye gaze coefficients",
                    JOptionPane.ERROR_MESSAGE);
            // Does nothing more and waiting to return
        } else {
            PrintWriter exportWriter = null;

            // Get the file to save raw info
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("Data export file");
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    exportWriter = new PrintWriter(fileChooser.getSelectedFile());
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Print header
                exportWriter.println(
                        "Screen Frame\t"
                        + "Pupil x\t"
                        + "Pupil y\t"
                        + "Pupil fit top left x\t"
                        + "Pupil fit topleft y\t"
                        + "Pupil fit bottom right x\t"
                        + "Pupil fit bottom right y\t"
                        + "Cornia reflect x\t"
                        + "Cornia reflect y\t"
                        + "Raw primary x\t" + // This is a gaze coor on the screen view
                        "Raw primary y\t"
                        + "Raw secondary x\t" + // This is a gaze coor on the screen view
                        "Raw secondary y\t"
                        + "Raw linear interpolated x\t" + // This is a gaze coor on the screen view
                        "Raw linear interpolated y\t"
                        + "Scene primary x\t"
                        + "Scene primary y\t"
                        + "Scene secondary x\t"
                        + "Scene secondary y\t"
                        + "Scene linear interpolated x\t"
                        + "Scene linear interpolated y\t"
                        + "Similarity of topleft\t"
                        + "Similarity of topright\t"
                        + "Similarity of bottomleft\t"
                        + "Similarity of bottomright\t"
                        + "Error\t"
                        + "Trial file name\t"
                        + "Trial number\t"
                        + "Eye Frame File\t"
                        + "Scene Frame File");

                // Variables
                Point2D fixation = new Point2D.Double(GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE);

                // Get a trial set
                TrialMarker[] trials = this.markTrialJPanel.getTrials();
                int trialNumber = 0;
                String trialName = null;
                int calibrationNumber = 0;
                String calibrationName = null;


                // Get a error set
                Iterator<ErrorMarking> errorIter = cleanDataJPanel.getCompressedErrorMarkingSet().iterator();
                ErrorMarking error = null;
                if (errorIter.hasNext()) {
                    error = errorIter.next();
                }

                for (int i = 1; i <= this.frameSynchronizor.getTotalFrame(); i++) {
                    // Get the current trial number
                    while (trialNumber < trials.length
                            && (this.frameSynchronizor.getSceneFrame(i) > trials[trialNumber].stopSceneFrame)) {
                        // Move to the next trial
                        trialNumber++;
                    }
                    // Get the trial name when appropriate
                    if (trialNumber < trials.length
                            && this.frameSynchronizor.getSceneFrame(i) >= trials[trialNumber].startSceneFrame) {
                        // Set new trial name
                        trialName = trials[trialNumber].label;
                    } else {
                        // Clear the name
                        trialName = null;
                    }

                    /** If not a trial check if this is a calibration */
                    if (trialName == null) {
                        CalibationPoint calibrationPoint = calibrateJPanel.frameToCalibrationPoint(
                                this.frameSynchronizor.getSceneFrame(i));
                        if (calibrationPoint != null) {
                            calibrationName = "C_" + calibrationPoint.location.x + "_"
                                    + calibrationPoint.location.y + "_" + calibrationPoint.type;
                            calibrationNumber = -(calibrationPoint.location.x
                                    + (calibrationPoint.location.y - 1)
                                    * CalibrateJPanel.TOTAL_CALIBRATION_X);
                        } else {
                            calibrationName = null;
                        }
                    }

                    // Get eyeInfo
                    EyeViewFrameInfo eyeInfo =
                            (EyeViewFrameInfo) eyeFrameManager.getFrameInfo(
                            this.frameSynchronizor.getEyeFrame(i));

                    // Skip a frame when there is no eye information
                    if (eyeInfo != null) {
                        double[] pupilFit = {GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE};
                        if (eyeInfo.getPupilFit() != null) {
                            pupilFit = eyeInfo.getPupilFit();
                        }

                        // Write current Frame, Pupil(x,y)
                        exportWriter.print(i + "\t"
                                + eyeInfo.getPupilX() + "\t"
                                + eyeInfo.getPupilY() + "\t");

                        // Write Pupil fit topleft x,y bottom right (x,y)
                        for (int j = 0; j < pupilFit.length; j++) {
                            exportWriter.print(pupilFit[j] + "\t");
                        }

                        // Write Cornia reflect (x,y), Gaze on screen view (x,y)
                        exportWriter.print(eyeInfo.getCorneaReflectX() + "\t"
                                + eyeInfo.getCorneaReflectX() + "\t");


                        // For storing gaze point
                        Point2D.Double point[] = new Point2D.Double[3];

                        // Don't compute eye gaze when the trial is marked bad
                        if (trialName != null && trials[trialNumber].isBadTrial) {
                            // Just put blank
                            exportWriter.print(
                                    GlobalConstants.ERROR_VALUE + "\t" + GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t" + GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t" + GlobalConstants.ERROR_VALUE + "\t");
                        } else {
                            // Compute eye vector
                            Point2D.Double vector = this.eyeGazeComputing.getEyeVector(eyeInfo);

                            EyeGazeComputing.ComputingApproach approach =
                                    EyeGazeComputing.ComputingApproach.PRIMARY;

                            // Compute eye gaze
                            point[0] = exportEyeGazes(exportWriter, i, vector,
                                    EyeGazeComputing.ComputingApproach.PRIMARY,
                                    screenViewFullSize);
                            point[1] = exportEyeGazes(exportWriter, i, vector,
                                    EyeGazeComputing.ComputingApproach.SECONDARY,
                                    screenViewFullSize);
                            point[2] = exportEyeGazes(exportWriter, i, vector,
                                    EyeGazeComputing.ComputingApproach.LINEAR,
                                    screenViewFullSize);
                        }

                        // Get screen info
                        ScreenViewFrameInfo sceneInfo =
                                (ScreenViewFrameInfo) screenFrameManager.getFrameInfo(
                                this.frameSynchronizor.getSceneFrame(i));

                        // Set default value for not available
                        fixation.setLocation(GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE);

                        if (sceneInfo != null) {
                            Point2D[] corners = sceneInfo.getCorners();

                            if (corners != null && corners[0] != null
                                    && corners[1] != null && corners[2] != null
                                    && corners[3] != null) {
                                for (int j = 0; j < point.length; j++) {
                                    // Only estimate fixation when this is not a bad trial
                                    if (trialName != null && trials[trialNumber].isBadTrial) {
                                        exportWriter.print(
                                                GlobalConstants.ERROR_VALUE + "\t" + GlobalConstants.ERROR_VALUE + "\t");
                                    } else {
                                        // Compute fixation
                                        fixation = Computation.ComputeScreenPositionProjective(
                                                realMonitorDimension, point[j],
                                                corners[ScreenViewFrameInfo.TOPLEFT],
                                                corners[ScreenViewFrameInfo.TOPRIGHT],
                                                corners[ScreenViewFrameInfo.BOTTOMLEFT],
                                                corners[ScreenViewFrameInfo.BOTTOMRIGHT]);

                                        if (fixation == null) {
                                            fixation = new Point2D.Double(GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE);
                                        }
                                        if ((point[j].x < 0 && point[j].y < 0)
                                                || fixation.getX() < 0 || fixation.getY() < 0
                                                || fixation.getX() > realMonitorDimension.width
                                                || fixation.getY() > realMonitorDimension.height) {
                                            fixation.setLocation(GlobalConstants.ERROR_VALUE, GlobalConstants.ERROR_VALUE);
                                        }

                                        exportWriter.print(
                                                fixation.getX() + "\t" + fixation.getY() + "\t");
                                    }
                                }

                                double[] similarities = sceneInfo.similarities;
                                // Write Gaze on monitor (x,y), Similarity (topleft, topright, bottomleft, bottomright)
                                exportWriter.print(
                                        similarities[ScreenViewFrameInfo.TOPLEFT] + "\t"
                                        + similarities[ScreenViewFrameInfo.TOPRIGHT] + "\t"
                                        + similarities[ScreenViewFrameInfo.BOTTOMLEFT] + "\t"
                                        + similarities[ScreenViewFrameInfo.BOTTOMRIGHT] + "\t");
                            } else {
                                // Just put blank
                                exportWriter.print(
                                        GlobalConstants.ERROR_VALUE + "\t"
                                        + GlobalConstants.ERROR_VALUE + "\t"
                                        + GlobalConstants.ERROR_VALUE + "\t"
                                        + GlobalConstants.ERROR_VALUE + "\t"
                                        + GlobalConstants.ERROR_VALUE + "\t"
                                        + GlobalConstants.ERROR_VALUE + "\t"
                                        + GlobalConstants.ERROR_VALUE + "\t"
                                        + GlobalConstants.ERROR_VALUE + "\t"
                                        + GlobalConstants.ERROR_VALUE + "\t"
                                        + GlobalConstants.ERROR_VALUE + "\t");
                            }
                        } else {
                            // Just put blank
                            exportWriter.print(
                                    GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t"
                                    + GlobalConstants.ERROR_VALUE + "\t");
                        }


                        // Processing error code
                        long errorValue = 0;
                        int eyeFrame = this.frameSynchronizor.getEyeFrame(i);
                        int sceneFrame = this.frameSynchronizor.getSceneFrame(i);
                        if (error != null
                                && ((error.startEyeFrame > 0
                                && error.startEyeFrame <= eyeFrame)
                                || (error.startSceneFrame > 0
                                && error.startSceneFrame <= sceneFrame))) {
                            // Check if it's in range
                            if ((error.stopEyeFrame > 0
                                    && eyeFrame <= error.stopEyeFrame)
                                    || (error.stopSceneFrame > 0
                                    && sceneFrame <= error.stopSceneFrame)) {
                                errorValue = error.getErrorCode();
                            } else {
                                // Mismatch. Try finding the next one
                                while (errorIter.hasNext()
                                        && ((error.stopEyeFrame > 0
                                        && error.stopEyeFrame < eyeFrame)
                                        || (error.stopSceneFrame > 0
                                        && error.stopSceneFrame < sceneFrame))) {
                                    error = errorIter.next();
                                }
                                // Check if we found one
                                if (error.startEyeFrame <= i) {
                                    if (i <= error.stopEyeFrame) {
                                        // Found it so store
                                        errorValue = error.getErrorCode();
                                    } else {
                                        // Nothing is found and we are out of error so stop searching
                                        error = null;
                                    }
                                }
                            }
                        }
                        // Shift one bit to the most significant bit and store bad trial bit
                        errorValue *= 2;
                        if (trialName != null && trials[trialNumber].isBadTrial) {
                            errorValue += 1;
                        }

                        exportWriter.print(errorValue + "\t");

                        if (trialName != null) {
                            exportWriter.print(trials[trialNumber].label + "\t" + trialNumber);
                        } else {
                            if (calibrationName != null) {
                                exportWriter.print(calibrationName + "\t" + calibrationNumber);
                            } else {
                                exportWriter.print("-\t" + GlobalConstants.ERROR_VALUE);
                            }
                        }

                        /** Print eye frame name */
                        String name = eyeFrameManager.getFrameFileName(
                                this.frameSynchronizor.getEyeFrame(i));
                        if (name != null) {
                            exportWriter.print("\t" + name);
                        } else {
                            exportWriter.print("\t-");
                        }
                        name = screenFrameManager.getFrameFileName(
                                this.frameSynchronizor.getSceneFrame(i));
                        if (name != null) {
                            exportWriter.println("\t" + name);
                        } else {
                            exportWriter.println("\t-");
                        }
                    }
                }
                if (exportWriter != null) {
                    exportWriter.close();
                }
            }
        }
    }

    private void updateFullScreenDimansion() {
        // Trying to determine full screen dimension
        String filename = screenFrameManager.getFrameFileName(1);

        if (filename != null
                && projectSelectPanel.getFullScreenFrameDirectory() != null) {
            // Use screen screen dir if full screen does not exists
            String fullScreenDir = projectSelectPanel.getFullScreenFrameDirectory();
            if (fullScreenDir.length() < 1) {
                fullScreenDir = projectSelectPanel.getScreenFrameDirectory();
            }

            File fullScreenFile = new File(fullScreenDir, filename);
            if (fullScreenFile.exists()) {
                BufferedImage image = ImageTools.loadImage(fullScreenFile);
                projectSelectPanel.setFullSceneDimensionPX(
                        String.valueOf(image.getWidth()),
                        String.valueOf(image.getHeight()));
            }
        }
    }
}
