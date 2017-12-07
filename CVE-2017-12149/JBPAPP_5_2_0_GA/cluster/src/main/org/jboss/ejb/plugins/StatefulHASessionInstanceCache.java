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
package org.jboss.ejb.plugins;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import org.jboss.ejb.EnterpriseContext;

/**
 * SFSB cache for clustered environment. Mainly avoid "excessive" locking
 * that can generate cluster-distributed deadlocks.
 *
 * @see org.jboss.ejb.plugins.StatefulSessionInstanceCache
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>19 decembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class StatefulHASessionInstanceCache
   extends StatefulSessionInstanceCache
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /**
    * Remove an object from the local cache *without* any locking
    * (synchronized, etc.) to avoid huge cluster-wide deadlock situations
    * We have to unschedule passivation as well as the bean may be
    * used on another node.
    */
   public void invalidateLocally (Object id)
   {
      if (id == null) return;
      
      try
      {
         getCache().remove(id);
      }
      catch (Exception e)
      {
         log.debug (e);
      }
   }
   
   // Z implementation ----------------------------------------------
   
   // AbstractInstanceCache overrides -------------------------------
   
   /*
    * Make less extensive use of global locking when state must be activated: it
    * will most probably generate time-costly cluster-wide communication =>
    * we must avoid globally locking the whole cache! Furthermore, The 
    * StatefulSessionInstanceInterceptor *already* locks concurrent access to the
    * cache that targets the *same* SFSB identity
    */
   public EnterpriseContext get(Object id)
      throws RemoteException, NoSuchObjectException
   {
      if (id == null) throw new IllegalArgumentException("Can't get an object with a null key");

      EnterpriseContext ctx = null;

      synchronized (getCacheLock())
      {
         ctx = (EnterpriseContext)getCache().get(id);
         if (ctx != null)
         {
            return ctx;
         }
      }

      // If the ctx is still null at this point, it means that we must activate it
      // => we don't lock the cache during this operation
      // StatefulSessionInstanceInterceptor prevents multiple accesses for the same id
      //
      try
      {
         ctx = acquireContext();
         setKey(id, ctx);
         activate(ctx);
         logActivation(id);
         insert(ctx);
      }
      catch (Exception x)
      {
         if (ctx != null)
            freeContext(ctx);
         log.debug("Activation failure, id="+id, x);
         throw new NoSuchObjectException(x.getMessage());
      }
      
      // FIXME marcf: How can this ever be reached? the ctx is always assigned
      if (ctx == null) throw new NoSuchObjectException("Can't find bean with id = " + id);

      return ctx;
   }
   
   

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
