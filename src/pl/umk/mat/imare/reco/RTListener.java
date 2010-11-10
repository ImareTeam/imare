package pl.umk.mat.imare.reco;

import pl.umk.mat.imare.reco.RTRecognizer.PixelType;

/**
 * Interfejs przystosowany do odbierania komunikatów o zdarzeniach od obiektu
 * klasy RTRecognizer.
 * @see RTRecognizer
 * @author PieterEr
 */
public interface RTListener {

    /**
     * Metoda wywoływana, gdy dostępne jest nowe widmo dźwięku.
     * @param reco  obiekt nadawcy klasy RTRecognizer
     * @param spectrum  tablica zawierająca wartości widma z przedziału 0..1
     * @param active  tablica zawierająca stany poszczególnych wartości
     */
    void spectrumNotification(RTRecognizer reco, final double[] spectrum, final PixelType[] active);

    /**
     * Metoda wywoływana, gdy wykryta zostanie nowa nuta utworu.
     * @param reco  obiekt nadawcy klasy RTRecognizer
     * @param n  nowowykryta nuta
     */
    void noteFound(RTRecognizer reco, Note n);

    /**
     * Metoda wywoływana, gdy proces rozpoznawania zostanie zakończony.
     * @param reco  obiekt nadawcy klasy RTRecognizer
     */
    void recognitionFinished(RTRecognizer reco);
}
