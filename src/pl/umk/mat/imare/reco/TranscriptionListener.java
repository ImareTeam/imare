package pl.umk.mat.imare.reco;

/**
 * Interfejs przystosowany do odbierania komunikatów o zdarzeniach od obiektu
 * klasy Transcriber.
 * @see Transcriber
 * @author PieterEr
 */
public interface TranscriptionListener {

    /**
     * Sygnalizuje zakończenie procesu transkrypcji.
     * @param transcriber  obiekt nadawcy klasy Transcriber
     */
    public void transcriptionFinished(Transcriber transcriber);
}
