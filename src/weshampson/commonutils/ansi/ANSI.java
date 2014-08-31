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

package weshampson.commonutils.ansi;

/**
 *
 * @author  Wes Hampson
 * @version 0.1.0 (Aug 25, 2014)
 * @since   0.1.0 (Aug 25, 2014)
 */
public class ANSI {
    public static final String ANSI_NORMAL = "\u001b[0m";
    public static final String ANSI_DEFAULTFOREGROUND = "\u001b[39m";
    public static final String ANSI_DEFAULTBACKGROUND = "\u001b[49m";
    public static Color rGBToANSI(int r, int g, int b) throws ANSIException {
        int[] rGB = new int[] {r, g, b};
        int[] aNSI = new int[3];
        float[] hSB = java.awt.Color.RGBtoHSB(r, g, b, null);
        int bright = Math.round(hSB[2]);
        if (hSB[0] == 0.0 && hSB[1] == 0.0) {
            int rounded;
            if (bright == 1) {
                rounded = (int)(64.0 * Math.round((float)r / 64.0));
                if (rounded == 256) {
                    rounded -= 1;
                }
            } else {
                rounded = (int)(128.0 * Math.round((float)r / 128.0));
            }
            for (int i = 0; i < aNSI.length; i++) {
                aNSI[i] = rounded;
            }
        } else {
            for (int i = 0; i < aNSI.length; i++) {
                if (rGB[i] == 0) {
                    aNSI[i] = 0;
                } else if (bright == 1) {
                    aNSI[i] = (int)(255.0 * Math.round((float)rGB[i] / 255.0));
                } else {
                    aNSI[i] = (int)(128.0 * Math.round((float)rGB[i] / 128.0));
                }
            }
        }
        return(Color.getColor(aNSI[0] << 16 | aNSI[1] << 8 | aNSI[2]));
    }
    public static Color rGBToANSI(java.awt.Color c) throws ANSIException {
        return(rGBToANSI(c.getRed(), c.getGreen(), c.getBlue()));
    }
    public static enum Color {
        BLACK(0x000000, "\u001b[0;30;49m"),
        DARK_RED(0x800000, "\u001b[0;31;49m"),
        DARK_GREEN(0x008000, "\u001b[0;32;49m"),
        DARK_YELLOW(0x808000, "\u001b[0;33;49m"),
        DARK_BLUE(0x000080, "\u001b[0;34;49m"),
        DARK_MAGENTA(0x800080, "\u001b[0;35;49m"),
        DARK_CYAN(0x008080, "\u001b[0;36;49m"),
        GRAY(0xC0C0C0, "\u001b[0;37;49m"),
        DARK_GRAY(0x808080, "\u001b[1;30;49m"),
        DEFAULT(0, "\u001b[39;49m"),    // rgb value is invalid; it is impossible to know true value from console
        RED(0xFF0000, "\u001b[1;31;49m"),
        GREEN(0x00FF00, "\u001b[1;32;49m"),
        YELLOW(0xFFFF00, "\u001b[1;33;49m"),
        BLUE(0x0000FF, "\u001b[1;34;49m"),
        MAGENTA(0xFF00FF, "\u001b[1;35;49m"),
        CYAN(0x00FFFF, "\u001b[1;36;49m"),
        WHITE(0xFFFFFF, "\u001b[1;37;49m");
        private final int rGBValue;
        private final String escapeSequence;
        private Color(int rGBValue, String escapeSequence) {
            this.rGBValue = rGBValue;
            this.escapeSequence = escapeSequence;
        }
        public int getRGBValue() {
            return(rGBValue);
        }
        public String getANSIEscapeSequence() {
            return(escapeSequence);
        }
        @Override
        public String toString() {
            return(getANSIEscapeSequence());
        }
        public static Color getColor(int rGBValue) throws ANSIException {
            for (Color c : values()) {
                if (c.getRGBValue() == rGBValue) {
                    return(c);
                }
            }
            throw new ANSIException("ANSI color match not found for RGB value - " + rGBValue);
        }
    }
}