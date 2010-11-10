/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.DecimalFormat;
import javax.swing.JPanel;

/**
 *
 * @author morti
 */
public class TimeRullerPanel extends JPanel {

	private StavePanel stavePanel;
	private DecimalFormat secFormat = new DecimalFormat("00");

	public TimeRullerPanel(StavePanel stavePanel) {
		this.stavePanel = stavePanel;
		setBackground(Color.white);
		setForeground(Color.black);
		setPreferredSize(new Dimension(100, 18));

		stavePanel.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				setSize(e.getComponent().getSize().width, 18);
			}

			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentShown(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g);

		if(stavePanel == null || stavePanel.getStaveData() == null) return;

		int seconds = 0;
		int space = (int)(stavePanel.getStaveData().getTempo() *
				stavePanel.getPixelsPerWholeNote());

		for(int i=stavePanel.getStartPosition(); i < getWidth(); i += space, seconds++) {
			g.drawLine(i, getHeight(), i, getHeight() / 4);
			g.drawLine(i+space/2, getHeight(), i+space/2, getHeight() / 2);

			g.drawString("" + (int)(seconds/60) + ":" + secFormat.format(seconds%60), i+3, getHeight() - 3);
		}

		g.drawLine(stavePanel.getStartPosition(), getHeight()-1, getWidth(), getHeight()-1);
	}

}
