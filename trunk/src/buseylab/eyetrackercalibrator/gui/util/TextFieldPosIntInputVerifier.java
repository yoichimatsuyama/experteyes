/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package buseylab.eyetrackercalibrator.gui.util;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author ruj
 */
public class TextFieldPosIntInputVerifier extends InputVerifier{

    @Override
    public boolean verify(JComponent input) {
        JTextField tf = (JTextField) input;
        String text = tf.getText();
        int i = 0;
        try {
            i = Integer.parseInt(text);
        } catch (NumberFormatException numberFormatException) {
            return false;
        }

        return i > 0;
    }

}
