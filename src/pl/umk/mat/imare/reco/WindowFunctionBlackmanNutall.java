package pl.umk.mat.imare.reco;

/**
 * Okno czasowe Blackmana-Nutalla.
 * @author PieterEr
 */
public class WindowFunctionBlackmanNutall implements WindowFunction {

    @Override
    public double value(double x) {
        double dwapx = 2*Math.PI*x;
        return 0.3635819 - 0.4891775*Math.cos(dwapx) +
                + 0.1365995*Math.cos(dwapx*2) - 0.0106411*Math.cos(dwapx*3);
    }

}
