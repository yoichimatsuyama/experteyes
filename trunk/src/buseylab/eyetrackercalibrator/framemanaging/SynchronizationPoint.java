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
package buseylab.eyetrackercalibrator.framemanaging;

import org.jdom.Element;

/**
 * This class help stores Synchronization point
 * @author ruj
 */
public class SynchronizationPoint{

    @Override
    public String toString() {
        return this.eyeFrame+", "+this.sceneFrame;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        if(SynchronizationPoint.class.isInstance(obj)){
            SynchronizationPoint o = (SynchronizationPoint) obj;
            return(o.eyeFrame == this.eyeFrame && o.sceneFrame == this.sceneFrame);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.eyeFrame * this.sceneFrame;
    }

    public SynchronizationPoint() {
    }

    public SynchronizationPoint(int eyeFrame, int sceneFrame) {
        this.eyeFrame = eyeFrame;
        this.sceneFrame = sceneFrame;
    }

    public int eyeFrame;
    public int sceneFrame;
    /** Name of XML exported element */
    final public static String XMLELEMENT = "syncpoint";
    final public static String ATTRIBUTE_EYE_FRAME = "eyeframe";
    final public static String ATTRIBUTE_SCENE_FRAME = "sceneframe";

    

    /** 
     * Convert into XML element the element is
     * <synchpoint eyeFrame="a" sceneFrame="b"/>
     */
    public Element toXMLElement() {
        Element e = new Element(XMLELEMENT);
        e.setAttribute(ATTRIBUTE_EYE_FRAME, String.valueOf(eyeFrame));
        e.setAttribute(ATTRIBUTE_SCENE_FRAME, String.valueOf(sceneFrame));
        return e;
    }

    /**
     * This will throws error when it cannot parse the element
     */
    public static SynchronizationPoint fromElement(Element xmlElement) throws InstantiationException {
        int eyeFrame = 0;
        int sceneFrame = 0;

        String buffer = xmlElement.getAttributeValue(ATTRIBUTE_EYE_FRAME);
        if(buffer == null){
            throw new InstantiationException("Missing eye frame information");
        }else{
            try {
                eyeFrame = Integer.parseInt(buffer);
            } catch (NumberFormatException numberFormatException) {
                throw new InstantiationException("Eye frame is not an integer");
            }
        }

        buffer = xmlElement.getAttributeValue(ATTRIBUTE_SCENE_FRAME);
        if(buffer == null){
            throw new InstantiationException("Missing scene frame information");
        }else{
            try {
                sceneFrame = Integer.parseInt(buffer);
            } catch (NumberFormatException numberFormatException) {
                throw new InstantiationException("Scene frame is not an integer");
            }
        }

        return new SynchronizationPoint(eyeFrame, sceneFrame);
    }
}
