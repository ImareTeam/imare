package pl.umk.mat.imare.reco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import pl.umk.mat.imare.io.Wave;
import pl.umk.mat.imare.reco.internal.LocalPeak;

/**
 * Wątek analizujący widmo i wykrywający w nim maksima, pełniący funkcję
 * pomocniczą względem klasy Recognizer.
 * @author PieterEr
 */
public class PeakPicker implements Runnable {

    private float PROGRESS_GRID = 20;
    
    /**
     * Czas reakcji estymatora poziomu szumu (w sekundach).
     */
    private final double noiseHalfLife = 0.1;

    /**
     * W celu identyfikacji szeregów harmonicznych porównywane są częstotliwości
     * poszczególnych tonów. Poniższy parametr określa maksymalną akceptowalną
     * niezgodność w częstotliwości tonów (wyrażoną w półtonach).
     */
    private double maxHarmonicDisplacement = 0.3;

    /**
     * Każdy bin transformaty FFT lub constant-Q ma przypisaną pewną
     * częstotliwość. Jednakże, analizując zmianę fazy dźwięku, można
     * niezależnie wyznaczyć poprawkę do tej częstotliwości, a jeśli okaże się,
     * że właściwa częstotliwość znacznie różni się od zakładanej, można taki
     * przedział zignorować.
     * Parametr poniższy określa maksymalne dozwolone przesunięcie
     * w częstotliwości w jednostkach szerokości przedziału transformaty constant-Q.
     */
    private double pitchMaxDisplacement = 0.55;

    private final PickerUpdater parent;
    private final int id;

    private final Wave wave;
    private final EfficientConstantQ cq;
    private final int startSample;
    private final int endSample;
    private final float samplingRate;
    private final int binsShift;
    private final double timeShift;

    private final double noiseMedianParameter;
    private final double overtoneSensitivity;

    private final int[] buffer;
    private Complex[] spectrum;
    private Complex[] spectrumOld;

    private int progressOld = 0;

    private int binsBuffer;
    private int offset;
    private double time;

    /**
     * Tworzy nowy obiekt klasy PeakPicker o zadanych parametrach.
     * @param parent  nadzorca obiektu
     * @param wave  plik dźwiękowy do przeanalizowania
     * @param startSample  początkowy numer próbki
     * @param endSample  końcowy numer próbki
     * @param id  unikalny identyfikator wątku
     * @param cq  transformacja Constant-Q
     */
    public PeakPicker(PickerUpdater parent, Wave wave, int startSample, int endSample, int id, EfficientConstantQ cq) {

        this.parent = parent;
        this.id = id;
        this.wave = wave;
        this.cq = cq;
        this.startSample = startSample;
        this.endSample = endSample;

        this.samplingRate = wave.getAudioFormat().getSampleRate();
        this.binsShift = (int) Math.round(parent.reco.getTimeShiftApprox() * samplingRate);
        this.timeShift = binsShift / samplingRate;
        this.binsBuffer = cq.getInputSize();
        this.buffer = new int[binsBuffer];
        this.spectrum = new Complex[cq.getOutputSize()];
        this.spectrumOld = new Complex[cq.getOutputSize()];

        this.noiseMedianParameter = parent.reco.getNoiseMedianParameter();
        this.overtoneSensitivity = parent.reco.getOvertoneSensitivity();
    }

    private void notifyProgress() {
        int progress = (int)(PROGRESS_GRID*(offset-startSample)/(endSample-startSample));
        if (progress > progressOld) {
            parent.pickerProgress((progress-progressOld)/PROGRESS_GRID);
            progressOld = progress;
        }
    }

    static private double computeNoiseLevel(Complex[] array) {

        double[] noise = new double[array.length];
        for (int i=array.length-1; i>=0; --i) {
            noise[i] = array[i].amplitude;
        }
        Arrays.sort(noise);
        return noise[noise.length/2];
    }

