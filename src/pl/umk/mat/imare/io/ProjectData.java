/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.io;

import java.io.Serializable;

/**
 *
 * @author morti
 */
public class ProjectData implements Serializable {
	private FrameInfo waveFrame = null;
	private FrameInfo noteFrame = null;
	private FrameInfo recoFrame = null;

	public FrameInfo getNoteFrame() {
		return noteFrame;
	}

	public void setNoteFrame(FrameInfo noteFrame) {
		this.noteFrame = noteFrame;
	}

	public FrameInfo getWaveFrame() {
		return waveFrame;
	}

	public void setWaveFrame(FrameInfo waveFrame) {
		this.waveFrame = waveFrame;
	}

	public FrameInfo getRecoFrame() {
		return recoFrame;
	}

	public void setRecoFrame(FrameInfo recoFrame) {
		this.recoFrame = recoFrame;
	}
}
