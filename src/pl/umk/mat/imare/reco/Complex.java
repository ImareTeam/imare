package pl.umk.mat.imare.reco;

/**
 * Prosta struktura przechowująca wartość liczby zespolonej w postaci trygonometrycznej.
 * @author PieterEr
 */
public final class Complex {

    /**
     * Moduł liczby zespolonej.
     */
    public final double amplitude;

    /**
     * Argument liczby zespolonej.
     */
    public final double phase;

    /**
     * Konstruktor klasy, tworzący postać trygonometryczną z części rzeczywistej i urojonej.
     * @param re  część rzeczywista liczby
     * @param im  część urojona liczby
     */
    public Complex(double re, double im) {
        amplitude = Math.hypot(re, im);
        phase = (amplitude>0) ? Math.atan2(im, re) : 0;
    }
}