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
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import org.jboss.test.security.beans.TestPasswordInjectedBean;
import org.jboss.util.file.Files;
import javax.naming.InitialContext;
import javax.management.ObjectName;

/**
 * Unit tests of password_tool.sh / password_tool.bat
 *
 * @author Jan Martiska
 * @version $Revision: $
 */
public class PasswordToolTestCase extends ScriptsTestBase
{
   private ObjectName SERVER_OBJ_NAME = null;

   private final File WORKING_DIR = new File(getBinDir());
   private final File JBOSS_DIST_DIR = new File(getDistDir());

   private final String KEYSTORE_PASSWORD = "testpass";
   private final String ALIAS_NAME = "jboss";
   private final String DOMAIN_NAME = "test-bean";
   private final String DOMAIN_PASSWORD = "TaWg84T%$&207";

   private final int STOP_TIMEOUT = Integer.parseInt(System.getProperty(SYSTEM_PROPERTY_JBOSSAS_SHUTDOWN_TIMEOUT, "30"));
   private final int START_TIMEOUT = Integer.parseInt(System.getProperty(SYSTEM_PROPERTY_JBOSSAS_STARTUP_TIMEOUT, "120"));

   private final PrintStream sysOut = System.out;

   /**
    * Create a new PasswordToolTestCase.
    *
    * @param name
    */
   public PasswordToolTestCase(String name)
   {
      super(name);
   }

   /**
    * preparation before every testing method
    * Didn't use JUnit setUp method cause it was causing some problems
    */
   private void before() throws Exception
   {
      // delete the keystore if it already exists
      File keystore = new File(WORKING_DIR, "server.keystore");
      if (keystore.exists())
         keystore.delete();

      // delete directory "bin/password" and all of its contents
      File passwordDirectory = new File(WORKING_DIR, "password");
      if (passwordDirectory.exists())
         Files.delete(passwordDirectory);

      // create new keystore
      Process p = Runtime.getRuntime().exec(
            new String[] { "keytool", "-genkey", "-alias", ALIAS_NAME, "-keyalg", "RSA", "-keysize",
            "1024", "-keystore", "server.keystore", "-storepass", "testpass",
                  "-dname", "cn=JBoss Security,ou=JBoss Division,o=RedHatInc,l=Raleigh,st=NC,c=US",
                  "-keypass", "testpass" }, null, WORKING_DIR);
      BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      p.waitFor();

      assertTrue("keytool command should not return anything in stdout", in.readLine() == null);
      assertTrue("keytool command should not return anything in stderr", err.readLine() == null);
   }

   /**
    * cleanup after every testing method
    * Didn't use JUnit tearDown method cause it was causing some problems
    */
   private void after()
   {
      // delete directory "bin/password" and all of its contents
      File passwordDirectory = new File(WORKING_DIR, "password");
      if (passwordDirectory.exists())
         Files.delete(passwordDirectory);
      // delete file "bin/server.keystore"
      File keystore = new File(WORKING_DIR, "server.keystore");
      if (keystore.exists())
         keystore.delete();
   }

   /**
    * Tests option "0" = encrypt a keystore password
    * Command: echo 0 testpass alongsaltstring 12308 5 | ./password_tool.sh
    * The result: file $JBOSS_DIST/bin/password/jboss_keystore_pass.dat is created
    *
    * @throws Exception
    */
   public void testEncryptKeystorePassword() throws Exception
   {
      before();
      this.encryptKeystorePassword(KEYSTORE_PASSWORD);
      after();
   }

   /**
    * Tests option "1" = specify keystore location and alias
    * Command: echo 1 server.keystore jboss | ./password_tool.sh
    * This needs to be done before every command following, as the program
    * doesn't seem to remember the keystore location from one invocation
    * to the next
    *
    * @throws Exception
    */
   public void testSpecifyKeystoreLocation() throws Exception
   {
      before();
      this.encryptKeystorePassword(KEYSTORE_PASSWORD); // setup

      Process p = startPasswordTool("1 server.keystore " + ALIAS_NAME + " 5");
      BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String s;
      boolean failed = false;
      while (stdout.readLine() != null)
      {
      }
      while ((s = stderr.readLine()) != null)
      { //  the error output must not contain "Exception"
         if (s.indexOf("Exception") != -1)
            failed = true;
         if (failed)
            System.out.println(s);
      }
      p.waitFor();
      after();
      if (failed)
         fail("password_tool threw an exception while specifying keystore location");
   }

