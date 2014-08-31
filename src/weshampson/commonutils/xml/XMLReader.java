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

package weshampson.commonutils.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 *
 * @author  Wes Hampson
 * @version 0.1.0 (Aug 31, 2014)
 * @since   0.1.0 (Aug 31, 2014)
 */
public class XMLReader {
    public static Document read(InputStream inputStream) throws IOException, DocumentException {
        SAXReader sAXReader = new SAXReader();
        Document formattedDocument = sAXReader.read(inputStream);
        OutputFormat outputFormat = OutputFormat.createCompactFormat();
        StringWriter stringWriter = new StringWriter();
        XMLWriter xMLWriter = new XMLWriter(stringWriter, outputFormat);
        xMLWriter.write(formattedDocument);
        Document unformattedDocument = DocumentHelper.parseText(stringWriter.toString());
        return(unformattedDocument);
    }
    public static Document read(File xMLFile) throws FileNotFoundException, IOException, DocumentException {
        return(read(new FileInputStream(xMLFile)));
    }
}