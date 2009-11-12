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

import eyetrackercalibrator.gui.util.FrameMarker;
import org.jdom.Element;
import org.jfree.chart.plot.IntervalMarker;

/**
 *
 * @author eeglab
 */
public class TrialMarker extends FrameMarker implements Comparable {

    /** Trial information */
    public String label;
    public boolean isBadTrial = false;

    /** For storing and loading information to the XML format */
    public final static String ELEMENT_NAME = "Trial";
    public final static String EYE_FRAME_ELEMENT_NAME = "EyeFrames";
    public final static String SCENE_FRAME_ELEMENT_NAME = "ScreenFrames";
    public final static String FROM_ATTRIBUTE = "from";
    public final static String TO_ATTRIBUTE = "to";
    public final static String NAME_ATTRIBUTE = "name";
    public final static String BAD_TRIAL_ATTRIBUTE = "is_bad";


    public TrialMarker() {
    }

    /**
     * Create trial marking from element
     */
    public TrialMarker(Element e) {
        String string = null;

        // Set eye frame
        Element element = e.getChild(EYE_FRAME_ELEMENT_NAME);
        if(element != null){
            string = element.getAttributeValue(FROM_ATTRIBUTE);
            if(string != null){
                startEyeFrame = Integer.parseInt(string);
            }
            string = element.getAttributeValue(TO_ATTRIBUTE);
            if(string != null){
                stopEyeFrame = Integer.parseInt(string);
            }
        }

        // Set screen frame
        element = e.getChild(SCENE_FRAME_ELEMENT_NAME);
        if(element != null){
            string = element.getAttributeValue(FROM_ATTRIBUTE);
            if(string != null){
                startSceneFrame = Integer.parseInt(string);
            }
            string = element.getAttributeValue(TO_ATTRIBUTE);
            if(string != null){
                stopSceneFrame = Integer.parseInt(string);
            }
        }
       
        // Set up flags
        label = e.getAttributeValue(NAME_ATTRIBUTE);
        string = e.getAttributeValue(BAD_TRIAL_ATTRIBUTE);
        if(string != null){
            isBadTrial = Boolean.parseBoolean(string);
        }
    }


    @Override
    public String toString() {
        String retValue = "<html>";

        if (label != null) {
            retValue = retValue.concat(label + ":<br>");
        }

        retValue = retValue.concat(
                "From:" + startEyeFrame + ":" + startSceneFrame + "(eye:scene)<br>");
        retValue = retValue.concat(
                "To:" + stopEyeFrame + ":" + stopSceneFrame + "(eye:scene)<br>");
        retValue = retValue.concat("</html>");

        return retValue;
    }

    public Element toElement() {
        Element root = new Element(ELEMENT_NAME);
        // Add eye frame range
        Element elm = new Element(EYE_FRAME_ELEMENT_NAME);
        elm.setAttribute(FROM_ATTRIBUTE, String.valueOf(startEyeFrame));
        elm.setAttribute(TO_ATTRIBUTE, String.valueOf(stopEyeFrame));
        root.addContent(elm);

        // Add screen frame range
        elm = new Element(SCENE_FRAME_ELEMENT_NAME);
        elm.setAttribute(FROM_ATTRIBUTE, String.valueOf(startSceneFrame));
        elm.setAttribute(TO_ATTRIBUTE, String.valueOf(stopSceneFrame));
        root.addContent(elm);

        root.setAttribute(NAME_ATTRIBUTE, label);
        root.setAttribute(BAD_TRIAL_ATTRIBUTE, String.valueOf(isBadTrial));

        return root;
    }

    public int compareTo(Object o) {
        TrialMarker m = (TrialMarker) o;
        if(this.startEyeFrame == m.startEyeFrame){
            return this.label.compareTo(m.label);
        }else{
            return this.startSceneFrame - m.startSceneFrame;
        }
    }
}
