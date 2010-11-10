/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.gui.related;

import pl.umk.mat.imare.reco.Transcriber;

/**
 *
 * @author morti
 */
public interface RecognitionProgressListener {
	public void cancelled();
	public void allFinished(Transcriber transcriber);
}
