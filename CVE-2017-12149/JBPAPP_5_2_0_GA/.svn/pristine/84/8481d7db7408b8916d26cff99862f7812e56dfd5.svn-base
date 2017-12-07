/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ejb.passivationcl.stateful;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.test.ejb.passivationcl.entity.ALocal;

/**
 * A MySessionBean.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class StatefulSessionBean implements SessionBean
{
   private boolean passivated;

   public void test() throws RemoteException
   {
      Object lookup;
      try
      {
         lookup = getIC().lookup("passivationcl/ABean/local");
      }
      catch (NamingException e)
      {
         throw new IllegalStateException("Failed to lookup", e);
      }
      ALocal.class.cast(lookup);
   }
   
   public void ejbCreate() throws RemoteException
   {
      test();
   }
   
   @Override
   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   @Override
   public void ejbPassivate() throws EJBException, RemoteException
   {
      passivated = true;
      test();
   }

   public boolean isPassivated() throws RemoteException
   {
      return passivated;
   }

   @Override
   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   @Override
   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
   }

   private InitialContext getIC()
   {
      try
      {
         return new InitialContext();
      }
      catch (NamingException e)
      {
         throw new EJBException("Failed to create InitialContext", e);
      }
   }
}
