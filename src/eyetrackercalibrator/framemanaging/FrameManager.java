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
 * FrameManager.java
 *
 * Created on September 11, 2007, 10:21 AM
 *
 * This class manage buffering of frames (pictures file) The frame is accessible
 * in the form of ImageIconIcon class.  Current implementation uses Berkley Db to
 * to manage picture buffering.  The access time should be faster than loading
 * a picture directly.  While it will be slower than loading all picture into
 * memory using Berkley Db will remove memory requirment limit of the software.
 * As long as we have enough hard drive, the software will run.
 *
 * Imoprtant!! close() method has to be called before destroying this class otherwise
 * it will damage the internal information database
 */
package eyetrackercalibrator.framemanaging;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

/**
 *
 * @author eeglab
 */
public class FrameManager {

    Logger logger;
    /** Location of database */
    protected String databaseHome = null;
    /** This database environment */
    protected Environment frameDBEnv = null;
    /** Table of (Frame ID , Frame) */
    protected Database frameDB = null;
    /** Table of (Frame ID, Frame Info) */
    protected Database frameInfoDB;
    /** Class catalog for database storage*/
    protected StoredClassCatalog javaCatalog = null;
    /** Frame Number to Frame Map */
    protected StoredMap numberToFrameMap = null;
    /** Frame Number to Frame Info Map */
    protected StoredMap numberToFrameInfoMap;
    /** Total frames */
    protected int totalFrames = 0;
    /** Current frames loaded */
    protected int currentFrameLoaded = 0;
    /** Frame Info class */
    protected FrameInfo frameInfoClass;
    protected int width, height;
    protected FrameLoadingListener frameLoadingListener = null;
    private String frameDirectory = null;
    protected boolean scalePicture = false;

    /**
     * Creates a new instance of FrameManager.  A database location is specified
     * if it is exists it will be loaded otherwise created.  If the database
     * already populated, there is no need to call loadFrames again.
     * The height and width is assumed to be the same as the image cached.  If
     * not the picture will not come out correctly.
     * @param databaseDirectory Database directory to store image
     * @param height Height of the image to be cache (not original image size)
     * @param width Width of the image to be cache (not original image size)
     */
    public FrameManager(String databaseDirectory, int height, int width,
            FrameInfo frameInfoClass) throws IOException {
        this.width = width;
        this.height = height;
        this.frameInfoClass = frameInfoClass;

        logger = Logger.getLogger("eyetrackercalibrator");

        /* Set location of database */
        File databaseHomeFile = new File(databaseDirectory);
        // Check if directory already exists
        if (databaseHomeFile.exists()) {
            // Check if it's really a directory
            if (!databaseHomeFile.isDirectory()) {
                throw new IOException(databaseHomeFile.toString() + " is not a directory.");
            }// Else OK.
        } else {
            // Directory does not exist, create one
            databaseHomeFile.mkdir();
        }
        // All successful, set the database location
        this.databaseHome = databaseHomeFile.getAbsolutePath();

        // Simply passing in dummy class
        openDatabase(frameInfoClass);
    }

    /**
     * Method for opening database.  This is needed to be called first before
     * any other operation
     */
    private void openDatabase(FrameInfo frameInfo) throws IOException {
        try {
            /*--Create database environment (Has to be done before creating database--*/
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            envConfig.setTransactional(true);
            frameDBEnv = new Environment(new File(databaseHome), envConfig);

            /*--Create database from environment--*/
            // Create database config before database
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setTransactional(true);

            /*--Create catalog for information storage--*/
            Database catalogDb = frameDBEnv.openDatabase(null, "Frame database class catalog", dbConfig);
            javaCatalog = new StoredClassCatalog(catalogDb);

            // open (local ID, Picture) table
            frameDB = frameDBEnv.openDatabase(null, "Frame Database", dbConfig);

            // Open (local ID, Frame Info) table
            frameInfoDB = frameDBEnv.openDatabase(null, "Frame Info Database", dbConfig);

        } catch (Exception e) {
            IOException myException = new IOException("Error in creating database to store Frame Info.");
            myException.setStackTrace(e.getStackTrace());
            close(); // Close database properly
            throw myException;
        }

        // Create frame info (ImageIcon) binding
        EntryBinding frameInfoBinding =
                new SerialBinding(javaCatalog, frameInfo.getClass());

        numberToFrameInfoMap =
                new StoredMap(frameInfoDB, new IntegerBinding(), frameInfoBinding, true);

        numberToFrameMap =
                new StoredMap(frameDB, new IntegerBinding(), new StringBinding(), true);

        // Set initial size
        totalFrames = numberToFrameMap.size();
    }

