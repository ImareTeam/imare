package pl.umk.mat.imare.reco;

import java.io.Serializable;

/**
 * Symbolizuje określoną tonację (a dokładniej: parę tonacji równoległych)
 * charakteryzującą się określoną ilością i rodzajem znaków przykluczowych.
 * @author PieterEr
 */
public class Tonality implements Serializable {

    /**
     * Rodzaj znaku chromatycznego.
     */
    static public enum Sign {

        /**
         * Krzyżyk.
         */
        SHARP,

        /**
         * Bemol.
         */
        FLAT,

        /**
         * Kasownik.
         */
        NATURAL,

        /**
         * Brak znaku chromatycznego.
         */
        NONE
    }

    /**
     * Określona pozycja na pięciolinii, którą może zajmować nuta, określana
     * poprzez wysokość względem dźwięku c<sup>1</sup> oraz znak chromatyczny.
     */
    static public final class StaffPitch {

        /**
         * Właściwy znak chromatyczny.
         */
        public Sign sign;

        /**
         * Poziom na pięciolinii względem c<sup>1</sup>.
         */
        public int level;

        /**
         * Domyślny konstruktor, tworzący pozycję dźwięku c<sup>1</sup> bez
         * znaku chromatycznego.
         */
        public StaffPitch() {
            sign = Sign.NONE;
            level = 0;
        }
    }

    /**
     * Struktura przedstawiająca znak przykluczowy.
     */
    static public final class ClefKey {

        /**
         * Rodzaj znaku przykluczowego.
         */
        public Sign sign;

        /**
         * Odpowiedni poziom znaku na pięciolinii z kluczem basowym, liczony
         * względem pierwszej (najniższej) linii.
         */
        public int levelBass;

        /**
         * Odpowiedni poziom znaku na pięciolinii z kluczem wiolinowym, liczony
         * względem pierwszej (najniższej) linii.
         */
        public int levelTreble;

        /**
         * Tworzy odpowiedni znak przykluczowy na podstawie pozycji na
         * pięciolinii określanej strukturą StaffPitch.
         * @param sp  struktura określająca tworzony znak przykluczowy
         */
        public ClefKey(StaffPitch sp) {
            sign = sp.sign;
            int max;
            max = sign == Sign.SHARP ? 11 : 9;
            levelTreble = sp.level;
            while (levelTreble + 7 <= max) {
                levelTreble += 7;
            }
            levelBass = levelTreble - 2;
        }
    }

    // liczone od C
    private static final int[] zerolevels = {0, 2, 4, 5, 7, 9, 11};
    private int levels[];
    private int signs[];
    private static final char[] names = {'c', 'd', 'e', 'f', 'g', 'a', 'b'};

    static private Sign signFromInt(int s) {
        switch (s) {
            case -1:
                return Sign.FLAT;
            case 0:
                return Sign.NATURAL;
            case 1:
                return Sign.SHARP;
            default:
                return Sign.NONE;
        }
    }

    protected Tonality() {
        signs = new int[7];
        levels = new int[7];
        for (int i = 0; i < 7; ++i) {
            levels[i] = zerolevels[i];
        }
    }

    protected void setLevel(int rlevel, int rsemi) {
        signs[rlevel] = rsemi - zerolevels[rlevel];
        levels[rlevel] = rsemi;
    }

    protected void setSign(int rlevel, int sign) {
        signs[rlevel] = sign;
        levels[rlevel] = zerolevels[rlevel] + sign;
    }

    /**
     * Zwraca tablicę znaków przykluczowych odpowiednich dla tonacji.
     * @return  tablica znaków przykluczowych w odpowiedniej kolejności
     */
    public ClefKey[] clefSigns() {
        int size = 0;
        for (int l = 0; l < 7; ++l) {
            if (signs[l] != 0) {
                ++size;
            }
        }
        ClefKey signOrder[] = new ClefKey[size];

        int where = 0;
        for (int l : Sonst.SHARP_ORDER) {
            if (signs[l] > 0) {
                StaffPitch sp = new StaffPitch();
                sp.level = l;
                sp.sign = Sign.SHARP;
                signOrder[where++] = new ClefKey(sp);
            }
        }
        for (int l : Sonst.FLAT_ORDER) {
            if (signs[l] < 0) {
                StaffPitch sp = new StaffPitch();
                sp.level = l;
                sp.sign = Sign.FLAT;
                signOrder[where++] = new ClefKey(sp);
            }
        }
        return signOrder;
    }

    private int getProperNoteLevel(Note n) {
        int rp = n.getPitch() % 12;

        for (int i = 0; i < 7; ++i) {
            if (levels[i] == rp) {
                return i;
            }
        }

        int i = 0;
        while (i < 6 && zerolevels[i + 1] <= rp) {
            ++i;
        }
        return i;
    }

    /**
     * Zwraca pozycję nuty na pięciolinii z uwzględnieniem tonacji. Ewentualny
     * znak chromatyczny wynikowej struktury uwzględnia obecność odpowiednich
     * dla tonacji znaków przykluczowych.
     * @param n  nuta, której pozycja ma zostać ustalona
     * @return struktura StaffPitch opisująca pozycję danej nuty dla określonej
     * tonacji
     */
    public StaffPitch adapt(Note n) {

        int pitch = n.getPitch();
        int rl = getProperNoteLevel(n);
        int rp = pitch % 12;
        int ro = (pitch / 12) - 5;

        StaffPitch sp = new StaffPitch();
        sp.level = 7 * ro + rl;
        if (rp != levels[rl]) {
            sp.sign = signFromInt(rp-zerolevels[rl]);
        }
        return sp;
    }

    /**
     * Generuje nazwę nuty na pięciolinii w określonej tonacji, bez uwzględniania
     * oktawy (zawsze generowana jest nazwa taka jak w oktawie małej). Nazwy
     * dźwięków są w systemie anglosaskim (c,d,e,f,g,a,b), zaś w przypadku
     * dźwięków alterowanych, nazwa tworzona jest według notacji duńskiej
     * (niestandardowej), w której wartość podwyższona krzyżykiem posiada
     * przedrostek "-is", zaś wartość obniżona bemolem "-es", przy czym
     * samogłoski w tych końcówkach nigdy nie ulegają skróceniu.
     * @param n  nuta, której nazwa ma zostać wygenerowana
     * @return ciąg znaków będący nazwą nuty (np. "ees", "g", ais")
     */
    public String generateName(Note n) {

        int pitch = n.getPitch();
        int rl = getProperNoteLevel(n);
        int rp = pitch % 12;

        String name = String.valueOf(names[rl]);
        switch(rp - zerolevels[rl]) {
            case +1: name += "is"; break;
            case -1: name += "es"; break;
        }
        return name;
    }
}