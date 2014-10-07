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
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import weshampson.commonutils.ansi.ANSI;
import weshampson.commonutils.ansi.ANSIException;
import static weshampson.commonutils.logging.Logger.STREAM_STDERR;
import static weshampson.commonutils.logging.Logger.STREAM_STDOUT;

/**
 *
 * @author  Wes Hampson
 * @version 0.3.0 (Sep 20, 2014)
 * @since   0.3.0 (Sep 20, 2014)
 */
public class Level {
    public static final SimpleAttributeSet DOCATTRS_INFO = new SimpleAttributeSet();
    public static final SimpleAttributeSet DOCATTRS_ERROR = new SimpleAttributeSet();
    public static final SimpleAttributeSet DOCATTRS_WARNING = new SimpleAttributeSet();
    static {
        StyleConstants.setForeground(DOCATTRS_INFO, Color.BLACK);
        StyleConstants.setForeground(DOCATTRS_ERROR, Color.RED);
        StyleConstants.setForeground(DOCATTRS_WARNING, Color.ORANGE);
    }
    public static final Level INFO = new Level(STREAM_STDOUT, "[%T INFO]: ", ANSI.Color.DEFAULT, DOCATTRS_INFO);
    public static final Level ERROR = new Level(STREAM_STDERR, "[%T ERROR]: ", ANSI.Color.RED, DOCATTRS_ERROR);
    public static final Level WARNING = new Level(STREAM_STDOUT, "[%T WARNING]: ", ANSI.Color.YELLOW, DOCATTRS_WARNING);
    private int streamDescriptor;
    private String prefix;
    private ANSI.Color consoleColor;
    private SimpleAttributeSet documentAttributeSet;
    public Level(int streamDescriptor, String prefix, ANSI.Color consoleColor, SimpleAttributeSet documentAttributeSet) {
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
