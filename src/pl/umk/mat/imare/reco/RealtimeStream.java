package pl.umk.mat.imare.reco;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * Obiekt pośredniczący w odczytywaniu strumienia danych z urządzenia
 * wejściowego i "inteligentnym" wypełnianiu bufora.
 * @author PieterEr
 */
public final class RealtimeStream {

    /**
     * Kopiuje zakres <code>byteOffset</code>...<code>byteOffset+byteCount-1</code>
     * bajtów z tablicy <code>aByte</code> i zapisuje do komórek
     * <code>intOffset</code>...<code>intOffset+(byteCount/2)-1</code>
     * tablicy <code>aInt</code>, traktując
     * strumień bajtów jako ciąg dwubajtowych wartości little-endian ze znakiem.
     *
     * @param aByte  źródłowa tablica bajtów
     * @param byteOffset  początek zakresu tablicy bajtów
     * @param aInt  docelowa tablica liczb całkowitych
     * @param intOffset  początek zakresu tablicy liczb całkowitych
     * @param byteCount  ilość bajtów do skopiowania
     * @return ilość skopiowanych liczb całkowitych
     */
    private static int signedLittleEndian16BitByteToIntArrayTransformCopy(byte[] aByte, int byteOffset, int[] aInt, int intOffset, int byteCount) {
        int bytePos = byteOffset;
        int intCount = byteCount / 2;
        for (int i = 0; i < intCount; ++i) {
            int lo = aByte[bytePos++];
            aInt[intOffset + i] = aByte[bytePos++] * 256 + (lo < 0 ? lo + 0x100 : lo);
        }
        return intCount;
    }

    private byte[] byteBuffer;
    private final TargetDataLine tdl;

    /**
     * Tworzy nowy obiekt klasy RealtimeStream, charakteryzujący się jednokanałowym
     * strumieniem (mono) o 16 bitach na próbkę (ze znakiem) i zadanej
     * częstotliwości próbkowania.
     *
     * @param samplingRate  zadana częstotliwość próbkowania w hercach
     * @throws LineUnavailableException  nie udało się uzyskać dostępu do urządzenia
     */
    public RealtimeStream(float samplingRate) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(samplingRate, 16, 1, true, false);
        byteBuffer = new byte[0];
        tdl = AudioSystem.getTargetDataLine(format);
        tdl.open();
    }

    /**
     * Zwraca format strumienia.
     * @return  obiekt klasy AudioFormat opisujący format strumienia
     * @see AudioFormat
     */
    public AudioFormat getFormat() {
        return tdl.getFormat();
    }

    /**
     * Rozpoczyna odbiór z urządzenia.
     */
    public void start() {
        tdl.start();
    }

    /**
     * Zatrzymuje odbiór z urządzenia i opróżnia bufor. Możliwe jest późniejsze
     * wznowienie odbioru metodą <code>start()</code>.
     */
    public void stop() {
        tdl.stop();
        tdl.drain();
    }

    /**
     * Uaktualnia bufor w "inteligentny" sposób, odczytując żądaną liczbę
     * wartości i zapisując je na koniec bufora, przenosząc jego poprzednie dane
     * o tyleż samo pozycji w lewo.
     * @param buffer  tablica liczb całkowitych do uaktualnienia
     * @param count  żądana ilość odczytanych bajtów
     */
    public void updateBuffer(int[] buffer, int count) {
        int byteCount = count*2;
        if (byteBuffer.length < byteCount) {
            byteBuffer = new byte[byteCount];
        }
        tdl.read(byteBuffer, 0, byteCount);

        int pos = 0;
        int offset = buffer.length - count;
        while (pos < offset) {
            buffer[pos] = buffer[pos + count];
            ++pos;
        }
        signedLittleEndian16BitByteToIntArrayTransformCopy(byteBuffer, 0, buffer, pos, byteCount);
    }


    /**
     * Metoda wywoływana przez odśmiecacza przy usuwaniu obiektu RealtimeStream.
     */
    @Override
    public void finalize() {
        tdl.close();
    }
}
