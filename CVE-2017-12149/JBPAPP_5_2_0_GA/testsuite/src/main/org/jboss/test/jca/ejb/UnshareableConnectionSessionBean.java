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
package org.jboss.test.jca.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.Context;

import org.jboss.test.jca.interfaces.UnshareableConnectionStatefulLocal;
import org.jboss.test.jca.interfaces.UnshareableConnectionStatefulLocalHome;

/**
 * A stateless session bean that invokes a stateful session with an unshareable resource
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class UnshareableConnectionSessionBean
   implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   public void runTest()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         UnshareableConnectionStatefulLocalHome home = (UnshareableConnectionStatefulLocalHome) enc.lookup("local/UnshareableStateful");
         UnshareableConnectionStatefulLocal stateful = home.create();
         stateful.runTestPart1();
         stateful.runTestPart2();
         stateful.remove();
      }
      catch (Exception e)
      {
         throw new EJBException(e.toString());
      }
   }

   public void ejbCreate()
      throws CreateException
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

   public void setSessionContext(SessionContext ctx)
   {
   }

   public void unsetSessionContext()
   {
   }
}

