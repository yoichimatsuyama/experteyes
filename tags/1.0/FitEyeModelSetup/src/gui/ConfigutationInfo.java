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
package gui;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

/**
 *
 * @author ruj
 */
public class ConfigutationInfo implements Comparable {

    public static String PUPIL_ATTRIBUTE = "pupil";
    public static String CR_ATTRIBUTE = "cr";
    public static String BACKGROUND_ATTRIBUTE = "background";
    public static String FRAME_ATTRIBUTE = "frame";
    public int pupil;
    public int cr;
    public int background;
    public String frameFileName;
    public int unsharpRadious;
    public double unsharpFactor;
    public boolean isDetectingPupilAngle;
    
    private Rectangle searchArea = new Rectangle();
    /**
     * These parameters will not be saved nor loaded.  It must be initialized
     * after the object is loaded by searching through the array of file names
     */
    volatile public int frameNum;
    /** 
     * In addition point should be recompute every time the threshold changes 
     */
    volatile public Point2D.Double point = new Point2D.Double();

    public ConfigutationInfo() {
    }

//    public ConfigutationInfo(Element element) {
//        this.pupil = Integer.parseInt(
//                element.getAttributeValue(PUPIL_ATTRIBUTE));
//        this.cr = Integer.parseInt(
//                element.getAttributeValue(CR_ATTRIBUTE));
//        this.background = Integer.parseInt(
//                element.getAttributeValue(BACKGROUND_ATTRIBUTE));
//        this.frameFileName = element.getAttributeValue(FRAME_ATTRIBUTE);
//    }

//    public Element toElement() {
//        Element root = new Element("graylevel");
//        root.setAttribute(FRAME_ATTRIBUTE, this.frameFileName);
//        root.setAttribute(PUPIL_ATTRIBUTE, Integer.toString(this.pupil));
//        root.setAttribute(CR_ATTRIBUTE, Integer.toString(this.cr));
//        root.setAttribute(BACKGROUND_ATTRIBUTE,
//                Integer.toString(this.background));
//
//        return root;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            return this.frameFileName.equals(((ConfigutationInfo) obj).frameFileName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.frameFileName != null ? this.frameFileName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        String string = "Frame " + this.frameFileName;
        return string;
    }

    public int compareTo(Object o) {
        return this.frameFileName.compareToIgnoreCase(((ConfigutationInfo) o).frameFileName);
    }

    public Rectangle getSearchArea() {
        return new Rectangle(searchArea);
    }

    public void setSearchArea(Rectangle searchArea) {
        this.searchArea.setRect(searchArea);
    }
}
