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
package org.jboss.test.marshal.ejb;

import org.jboss.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * Bean to accept payload that cannot be unmarshalled
 *
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 * @version $Revision: 81083 $
 */
public class MarshalSessionBean implements SessionBean
{
   private static final Logger log = Logger.getLogger(MarshalSessionBean.class);

   public MarshalSessionBean()
   {
   }

   public int testMethod(Object payload)
   {
      return 0;
   }

   // Container callbacks -------------------------------------------

   public void setSessionContext(SessionContext ctx)
   {
   }

   public void ejbCreate() throws CreateException
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
}
