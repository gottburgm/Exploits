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
package org.jboss.test.cluster.ejb2.basic.bean;

import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import org.jboss.test.cluster.ejb2.basic.interfaces.StatelessSession;
import org.jboss.test.cluster.ejb2.basic.interfaces.StatelessSessionHome;

public class StatelessSessionBean implements SessionBean
{
   public static long numberOfCalls = 0;

   public void ejbCreate()
   {
   }
   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
   }

   public void callBusinessMethodA()
   {
      numberOfCalls++;
   }
   
   public String callBusinessMethodB(String jndiURL)
   {
      numberOfCalls++;
      String rtn = "callBusinessMethodB-" + numberOfCalls;
      testColocation(jndiURL);
      return rtn;
   }

   public void testColocation(String jndiURL)
   {
      try
      {
         System.out.println("begin testColocation");
         InitialContext ctx = new InitialContext();
         if( jndiURL == null )
            jndiURL = "jnp://" + System.getProperty("jboss.bind.address", "localhost") + ":1100/nextgen_StatelessSession";
         StatelessSessionHome home = (StatelessSessionHome) ctx.lookup(jndiURL);
         StatelessSession session = home.create();
         session.callBusinessMethodA();
         System.out.println("end testColocation");
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }

   }
   
   public void resetNumberOfCalls ()
   {
      System.out.println("Number of calls has been reseted");
      numberOfCalls = 0;
   }
   
   public void makeCountedCall ()
   {
      System.out.println("makeCountedCall called");
      numberOfCalls++;
   }
   
   public long getCallCount ()
   {
      System.out.println("getCallCount called");
      return numberOfCalls;
   }
   
   public String getBindAddress()
   {
      return System.getProperty("jboss.bind.address");
   }

}
