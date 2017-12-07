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
package org.jboss.test.hibernate.timers;

import java.util.List;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 The EJB for the ITimers interface

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class TimersBean implements SessionBean
{
   private TimersFactory delegate;

   public void setSessionContext(SessionContext ctx)
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
   public void ejbCreate()
   {
      delegate = new TimersFactory();
   }

   public void persist(Timers transientInstance)
   {
      delegate.persist(transientInstance);
   }

   public void attachDirty(Timers instance)
   {
      delegate.attachDirty(instance);
   }

   public void attachClean(Timers instance)
   {
      delegate.attachClean(instance);
   }

   public void delete(Timers persistentInstance)
   {
      delegate.delete(persistentInstance);
   }

   public Timers merge(Timers detachedInstance)
   {
      return delegate.merge(detachedInstance);
   }

   public Timers findById(TimersID id)
   {
      return delegate.findById(id);
   }
   public List listTimers()
   {
      return delegate.listUsers();
   }
}
