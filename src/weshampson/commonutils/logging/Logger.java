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

package weshampson.commonutils.logging;

import java.awt.Color;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import jline.console.ConsoleReader;
import weshampson.commonutils.ansi.ANSI;
import weshampson.commonutils.ansi.ANSIException;
import weshampson.commonutils.io.DocumentOutputStream;

/**
 *
 * @author  Wes Hampson
 * @version 0.1.0 (Aug 25, 2014)
 * @since   0.1.0 (Aug 25, 2014)
 */
public abstract class Logger {
    public static final int STREAM_STDOUT = 0;
    public static final int STREAM_STDERR = 1;
    public static final PrintWriter DEFAULT_STDOUT = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FileDescriptor.out), Charset.forName("UTF-8")));
    public static final PrintWriter DEFAULT_STDERR = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FileDescriptor.err), Charset.forName("UTF-8")));
    private static final SimpleAttributeSet DOCATTRS_INFO = new SimpleAttributeSet();
    private static final SimpleAttributeSet DOCATTRS_ERROR = new SimpleAttributeSet();
    private static final SimpleAttributeSet DOCATTRS_WARNING = new SimpleAttributeSet();
    
    private static Logger currentLogger = new Logger(DEFAULT_STDOUT, DEFAULT_STDERR) {
        @Override
        public void print(Level level, String message, boolean writePrefix, boolean newLine) {
            PrintWriter pw;
            if (level.getStream() == STREAM_STDERR) {
                pw = getStderr();
            } else {
                pw = getStdout();
            }
            if (isColorEnabled()) {
                pw.print(level.getConsoleColor().toString());
            }
            if (writePrefix) {
                pw.print(parsePrefix(level, level.getPrefix()));
            }
            pw.print(message);
            if (isColorEnabled()) {
                pw.print(ANSI.ANSI_NORMAL);
            }
            if (newLine) {
                pw.println();
            }
            pw.flush();
            if (isLoggingToDocumentEnabled()) {
                DocumentOutputStream documentOutputStream;
                if (level.getStream() == STREAM_STDERR) {
                    documentOutputStream = getDocumentErr();
                } else {
                    documentOutputStream = getDocumentOut();
                }
                documentOutputStream.setDocumentAttributeSet(level.getDocumentAttributeSet());
                pw = new PrintWriter(documentOutputStream);
                if (writePrefix) {
                    pw.print(parsePrefix(level, level.getPrefix()));
                }
                pw.print(message);
                if (newLine) {
                    pw.println();
                }
                pw.flush();
                documentOutputStream.getTextComponent().setCaretPosition(documentOutputStream.getDocument().getLength());
            }
            if (isLoggingToFileEnabled()) {
                if (level.getStream() == STREAM_STDERR) {
                    pw = new PrintWriter(getFileErr());
                } else {
                    pw = new PrintWriter(getFileOut());
                }
                if (writePrefix) {
                    pw.print(parsePrefix(level, level.getPrefix()));
                }
                pw.print(message);
                if (newLine) {
                    pw.println();
                }
                pw.flush();
            }
        }
    };
    private final PrintWriter stdout;
    private final PrintWriter stderr;
    private ConsoleReader consoleReader;
    private DocumentOutputStream documentOut;
    private DocumentOutputStream documentErr;
    private FileOutputStream fileOut;
    private FileOutputStream fileErr;
    private boolean isColorEnabled;
    private boolean isLoggingToDocumentEnabled;
    private boolean isLoggingToFileEnabled;
    private String consoleReaderPrompt;
    public Logger(PrintWriter stdout, PrintWriter stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
    }
    public Logger(ConsoleReader cr, PrintWriter stderr) {
        this.stdout = new PrintWriter(cr.getOutput());
        this.stderr = stderr;
        this.consoleReader = cr;
        this.consoleReaderPrompt = cr.getPrompt();
    }
    public abstract void print(Level level, String message, boolean writePrefix, boolean newLine);
    public static synchronized void log(Level level, String message) {
        currentLogger.print(level, message, true, true);
    }
    public static synchronized void log(Level level, String message, boolean writePrefix, boolean newLine) {
        currentLogger.print(level, message, writePrefix, newLine);
    }
    public static synchronized void log(Level level, Throwable t, String customMessage) {
        if (customMessage == null) {
            customMessage = t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        currentLogger.print(level, customMessage, true, true);
    }
    public static synchronized void log(Level level, Throwable t, String customMessage, boolean writePrefix, boolean newLine) {
        if (customMessage == null) {
            customMessage = t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        currentLogger.print(level, customMessage, writePrefix, newLine);
    }
    public static String parsePrefix(Level level, String text) {
        String prefix = level.getPrefix();
        prefix = prefix.replaceAll("%d", new Timestamp("MM-dd-YYYY").getTimestamp());
        prefix = prefix.replaceAll("%D", new Timestamp("dd-MMM-YYYY").getTimestamp());
        prefix = prefix.replaceAll("%s", text);
        prefix = prefix.replaceAll("%t", new Timestamp("hh:mm:ss a").getTimestamp());
        prefix = prefix.replaceAll("%T", new Timestamp("HH:mm:ss").getTimestamp());
        Pattern p = Pattern.compile("%[\\{].*[\\}]dt");
        Matcher m = p.matcher(prefix);
        if (m.find()) {
            p = Pattern.compile("\\{.*?\\}");
            m = p.matcher(prefix);
            if (m.find()) {
                String dtFormat = m.group(0).replaceAll("\\{|\\}", "");
                prefix = prefix.replaceAll("%[\\{].*[\\}]dt", new Timestamp(dtFormat).getTimestamp());
            }
        }
        return(prefix);
    }
    public static synchronized Logger getLogger() {
        return(currentLogger);
    }
    public static synchronized void setLogger(Logger logger) {
        currentLogger = logger;
    }
    public String getConsoleReaderPrompt() {
        return(consoleReaderPrompt);
    }
    public DocumentOutputStream getDocumentOut() {
        return(documentOut);
    }
    public DocumentOutputStream getDocumentErr() {
        return(documentErr);
    }
    public FileOutputStream getFileOut() {
        return(fileOut);
    }
    public FileOutputStream getFileErr() {
        return(fileErr);
    }
    public PrintWriter getStderr() {
        return(stderr);
    }
    public PrintWriter getStdout() {
        return(stdout);
    }
    public ConsoleReader getConsoleReader() {
        return(consoleReader);
    }
    public boolean isColorEnabled() {
        return(isColorEnabled);
    }
    public boolean isLoggingToDocumentEnabled() {
        return(isLoggingToDocumentEnabled);
    }
    public boolean isLoggingToFileEnabled() {
        return(isLoggingToFileEnabled);
    }
    public synchronized void setColorEnabled(boolean enabled) {
        isColorEnabled = enabled;
    }
    public synchronized void setConsoleReaderPrompt(String prompt) {
        consoleReaderPrompt = prompt;
        consoleReader.setPrompt(prompt);
    }
    public synchronized void setDocumentErr(DocumentOutputStream documentErr) {
        this.documentErr = documentErr;
    }
    public synchronized void setDocumentOut(DocumentOutputStream documentOut) {
        this.documentOut = documentOut;
    }
    public synchronized void setFileErr(FileOutputStream fileErr) {
        this.fileErr = fileErr;
    }
    public synchronized void setFileOut(FileOutputStream fileOut) {
        this.fileOut = fileOut;
    }
    public synchronized void setLoggingToDocumentEnabled(boolean enabled) {
        isLoggingToDocumentEnabled = enabled;
    }
    public synchronized void setLoggingToFileEnabled(boolean enabled) {
        isLoggingToFileEnabled = enabled;
    }
    public static enum Level {
        INFO(STREAM_STDOUT, "[%T INFO]: ", ANSI.Color.DEFAULT, DOCATTRS_INFO),
        ERROR(STREAM_STDERR, "[%T ERROR]: ", ANSI.Color.RED, DOCATTRS_ERROR),
        WARNING(STREAM_STDOUT, "[%T WARNING]: ", ANSI.Color.YELLOW, DOCATTRS_WARNING);
        static {
            StyleConstants.setForeground(DOCATTRS_INFO, Color.BLACK);
            StyleConstants.setForeground(DOCATTRS_ERROR, Color.RED);
            StyleConstants.setForeground(DOCATTRS_WARNING, Color.ORANGE);
        }
        private int streamDescriptor;
        private String prefix;
        private ANSI.Color consoleColor;
        private SimpleAttributeSet documentAttributeSet;
        private Level(int streamDescriptor, String prefix, ANSI.Color consoleColor, SimpleAttributeSet documentAttributeSet) {
            this.streamDescriptor = streamDescriptor;
            this.prefix = prefix;
            this.consoleColor = consoleColor;
            this.documentAttributeSet = documentAttributeSet;
        }
        public ANSI.Color getConsoleColor() {
            return(consoleColor);
        }
        public SimpleAttributeSet getDocumentAttributeSet() {
            return(documentAttributeSet);
        }
        public String getPrefix() {
            return(prefix);
        }
        public int getStream() {
            return(streamDescriptor);
        }
        public void setConsoleColor(ANSI.Color color) throws ANSIException {
            this.consoleColor = color;
        }
        public void setDocumentAttributeSet(SimpleAttributeSet documentAttributeSet) {
            this.documentAttributeSet = documentAttributeSet;
        }
        /**
         * Sets the text that will be printed before each logged item.
         * <p>
         * Special character sequences can be used to specify dynamic text.
         * <br>Character sequences include:
         * <br>&nbsp;%d - Date; MM-dd-YYYY
         * <br>&nbsp;%D - Date; dd-MMM-YYYY
         * <br>&nbsp;%s - String; any additional text that varies each time this
         * level is used
         * <br>&nbsp;%t - Timestamp; 12-hour format (hh:mm:ss a)
         * <br>&nbsp;%T - Timestamp; 24-hour format (HH:mm:ss)
         * <p>
         * Additionally, more advanced character sequences can be used to
         * specify a date and/or time with specific formatting:
         * <br>&nbsp;%{&#60format&#62}dt
         * <br>where &#60format&#62 is a simple date/time pattern string
         * <p>
         * Example: the prefix {@code [%T INFO]:} may result in the string
         * {@code [14:42:22 INFO]:}
         * @param prefix The text to be used as a prefix
         * @see java.text.SimpleDateFormat
         */
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
        public void setStream(int stream) {
            this.streamDescriptor = stream;
        }
    }
}