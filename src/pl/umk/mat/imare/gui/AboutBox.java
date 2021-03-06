/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AboutBox.java
 *
 * Created on 2010-03-26, 13:18:45
 */

package pl.umk.mat.imare.gui;

import java.awt.image.BufferedImage;

/**
 *
 * @author Bartek
 */
public class AboutBox extends javax.swing.JDialog {

    private BufferedImage bi;

    /** Creates new form AboutBox */
    public AboutBox(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
		setLocationRelativeTo(parent);
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("O programie");
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pl/umk/mat/imare/gui/gfx/imare_black.png"))); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        getContentPane().add(jLabel1, new java.awt.GridBagConstraints());

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel2.setText("Intelligent Modular Audio Recognition Engine");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        getContentPane().add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel3.setText("<html>&copy;2010</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jLabel3, gridBagConstraints);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText("<html>Projekt zaliczeniowy z przedmiotu Programowanie Zespołowe grupy nr 4 w roku akademickim 2009/2010</html>");
        jLabel4.setMinimumSize(new java.awt.Dimension(30, 30));
        jLabel4.setPreferredSize(new java.awt.Dimension(30, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        getContentPane().add(jLabel4, gridBagConstraints);

        jLabel5.setText("Opiekun zespołu:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jLabel5, gridBagConstraints);

        jLabel12.setText("Michał Dudkiewicz");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jLabel12, gridBagConstraints);

        jLabel6.setText("Członkowie zespołu:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jLabel6, gridBagConstraints);

        jLabel7.setText("Mariusz Tycz");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jLabel7, gridBagConstraints);

        jLabel8.setText("Marcin Radoszewski");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jLabel8, gridBagConstraints);

        jLabel9.setText("Piotr Różański");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jLabel9, gridBagConstraints);

        jLabel10.setText("Maciej Włoch");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jLabel10, gridBagConstraints);

        jLabel11.setText("Bartosz Zasada");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jLabel11, gridBagConstraints);

        btnOK.setText("Zamknij");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(btnOK, gridBagConstraints);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-493)/2, (screenSize.height-361)/2, 493, 361);
    }// </editor-fold>//GEN-END:initComponents

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables

}
