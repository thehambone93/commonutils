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

import java.awt.CardLayout;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import weshampson.commonutils.ansi.ANSI;
import weshampson.commonutils.jar.JarUtils;
import weshampson.commonutils.jvm.JVMBuilder;
import weshampson.commonutils.logging.Level;
import static weshampson.commonutils.logging.Level.DOCATTRS_ERROR;
import static weshampson.commonutils.logging.Level.DOCATTRS_INFO;
import static weshampson.commonutils.logging.Level.DOCATTRS_WARNING;
import weshampson.commonutils.logging.Logger;
import static weshampson.commonutils.logging.Logger.STREAM_STDERR;
import static weshampson.commonutils.logging.Logger.STREAM_STDOUT;

/**
 *
 * @author  Wes Hampson
 * @version 0.3.1 (Nov 23, 2014)
 * @since   0.3.0 (Sep 20, 2014)
 */
public class Updater extends javax.swing.JDialog {
    public static final int CANCEL_OPTION = -1;
    public static final int YES_OPTION = 0;
    public static final int NO_OPTION = 1;
    protected static final Level UPDATER_LEVEL_INFO = new Level(STREAM_STDOUT, "[%T UPDATER/INFO]: ", ANSI.Color.DEFAULT, DOCATTRS_INFO);
    protected static final Level UPDATER_LEVEL_ERROR = new Level(STREAM_STDERR, "[%T UPDATER/ERROR]: ", ANSI.Color.RED, DOCATTRS_ERROR);
    protected static final Level UPDATER_LEVEL_WARNING = new Level(STREAM_STDOUT, "[%T UPDATER/WARNING]: ", ANSI.Color.YELLOW, DOCATTRS_WARNING);
    private static final String UPDATE_AVAILABLE_PANEL_IDENTIFIER = "updateAvailablePanel";
    private static final String DOWNLOAD_PROGRESS_PANEL_IDENTIFIER = "downloadProgressPanel";
    
    private final Window parentWindow = this;
    private final CardLayout cardLayout = new CardLayout();
//    private final Properties updaterProperties;
    private volatile boolean download;
    private boolean downloadCancelled;
    private boolean downloadError;
    private JSONObject updateInfo;
    private int exitOption = CANCEL_OPTION;
    public Updater(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLayout(cardLayout);
        cardLayout.addLayoutComponent(updateAvailablePanel, UPDATE_AVAILABLE_PANEL_IDENTIFIER);
        cardLayout.addLayoutComponent(downloadProgressPanel, DOWNLOAD_PROGRESS_PANEL_IDENTIFIER);
        getContentPane().add(updateAvailablePanel);
        getContentPane().add(downloadProgressPanel);
    }
    
