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

import java.awt.Point;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ruj
 */
public class ParseData {

    public ParseData() {
    }

    public static void main(String args[]) {

        HashMap<Point, Double> map = new HashMap<Point, Double>();
        Set<Entry<Point, Double>> set;

        try {
            RandomAccessFile in = new RandomAccessFile(args[0], "r");

            PrintStream out = new PrintStream(args[1]);

            for (String line = in.readLine(); line != null; line = in.readLine()) {

                String[] token = line.split("\t");

                Point p = new Point(Integer.parseInt(token[0]),
                        Integer.parseInt(token[1]));

                Double nd = Double.parseDouble(token[2]);

                Double d = map.put(p, nd);
                if (d != null) {
                    if (d < nd) {
                        map.put(p, d);
                    }
                }
            }

            set = map.entrySet();
            for (Iterator<Entry<Point, Double>> it = set.iterator(); it.hasNext();) {
                Entry<Point, Double> entry = it.next();
                Point p = entry.getKey();

                out.println(p.x + "\t" + p.y + "\t" + entry.getValue());
            }


            out.close();
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(ParseData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
