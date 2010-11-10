package pl.umk.mat.imare.reco;

/**
 * Okno czasowe Blackmana.
 * @author PieterEr
 */
public final class WindowFunctionBlackman implements WindowFunction {

    private final double α = 0.16;

    @Override
    public double value(double x) {
        //return (1.0-α)/2.0 - 0.5*Math.cos(2.0*Math.PI*x) + (α/2.0)*Math.cos(4.0*Math.PI*x);
        double cx = Math.cos(2*Math.PI*x);
        return 0.5*(1.0 + (2*α*cx-1.0)*cx) - α;
    }
}
