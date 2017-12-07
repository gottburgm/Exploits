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
package org.jboss.test.isolation.ejb.b;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.test.isolation.interfaces.IsolationDTO;
import org.jboss.test.util.Debug;

/**
 * A SessionB.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class SessionBEJB implements SessionBean
{
   private static final Logger log = Logger.getLogger(SessionBEJB.class);
   
   public IsolationDTO sayHello(IsolationDTO value)
   {
      if (value.payload.equals("hello") == false)
         throw new EJBException("Did not get the expected 'hello'");
      value.payload = "goodbye";
      return value;
   }
   
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
   
   public void setSessionContext(SessionContext ctx)
   {
   }
}
