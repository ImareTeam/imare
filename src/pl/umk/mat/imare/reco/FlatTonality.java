package pl.umk.mat.imare.reco;

/**
 * Pełni rolę tonacji bemolowej bądź neutralnej.
 * @author PieterEr
 */
public class FlatTonality extends Tonality {

    /**
     * Tworzy obiekt tonacji o zadanej ilości bemoli przykluczowych.
     * @param rank  żądana ilość bemoli przykluczowych
     */
    public FlatTonality(int rank) {
        super();
        for (int i=0; i<rank; ++i) {
            setSign(Sonst.FLAT_ORDER[i],-1);
        }
    }
}
