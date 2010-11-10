package pl.umk.mat.imare.reco;

import pl.umk.mat.imare.reco.internal.AlgebraicComplex;

/**
 * Klasa służąca do obliczania transformaty Constant Q przy wykorzystaniu transformacji FFT.
 * @author PieterEr
 */
public class EfficientConstantQ implements Cloneable {

    /**
     * Ilość binów (komórek) przypadająca na każdą oktawę transformaty.
     */
    static public final int BINS_PER_OCTAVE = 24;

    private final double Q;
    private final double samplingRate;
    private double minFrequency;
    private double maxFrequency;
    private WindowFunction wf = null;

    private boolean updated;
    
    private int fftInputSize, fftOutputSize;
    private SequentialFFT fft;
    private int binCount;
    private double[][] Kc;
    private int[] KcFirst;
    private int[] KcLast;

    /**
     * Tworzy obiekt klasy ConstantQ gotowy do przeprowadzenia obliczeń.
     * @param Q  parametr jakości przekształcenia Constant Q
     * @param samplingRate  częstotliwość próbkowania dźwięku, na którym
     * stosowana będzie transformacja
     */
    public EfficientConstantQ(double Q, double samplingRate) {
        this.Q = Q;
        this.samplingRate = samplingRate;

        this.minFrequency = 100;
        this.maxFrequency = 10000;
        this.updated = false;
    }

    /**
     * Tworzy nowy obiekt klasy EfficientConstantQ, będący wierną
     * (choć niezależną) kopią obiektu w bieżącym stanie.
     * @return  utworzony obiekt klasy EfficientConstantQ
     */
    @Override
    public EfficientConstantQ clone() {

        prepare();

        EfficientConstantQ cq2 = new EfficientConstantQ(Q,samplingRate);
        cq2.minFrequency = minFrequency;
        cq2.maxFrequency = maxFrequency;
        cq2.wf = wf;

        cq2.updated = updated;

        cq2.fftInputSize = fftInputSize;
        cq2.fftOutputSize = fftOutputSize;
        cq2.fft = new SequentialFFT(fftInputSize);
        
        cq2.binCount = binCount;

        cq2.Kc = new double[binCount][fftOutputSize];
        for (int b=0; b<binCount; ++b) {
            System.arraycopy(Kc[b], 0, cq2.Kc[b], 0, fftOutputSize);
        }
        cq2.KcFirst = new int[binCount];
        System.arraycopy(KcFirst, 0, cq2.KcFirst, 0, binCount);
        cq2.KcLast = new int[binCount];
        System.arraycopy(KcLast, 0, cq2.KcLast, 0, binCount);

        return cq2;
    }

    /**
     * Metoda wywoływana wewnętrznie przy wywoływaniu metody process(...).
     * Oblicza przebiegi funkcji (tzw. kernel functions) wymaganych do przetworzenia
     * widma FFT w widmo Constant Q, a ponadto tworzy obiekt klasy FFT o odpowiedniej
     * długości bufora wejściowego.
     */
    private void updateInternals() {
        double N = Q * samplingRate / minFrequency;
        fftInputSize = 1 << (int) Math.ceil(Sonst.log2(N));
        fft = new SequentialFFT(fftInputSize);
        fftOutputSize = fftInputSize >> 1;

        binCount = (int) Math.floor(Sonst.log2(maxFrequency / minFrequency) * BINS_PER_OCTAVE) + 1;
        Kc = new double[binCount][fftOutputSize];
        KcFirst = new int[binCount];
        KcLast = new int[binCount];

        AlgebraicComplex[] KK = new AlgebraicComplex[fftInputSize];
        double[] out = new double[fftOutputSize];

        for (int kcq = 0; kcq < binCount; ++kcq) {
            double freq = getBinFrequency(kcq);
            N = Q * samplingRate / freq;
            for (int n = 0; n < fftInputSize; ++n) {
                double xWF = (n-fftOutputSize)/N + 0.5;
                if (xWF>=0 && xWF<=1) {
                    double w = (wf==null ? 1.0 : wf.value(xWF)) / N;
                    double phi = (2.0*Math.PI)*(freq/samplingRate)*(n-fftOutputSize);
                    KK[n] = new AlgebraicComplex(w*Math.cos(phi), w*Math.sin(phi));
                } else {
                    KK[n] = new AlgebraicComplex(0,0);
                }
            }
            fft.process(KK);
            AlgebraicComplex[] outCplx = fft.getDataArray();
            for (int k = 0; k < fftOutputSize; ++k) {
                out[k] = outCplx[k].re;
            }

            int kMax = 0;
            for (int k = 1; k < fftOutputSize; ++k) {
                if (out[k] > out[kMax]) kMax = k;
            }

            int leftMost=kMax, rightMost=kMax;
            while (leftMost>0 && out[leftMost-1]<out[leftMost]) --leftMost;
            while (rightMost+1<fftOutputSize && out[rightMost+1]<out[rightMost]) ++rightMost;
            KcFirst[kcq] = leftMost;
            KcLast[kcq] = rightMost;

            for (int k = leftMost ; k <= rightMost; ++k) {
                Kc[kcq][k] = out[k];
            }
        }
        updated = true;
    }

