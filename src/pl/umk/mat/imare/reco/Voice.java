package pl.umk.mat.imare.reco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Zbiór dźwięków (obiektów klasy Tone) samoorganizujący się
 * w porządku chronologicznym.
 * @author PieterEr
 */
public class Voice implements Iterable<Tone> {

    private ArrayList<Tone> tones;

    /**
     * Tworzy pusty obiekt klasy Voice
     */
    public Voice() {
        this.tones = new ArrayList<Tone>();
    }

    /**
     * Zwraca rozmiar zbioru.
     * @return  ilość aktualnie przechowywanych dźwięków
     */
    public int getToneCount() {
        return tones.size();
    }

    /**
     * Zwraca dźwięk o wskazanym numerze.
     * @param n  numer wybranego dźwięku
     * @throws IndexOutOfBoundsException  jeżeli <code>n&lt;0</code>
     * lub <code>n&gt;=getToneCount()</code>
     * @return dźwięk o numerze równym <code>n</code>
     */
    public Tone getTone(int n) throws IndexOutOfBoundsException {
        return tones.get(n);
    }

    /**
     * Dodaje dźwięk do zbioru przechowywanych dźwięków.
     * @param newTone  dźwięk do dodania
     */
    public void addTone(Tone newTone) {
        if (newTone != null) {
            float thisTimeOffset = newTone.getTimeOffset();
            int pos = tones.size();
            while (pos>0 && tones.get(pos-1).getTimeOffset() > thisTimeOffset) --pos;
            tones.add(pos, newTone);
        }
    }

    /**
     * Pozwala na przeglądanie elementów w porządku chronologicznym.
     * @return  iterator do zbioru elementów (bez możliwości modyfikacji)
     */
    @Override
    public Iterator<Tone> iterator() {
        return Collections.unmodifiableList(tones).iterator();
    }
}
