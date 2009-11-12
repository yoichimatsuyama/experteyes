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
package eyetrackercalibrator.gui;

import eyetrackercalibrator.gui.util.FrameMarker;
import java.awt.Color;
import org.jdom.Element;
import org.jfree.chart.plot.IntervalMarker;

public class ErrorMarking extends FrameMarker{

    /**
     * THe clone get all except Intervalmarker
     */
    @Override
    protected Object clone() {
        ErrorMarking e = new ErrorMarking();
        e.eye = eye;
        e.unrecoverable = unrecoverable;
        e.topleft = topleft;
        e.topright = topright;
        e.bottomleft = bottomleft;
        e.bottomright = bottomright;
        e.startEyeFrame = startEyeFrame;
        e.stopEyeFrame = stopEyeFrame;
        e.startSceneFrame = startSceneFrame;
        e.stopSceneFrame = stopSceneFrame;
        e.referenceFrame = referenceFrame;
        return e;
    }
    
    public boolean eye = false;
    public boolean unrecoverable = false;
    public boolean topleft = false;
    public boolean topright = false;
    public boolean bottomleft = false;
    public boolean bottomright = false;
    private int referenceFrame = 0;

    public ErrorMarking() {
    }

    /**
     * Create error marking from element
     */
    public ErrorMarking(Element e) {
        // Set eye frame
        Element element = e.getChild("EyeFrames");
        startEyeFrame = Integer.parseInt(element.getAttributeValue("from"));
        stopEyeFrame = Integer.parseInt(element.getAttributeValue("to"));

        // Set screen frame
        element = e.getChild("ScreenFrames");
        startSceneFrame = Integer.parseInt(element.getAttributeValue("from"));
        stopSceneFrame = Integer.parseInt(element.getAttributeValue("to"));

        // Set up flags
        unrecoverable = Boolean.parseBoolean(e.getAttributeValue("unrecoverable"));
        eye = Boolean.parseBoolean(e.getAttributeValue("eye"));
        topleft = Boolean.parseBoolean(e.getAttributeValue("topleftcorner"));
        topright = Boolean.parseBoolean(e.getAttributeValue("toprightcorner"));
        bottomleft = Boolean.parseBoolean(e.getAttributeValue("bottomleftcorner"));
        bottomright = Boolean.parseBoolean(e.getAttributeValue("bottomrightcorner"));
    }


    @Override
    public String toString() {
        String retValue = "<html>";

        retValue = retValue.concat(
                "From:" + startEyeFrame + ":" + startSceneFrame + "(eye:screen)<br>");
        retValue = retValue.concat(
                "To:" + stopEyeFrame + ":" + stopSceneFrame + "(eye:screen)<br>");
        retValue = retValue.concat("Errors:");

        if (unrecoverable) {
            retValue = retValue.concat("&nbsp;Severe<br>");
        }

        if (eye) {
            retValue = retValue.concat("&nbsp;Eye<br>");
        }

        if (topleft) {
            retValue = retValue.concat("&nbsp;Top left corner<br>");
        }

        if (topright) {
            retValue = retValue.concat("&nbsp;Top right corner<br>");
        }

        if (bottomleft) {
            retValue = retValue.concat("&nbsp;Bottom left corner<br>");
        }

        if (bottomright) {
            retValue = retValue.concat("&nbsp;Bottom left corner<br>");
        }

        retValue = retValue.concat("</html>");

        return retValue;
    }

    public Element toElement() {
        Element root = new Element("Error");
        // Add eye frame range
        Element elm = new Element("EyeFrames");
        elm.setAttribute("from", String.valueOf(startEyeFrame));
        elm.setAttribute("to", String.valueOf(stopEyeFrame));
        root.addContent(elm);

        // Add screen frame range
        elm = new Element("ScreenFrames");
        elm.setAttribute("from", String.valueOf(startSceneFrame));
        elm.setAttribute("to", String.valueOf(stopSceneFrame));
        root.addContent(elm);

        root.setAttribute("unrecoverable", String.valueOf(unrecoverable));
        root.setAttribute("eye", String.valueOf(eye));
        root.setAttribute("topleftcorner", String.valueOf(topleft));
        root.setAttribute("toprightcorner", String.valueOf(topright));
        root.setAttribute("bottomleftcorner", String.valueOf(bottomleft));
        root.setAttribute("bottomrightcorner", String.valueOf(bottomright));

        return root;
    }

    public void setIntervalMarker(IntervalMarker intervalMarker) {
        super.setIntervalMarker(intervalMarker);
        
        if (unrecoverable) {
            // Mark red if unrecoverable
            intervalMarker.setPaint(Color.RED);

        } else if (eye && (topleft || topright || bottomleft || bottomright)) {

            intervalMarker.setPaint(Color.CYAN);

        } else if (eye) {

            intervalMarker.setPaint(Color.BLUE);

        } else if (topleft || topright || bottomleft || bottomright) {

            intervalMarker.setPaint(Color.GREEN);

        }
    }

    public int getErrorCode() {
        int b = 0;
        if (eye) {
            b += 1;
        }
        if (unrecoverable) {
            b += 2;
        }
        if (topleft) {
            b += 4;
        }
        if (topright) {
            b += 8;
        }
        if (bottomleft) {
            b += 16;
        }
        if (bottomright) {
            b += 32;
        }

        return b;
    }

    public void parseErrorCode(int b) {
        eye = ((b & 1) == 1);

        unrecoverable = ((b & 02) == 02);

        topleft = ((b & 4) == 04);

        topright = ((b & 8) == 8);

        int v = b & 16;
        bottomleft = ((b & 16) == 16);

        bottomright = ((b & 32) == 32);
    }
}
