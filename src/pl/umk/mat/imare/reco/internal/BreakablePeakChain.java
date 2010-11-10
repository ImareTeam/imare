package pl.umk.mat.imare.reco.internal;

import java.util.LinkedList;

/**
 *
 * @author PieterEr
 */
public final class BreakablePeakChain extends PeakChain {

    private final LinkedList<LocalPeak> avBuffer;
    private final double sigmaParameter;
    private final double timeSpan;

    private double volumeSum;
    private double volume2Sum;

    public BreakablePeakChain(double timeSpan, double sigmaParameter) {

        super();
        this.avBuffer = new LinkedList<LocalPeak>();
        this.timeSpan = timeSpan;
        this.sigmaParameter = sigmaParameter;
        volumeSum = 0;
        volume2Sum = 0;
    }

    @Override
    public void addPeak(LocalPeak t) {

        super.addPeak(t);
        double timeLimit = t.getTime() - timeSpan;
        while (!avBuffer.isEmpty()) {
            LocalPeak first = avBuffer.getFirst();
            if (first.getTime() < timeLimit) {
                double v = first.getVolume();
                volumeSum -= v;
                volume2Sum -= v*v;
                avBuffer.removeFirst();
            } else {
                break;
            }
        }
        avBuffer.add(t);
        double v = t.getVolume();
        volumeSum += v;
        volume2Sum += v*v;
    }

    public boolean wouldBreak(LocalPeak t) {

        int size = avBuffer.size();
        if (size < 10) {
            return false;
        }
        double mean = volumeSum/size;
        double variance = volume2Sum/(size-1) - mean*mean;
        return t.getVolume() > mean + sigmaParameter*Math.sqrt(variance);
    }
}