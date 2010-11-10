/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.umk.mat.imare.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import pl.umk.mat.imare.exception.FileDoesNotExistException;
import pl.umk.mat.imare.exception.MissingChannelException;
import pl.umk.mat.imare.gui.ProgressFrame;
import pl.umk.mat.imare.gui.related.ProgressListener;

/**
 * Klasa pozwalajaca uzyskac dostep do pliku WAVE.
 * @author morti
 */
public class Wave {

    /** Informacje o pliku */
    protected AudioFormat format = null;
    /** Dane pliku wave */
    protected short[][] data = null;
    protected File file = null;
    protected LinkedList<ProgressListener> listeners = new LinkedList<ProgressListener>();

    public static final int MAX_BIT = 16;
    public static final int MAX_VAL = 32768;

    /**
     * Otwiera plik do odczytu
     * @param file Plik do otwarcia
     * @return Zwraca obiekt klasy Wave pozwalajacy odczytac plik.
     * @throws FileDoesNotExistException
     * @throws UnsupportedAudioFileException
     * @throws IOException
     */
    public static Wave create(final File file, final ProgressListener progressListener) throws FileDoesNotExistException, UnsupportedAudioFileException, IOException {
        if (!file.exists()) {
            throw new FileDoesNotExistException();
        }

        String s = file.getName();
        if (s.toLowerCase().endsWith(".mp3")) {
            /* otwierany plik to mp3 */
            MP3Loader wave = new MP3Loader();
            wave.addListener(progressListener);
            wave.file = file;
            wave.load2(new FileInputStream(file));
            return wave;
        } else {
            /* mamy zwykłego wave'a */
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            AudioFormat format = ais.getFormat();

            AudioFormat.Encoding enc = format.getEncoding();
            if (((format.getSampleSizeInBits() & 7) != 0)
                    || !(enc.equals(AudioFormat.Encoding.PCM_SIGNED)
                    || enc.equals(AudioFormat.Encoding.PCM_UNSIGNED))) {
                ais.close();
                throw new UnsupportedAudioFileException();
            }

            Wave wave = new Wave();
            wave.addListener(progressListener);
            wave.file = file;
            wave.format = format;
            wave.load(ais);
            return wave;
        }
    }

    /**
     * Otwiera plik do odczytu
     * @param filePath Sciezka pliku do otwarcia
     * @return Zwraca obiekt klasy Wave pozwalajacy odczytac plik.
     * @throws FileDoesNotExistException
     * @throws UnsupportedAudioFileException
     * @throws IOException
     */
    public static Wave create(String filePath, ProgressFrame progressFrame) throws FileDoesNotExistException, UnsupportedAudioFileException, IOException {
        return create(new File(filePath), progressFrame);
    }

    /**
     * Metoda ladujaca plik. Laduje CALY plik do pamieci.
     * @param ais  strumien powiązany z plikiem do załadowania
     * @throws FileDoesNotExistException
     * @throws UnsupportedAudioFileException
     * @throws IOException
     */
    protected void load(AudioInputStream ais) throws FileDoesNotExistException, UnsupportedAudioFileException, IOException {
        int channels = format.getChannels();
        int samples = (int)ais.getFrameLength();
        int bytesPerSample = format.getSampleSizeInBits() / 8;
        boolean isUnsigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED);

        final int sbHigh, sbLow, sbThird;
        boolean readBoth = bytesPerSample >= 2;
        if (format.isBigEndian()) {
            sbHigh = 0;
            sbLow = 1;
            sbThird = bytesPerSample>=3 ? 2 : -1;
        } else {
            sbHigh = bytesPerSample - 1;
            sbLow = bytesPerSample - 2;
            sbThird = bytesPerSample - 3;
        }

        data = new short[channels][samples];
        byte[] buffer = new byte[524288]; // bufor - 512 kb
        int bytesRead, s = 0;

        notifyLoadingStarted();
        try {
            while (ais.available() > 0) {
                bytesRead = ais.read(buffer);

                for (int i = 0; i < bytesRead; ++s) {
                    for (int c=0; c<channels; ++c) {
                        int result = buffer[i+sbHigh];
                        if (isUnsigned) result = (result<0 ? result+0x80 : result-0x80);
                        result *= 256;
                        if (readBoth) {
                            int b = buffer[i+sbLow];
                            if (sbThird>=0 && b!=-1 && buffer[i+sbThird]<0) ++b;
                            result += (b<0 ? b+0x100 : b);
                        }
                        i += bytesPerSample;
                        data[c][s] = (short)result;
                    }
                }
                notifyLoadingProgress(s / samples);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            notifyLoadingFinished();
        }
    }

    /**
     *
     * @return Zwraca informacje o pliku
     */
    public AudioFormat getAudioFormat() {
        return format;
    }

    /**
     * Odczytuje probki.
     * @param channelNumber Numer kanalu. Dla dzwieku stereo kanal lewy - 0, kanal prawy - 1.
     * @param output Bufor na odczytane dane. Metoda bedzie probowala odczytac
     * tyle sampli ile bufor pomiesci.
     * @param offset Od ktorego sampla chcemy zaczac czytac
     * @return Zwraca ilosc przeczytanych sampli
     */
    public int read(int channelNumber, int[] output, int offset) {
        if (channelNumber >= format.getChannels()) {
            throw new MissingChannelException(channelNumber);
        }
        int samplesRead = Math.min(output.length, data[channelNumber].length - offset);
        if (samplesRead <= 0) {
            return 0;
        }

        for (int i = 0; i < samplesRead; ++i) {
            output[i] = data[channelNumber][i + offset];
        }
        return samplesRead;
    }

    /**
     * Odczytuje sumę jednoczesnych próbek ze wszystkich kanałów.
     * @param output Bufor na odczytane dane. Metoda bedzie probowala odczytac
     * tyle sampli ile bufor pomiesci.
     * @param offset Od ktorego sampla chcemy zaczac czytac
     * @return Zwraca ilosc przeczytanych sampli
     */
    public int readMono(int[] output, int offset) {
        int samplesRead = Math.min(output.length, data[0].length - offset);
        if (samplesRead <= 0) {
            return 0;
        }

        int channels = format.getChannels();
        for (int i = 0; i < samplesRead; ++i) {
            output[i] = 0;
            for (int c = 0; c < channels; ++c) {
                output[i] += data[c][i + offset];
            }
        }
        return samplesRead;
    }

    /**
     *
     * @return Zwraca ilosc probek dzwieku.
     */
    public int getSampleCount() {
        return data[0].length;
    }

    /**
     * @return Zwraca wartosc probki w danym miejscu
     */
    public int getSample(int channel, int offset) {
        return data[channel][offset];
    }

    public File getFile() {
        return file;
    }

    public void addListener(ProgressListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    public void removeListener(ProgressListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    protected void notifyLoadingStarted() {
        for (ProgressListener l : listeners) {
            l.jobStarted(this);
        }
    }

    protected void notifyLoadingProgress(float progress) {
        for (ProgressListener l : listeners) {
            l.jobProgress(this, progress);
        }
    }

    protected void notifyLoadingFinished() {
        for (ProgressListener l : listeners) {
            l.jobFinished(this);
        }
    }
}
