/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009 Red Hat Middleware, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.scripts.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

/**
 * Unit tests of wsprovide.sh and wsprovide.bat.
 * 
 * @author Rostislav Svoboda
 * @version $Revision: $
 */
public class WsprovideTestCase extends ScriptsTestBase {

    private static final URL classesDirUrl = Thread.currentThread().getContextClassLoader().getResource("");
    private static final URL scriptsDirUrl = Thread.currentThread().getContextClassLoader().getResource("scripts");

    /**
     * Create a new WsprovideTestCase.
     *
     * @param name
     */
    public WsprovideTestCase(String name) {
        super(name);
    }

    /**
     * Prints out some basic info about the environment
     */
    public void testExecutionEnvironment() {
        String os = isWindows() ? "Windows" : "non-Windows";
        // dump out some basic config information
        System.out.println("Testing run on " + os + " host");
        System.out.println("Working directory: " + getBinDir());
        System.out.println("Dist directory: " + getDistDir());
        System.out.println("Log directory: " + getLogDir());
        System.out.println("Server config: " + getServerConfig());
        System.out.println("CXF installed: " + isCXFInstalled());
    }

    /**
     * Tests if IPv4 is forced to be used
     */
    public void testIPv4() {
        String suffix = isWindows() ? ".bat" : ".sh";
        String ipv4Text = "-Djava.net.preferIPv4Stack=true";
        File file = new File(getBinDir() + FS + "wsprovide" + suffix);

        if (!fileContainsText(file, ipv4Text)) {
            fail("File " + file.getAbsolutePath()  + " doesn't contain '" + ipv4Text + "' text");
        }
    }

    /**
     * Tests run "help" command (no args)
     *
     * @throws Exception
     */
    public void testNoArgs() throws Exception {
        String command = "wsprovide";
        String options = null;
        String args = "-h";
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        getShellScriptExecutor().assertOnOutputStream("usage: WSProvide", "usage string not found in command output:\n" + getShellScriptExecutor().getOutput());
    }

