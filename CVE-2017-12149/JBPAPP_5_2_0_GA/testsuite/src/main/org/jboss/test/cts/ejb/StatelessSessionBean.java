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

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.NoSuchObjectLocalException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

import org.jboss.test.cts.interfaces.StatefulSessionLocalHome;
import org.jboss.test.cts.interfaces.StatefulSessionLocal;
import org.jboss.test.cts.interfaces.StatelessSessionHome;
import org.jboss.test.cts.interfaces.StatelessSession;
import org.jboss.test.cts.interfaces.ClientCallback;
import org.jboss.test.cts.interfaces.StatelessSessionLocalHome;
import org.jboss.test.cts.interfaces.StatelessSessionLocal;
import org.jboss.test.util.ejb.SessionSupport;

/** The stateless session bean implementation
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class StatelessSessionBean
      extends SessionSupport
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private static Logger log = Logger.getLogger(StatelessSessionBean.class);

   private static boolean breakCreate = false;
   
   public void ejbCreate()
         throws CreateException
   {
      if (breakCreate)
         throw new CreateException("broken create");
   }

   public String method1(String msg)
   {
      return msg;
   }

   /**
    * This method is actually invalid (a hack) 
    * because it lets the client change the state of the SLSB
    * 
    * @param action the action
    */
   public void breakCreate()
   {
      breakCreate = true;
   }

   public void loopbackTest()
         throws java.rmi.RemoteException
   {
      try
      {
         InitialContext ctx = new InitialContext();
         StatelessSessionHome home = (StatelessSessionHome) ctx.lookup("ejbcts/StatelessSessionBean");
         StatelessSession sessionBean;
         try
         {
            sessionBean = home.create();
         }
         catch (CreateException ex)
         {
            log.debug("Loopback CreateException: " + ex);
            throw new EJBException(ex);
         }
         sessionBean.loopbackTest(sessionCtx.getEJBObject());
      }
      catch (javax.naming.NamingException nex)
      {
         log.debug("Could not locate bean instance");
      }
   }

   public void loopbackTest(EJBObject obj)
         throws java.rmi.RemoteException
   {
      // This should throw an exception.
      StatelessSession bean = (StatelessSession) obj;
      bean.method1("Hello");
   }

   public void callbackTest(ClientCallback callback, String data)
         throws java.rmi.RemoteException
   {
      callback.callback(data);
   }

   public void npeError()
   {
      Object obj = null;
      obj.toString();
   }

   public void testLocalHome() throws InvocationTargetException
   {
      StatelessSessionLocalHome home = (StatelessSessionLocalHome) sessionCtx.getEJBLocalHome();
      log.debug("Obtained StatelessSessionLocalHome from ctx");
      try
      {
         StatelessSessionLocal local = home.create();
         log.debug("Created StatelessSessionLocal#1");
         StatelessSessionLocalHome home2 = (StatelessSessionLocalHome) local.getEJBLocalHome();
         log.debug("Obtained StatelessSessionLocalHome from StatelessSessionLocal");
         local = home2.create();
         log.debug("Created StatelessSessionLocal#2");
         local.remove();
      }
      catch(Exception e)
      {
         log.debug("testLocalHome failed", e);
         throw new InvocationTargetException(e, "testLocalHome failed");
      }
   }

   public void testPassivationByTimeLocal()
   {
      StatefulSessionLocal sessionBean1 = null;
      Handle handle = null;
      try
      {
         Context ctx = new InitialContext();
         log.debug("+++ testPassivationByTime");
         StatefulSessionLocalHome sessionHome = ( StatefulSessionLocalHome ) ctx.lookup("ejbcts/StatefulSessionLocalBean");
         sessionBean1 = sessionHome.create("testPassivationByTimeLocal");		
         sessionBean1.ping();

		   handle = sessionBean1.getHandle();

         log.debug("Waiting 41 seconds for passivation...");
         Thread.sleep(41*1000);

         // Validate that sessionBean1 was passivated and activated
         boolean passivated = sessionBean1.getWasPassivated();
         if (passivated == false) throw new EJBException("sessionBean1 WasPassivated");
         boolean activated = sessionBean1.getWasActivated();
         if (activated == false) throw new EJBException("sessionBean1 WasActivated");

         log.debug("Waiting 90 seconds for removal due to age...");
         Thread.sleep(90*1000);
      }
      catch (CreateException e)
      {
         throw new EJBException(e.toString());
      }
      catch (NamingException e)
      {
         throw new EJBException(e.toString());
      }
      catch (InterruptedException e)
      {
         throw new EJBException(e.toString());
      }

      try
      {
         sessionBean1.ping();
         throw new EJBException("Was able to ping for a removed session");
      }
      catch (NoSuchObjectLocalException expected)
      {
         log.debug("Session access failed as expected", expected);
      }

      try
      {
         handle.getEJBObject();
         throw new EJBException("Was able to getEJBObject for a removed session");
      }
      catch (RemoteException expected)
      {
         log.debug("Session access failed as expected", expected);
      }
   }
}
