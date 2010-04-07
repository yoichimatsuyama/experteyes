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
package buseylab.fiteyemodel.util;

import buseylab.fiteyemodel.gui.GradientPanel;
import buseylab.fiteyemodel.gui.GradientPanel.Corner;
import buseylab.fiteyemodel.logic.GradientCorrection;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * This class helps collect parameters together so that loading and saving
 * is not too complicate.
 * @author ruj
 */
public class ParameterList {

    private String comment = null;
    private boolean gradientCorrecting = false;
    private Rectangle gradientBoxGuide = new Rectangle();
    private int gradientBrightnessAddValue = 0;
    private GradientPanel.Corner gradientStartCorner = GradientPanel.Corner.TOPLEFT;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public class Entry {

        public Entry(Point2D p, String fileName, Parameters parameters) {
            this.p = p;
            this.filename = fileName;
            this.parameters = parameters;
        }
        public Point2D p;
        public Parameters parameters;
        public String filename;
    }
    LinkedList<Entry> parametersList = new LinkedList<ParameterList.Entry>();

    /**
     * Get parameters closest to the given coordinate
     */
    public Parameters get(Point2D point) {
        Parameters parameters = null;

        double distance = Double.MAX_VALUE;

        // Search for the closest one
        for (Iterator<ParameterList.Entry> it = parametersList.iterator(); it.hasNext();) {
            ParameterList.Entry info = it.next();

            double newDistance = info.p.distance(point);
            if (newDistance < distance) {
                distance = newDistance;
                parameters = info.parameters;
            }
        }

        return parameters;
    }

    public void addParameters(Point2D point, String filename, Parameters parameters) {
        parametersList.add(new Entry(point, filename, parameters));
    }

    public void setGradientCorrectionInfo(boolean enable, GradientPanel.Corner corner,
            int x, int y, int width, int height, int brightnessAddValue) {
        this.gradientCorrecting = enable;
        this.gradientBrightnessAddValue = brightnessAddValue;
        this.gradientBoxGuide.setBounds(x, y, width, height);
        this.gradientStartCorner = corner;
    }
    public static String GRADIENT_CORRECTING_ELEMENT = "gradient_correcting";
    public static String GRADIENT_CORRECTING_ENABLE_ATTRIBUTE = "enable";
    public static String GRADIENT_CORRECTING_X_ATTRIBUTE = "x";
    public static String GRADIENT_CORRECTING_Y_ATTRIBUTE = "y";
    public static String GRADIENT_CORRECTING_W_ATTRIBUTE = "w";
    public static String GRADIENT_CORRECTING_H_ATTRIBUTE = "h";
    public static String GRADIENT_CORRECTING_V_ATTRIBUTE = "v";
    public static String GRADIENT_CORRECTING_CORNER_ATTRIBUTE = "corner";
    public static String X_ATTRIBUTE = "x";
    public static String Y_ATTRIBUTE = "y";
    public static String FILE_NAME_ATTRIBUTE = "filename";
    public static String COMMENT_ELEMENT = "comment";
    public static String PARAMETER_ELEMENT = "parameters";

    public void save(File output) throws IOException {
        Element root = new Element("ParameterSet");

        Element commentElement = new Element(COMMENT_ELEMENT);
        commentElement.setText(this.comment);
        root.addContent(commentElement);

        Element gradientCorrectionElement = new Element(GRADIENT_CORRECTING_ELEMENT);
        gradientCorrectionElement.setAttribute(GRADIENT_CORRECTING_ENABLE_ATTRIBUTE,
                String.valueOf(this.gradientCorrecting));
        gradientCorrectionElement.setAttribute(GRADIENT_CORRECTING_X_ATTRIBUTE,
                String.valueOf(this.gradientBoxGuide.x));
        gradientCorrectionElement.setAttribute(GRADIENT_CORRECTING_Y_ATTRIBUTE,
                String.valueOf(this.gradientBoxGuide.y));
        gradientCorrectionElement.setAttribute(GRADIENT_CORRECTING_W_ATTRIBUTE,
                String.valueOf(this.gradientBoxGuide.width));
        gradientCorrectionElement.setAttribute(GRADIENT_CORRECTING_H_ATTRIBUTE,
                String.valueOf(this.gradientBoxGuide.height));
        gradientCorrectionElement.setAttribute(GRADIENT_CORRECTING_CORNER_ATTRIBUTE,
                String.valueOf(this.gradientStartCorner));
        gradientCorrectionElement.setAttribute(GRADIENT_CORRECTING_V_ATTRIBUTE,
                String.valueOf(this.gradientBrightnessAddValue));


        for (Iterator<ParameterList.Entry> it = parametersList.iterator(); it.hasNext();) {
            ParameterList.Entry info = it.next();

            Element parameters = new Element(PARAMETER_ELEMENT);
            parameters.setAttribute(X_ATTRIBUTE, String.valueOf(info.p.getX()));
            parameters.setAttribute(Y_ATTRIBUTE, String.valueOf(info.p.getY()));
            parameters.setAttribute(FILE_NAME_ATTRIBUTE, info.filename);

            parameters.addContent(info.parameters.toElement());

            root.addContent(parameters);
        }

        // Write out to file as xml
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(new Document(root), new FileWriter(output));
    }

