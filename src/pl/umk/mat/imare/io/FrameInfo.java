/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.io;

import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import javax.swing.JInternalFrame;

/**
 *
 * @author morti
 */
public class FrameInfo implements Serializable {
	private Rectangle bounds = null;
	private boolean visible = false;
	private boolean selected = false;

	public FrameInfo(JInternalFrame frame) {
		setBounds(frame.getBounds());
		setVisible(frame.isVisible());
		setSelected(frame.isSelected());
	}

	public void apply(JInternalFrame frame) {
		frame.setBounds(getBounds());
		frame.setVisible(isVisible());
//		try {
//			frame.setSelected(isSelected());
//		} catch (PropertyVetoException ex) {}
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}