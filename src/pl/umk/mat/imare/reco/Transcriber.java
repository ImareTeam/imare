package pl.umk.mat.imare.reco;

import java.awt.EventQueue;
import java.util.LinkedList;
import java.util.List;

/**
 * Moduł transkrypcji rozpoznanych dźwięków do zapisu nutowego.
 * @author PieterEr
 */
public class Transcriber extends Thread {

    private Voice voice;
    private StaveData distave;
    private TFinishBroadcaster broadcaster;

    private static class TFinishBroadcaster implements Runnable {
        private List<TranscriptionListener> listeners;
        private Transcriber trans;

        TFinishBroadcaster(Transcriber trans) {
            this.trans = trans;
            this.listeners = new LinkedList<TranscriptionListener>();
        }

        @Override
        public void run() {
            for (TranscriptionListener l : listeners) {
                l.transcriptionFinished(trans);
            }
        }
    }

    /**
     * Tworzy obiekt klasy Transcriber w celu przekształcenia znalezionego
     * zbioru dźwięków w elementy zapisu nutowego. Po utworzeniu obiektu
     * i ustaleniu żądanych opcji, należy wywołać metodę <code>start()</code>
     * w celu rozpoczęcia procesu transkrypcji.
     * @param voice  zbiór dźwięków do przekształcenia
     */
    public Transcriber(Voice voice) {
        this.voice = voice;
        this.distave = new StaveData();
        this.broadcaster = new TFinishBroadcaster(this);
    }

    /** Dodaje obiekt <code>newListener</code> do listy obiektów, które
     * otrzymają komunikat o zakończeniu procesu transkrypcji.
     * @param newListener  instancja klasy implementującej
     *                     interfejs TranscriptionListener
     * @see  TranscriptionListener
     */
    synchronized public void addTranscriptionListener(TranscriptionListener newListener) {
        broadcaster.listeners.add(newListener);
    }

    private void notifyFinished() {
        EventQueue.invokeLater(broadcaster);
    }

    /**
     * Rozpoczyna proces transkrypcji. W celu uruchomienia wątku transkrybującego
     * należy wywołać metodę <code>start()</code>, która po stworzeniu wątku
     * wywoła niniejszą metodę <code>run()</code>. Po zakończeniu pracy wątku
     * wyniki dostępne będą przy pomocy metody <code>getStaveData()</code>.
     */
    @Override
    public void run() {

        int tones = voice.getToneCount();
        double tempo = Sonst.DEFAULT_TEMPO;
        distave.setTempo(tempo);

        double maxVolume = 0;
        for (int it = 0; it < tones; ++it) {
            maxVolume = Math.max(maxVolume, voice.getTone(it).getVolume());
        }

        Tonality tonalities[] = new Tonality[13];
        int rights[] = new int[13];
        tonalities[0] = new SharpTonality(0);
        for (int i=1; i<=6; ++i) {
            tonalities[i] = new SharpTonality(i);
            tonalities[i+6] = new FlatTonality(i);
        }

        if (maxVolume > 0)  {
            for (int it = 0; it < tones; ++it) {
                Tone t = voice.getTone(it);
                double dur = t.getTimeDuration();
                if (it + 1 < tones) {
                    dur = Math.max(dur, voice.getTone(it+1).getTimeOffset()-t.getTimeOffset());
                }

                long logDur = -Math.round(Sonst.log2(dur*tempo));
                if (logDur < 0) logDur = 0;
                if (logDur > 4) logDur = 4;

                long semi = Math.round(Sonst.semitoneFromFrequency(t.getFrequency()));
                if (semi<=0 || semi>127) continue;

                float off = (float)(t.getTimeOffset() * tempo);
                byte volume = (byte)Math.round(63.0 * t.getVolume() / maxVolume + 64.0);

                Note n = new Note((byte)logDur, (byte)0, (byte)semi, off, volume);
                List<Note> staff = (semi >= Sonst.TOP_STAFF_LOW_PITCH_BOUND) ? distave.top : distave.bottom;

                for (int i=0; i<13; ++i) {
                    if (tonalities[i].adapt(n).sign == Tonality.Sign.NONE) {
                        ++rights[i];
                    }
                }

                staff.add(n);
            }
        }

        int max = 0;
        for (int i=0; i<13; ++i) {
            if (rights[i] > max) {
                distave.setTonality(tonalities[i]);
                max = rights[i];
            }
        }

        notifyFinished();
    }

    /**
     * Zwraca wynik transkrypcji w postaci obiektu klasy StaveData.
     * @return  obiekt klasy StaveData stanowiący wynik transkrypcji
     * @see StaveData
     */
    public StaveData getStaveData() {
        return distave;
    }
}
