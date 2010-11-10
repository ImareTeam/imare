package pl.umk.mat.imare.reco.internal;

/**
 * Struktura przedstawiająca liczbę zespoloną w postaci algebraicznej,
 * dostosowaną do wykonywania obliczeń.
 * @author PieterEr
 */
public final class AlgebraicComplex {

    /**
     * Wartość rzeczywista liczby zespolonej.
     */
    public final double re;

    /**
     * Wartość urojona liczby zespolonej.
     */
    public final double im;

    /**
     * Tworzy nową liczbę zespoloną o podanych wartościach.
     * @param re  wartość rzeczywista liczby zespolonej
     * @param im  wartość urojona liczby zespolonej
     */
    public AlgebraicComplex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    /**
     * Zwraca wynik mnożenia przez inną liczbę zespoloną.
     * @param a  inna liczba zespolona
     * @return  this · a;
     */
    public AlgebraicComplex mul(AlgebraicComplex a) {
        return new AlgebraicComplex(re*a.re - im*a.im, re*a.im + im*a.re);
    }

    /**
     * Zwraca wynik dodawania z inną liczbą zespoloną.
     * @param a  inna liczba zespolona
     * @return  this + a
     */
    public AlgebraicComplex add(AlgebraicComplex a) {
        return new AlgebraicComplex(re+a.re, im+a.im);
    }

    /**
     * Zwraca liczbę sprzężoną.
     * @return liczba sprzężona do a
     */
    public AlgebraicComplex conj() {
        return new AlgebraicComplex(re,-im);
    }

    /**
     * Zwraca kwadrat modułu liczby zespolonej.
     * @return |this|<sup>2</sup>
     */
    public double abs2() {
        return re*re + im*im;
    }
}
