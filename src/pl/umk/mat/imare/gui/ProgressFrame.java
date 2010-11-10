/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ProgressFrame.java
 *
 * Created on 2010-04-23, 11:44:49
 */

package pl.umk.mat.imare.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyVetoException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.umk.mat.imare.gui.related.ProgressListener;


/**
 *
 * @author morti
 */
public class ProgressFrame extends javax.swing.JInternalFrame implements ProgressListener {


	private double startTime;
	private Object objectToNotify;
	private MainGUI mainWindow;

	private DecimalFormat dformat = new DecimalFormat("0");
    /** Creates new form ProgressFrame */
    public ProgressFrame(String title, Object objectToNotify) {
        initComponents();
		setTitle(title);
		setVisible(true);
		try {
			setSelected(true);
		} catch (PropertyVetoException ex) {
			Logger.getLogger(ProgressFrame.class.getName()).log(Level.SEVERE, null, ex);
		}
		this.objectToNotify = objectToNotify;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        progressBar = new javax.swing.JProgressBar();

        setTitle("Ładowanie pliku audio...");
        setDoubleBuffered(true);
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
		Component c = getDesktopPane();
		while(!(c instanceof MainGUI) && c != null) c = c.getParent();

		if(c != null) mainWindow = (MainGUI)c;

		int x = (getDesktopPane().getWidth() - getWidth()) / 2;
		int y = (getDesktopPane().getHeight() - getHeight()) / 2;
		setLocation(x, y);
	}//GEN-LAST:event_formAncestorAdded

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables

	@Override
	public void jobStarted(Object sender) {
		startTime = (double)System.currentTimeMillis() / 1000.0;
	}

	@Override
	public void jobProgress(Object sender, final float progress) {		
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
//				double currentTime = (double)System.currentTimeMillis() / 1000.0;
//				double deltaTime = currentTime - startTime;
//
//				final double estTime = deltaTime / progress * (1f - progress);
//				timeLabel.setText("Pozostało sekund: " + dformat.format(estTime));
				progressBar.setValue((int)(progress * 100f));

			}
		});		
	}

	@Override
	public void jobFinished(Object sender) {
		if(objectToNotify != null) {
			synchronized(objectToNotify) {
				objectToNotify.notifyAll();
			}
		}
		dispose();
	}

}