   /**
    * Tests option "1" = specify a keystore location and an INVALID alias = should FAIL
    * Command: echo 1 server.keystore foo | ./password_tool.sh
    *
    * @throws Exception
    */
   public void testSpecifyInvalidKeystoreAlias() throws Exception
   {
      before();
      this.encryptKeystorePassword(KEYSTORE_PASSWORD); // setup

      Process p = startPasswordTool("1 server.keystore " + ALIAS_NAME + "foo 5");
      BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
      while (stdout.readLine() != null)
      {
      }
      String s;
      // we search the error output for a substring: "Exception" -> one has to be thrown
      boolean found = false;
      while ((s = err.readLine()) != null)
      {
         if (s.indexOf("Exception") != -1)
            found = true;
      }
      p.waitFor();

      after();
      assertTrue("password_tool should raise an Exception if given an invalid keystore alias", found);
   }

   /**
    * Tests option "4" : checks for security domain password existence
    * Command: echo "1 server.keystore jboss 4 test-bean 5" | ./password_tool.sh
    * Result: "Exists = true" in the standard output
    *
    * @throws Exception
    */
   public void testCheckSecurityDomainExistence() throws Exception
   {
      before();
      this.encryptKeystorePassword(KEYSTORE_PASSWORD); // setup
      this.createSecurityDomain(DOMAIN_NAME, DOMAIN_PASSWORD); // setup
      this.checkSecurityDomainExistence(DOMAIN_NAME, true);
      after();
   }

   /**
    * Tests option "3": delete security domain password
    * Command: echo "1 server.keystore jboss 3 messaging 5" | ./password_tool.sh
    *
    * @throws Exception
    */
   public void testDeleteSecurityDomain() throws Exception
   {
      before();
      this.encryptKeystorePassword(KEYSTORE_PASSWORD); // setup
      this.createSecurityDomain(DOMAIN_NAME, DOMAIN_PASSWORD); // setup

      this.deleteSecurityDomain(DOMAIN_NAME);
      // test if the domain was really deleted
      this.checkSecurityDomainExistence(DOMAIN_NAME, false);
      after();
   }

   /**
    * simulates the test org.jboss.test.passwordinjection.test.PasswordInjectionUnitTestCase
    * @throws Exception
    */
   public void testPasswordInjection() throws Exception
   {
      before();

      final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
      System.setOut(new PrintStream(myOut));

      // generate new server.keystore and directory password, overwrite those in passwordtest config
      // a fresh server.keystore already exists. name it password.keystore
      this.encryptKeystorePassword(KEYSTORE_PASSWORD);
      this.createSecurityDomain(DOMAIN_NAME, DOMAIN_PASSWORD);

      // mkdir server/production/conf/password
      File passworddir = new File(JBOSS_DIST_DIR.getAbsolutePath() + FS + "server" +
              FS + "production" + FS + "conf" + FS + "password");
      try
      {
         if (!passworddir.exists())
            passworddir.mkdir();
      }
      catch (Exception e)
      {
         System.out.println("Exception: " + e.getMessage());
      }

      // copy bin/server.keystore -> server/production/conf/password/password.keystore
      File serverKeystore = new File(WORKING_DIR, "server.keystore");
      Files.copy(new File(WORKING_DIR.getAbsolutePath() + FS + "server.keystore"),
              new File(passworddir.getAbsolutePath() + FS + "password.keystore"));

      // cp  bin/password/jboss_keystore_pass.dat server/production/conf/password/jboss_keystore_pass.dat
      Files.copy(new File(WORKING_DIR.getAbsolutePath() + FS + "password" + FS + "jboss_keystore_pass.dat"),
              new File(passworddir.getAbsolutePath() + FS  + "jboss_keystore_pass.dat"));
      Files.copy(new File(WORKING_DIR.getAbsolutePath() + FS + "password" + FS + "jboss_password_enc.dat"),
              new File(passworddir.getAbsolutePath() + FS + "jboss_password_enc.dat"));

      // run the "production" configuration
      Exception fail = null;
      String[] shellCommand = getShellCommand("run", "-c production -b " + getServerHost(), null);
      String[] envp = null;
      try
      {
         // execute command
         getAsyncShellScriptExecutor().startShellCommand(shellCommand, envp, WORKING_DIR);
         // waitForServerStart kills the process and throws an exception if server does not start
         waitForServerStart(getAsyncShellScriptExecutor(), getServerHost(), START_TIMEOUT);
         System.out.println("Server started successfully");

         // run the contents of org.jboss.test.passwordinjection.test.PasswordInjectionUnitTestCase
         String passBeans = "test-password-jboss-beans.xml";
         String jarName = "passwordbean.jar";
         deploy(jarName);
         // deploy the Password Beans
         String url1 = getResourceURL("security/password-mask/" + passBeans);
         deploy(url1);

         InitialContext ic = new InitialContext();
         TestPasswordInjectedBean tp = (TestPasswordInjectedBean)ic.lookup("testJNDIBean");
         assertNotNull("Password Bean is in JNDI", tp);
         assertTrue("Password has not been injected", tp.isPasswordSet());

         // search for expected password in stdout
         String outStream = myOut.toString();
         assertTrue("Domain password should appear in the standard output: " + outStream, outStream.indexOf(DOMAIN_PASSWORD) != -1);

         undeploy(jarName);
         // undeploy the Password Beans
         url1 = getResourceURL("security/password-mask/" + passBeans);
         undeploy(url1);
      }
      catch (Exception e)
      {
         fail = e;
      }
      finally
      {
         // shutdown the server using the shutdown command
         String[] shutdownCommand = getShellCommand("shutdown", "-s " + getJndiURL() + " -S", null);
         getShellScriptExecutor().runShellCommand(shutdownCommand, envp, WORKING_DIR);

         System.out.println("shutdown output = " + getShellScriptExecutor().getOutput());
         System.out.println("shutdown error = " + getShellScriptExecutor().getError());

         // waitForServerStop kills the process and throws an exception if server does not stop
         waitForServerStop(getAsyncShellScriptExecutor(), STOP_TIMEOUT);
         System.out.println("Server stopped successfully");
         getAsyncShellScriptExecutor().assertOnOutputStream(SERVER_STOPPED_MESSAGE,
                 "Server shutdown message did not appear in logs");

         // rm -r server/production/conf/password
         Files.delete(new File(JBOSS_DIST_DIR.getAbsolutePath() + FS + "server" + FS +
                 "production" + FS + "conf" + FS + "password"));

         System.setOut(sysOut);
         after();
         if (fail != null)
            fail(fail.getMessage());
      }
   }

