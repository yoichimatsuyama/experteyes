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

package eyetrackercalibrator.util;

import eyetrackercalibrator.GlobalConstants;
import eyetrackercalibrator.gui.ExportMovieJFrame;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author SQ
 */
public class FFMPEGHandler {

    public static String FFMPEG_LOCATION_PROPERTY_KEY = "ffmpeg location";

    /**
     * This method try to load ffmpeg executable file according to the property
     * file.  If fail, the user will be asked for the location.
     *
     * @return null when cannot locate ffmpeg file, otherwise returns the file
     * pointing to ffmpeg executable file.
     *
     */
    public static File getFFMPEGExecutable(Component parent) {
        File ffmpegFile = null;

        // Load ffmpeg location from property file
        File propertyFile = new File(GlobalConstants.PROPERTY_FILE);
        Properties properties = new Properties();
        try {
            FileInputStream fileInputStream = null;
            fileInputStream = new FileInputStream(propertyFile);
            properties.loadFromXML(fileInputStream);
            String ffmpegLocationStr = properties.getProperty(FFMPEG_LOCATION_PROPERTY_KEY);
            if (ffmpegLocationStr != null) {
                ffmpegFile = new File(ffmpegLocationStr);
            }
            fileInputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ExportMovieJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (ffmpegFile == null || !ffmpegFile.exists()) {
            // We don't have valid copy of ffmpeg.  Ask user to give one
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Please Locate FFMPEG Executable");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                ffmpegFile = chooser.getSelectedFile();

                // Save the selection

                // If old file exists load it
                if (propertyFile.exists()) {
                    try {
                        FileInputStream fileInputStream = null;
                        fileInputStream = new FileInputStream(propertyFile);
                        properties.loadFromXML(fileInputStream);
                        fileInputStream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ExportMovieJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        // Make one
                        propertyFile.createNewFile();
                    } catch (IOException ex) {
                        Logger.getLogger(ExportMovieJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // Set new property
                properties.setProperty(FFMPEG_LOCATION_PROPERTY_KEY, ffmpegFile.getAbsolutePath());

                // Save the config
                FileOutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(GlobalConstants.PROPERTY_FILE);
                    properties.storeToXML(outputStream, null);
                    outputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(ExportMovieJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            return null;
        }

        // Sanity check
        if (ffmpegFile != null && ffmpegFile.exists()) {
            return ffmpegFile;
        }else{
            return null;
        }
    }
}
