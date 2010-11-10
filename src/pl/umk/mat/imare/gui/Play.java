/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.umk.mat.imare.gui;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;
import pl.umk.mat.imare.io.Wave;

/**
 *
 * @author Tyczo
 */
public class Play implements Runnable {

    private boolean pause = false;
    private boolean playing = false;
    private LinkedList<PlayListener> listeners;
    private boolean stop = false;
    private SourceDataLine auline = null;
    private Wave wave = null;
    private int posStart, posEnd;
    private FloatControl volumeControl = null;

    @Override
    public void run() {

        AudioFormat fileFormat, bestFormat;

        DataLine.Info info;
        try {

            fileFormat = wave.getAudioFormat();
            bestFormat = new AudioFormat(fileFormat.getSampleRate(), 16, fileFormat.getChannels(), true, false);

            info = new DataLine.Info(SourceDataLine.class, bestFormat);

            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(bestFormat);
            volumeControl = (FloatControl) auline.getControl(FloatControl.Type.MASTER_GAIN);
            for (PlayListener l : listeners) {
                l.setVolume();
            }

        } catch (LineUnavailableException ex) {
            JOptionPane.showMessageDialog(null, "Urządzenie audio niedostępne.",
                    "Błąd", JOptionPane.ERROR_MESSAGE);
            notifyFinished();
            return;
        }

        // Tworzymy updater mowiacy o zmianie polozenia
        Runnable posUpdater = new Runnable() {

            @Override
            public void run() {
                int newPosition;
                while (!stop && (auline.getLongFramePosition() + posStart) < posEnd) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Play.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (pause) {
                        continue;
                    }

                    newPosition = (int) auline.getLongFramePosition() + posStart;
                    for (PlayListener l : listeners) {
                        l.positionChanged(newPosition);
                    }
                }
                notifyFinished();
            }
        };
        Thread updaterThread = new Thread(posUpdater);
        updaterThread.start();

        auline.start();
        // tworzymy sobie bufor do czytania pliku
        byte[] dataBuffer = new byte[8192];

        int channels = bestFormat.getChannels();
        int frameSize = 2*channels;
        int step = dataBuffer.length / frameSize;

        playing = true;

        try {

            int start = posStart;
            while (!stop && start < posEnd) {

                Thread.sleep(10);
                if (pause) {
                    continue;
                }

                int bufUsed;
                if (auline.available() >= (bufUsed = Math.min((posEnd-start)*frameSize, dataBuffer.length))) {

                    for (int bufPos = 0; bufPos < bufUsed; bufPos+=2) {
                        int data = wave.getSample((bufPos>>1)%channels, start+bufPos/frameSize)/(1<<(Wave.MAX_BIT-16)) + 0x8000;
                        dataBuffer[bufPos] = (byte)(data & 0xff);
                        dataBuffer[bufPos+1] = (byte)(((data & 0xff00) >> 8) - 128);
                    }

                    auline.write(dataBuffer, 0, bufUsed);
                    start += step;
                }
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Play.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            notifyFinished();
        }
    }

    public Play(Wave wave, int posStart, int posEnd) {
        this.wave = wave;
        this.posStart = posStart;
        this.posEnd = posEnd;

        this.listeners = new LinkedList<PlayListener>();

        Thread watek = new Thread(this);
        watek.start();

    }

    public void pause() {
        synchronized (this) {
            auline.stop();
        }
        pause = true;
    }

    public synchronized void play() {
        pause = false;
        auline.start();
    }

    public synchronized void stop() {
        stop = true;
        auline.stop();
        auline.flush();
    }

    public boolean isPlaying() {
        if (pause) {
            return false;
        } else {
            return playing;
        }
    }

    public void addPlayListener(PlayListener newListener) {
        listeners.add(newListener);
    }

    private void notifyFinished() {
        auline.drain();
        auline.close();
        playing = false;

        for (PlayListener l : listeners) {
            l.playingFinished();
        }
    }

    public void setVolume(float value) {
        // getMaximum() zwraca wartość większą od zera, ale ustawienie
        // gain > 0 powoduje spłaszczanie sygnału
        double newVolume = volumeControl.getMinimum()*(1.0-value);
        volumeControl.setValue((float)newVolume);
    }
}
