package pl.umk.mat.imare.reco;

import java.awt.EventQueue;
import java.util.ArrayList;
import pl.umk.mat.imare.io.Wave;
import pl.umk.mat.imare.reco.internal.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import javax.sound.sampled.AudioFormat;

/**
 * Moduł rozpoznający muzykę z wczytanej struktury Wave. W przypadku większej
 * niż 1 ilości dostępnych rdzeni procesorów, dzieli podany utwór na kilka
 * równych części i każdą z nich zleca do rozpoznania innemu wątkowi. Łączenie
 * wyników odbywa się w wątku głównym.
 * @author PieterEr
 */
public class Recognizer extends Thread {

//------------------------------- U S T A W I E N I A  U Ż Y T K O W N I K A ---

    /**
     * Ustawia dolną granicę częstotliwości (w hercach).
     */
    public void setFreqLowBound(double freqLowBound) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.freqLowBound = freqLowBound;
    }
    private double freqLowBound = 110;


    /**
     * Ustawia górną (przybliżoną) granicę częstotliwości w hercach.
     */
    public void setFreqHighBoundApprox(double freqHighBound) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.freqHighBound = freqHighBound;
    }
    private double freqHighBound = 10000;


    /**
     * Ustawia okno czasowe (ang. window function) dla transformaty constant-Q.
     */
    public void setWindowFunction(WindowFunction windowFunction) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.windowFunction = windowFunction;
    }
    private WindowFunction windowFunction = null;


    /**
     * Ustawia przybliżony krok czasowy (w sekundach).
     */
    public void setTimeShiftApprox(double timeShiftApprox) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.timeShiftApprox = timeShiftApprox;
    }
    public double getTimeShiftApprox() {
        return timeShiftApprox;
    }
    private double timeShiftApprox = 0.01;


    /**
     * Ustawia próg rejestrowanego sygnału, podany jako wielokrotność poziomu
     * szumu (jako estymator poziomu szumu wykorzystana zostanie mediana).
     */
    public void setNoiseMedianParameter(double noiseMedianParameter) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.noiseMedianParameter = noiseMedianParameter;
    }
    public double getNoiseMedianParameter() {
        return noiseMedianParameter;
    }
    private double noiseMedianParameter = 20;


    /**
     * Ustawia maksymalną dozwoloną zmienność częstotliwości dźwięku
     * (w półtonach).
     */
    public void setSemiMaxSeparation(double semiMaxSeparation) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.semiMaxSeparation = semiMaxSeparation;
    }
    private double semiMaxSeparation = 0.5;


    /**
     * Ustawia maksymalną dozwoloną czasową nieciągłość dźwięku (w sekundach).
     */
    public void setTimeMaxSeparation(double timeMaxSeparation) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.timeMaxSeparation = timeMaxSeparation;
    }
    private double timeMaxSeparation = 0.05;


    /**
     * Ustawia minimalną dozwolona długość trwania dźwięku (w sekundach).
     * Dźwięki krótsze będą pomijane.
     */
    public void setTimeMinDuration(double timeMinDuration) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.timeMinDuration = timeMinDuration;
    }
    private double timeMinDuration = 0.15;


    /**
     * Ustawia czułość wykrywania repetycji dźwięku.
     */
    public void setRepetitionSensitivity(double repetitionSensitivity) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.repetitionSensitivity = repetitionSensitivity;
    }
    private double repetitionSensitivity = 1.0;


    /**
     * Ustawia czułość detekcji tonów wyższych rzędów.
     */
    public void setOvertoneSensitivity(double overtoneSensitivity) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.overtoneSensitivity = overtoneSensitivity;
    }
    public double getOvertoneSensitivity() {
        return overtoneSensitivity;
    }
    private double overtoneSensitivity = 1.0;


    /**
     * Ustawia dopuszczalną przestrzeń głośności
     * między najgłośniejszymi a najcichszymi dźwiękami w utworze.
     */
    public void setMaxToMinRatio(double maxToMinRatio) {
        if (this.getState() != State.NEW) throw new IllegalThreadStateException();
        this.maxToMinRatio = maxToMinRatio;
    }
    private double maxToMinRatio = 50;


