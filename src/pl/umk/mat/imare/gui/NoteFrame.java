/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NoteFrame.java
 *
 * Created on 2010-03-27, 08:39:32
 */
package pl.umk.mat.imare.gui;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.swing.JFileChooser;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import pl.umk.mat.imare.gui.related.ProgressListener;
import pl.umk.mat.imare.midi.MidiPlayer;
import pl.umk.mat.imare.midi.MidiPlayerListener;
import pl.umk.mat.imare.reco.StaveData;

/**
 *
 * @author morti
 */
public class NoteFrame extends javax.swing.JInternalFrame implements MetaEventListener, MidiPlayerListener {

    /** Creates new form NoteFrame */
    private MidiPlayer midi = null;
    private JPanel cursorPanel = null;
    private BufferedImage timeImage = null;
    private String previnstrument = "";

    public void exportPDF(ProgressFrame listenframe) {
        final ProgressListener listen=listenframe;
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".pdf") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Plik PDF (*.pdf)";
            }
        });

        switch (fc.showSaveDialog(this)) {
            case JFileChooser.CANCEL_OPTION:
                return;
        }
        File f = fc.getSelectedFile();

        if (!f.getName().endsWith(".pdf")) {
            f = new File(f.getAbsolutePath() + ".pdf");
        }
        if (f.exists()) {
            if (JOptionPane.showConfirmDialog(this,
                    "Czy nadpisać istniejący plik?") == JOptionPane.NO_OPTION) {
                return;
            }
        }
		
        final File file=f;
        Runnable ru = new Runnable() {

            @Override
            public void run() {
                listen.jobStarted(this);
                int lmargin = 30, rmargin = 30, tmargin = 50, bmargin = 50, smargin = 10;
                int n = 2;
                int w = 595, h = 842;
                while (tmargin + bmargin + stavePanel.getTotalHeight() > n * h) {
                    n++;
                }
                Document document = new Document();
                w *= n;
                h *= n;
                Rectangle r = new Rectangle(0, 0, w, h);
                document.setPageSize(r);
                try {
                    BufferedImage buff = new BufferedImage(w - (lmargin + rmargin),
                            stavePanel.getTotalHeight(), BufferedImage.TYPE_INT_ARGB);

                    Graphics g = buff.getGraphics();
                    g.setColor(stavePanel.getBackground());

                    /*g.clearRect(0, 0, buff.getWidth(), buff.getHeight());

                    g.setClip(0, 0, buff.getWidth(), buff.getHeight());

                    stavePanel.paint(g);
                    g.dispose();*/

                    PdfWriter writer;
                    writer = PdfWriter.getInstance(document, new FileOutputStream(file));
                    document.open();

                    int x = 0, y = tmargin;
                    PdfContentByte cb = writer.getDirectContent();
                    int id = 0;
                    while (x < stavePanel.getWidth()) {
                        id++;
                        if (y + stavePanel.getTotalHeight() > h - bmargin) {
                            document.newPage();
                            //writer.newPage();
                            y = tmargin;
                        }
                        int wid = w - (lmargin + rmargin);
                        PdfTemplate tp = cb.createTemplate(wid, stavePanel.getTotalHeight());
                        Graphics2D g2;
                        g2 = tp.createGraphicsShapes(wid, stavePanel.getTotalHeight());
                        g2.setColor(Color.white);
                        g2.fillRect(0, 0, wid, stavePanel.getTotalHeight());

                        g.setClip(x, 0, buff.getWidth(), buff.getHeight());

                        /*try {
                        ImageIO.write(buff, "png", new File("a" + id + ".png"));
                        } catch (IOException ex) {
                        Logger.getLogger(NoteFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }*/
                        g.setColor(stavePanel.getBackground());
                        g.fillRect(x, 0, buff.getWidth(), buff.getHeight());
                        stavePanel.print(g);
                        g.translate(-buff.getWidth(), 0);

                        g2.drawImage(buff, null, 0, 0);

                        g2.dispose();
                        cb.addTemplate(tp, lmargin, h - (y + stavePanel.getTotalHeight()));

                        y += stavePanel.getTotalHeight() + smargin;
                        x += wid;
                        listen.jobProgress(this, (float)x/(float)stavePanel.getWidth());
                    }
                    g.dispose();
                } catch (DocumentException ex) {
                    //Logger.getLogger(NoteFrame.class.getName()).log(Level.SEVERE, null, ex);
                  JOptionPane.showMessageDialog(null, "Nie można utworzyć pliku", "Nie można utworzyć pliku", JOptionPane.ERROR_MESSAGE);
                } catch (FileNotFoundException ex) {
                    //Logger.getLogger(NoteFrame.class.getName()).log(Level.SEVERE, null, ex);
                  JOptionPane.showMessageDialog(null, "Nie można utworzyć pliku", "Nie można utworzyć pliku", JOptionPane.ERROR_MESSAGE);
                }
                document.close();
                listen.jobFinished(this);
            }
        };
        Thread thread=new Thread(ru);
        thread.setDaemon(true);
        listenframe.setVisible(true);
        thread.start();
    }

    public void exportMidi() {
        if (instrumentComboBox.isEnabled()) {
            String s = instrumentComboBox.getSelectedItem().toString();
            if (!s.contentEquals(previnstrument)) {
                previnstrument = s;
                long p = midi.getPosition();
                Instrument ins = midi.findInstrument(s);
                try {
                    midi.setSequenceInstrument(0, ins);
                    midi.setPosition(p);
                } catch (InvalidMidiDataException ex) {
                    Logger.getLogger(NoteFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (!midi.haveSequence()) {
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".mid") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Plik MIDI (*.mid)";
            }
        });

        switch (fc.showSaveDialog(this)) {
            case JFileChooser.CANCEL_OPTION:
                return;
        }
        File file = fc.getSelectedFile();

        if (!file.getName().endsWith(".mid")) {
            file = new File(file.getAbsolutePath() + ".mid");
        }
        if (file.exists()) {
            if (JOptionPane.showConfirmDialog(this,
                    "Czy nadpisać istniejący plik?") == JOptionPane.NO_OPTION) {
                return;
            }
        }
        try {
            midi.saveSequence(file);
        } catch (IOException ex) {
            //Logger.getLogger(NoteFrame.class.getName()).log(Level.SEVERE, null, ex);
          JOptionPane.showMessageDialog(null, "Nie można utworzyć pliku", "Nie można utworzyć pliku", JOptionPane.ERROR_MESSAGE);
        }
    }

    public NoteFrame() {
        initComponents();
        initMidi();

		layerPane.add(new TimeRullerPanel(stavePanel), new Integer(100));

        cursorPanel = new JPanel();
        cursorPanel.setMaximumSize(new Dimension(1, 0));
        cursorPanel.setMinimumSize(new Dimension(1, 0));
        cursorPanel.setBackground(Color.red);

        cursorPanel.setLocation(-1, 0);
        layerPane.add(cursorPanel, new Integer(1));

		stavePanel.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				Dimension size = e.getComponent().getSize();
				
				layerPane.setSize(size);

				layerPane.setPreferredSize(size);
//				timePanel.setSize(size.width, timePanel.getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});

		stavePanel.setLocation(0, 0);
		stavePanel.setSize(scrollPane.getViewport().getSize());
    }

    private void initMidi() {
        /*try {
        midi = new MidiPlayer();
        midi.addListener(this);
        //			if (stavePanel.getStaveData() != null) {
        //				midi.createFromNotes(stavePanel.getStaveData());
        //			}
        midi.addMidiPlayerListener(this);

        String[] s=midi.listAllInstuments();
        for(int i=0;i<s.length;i++)
        {
        jComboBox1.addItem(s[i]);
        }
        if(s.length>0)
        {
        previnstrument=s[0];
        }
        else
        {
        playButton.setEnabled(false);
        jComboBox1.setEnabled(false);
        }
        } catch (MidiUnavailableException ex) {
        playButton.setEnabled(false);
        jComboBox1.setEnabled(false);
        Logger.getLogger(NoteFrame.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        midi = new MidiPlayer();
        midi.addListener(this);
//	if (stavePanel.getStaveData() != null) {
//		midi.createFromNotes(stavePanel.getStaveData());
//	}
        midi.addMidiPlayerListener(this);

        String[] s = midi.listAllInstuments();
		Arrays.sort(s);
		int index = Arrays.binarySearch(s, "Piano");

        for (int i = 0; i < s.length; i++) {
            instrumentComboBox.addItem(s[i]);
        }
        if (s.length > 0) {
            previnstrument = s[0];
        } else {
            playButton.setEnabled(false);
            instrumentComboBox.setEnabled(false);
        }
        if (!midi.getInitialized()) {
            playButton.setEnabled(false);
            instrumentComboBox.setEnabled(false);
        }

	if (index>=0) instrumentComboBox.setSelectedIndex(index);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        scrollPane = new javax.swing.JScrollPane();
        layerPane = new JLayeredPane() {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };
        stavePanel = new pl.umk.mat.imare.gui.StavePanel();
        jPanel1 = new javax.swing.JPanel();
        playButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        instrumentComboBox = new javax.swing.JComboBox();
        transparencyButton = new javax.swing.JButton();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setMaximizable(true);
        setResizable(true);
        setTitle("Nuty");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                onClose(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
                onAncestorRemoved(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        scrollPane.setBackground(new java.awt.Color(255, 255, 255));
        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new java.awt.Dimension(450, 300));
        scrollPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                scrollPaneComponentResized(evt);
            }
        });

        layerPane.setBackground(new java.awt.Color(255, 255, 255));

        stavePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stavePanelMouseClicked(evt);
            }
        });
        stavePanel.setLayout(new java.awt.BorderLayout());
        stavePanel.setBounds(0, 0, 520, 230);
        layerPane.add(stavePanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        scrollPane.setViewportView(layerPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        getContentPane().add(scrollPane, gridBagConstraints);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        playButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pl/umk/mat/imare/gui/gfx/play.png"))); // NOI18N
        playButton.setPreferredSize(new java.awt.Dimension(27, 27));
        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playButtonActionPerformed(evt);
            }
        });
        jPanel1.add(playButton);

        pauseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pl/umk/mat/imare/gui/gfx/pause.png"))); // NOI18N
        pauseButton.setEnabled(false);
        pauseButton.setPreferredSize(new java.awt.Dimension(27, 27));
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });
        jPanel1.add(pauseButton);

        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pl/umk/mat/imare/gui/gfx/stop.png"))); // NOI18N
        stopButton.setEnabled(false);
        stopButton.setPreferredSize(new java.awt.Dimension(27, 27));
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        jPanel1.add(stopButton);

        jPanel1.add(instrumentComboBox);

        transparencyButton.setText("Przezroczystość Wł/Wył");
        transparencyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transparencyButtonActionPerformed(evt);
            }
        });
        jPanel1.add(transparencyButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jPanel1, gridBagConstraints);

        setBounds(0, 0, 613, 312);
    }// </editor-fold>//GEN-END:initComponents

	private void transparencyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transparencyButtonActionPerformed
            stavePanel.setNoteVolumeAlphaBlending(!stavePanel.isNoteVolumeAlphaBlending());
	}//GEN-LAST:event_transparencyButtonActionPerformed

        private void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
            /*try {
            midi.createFromNotes(stavePanel.getStaveData());
            } catch (InvalidMidiDataException ex) {
            Logger.getLogger(NoteFrame.class.getName()).log(Level.SEVERE, null, ex);
            }*/
            String s = instrumentComboBox.getSelectedItem().toString();
            if (!s.contentEquals(previnstrument)) {
                previnstrument = s;
                long p = midi.getPosition();
                Instrument ins = midi.findInstrument(s);
                try {
//					midi.setTempo(p)
                    midi.setSequenceInstrument(0, ins);
                    midi.setPosition(p);
                } catch (InvalidMidiDataException ex) {
                    Logger.getLogger(NoteFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (!stopButton.isEnabled() && cursorPanel.getLocation().x==-1) {
                scrollPane.getHorizontalScrollBar().setValue(0);
            }

            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
            playButton.setEnabled(false);
            instrumentComboBox.setEnabled(false);
            midi.start();
        }//GEN-LAST:event_playButtonActionPerformed

        private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
            midi.pause();
            pauseButton.setEnabled(false);
            stopButton.setEnabled(true);
            playButton.setEnabled(true);
            instrumentComboBox.setEnabled(true);
        }//GEN-LAST:event_pauseButtonActionPerformed

        private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
            midi.setPosition(0);
            midi.stop();
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
            playButton.setEnabled(true);
            instrumentComboBox.setEnabled(true);
        }//GEN-LAST:event_stopButtonActionPerformed

        private void onClose(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_onClose
            if (midi != null) {
                stopButtonActionPerformed(null);
//            midi.stop();
//            midi.close();
//            midi=null;
            }
        }//GEN-LAST:event_onClose

        private void onAncestorRemoved(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_onAncestorRemoved
            if (midi != null) {
                stopButtonActionPerformed(null);
//            midi.stop();
//            midi.close();
//            midi=null;
            }
        }//GEN-LAST:event_onAncestorRemoved

        private void scrollPaneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_scrollPaneComponentResized
//            stavePanel.setSize(
//                    Math.max(layerPane.getWidth(), scrollPane.getWidth()),
//                    Math.max(layerPane.getHeight(), scrollPane.getHeight()));
//            timePanel.setSize(stavePanel.getWidth(), timePanel.getHeight());
//            drawTime();
//            if (cursorPanel != null) {
//                cursorPanel.setSize(1, scrollPane.getHeight());
//            }

			JViewport view = scrollPane.getViewport();
			stavePanel.setSize(stavePanel.getWidth(), view.getHeight());
			stavePanel.revalidate();

			if (cursorPanel != null) {
				cursorPanel.setSize(1, scrollPane.getHeight());
			}			
        }//GEN-LAST:event_scrollPaneComponentResized

        private void instrumentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instrumentComboBoxActionPerformed
            // TODO add your handling code here:
        }//GEN-LAST:event_instrumentComboBoxActionPerformed

        private void stavePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stavePanelMouseClicked
            if(playButton.isEnabled()){
                long pos = (long) ((evt.getX() - stavePanel.getStartPosition()) / (double) (midi.getTempo()*stavePanel.getPixelsPerWholeNote())*1000000);
                if(pos<0){pos=0;}
                midi.setPosition(pos);
                if(pos==0)
                {
                  cursorPanel.setLocation(stavePanel.getStartPosition(), 0);
                }
                else
                {
                  cursorPanel.setLocation(evt.getX(), 0);
                }
            }
        }//GEN-LAST:event_stavePanelMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox instrumentComboBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLayeredPane layerPane;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton playButton;
    private javax.swing.JScrollPane scrollPane;
    private pl.umk.mat.imare.gui.StavePanel stavePanel;
    private javax.swing.JButton stopButton;
    private javax.swing.JButton transparencyButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void meta(MetaMessage meta) {
        if (meta.getType() == MidiPlayer.END_OF_TRACK_MESSAGE) {
            stopButtonActionPerformed(null);
        }
    }

    @Override
    public void positionChanged(long newPosition) {
        double sec = (double) newPosition / 1000000.0;
        double tempo = midi.getTempo();
        int x = (int) (sec * tempo
                * stavePanel.getPixelsPerWholeNote()) + stavePanel.getStartPosition();

        cursorPanel.setLocation(x, 0);
        if (x >= (scrollPane.getHorizontalScrollBar().getValue() + scrollPane.getHorizontalScrollBar().getVisibleAmount()) / (double) scrollPane.getHorizontalScrollBar().getMaximum() * stavePanel.getWidth()) {
            scrollPane.getHorizontalScrollBar().setValue(scrollPane.getHorizontalScrollBar().getValue() + scrollPane.getHorizontalScrollBar().getVisibleAmount());
        }
    }

    @Override
    public void notifyFinished() {
        if (cursorPanel != null) {
            cursorPanel.setLocation(-1, 0);
        }
    }

    public void setNotes(StaveData notes) {
        stavePanel.setStaveData(notes);
        try {
            StaveData sd = stavePanel.getStaveData();
            if (sd != null && midi != null && notes != null) {
                midi.setTempo(notes.getTempo());
                midi.createFromNotes(sd);
            }

            setTime();

        } catch (InvalidMidiDataException ex) {
            Logger.getLogger(NoteFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        layerPane.setBackground(Color.white);
        scrollPane.setBackground(Color.white);
        stavePanel.setBackground(Color.white);

        layerPane.setSize(stavePanel.getSize());
        layerPane.setPreferredSize(stavePanel.getSize());

        cursorPanel.setSize(1, stavePanel.getHeight());

        stavePanel.setSize(
                Math.max(stavePanel.getWidth(), scrollPane.getWidth()),
                Math.max(stavePanel.getHeight(), scrollPane.getHeight()));
//        timePanel.setSize(stavePanel.getWidth(), timePanel.getHeight());

        if (cursorPanel != null) {
            cursorPanel.setSize(1, scrollPane.getHeight());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (midi != null) {
            midi.stop();
            midi.close();
            midi = null;
        }
    }

    private void setTime() {
//        DecimalFormat df = new DecimalFormat("00.000");
//        //double length = midi.getLength() / 1000000.0;
//        double length = (stavePanel.getWidth() - stavePanel.getStartPosition()) / (double) stavePanel.getPixelsPerWholeNote() / (double) midi.getTempo();
//        String str = "<html><b>Długość:</b> ";
//
//        if (length >= 60) {
//
//            str += Integer.toString((int) (length / 60)) + ":";
//            length %= 60;
//        }
//        str += df.format(length) + "</html>";
//        lengthLabel.setText(str);
    }
}
