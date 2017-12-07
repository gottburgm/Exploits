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

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.securitymgr.interfaces.IOSession;
import org.jboss.test.securitymgr.interfaces.IOSessionHome;

/** Tests of the programming restrictions defined by the EJB spec. The JBoss
server must be running under a security manager. The securitymgr-ejb.jar
should be granted only the following permission:

grant securitymgr-ejb.jar {
   permission java.util.PropertyPermission "*", "read";
   permission java.lang.RuntimePermission "queuePrintJob";
   permission java.net.SocketPermission "*", "connect";
 };

@author Scott.Stark@jboss.org
@version $Revision: 81036 $
 */
public class EJBSpecUnitTestCase
   extends JBossTestCase
{

   public EJBSpecUnitTestCase(String name)
   {
      super(name);
   }

   /** Test that a bean cannot access the filesystem using java.io.File
    */
   public void testFileExists() throws Exception
   {
      log.debug("+++ testFileExists()");
      IOSession bean = getIOSession();

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
   }
   /** Test that a bean cannot access the filesystem using java.io.File
    */
   public void testFileWrite() throws Exception
   {
      log.debug("+++ testFileWrite()");
      IOSession bean = getIOSession();
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
      bean.remove();
   }

   public void testSocketListen() throws Exception
   {
      log.debug("+++ testSocketListen()");
      IOSession bean = getIOSession();
      try
      {
         bean.listen(0);
         doFail("Was able to call IOSession.listen");
      }
      catch(Exception e)
      {
         log.debug("IOSession.listen failed as expected", e);
      }
   }

   public void testSocketConnect() throws Exception
   {
      log.debug("+++ testSocketConnect()");
      IOSession bean = getIOSession();
      final ServerSocket tmp = new ServerSocket(0);
      log.debug("Created ServerSocket: "+tmp);
      Thread t = new Thread("Acceptor")
      {
         public void run()
         {
            try
            {
               Socket s = tmp.accept();
               log.debug("Accepted Socket: "+s);
               s.close();
               log.debug("ServerSocket thread exiting");
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
      bean.remove();
   }

   public void testCreateClassLoader() throws Exception
   {
      log.debug("+++ testCreateClassLoader()");
      IOSession bean = getIOSession();
      try
      {
         bean.createClassLoader();
         doFail("Was able to call IOSession.createClassLoader");
      }
      catch(Exception e)
      {
         log.debug("IOSession.createClassLoader failed as expected", e);
      }
   }
   
   public void testGetContextClassLoader() throws Exception
   {
      log.debug("+++ testGetContextClassLoader()");
      IOSession bean = getIOSession();
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
      bean.remove();
   }

   public void testSetContextClassLoader() throws Exception
   {
      log.debug("+++ testSetContextClassLoader()");
      IOSession bean = getIOSession();
      try
      {
         bean.setContextClassLoader();
         doFail("Was able to call IOSession.setContextClassLoader");
      }
      catch(Exception e)
      {
         log.debug("IOSession.setContextClassLoader failed as expected", e);
      }
      bean.remove();
   }

   public void testReflection() throws Exception
   {
      log.debug("+++ testReflection()");
      IOSession bean = getIOSession();
      try
      {
         bean.useReflection();
         doFail("Was able to call IOSession.useReflection");
      }
      catch(Exception e)
      {
         log.debug("IOSession.useReflection failed as expected", e);
      }
      bean.remove();
   }

   public void testThreadAccess() throws Exception
   {
      log.debug("+++ testThreadAccess()");
      IOSession bean = getIOSession();
      try
      {
         /* This test will fail because the calling thread it not in the root
            thread group so we just log a warning */
         bean.renameThread();
         log.warn("Was able to call IOSession.renameThread");
      }
      catch(Exception e)
      {
         log.debug("IOSession.renameThread failed as expected", e);
      }
      bean.remove();
   }

   public void testCreateThread() throws Exception
   {
      log.debug("+++ testCreateThread()");
      IOSession bean = getIOSession();
      try
      {
         /* This test will fail because the calling thread it not in the root
            thread group so we just log a warning */
         bean.createThread();
         log.warn("Was able to call IOSession.createThread");
      }
      catch(Exception e)
      {
         log.debug("IOSession.createThread failed as expected", e);
      }
      bean.remove();
   }

   public void testCreateSecurityMgr() throws Exception
   {
      log.debug("+++ testCreateSecurityMgr()");
      IOSession bean = getIOSession();
      try
      {
         bean.createSecurityMgr();
         doFail("Was able to call IOSession.createSecurityMgr");
      }
      catch(Exception e)
      {
         log.debug("IOSession.createSecurityMgr failed as expected", e);
      }
      bean.remove();
   }

   public void testChangeSystemErr() throws Exception
   {
      log.debug("+++ testChangeSystemErr()");
      IOSession bean = getIOSession();
      try
      {
         bean.changeSystemErr();
         doFail("Was able to call IOSession.changeSystemErr");
      }
      catch(Exception e)
      {
         log.debug("IOSession.changeSystemErr failed as expected", e);
      }
      bean.remove();
   }

   public void testLoadLibrary() throws Exception
   {
      log.debug("+++ testLoadLibrary()");
      IOSession bean = getIOSession();
      try
      {
         bean.loadLibrary();
         doFail("Was able to call IOSession.loadLibrary");
      }
      catch(Exception e)
      {
         log.debug("IOSession.loadLibrary failed as expected", e);
      }
      bean.remove();
   }

   public void testSystemExit() throws Exception
   {
      log.debug("+++ testSystemExit()");
      IOSession bean = getIOSession();
      try
      {
         bean.systemExit(1);
         doFail("Was able to call IOSession.systemExit");
      }
      catch(Exception e)
      {
         log.debug("IOSession.systemExit failed as expected", e);
      }
      bean.remove();
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(EJBSpecUnitTestCase.class, "securitymgr-ejb.jar");
   }

   private IOSession getIOSession() throws Exception
   {
      Object obj = getInitialContext().lookup("secmgr.IOSessionHome");
      IOSessionHome home = (IOSessionHome) obj;
      log.debug("Found secmgr.IOSessionHome");
      IOSession bean = home.create();
      log.debug("Created IOSession");
      return bean;
   }

   private void doFail(String msg)
   {
      log.error(msg);
      fail(msg);
   }
}