    /**
     * Tests basic command to generate request and response classes
     *
     * @throws Exception
     */
    public void testGenerateRequestResponseClasses() throws Exception {
        File outDir = new File(scriptsDirUrl.getFile() + FS + "wsprovide-basic-out");
        if (!outDir.exists()) {
            outDir.mkdir();
        }
        String command = "wsprovide";
        String options = "-c " + classesDirUrl.getFile() + " -o " + outDir.getAbsolutePath();
        String args = "org.jboss.test.scripts.support.HelloJavaBean";
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        if (isCXFInstalled()) {
            getShellScriptExecutor().assertOnOutputStream(args, "'" + args + "' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("java2ws", "'java2ws' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        } else {
            getShellScriptExecutor().assertOnOutputStream("Output directory:", "'Output directory:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("Writing Classes:", "'Writing Classes:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        }
        //check files
        File echoClassFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "scripts" + FS + "support" + FS + "jaxws" + FS + "EchoString.class");
        assertTrue(echoClassFile.getAbsolutePath() + " doesn't exist", echoClassFile.exists());
    }

    /**
     * Tests basic command to generate WSDL file
     *
     * @throws Exception
     */
    public void testGenerateWSDL() throws Exception {
        File outDir = new File(scriptsDirUrl.getFile() + FS + "wsprovide-wsdl-out");
        if (!outDir.exists()) {
            outDir.mkdir();
        }
        String command = "wsprovide";
        String options = "-w -c " + classesDirUrl.getFile() + " -o " + outDir.getAbsolutePath();
        String args = "org.jboss.test.scripts.support.HelloJavaBean";
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        if (!isCXFInstalled()) {
            getShellScriptExecutor().assertOnOutputStream("Output directory:", "'Output directory:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("Generating WSDL:", "'Generating WSDL:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        }
        //check files
        File wsdlFile = new File(outDir.getAbsolutePath() + FS + "HelloService.wsdl");
        assertTrue(wsdlFile.getAbsolutePath() + " doesn't exist", wsdlFile.exists());
    }

    /**
     * Tests basic command to generate WSDL file with SOAP 1.2 binding extension
     *
     * @throws Exception
     */
    public void testGenerateWSDLWithExtension() throws Exception {
        File outDir = new File(scriptsDirUrl.getFile() + FS + "wsprovide-wsdl-ext-out");
        if (!outDir.exists()) {
            outDir.mkdir();
        }
        String command = "wsprovide";
        String options = "-w -e -c " + classesDirUrl.getFile() + " -o " + outDir.getAbsolutePath();
        String args = "org.jboss.test.scripts.support.HelloJavaBean";
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        //check files
        File wsdlFile = new File(outDir.getAbsolutePath() + FS + "HelloService.wsdl");
        assertTrue(wsdlFile.getAbsolutePath() + " doesn't exist", wsdlFile.exists());

        final BufferedReader inStream = new BufferedReader(new FileReader(wsdlFile));
        String line = inStream.readLine();
        boolean containsExtension = false;
        while (line != null && !containsExtension) {
            containsExtension = line.contains("soap12");
            line = inStream.readLine();
        }
        assertTrue(wsdlFile.getAbsolutePath() + " doesn't doesn't contain SOAP 1.2 "
                + "binding extension", containsExtension);
    }

    /**
     * Tests basic command to generate and keep source files
     *
     * @throws Exception
     */
    public void testKeepSources() throws Exception {
        File outDir = new File(scriptsDirUrl.getFile() + FS + "wsprovide-keep-sources-out");
        if (!outDir.exists()) {
            outDir.mkdir();
        }
        String command = "wsprovide";
        String options = "-k -c " + classesDirUrl.getFile() + " -o " + outDir.getAbsolutePath();
        String args = "org.jboss.test.scripts.support.HelloJavaBean";
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        if (!isCXFInstalled()) {
            getShellScriptExecutor().assertOnOutputStream("Output directory:", "'Output directory:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("Writing Source:", "'Writing Source:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("Writing Classes:", "'Writing Classes:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        }
        //check files
        File echoClassFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "scripts" + FS + "support" + FS + "jaxws" + FS + "EchoString.class");
        File echoJavaFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "scripts" + FS + "support" + FS + "jaxws" + FS + "EchoString.java");
        assertTrue(echoClassFile.getAbsolutePath() + " doesn't exist", echoClassFile.exists());
        assertTrue(echoJavaFile.getAbsolutePath() + " doesn't exist", echoJavaFile.exists());
    }

    /**
     * Tests command to generate WSDL, support classes and keep source files.
     * Each in separated directory.
     *
     * @throws Exception
     */
    public void testGenerateAllSeparated() throws Exception {
        File outClassesDir = new File(scriptsDirUrl.getFile() + FS + "wsprovide-separate-classes-out");
        File outSourcesDir = new File(scriptsDirUrl.getFile() + FS + "wsprovide-separate-sources-out");
        File outResourcesDir = new File(scriptsDirUrl.getFile() + FS + "wsprovide-separate-resources-out");
        if (!outClassesDir.exists()) {
            outClassesDir.mkdir();
        }
        if (!outSourcesDir.exists()) {
            outSourcesDir.mkdir();
        }
        if (!outResourcesDir.exists()) {
            outResourcesDir.mkdir();
        }

        String command = "wsprovide";
        String options = "-k -w -c " + classesDirUrl.getFile()
                + " -o " + outClassesDir.getAbsolutePath()
                + " -r " + outResourcesDir.getAbsolutePath()
                + " -s " + outSourcesDir.getAbsolutePath();
        String args = "org.jboss.test.scripts.support.HelloJavaBean";
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        if (!isCXFInstalled()) {
            getShellScriptExecutor().assertOnOutputStream("Output directory:", "'Output directory:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("Writing Source:", "'Writing Source:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("Writing Classes:", "'Writing Classes:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("Generating WSDL:", "'Generating WSDL:' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        }
        //check files
        File echoClassFile = new File(outClassesDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "scripts" + FS + "support" + FS + "jaxws" + FS + "EchoString.class");
        File echoJavaFile = new File(outSourcesDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "scripts" + FS + "support" + FS + "jaxws" + FS + "EchoString.java");
        File wsdlFile = new File(outResourcesDir.getAbsolutePath() + FS + "HelloService.wsdl");

        assertTrue(echoClassFile.getAbsolutePath() + " doesn't exist", echoClassFile.exists());
        assertTrue(echoJavaFile.getAbsolutePath() + " doesn't exist", echoJavaFile.exists());
        assertTrue(wsdlFile.getAbsolutePath() + " doesn't exist", wsdlFile.exists());
    }

    /**
     * Tests basic command to generate support classes from non-existing class
     *
     * @throws Exception
     */
    public void testNonExistingClass() throws Exception {
        String command = "wsprovide";
        String options = "-k -c " + classesDirUrl.getFile();
        String args = "org.jboss.test.scripts.support.NonExistingBean";
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);
        assertTrue("'Could not load class' string not found in error output",
                (getShellScriptExecutor().getError().indexOf("Could not load class") != -1));
    }
}
