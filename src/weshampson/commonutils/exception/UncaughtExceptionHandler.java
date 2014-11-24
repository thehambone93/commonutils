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

package weshampson.commonutils.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import weshampson.commonutils.exception.gui.UncaughtExceptionDialog;
import weshampson.commonutils.logging.Level;
import weshampson.commonutils.logging.Logger;

/**
 *
 * @author  Wes Hampson
 * @version 0.2.0 (Sep 6, 2014)
 * @since   0.2.0 (Sep 5, 2014)
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    public static final int EXIT_CODE = 1;
    private boolean showDialog;
    public void showDialog(boolean bool) {
        this.showDialog = bool;
    }
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter stackTraceWriter = new PrintWriter(stringWriter);
        e.printStackTrace(stackTraceWriter);
        Logger.log(Level.ERROR, "Uncaught exception: " + e.toString() + "\nStack trace:\n" + stringWriter.toString());
        if (showDialog) {
            UncaughtExceptionDialog uncaughtExceptionDialog = new UncaughtExceptionDialog(null, true, e);
            uncaughtExceptionDialog.pack();
            uncaughtExceptionDialog.setLocationRelativeTo(null);
            uncaughtExceptionDialog.setVisible(true);
        }
        System.exit(EXIT_CODE);
    }
}