   private void encryptKeystorePassword(String password) throws Exception
   {
      System.out.println("encryptKeystorePassword invoked");
      Process p = startPasswordTool("0 " + password + " SaltyStringy 208 5");
      BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String s;
      // we search the output for a substring: "Keystore Password encrypted into"
      boolean found = false;
      while ((s = stdout.readLine()) != null)
      {
         System.out.println("output -- " + s);

         if (s.indexOf("Keystore Password encrypted into") != -1)
            found = true;
      }
      p.waitFor();
      assertTrue("password_tool should return \"Keystore Password encrypted\" into the standard output", found);
   }

   private void createSecurityDomain(String domain, String password) throws Exception
   {
      Process p = startPasswordTool("1 server.keystore jboss 2 " + domain + " " + password + " 5");

      BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String s;
      // we search the standard output for password creation confirmation
      boolean found = false;
      while ((s = stdout.readLine()) != null)
      {
         if (s.indexOf("Password created for domain:" + domain) != -1)
            found = true;
      }
      p.waitFor();
      assertTrue("keytool did not create a password for domain " + domain, found);
   }

   private void deleteSecurityDomain(String domain) throws Exception
   {
      Process p = startPasswordTool("1 server.keystore " + ALIAS_NAME + " 3 " + domain + " 5");

      BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String s;
      // we search the standard output for password creation confirmation
      boolean found = false;
      while (stdout.readLine() != null)
      {
      }
      p.waitFor();
   }

   private void checkSecurityDomainExistence(String domain, boolean existence) throws Exception
   {
      Process p = startPasswordTool("1 server.keystore " + ALIAS_NAME + " 4 " + DOMAIN_NAME + " 5");
      BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String s;
      // we search the standard output for password creation confirmation
      boolean found = false;
      while ((s = stdout.readLine()) != null)
      {
         if (s.indexOf(existence ? "Exists = true" : "Exists = false") != -1)
            found = true;
      }
      p.waitFor();
      assertTrue(existence ? "password_tool did not confirm existence of a password for security domain"
            : "password_tool did not confirm non-existence of security domain password", found);
   }

   /**
    * starts the password tool and optionally (very recommendedly) sends some data to its standard input
    * do NOT open its stdin any time after that again  !!!
    * hint: most likely the string for stdin will end with "5" that means the script will exit
    *
    * @param stdin string to be sent to standard input of password_tool.bat/sh
    * @return password_tool's process
    */
   private Process startPasswordTool(String stdin) throws IOException
   {
      System.out.println("--------- " + stdin + " --------- ");
      Process p = Runtime.getRuntime().exec(isWindows() ? new String[] { "cmd", "/c", "password_tool" } : new String[] { WORKING_DIR + "/password_tool.sh" }, null, WORKING_DIR);
      OutputStream stdinStream = p.getOutputStream();
      stdinStream.write(stdin.getBytes());
      stdinStream.close();
      return p;
   }

}
