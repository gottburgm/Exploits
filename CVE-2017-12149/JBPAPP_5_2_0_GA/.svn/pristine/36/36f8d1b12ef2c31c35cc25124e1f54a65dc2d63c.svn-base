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
package com.cluster.simple.sessionbeans;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class CallerSessionBean implements SessionBean
{
   public String processRequest(String msg)
         throws RemoteException
   {
      System.out.println("CallerSessionBean:processRequest");
      String res = null;

      try
      {
         //Get JNDI context
         Context naming = null;

         if (msg == null || msg.length() == 0)
         {
            naming = new InitialContext();
         }
         else
         {
            Properties env = new Properties();
            env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
            env.put("java.naming.provider.url", msg);
            env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");

            naming = new InitialContext(env);
         }

         SimpleSessionHome home = (SimpleSessionHome) naming.lookup("SimpleSession");
         SimpleSession targetBean = home.create();
         targetBean.processRequest("");
         targetBean.remove();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return res;
   }

   public void ejbCreate()
   {
   }

   public void ejbRemove()
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void setSessionContext(SessionContext sc)
   {
   }


}
