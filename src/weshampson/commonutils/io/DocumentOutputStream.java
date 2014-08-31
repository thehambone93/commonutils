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

package weshampson.commonutils.io;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;

/**
 *
 * @author  Wes Hampson
 * @version 0.1.0 (Aug 26, 2014)
 * @since   0.1.0 (Aug 26, 2014)
 */
public class DocumentOutputStream extends OutputStream {
    private final Document doc;
    private final JTextComponent textComponent;
    private SimpleAttributeSet attributeSet;
    public DocumentOutputStream(JTextComponent textComponent) {
        this.textComponent = textComponent;
        this.doc = textComponent.getDocument();
    }
    public DocumentOutputStream(JTextPane textPane) {
        this.textComponent = textPane;
        this.doc = textPane.getStyledDocument();
    }
    public Document getDocument() {
        return(doc);
    }
    public SimpleAttributeSet getDocumentAttributeSet() {
        return(attributeSet);
    }
    public JTextComponent getTextComponent() {
        return(textComponent);
    }
    public void setDocumentAttributeSet(SimpleAttributeSet attributeSet) {
        this.attributeSet = attributeSet;
    }
    @Override
    public void write(int b) throws IOException {
        try {
            doc.insertString(doc.getLength(), String.valueOf(b), attributeSet);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            doc.insertString(doc.getLength(), new String(b, off, len), attributeSet);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
}
