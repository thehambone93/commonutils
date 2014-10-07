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

package weshampson.commonutils.jvm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author  Wes Hampson
 * @version 0.3.0 (Sep 25, 2014)
 * @since   0.3.0 (Sep 25, 2014)
 */
public class JVMBuilder {
    public static void exec(String[] jVMArgs, String execFile) throws IOException, InterruptedException {
        invokeNewJVMInstance(constructExecCommand(jVMArgs, execFile));
    }
    private static void invokeNewJVMInstance(List<String> execCommand) throws IOException, InterruptedException {
        System.out.println("Invoking new JVM instance...");
        ProcessBuilder jVMInstance = new ProcessBuilder(execCommand);
        Process p = jVMInstance.start();
        int waitFor = p.waitFor();
    }
    private static String getJVMExecutable() {
        String javaHome = System.getProperty("java.home");
        String fileSeparator = System.getProperty("file.separator");
        String osName = System.getProperty("os.name");
        String jVMExecutable = javaHome + fileSeparator + "bin" + fileSeparator + "java";
        if (osName.startsWith("Win")) {
            jVMExecutable += ".exe";
        }
        return(jVMExecutable);
    }
    private static List<String> constructExecCommand(String[] jVMArgs, String execFile) {
        List<String> execCommand = new ArrayList<>();
        execCommand.add(getJVMExecutable());
        execCommand.addAll(Arrays.asList(jVMArgs));
        execCommand.add(execFile);
        System.out.println("cmd: " + execCommand);
        return(execCommand);
    }
}
