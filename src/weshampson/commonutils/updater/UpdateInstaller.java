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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author  Wes Hampson
 * @version 0.3.1 (Nov 23, 2014)
 * @since   0.3.0 (Sep 23, 2014)
 */
class UpdateInstaller {
    public static final String INSTALLER_VERSION_STRING = "0.3.1";
    public static void main(String[] args) {
        // java -cp "<rootdir>" weshampson.commonutils.updater.UpdateInstaller
        String newVersionPath = "";
        System.out.println("Updater version " + INSTALLER_VERSION_STRING);
        String usageString = "Usage: UpdateInstaller <new version file>";
        if (args.length < 1) {
            printMessage(usageString);
            System.exit(1);
        } else {
            newVersionPath = args[0];
        }
        if (newVersionPath.isEmpty()) {
            printMessage(usageString);
            System.exit(1);
        }
        System.out.println("Launching new version...");
        File newVersionFile = new File(newVersionPath);
        if (newVersionFile.exists() && !newVersionFile.isDirectory()) {
            try {
                launchNewVersion(newVersionFile);
            } catch (IOException ex) {
                printError("Failed to launch new version.\n"
                        + "Error details: " + ex.toString());
                System.exit(1);
            }
        } else {
            printError("Error: file not found - " + newVersionPath);
            System.exit(1);
        }
    }
    private static void launchNewVersion(File jarFile) throws IOException {
        List<String> execCommand = new ArrayList<>();
        String javaHome = System.getProperty("java.home");
        String fileSeparator = System.getProperty("file.separator");
        String osName = System.getProperty("os.name");
        String jVMExecutable = javaHome + fileSeparator + "bin" + fileSeparator + "java";
        if (osName.startsWith("Win")) {
            jVMExecutable += ".exe";
        }
        execCommand.add(jVMExecutable);
        execCommand.add("-jar");
        execCommand.add(jarFile.getAbsolutePath());
        ProcessBuilder jVMInstance = new ProcessBuilder(execCommand);
        jVMInstance.start();
    }
    private static void printMessage(String s) {
        System.out.println(s);
        JOptionPane.showMessageDialog(null, s);
    }
    private static void printError(String s) {
        System.err.println(s);
        JOptionPane.showMessageDialog(null, s, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
