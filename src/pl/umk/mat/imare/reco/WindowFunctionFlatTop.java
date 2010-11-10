package pl.umk.mat.imare.reco;

/**
 * Okno czasowe rodzaju flat-top.
 * @author PieterEr
 */
public final class WindowFunctionFlatTop implements WindowFunction {

    private static final double[] a = { 1, 1.93, 1.29, 0.338, 0.032 };

    @Override
    public double value(double x) {
        double dwapx = 2*Math.PI*x;
        return 1 - 1.93*Math.cos(dwapx) + 1.29*Math.cos(dwapx*2) - 0.338*Math.cos(dwapx*3) + 0.032*Math.cos(dwapx*4);
    }

}