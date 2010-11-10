package pl.umk.mat.imare.reco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import javax.sound.sampled.LineUnavailableException;
import pl.umk.mat.imare.reco.internal.BreakablePeakChain;
import pl.umk.mat.imare.reco.internal.LocalPeak;
import pl.umk.mat.imare.reco.internal.PeakChain;

/**
 * Moduł rozpoznający muzykę strumieniowaną na bieżąco
 * z urządzenia wejściowego.
 * @author PieterEr
 */
public class RTRecognizer extends Thread {

    /**
     * Stan wybranej wartości w widmie.
     */
    public static enum PixelType {

        /**
         * Wartość znajduje się poniżej progu detekcji.
         */
        LOW,

        /**
         * Wartość znajduje się powyżej progu detekcji i pomyślnie przeszła
         * weryfikację.
         */
        HIGH,
        
        /**
         * Wartość znajduje się powyżej progu detekcji, ale została odrzucona
         * w trakcie weryfikacji.
         */
        FAKE
    }

    /**
     * Częstotliwość próbkowania tworzonego strumienia.
     */
    private static final int SAMPLING_RATE = 22050;
    /**
     * Dolna granica częstotliwości w hercach.
     */
    private final double freqLowBound = 110;
    /**
     * Górna (przybliżona) granica częstotliwości w hercach.
     */
    private final double freqHighBound = 5000;
    /**
     * Przybliżony krok czasowy (w sekundach).
     */
    private double timeShiftApprox = 0.01;
    /**
     * Funkcja okna dla transformaty constant-Q.
     */
    private final WindowFunction windowFunction = new WindowFunctionBlackman();
    /**
     * Próg rejestrowanego sygnału, podany jako wielokrotność poziomu szumu
     * (jako estymator poziomu szumu wykorzystana została mediana).
     */
    private final double noiseSigmaParameter = 30;
    /**
     * Maksymalna dozwolona czasowa nieciągłość dźwięku.
     */
    private final double timeMaxSeparation = 0.05;
    /**
     * Maksymalna dozwolona fluktuacja częstotliwości w przebiegu dźwięku.
     */
    private final double semiMaxSeparation = 0.5;
    /**
     * Minimalna dozwolona długość trwania dźwięku (w sekundach).
     * Dźwięki o długości mniejszej niż podana będą pomijane.
     */
    private final double timeMinDuration = 0.05;
    /**
     * Każdy bin transformaty FFT lub constant-Q ma przypisaną pewną
     * częstotliwość. Jednakże, analizując zmianę fazy dźwięku, można
     * niezależnie wyznaczyć poprawkę do tej częstotliwości, a jeśli okaże się,
     * że właściwa częstotliwość znacznie różni się od zakładanej, można taki
     * bin zignorować.
     * Parametr poniższy określa maksymalne dozwolone przesunięcie
     * w częstotliwości w jednostkach szerokości binu transformaty constant-Q.
     */
    private final double pitchMaxDisplacement = 1.0;
    /**
     * W celu identyfikacji szeregów harmonicznych porównywane są częstotliwości
     * poszczególnych tonów. Poniższy parametr określa maksymalną akceptowalną
     * niezgodność w częstotliwości tonów (wyrażoną w półtonach).
     */
    private final double maxHarmonicDisplacement = 0.3;
    /**
     * Ustawia dopuszczalną przestrzeń głośności
     * między najgłośniejszymi a najcichszymi dźwiękami w utworze.
     */
    private final double maxToMinRatio = 30;
    /**
     * Czułość detekcji tonów wyższych rzędów.
     */
    private final double overtoneSensitivity = 0.9;
    /**
     * Szerokość przedziału czasu branego pod uwagę przy wykrywaniu repetycji
     * (w sekundach).
     */
    private final double repetitionAvgTimeSpan = 0.2;
    /**
     * Czułość wykrywania repetycji dźwięku.
     */
    private final double repetitionSensitivity = 1.5;

    private RealtimeStream rts;
    private EfficientConstantQ cq;
    private int binsBuffer;
    private int binsShift;
    private int binsPos;
    private double timeShift;
    private double timeOffset;
    private int[] bufInt;
    private Complex[] spectrum;
    private Complex[] spectrumOld;
    private double currentMaxVolume = 0;
    private double time;
    private LinkedList<BreakablePeakChain> currentChains;
    private LinkedList<RTListener> listeners;

    /** Dodaje obiekt <code>newListener</code> do listy obiektów, które będą
     * otrzymywać komunikaty o postępach (a w szczególności, o zakończeniu)
     * procesu rozpoznawania.
     * @param newListener  instancja klasy implementującej
     *                     interfejs RTListener
     * @see  RTListener
     */
    public void addListener(RTListener newListener) {
        listeners.add(newListener);
    }

