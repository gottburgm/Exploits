/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.securitymgr.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.jboss.test.securitymgr.ejb.BadBean;
import org.jboss.test.securitymgr.ejb.IOStatelessSessionBean;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;

import org.jboss.test.JBossTestCase;

/** Tests of the security permission enforcement that creates and directly
 invokes the ejb methods to test the security policy permissions
 without the noise of the ejb container.

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
 */
public class PolicyUnitTestCase extends JBossTestCase
{

   public PolicyUnitTestCase(String name)
   {
      super(name);
   }

   /** Test that a bean cannot access the SecurityAssociation class
    */
   public void testSecurityAssociation() throws Exception
   {
      log.debug("+++ testSecurityAssociation()");
      BadBean bean = getBadSession();

      try
      {
         bean.getPrincipal();
         doFail("Was able to call Bad.getPrincipal");
      }
      catch(Exception e)
      {
         log.debug("Bad.getPrincipal failed as expected", e);
      }

      try
      {
         bean.getCredential();
         doFail("Was able to call Bad.getCredential");
      }
      catch(Exception e)
      {
         log.debug("Bad.getCredential failed as expected", e);
      }

      try
      {
         bean.setPrincipal(null);
         doFail("Was able to call Bad.setPrincipal");
      }
      catch(Exception e)
      {
         log.debug("Bad.setPrincipal failed as expected", e);
      }

      try
      {
         char[] password = "secret".toCharArray();
         bean.setCredential(password);
         doFail("Was able to call Bad.setCredential");
      }
      catch(Exception e)
      {
         log.debug("Bad.setCredential failed as expected", e);
      }
   }

   /** Test that a bean cannot access the filesystem using java.io.File
    */
   public void testFileIO() throws Exception
   {
      log.debug("+++ testFileIO()");
      IOStatelessSessionBean bean = getIOSession();

      try
      {
         // This should fail because the bean calls File.exists()
         bean.read("nofile.txt");
         doFail("Was able to call IOSession.read");
      }
      catch(Exception e)
      {
         log.debug("IOSession.read failed as expected", e);
      }

      try
      {
         // This should fail because the bean calls File.exists()
         bean.write("nofile.txt");
         doFail("Was able to call IOSession.write");
      }
      catch(Exception e)
      {
         log.debug("IOSession.write failed as expected", e);
      }
   }

   public void testSockets() throws Exception
   {
      log.debug("+++ testSockets()");
      IOStatelessSessionBean bean = getIOSession();
      try
      {
         bean.listen(0);
         doFail("Was able to call IOSession.listen");
      }
      catch(Exception e)
      {
         log.debug("IOSession.listen failed as expected", e);
      }

      final ServerSocket tmp = new ServerSocket(0);
      log.debug("Created ServerSocket: "+tmp);
      final Logger theLog = log;
      Thread t = new Thread("Acceptor")
      {
         public void run()
         {
            try
            {
               Socket s = tmp.accept();
               theLog.debug("Accepted Socket: "+s);
               s.close();
               theLog.debug("ServerSocket thread exiting");
            }
            catch(IOException e)
            {
            }
         }
      };
      int port = tmp.getLocalPort();
      t.start();
      bean.connect("localhost", port);
      tmp.close();
   }

   public void testClassLoaders() throws Exception
   {
      log.debug("+++ testClassLoaders()");
      IOStatelessSessionBean bean = getIOSession();
      try
      {
         bean.createClassLoader();
         doFail("Was able to call IOSession.createClassLoader");
      }
      catch(Exception e)
      {
         log.debug("IOSession.createClassLoader failed as expected", e);
      }

      try
      {
         bean.getContextClassLoader();
         //doFail("Was able to call IOSession.getContextClassLoader");
         log.debug("Was able to call IOSession.getContextClassLoader");
      }
      catch(Exception e)
      {
         log.debug("IOSession.getContextClassLoader failed as expected", e);
      }

      try
      {
         bean.setContextClassLoader();
         doFail("Was able to call IOSession.setContextClassLoader");
      }
      catch(Exception e)
      {
         log.debug("IOSession.setContextClassLoader failed as expected", e);
      }
   }

   public void testReflection() throws Exception
   {
      log.debug("+++ testReflection()");
      IOStatelessSessionBean bean = getIOSession();
      try
      {
         bean.useReflection();
         doFail("Was able to call IOSession.useReflection");
      }
      catch(Exception e)
      {
         log.debug("IOSession.useReflection failed as expected", e);
      }
   }

   public void testThreadAccess() throws Exception
   {
      log.debug("+++ testThreadAccess()");
      IOStatelessSessionBean bean = getIOSession();
      try
      {
         // This test will fail because the calling thread it not in the root thread group
         bean.renameThread();
         doFail("Was able to call IOSession.renameThread");
      }
      catch(Exception e)
      {
         log.debug("IOSession.renameThread failed as expected", e);
      }
   }

   public void testSystemAccess() throws Exception
   {
      log.debug("+++ testSystemAccess()");
      IOStatelessSessionBean bean = getIOSession();
      try
      {
         bean.createSecurityMgr();
         doFail("Was able to call IOSession.createSecurityMgr");
      }
      catch(Exception e)
      {
         log.debug("IOSession.createSecurityMgr failed as expected", e);
      }

      try
      {
         bean.createSecurityMgr();
         doFail("Was able to call IOSession.changeSystemOut");
      }
      catch(Exception e)
      {
         log.debug("IOSession.changeSystemOut failed as expected", e);
      }

      try
      {
         bean.changeSystemErr();
         doFail("Was able to call IOSession.changeSystemErr");
      }
      catch(Exception e)
      {
         log.debug("IOSession.changeSystemErr failed as expected", e);
      }

      try
      {
         bean.loadLibrary();
         doFail("Was able to call IOSession.loadLibrary");
      }
      catch(Exception e)
      {
         log.debug("IOSession.loadLibrary failed as expected", e);
      }

      /* This test can't be enforced when running in a forked VM. The
       Runtime.exec adss the (java.lang.RuntimePermission exitVM) to all jars
       in the caller classpath it seems. We can't let it fail as this
       kills the junit vm and no results are generated.
      try
      {
         bean.systemExit(1);
         doFail("Was able to call IOSession.systemExit");
      }
      catch(Exception e)
      {
         log.debug("IOSession.systemExit failed as expected", e);
      }
      */
   }

   /** We don't use the JBoss server so nullify this test
    */
   public void testServerFound() throws Exception
   {
     log.debug("+++ testServerFound()");
   }

   /** We don't deploy anything.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite(PolicyUnitTestCase.class);  
      //suite.addTest(new PolicyUnitTestCase("testReflection"));
      return suite;
   }

   private BadBean getBadSession()
   {
      BadBean bean = new BadBean();
      return bean;
   }
   private IOStatelessSessionBean getIOSession()
   {
      IOStatelessSessionBean bean = new IOStatelessSessionBean();
      return bean;
   }
   private void doFail(String msg)
   {
      log.error(msg);
      fail(msg);
   }
   private void doFail(String msg, Throwable t)
   {
      log.error(msg, t);
      fail(msg);
   }
}
