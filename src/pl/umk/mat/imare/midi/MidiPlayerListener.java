/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.midi;

/**
 *
 * @author Tyczo
 */
public interface MidiPlayerListener {

    public void positionChanged(long newPosition);
    public void notifyFinished();

}
