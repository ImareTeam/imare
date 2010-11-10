/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.umk.mat.imare.exception;

/**
 *
 * @author morti
 */
public class MissingChannelException extends RuntimeException {

	public MissingChannelException(int channelNumber) {
		super("There is no such channel: " + channelNumber);
	}

}