/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.umk.mat.imare.io;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.LinkedList;
//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.UnsupportedAudioFileException;
//import pl.umk.mat.imare.exception.FileDoesNotExistException;
//import pl.umk.mat.imare.exception.MissingChannelException;
//import pl.umk.mat.imare.gui.ProgressFrame;
//import pl.umk.mat.imare.gui.related.ProgressListener;

/**
 * Klasa pozwalajaca uzyskac dostep do pliku WAVE.
 * @author morti
 */
public abstract class OldWave {
//
//    /** Informacje o pliku */
//    protected AudioFormat format = null;
//    /** Dane pliku wave */
//    protected int[][] data = null;
//    protected File file = null;
//    protected LinkedList<ProgressListener> listeners = new LinkedList<ProgressListener>();
//
//    /**
//     * Otwiera plik do odczytu
//     * @param file Plik do otwarcia
//     * @return Zwraca obiekt klasy Wave pozwalajacy odczytac plik. Moze zwrocic
//     * null jesli probka bedzie miala wiecej niz 2 kanaly lub bedzie miala
//     * liczbe bitow na probke inna niz 8 lub 16.
//     * @throws FileDoesNotExistException
//     * @throws UnsupportedAudioFileException
//     * @throws IOException
//     */
//    public static OldWave create(final File file, final ProgressListener progressListener) throws FileDoesNotExistException, UnsupportedAudioFileException, IOException {
//        if (!file.exists()) {
//            throw new FileDoesNotExistException();
//        }
//
//        String s = file.getName();
//
//        /**
//         * Jeśli otwierany plik to mp3 to
//         */
//        if (s.toLowerCase().endsWith(".mp3")) {
//            MP3Loader wave = null;
//            wave = new MP3Loader();
//            wave.addListener(progressListener);
//            wave.file = file;
//            wave.load2(new FileInputStream(file));
//            return wave;
//        }
//
//        /**
//         * W innym wypadku mamy zwykłego wavea
//         */
//        //System.out.println(file);
//        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
//        AudioFormat format = ais.getFormat();
//        OldWave wave = null;
//
//        int channels = format.getChannels();
//        int bitsPerSample = format.getSampleSizeInBits();
//
//        if (channels == 2) {
//            // stereo
//            if (bitsPerSample == 16) {
//                wave = new Wave16BitStereo();
//            } else if (bitsPerSample == 8) {
//                wave = new Wave8BitStereo();
//            }
//        } else if (channels == 1) {
//            // mono
//            if (bitsPerSample == 16) {
//                wave = new Wave16BitMono();
//            } else if (bitsPerSample == 8) {
//                wave = new Wave8BitMono();
//            }
//        }
//        wave.addListener(progressListener);
//        wave.file = file;
//        wave.format = format;
//        wave.load(ais);
//        return wave;
//
//    }
//
//    /**
//     * Otwiera plik do odczytu
//     * @param filePath Sciezka pliku do otwarcia
//     * @return Zwraca obiekt klasy Wave pozwalajacy odczytac plik.
//     * @throws FileDoesNotExistException
//     * @throws UnsupportedAudioFileException
//     * @throws IOException
//     */
//    public static OldWave create(String filePath, ProgressFrame progressFrame) throws FileDoesNotExistException, UnsupportedAudioFileException, IOException {
//        return create(new File(filePath), progressFrame);
//    }
//
//    /**
//     *
//     * @param format
//     * @param samples
//     * @return
//     */
//    public static OldWave fromSamples(AudioFormat format, int[] samples) {
//        OldWave w = new OldWave() {
//            @Override
//            protected void load(AudioInputStream ais) throws FileDoesNotExistException, UnsupportedAudioFileException, IOException {
//            }
//        };
//
//        w.data = new int[1][0];
//        w.data[0] = samples;
//        w.format = format;
//        return w;
//    }
//
//    /**
//     * Metoda ladujaca plik. Laduje CALY plik do pamieci.
//     * @param file Plik do zaladowania
//     * @throws FileDoesNotExistException
//     * @throws UnsupportedAudioFileException
//     * @throws IOException
//     */
//    protected abstract void load(AudioInputStream ais) throws FileDoesNotExistException, UnsupportedAudioFileException, IOException;
//
//    /**
//     *
//     * @return Zwraca informacje o pliku
//     */
//    public AudioFormat getAudioFormat() {
//        return format;
//    }
//
//    /**
//     * Odczytuje probki. <br>
//     * UWAGA: Ta metoda kopiuje probki przy uzyciu petli FOR poniewaz
//     * wewnetrznie probki przechowywane sa w tablicy typu INT. To bardzo powolne.
//     * Zalecane jest uzycie wersji z tablica typu INT.
//     * @param channelNumber Numer kanalu. Dla dzwieku stereo kanal lewy - 0, kanal prawy - 1.
//     * @param output Bufor na odczytane dane. Metoda bedzie probowala odczytac
//     * tyle sampli ile bufor pomiesci.
//     * @param offset Od ktorego sampla chcemy zaczac czytac
//     * @return Zwraca ilosc przeczytanych sampli
//     */
////	public int read(int channelNumber, double[] output, int offset) {
////
////		if(channelNumber >= format.getChannels())
////			throw new MissingChannelException(channelNumber);
////
////		int samplesRead;
//////		// czytamy sample dopoki nie skonczy sie jedna z tablic output lub data
////		for(samplesRead=0;
////			samplesRead < output.length && samplesRead+offset < data[channelNumber].length;
////			samplesRead++ ) {
////
////			output[samplesRead] = (double)data[channelNumber][samplesRead + offset];
////		}
////		return samplesRead;
////	}
//    /**
//     * Odczytuje probki.
//     * @param channelNumber Numer kanalu. Dla dzwieku stereo kanal lewy - 0, kanal prawy - 1.
//     * @param output Bufor na odczytane dane. Metoda bedzie probowala odczytac
//     * tyle sampli ile bufor pomiesci.
//     * @param offset Od ktorego sampla chcemy zaczac czytac
//     * @return Zwraca ilosc przeczytanych sampli
//     */
//    public int read(int channelNumber, int[] output, int offset) {
//        if (channelNumber >= format.getChannels()) {
//            throw new MissingChannelException(channelNumber);
//        }
//        int samplesRead = Math.min(output.length, data[channelNumber].length - offset);
//        if (samplesRead <= 0) return 0;
//
//        System.arraycopy(data[channelNumber], offset, output, 0, samplesRead);
////      for(int i=0; i<samplesRead; i++) output[i]=data[channelNumber][i+offset];
//        return samplesRead;
//    }
//
//    public int readMono(int[] output, int offset) {
//        int samplesRead = Math.min(output.length, data[0].length - offset);
//        if (samplesRead <= 0) return 0;
//
//        int channels = format.getChannels();
//        for (int i = 0; i < samplesRead; ++i) {
//            output[i] = 0;
//            for (int c = 0; c < channels; ++c) {
//                output[i] += data[c][i + offset];
//            }
//        }
//        return samplesRead;
//    }
//
//    /**
//     *
//     * @return Zwraca ilosc probek dzwieku.
//     */
//    public int getSampleCount() {
//        return data[0].length;
//    }
//
//    /**
//     * @return Zwraca wartosc probki w danym miejscu
//     */
//    public int getSample(int channel, int offset) {
//        return data[channel][offset];
//    }
//
//    public File getFile() {
//        return file;
//    }
//
//    public void addListener(ProgressListener listener) {
//        if (listener == null) {
//            return;
//        }
//        listeners.add(listener);
//    }
//
//    public void removeListener(ProgressListener listener) {
//        if (listener == null) {
//            return;
//        }
//        listeners.remove(listener);
//    }
//
//    protected void notifyLoadingStarted() {
//        for (ProgressListener l : listeners) {
//            l.jobStarted(this);
//        }
//    }
//
//    protected void notifyLoadingProgress(float progress) {
//        for (ProgressListener l : listeners) {
//            l.jobProgress(this, progress);
//        }
//    }
//
//    protected void notifyLoadingFinished() {
//        for (ProgressListener l : listeners) {
//            l.jobFinished(this);
//        }
//    }
}
