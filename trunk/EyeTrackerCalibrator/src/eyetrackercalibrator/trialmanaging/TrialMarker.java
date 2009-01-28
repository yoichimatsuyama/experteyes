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
package eyetrackercalibrator.trialmanaging;

import java.awt.Color;
import org.jdom.Element;
import org.jfree.chart.plot.IntervalMarker;

/**
 *
 * @author eeglab
 */
public class TrialMarker implements Comparable{

    public int startEyeFrame = 0;
    public int stopEyeFrame = 0;
    public int startScreenFrame = 0;
    public int stopScreenFrame = 0;
    public String label;
    private int referenceFrame;
    private IntervalMarker intervalMarker = null;
    public static String ELEMENT_NAME = "Trial";

    public TrialMarker() {
    }

    /**
     * Create trial marking from element
     */
    public TrialMarker(Element e) {
        // Set eye frame
        Element element = e.getChild("EyeFrames");
        startEyeFrame = Integer.parseInt(element.getAttributeValue("from"));
        stopEyeFrame = Integer.parseInt(element.getAttributeValue("to"));

        // Set screen frame
        element = e.getChild("ScreenFrames");
        startScreenFrame = Integer.parseInt(element.getAttributeValue("from"));
        stopScreenFrame = Integer.parseInt(element.getAttributeValue("to"));

        // Set up flags
        label = e.getAttributeValue("name");
    }

    public void setEndFrame(int currentFrame, int eyeOffset, int screenOffset) {
        int endRef = 0;
        int startRef = 0;

        if (currentFrame >= referenceFrame) {
            startRef = referenceFrame;
            endRef = currentFrame;
        } else {
            startRef = currentFrame;
            endRef = referenceFrame;
        }

        stopEyeFrame = endRef + eyeOffset;
        stopScreenFrame = endRef + screenOffset;
        startEyeFrame = startRef + eyeOffset;
        startScreenFrame = startRef + screenOffset;
        if (intervalMarker != null) {
            intervalMarker.setEndValue(endRef);
            intervalMarker.setStartValue(startRef);
        }
    }

    public void setStartFrame(int currentFrame, int eyeOffset, int screenOffset) {
        startEyeFrame = currentFrame + eyeOffset;
        startScreenFrame = currentFrame + screenOffset;
        if (intervalMarker != null) {
            intervalMarker.setStartValue(currentFrame);
        }
        referenceFrame = currentFrame;
    }

    @Override
    public String toString() {
        String retValue = "<html>";

        if (label != null) {
            retValue = retValue.concat(label + ":<br>");
        }

        retValue = retValue.concat(
                "From:" + startEyeFrame + ":" + startScreenFrame + "(eye:screen)<br>");
        retValue = retValue.concat(
                "To:" + stopEyeFrame + ":" + stopScreenFrame + "(eye:screen)<br>");
        retValue = retValue.concat("</html>");

        return retValue;
    }

    public Element toElement() {
        Element root = new Element(ELEMENT_NAME);
        // Add eye frame range
        Element elm = new Element("EyeFrames");
        elm.setAttribute("from", String.valueOf(startEyeFrame));
        elm.setAttribute("to", String.valueOf(stopEyeFrame));
        root.addContent(elm);

        // Add screen frame range
        elm = new Element("ScreenFrames");
        elm.setAttribute("from", String.valueOf(startScreenFrame));
        elm.setAttribute("to", String.valueOf(stopScreenFrame));
        root.addContent(elm);

        root.setAttribute("name", label);

        return root;
    }

    public IntervalMarker getIntervalMarker() {
        return intervalMarker;
    }

    /** 
     * Only set intervalmarker.  It will not change start and end value
     * of the marker
     */
    public void setIntervalMarker(IntervalMarker intervalMarker) {
        //intervalMarker.setPaint(Color.GREEN);
        this.intervalMarker = intervalMarker;
    }

    public int compareTo(Object o) {
        TrialMarker m = (TrialMarker) o;
        if(this.startEyeFrame == m.startEyeFrame){
            return this.label.compareTo(m.label);
        }else{
            return this.startScreenFrame - m.startScreenFrame;
        }
    }
}
