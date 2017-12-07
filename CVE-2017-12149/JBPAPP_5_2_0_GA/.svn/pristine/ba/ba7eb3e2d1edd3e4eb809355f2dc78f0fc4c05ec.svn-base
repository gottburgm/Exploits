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
package org.jboss.test.deadlock.bean;

import java.rmi.*;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.Context;
import org.jboss.test.deadlock.interfaces.*;
import org.jboss.util.deadlock.ApplicationDeadlockException;

public class StatelessSessionBean implements SessionBean 
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
  private SessionContext sessionContext;

   public void ejbCreate() throws RemoteException, CreateException 
   {
   }
   
   public void ejbActivate() throws RemoteException {
   }
   
   public void ejbPassivate() throws RemoteException {
   }
   
   public void ejbRemove() throws RemoteException {
   }
   
   public void setSessionContext(SessionContext context) throws RemoteException {
      sessionContext = context;
      //Exception e = new Exception("in set Session context");
      //log.debug("failed", e);
   }
   
   
   public void callAB() throws RemoteException
   {
      try
      {
	 log.info("****callAB start****");
	 EnterpriseEntityHome home = (EnterpriseEntityHome)new InitialContext().lookup("nextgenEnterpriseEntity");
	 EnterpriseEntity A = home.findByPrimaryKey("A");
	 EnterpriseEntity B = home.findByPrimaryKey("B");
	 A.getOtherField();
	 log.debug("callAB is sleeping");
	 Thread.sleep(1000);
	 log.debug("callAB woke up");
	 B.getOtherField();
	 log.debug("callAB end");
      }
      catch (ApplicationDeadlockException ade)
      {
         System.out.println("APPLICATION DEADLOCK EXCEPTION");
         throw ade;
      }
      catch (RemoteException rex)
      {
         throw rex;
      }
      catch (Exception ex)
      {
	 throw new RemoteException("failed");
      }
   }
   
   public void callBA() throws RemoteException
   {
      try
      {
	 log.info("****callBA start****");
	 EnterpriseEntityHome home = (EnterpriseEntityHome)new InitialContext().lookup("nextgenEnterpriseEntity");
	 EnterpriseEntity B = home.findByPrimaryKey("B");
	 EnterpriseEntity A = home.findByPrimaryKey("A");
	 B.getOtherField();
	 log.debug("callBA is sleeping");
	 Thread.sleep(1000);
	 log.debug("callBA woke up");
	 A.getOtherField();
	 log.debug("callBA end");
      }
      catch (ApplicationDeadlockException ade)
      {
         System.out.println("APPLICATION DEADLOCK EXCEPTION");
         throw ade;
      }
      catch (RemoteException rex)
      {
         throw rex;
      }
      catch (Exception ex)
      {
	 throw new RemoteException("failed");
      }
   }

   public void requiresNewTest(boolean first) throws RemoteException
   {
      try
      {
	 log.info("***requiresNewTest start***");
         InitialContext ctx = new InitialContext();
	 EnterpriseEntityHome home = (EnterpriseEntityHome)ctx.lookup("nextgenEnterpriseEntity");
	 EnterpriseEntity C = home.findByPrimaryKey("C");

         C.getOtherField();
         if (first)
         {
            StatelessSessionHome shome = (StatelessSessionHome)ctx.lookup("nextgen.StatelessSession");
            StatelessSession session = shome.create();
            session.requiresNewTest(false);
         }
      }
      catch (RemoteException rex)
      {
         throw rex;
      }
      catch (Exception ex)
      {
         throw new RemoteException("failed");
      }
   }

   public void createCMRTestData(String jndiName)
   {
      try
      {
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         EnterpriseEntityLocalHome home = (EnterpriseEntityLocalHome)enc.lookup(jndiName);
         try
         {
            home.create("First");
         }
         catch (DuplicateKeyException dontCare)
         {
         }
         try
         {
            home.create("Second");
         }
         catch (DuplicateKeyException dontCare)
         {
         }
         EnterpriseEntityLocal first = home.findByPrimaryKey("First");
         EnterpriseEntityLocal second = home.findByPrimaryKey("Second");
         first.setNext(second);
         second.setNext(first);
      }
      catch (Exception e)
      {
         throw new EJBException("Unable to create data", e);
      }
   }

   public void cmrTest(String jndiName, String start)
   {
      try
      {
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         EnterpriseEntityLocalHome home = (EnterpriseEntityLocalHome)enc.lookup(jndiName);
         EnterpriseEntityLocal initial = home.findByPrimaryKey(start);
         initial.getNext().getName();
      }
      catch (Exception e)
      {
         throw new EJBException("Unable to create data", e);
      }
   }
}
