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

import java.rmi.RemoteException;
import javax.ejb.EJBException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatelessSessionEnterpriseContext;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 *  Singleton pool for session beans. This lets you have
 * singletons in EJB!
 *
 *  @author Rickard Oberg
 *  @version $Revision: 81030 $
 */
public class SingletonStatelessSessionInstancePool extends AbstractInstancePool
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   EnterpriseContext ctx;
   boolean inUse = false;
   boolean isSynchronized = true;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /**
    *   Get the singleton instance
    *
    * @return     Context /w instance
    * @exception   Exception
    */
   public synchronized EnterpriseContext get()
      throws Exception
   {
      // Wait while someone else is using it
      while(inUse && isSynchronized)
      {
         try { this.wait(); } catch (InterruptedException e) {}
      }

      // Create if not already created (or it has been discarded)
      if (ctx == null)
      {
         try
         {
            ctx = create(getContainer().createBeanClassInstance());
         } catch (InstantiationException e)
         {
            throw new EJBException("Could not instantiate bean", e);
         } catch (IllegalAccessException e)
         {
            throw new EJBException("Could not instantiate bean", e);
         }
      }
      else
      {
      }

      // Lock and return instance
      inUse = true;
      return ctx;
   }

   /**
    *   Return an instance after invocation.
    *
    *   Called in 2 cases:
    *   a) Done with finder method
    *   b) Just removed
    *
    * @param   ctx
    */
   public synchronized void free(EnterpriseContext ctx)
   {
      // Notify waiters
      inUse = false;
      this.notifyAll();
   }

   public synchronized void discard(EnterpriseContext ctx)
   {
      // Throw away
      try
      {
         ctx.discard();
      } catch (RemoteException e)
      {
         // DEBUG Logger.exception(e);
      }

      // Notify waiters
      inUse = false;
      this.notifyAll();
   }

   /**
    * Add a instance in the pool
    */
   public void add()
      throws Exception
   {
      // Empty
   }

   public int getCurrentSize()
   {
      return 1;
   }

   public int getMaxSize()
   {
      return 1;
   }

   public long getAvailableCount()
   {
      return 1;
   }

   // Z implementation ----------------------------------------------

    // XmlLoadable implementation
    public void importXml(Element element) throws DeploymentException
    {
      Element synch = MetaData.getUniqueChild(element, "Synchronized");
      isSynchronized = Boolean.valueOf(MetaData.getElementContent(synch)).booleanValue();
    }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   protected EnterpriseContext create(Object instance)
      throws Exception
   {
      // The instance is created by the caller and is a newInstance();
      return new StatelessSessionEnterpriseContext(instance, getContainer());
   }
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