    /**
     * Tworzy nowy obiekt klasy RTRecognizer z domyślnymi parametrami.
     * @throws LineUnavailableException  nie udało się uzyskać dostępu do urządzenia
     */
    public RTRecognizer() throws LineUnavailableException {
        super("Realtime Recognizing Thread");
        cq = new EfficientConstantQ(17.309933963814844981086787570342, SAMPLING_RATE);
        this.cq.setFrequencyRange(freqLowBound, freqHighBound);
        this.cq.setWindowFunction(windowFunction);
        this.binsBuffer = this.cq.getInputSize();
        this.binsShift = (int) Math.round(timeShiftApprox * SAMPLING_RATE);
        this.timeShift = (double) binsShift / (double) SAMPLING_RATE;
        rts = new RealtimeStream(SAMPLING_RATE);
        listeners = new LinkedList<RTListener>();
        setDaemon(true);
    }

    private boolean loadFirstSpectrum() {
        rts.updateBuffer(bufInt,binsBuffer);
        binsPos = binsBuffer / 2;

        cq.process(bufInt, spectrum);
        time = (double) (binsPos) / (double) SAMPLING_RATE;
        return true;
    }

    private boolean loadNextSpectrum() {
        rts.updateBuffer(bufInt,binsShift);
        binsPos += binsShift;

        Complex[] temp = spectrumOld;
        spectrumOld = spectrum;
        spectrum = temp;

        cq.process(bufInt, spectrum);
        time = (double) (binsPos) / (double) SAMPLING_RATE;
        return true;
    }
    private boolean cancelled = false;

    /**
     * Przerywa proces rozpoznawania.
     */
    synchronized public void terminate() {
        cancelled = true;
    }

    /**
     * Zwraca informację o tym, czy proces rozpoznawania został anulowany.
     * @return <code>true</code>, jeśli proces rozpoznawania został anulowany,
     * <code>false</code> w przeciwnym wypadku
     */
    synchronized public boolean hasBeenTerminated() {
        return cancelled;
    }

    private boolean isRetired(PeakChain oldChain) {
        return time > oldChain.getEndTime() + timeMaxSeparation;
    }

    private void removeRetiredChains() {
        Iterator<? extends PeakChain> li = currentChains.iterator();
        while (li.hasNext()) {
            PeakChain next = li.next();
            if (isRetired(next)) {
                processChain(next);
                li.remove();
            }
        }
    }

    private void processChain(PeakChain chain) {
        if (chain.getEndTime()-chain.getStartTime() >= timeMinDuration) {
            Tone t = chain.makeTone(-(float)timeOffset);
            currentMaxVolume = Math.max(currentMaxVolume, t.getVolume());
            Note n = t.makeNote(Sonst.DEFAULT_TEMPO, currentMaxVolume);
            if (n != null) {
                broadcast(n);
            }
        }
    }

    private void broadcast(Note n) {
        for (RTListener l : listeners) {
            l.noteFound(this, n);
        }
    }

    private void notifyFinished() {
        for (RTListener l : listeners) {
            l.recognitionFinished(this);
        }
    }

    private void broadcastSpectrum(double[] values, PixelType[] active) {
        double maxAmplitude = 0;
        for (int b = 0; b < values.length; ++b) {
            maxAmplitude = Math.max(maxAmplitude, values[b]);
        }
        if (maxAmplitude > 0) {
            for (int b = 0; b < values.length; ++b) {
                values[b] /= maxAmplitude;
            }
        }
        for (RTListener l : listeners) {
            l.spectrumNotification(this, values, active);
        }
    }

    private LocalPeak identifyPeak(double freq0, Complex valLatter, Complex valFormer) {

        double d = (valLatter.phase - valFormer.phase) / (2.0 * Math.PI) - timeShift * freq0;
        d -= Math.floor(d+0.5);
        double freq = freq0 + d/timeShift;
        double kappa = Math.abs(EfficientConstantQ.BINS_PER_OCTAVE * Sonst.log2(freq/freq0));
        if (kappa >= pitchMaxDisplacement) return null;

        LocalPeak lp = new LocalPeak(time);
        lp.setFrequency(freq);
        lp.setAmplitude(valLatter.amplitude);
        return lp;
    }

