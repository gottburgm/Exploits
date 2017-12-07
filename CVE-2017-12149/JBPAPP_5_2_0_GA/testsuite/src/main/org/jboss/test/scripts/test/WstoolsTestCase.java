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
 * Unit tests of wstools.sh and wstools.bat.
 *
 * @author Rostislav Svoboda
 * @version $Revision: $
 */
public class WstoolsTestCase extends ScriptsTestBase {

    private static final URL configFileUrl = Thread.currentThread().getContextClassLoader().getResource("scripts/wstools-config.xml");
    private static final URL scriptsDirUrl = Thread.currentThread().getContextClassLoader().getResource("scripts");
    private static final URL classesDirUrl = Thread.currentThread().getContextClassLoader().getResource("");
    
    /**
     * Create a new WstoolsTestCase.
     *
     * @param name
     */
    public WstoolsTestCase(String name) {
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
        System.out.println("WSTools present: " + isWSToolsPresent());
    }

    private boolean isWSToolsPresent() {
        final File wstoolsFile = new File(getDistDir() + FS + "bin" + FS  + "wstools" + (isWindows() ? ".bat" : ".sh"));
        return wstoolsFile.exists();
    }

    /**
     * Tests if IPv4 is forced to be used
     */
    public void testIPv4() {
        if (isCXFInstalled() && !isWSToolsPresent()) {
            System.out.println("Installed CXF and no WSTools command line script as expected (EAP 5.1.x) ");
            return;
        }
        String suffix = isWindows() ? ".bat" : ".sh";
        String ipv4Text = "-Djava.net.preferIPv4Stack=true";
        File file = new File(getBinDir() + FS + "wstools" + suffix);

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
        if (isCXFInstalled() && !isWSToolsPresent()) {
            System.out.println("Installed CXF and no WSTools command line script as expected (EAP 5.1.x) ");
            return;
        }
        String command = "wstools";
        String options = null;
        String args = "-h";
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // check assertions
        getShellScriptExecutor().assertOnOutputStream("Usage: wstools", "usage string not found in command output");
    }

    /**
     * Tests generate classes command
     *
     * @throws Exception
     */
    public void testGenerateClasses() throws Exception {
        if (isCXFInstalled() && !isWSToolsPresent()) {
            System.out.println("Installed CXF and no WSTools command line script as expected (EAP 5.1.x) ");
            return;
        }
        File outDir = new File(scriptsDirUrl.getFile() + FS + "wstools-classes-out");
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        String command = "wstools";
        // original package was org.jboss.test.ws.benchmark.jaxws.doclit
        String options = "-classpath " + classesDirUrl.getFile() + " -config " + configFileUrl.getFile() +
                " -dest " + outDir.getAbsolutePath();
        String args = null;
        String[] shellCommand = getShellCommand(command, options, args);

        String[] envp = null;                       // set the environment if necessary
        File workingDir = new File(getBinDir());    // set the working directory
        getShellScriptExecutor().runShellCommand(shellCommand, envp, workingDir);

        // NO console output
        
        //check files
        File wsdlFile = new File(outDir.getAbsolutePath() + FS + "wsdl" + FS + "HelloService.wsdl");
        File webservicesFile = new File(outDir.getAbsolutePath() +  FS + "webservices.xml");
        File mappingFile = new File(outDir.getAbsolutePath() + FS + "jaxrpc-mapping.xml");
                
        assertTrue(wsdlFile.getAbsolutePath() + " doesn't exist", wsdlFile.exists());
        assertTrue(webservicesFile.getAbsolutePath() + " doesn't exist", webservicesFile.exists());
        assertTrue(mappingFile.getAbsolutePath() + " doesn't exist", mappingFile.exists());
    }
}