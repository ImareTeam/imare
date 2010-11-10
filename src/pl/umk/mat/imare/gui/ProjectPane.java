/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import pl.umk.mat.imare.gui.related.ProjectPaneListener;
import pl.umk.mat.imare.gui.related.WizardFrameListener;

/**
 *
 * @author morti
 */
public class ProjectPane extends JPanel implements WizardFrameListener {
	public final static int OPEN_AUDIO_STEP = 1;
	public final static int RECOGNIZE_STEP = 2;
	public final static int NOTES_STEP = 3;
	public final static int SAVE_STEP = 4;

	private MainGUI mainWindow;
	private BufferedImage[] steps = new BufferedImage[4];
	private int totalImageWidth = 0;
	private int activeStep = 0;

	private LinkedList<ProjectPaneListener> listeners = new LinkedList<ProjectPaneListener>();
	private WizardFrame wizardFrame = null;

	public ProjectPane(MainGUI mainWindow) {
		this.mainWindow = mainWindow;
		addMouseListener(mouseListener);

		try {
			steps[0] = ImageIO.read(getClass().getResource("/pl/umk/mat/imare/gui/gfx/wizard/open.png"));
			steps[1] = ImageIO.read(getClass().getResource("/pl/umk/mat/imare/gui/gfx/wizard/recognize.png"));
			steps[2] = ImageIO.read(getClass().getResource("/pl/umk/mat/imare/gui/gfx/wizard/notes.png"));
			steps[3] = ImageIO.read(getClass().getResource("/pl/umk/mat/imare/gui/gfx/wizard/save.png"));
		} catch (IOException ex) {
			Logger.getLogger(WizardFrame.class.getName()).log(Level.SEVERE, null, ex);
		}

		totalImageWidth = 0;
		for(int i=0; i < steps.length; i++)
			totalImageWidth += steps[i].getWidth();		
	}

	/**
	 * Rysuje panel.
	 * @param g
	 */
	@Override
	public void paint(Graphics g) {
		float[] scales = { 1, 1, 1, 0.3f };
		float[] offsets = { 0, 0, 0, 0 };		
		
		g.setColor(getBackground());
		int partSize = (getWidth() / 4);		
		for(int i=0; i < 4; i++) {
			int x = partSize*i + (partSize - steps[i].getWidth())/2;

			if(i <= activeStep) g.setColor(new Color(0x008cb0e3));
			else g.setColor(Color.lightGray);
			g.fillRect(partSize*i, 0, partSize, getHeight());

			for(int j=0; j < 4; j++)
				scales[j] = (i<=activeStep ? 1 : (j<3 ? 0 : 0.4f));

			BufferedImageOp alphaOp = new RescaleOp(scales, offsets, null);
			((Graphics2D)g).drawImage(steps[i], alphaOp,x, 0);
			x += steps[i].getWidth();
		}

		paintBorder(g);
	}

	/**
	 * Dodaje listenera, który będzie otrzymywał informacje od panelu
	 * @param listener
	 */
	public void addListener(ProjectPaneListener listener) {
		listeners.add(listener);
	}

	/**
	 * Usuwa listenera.
	 * @param listener
	 */
	public void removeListener(ProjectPaneListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Do użycia wewnętrznego.
	 * Informuje o kliknięciu któregoś z kroków.
	 * @param step
	 */
	private void notifyStepClicked(int step) {
		for(ProjectPaneListener l : listeners)
			l.stepClicked(step);
	}

	/**
	 * Nasłuchuje zdarzeń myszki i generuje zdarzenie kliknięcia
	 * jakiegoś kroku.
	 */
	private MouseListener mouseListener = new MouseListener() {

		@Override
		public void mouseClicked(MouseEvent e) {
			Point p = e.getPoint();
			int partSize = getWidth() / 4;
			int step = (int)p.getX() / partSize;
			if(step <= activeStep)
				notifyStepClicked(step);
		}

		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
	};

//	public int getActiveStep() {
//		return activeStep;
//	}
//
//	public void setActiveStep(int activeStep) {
//		this.activeStep = activeStep;
//	}

	@Override
	public void stepChanged(int newStep) {
//		updateState();
//		repaint();
		activeStep = Math.max(newStep-1, activeStep);
		repaint();
	}

	private void updateState() {
		activeStep = 0;
		if(mainWindow.getOpenedWave() == null) return;
		activeStep++;
		if(mainWindow.getNotes() == null) return;
		activeStep++;
	}

	@Override
	public void wizardFrameClosed() {
		setVisible(false);
	}

	public void setWizardFrame(WizardFrame wizardFrame) {
		if(wizardFrame != null) wizardFrame.removeListener(this);
		this.wizardFrame = wizardFrame;
		wizardFrame.addListener(this);
		addListener(wizardFrame);

		updateState();
	}
}
