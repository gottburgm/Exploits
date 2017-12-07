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
package org.jboss.test.cts.ejb;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Properties;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.test.cts.interfaces.CallerSession;
import org.jboss.test.cts.interfaces.CallerSessionHome;
import org.jboss.test.cts.interfaces.ReferenceTest;
import org.jboss.test.cts.interfaces.CalleeData;
import org.jboss.test.cts.interfaces.CalleeException;
import org.jboss.test.util.ejb.SessionSupport;
import org.jboss.util.Classes;
import org.jboss.mx.loading.ClassLoaderUtils;

/** The stateless session bean implementation
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class CallerSessionBean
      extends SessionSupport
{
   private static Logger log = Logger.getLogger(CallerSessionBean.class);

   private CallerSessionHome cachedHome;

   public void ejbCreate() throws CreateException
   {
   }

   public CalleeData simpleCall2(boolean isCaller) throws RemoteException
   {
      StringBuffer info = new StringBuffer("simpleCall2, isCaller: "+isCaller);
      info.append(" CalleeData, ");
      try
      {
         Classes.displayClassInfo(CalleeData.class, info);
         log.info(info.toString());
      }
      catch(Exception e)
      {
         // Can happen due to no permissions to get TCL
         e.printStackTrace();
      }

      // If this is the callee just return
      if( isCaller == false )
         return new CalleeData();

      // Call the second deployment instance
      CallerSessionHome home = null;
      CallerSession callee = null;

      try
      {
         home = lookupHome("ejbcts2/CalleeSessionHome");
         callee = home.create();
      }
      catch(NamingException e)
      {
         throw new ServerException("Failed to lookup CalleeHome", e);
      }
      catch(CreateException e)
      {
         throw new ServerException("Failed to create Callee", e);
      }

      CalleeData data = callee.simpleCall(false);
      return data;
   }

   public CalleeData simpleCall(boolean isCaller) throws RemoteException
   {
      StringBuffer info = new StringBuffer("simpleCall, isCaller: "+isCaller);
      info.append(" CalleeData, ");
      try
      {
         Classes.displayClassInfo(CalleeData.class, info);
         log.info(info.toString());
      }
      catch(Exception e)
      {
         // Can happen due to no permissions to get TCL
         log.debug("displayClassInfo failure", e);
      }
      // If this is the callee just return
      if( isCaller == false )
         return new CalleeData();

      // Call the second deployment instance
      CallerSession callee = null;
      try
      {
         cachedHome = lookupHome("ejbcts2/CalleeSessionHome");
         callee = cachedHome.create();
      }
      catch(NamingException e)
      {
         throw new ServerException("Failed to lookup CalleeHome", e);
      }
      catch(CreateException e)
      {
         throw new ServerException("Failed to create Callee", e);
      }
      catch(Throwable e)
      {
         log.error("Unexpected error", e);
         throw new ServerException("Unexpected error"+e.getMessage());
      }

      CalleeData data = callee.simpleCall2(false);
      return data;
   }

   /** Lookup the cts.jar/CalleeHome binding and invoke
    *
    * @throws RemoteException
    */
   public void callByValueInSameJar() throws RemoteException
   {
      // Call the second deployment instance
      CallerSession callee = null;
      try
      {
         cachedHome = lookupHome("ejbcts/CalleeSessionHome");
         callee = cachedHome.create();
      }
      catch(NamingException e)
      {
         throw new ServerException("Failed to lookup CalleeHome", e);
      }
      catch(CreateException e)
      {
         throw new ServerException("Failed to create Callee", e);
      }
      catch(Throwable e)
      {
         log.error("Unexpected error", e);
         throw new ServerException("Unexpected error"+e.getMessage());
      }

      ReferenceTest test = new ReferenceTest();
      callee.validateValueMarshalling(test);
   }

   public void callAppEx() throws CalleeException, RemoteException
   {
      StringBuffer info = new StringBuffer("appEx, CalleeException, ");
      try
      {
         Classes.displayClassInfo(CalleeException.class, info);
         log.info(info.toString());
      }
      catch(Exception e)
      {
         // Can happen due to no permissions to get TCL
         log.debug("displayClassInfo failure", e);
      }
      // Call the second deployment instance
      CallerSessionHome home = null;
      CallerSession callee = null;

      try
      {
         home = lookupHome("ejbcts2/CalleeSessionHome");
         callee = home.create();
         callee.appEx();
      }
      catch(NamingException e)
      {
         throw new ServerException("Failed to lookup CalleeHome", e);
      }
      catch(CreateException e)
      {
         throw new ServerException("Failed to create Callee", e);
      }
      catch(CalleeException e)
      {
         throw e;
      }
   }

   public void appEx() throws CalleeException
   {
      StringBuffer info = new StringBuffer("appEx, CalleeException, ");
      try
      {
         Classes.displayClassInfo(CalleeException.class, info);
         log.info(info.toString());
      }
      catch(Exception e)
      {
         // Can happen due to no permissions to get TCL
         log.debug("displayClassInfo failure", e);
      }
      throw new CalleeException();
   }

   public void validateValueMarshalling(ReferenceTest test)
   {
      boolean wasSerialized = test.getWasSerialized();
      log.info("validateValueMarshalling, testWasSerialized: "+wasSerialized);
      if( wasSerialized == false )
         throw new EJBException("ReferenceTest was not serialized");
   }

   private CallerSessionHome lookupHome(String ejbName) throws NamingException
   {
      CallerSessionHome home = null;
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.OBJECT_FACTORIES, "org.jboss.naming:org.jnp.interfaces");
      env.setProperty(Context.PROVIDER_URL, System.getProperty("jboss.bind.address", "localhost") + ":1099");

      InitialContext ctx = new InitialContext(env);
      log.info("looking up: "+ejbName);
      Object ref = ctx.lookup(ejbName);
      StringBuffer buffer = new StringBuffer("JNDI CallerSessionHome.class: ");
      try
      {
         Classes.displayClassInfo(ref.getClass(), buffer);
         log.info(buffer.toString());
      }
      catch(Exception e)
      {
         // Can happen due to no permissions to get TCL
         log.debug("displayClassInfo failure", e);
      }
      buffer.setLength(0);
      buffer.append("Session CallerSessionHome.class: ");
      try
      {
         Classes.displayClassInfo(CallerSessionHome.class, buffer);
         log.info(buffer.toString());
      }
      catch(Exception e)
      {
         // Can happen due to no permissions to get TCL
         log.debug("displayClassInfo failure", e);
      }

      home = (CallerSessionHome) ref;
      return home;
   }
}