    /** Extracts the Updater class to the system's "temp" folder.
     * This is necessary for "installing" updates.
     * @return a {@link java.io.File} representing the classpath of the
     * extracted Updater class 
     * @throws java.io.IOException 
     */
    public File extractInstaller() throws IOException {
        String fileSeparator = System.getProperty("file.separator");
        String tmpDir = System.getProperty("java.io.tmpdir");
        String fullyQualifiedClassName = UpdateInstaller.class.getCanonicalName();
        String resourceName = fullyQualifiedClassName.replace(".", "/") + ".class";
        String outFilePath = tmpDir + fileSeparator + fullyQualifiedClassName.replace(".", fileSeparator) + ".class";
        Logger.log(UPDATER_LEVEL_INFO, "Extracting installer to " + tmpDir);
        JarUtils.extractResource(resourceName, outFilePath);
        return(new File(tmpDir));
    }
    public boolean checkForUpdate() throws MalformedURLException, IOException {
        String programName = UpdaterSettingsManager.get(UpdaterSettingsManager.PROPERTY_PROGRAM_NAME);
        String versionString = UpdaterSettingsManager.get(UpdaterSettingsManager.PROPERTY_VERSION_STRING);
        String buildState = UpdaterSettingsManager.get(UpdaterSettingsManager.PROPERTY_BUILD_STATE);
        String updateURLString = UpdaterSettingsManager.get(UpdaterSettingsManager.PROPERTY_UPDATE_URL);
        Logger.log(UPDATER_LEVEL_INFO, "Checking for updates...");
        URL updateURL = new URL(updateURLString + "?programName=" + programName
                + "&versionString=" + versionString
                + "&buildState=" + buildState);
        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection uRLConnection = (HttpURLConnection)updateURL.openConnection();
        uRLConnection.setRequestMethod("GET");
        try {
            boolean redirect = false;
            uRLConnection.connect();
            if (uRLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                if (uRLConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                        || uRLConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
                        || uRLConnection.getResponseCode() == HttpURLConnection.HTTP_SEE_OTHER) {
                    redirect = true;
                    Logger.log(UPDATER_LEVEL_INFO, "HTTP " + uRLConnection.getResponseCode() + " - " + uRLConnection.getResponseMessage(), true, true);
                } else {
                    Logger.log(UPDATER_LEVEL_INFO, "Failed to check for updates: HTTP " + uRLConnection.getResponseCode() + " - " + uRLConnection.getResponseMessage(), true, true);
                    throw new IOException("Failed to check for updates: HTTP " + uRLConnection.getResponseCode() + " - " + uRLConnection.getResponseMessage());
                }
            }
            if (redirect) {
                String newURL = uRLConnection.getHeaderField("Location") + "?programName=" + programName
                        + "&versionString=" + versionString
                        + "&buildState=" + buildState;
                uRLConnection = (HttpURLConnection)new URL(newURL).openConnection();
            }
            Reader reader = new InputStreamReader(uRLConnection.getInputStream(), "UTF-8");
            int character;
            while ((character = reader.read()) != -1) {
                stringBuilder.append((char)character);
            }
        } catch (IOException ex) {
            if (ex instanceof ConnectException) {
                Logger.log(UPDATER_LEVEL_ERROR, "Failed to check for updates: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            } else {
                Logger.log(UPDATER_LEVEL_ERROR, "Failed to check for updates: HTTP " + uRLConnection.getResponseCode());
            }
            throw new IOException(ex);
        }
        String response = stringBuilder.toString();
        JSONParser jSONParser = new JSONParser();
        try {
            updateInfo = (JSONObject)jSONParser.parse(response);
            if (((String)updateInfo.get("updateAvailable")).equals("true")) {
                Logger.log(UPDATER_LEVEL_INFO, "Update found! Version: " + (String)updateInfo.get("versionString"));
                return(true);
            } else {
                Logger.log(UPDATER_LEVEL_INFO, "No updates found.");
                return(false);
            }
        } catch (org.json.simple.parser.ParseException ex) {
            throw new IOException(ex);
        }
    }
    public int showUpdateAvailableDialog() {
        updateAvailableYesButton.requestFocus();
        cardLayout.show(getContentPane(), UPDATE_AVAILABLE_PANEL_IDENTIFIER);
        pack();
        setModal(isModal());
        setTitle("Update available!");
        setLocationRelativeTo(getParent());
        setVisible(true);
        return(exitOption);
    }
    public File downloadUpdate() {
        try {
            final URL uRL = new URL(((String)updateInfo.get("downloadURL")).replace("\\", ""));
            String outputFileName = uRL.getPath().substring(uRL.getPath().lastIndexOf("/") + 1, uRL.getPath().length());
            final File outputFile = new File(outputFileName);
            download = true;
            downloadError = false;
            SwingWorker swingWorker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    while (download) {
                        Logger.log(UPDATER_LEVEL_INFO, "Downloading update from " + uRL + "...");
                        HttpURLConnection uRLConnection = (HttpURLConnection)uRL.openConnection();
                        uRLConnection.setRequestMethod("GET");
                        boolean redirect = false;
                        uRLConnection.connect();
                        if (uRLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            if (uRLConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                                    || uRLConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
                                    || uRLConnection.getResponseCode() == HttpURLConnection.HTTP_SEE_OTHER) {
                                redirect = true;
                                Logger.log(UPDATER_LEVEL_INFO, "HTTP " + uRLConnection.getResponseCode() + " - " + uRLConnection.getResponseMessage());
                            } else {
                                downloadError = true;
                                Logger.log(UPDATER_LEVEL_INFO, "Download failed: HTTP " + uRLConnection.getResponseCode() + " - " + uRLConnection.getResponseMessage());
                                showDownloadErrorDialog("HTTP " + uRLConnection.getResponseCode() + " - " + uRLConnection.getResponseMessage());;
                                dispose();
                                return(null);
                            }
                        }
                        if (redirect) {
                            String newURL = uRLConnection.getHeaderField("Location");
                            Logger.log(UPDATER_LEVEL_INFO, "New URL: " + newURL);
                            uRLConnection = (HttpURLConnection)new URL(newURL).openConnection();
                        }
                        int retries = -1;
                        do {
                            retries++;
                            if (retries > 1) {
                                Logger.log(UPDATER_LEVEL_INFO, "Retrying download... (" + retries + " of 20)");
                            }
                            int bytesRead = 0;
                            byte[] buffer = new byte[16 * 1024];
                            long downloadSize = uRLConnection.getContentLengthLong();
                            long bytesDownloaded = 0;
                            int percentComplete = 0;
                            if (downloadSize == -1) {
                                retries++;
                                continue;
                            }
                            BufferedInputStream fileIn = new BufferedInputStream(uRLConnection.getInputStream());
                            FileOutputStream fileOut = new FileOutputStream(outputFile);
                            Logger.log(UPDATER_LEVEL_INFO, "File size: " + downloadSize  + " bytes");
                            try {
                                while ((bytesRead = fileIn.read(buffer)) != -1 && download) {
                                    fileOut.write(buffer, 0, bytesRead);
                                    bytesDownloaded += bytesRead;
                                    int tempPercent = (int)Math.round(((float)bytesDownloaded / (float)downloadSize) * 100);
                                    if (tempPercent > percentComplete) {
                                        percentComplete = tempPercent;
                                        downloadProgressBar.setValue(percentComplete);
                                        downloadProgressSizeLabel.setText(Math.round((float)bytesDownloaded / 1024.0) + " kB / " + Math.round((float)downloadSize / 1024.0) + " kB downloaded");
                                    }
                                }
                            } catch (IOException ex) {
                                downloadError = true;
                                showDownloadErrorDialog(ex.toString());
                                dispose();
                                break;
                            } finally {
                                fileOut.close();
                                fileIn.close();
                                download = false;
                                if (!downloadCancelled && !downloadError) {
                                    Logger.log(UPDATER_LEVEL_INFO, "Download complete! File saved at " + outputFile.getCanonicalPath());
                                }
                                dispose();
                                break;
                            }
                        } while (retries <= 20);
                    }
                    return(null);
                }
                @Override
                protected void done() {
                    if (downloadCancelled) {
                        Logger.log(UPDATER_LEVEL_INFO, "Download cancelled by user.");
                        outputFile.delete();
                    }
                }
            };
            swingWorker.execute();
            cardLayout.show(getContentPane(), DOWNLOAD_PROGRESS_PANEL_IDENTIFIER);
            pack();
            setModal(isModal());
            setTitle("Downloading update...");
            downloadProgressFileLabel.setText("Downloading " + outputFileName + "...");
            setLocationRelativeTo(getParent());
            setVisible(true);
            if (!downloadError && !downloadCancelled) {
                return(outputFile);
            }
        } catch (MalformedURLException ex) {
            downloadError = true;
            showDownloadErrorDialog(ex.toString());
            dispose();
        }
        return(null);
    }
    public void installUpdate(File newVersionFile, File oldVersionFile, File installerClassPath) throws IOException, InterruptedException {
        JVMBuilder.exec(new String[] {"-cp", installerClassPath.getCanonicalPath()}, UpdateInstaller.class.getCanonicalName(), new String[] {newVersionFile.getCanonicalPath()});
    }
    private void showDownloadErrorDialog(String errorMessage) {
        JOptionPane.showMessageDialog(parentWindow, "<html><p style='width: 200px;'>An error occured while downloading the update.\n"
                + "Error details:\n"
                + errorMessage, "Error Downloading Update", JOptionPane.ERROR_MESSAGE);
    }
    private void cancelDownload() {
        download = false;
        downloadCancelled = true;
    }
    private static enum BuildState {
        ALL("All", "*"),
        ALPHA("Alpha", "alpha"),
        BETA("Beta", "beta"),
        STABLE("Stable", "stable");
        
        private final String displayName;
        private final String uRLString;
        private BuildState(String displayName, String uRLString) {
            this.displayName = displayName;
            this.uRLString = uRLString;
        }
        public String getDisplayName() {
            return(displayName);
        }
        public String getURLString() {
            return(uRLString);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        updateAvailablePanel = new javax.swing.JPanel();
        updateAvailableLabel1 = new javax.swing.JLabel();
        updateAbailableLabel2 = new javax.swing.JLabel();
        updateAvailableMoreInfoButton = new javax.swing.JButton();
        updateAvailableNoButton = new javax.swing.JButton();
        updateAvailableYesButton = new javax.swing.JButton();
        downloadProgressPanel = new javax.swing.JPanel();
        downloadProgressFileLabel = new javax.swing.JLabel();
        downloadProgressBar = new javax.swing.JProgressBar();
        downloadProgressSizeLabel = new javax.swing.JLabel();
        downloadProgressCancelButton = new javax.swing.JButton();

        updateAvailableLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        updateAvailableLabel1.setText("An update for " + UpdaterSettingsManager.get(UpdaterSettingsManager.PROPERTY_PROGRAM_NAME) + " is available!");

        updateAbailableLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        updateAbailableLabel2.setText("Would you like to download it?");

        updateAvailableMoreInfoButton.setText("More Info");
        updateAvailableMoreInfoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateAvailableMoreInfoButtonActionPerformed(evt);
            }
        });

        updateAvailableNoButton.setText("No");
        updateAvailableNoButton.setMaximumSize(new java.awt.Dimension(80, 23));
        updateAvailableNoButton.setMinimumSize(new java.awt.Dimension(80, 23));
        updateAvailableNoButton.setPreferredSize(new java.awt.Dimension(80, 23));
        updateAvailableNoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateAvailableNoButtonActionPerformed(evt);
            }
        });

        updateAvailableYesButton.setText("Yes");
        updateAvailableYesButton.setMaximumSize(new java.awt.Dimension(80, 23));
        updateAvailableYesButton.setMinimumSize(new java.awt.Dimension(80, 23));
        updateAvailableYesButton.setPreferredSize(new java.awt.Dimension(80, 23));
        updateAvailableYesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateAvailableYesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout updateAvailablePanelLayout = new javax.swing.GroupLayout(updateAvailablePanel);
        updateAvailablePanel.setLayout(updateAvailablePanelLayout);
        updateAvailablePanelLayout.setHorizontalGroup(
            updateAvailablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateAvailablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(updateAvailablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(updateAbailableLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(updateAvailableLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, updateAvailablePanelLayout.createSequentialGroup()
                        .addComponent(updateAvailableMoreInfoButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                        .addComponent(updateAvailableNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updateAvailableYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        updateAvailablePanelLayout.setVerticalGroup(
            updateAvailablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateAvailablePanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(updateAvailableLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateAbailableLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(updateAvailablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updateAvailableYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateAvailableNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateAvailableMoreInfoButton))
                .addContainerGap())
        );

        downloadProgressFileLabel.setText("Downloading <file>...");

        downloadProgressBar.setStringPainted(true);

        downloadProgressSizeLabel.setText("0 kB / 0 kB downloaded");

        downloadProgressCancelButton.setText("Cancel");
        downloadProgressCancelButton.setMaximumSize(new java.awt.Dimension(80, 23));
        downloadProgressCancelButton.setMinimumSize(new java.awt.Dimension(80, 23));
        downloadProgressCancelButton.setPreferredSize(new java.awt.Dimension(80, 23));
        downloadProgressCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadProgressCancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout downloadProgressPanelLayout = new javax.swing.GroupLayout(downloadProgressPanel);
        downloadProgressPanel.setLayout(downloadProgressPanelLayout);
        downloadProgressPanelLayout.setHorizontalGroup(
            downloadProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(downloadProgressPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(downloadProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(downloadProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, downloadProgressPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(downloadProgressCancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(downloadProgressPanelLayout.createSequentialGroup()
                        .addGroup(downloadProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(downloadProgressSizeLabel)
                            .addComponent(downloadProgressFileLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        downloadProgressPanelLayout.setVerticalGroup(
            downloadProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, downloadProgressPanelLayout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addComponent(downloadProgressFileLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadProgressSizeLabel)
                .addGap(18, 18, 18)
                .addComponent(downloadProgressCancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 345, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 151, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateAvailableMoreInfoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateAvailableMoreInfoButtonActionPerformed
        try {
            JOptionPane.showMessageDialog(this, "<html><p style='width: 200px;'>Version: " + updateInfo.get("versionString") + "<br>"
                    + "Build state: " + updateInfo.get("buildState") + "<br>"
                    + "Build date: " + new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String)updateInfo.get("buildTimestamp"))) + "<br><br>"
                    + "Description: " + updateInfo.get("updateInfo"), "Update Info", JOptionPane.INFORMATION_MESSAGE);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }//GEN-LAST:event_updateAvailableMoreInfoButtonActionPerformed

    private void updateAvailableNoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateAvailableNoButtonActionPerformed
        exitOption = NO_OPTION;
        dispose();
    }//GEN-LAST:event_updateAvailableNoButtonActionPerformed

    private void updateAvailableYesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateAvailableYesButtonActionPerformed
        exitOption = YES_OPTION;
        dispose();
    }//GEN-LAST:event_updateAvailableYesButtonActionPerformed

    private void downloadProgressCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadProgressCancelButtonActionPerformed
        cancelDownload();
        dispose();
    }//GEN-LAST:event_downloadProgressCancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar downloadProgressBar;
    private javax.swing.JButton downloadProgressCancelButton;
    private javax.swing.JLabel downloadProgressFileLabel;
    private javax.swing.JPanel downloadProgressPanel;
    private javax.swing.JLabel downloadProgressSizeLabel;
    private javax.swing.JLabel updateAbailableLabel2;
    private javax.swing.JLabel updateAvailableLabel1;
    private javax.swing.JButton updateAvailableMoreInfoButton;
    private javax.swing.JButton updateAvailableNoButton;
    private javax.swing.JPanel updateAvailablePanel;
    private javax.swing.JButton updateAvailableYesButton;
    // End of variables declaration//GEN-END:variables
}