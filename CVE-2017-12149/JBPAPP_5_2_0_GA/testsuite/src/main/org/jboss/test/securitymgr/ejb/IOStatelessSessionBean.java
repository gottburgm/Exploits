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
package org.jboss.test.securitymgr.ejb;

import java.io.File;
import java.io.IOException;
import java.lang.SecurityManager;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Permission;
import java.security.Principal;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jboss.logging.Logger;

import org.jboss.security.SecurityAssociation;

/** A session bean that attempts operations not allowed by the EJB 2.0
 spec as a test of running JBoss with a security manager.
 
@author Scott.Stark@jboss.org
@version $Revision: 81036 $
 */
public class IOStatelessSessionBean implements SessionBean
{
   static final Logger log = Logger.getLogger(IOStatelessSessionBean.class);

   private SessionContext sessionContext;

   public void ejbCreate() throws CreateException
   {
   }
   public void ejbActivate()
   {
   }
   public void ejbPassivate()
   {
   }
   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext context)
   {
      sessionContext = context;
   }

   /**
    */
   public String read(String path) throws IOException
   {
      log.debug("read, path="+path);
      File tstPath = new File(path);
      if( tstPath.exists() == false )
         path = null;
      return path;
   }

   public void write(String path) throws IOException
   {
      log.debug("write, path="+path);
      File tstPath = new File(path);
      tstPath.createNewFile();
   }

   public void listen(int port) throws IOException
   {
      log.debug("Creating server listening port: "+port);
      ServerSocket ss = new ServerSocket(port);
      log.debug("Listening");
      ss.close();
   }

   public void connect(String host, int port) throws IOException
   {
      log.debug("connect, host: "+host+", port: "+port);
      Socket s = new Socket(host, port);
      log.debug("Connected");
      s.close();
   }

   public void createClassLoader()
   {
      log.debug("createClassLoader");
      // Can't use URLClassLoader.newInstance as this uses a privaledged block
      ClassLoader cl = new ClassLoader()
         {
         };
      log.debug("Created ClassLoader");
   }
   public void getContextClassLoader()
   {
      // This will be allowed because the our class loader is an ancestor of the TCL
      log.debug("Begin getContextClassLoader");
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      log.debug("End getContextClassLoader");
   }
   public void setContextClassLoader()
   {
      log.debug("Begin setContextClassLoader");
      ClassLoader cl = null;
      Thread.currentThread().setContextClassLoader(cl);
      log.debug("End setContextClassLoader");
   }
   public void createSecurityMgr()
   {
      log.debug("createSecurityMgr");
      SecurityManager secmgr = new SecurityManager()
      {
         public void checkPermission(Permission p)
         {
         }
      };
      System.setSecurityManager(secmgr);
   }

   /** This will only be disallowed if the current thread belongs to the
    root thread group and this is rarely true as even the thread that
    starts main() is not in this group.
    */
   public void renameThread()
   {
      log.debug("renameThread");
      Thread t = Thread.currentThread();
      t.setName("Hijacked name");
      log.debug("Renamed current thread");
   }
   public void createThread()
   {
      log.debug("createThread");
      Thread t = new Thread("IOSession.createThread");
      t.start();
      log.debug("Started a thread");
   }

   /** This test will only fail if reflection is used on a class that
    has not been loaded by the same class loader as the IOStatelessSessionBean
    */
   public void useReflection()
   {
      log.debug("useReflection");
      try
      {
         Field secret = System.class.getDeclaredField("secret");
         Object value = secret.get(null);
      }
      catch(NoSuchFieldException e)
      {
      }
      catch(IllegalAccessException e)
      {
      }
      log.debug("Search for System.secret did not fail with a SecurityException");
   }

   public void loadLibrary()
   {
      log.debug("loadLibrary");
      System.loadLibrary("jdwp");
      log.debug("Called System.loadLibrary");
   }

   public void changeSystemOut()
   {
      log.debug("changeSystemOut");
      System.setOut(null);
   }
   public void changeSystemErr()
   {
      log.debug("changeSystemErr");
      System.setErr(null);
   }

   public void systemExit(int status)
   {
      log.debug("systemExit");
      System.exit(status);
   }

}
