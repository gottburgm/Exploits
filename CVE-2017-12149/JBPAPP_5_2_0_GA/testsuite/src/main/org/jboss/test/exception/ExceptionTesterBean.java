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
package org.jboss.test.exception;

import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/** Tests of exception propagation via remote and local interfaces
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class ExceptionTesterBean implements SessionBean
{
   private SessionContext ctx;

   public void ejbCreate() throws CreateException
   {
      try
      {
         InitialContext ic = new InitialContext();
         Boolean failInEjbCreate = (Boolean) ic.lookup("java:comp/env/failInEjbCreate");
         if( failInEjbCreate.booleanValue() )
            throw new CreateException("Failed in ejbCreate as requested");
      }
      catch(NameNotFoundException ignore)
      {
         // Assume failInEjbCreate = false
      }
      catch(NamingException e)
      {
         throw new CreateException("Failed to access ENC, "+e.getMessage());
      }
   }

   public void applicationExceptionInTx() throws ApplicationException
   {
      throw new ApplicationException("Application exception from within " +
         " an inherited transaction");
   }

   public void applicationErrorInTx()
   {
      throw new ApplicationError("Application error from within " +
         " an inherited transaction");
   }

   public void ejbExceptionInTx()
   {
      throw new EJBException("EJB exception from within " +
         " an inherited transaction");
   }

   public void runtimeExceptionInTx()
   {
      throw new RuntimeException("Runtime exception from within " +
         " an inherited transaction");
   }

   public void remoteExceptionInTx() throws RemoteException
   {
      throw new RemoteException("Remote exception from within " +
         " an inherited transaction");
   }

   public void applicationExceptionNewTx() throws ApplicationException
   {
      throw new ApplicationException("Application exception from within " +
         " a new container transaction");
   }

   public void applicationErrorNewTx()
   {
      throw new ApplicationError("Application error from within " +
         " an inherited transaction");
   }

   public void ejbExceptionNewTx()
   {
      throw new EJBException("EJB exception from within " +
         " a new container transaction");
   }

   public void runtimeExceptionNewTx()
   {
      throw new RuntimeException("Runtime exception from within " +
         " a new container transaction");
   }

   public void remoteExceptionNewTx() throws RemoteException
   {
      throw new RemoteException("Remote exception from within " +
         " a new container transaction");
   }

   public void applicationExceptionNoTx() throws ApplicationException
   {
      throw new ApplicationException("Application exception without " +
         " a transaction");
   }

   public void applicationErrorNoTx()
   {
      throw new ApplicationError("Application error from within " +
         " an inherited transaction");
   }

   public void ejbExceptionNoTx()
   {
      throw new EJBException("EJB exception without " +
         " a transaction");
   }

   public void runtimeExceptionNoTx()
   {
      throw new RuntimeException("Runtime exception without " +
         " a transaction");
   }

   public void remoteExceptionNoTx() throws RemoteException
   {
      throw new RemoteException("Remote exception without " +
         " a transaction");
   }

   public void securityExceptionByAppNoTx() 
         throws InvalidKeyException
   {
      // This exception should be propagated as is
      throw new InvalidKeyException("securityExceptionByAppNoTx");
   }

   public void securityExceptionNoTx()
   {
      // The method permissions should cause the security exception
   }

   public void setSessionContext(SessionContext ctx)
   {
      this.ctx = ctx;
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
}
