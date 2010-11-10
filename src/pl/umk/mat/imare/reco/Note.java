package pl.umk.mat.imare.reco;

import java.io.Serializable;

/**
 * Reprezentuje pojedynczą nutę w postaci niezależnej od tonacji.
 * @author morti
 */
public class Note implements Serializable {

    /**
     * Standardowy konstruktor klasy, ustala wartości domyślne.
     */
    public Note() {
        this.type = 0;
        this.dots = 0;
        this.pitch = Sonst.STANDARD_PITCH;
        this.offset = 0;
        this.volume = 127;
    }

    /**
     * Konstruktor klasy, ustawia żądane właściwości nuty.
     * @param type  kategoria długości nuty (4=szesnastka, 3=ósemka, 2=ćwierćnuta, itp.)
     * @param dots  ilość kropek występujących przy nucie
     * @param pitch  wysokość dźwięku w półtonach (według skali MIDI)
     * @param offset  pozycja czasowa nuty względem początku utworu (w całych nutach)
     * @param volume  głośność nuty (według skali MIDI)
     */
    public Note(byte type, byte dots, byte pitch, float offset, byte volume) {
        this.type = type;
        this.dots = dots;
        this.pitch = pitch;
        this.offset = offset;
        this.volume = volume;
    }

    private byte dots;
    /**
     * Zwraca ilość kropek występujących przy nucie.
     */
    public byte getDots() {
        return dots;
    }
    /**
     * Ustawia ilość kropek występujących przy nucie.
     */
    public void setDots(byte dots) {
        this.dots = dots;
    }

    private float offset;
    /**
     * Zwraca pozycję czasową nuty względem początku utworu
     * (w całych nutach).
     */
    public float getOffset() {
        return offset;
    }
    /**
     * Ustawia pozycję czasową nuty względem początku utworu
     * (w całych nutach).
     */
    public void setOffset(float offset) {
        this.offset = offset;
    }

    private byte pitch;
    /**
     * Zwraca wysokość dźwięku w półtonach (według skali MIDI).
     */
    public byte getPitch() {
        return pitch;
    }
    /**
     * Ustawia wysokość dźwięku w półtonach (według skali MIDI).
     */
    public void setPitch(byte pitch) {
        this.pitch = pitch;
    }

    private byte type;
    /**
     * Zwraca kategorię długości nuty.
     */
    public byte getType() {
        return type;
    }
    /**
     * Ustawia kategorię długości nuty.
     */
    public void setType(byte type) {
        this.type = type;
    }

    private byte volume;
    /**
     * Zwraca głośność nuty (według skali MIDI).
     */
    public byte getVolume() {
        return volume;
    }
    /**
     * Ustawia głośność nuty (według skali MIDI).
     */
    public void setVolume(byte volume) {
        this.volume = volume;
    }

    /**
     * Wyznacza czas trwania nuty w całych nutach.
     * @return długość nuty w całych nutach
     */
    public float getDuration() {
        float factor = 1;
        float q = 1;
        for (int d = 0; d < dots; ++d) {
            factor += (q *= 0.5);
        }
        return Math.scalb(factor, -type);
    }
}
