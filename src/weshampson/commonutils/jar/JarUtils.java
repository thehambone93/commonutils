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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import weshampson.commonutils.logging.Level;
import weshampson.commonutils.logging.Logger;

/**
 *
 * @author  Wes Hampson
 * @version 0.3.0 (Sep 20, 2014)
 * @since   0.3.0 (Sep 20, 2014)
 */
public class JarUtils {
    public static File extractResource(String src, String dest) throws IOException {
        File destFile = new File(dest);
        destFile.getParentFile().mkdirs();
        InputStream rIn = JarUtils.class.getClassLoader().getResourceAsStream(src);
        if (rIn == null) {
            throw new IOException("failed to read resource as stream - " + src);
        }
        FileOutputStream fOut = new FileOutputStream(destFile);
        byte[] buffer = new byte[256];
        int bytesRead = 0;
        while ((bytesRead = rIn.read(buffer)) != -1) {
            fOut.write(buffer, 0, bytesRead);
        }
        fOut.close();
        rIn.close();
        Logger.log(Level.INFO, "Extracted resource " + src + " to " + destFile.getAbsolutePath());
        return(destFile);
    }
}
