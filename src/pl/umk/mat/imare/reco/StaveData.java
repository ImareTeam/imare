package pl.umk.mat.imare.reco;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Reprezentuje zapis nutowy na dwóch pięcioliniach,
 * łącznie z określoną tonacją oraz tempem.
 * @author PieterEr
 */
public class StaveData implements Serializable {

    /**
     * Nuty znajdujące się na górnej pięciolinii.
     */
    public final ArrayList<Note> top;

    /**
     * Nuty znajdujące się na dolnej pięciolinii.
     */
    public final ArrayList<Note> bottom;

    private Tonality tonality;
    private double tempo;

    /**
     * Zwraca tempo przechowywanej muzyki.
     * @return tempo w całych nutach na sekundę
     */
    public double getTempo() {
        return tempo;
    }

    /**
     * Ustala tempo dla przechowywanej muzyki.
     * @param tempo  tempo w całych nutach na sekundę
     */
    public void setTempo(double tempo) {
        if (tempo > 0) this.tempo = tempo;
    }

    /**
     * Zwraca tonację przechowywanej muzyki.
     * @return obiekt określający tonację
     * @see Tonality
     */
    public Tonality getTonality() {
        return tonality;
    }

    /**
     * Wskazuje tonację dla przechowywanej muzyki.
     * @param tonality  obiekt określający tonację
     * @throws IllegalArgumentException  jeśli <code>tonality == null</code>
     * @see Tonality
     */
    public void setTonality(Tonality tonality) {
        if (tonality == null) {
            throw new IllegalArgumentException("tonality must not be null");
        }
        this.tonality = tonality;
    }

    /**
     * Tworzy pusty obiekt klasy StaveData, z domyślnym tempem
     * i tonacją C-dur.
     */
    public StaveData() {
        top = new ArrayList<Note>();
        bottom = new ArrayList<Note>();
        tempo = Sonst.DEFAULT_TEMPO;
        tonality = new SharpTonality(0);
    }
}
