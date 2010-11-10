package pl.umk.mat.imare.reco;

/**
 * WindowFunction reprezentuje funkcję stosowaną jako tzw. okno czasowe,
 * określoną na przedziale [0..1] i przyjmującą na nim wartości nieujemne.
 * @author PieterEr
 */
public interface WindowFunction {

    /**
     * Zwraca wartości funkcji dla podanego argumentu.
     * @param x  argument z przedziału [0..1]
     * @return   wartość funkcji dla <code>x</code>
     */
    double value(double x);
}
