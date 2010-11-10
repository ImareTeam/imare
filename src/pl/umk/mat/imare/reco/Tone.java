package pl.umk.mat.imare.reco;

/**
 * Reprezentuje pojedynczy, ograniczony w czasie dźwięk.
 * @author PieterEr
 */
public final class Tone {

    /**
     * Minimalna głośność dźwięku w skali MIDI.
     */
    private static final double BASE_MIDI_VOLUME = 32.0;

    private final float frequency;
    private final float timeOffset;
    private final float timeDuration;
    private final float volume;

    /**
     * Tworzy nowy dźwięk o zadanych parametrach.
     * @param frequency  częstotliwość w hercach
     * @param volume  głośność w skali MIDI
     * @param timeOffset  położenie początku dźwięku w sekundach
     * @param timeDuration  czas trwania dźwięku w sekundach
     */
    public Tone(float frequency, float volume, float timeOffset, float timeDuration) {
        this.frequency = frequency;
        this.timeOffset = timeOffset;
        this.timeDuration = timeDuration;
        this.volume = volume;
    }

    /**
     * Zwraca częstotliwość dźwięku w hercach.
     */
    public float getFrequency() {
        return this.frequency;
    }

    /**
     * Zwraca położenie początku dźwięku w sekundach.
     */
    public float getTimeOffset() {
        return this.timeOffset;
    }

    /**
     * Zwraca czas trwania dźwięku w sekundach.
     */
    public float getTimeDuration() {
        return this.timeDuration;
    }

    /**
     * Zwraca głośność dźwięku w skali MIDI.
     */
    public float getVolume() {
        return this.volume;
    }

    /**
     * Generuje informacje o dźwięku w postaci tekstowej.
     * @return  łańcuch znaków, np. "440 Hz, -10 dB (5.3 s)"
     */
    @Override
    public String toString() {
        return String.format("%f Hz, %f dB (%f s)", frequency, 10.0 * Math.log10(volume), timeDuration);
    }

    /**
     * Generuje element zapisu nutowego.
     * @param tempo  zadane tempo utworu w całych nutach na sekundę
     * @param maxVolume  punkt zerowy skali głośności (0 dB)
     * @return  równoważny dźwiękowi element zapisu nutowego
     */
    public Note makeNote(double tempo, double maxVolume) {
        double dur = timeDuration;

        long logDur = -Math.round(Sonst.log2(dur*tempo));
        if (logDur < 0) logDur = 0;
        if (logDur > 4) logDur = 4;

        long semi = Math.round(Sonst.semitoneFromFrequency(frequency));
        if (semi<=0 || semi>127) return null;

        float off = (float)(timeOffset * tempo);
        byte vol = (byte)Math.round((127.0-BASE_MIDI_VOLUME) * volume / maxVolume + BASE_MIDI_VOLUME);

        return new Note((byte)logDur, (byte)0, (byte)semi, off, vol);
    }
}
