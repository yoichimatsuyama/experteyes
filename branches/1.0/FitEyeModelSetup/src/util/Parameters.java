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
package util;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author SQ
 */
public class Parameters {

    public Parameters(int crThreshold, int pupilThreshold, int crGrayValue,
            int pupilGrayValue, int backgroundGrayValue, Rectangle searchArea,
            int unsharpRadious, double unsharpFactor, boolean detectAngle) {
        this.crThreshold = crThreshold;
        this.pupilThreshold = pupilThreshold;
        this.crGrayValue = crGrayValue;
        this.pupilGrayValue = pupilGrayValue;
        this.backgroundGrayValue = backgroundGrayValue;
        this.searchArea = searchArea;
        this.unsharpRadious = unsharpRadious;
        this.unsharpFactor = unsharpFactor;
        this.detectPupilAngle = detectAngle;
    }

    public Parameters() {
        this.crThreshold = 0;
        this.pupilThreshold = 0;
        this.crGrayValue = 0;
        this.pupilGrayValue = 0;
        this.backgroundGrayValue = 0;
        this.searchArea = new Rectangle();
        this.unsharpFactor = 0d;
        this.unsharpRadious = 0;
        this.detectPupilAngle = false;
    }

    public Parameters(Element root) {
        Element element = root.getChild(CR_THRESHOLD_ELEMENT_NAME);
        String value = element.getAttributeValue(VALUE_ATTRIBUTE);
        if (value != null) {
            this.crThreshold = Integer.parseInt(value);
        }


        element = root.getChild(PUPIL_THRESHOLD_ELEMENT_NAME);
        value = element.getAttributeValue(VALUE_ATTRIBUTE);
        if (value != null) {
            this.pupilThreshold = Integer.parseInt(value);
        }


        element = root.getChild(CR_GRAY_VALUE_ELEMENT_NAME);
        value = element.getAttributeValue(VALUE_ATTRIBUTE);
        if (value != null) {
            this.crGrayValue = Integer.parseInt(value);
        }


        element = root.getChild(BACKGROUND_GRAY_VALUE_ELEMENT_NAME);
        value = element.getAttributeValue(VALUE_ATTRIBUTE);
        if (value != null) {
            this.backgroundGrayValue = Integer.parseInt(value);
        }


        element = root.getChild(PUPIL_GRAY_VALUE_ELEMENT_NAME);
        value = element.getAttributeValue(VALUE_ATTRIBUTE);
        if (value != null) {
            this.pupilGrayValue = Integer.parseInt(value);
        }


        this.searchArea = new Rectangle();
        element = root.getChild(SEARCH_AREA_ELEMENT_NAME);
        if (element != null) {
            value = element.getAttributeValue(X_ATTRIBUTE);
            if (value != null) {
                this.searchArea.x = Integer.parseInt(value);
            }
            value = element.getAttributeValue(Y_ATTRIBUTE);
            if (value != null) {
                this.searchArea.y = Integer.parseInt(value);
            }
            value = element.getAttributeValue(WIDTH_ATTRIBUTE);
            if (value != null) {
                this.searchArea.width = Integer.parseInt(value);
            }
            value = element.getAttributeValue(HEIGHT_ATTRIBUTE);
            if (value != null) {
                this.searchArea.height = Integer.parseInt(value);
            }
        }

        element = root.getChild(UNSHARP_ELEMENT_NAME);
        if (element != null) {
            value = element.getAttributeValue(UNSHARP_RADIOUS_ATTRIBUTE);
            if (value != null) {
                this.unsharpRadious = Integer.parseInt(value);
            }
            value = element.getAttributeValue(UNSHARP_FACTOR_ATTRIBUTE);
            if (value != null) {
                this.unsharpFactor = Double.parseDouble(value);
            }
        }
    
        this.detectPupilAngle = false;
        element = root.getChild(DETECT_ANGLE_ELEMENT_NAME);
        if(element != null){
            value = element.getAttributeValue(VALUE_ATTRIBUTE);
            if(value != null){
                this.detectPupilAngle = Boolean.parseBoolean(value);
            }
        }
    }