    private ArrayList<LocalPeak> detectPeaks(double threshold) {

        int bins = spectrum.length;
        ArrayList<LocalPeak> peaks = new ArrayList<LocalPeak>();

        for (int b = 1; b + 1 < bins; ++b) {
            Complex val = spectrum[b];
            if (val.amplitude > Math.max(threshold, Math.max(spectrum[b - 1].amplitude, spectrum[b + 1].amplitude))) {
                LocalPeak np = identifyPeak(cq.getBinFrequency(b), spectrumOld[b], val);
                if (np != null) {
                    peaks.add(np);
                }
            }
        }
        return peaks;
    }

    private LocalPeak identifyPeak(double freq0, Complex valFormer, Complex valLatter) {

        double d = (valLatter.phase - valFormer.phase) / (2.0 * Math.PI) - timeShift * freq0;
        d -= Math.floor(d);
        if (d > 0.5) {
            d -= 1.0;
        }
        double freq = freq0 + d / timeShift;
        double kappa = Math.abs(EfficientConstantQ.BINS_PER_OCTAVE * Sonst.log2(freq / freq0));
        if (kappa >= pitchMaxDisplacement) {
            return null;
        }

        LocalPeak lp = new LocalPeak(time);
        lp.setFrequency(freq);
        lp.setAmplitude(valLatter.amplitude);
        return lp;
    }

    private boolean cancelled = false;

    /**
     * Przerywa pracę wątku.
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * W celu uruchomienia wątku należy wywołać np. <code>new Thread(p).start()</code>,
     * co spowoduje wywołanie metody <code>run()</code> i rozpoczęcie wykrywania
     * maksimów. Po skończonej analizie, rezultat pracy przekazany zostanie do
     * obiektu nadawcy.
     */
    @Override
    public void run() {

        double oldNoiseLevelWeight = noiseHalfLife>0 ? Math.pow(0.5, timeShift/noiseHalfLife) : 0.0;
        double currentNoiseLevelWeight = 1.0 - oldNoiseLevelWeight;

        TreeMap<Double,ArrayList<LocalPeak>> totalResult = new TreeMap<Double,ArrayList<LocalPeak>>();

        loadFirstSpectrum();
        double noiseLevel = computeNoiseLevel(spectrum);
        while (!cancelled && loadNextSpectrum()) {

            notifyProgress();
            
            noiseLevel = oldNoiseLevelWeight*noiseLevel + currentNoiseLevelWeight*computeNoiseLevel(spectrum);
            ArrayList<LocalPeak> currentPeaks = detectPeaks(noiseMedianParameter * noiseLevel);

            int peakN = currentPeaks.size();
            double currentMax = 0;

            for (int j = peakN - 1; j > 0; --j) {

                LocalPeak phi = currentPeaks.get(j);
                double fhi = phi.getFrequency();
                double vhi = phi.getVolume();
                currentMax = Math.max(currentMax, vhi);

                for (int i = j - 1; i >= 0; --i) {
                    LocalPeak plo = currentPeaks.get(i);
                    double flo = plo.getFrequency();

                    int n = (int) Math.round(fhi / flo);
                    if (n > 1) {
                        double delta = Math.abs(Sonst.semitoneFromFrequency(fhi) - Sonst.semitoneFromFrequency(flo * n));
                        if (delta < maxHarmonicDisplacement && 0.9 * overtoneSensitivity * vhi < plo.getVolume()) {
                            phi.setParentPeak(plo);
                            break;
                        }
                    }
                }
            }

            totalResult.put(time, currentPeaks);
        }
        if (cancelled) {
            totalResult.clear();
            totalResult = null;
        }
        parent.pickerFinish(id,totalResult);
    }

    private boolean loadFirstSpectrum() {

        offset = startSample;
        if (offset >= binsShift) offset -= binsShift;
        time = (offset + binsBuffer / 2) / samplingRate;
        if (offset >= endSample || wave.readMono(buffer, offset) < binsBuffer) {
            return false;
        }

        cq.process(buffer, spectrum);
        return true;
    }

    private boolean loadNextSpectrum() {

        offset += binsShift;
        time = (offset + binsBuffer / 2) / samplingRate;
        if (offset >= endSample || wave.readMono(buffer, offset) < binsBuffer) {
            return false;
        }

        Complex[] recycling = spectrumOld;
        spectrumOld = spectrum;
        spectrum = recycling;

        cq.process(buffer, spectrum);
        return true;
    }
}
