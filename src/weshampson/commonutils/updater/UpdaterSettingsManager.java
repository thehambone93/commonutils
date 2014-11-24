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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import weshampson.commonutils.jar.JarUtils;
import weshampson.commonutils.logging.Logger;

/**
 *
 * @author  Wes Hampson
 * @version 0.3.1 (Nov 22, 2014)
 * @since   0.3.1 (Nov 22, 2014)
 */
public class UpdaterSettingsManager {
    public static final String PROPERTY_UPDATE_URL = "updateURL";
    public static final String PROPERTY_PROGRAM_NAME = "programName";
    public static final String PROPERTY_VERSION_STRING = "versionString";
    public static final String PROPERTY_BUILD_STATE = "buildState";
    public static final String PROPERTY_CHECK_ON_STARTUP = "checkOnStartup";
    public static final String XMLTAG_ROOT = "updaterConfiguration";
    private static Map<String, String> UPDATER_SETTINGS = new HashMap<>();
    private static Map<String, String> DEFAULT_UPDATER_SETTINGS = new HashMap<>();
    public static String get(String property) {
        return(UPDATER_SETTINGS.get(property));
    }
    public static String getDefault(String property) {
        return(DEFAULT_UPDATER_SETTINGS.get(property));
    }
    public static void set(String property, String value) {
        UPDATER_SETTINGS.put(property, value);
    }
    public static void defineDefaultSettings(HashMap<String, String> defaultSettings) {
        DEFAULT_UPDATER_SETTINGS = defaultSettings;
        if (UPDATER_SETTINGS.isEmpty()) {
            UPDATER_SETTINGS = new HashMap<>(defaultSettings);
        }
    }
    public static void loadSettings(File settingsXMLFile) throws IOException, DocumentException {
        if (!settingsXMLFile.exists()) {
            Logger.log(Updater.UPDATER_LEVEL_WARNING, "Updater configuration file not found!");
            Logger.log(Updater.UPDATER_LEVEL_INFO, "Creating new configuration file...");
            settingsXMLFile.getParentFile().mkdirs();
            settingsXMLFile.createNewFile();
            Logger.log(Updater.UPDATER_LEVEL_INFO, "Updater configuration file successfully created at " + settingsXMLFile.getCanonicalPath());
            return;
        }
        SAXReader sAXReader = new SAXReader();
        Document formattedDocument = sAXReader.read(settingsXMLFile);
        OutputFormat outputFormat = OutputFormat.createCompactFormat();
        StringWriter stringWriter = new StringWriter();
        XMLWriter xMLWriter = new XMLWriter(stringWriter, outputFormat);
        xMLWriter.write(formattedDocument);
        Document unformattedDocument = DocumentHelper.parseText(stringWriter.toString());
        Element root = unformattedDocument.getRootElement();
        for (Iterator i = root.elementIterator(); i.hasNext();) {
            Element element = (Element)i.next();
            UPDATER_SETTINGS.put(element.getName(), element.getText());
        }
        Logger.log(Updater.UPDATER_LEVEL_INFO, "Loaded updater configuration from file: " + settingsXMLFile.getCanonicalPath());
    }
    public static void saveSettings(File settingsXMLFile) throws IOException {
        Document xMLDocument = DocumentHelper.createDocument();
        Element rootElement = xMLDocument.addElement(XMLTAG_ROOT);
        for (Map.Entry keyPair : UPDATER_SETTINGS.entrySet()) {
            rootElement.addElement((String)keyPair.getKey()).addText((String)keyPair.getValue());
        }
        OutputFormat outputFormat = OutputFormat.createPrettyPrint();
        outputFormat.setIndentSize(4);
        XMLWriter xMLWriter = new XMLWriter(new FileWriter(settingsXMLFile), outputFormat);
        xMLWriter.write(xMLDocument);
        xMLWriter.close();
        Logger.log(Updater.UPDATER_LEVEL_INFO, "Updater configuration saved to file: " + settingsXMLFile.getCanonicalPath());
    }
}