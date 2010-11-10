package pl.umk.mat.imare.reco;

/**
 * Statyczna klasa zawierająca stałe liczbowe
 * i metody rachunkowe stosowane w rozpoznawaniu.
 * @author PieterEr
 */
public final class Sonst {

    /**
     * Logarytm naturalny z 2.
     */
    public static final double LN2 = Math.log(2.0);

    /**
     * Położenie dźwięku standardowego (a<sup>1</sup>) względem systemu MIDI.
     */
    public static final int STANDARD_PITCH = 69;

    /**
     * Częstotliwość dźwięku standardowego (a<sup>1</sup>) w hercach.
     */
    public static final double STANDARD_FREQUENCY = 440.0;

    /**
     * Logarytm o podstawie 2 z częstotliwości dźwięku standardowego (a<sup>1</sup>).
     */
    public static final double LOG_2_STANDARD_FREQUENCY = log2(STANDARD_FREQUENCY);

    /**
     * Ilość półtonów przypadająca na jedną oktawę.
     */
    public static final int SEMITONES_PER_OCTAVE = 12;

    /**
     * Najniższy dźwięk, który ma się pojawiać na górnej, nie zaś na dolnej
     * pięciolinii, względem systemu MIDI (60 = c<sup>1</sup>).
     */
    public static final int TOP_STAFF_LOW_PITCH_BOUND = 60;

    /**
     * Domyślne tempo utworów muzycznych, przedstawione w całych nutach na sekundę.
     */
    public static final double DEFAULT_TEMPO = 0.25;

    /**
     * Kolejność, w jakiej pojawiają się kolejne krzyżyki na kole kwintowym,
     * dźwięki liczone są od c = 0.
     */
    public static final int[] SHARP_ORDER = { 3,0,4,1,5,2,6 };

    /**
     * Kolejność, w jakiej pojawiają się kolejne bemole na kole kwintowym,
     * dźwięki liczone są od c = 0.
     */
    public static final int[] FLAT_ORDER = { 6,2,5,1,4,0,3 };

    /**
     * Zwraca logarytm o podstawie 2 z podanego argumentu.
     * @param x  liczba rzeczywista dodatnia
     * @return log<sub>2</sub>(x)
     */
    public static double log2(double x) {
        return Math.log(x) / LN2;
    }

    /**
     * Sprawdza czy dana liczba jest liczbą pierwszą.
     * Wykorzystywany jest algorytm o złożoności O(n<sup>½</sup>).
     * @param n
     * @return true, jeśli podana liczba jest liczbą pierwszą,
     * false w przeciwnym wypadku
     */
    public static boolean isPrime(int n) {
        if (n<2) return false;
        for (int k=2; k*k<=n; ++k) if (n%k==0) return false;
        return true;
    }

    /**
     * Przelicza podaną częstotliwość na współrzędną w tzw. Bark scale.
     * @param freq  częstotliwość do przeliczenia (w hercach)
     * @return współrzędna w Bark scale
     */
    public static double barkFromFrequency(double freq) {
        return 13.0*Math.atan(0.00076*freq) + 3.5*Math.atan(freq*freq/5.625E+7);
    }

    /**
     * Przelicza podaną częstotliwość na wysokość dźwięku w półtonach.
     * @param freq  częstotliwość do przeliczenia (w hercach)
     * @return wysokość dźwięku w półtonach względem a<sup>1</sup>
     */
    public static double semitoneFromFrequency(double freq) {
        return (double) SEMITONES_PER_OCTAVE * (log2(freq) - LOG_2_STANDARD_FREQUENCY) + STANDARD_PITCH;
    }

    /**
     * Przelicza podaną wysokość dźwięku w półtonach na częstotliwość.
     * @param sem  wysokość dźwięku w półtonach względem a<sup>1</sup>
     * @return częstotliwość dźwięku w hercach
     */
    public static double frequencyFromSemitone(double sem) {
        return Math.pow(2.0, (sem-STANDARD_PITCH) / (double) SEMITONES_PER_OCTAVE + LOG_2_STANDARD_FREQUENCY);
    }
}
