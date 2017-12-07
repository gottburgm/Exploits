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
package org.jboss.test.security.clientlogin;

import java.security.Principal;
import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.security.auth.login.LoginContext;
import javax.naming.InitialContext;

import org.jboss.security.auth.callback.UsernamePasswordHandler;

/**
 An IClientLogin session bean that calls a BeanC with changes in the
 caller indentity using ClientLogin module.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class BeanB implements SessionBean
{
   private SessionContext context;

   public void ejbCreate()
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
      this.context = context;
   }

   public Principal callBeanAsClientLoginUser() throws RemoteException
   {
      Principal caller = context.getCallerPrincipal();
      String inputName = caller.getName();
      try
      {
         UsernamePasswordHandler handler = new UsernamePasswordHandler("clientLoginB1", "B1");
         LoginContext lc = new LoginContext("client-login", handler);
         lc.login();
         InitialContext ctx = new InitialContext();
         IClientLoginHome home = (IClientLoginHome) ctx.lookup("java:comp/env/TargetBean");
         IClientLogin bean = home.create();
         Principal callerB1 = bean.callTarget();
         if( callerB1.getName().equals("clientLoginB1") == false )
            throw new RemoteException("callBeanAsClientLoginUser#1 != clientLoginB1");
         lc.logout();

         handler = new UsernamePasswordHandler("clientLoginB2", "B2");
         lc = new LoginContext("client-login", handler);
         lc.login();
         Principal callerB2 = bean.callTarget();
         if( callerB2.getName().equals("clientLoginB2") == false )
            throw new RemoteException("callBeanAsClientLoginUser#2 != clientLoginB2");
         lc.logout();

         // Make sure the caller principal is the same
         String inputName2 = context.getCallerPrincipal().getName();
         if( inputName.equals(inputName2) == false )
            throw new RemoteException("CallerPrincipal changed after logout");
      }
      catch(Exception e)
      {
         if(e instanceof RemoteException )
            throw (RemoteException) e;
         throw new RemoteException("callBeanAsClientLoginUser", e);
      }
      return caller;
   }
   public Principal callTarget() throws RemoteException
   {
      return null;
   }

}
