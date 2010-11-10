/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.gui;

/**
 *
 * @author Tyczo
 */
public interface PlayListener {

    public void positionChanged(int newPosition);
    public void playingFinished();
    public void setVolume();

}