    public int crThreshold;
    public int pupilThreshold;
    public int crGrayValue;
    public int pupilGrayValue;
    public int backgroundGrayValue;
    public Rectangle searchArea;
    public int unsharpRadious;
    public double unsharpFactor;
    public boolean detectPupilAngle;
    /** Constants for parsing file */
    public static String CR_THRESHOLD_ELEMENT_NAME = "crthreshold";
    public static String PUPIL_THRESHOLD_ELEMENT_NAME = "pupilthreshold";
    public static String CR_GRAY_VALUE_ELEMENT_NAME = "crgrayvalue";
    public static String PUPIL_GRAY_VALUE_ELEMENT_NAME = "pupilgrayvalue";
    public static String BACKGROUND_GRAY_VALUE_ELEMENT_NAME = "backgroundgrayvalue";
    public static String SEARCH_AREA_ELEMENT_NAME = "searcharea";
    public static String UNSHARP_ELEMENT_NAME = "unsharp";
    public static String DETECT_ANGLE_ELEMENT_NAME = "useAngle";
    public static String VALUE_ATTRIBUTE = "value";
    public static String X_ATTRIBUTE = "x";
    public static String Y_ATTRIBUTE = "y";
    public static String WIDTH_ATTRIBUTE = "width";
    public static String HEIGHT_ATTRIBUTE = "height";
    public static String UNSHARP_RADIOUS_ATTRIBUTE = "radious";
    public static String UNSHARP_FACTOR_ATTRIBUTE = "factor";
    

    /**
     * Create parameter from xml file
     * @param xmlFile 
     */
    static public Parameters load(File xmlFile) {
        // Load from file
        SAXBuilder builder = new SAXBuilder();
        Element root = null;
        try {
            Document doc = builder.build(xmlFile);
            root = doc.getRootElement();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        Parameters parameters = new Parameters(root);

        return parameters;
    }

    /**
     * Save parameter to xml file
     * @param xmlFile
     * @throws java.io.IOException 
     */
    public void save(File xmlFile) throws IOException {
        // Prepare xml elements
        Element root = toElement();

        // Write out to file as xml
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(new Document(root), new FileWriter(xmlFile));
    }

    public Element toElement() {
        Element root = new Element("FitEyeModelParameters");

        Element element = new Element(CR_THRESHOLD_ELEMENT_NAME);
        element.setAttribute(VALUE_ATTRIBUTE,
                String.valueOf(crThreshold));
        root.addContent(element);

        element = new Element(PUPIL_THRESHOLD_ELEMENT_NAME);
        element.setAttribute(VALUE_ATTRIBUTE,
                String.valueOf(pupilThreshold));
        root.addContent(element);

        element = new Element(CR_GRAY_VALUE_ELEMENT_NAME);
        element.setAttribute(VALUE_ATTRIBUTE,
                String.valueOf(crGrayValue));
        root.addContent(element);

        element = new Element(BACKGROUND_GRAY_VALUE_ELEMENT_NAME);
        element.setAttribute(VALUE_ATTRIBUTE,
                String.valueOf(backgroundGrayValue));
        root.addContent(element);

        element = new Element(PUPIL_GRAY_VALUE_ELEMENT_NAME);
        element.setAttribute(VALUE_ATTRIBUTE,
                String.valueOf(pupilGrayValue));
        root.addContent(element);

        element = new Element(SEARCH_AREA_ELEMENT_NAME);
        element.setAttribute(X_ATTRIBUTE, String.valueOf(searchArea.x));
        element.setAttribute(Y_ATTRIBUTE, String.valueOf(searchArea.y));
        element.setAttribute(WIDTH_ATTRIBUTE, String.valueOf(searchArea.width));
        element.setAttribute(HEIGHT_ATTRIBUTE, String.valueOf(searchArea.height));
        root.addContent(element);

        element = new Element(UNSHARP_ELEMENT_NAME);
        element.setAttribute(UNSHARP_FACTOR_ATTRIBUTE, String.valueOf(unsharpFactor));
        element.setAttribute(UNSHARP_RADIOUS_ATTRIBUTE, String.valueOf(unsharpRadious));
        root.addContent(element);

        element = new Element(DETECT_ANGLE_ELEMENT_NAME);
        element.setAttribute(VALUE_ATTRIBUTE,
                String.valueOf(detectPupilAngle));
        root.addContent(element);
        
        return root;
    }
}
