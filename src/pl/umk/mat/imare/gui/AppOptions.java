/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AppOptions.java
 *
 * Created on 2010-04-09, 13:00:59
 */

package pl.umk.mat.imare.gui;

import javax.swing.JOptionPane;
import pl.umk.mat.imare.io.Config;

/**
 *
 * @author Bartek
 */
public class AppOptions extends javax.swing.JInternalFrame {

    /** Creates new form AppOptions */
    public AppOptions() {
        initComponents();
        readConfig();
    }

    private void readConfig() {
        String[] s = null;
        s = Config.read("ShowStartupScreen");
        cbShowStartupScreen.setSelected(s[0].equals("0") ? false : true);
        s = Config.read("RecentItemsNumber");
        spRecentItemsNumber.setValue(Integer.parseInt(s[0]));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbShowStartupScreen = new javax.swing.JCheckBox();
        spRecentItemsNumber = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        btnRestoreDefaults = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        setClosable(true);
        setTitle("Opcje");

        cbShowStartupScreen.setText("Pokazuj ekran startowy");
        cbShowStartupScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbShowStartupScreenActionPerformed(evt);
            }
        });

        spRecentItemsNumber.setModel(new javax.swing.SpinnerNumberModel(5, 1, 20, 1));
        spRecentItemsNumber.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spRecentItemsNumberStateChanged(evt);
            }
        });

        jLabel1.setText("Ilość elementów w Ostatnio otwierane:");

        btnRestoreDefaults.setText("Przywróć domyślne");
        btnRestoreDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRestoreDefaultsActionPerformed(evt);
            }
        });

        btnClose.setText("Zamknij");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbShowStartupScreen)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spRecentItemsNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnRestoreDefaults)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbShowStartupScreen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spRecentItemsNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRestoreDefaults)
                    .addComponent(btnClose))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cbShowStartupScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbShowStartupScreenActionPerformed
        Config.write("ShowStartupScreen", cbShowStartupScreen.isSelected() ? "1" : "0");
    }//GEN-LAST:event_cbShowStartupScreenActionPerformed

    private void spRecentItemsNumberStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spRecentItemsNumberStateChanged
        Config.write("RecentItemsNumber", "" + spRecentItemsNumber.getValue());
    }//GEN-LAST:event_spRecentItemsNumberStateChanged

    private void btnRestoreDefaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRestoreDefaultsActionPerformed
        if (JOptionPane.showConfirmDialog(this,
                "Czy na pewno chcesz przywrócić ustawienia domyślne?",
                "Przywróć domyślne", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            Config.reset();
            readConfig();
        }
    }//GEN-LAST:event_btnRestoreDefaultsActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        dispose();
    }//GEN-LAST:event_btnCloseActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnRestoreDefaults;
    private javax.swing.JCheckBox cbShowStartupScreen;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSpinner spRecentItemsNumber;
    // End of variables declaration//GEN-END:variables

}