    private void addToMatching(LocalPeak newPeak) {
        if (Double.isNaN(timeOffset)) {
            timeOffset = time;
        }

        double semiNew = newPeak.getSemitone();
        Iterator<BreakablePeakChain> it = currentChains.iterator();
        while (it.hasNext()) {
            BreakablePeakChain chain = it.next();
            double semiOld = Sonst.semitoneFromFrequency(chain.getMeanFrequency());
            if (Math.abs(semiNew - semiOld) < semiMaxSeparation) {
                if (chain.wouldBreak(newPeak) && chain.getEndTime()-chain.getStartTime() > timeMinDuration) {
                    it.remove();
                    break;
                } else {
                    chain.addPeak(newPeak);
                    return;
                }
            }
        }
        BreakablePeakChain newChain = new BreakablePeakChain(repetitionAvgTimeSpan, 3.0 / repetitionSensitivity);
        newChain.addPeak(newPeak);
        currentChains.add(newChain);
    }

    /**
     * Rozpoczyna proces rozpoznawania. W celu uruchomienia wątku rozpoznającego
     * należy wywołać metodę <code>start()</code>, która po stworzeniu wątku
     * wywoła niniejszą metodę <code>run()</code>. Wyniki są na bieżąco
     * przesyłane zapamiętanym obiektom oczekującym na wiadomości.
     */
    @Override
    public void run() {
        this.bufInt = new int[binsBuffer];
        this.timeOffset = Double.NaN;
        this.currentChains = new LinkedList<BreakablePeakChain>();

        int bins = cq.getOutputSize();
        this.spectrumOld = new Complex[bins];
        this.spectrum = new Complex[bins];
        double[] noise = new double[bins];

        rts.start();
        if (loadFirstSpectrum()) while (!hasBeenTerminated() && loadNextSpectrum()) {
            removeRetiredChains();

            ArrayList<LocalPeak> currentPeaks = new ArrayList<LocalPeak>();
            double[] values = new double[bins];
            PixelType[] active = new PixelType[bins];

            for (int b = 0; b < bins; ++b) {
                values[b] = noise[b] = spectrum[b].amplitude;
                active[b] = PixelType.LOW;
            }
            Arrays.sort(noise);
            double threshold = noiseSigmaParameter * noise[bins>>1];

            for (int b = 1; b+1 < bins; ++b) {
                Complex val = spectrum[b];
                if (val.amplitude > Math.max(threshold,Math.max(spectrum[b-1].amplitude, spectrum[b+1].amplitude))) {
                    LocalPeak np = identifyPeak(cq.getBinFrequency(b),val,spectrumOld[b]);
                    if (np == null) {
                        active[b] = PixelType.FAKE;
                    } else {
                        active[b] = PixelType.HIGH;
                        currentPeaks.add(np);
                    }
                }
            }

            double maxAmplitude = 0;
            for (int b = 0; b < bins; ++b) {
                maxAmplitude = Math.max(maxAmplitude, values[b]);
            }
            if (maxAmplitude > 0) for (int b = 0; b < bins; ++b) {
                values[b] /= maxAmplitude;
            }
            broadcastSpectrum(values, active);

            double max = 0;
            for (int i=0; i<currentPeaks.size(); ++i) {
                LocalPeak plo = currentPeaks.get(i);
                double flo = plo.getFrequency();
                double totalVolume = plo.getVolume();
                for (int j=currentPeaks.size()-1; j>i; --j) {
                    LocalPeak phi = currentPeaks.get(j);
                    double fhi = phi.getFrequency();

                    int n = (int) Math.round(fhi / flo);
                    if (n > 1) {
                        double delta = Math.abs(Sonst.semitoneFromFrequency(fhi) - Sonst.semitoneFromFrequency(flo*n));
                        if (delta < maxHarmonicDisplacement) {
                            totalVolume += phi.getVolume();
                        }
                    }
                }

                for (int j=currentPeaks.size()-1; j>i; --j) {
                    LocalPeak phi = currentPeaks.get(j);
                    double fhi = phi.getFrequency();

                    int n = (int) Math.round(fhi / flo);
                    if (n > 1) {
                        double delta = Math.abs(Sonst.semitoneFromFrequency(fhi) - Sonst.semitoneFromFrequency(flo*n));
                        if (delta < maxHarmonicDisplacement) {
                            if ((1.0+overtoneSensitivity) * currentPeaks.get(j).getVolume() > totalVolume) {
                                totalVolume -= phi.getVolume();
                            } else {
                                currentPeaks.remove(j);
                            }
                        }
                    }
                }
                max = Math.max(max, totalVolume);
                plo.setVolume(totalVolume);
            }

            for (LocalPeak plo : currentPeaks) {
                if (plo.getVolume()*maxToMinRatio >= max) {
                    addToMatching(plo);
                }
            }
        }
        rts.stop();
        rts = null;
        cq = null;

        for (PeakChain chain : currentChains) {
            processChain(chain);
        }
        notifyFinished();
    }
}