    /**
     * Ustawia minimalną i maksymalną częstotliwość, zmniejszając ją odpowiednio
     * jeśli przekracza częstotliwość Nyquista. 
     * @param min  dolna zadana granica częstotliwości w hercach
     * @param max  górna zadana granica częstotliwości w hercach
     * @throws Error  jeżeli zadany przedział jest pusty
     */
    public void setFrequencyRange(double min, double max) {
        if (min < 0 || max < 0) {
            throw new Error("frequency is less than 0");
        }
        min = Math.min(min, 0.5 * samplingRate);
        max = Math.min(max, 0.5 * samplingRate);
        if (min > max) {
            throw new Error("frequency range is empty");
        }
        this.minFrequency = min;
        this.maxFrequency = max;

        updated = false;
    }

    /**
     * Ustawia zadane okno czasowe (ang. window function) jako obowiązujące
     * dla transformacji.
     * @param nwf  zadane okno czasowe (obiekt dowolnej klasy implementującej
     * interfejs WindowFunction)
     */
    public void setWindowFunction(WindowFunction nwf) {
        wf = nwf;
        updated = false;
    }

    public double getMinFrequency() {
        return minFrequency;
    }

    public double getMaxFrequency() {
        return maxFrequency;
    }

    /**
     * Zwraca dokładną częstotliwość odpowiadającą kanałowi o zadanym numerze.
     * @param bin  zadany numer kanału, kanał o najniższej częstotliwości ma
     * numer 0
     * @return  częstotliwość w hercach
     */
    public double getBinFrequency(int bin) {
        return minFrequency * Math.pow(2, (double) bin / (double) BINS_PER_OCTAVE);
    }

    /**
     * Zwraca żądany rozmiar bufora danych wejściowych.
     * @return długość tablicy bufora danych wejściowych
     */
    public int getInputSize() {
        prepare();
        return fftInputSize;
    }

    /**
     * Zwraca rozmiar bufora danych wyjściowych.
     * @return rozmiar bufora danych wyjściowych (ilość kanałów widma)
     */
    public int getOutputSize() {
        prepare();
        return binCount;
    }

    /**
     * Jeśli transformacja nie była jeszcze wykonywana, lub lub od ostatniego
     * jej wykonania zmienione zostały żądane zakresy częstotliwości lub okno
     * czasowe, wywołana zostanie wewnętrznie metoda <code>updateInternals()</code>.
     */
    public void prepare() {
        if (!updated) {
            updateInternals();
        }
    }
    
    /**
     * Główna funkcja klasy EfficientConstantQ, przetwarza tablicę wejściową
     * <code>input</code>, zaś
     * wynik (widmo dźwięku) zapisuje do tablicy <code>output</code>.
     * Wywołuje uprzednio metodę <code>prepare()</code>.
     * @param input  tablica danych wejściowych, powinna mieć rozmiar dokładnie
     * taki jaki zwracany jest przez metodę getInputSize()
     * @param output  tablica danych wynikowych, powinna mieć rozmiar
     * co najmniej taki jaki zwracany jest przez metodę getOutputSize()
     */
    public void process(int[] input, Complex[] output) {
        prepare();
        fft.process(input);
        AlgebraicComplex[] X = fft.getDataArray();
        for (int kcq = 0; kcq < binCount; ++kcq) {
            double re=0, im=0;
            for (int k = KcFirst[kcq]; k <= KcLast[kcq]; ++k) {
                re += X[k].re * Kc[kcq][k];
                im += X[k].im * Kc[kcq][k];
            }
            output[kcq] = new Complex(re, im);
        }
    }
}