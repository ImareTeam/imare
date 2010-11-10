/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.gui.related;

/**
 * Uniwersalny interfejs dla słuchaczy obiektów
 * generujących zdarzenia postępu pracy.
 * @author morti
 */
public interface ProgressListener {
	/**
	 * Informuje słuchacza o rozpoczęciu pracy.
	 * @param sender Obiekt wykonujący pracę.
	 */
	public void jobStarted(Object sender);

	/**
	 * Informuje słuchacza o postępie w pracy.
	 * @param sender Obiekt wykonujący pracę.
	 * @param progress Postęp w pracy. Przyjmuje wartości
	 * z przedziału [0,1] przy czym 0 oznacza początek pracy,
	 * a 1 koniec pracy.
	 */
	public void jobProgress(Object sender, float progress);

	/**
	 * Informuje słuchacza o zakończeniu pracy.
	 * @param sender Obiekt wykonujący pracę.
	 */
	public void jobFinished(Object sender);
}
