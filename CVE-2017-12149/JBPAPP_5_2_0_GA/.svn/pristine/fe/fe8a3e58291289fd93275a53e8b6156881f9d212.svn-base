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

import java.io.File;
import java.net.URL;

/**
 * Unit tests of wsconsume.sh and wsconsume.bat.
 *    
 * @author Rostislav Svoboda
 * @version $Revision: $
 */
public class WsconsumeTestCase extends ScriptsTestBase {

    private static final URL wsdlFileUrl = Thread.currentThread().getContextClassLoader().getResource("scripts/BenchmarkWebService.wsdl");
    private static final URL scriptsDirUrl = Thread.currentThread().getContextClassLoader().getResource("scripts");

    /**
     * Create a new WsconsumeTestCase.
     *
     * @param name
     */
    public WsconsumeTestCase(String name) {
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
        File file = new File(getBinDir() + FS + "wsconsume" + suffix);

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
        String command = "wsconsume";
        String options = null;
        String args = "-h";
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        getShellScriptExecutor().assertOnOutputStream("usage: org.jboss.wsf.spi.tools.cmd.WSConsume",
                "usage string not found in command output:\n" + getShellScriptExecutor().getOutput());
    }

    /**
     * Tests generate classes command
     *
     * @throws Exception
     */
    public void testGenerateClasses() throws Exception {
        File outDir = new File(scriptsDirUrl.getFile() + FS + "wsconsume-classes-out");
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        String command = "wsconsume";
        // original package was org.jboss.test.ws.benchmark.jaxws.doclit
        String options = "-p org.jboss.test.script -o " + outDir.getAbsolutePath();
        String args = wsdlFileUrl.getFile();
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        if (isCXFInstalled()) {
            getShellScriptExecutor().assertOnOutputStream("wsdl2java -compile", "'wsdl2java -compile' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        } else {
            getShellScriptExecutor().assertOnOutputStream("parsing WSDL...", "'parsing WSDL...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("generating code...", "'generating code...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("compiling code...", "'compiling code...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        }
        //check files
        File customerClassFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "Customer.class");
        File getOrderClassFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "GetOrder.class");
        File customerJavaFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "Customer.java");
        File getOrderJavaFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "GetOrder.java");

        assertTrue(customerClassFile.getAbsolutePath() + " doesn't exist", customerClassFile.exists());
        assertTrue(getOrderClassFile.getAbsolutePath() + " doesn't exist", getOrderClassFile.exists());
        assertFalse(customerJavaFile.getAbsolutePath() + " exists", customerJavaFile.exists());
        assertFalse(getOrderJavaFile.getAbsolutePath() + " exists", getOrderJavaFile.exists());
    }

    /**
     * Tests generate only sources command
     *
     * If there are "-n" and "-s" flag, without "-k", the generated artifacts should be placed in output directory
     * For details see JBWS-3193 and http://fisheye.jboss.org/changelog/JBossWS/?cs=13533
     *
     * @throws Exception
     */
    public void testGenerateSources() throws Exception {
        File outDir = new File(scriptsDirUrl.getFile() + FS + "wsconsume-sources-out");
        File srcDir = new File(scriptsDirUrl.getFile() + FS + "wsconsume-sources-src");
        if (!outDir.exists()) {
            outDir.mkdir();
        }
        if (!srcDir.exists()) {
            srcDir.mkdir();
        }

        String command = "wsconsume";
        String options = "-n -p org.jboss.test.script -s " + srcDir.getAbsolutePath() + " -o " + outDir.getAbsolutePath();
        String args = wsdlFileUrl.getFile();
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        if (isCXFInstalled()) {
            getShellScriptExecutor().assertOnOutputStream("wsdl2java", "'wsdl2java' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        } else {
            getShellScriptExecutor().assertOnOutputStream("parsing WSDL...", "'parsing WSDL...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("generating code...", "'generating code...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            assertTrue("'compiling code...' string found in command output",
                    (getShellScriptExecutor().getOutput().indexOf("compiling code...") == -1));
        }
        //check files
        final String commonOutPart = outDir.getAbsolutePath() + FS + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS;
        final String commonSrcPart = srcDir.getAbsolutePath() + FS + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS;
        File customerClassFile = new File(commonOutPart + "Customer.class");
        File getOrderClassFile = new File(commonOutPart + "GetOrder.class");
        File outDirCustomerJavaFile = new File(commonOutPart + "Customer.java");
        File outDirGetOrderJavaFile = new File(commonOutPart + "GetOrder.java");
        File srcDirCustomerJavaFile = new File(commonSrcPart + "Customer.java");
        File srcDirGetOrderJavaFile = new File(commonSrcPart + "GetOrder.java");

        assertFalse(customerClassFile.getAbsolutePath() + " exists", customerClassFile.exists());
        assertFalse(getOrderClassFile.getAbsolutePath() + " exists", getOrderClassFile.exists());
        assertFalse(srcDirCustomerJavaFile.getAbsolutePath() + " exists", srcDirCustomerJavaFile.exists());
        assertFalse(srcDirGetOrderJavaFile.getAbsolutePath() + " exists", srcDirGetOrderJavaFile.exists());
        assertTrue(outDirCustomerJavaFile.getAbsolutePath() + " doesn't exist", outDirCustomerJavaFile.exists());
        assertTrue(outDirGetOrderJavaFile.getAbsolutePath() + " doesn't exist", outDirGetOrderJavaFile.exists());
    }

    /**
     * Tests generate only sources command with keep parameter
     *
     * @throws Exception
     */
    public void testGenerateSourcesKeep() throws Exception {
        File outDir = new File(scriptsDirUrl.getFile() + FS + "wsconsume-sources-keep-out");
        File srcDir = new File(scriptsDirUrl.getFile() + FS + "wsconsume-sources-keep-src");
        if (!outDir.exists()) {
            outDir.mkdir();
        }
        if (!srcDir.exists()) {
            srcDir.mkdir();
        }

        String command = "wsconsume";
        String options = "-n -k -p org.jboss.test.script -s " + srcDir.getAbsolutePath() + " -o " + outDir.getAbsolutePath();
        String args = wsdlFileUrl.getFile();
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        if (isCXFInstalled()) {
            getShellScriptExecutor().assertOnOutputStream("wsdl2java", "'wsdl2java' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        } else {
            getShellScriptExecutor().assertOnOutputStream("parsing WSDL...", "'parsing WSDL...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("generating code...", "'generating code...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            assertTrue("'compiling code...' string found in command output",
                    (getShellScriptExecutor().getOutput().indexOf("compiling code...") == -1));
        }
        //check files
        final String commonOutPart = outDir.getAbsolutePath() + FS + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS;
        final String commonSrcPart = srcDir.getAbsolutePath() + FS + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS;
        File customerClassFile = new File(commonOutPart + "Customer.class");
        File getOrderClassFile = new File(commonOutPart + "GetOrder.class");
        File srcDirCustomerJavaFile = new File(commonSrcPart + "Customer.java");
        File srcDirGetOrderJavaFile = new File(commonSrcPart + "GetOrder.java");

        assertFalse(customerClassFile.getAbsolutePath() + " exists", customerClassFile.exists());
        assertFalse(getOrderClassFile.getAbsolutePath() + " exists", getOrderClassFile.exists());
        assertTrue(srcDirCustomerJavaFile.getAbsolutePath() + " doesn't exist", srcDirCustomerJavaFile.exists());
        assertTrue(srcDirGetOrderJavaFile.getAbsolutePath() + " doesn't exist", srcDirGetOrderJavaFile.exists());
    }

    /**
     * Tests generate only sources command
     *
     * @throws Exception
     */
    public void testKeepSources() throws Exception {
        File outDir = new File(scriptsDirUrl.getFile() + FS + "wsconsume-sources-keep-out");
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        String command = "wsconsume";
        String options = "-k -p org.jboss.test.script -s " + outDir.getAbsolutePath();
        String args = wsdlFileUrl.getFile();
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        if (isCXFInstalled()) {
            getShellScriptExecutor().assertOnOutputStream("wsdl2java", "'wsdl2java' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        } else {
            getShellScriptExecutor().assertOnOutputStream("parsing WSDL...", "'parsing WSDL...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("generating code...", "'generating code...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("compiling code...", "'compiling code...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        }
        //check files
        File customerJavaFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "Customer.java");
        File getOrderJavaFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "GetOrder.java");
        assertTrue(customerJavaFile.getAbsolutePath() + " doesn't exist", customerJavaFile.exists());
        assertTrue(getOrderJavaFile.getAbsolutePath() + " doesn't exist", getOrderJavaFile.exists());
    }

    /**
     * Tests generate classes and sources command
     *
     * @throws Exception
     */
    public void testGenerateBoth() throws Exception {
        File outDir = new File(scriptsDirUrl.getFile() + FS + "wsconsume-both-out");
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        String command = "wsconsume";
        String options = "-k -p org.jboss.test.script -o " + outDir.getAbsolutePath();
        String args = wsdlFileUrl.getFile();
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        if (!isCXFInstalled()) {
            getShellScriptExecutor().assertOnOutputStream("parsing WSDL...", "'parsing WSDL...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("generating code...", "'generating code...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("compiling code...", "'compiling code...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        }
        //check files
        File customerClassFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "Customer.class");
        File getOrderClassFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "GetOrder.class");
        File customerJavaFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "Customer.java");
        File getOrderJavaFile = new File(outDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "GetOrder.java");

        assertTrue(customerClassFile.getAbsolutePath() + " doesn't exist", customerClassFile.exists());
        assertTrue(getOrderClassFile.getAbsolutePath() + " doesn't exist", getOrderClassFile.exists());
        assertTrue(customerJavaFile.getAbsolutePath() + " doesn't exist", customerJavaFile.exists());
        assertTrue(getOrderJavaFile.getAbsolutePath() + " doesn't exist", getOrderJavaFile.exists());
    }

    /**
     * Tests generate classes and sources command, each in separate directory
     *
     * @throws Exception
     */
    public void testGenerateBothSeparated() throws Exception {
        File outClassesDir = new File(scriptsDirUrl.getFile() + FS + "wsconsume-separate-classes-out");
        File outSourcesDir = new File(scriptsDirUrl.getFile() + FS + "wsconsume-separate-sources-out");
        if (!outClassesDir.exists()) {
            outClassesDir.mkdir();
        }
        if (!outSourcesDir.exists()) {
            outSourcesDir.mkdir();
        }

        String command = "wsconsume";
        String options = "-k -p org.jboss.test.script -o " + outClassesDir.getAbsolutePath()
                + " -s " + outSourcesDir.getAbsolutePath();
        String args = wsdlFileUrl.getFile();
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        if (!isCXFInstalled()) {
            getShellScriptExecutor().assertOnOutputStream("parsing WSDL...", "'parsing WSDL...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("generating code...", "'generating code...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
            getShellScriptExecutor().assertOnOutputStream("compiling code...", "'compiling code...' string not found in command output:\n" + getShellScriptExecutor().getOutput());
        }
        //check files
        File customerClassFile = new File(outClassesDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "Customer.class");
        File getOrderClassFile = new File(outClassesDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "GetOrder.class");
        File customerJavaFile = new File(outSourcesDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "Customer.java");
        File getOrderJavaFile = new File(outSourcesDir.getAbsolutePath() + FS
                + "org" + FS + "jboss" + FS + "test" + FS + "script" + FS + "GetOrder.java");

        assertTrue(customerClassFile.getAbsolutePath() + " doesn't exist", customerClassFile.exists());
        assertTrue(getOrderClassFile.getAbsolutePath() + " doesn't exist", getOrderClassFile.exists());
        assertTrue(customerJavaFile.getAbsolutePath() + " doesn't exist", customerJavaFile.exists());
        assertTrue(getOrderJavaFile.getAbsolutePath() + " doesn't exist", getOrderJavaFile.exists());
    }

    /**
     * Tests generate classes command when wsdl doesn't exist
     *
     * @throws Exception
     */
    public void testNonExistingWsdl() throws Exception {
        String command = "wsconsume";
        String options = "-v -p org.jboss.test.script ";
        String args = "NonExistingWsdl.wsdl";
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        if (isCXFInstalled()) {
            assertTrue("'Failed to invoke WSDLToJava' string not found in command output:\n" + getShellScriptExecutor().getOutput(),
                    getShellScriptExecutor().getOutput().indexOf("Failed to invoke WSDLToJava") != -1);
        } else {
            assertTrue("'Failed to parse the WSDL' string not found in command output",
                    (getShellScriptExecutor().getOutput().indexOf("Failed to parse the WSDL") != -1));
            assertTrue("'WsImport invocation failed' string not found in error output",
                    (getShellScriptExecutor().getError().indexOf("WsImport invocation failed") != -1));
        }
    }
}
