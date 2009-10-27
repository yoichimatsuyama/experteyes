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
package eyetrackercalibrator.framemanaging;

import com.sleepycat.bind.tuple.DoubleBinding;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author SQ
 */
public class InformationDatabase {

    private Logger logger;
    private Environment infoDBEnv;
    private Database infoDB;
    private StoredMap frameNumberToDoubleInfoMap;
    private int largestKey = 0;

    public InformationDatabase(String databaseDirectory) throws IOException {
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
        // Simply passing in dummy class
        openDatabase(databaseHomeFile);
    }

    public void close() {
        if (infoDB != null) {
            try {
                infoDB.close();
                infoDB = null;
            } catch (DatabaseException ex) {
//                Logger.getLogger(InformationDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//            if (javaCatalog != null) {
//                javaCatalog.close();
//                javaCatalog = null;
//            }
        if (infoDBEnv != null) {
            try {
                infoDBEnv.close();
                infoDBEnv = null;
            } catch (DatabaseException ex) {
//                Logger.getLogger(InformationDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void openDatabase(File databaseHomeFile) throws IOException {
        try {
            /*--Create database environment (Has to be done before creating database--*/
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            envConfig.setTransactional(true);
            infoDBEnv = new Environment(databaseHomeFile, envConfig);

            /*--Create database from environment--*/
            // Create database config before database
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setTransactional(true);

            /*--Create catalog for information storage--*/
            //Database catalogDb = infoDBEnv.openDatabase(null, "Frame database class catalog", dbConfig);
            //javaCatalog = new StoredClassCatalog(catalogDb);

            // open (local ID, Picture) table
            infoDB = infoDBEnv.openDatabase(null, "Information Database", dbConfig);

            // Try to find the largest key
            Cursor cursor = infoDB.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            if (cursor.getLast(foundKey, foundData, LockMode.DEFAULT) ==
                    OperationStatus.SUCCESS) {
                this.largestKey = IntegerBinding.entryToInt(foundKey);
            }
        } catch (Exception e) {
            IOException myException = new IOException("Error in creating database to store Information.");
            myException.setStackTrace(e.getStackTrace());
            close(); // Close database properly
            throw myException;
        }

        // Create frame info (ImageIcon) binding
        //EntryBinding infoBinding =
        //        new SerialBinding(javaCatalog, frameInfo.getClass());

        frameNumberToDoubleInfoMap =
                new StoredMap(infoDB, new IntegerBinding(), new DoubleBinding(), true);
    }

    /**
     * 
     * @param frameNumber
     * @return
     */
    public Double getInfo(Integer frameNumber) {
        return (Double) frameNumberToDoubleInfoMap.get(frameNumber);
    }

    /**
     * 
     * @param frameNumber
     * @param value
     */
    public void putInfo(Integer frameNumber, Double value) {
        largestKey = Math.max(frameNumber, largestKey);
        frameNumberToDoubleInfoMap.put(frameNumber, value);
    }

    public int getLastFrame() {
        return this.largestKey;
    }
}
