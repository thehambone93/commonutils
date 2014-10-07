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

package weshampson.commonutils.jar;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import weshampson.commonutils.logging.Level;
import weshampson.commonutils.logging.Logger;

/**
 *
 * @author  Wes Hampson
 * @version 0.3.0 (Sep 16, 2014)
 * @since   0.2.0 (Sep 5, 2014)
 */
public class JarProperties {
    private static Class sourceClass = JarProperties.class;
    private static String jarPropertiesResourcePath = "META-INF/jar.properties";
    public static String getApplicationTitle() {
        try {
            Properties jarProperties = loadProperties();
            if (jarProperties.isEmpty()) {
                return("<null>");
            } else {
                String version = jarProperties.getProperty("application.title");
                return(version);
            }
        } catch (IOException ex) {
            Logger.log(Level.ERROR, ex, "Failed to load jar properties - " + ex.toString());
            return("<null>");
        }
    }
    public static String getApplicationVersion() {
        try {
            Properties jarProperties = loadProperties();
            if (jarProperties.isEmpty()) {
                return("<null>");
            } else {
                String version = jarProperties.getProperty("application.version");
                return(version);
            }
        } catch (IOException ex) {
            Logger.log(Level.ERROR, ex, "Failed to load jar properties - " + ex.toString());
            return("<null>");
        }
    }
    public static Date getBuildDate() {
        try {
            Properties jarProperties = loadProperties();
            if (jarProperties.isEmpty()) {
                return(new Date(0));
            } else {
                Date buildDate = new SimpleDateFormat(jarProperties.getProperty("build.timestamp.format")).parse(jarProperties.getProperty("build.timestamp"));
                return(buildDate);
            }
        } catch (IOException ex) {
            Logger.log(Level.ERROR, ex, "Failed to load jar properties - " + ex.toString());
            return(new Date(0));
        } catch (ParseException ex) {
            Logger.log(Level.ERROR, ex, null);
            return(new Date(0));
        }
    }
    public static int getBuildNumber() {
        try {
            Properties jarProperties = loadProperties();
            if (jarProperties.isEmpty()) {
                return(-1);
            } else {
                int buildNumber = Integer.parseInt(jarProperties.getProperty("build.number"));
                return(buildNumber);
            }
        } catch (IOException ex) {
            Logger.log(Level.ERROR, ex, "Failed to load jar properties - " + ex.toString());
            return(-1);
        }
    }
    public static String getJarPropertiesResourcePath() {
        return(jarPropertiesResourcePath);
    }
    public String getProperty(String key) {
        try {
            Properties jarProperties = loadProperties();
            return(jarProperties.getProperty(key));
        } catch (IOException ex) {
            Logger.log(Level.ERROR, ex, "Failed to load jar properties - " + ex.toString());
            return(null);
        }
    }
    public static Class getSourceClass() {
        return(sourceClass);
    }
    public static void setJarPropertiesResourcePath(String resourcePath) {
        jarPropertiesResourcePath = resourcePath;
    }
    public static void setSourceClass(Class c) {
        sourceClass = c;
    }
    private static Properties loadProperties() throws IOException {
        InputStream inputStream = sourceClass.getClassLoader().getResourceAsStream(jarPropertiesResourcePath);
        Properties jarProperties = new Properties();
        if (inputStream != null) {
            jarProperties.load(inputStream);
        }
        return(jarProperties);
    }
}