/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StartupFrame.java
 *
 * Created on 2010-03-31, 15:00:18
 */

package pl.umk.mat.imare.gui;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.umk.mat.imare.io.Config;

/**
 *
 * @author morti
 */
public class StartupFrame extends javax.swing.JInternalFrame {

	private MainGUI mainWindow = null;

    /** Creates new form StartupFrame */
    public StartupFrame() {
        initComponents();
    }

    public void setCB(boolean state) {
        this.dontShowCheckBox.setSelected(state);
        this.repaint();
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

        newProjectButton = new javax.swing.JButton();
        openProjectButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        dontShowCheckBox = new javax.swing.JCheckBox();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Ekran powitalny");
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        newProjectButton.setFont(new java.awt.Font("Tahoma", 0, 36));
        newProjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pl/umk/mat/imare/gui/gfx/menu_newproject.png"))); // NOI18N
        newProjectButton.setText("Nowy Projekt");
        newProjectButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        newProjectButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        newProjectButton.setIconTextGap(100);
        newProjectButton.setPreferredSize(new java.awt.Dimension(550, 128));
        newProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 59;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(newProjectButton, gridBagConstraints);

        openProjectButton.setFont(new java.awt.Font("Tahoma", 0, 36));
        openProjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pl/umk/mat/imare/gui/gfx/menu_openproject.png"))); // NOI18N
        openProjectButton.setText("Otwórz Projekt");
        openProjectButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        openProjectButton.setIconTextGap(100);
        openProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openProjectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 28;
        gridBagConstraints.ipady = -10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        getContentPane().add(openProjectButton, gridBagConstraints);

        helpButton.setFont(new java.awt.Font("Tahoma", 0, 36));
        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pl/umk/mat/imare/gui/gfx/menu_help.png"))); // NOI18N
        helpButton.setText("Pomoc");
        helpButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        helpButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        helpButton.setIconTextGap(100);
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 172;
        gridBagConstraints.ipady = -10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(helpButton, gridBagConstraints);

        dontShowCheckBox.setFont(new java.awt.Font("Tahoma", 0, 18));
        dontShowCheckBox.setText("Nie pokazuj więcej tego okna");
        dontShowCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dontShowCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(dontShowCheckBox, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void newProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProjectButtonActionPerformed

		mainWindow.addFrame(new WizardFrame(), true);
		this.setVisible(false);
}//GEN-LAST:event_newProjectButtonActionPerformed

	private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
           try {
            // TODO add your handling code here:
            Desktop.getDesktop().open(new File("doc/pomoc.html"));
        } catch (IOException ex) {
            Logger.getLogger(StartupFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_helpButtonActionPerformed

        private void dontShowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dontShowCheckBoxActionPerformed
            Config.write("ShowStartupScreen", this.dontShowCheckBox.isSelected() ? "0" : "1");
        }//GEN-LAST:event_dontShowCheckBoxActionPerformed

		private void openProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openProjectButtonActionPerformed
			dispose();
			mainWindow.openProjectClicked();
		}//GEN-LAST:event_openProjectButtonActionPerformed

		private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
			Component c = getDesktopPane();
			while(!(c instanceof MainGUI) && c != null) c = c.getParent();

			if(c != null) mainWindow = (MainGUI)c;
		}//GEN-LAST:event_formAncestorAdded


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox dontShowCheckBox;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton newProjectButton;
    private javax.swing.JButton openProjectButton;
    // End of variables declaration//GEN-END:variables

}
