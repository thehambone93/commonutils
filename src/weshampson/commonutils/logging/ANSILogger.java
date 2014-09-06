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

import java.io.PrintWriter;
import org.fusesource.jansi.AnsiConsole;
import weshampson.commonutils.ansi.ANSI;
import weshampson.commonutils.io.DocumentOutputStream;

/**
 *
 * @author  Wes Hampson
 * @version 0.2.0 (Sep 6, 2014)
 * @since   0.2.0 (Sep 5, 2014)
 */
public class ANSILogger extends Logger {
    public ANSILogger() {
        super(new PrintWriter(AnsiConsole.out), new PrintWriter(AnsiConsole.err));
    }
    @Override
    public void print(Level level, String message, boolean writePrefix, boolean newLine) {
        PrintWriter pw;
        if (level.getStream() == STREAM_STDERR) {
            pw = getStderr();
        } else {
            pw = getStdout();
        }
        if (isColorEnabled()) {
            pw.print(level.getConsoleColor().getANSIEscapeSequence());
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
}