//----------------------------- U S T A W I E N I A  Z A A W A N S O W A N E ---

    /**
     * Współczynnik jakości transformacji constant-Q.
     */
    private final double cqQuality = 17.309933963814844981086787570342;

    /**
     * Szerokość przedziału czasu branego pod uwagę przy wykrywaniu repetycji
     * (w sekundach). Przynajmniej 10 x timeShiftApprox.
     */
    private final double repetitionAvgTimeSpan = 0.15;

    public int getThreadCount() {
        return threadCount;
    }
    private final int threadCount;


//---------------------------------- K O N S T R U K C J A  I  W E J Ś C I E ---

    private final Wave wave;
    private final AudioFormat format;
    private final int startOffset;
    private final int endOffset;

    private EfficientConstantQ cq;
    private double time;
    private LinkedList<BreakablePeakChain> currentChains;
    private LinkedList<BreakablePeakChain> allChains;
    private Voice voice;

    /**
     * Tworzy obiekt klasy Recognizer w celu wykonania rozpoznawania melodii
     * w danym obiekcie Wave. Po utworzeniu obiektu i ustaleniu żądanych opcji,
     * należy wywołać metodę <code>start()</code> w celu rozpoczęcia procesu
     * rozpoznawania.
     *
     * @param wave  strumień audio do rozpoznania
     * @see Wave
     */
    public Recognizer(Wave wave) {

        this(wave, 0, wave.getSampleCount());
    }

    /**
     * Tworzy obiekt klasy Recognizer w celu wykonania rozpoznawania melodii
     * na przedziale próbek od <code>startOffset</code> do <code>endOffset</code>
     * w danym obiekcie Wave. Po utworzeniu obiektu i ustaleniu żądanych opcji,
     * należy wywołać metodę <code>start()</code> w celu rozpoczęcia procesu
     * rozpoznawania.
     *
     * @param wave  strumień audio do rozpoznania
     * @param startOffset  początek przedziału próbek
     * @param endOffset  koniec przedziału próbek
     * @see Wave
     */
    public Recognizer(Wave wave, int startOffset, int endOffset) {

        super("RecognizingThread");
        this.allChains = new LinkedList<BreakablePeakChain>();
        this.currentChains = new LinkedList<BreakablePeakChain>();
        this.format = wave.getAudioFormat();
        this.listeners = new Vector<RecognitionListener>();
        this.wave = wave;

        if (startOffset > endOffset) {
            throw new java.lang.IllegalArgumentException("startOffset > endOffset");
        }
        this.startOffset = startOffset;
        this.endOffset = endOffset;

        int processors = Runtime.getRuntime().availableProcessors();
        this.threadCount = processors>1 ? processors+1 : 1;
        this.pickers = new PeakPicker[threadCount];
    }

//-------------------- S Y N C H R O N I Z A C J A  I  K O M U N I K A C J A ---

    private final PeakPicker[] pickers;
    private final Vector<RecognitionListener> listeners;
    private boolean started = false;
    private boolean cancelled = false;

    /**
     * Przerywa proces rozpoznawania. Obiekty nasłuchujące otrzymają stosowny
     * komunikat.
     */
    public void cancel() {
        cancelled = true;
        if (started) {
            for (PeakPicker pp : pickers) {
                pp.cancel();
            }
        }
    }

    private void notifyFinished() {
        final Recognizer reco = this;
        for (final RecognitionListener l : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    l.recognitionFinished(reco, false);
                }
            });
        }
    }

    private void notifyCancelled() {
        final Recognizer reco = this;
        for (final RecognitionListener l : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    l.recognitionFinished(reco, true);
                }
            });
        }
    }

    /** Dodaje obiekt <code>newListener</code> do listy obiektów, które będą
     * otrzymywać komunikaty o postępach (a w szczególności, o zakończeniu)
     * procesu rozpoznawania.
     * @param newListener  instancja klasy implementującej
     *                     interfejs RecognitionListener
     * @see  RecognitionListener
     */
    public void addRecognitionListener(RecognitionListener newListener) {
        listeners.add(newListener);
    }

    /**
     * Zwraca wynik działania procesu rozpoznawania.
     * @return  zbiór dźwięków znalezionych w rozpoznawanym utworze,
     * lub <code>null</code> jeśli rozpoznawanie jeszcze się nie zakończyło
     * @see Voice
     */
    public Voice getVoice() {
        return voice;
    }

