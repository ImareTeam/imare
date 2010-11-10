package pl.umk.mat.imare.reco;

/**
 * Interfejs przystosowany do odbierania komunikatów o zdarzeniach od obiektu
 * klasy Recognizer.
 * @see Recognizer
 * @author PieterEr
 */
public interface RecognitionListener {

    /**
     * Sygnalizuje zakończenie procesu rozpoznawania.
     * @param recognizer  obiekt nadawcy klasy Recognizer
     * @param cancelled  <code>true</code> jeśli proces został anulowany
     * przez użytkownika, <code>false</code> w przeciwnym wypadku
     */
    public void recognitionFinished(Recognizer recognizer, boolean cancelled);

    /**
     * Sygnalizuje postęp procesu rozpoznawania.
     * @param recognizer  obiekt nadawcy klasy Recognizer
     * @param newProgress  aktualna wartość postępu procesu z przedziału [0..1]
     */
    public void progressChanged(Recognizer recognizer, float newProgress);
}
