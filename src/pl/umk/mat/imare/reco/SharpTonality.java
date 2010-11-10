package pl.umk.mat.imare.reco;

/**
 * Pełni rolę tonacji krzyżykowej bądź neutralnej.
 * @author PieterEr
 */
public class SharpTonality extends Tonality {

    /**
     * Tworzy obiekt tonacji o zadanej ilości krzyżyków przykluczowych.
     * @param rank  żądana ilość krzyżyków przykluczowych
     */
    public SharpTonality(int rank) {
        super();
        for (int i=0; i<rank; ++i) {
            setSign(Sonst.SHARP_ORDER[i],1);
        }
    }
}
