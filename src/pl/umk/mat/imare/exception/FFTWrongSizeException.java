/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.umk.mat.imare.exception;

/**
 *
 * @author pieterer
 */
public class FFTWrongSizeException extends Exception {

    public FFTWrongSizeException(int size) {
        super("FFT input size has to be a natural power of two - " + size + " isn't");
    }
}
