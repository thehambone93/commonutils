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

import java.io.IOException;
import java.io.PrintWriter;
import jline.console.ConsoleReader;
import weshampson.commonutils.ansi.ANSI;
import weshampson.commonutils.io.DocumentOutputStream;

/**
 *
 * @author  Wes Hampson
 * @version 0.1.0 (Aug 25, 2014)
 * @since   0.1.0 (Aug 25, 2014)
 */
public class JLineLogger extends Logger {
    public JLineLogger(ConsoleReader cr, PrintWriter stderr) {
        super(cr, stderr);
    }
    @Override
    public void print(Level level, String message, boolean writePrefix, boolean newLine) {
        try {
            ConsoleReader cr = getConsoleReader();
            StringBuilder sb = new StringBuilder();
            if (isColorEnabled()) {
                sb.append(level.getConsoleColor().getANSIEscapeSequence());
            }
            if (writePrefix) {
                sb.append(parsePrefix(level, level.getPrefix()));
            }
            sb.append(message);
            if (isColorEnabled()) {
                sb.append(ANSI.ANSI_NORMAL);
            }
            if (newLine) {
                sb.append("\n");
            }
            cr.killLine();
            String buf = cr.getCursorBuffer().buffer.toString();
            if (cr.getPrompt() != null) {
                cr.resetPromptLine("", "", 0);
                cr.print(sb.toString());
                cr.resetPromptLine(getConsoleReaderPrompt(), buf, buf.length());
            } else {
                cr.print(sb.toString());
            }
            if (isLoggingToDocumentEnabled()) {
                PrintWriter pw;
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
                PrintWriter pw;
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
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}