    public Rectangle getGradientBoxGuide() {
        return gradientBoxGuide;
    }

    public int getGradientBrightnessAddValue() {
        return gradientBrightnessAddValue;
    }

    public boolean isGradientCorrecting() {
        return gradientCorrecting;
    }

    public Corner getGradientStartCorner() {
        return gradientStartCorner;
    }

    static public ParameterList load(File input) {
        ParameterList parameterMap = new ParameterList();

        // Load from file
        SAXBuilder builder = new SAXBuilder();
        Element root = null;
        try {
            Document doc = builder.build(input);
            root = doc.getRootElement();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        // Get comment if any
        Element commentElement = root.getChild(COMMENT_ELEMENT);
        if (commentElement != null) {
            parameterMap.setComment(commentElement.getText());
        }

        // Get gradient if any
        Element gradientCorrectionElement = root.getChild(GRADIENT_CORRECTING_ELEMENT);
        boolean gradientCorrecting = false;
        Rectangle gradientBoxGuide = new Rectangle(0, 0, 1, 1);
        int gradientBrightnessAddValue = 0;
        GradientPanel.Corner gradientStartCorner = GradientPanel.Corner.TOPLEFT;
        if (gradientCorrectionElement != null) {
            gradientCorrecting = Boolean.parseBoolean(
                    gradientCorrectionElement.getAttributeValue(GRADIENT_CORRECTING_ENABLE_ATTRIBUTE));
            gradientBoxGuide.x = Integer.parseInt(
                    gradientCorrectionElement.getAttributeValue(GRADIENT_CORRECTING_X_ATTRIBUTE));
            gradientBoxGuide.y = Integer.parseInt(
                    gradientCorrectionElement.getAttributeValue(GRADIENT_CORRECTING_Y_ATTRIBUTE));
            gradientBoxGuide.width = Integer.parseInt(
                    gradientCorrectionElement.getAttributeValue(GRADIENT_CORRECTING_W_ATTRIBUTE));
            gradientBoxGuide.height = Integer.parseInt(
                    gradientCorrectionElement.getAttributeValue(GRADIENT_CORRECTING_H_ATTRIBUTE));
            gradientStartCorner = GradientPanel.Corner.valueOf(
                    gradientCorrectionElement.getAttributeValue(GRADIENT_CORRECTING_CORNER_ATTRIBUTE));
            gradientBrightnessAddValue = Integer.parseInt(
                    gradientCorrectionElement.getAttributeValue(GRADIENT_CORRECTING_V_ATTRIBUTE));
        }

        // Iterate through each parameter set
        for (Iterator<Element> iter = root.getChildren(PARAMETER_ELEMENT).iterator();
                iter.hasNext();) {
            Element element = iter.next();

            Point2D.Double p = new Point2D.Double(
                    Double.parseDouble(element.getAttributeValue(X_ATTRIBUTE)),
                    Double.parseDouble(element.getAttributeValue(Y_ATTRIBUTE)));

            Parameters parameters = new Parameters(
                    (Element) element.getChildren().get(0));

            parameterMap.addParameters(p, element.getAttributeValue(FILE_NAME_ATTRIBUTE),
                    parameters);
        }

        return parameterMap;
    }

    public Iterator<Entry> iterator() {
        return this.parametersList.iterator();
    }

    public Parameters getFirstParameters() {
        Entry entry = this.parametersList.getFirst();
        if (entry != null) {
            return entry.parameters;
        }
        return null;
    }

    public void updateAllSearchArea(Rectangle2D rectangle) {
        for (Iterator<ParameterList.Entry> it = parametersList.iterator(); it.hasNext();) {
            ParameterList.Entry entry = it.next();
            entry.parameters.searchArea.setRect(rectangle);
        }

    }
}
