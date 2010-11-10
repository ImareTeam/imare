/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.reco.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import pl.umk.mat.imare.reco.Tone;

/**
 *
 * @author pieterer
 */
public class PeakChain implements Iterable<LocalPeak> {

    private ArrayList<LocalPeak> peaks;
    private double maxPeakVolume;
    private double frequencySum;
    private double bonusVolume;

    private PeakChain parent;

    public PeakChain() {
        peaks = new ArrayList<LocalPeak>();
        maxPeakVolume = 0;
        frequencySum = 0;
        bonusVolume = 0;
    }

    public int getCount() {
        return peaks.size();
    }

    public void addPeak(LocalPeak t) {
        if (t.getTime() >= getEndTime()) {
            peaks.add(t);
            t.setHomeChain(this);
            maxPeakVolume = Math.max(maxPeakVolume, t.getVolume());
            frequencySum += t.getFrequency();
        } else {
            throw new IllegalArgumentException("peaks must be added in chronological order");
        }
    }

    public void addChild(PeakChain child) {

        if (child.parent != null) {
            throw new IllegalStateException("child has been already attached to a parent");
        }
        child.parent = this;
        double delta = child.getVolume();
        child.bonusVolume = 0;
        if (delta > 0) gainBonus(delta);
    }

    private void gainBonus(double bonus) {
        if (parent == null) {
            bonusVolume += bonus;
        } else {
            parent.gainBonus(bonus);
        }
    }

    public double getStartTime() {
        return peaks.isEmpty() ? Double.POSITIVE_INFINITY : peaks.get(0).getTime();
    }

    public double getEndTime() {
        return peaks.isEmpty() ? Double.NEGATIVE_INFINITY : getLastPeak().getTime();
    }

    public LocalPeak getLastPeak() {
        return peaks.get(peaks.size()-1);
    }

    public double getMeanFrequency() {
        return frequencySum / peaks.size();
    }

    public double getMaxPeakVolume() {
        return maxPeakVolume;
    }

    public double getVolume() {
        return maxPeakVolume + bonusVolume;
    }

    @Override
    public Iterator<LocalPeak> iterator() {
        return Collections.unmodifiableCollection(peaks).iterator();
    }

    public Tone makeTone(float timeOffset) {
        double timeDuration = getEndTime() - getStartTime();
        return new Tone((float)getMeanFrequency(), (float)getVolume(), timeOffset+(float)getStartTime(), (float)timeDuration);
    }
}
