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
package org.jboss.test.jmx.eardepends.dependent.ejb;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * @author vescoc
 * 
 * @ejb.bean description = "DependentA SessionBean"
 *           display-name = "DependentA SessionBean"
 *           name = "DependentAEJB"
 *           view-type = "remote"
 *           jndi-name = "test/DependentA"
 */
public class DependentABean implements SessionBean
{
   /**
    * 
    * @throws CreateException
    * 
    * @ejb.create-method
    */
   public void ejbCreate() throws CreateException
   {
   }

   /* (non-Javadoc)
    * @see javax.ejb.SessionBean#ejbActivate()
    */
   public void ejbActivate()
   {
   }

   /* (non-Javadoc)
    * @see javax.ejb.SessionBean#ejbPassivate()
    */
   public void ejbPassivate()
   {
   }

   /* (non-Javadoc)
    * @see javax.ejb.SessionBean#ejbRemove()
    */
   public void ejbRemove()
   {
   }

   /* (non-Javadoc)
    * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
    */
   public void setSessionContext(SessionContext ctx)
   {
   }
   
   /**
    * 
    * @param value
    * 
    * @ejb.interface-method
    * @return
    */
   public Object echo(Object value)
   {
      return value;
   }
}
