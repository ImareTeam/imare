package pl.umk.mat.imare.reco.internal;

import pl.umk.mat.imare.reco.Sonst;

/**
 * Pojedynczy impuls (maksimum) widma o określonej częstotliwości
 * i pozycji czasowej.
 * @author pieterer
 */
public final class LocalPeak implements Comparable {

    private final double t;
    private double f;
    private double s;
    private double v;

    private PeakChain home;
    private LocalPeak parent;

    /**
     * Tworzy nowy obiekt klasy LocalPeak o ustalonej pozycji czasowej,
     * standardowej wysokości (a<sup>1</sup>) oraz zerowym natężeniu.
     * @param time  czas wystąpienia impulsu
     */
    public LocalPeak(double time) {
        this.t = time;
        setSemitone(0);
        setVolume(0);
    }

    /**
     * Zwraca częstotliwość impulsu w hercach.
     */
    public double getFrequency() {
        return f;
    }
    /**
     * Ustawia częstotliwość impulsu w hercach, wyznacza też na jej podstawie
     * wysokość dźwięku w półtonach.
     */
    public void setFrequency(double f) {
        this.f = f;
        this.s = Sonst.semitoneFromFrequency(f);
    }

    /**
     * Zwraca wysokość impulsu w półtonach (względem a<sup>1</sup>).
     */
    public double getSemitone() {
        return s;
    }
    /**
     * Ustala wysokość impulsu w półtonach (względem a<sup>1</sup>), wyznacza
     * też na jej podstawie częstotliwość dźwięku.
     */
    public void setSemitone(double s) {
        this.s = s;
        this.f = Sonst.frequencyFromSemitone(s);
    }

    /**
     * Zwraca pozycję czasową impulsu (w sekundach).
     */
    public double getTime() {
        return t;
    }

    /**
     * Zwraca względną głośność impulsu.
     */
    public double getVolume() {
        return v;
    }

    /**
     * Ustala względną głośność impulsu.
     * @param v  głośność w skali względnej
     */
    public void setVolume(double v) {
        this.v = v;
    }

    /**
     * Ustala względną głośność impulsu na podstawie podanej amplitudy.
     * @param a  amplituda impulsu
     */
    public void setAmplitude(double a) {
        this.v = a*a;
    }

    /**
     * Pozwala na porównywanie impulsów pod względem porządku czasowego.
     * @param o  impuls do porównania
     * @return  -1, jeśli impuls <code>o</code> jest późniejszy od badanego,
     * 0 jeśli występują równocześnie lub 1, jeśli impuls <code>o</code>
     * występuje wcześniej
     * @throws UnsupportedOperationException  jeżeli obiekt <code>o</code>
     * nie jest obiektem klasy LocalPeak
     */
    @Override
    public int compareTo(Object o) throws UnsupportedOperationException {
        if (o instanceof LocalPeak) {
            return (int) Math.signum(t - ((LocalPeak)o).t);
        } else {
            throw new UnsupportedOperationException("Comparison between LocalPeak and "+o.getClass().getName()+" not supported.");
        }
    }

    /**
     * Przedstawia impuls w postaci łańcucha znaków.
     * @return tekstowy opis impulsu, np. "time = 3.3 s, freq = 440 Hz, volume = 3E+9"
     */
    @Override
    public String toString() {
        return String.format("time = %f s, freq = %f Hz, volume = %f", t,f,v);
    }

    /**
     * Ustala łańcuch <code>pc</code> jako macierzysty dla danego impulsu.
     * @param pc  łańcuch
     */
    public void setHomeChain(PeakChain pc) {
        home = pc;
    }

    /**
     * Zwraca macierzysty łańcuch impulsu, lub null, jeśli nie jest on przypisany
     * do żadnego łańcucha.
     */
    public PeakChain getHomeChain() {
        return home;
    }

    /**
     * Ustala częstotliwość podstawową dla szczytu.
     * @param lp  szczyt wyznaczający częstotliwość podstawową
     */
    public void setParentPeak(LocalPeak lp) {
        parent = lp;
    }

    /**
     * Zwraca szczyt wyznaczający częstotliwość podstawową, lub null, jeśli
     * bieżący szczyt sam ją wyznacza.
     */
    public LocalPeak getParentPeak() {
        return parent;
    }

    /**
     * Zwraca true, jeśli inny szczyt wyznacza częstotliwość podstawową bieżącego,
     * false w przeciwnym wypadku.
     */
    public boolean hasParent() {
        return parent!=null;
    }
}