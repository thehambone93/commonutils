/*
 * Copyright (C) 2014 Wes Hampson.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package weshampson.commonutils.updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import weshampson.commonutils.logging.Level;
import weshampson.commonutils.logging.Logger;
import weshampson.commonutils.xml.XMLReader;

/**
 *
 * @author Wes Hampson
 * @version 0.3.0 (Sep 27, 2014)
 * @since   0.3.0 (Sep 27, 2014)
 */
public class UpdaterSettingsPanel extends javax.swing.JPanel {
    private final File configFile;
    private final Properties updaterProperties;
    public UpdaterSettingsPanel(File configFile) throws IOException, DocumentException {
        initComponents();
        this.configFile = configFile;
        this.updaterProperties = loadProperties(configFile);
    }
    private Properties loadProperties(File configFile) throws IOException, DocumentException {
        Properties properties = new Properties();
        if (!configFile.exists()) {
            throw new FileNotFoundException("file not found - " + configFile.getAbsolutePath());
        }
        Document doc = XMLReader.read(configFile);
        Element rootElement = doc.getRootElement();
        for (Iterator i = rootElement.elementIterator(); i.hasNext();) {
            Element e = (Element)i.next();
            properties.put(e.getName(), e.getText());
        }
        jTextField1.setText(properties.getProperty("updateURL"));
        String buildState = properties.getProperty("buildState");
        if (buildState.equals("*")) {
            buildState = "all";
        }
        jComboBox1.setSelectedItem(buildState);
        jCheckBox1.setSelected(Boolean.valueOf(properties.getProperty("checkOnStartup")));
        jCheckBox2.setSelected(Boolean.valueOf(properties.getProperty("deleteOldVersion")));
        return(properties);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();

        jLabel1.setText("Update URL:");

        jLabel2.setText("Build state:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "alpha", "beta", "stable", "all" }));

        jCheckBox1.setText("Check for updates on startup");

        jCheckBox2.setText("Delete old version after installing");

        jButton1.setText("Save");
        jButton1.setMaximumSize(new java.awt.Dimension(65, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(65, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(65, 23));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox2)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jCheckBox1))
                        .addGap(0, 105, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configFile);
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            XMLWriter xMLWriter = new XMLWriter(fileOutputStream, outputFormat);
            Document doc = DocumentHelper.createDocument();
            Element root = doc.addElement("updaterConfig");
            root.addElement("updateURL").addText(jTextField1.getText());
            root.addElement("programName").addText(updaterProperties.getProperty("programName"));
            root.addElement("versionString").addText(updaterProperties.getProperty("versionString"));
            String buildState = (String)jComboBox1.getSelectedItem();
            if (buildState.equals("all")) {
                buildState = "*";
            }
            root.addElement("buildState").addText(buildState);
            root.addElement("deleteOldVersion").addText(Boolean.toString(jCheckBox2.isSelected()));
            root.addElement("checkOnStartup").addText(Boolean.toString(jCheckBox1.isSelected()));
            xMLWriter.write(doc);
            xMLWriter.close();
            Logger.log(Level.INFO, "Saved updater configuration to " + configFile.getAbsolutePath());
        } catch (IOException ex) {
            Logger.log(Level.ERROR, ex, "Failed to write file - " + configFile.getAbsolutePath() + ": " + ex.toString());
            JOptionPane.showMessageDialog(getParent(), "<html><p style='width: 200px;'>Failed to save updater configuration.\n"
                    + "Details: " + ex.toString(), "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}