//--------------------------- H E U R Y S T Y K A  R O Z P O Z N A W A N I A ---

    private void addToMatching(LocalPeak newPeak) {

        double semiNew = newPeak.getSemitone();
        Iterator<BreakablePeakChain> it = currentChains.iterator();
        while (it.hasNext()) {
            BreakablePeakChain chain = it.next();
            double semiOld = Sonst.semitoneFromFrequency(chain.getMeanFrequency());
            if (Math.abs(semiNew - semiOld) < semiMaxSeparation) {
                if (chain.wouldBreak(newPeak) && chain.getEndTime() - chain.getStartTime() > timeMinDuration) {
                    it.remove();
                    break;
                } else {
                    chain.addPeak(newPeak);
                    return;
                }
            }
        }
        BreakablePeakChain newChain = new BreakablePeakChain(repetitionAvgTimeSpan, 3.0/repetitionSensitivity);
        newChain.addPeak(newPeak);
        currentChains.add(newChain);
        allChains.add(newChain);
    }

    private boolean isRetired(PeakChain oldChain) {

        return time > oldChain.getEndTime() + timeMaxSeparation;
    }

    private void removeRetiredChains() {

        Iterator<? extends PeakChain> li = currentChains.iterator();
        while (li.hasNext()) {
            PeakChain next = li.next();
            if (isRetired(next)) {
                li.remove();
            }
        }
    }

    /**
     * Rozpoczyna proces rozpoznawania. W celu uruchomienia wątku rozpoznającego
     * należy wywołać metodę <code>start()</code>, która po stworzeniu wątku
     * wywoła niniejszą metodę <code>run()</code>. Po zakończeniu pracy wątku
     * wyniki dostępne będą przy pomocy metody <code>getVoice()</code>.
     */
    @Override
    public void run() {

        this.cq = new EfficientConstantQ(cqQuality, format.getSampleRate());
        cq.setFrequencyRange(freqLowBound, freqHighBound);
        cq.setWindowFunction(windowFunction);
        cq.prepare();

        PickerUpdater pupa = new PickerUpdater(this,listeners);
        if (cancelled) {
            notifyCancelled();
            return;
        }

        int samples = endOffset-startOffset;
        for (int i=0; i<threadCount; ++i) {
            pickers[i] = new PeakPicker(pupa,wave,startOffset+i*samples/threadCount,startOffset+(i+1)*samples/threadCount,i,cq.clone());
            new Thread(pickers[i]).start();
        }
        started = true;

        TreeMap<Double,ArrayList<LocalPeak>> allPeaks = pupa.waitForPickersToFinish();
        if (allPeaks == null) {
            notifyCancelled();
            return;
        }

        for (Entry<Double,ArrayList<LocalPeak>> entry : allPeaks.entrySet()) {
            time = entry.getKey();
            removeRetiredChains();

            for (LocalPeak plo : entry.getValue()) {
                    addToMatching(plo);
            }
        }

        currentChains.clear();
        for (BreakablePeakChain chain : allChains) {
            TreeMap<Integer,Integer> connections = new TreeMap<Integer,Integer>();
            PeakChain mostPossibleParent = null;

            int peaks = chain.getCount();
            for (LocalPeak peak : chain) if (peak.hasParent()) {
                PeakChain parent = peak.getParentPeak().getHomeChain();
                if (parent != null) {
                    int hash = parent.hashCode();
                    Integer count = connections.get(hash);
                    int c = (count == null) ? 1 : count+1;
                    if (2*c > peaks) {
                        mostPossibleParent = parent;
                        break;
                    }
                    connections.put(hash,c);
                }
            }
            
            if (mostPossibleParent == null) {
                currentChains.add(chain);
            } else {
                mostPossibleParent.addChild(chain);
            }
        }

        double maxVolume = 0;
        for (PeakChain pc : currentChains) {
            maxVolume = Math.max(maxVolume, pc.getVolume());
        }

        Voice results = new Voice();
        for (PeakChain chain : currentChains) {
            if (chain.getEndTime() - chain.getStartTime() >= timeMinDuration && chain.getVolume() * maxToMinRatio >= maxVolume) {
                Tone t = chain.makeTone(-startOffset/format.getSampleRate());
                results.addTone(t);
            }
        }
        currentChains.clear();
        allChains.clear();

        voice = results;
        notifyFinished();
    }
}