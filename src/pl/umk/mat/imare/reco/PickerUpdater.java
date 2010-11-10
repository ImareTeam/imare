package pl.umk.mat.imare.reco;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import pl.umk.mat.imare.reco.internal.LocalPeak;

/**
 * Pomocniczy obiekt nadzorujący wątki rozpoznające równolegle. Ma on za zadanie
 * przyjmować od wątków informacje o postępie prac i przekazywać je do
 * oczekujących elementów interfejsu użytkownika. Po zakończeniu prac zwalnia
 * semafor powiadamiający wątek obiektu Recognizer, który winien wtedy oczekiwać
 * na tymże semaforze po wywołaniu metody waitForPickersToFinish().
 * @author PieterEr
 */
public class PickerUpdater {

    private final List<RecognitionListener> listeners;

    /**
     * Nadrzędny obiekt rozpoznający.
     */
    public final Recognizer reco;
    private final Semaphore waiter;

    private final int threadCount;

    private int finished = 0;
    private int completed = 0;
    private float progress = 0.0f;
    private TreeMap<Double,ArrayList<LocalPeak>>[] results;

    /**
     * Tworzy nowy obiekt pomocniczy.
     * @param reco  nadrzędny obiekt rozpoznający
     * @param listeners  lista obiektów oczekujących na komunikaty
     */
    @SuppressWarnings({"unchecked"})
    public PickerUpdater(Recognizer reco, List<RecognitionListener> listeners) {
        this.listeners = listeners;
        this.threadCount = reco.getThreadCount();
        this.reco = reco;
        this.waiter = new Semaphore(0);
        this.results = new TreeMap[threadCount];
    }

    /**
     * Sygnalizuje postęp prac wątku rozpoznającego.
     * @param delta  wzrost wskaźnika postępu (0..1) dla pojedynczego,
     * wywołującego wątku
     */
    synchronized public void pickerProgress(float delta) {
        progress += (delta / threadCount);
        for (final RecognitionListener l : listeners) {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    l.progressChanged(reco, progress);
                }
            });
        }
    }

    /**
     * Sygnalizuje zakończenie prac przez pojedynczy wątek.
     * @param id  identyfikator wątku (0..threadCount-1)
     * @param result  wyznaczone przez wątek maksima widma
     */
    synchronized public void pickerFinish(int id, TreeMap<Double,ArrayList<LocalPeak>> result) {
        if (result != null && results[id] == null) {
            results[id] = result;
            ++completed;
        }
        if (++finished == threadCount) {
            waiter.release();
        }
    }

    /**
     * Oczekuje na zakończenie pracy wszystkich wątków rozpoznających i zwraca
     * rezultat ich pracy.
     * @return  wykryte maksima widma, lub null, jeśli praca została przerwana
     * lub zakończyła się błędem
     */
    public TreeMap<Double,ArrayList<LocalPeak>> waitForPickersToFinish() {
        TreeMap<Double,ArrayList<LocalPeak>> result = null;
        try {
            waiter.acquire();
            if (completed == threadCount) {
                result = new TreeMap<Double,ArrayList<LocalPeak>>();
                for (TreeMap<Double,ArrayList<LocalPeak>> lalp : results) {
                    result.putAll(lalp);
                }
            }
        } catch (InterruptedException ex) {
            result = null;
        }
        return result;
    }
}
