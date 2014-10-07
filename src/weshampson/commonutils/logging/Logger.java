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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jline.console.ConsoleReader;
import weshampson.commonutils.io.DocumentOutputStream;

/**
 *
 * @author  Wes Hampson
 * @version 0.3.0 (Sep 27, 2014)
 * @since   0.1.0 (Aug 25, 2014)
 */
public abstract class Logger {
    public static final int STREAM_STDOUT = 0;
    public static final int STREAM_STDERR = 1;
    public static final PrintWriter DEFAULT_STDOUT = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FileDescriptor.out), Charset.forName("UTF-8")));
    public static final PrintWriter DEFAULT_STDERR = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FileDescriptor.err), Charset.forName("UTF-8")));
    private static Logger currentLogger = new Logger(DEFAULT_STDOUT, DEFAULT_STDERR) {
        @Override
        public void print(Level level, String message, boolean writePrefix, boolean newLine) {
            PrintWriter pw;
            if (level.getStream() == STREAM_STDERR) {
                pw = getStderr();
            } else {
                pw = getStdout();
            }
            if (writePrefix) {
                pw.print(parsePrefix(level, level.getPrefix()));
            }
            pw.print(message);
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
}