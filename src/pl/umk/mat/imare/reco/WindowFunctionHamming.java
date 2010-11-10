package pl.umk.mat.imare.reco;

/**
 * Okno czasowe Hamminga.
 * @author pieterer
 */
public class WindowFunctionHamming implements WindowFunction {

    @Override
    public double value(double x) {
        return 0.54 - 0.46*Math.cos(2.0*Math.PI*x);
    }

}