    /**
     * Method for properly disposing of the class.  Once call the class instance
     * can nolonger be use.
     */
    public void close() {
        if (frameDB != null) {
            try {
                frameDB.close();
                frameDB = null;
            } catch (DatabaseException ex) {
//                Logger.getLogger(FrameManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (frameInfoDB != null) {
            try {
                frameInfoDB.close();
                frameInfoDB = null;
            } catch (DatabaseException ex) {
//                Logger.getLogger(FrameManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (javaCatalog != null) {
            try {
                javaCatalog.close();
                javaCatalog = null;
            } catch (DatabaseException ex) {
//                Logger.getLogger(FrameManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (frameDBEnv != null) {
            try {
                frameDBEnv.close();
                frameDBEnv = null;
            } catch (DatabaseException ex) {
//                Logger.getLogger(FrameManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Load all pictures from directory into database.  This action remove all
     * previous information.
     */
    public boolean loadFrames(String frameDirectoryName, String frameInfoDirectoryName) {
        return loadFrames(frameDirectoryName, frameInfoDirectoryName, 0, 0, false);
    }

    /**
     * Load pictures from a file starting a file indicated by offset for a total
     * number as stated by "total" parameter.  A file is sorted by name in ascending
     * order.  So offset of 1 will skip the file name which is the first file after
     * sorted.
     * @param frameDirectoryName Directory containing pictures of frames
     * @param frameInfoDirectoryName Directory containing information of frames
     * @param offset Starting file. 0 indicate starting from the first file
     * @param total Total number of file to load.  0 indicates loading all files.
     * @param onlyLoadInfo Set to true to load only information corresponsing to a frame file
     * @return true when successful, false otherwise.
     */
    public boolean loadFrames(String frameDirectoryName,
            String frameInfoDirectoryName,
            int offset, int total, boolean onlyLoadInfo) {
        boolean success = true;

        // Use same dir with frame if not specify
        if (frameInfoDirectoryName == null) {
            frameInfoDirectoryName = frameDirectoryName;
        }

        File infoDir = new File(frameInfoDirectoryName);

        File dir = new File(frameDirectoryName);
        if (dir.isDirectory()) {
            // OK we have directory.  Get all file name
            String[] filenames = dir.list(new PictureFilenameFilter());
            this.setTotalFrames(filenames.length);

            // Update progress
            frameLoadingListener.update("Loading file listing", 0, 0, 100);

            // Recording number failing
            int totalFail = 0;

            if (filenames != null && filenames.length > 0) {
                // Sort file names
                Arrays.sort(filenames);
                // Set total number to read
                if (total == 0) {
                    total = filenames.length;
                } else {
                    total = Math.min(offset + total, filenames.length);
                }
                // Add all to database
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                for (int i = offset; i < filenames.length; i++) {
                    if (!onlyLoadInfo) {
                        numberToFrameMap.put(new Integer(i + 1), filenames[i]);
                    }
                    // Try reading associate information file (Assume same file name but .txt)
                    File infoFile = getInfoFile(infoDir, filenames[i]);
                    FrameInfo info = this.frameInfoClass.getInstance(infoFile,
                            new File(frameDirectoryName, filenames[i]),
                            (FrameInfo) numberToFrameInfoMap.get(i + 1));
                    if (info != null) {
                        // Set source file too
                        info.setSourceFileName(filenames[i]);

                        // Add to database
                        numberToFrameInfoMap.put(new Integer(i + 1), info);
                    } else {
                        // Remove information if it's not there
                        numberToFrameInfoMap.remove(new Integer(i + 1));

                        // Mark failing
                        totalFail++;
                    }

                    // Update progress
                    frameLoadingListener.update((i + 1) + " of " + filenames.length, i, totalFail, filenames.length);
                    //Thread.yield();
                }

                // Clear the left over information in case there are less files
                int currentSize = Math.max(numberToFrameInfoMap.size(), numberToFrameMap.size());
                if(currentSize > filenames.length){
                    // Get rid of the excess
                    for (int i = filenames.length + 1; i <= currentSize; i++) {
                        numberToFrameInfoMap.remove(i);
                        numberToFrameMap.remove(i);
                    }
                }

                // Update the last progress
                frameLoadingListener.update("Completed " + filenames.length + " frames", filenames.length, totalFail, filenames.length);
            } else {
                logger.info("No file is found in " + frameDirectoryName);
                success = false;
            }
        } else {
            logger.info(frameDirectoryName + " is not directory");
            success = false;
        }

        return success;
    }

    /**
     * This method return a File object of the info file.
     * @param dir Directory containing info files.  
     * @param fileName The eye screen file name
     */
    public File getInfoFile(File dir, String fileName) {
        // Parse file name to replace extension with .txt
        fileName = fileName.substring(0, fileName.lastIndexOf('.')).concat(".txt");

        return new File(dir, fileName);
    }

    public String getFrameDirectory() {
        return frameDirectory;
    }

    /**
     * For filtering .jpg, .gif, .png and .tif files
     */
    private class PictureFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File file, String string) {
            return !file.isHidden() && !string.startsWith(".") && (string.endsWith(".jpg") ||
                    string.endsWith(".png") || string.endsWith(".gif") ||
                    string.endsWith(".tif") || string.endsWith(".tiff"));
        }
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public void setTotalFrames(int totalFrames) {
        this.totalFrames = totalFrames;
    }

    public BufferedImage getFrame(Integer i) {
        return getFrame(frameDirectory, i);
    }

    public BufferedImage getFrame(String frameDir, Integer i) {
        String picData = (String) numberToFrameMap.get(i);
        if (picData != null) {
            RenderedOp op = null;
            try {
                op = JAI.create("fileload", frameDir + picData);
            } catch (java.lang.IllegalArgumentException e) {
                //e.printStackTrace();
                return null;
            }
            try {
                return op.getAsBufferedImage();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public String getFrameFileName(Integer i) {
        return (String) numberToFrameMap.get(i);
    }

    /** To get Frame info from frame manager
     * @param i
     * @return 
     */
    public FrameInfo getFrameInfo(Integer i) {
        return (FrameInfo) numberToFrameInfoMap.get(i);
    }

    /** To set frame info
     * @param i
     * @param frameInfo 
     */
    public void setFrameInfo(Integer i, FrameInfo frameInfo) {
        numberToFrameInfoMap.put(i, frameInfo);
    }

    /**
     *
     * program assume that there will be image files with corresponding text file
     * in the same directory
     * @param frameDirectoryName
     * @param frameInfoDirectoryName
     * @return 
     */
    public boolean loadFrameInfo(String frameDirectoryName, String frameInfoDirectoryName) {
        return loadFrames(frameDirectoryName, frameInfoDirectoryName, 0, 0, true);
    }

    public void setLoadingListener(FrameLoadingListener listener) {
        this.frameLoadingListener = listener;
        // Init listener
        if (getTotalFrames() < 1) {
            listener.update("N/A", 0, 0, 100);
        } else {
            listener.update("Completed total " + getTotalFrames(), getTotalFrames(), 0, getTotalFrames());
        }
    }

    public void setFrameDirectory(String frameDirectory) {
        File dir = new File(frameDirectory);
        this.frameDirectory = dir.getPath() + File.separator;
    }
}
