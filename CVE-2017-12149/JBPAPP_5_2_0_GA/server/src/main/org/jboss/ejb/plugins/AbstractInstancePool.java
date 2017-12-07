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

import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import java.util.LinkedList;

import javax.ejb.CreateException;
import javax.ejb.EJBException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstancePool;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.system.ServiceMBeanSupport;
import org.w3c.dom.Element;

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;

/**
 *  Abstract Instance Pool class containing the basic logic to create
 *  an EJB Instance Pool.
 *
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 *  @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 *  @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 *  @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *  @author <a href="mailto:scott.stark@jboss.org">Scott Stark/a>
 *  @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 *  @version $Revision: 81030 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public abstract class AbstractInstancePool
   extends ServiceMBeanSupport
   implements AbstractInstancePoolMBean, InstancePool, XmlLoadable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   /** A FIFO semaphore that is set when the strict max size behavior is in effect.
    When set, only maxSize instances may be active and any attempt to get an
    instance will block until an instance is freed.
    */
   private FIFOSemaphore strictMaxSize;
   /** The time in milliseconds to wait for the strictMaxSize semaphore.
    */
   private long strictTimeout = Long.MAX_VALUE;
   /** The Container the instance pool is associated with */
   protected Container container;
   /** The pool data structure */
   protected LinkedList pool = new LinkedList();
   /** The maximum number of instances allowed in the pool */
   protected int maxSize = 30;
   /** determine if we reuse EnterpriseContext objects i.e. if we actually do pooling */
   protected boolean reclaim = false;
   /** Will the pool block when MaximumSize instances are active */
   protected Boolean isStrict = Boolean.FALSE;


   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /**
    *   Set the callback to the container. This is for initialization.
    *   The IM may extract the configuration from the container.
    *
    * @param   c
    */
   public void setContainer(Container c)
   {
      this.container = c;
   }

   /**
    * @return Callback to the container which can be null if not set proviously
    */
   public Container getContainer()
   {
      return container;
   }

   /**
    * @jmx:managed-attribute
    * @return the current pool size
    */
   public int getCurrentSize()
   {
      synchronized (pool)
      {
         return this.pool.size();
      }
   }

   /**
    * @jmx:managed-attribute
    * @return the current pool size
    */
   public int getMaxSize()
   {
      return this.maxSize;
   }

   /** Get the current avaiable count from the strict max view. If there is
    * no strict max then this will be Long.MAX_VALUE to indicate there is no
    * restriction.
    * @jmx:managed-attribute
    * @return the current avaiable count from the strict max view
    */
   public long getAvailableCount()
   {
      long size = Long.MAX_VALUE;
      if( strictMaxSize != null )
         size = strictMaxSize.permits();
      return size;
   }

   /**
    *   Get an instance without identity.
    *   Can be used by finders,create-methods, and activation
    *
    * @return     Context /w instance
    * @exception   RemoteException
    */
   public EnterpriseContext get()
      throws Exception
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("Get instance "+this+"#"+pool.size()+"#"+getContainer().getBeanClass());

      if( strictMaxSize != null )
      {
         // Block until an instance is available
         boolean acquired = strictMaxSize.attempt(strictTimeout);
         if( trace )
            log.trace("Acquired("+acquired+") strictMaxSize semaphore, remaining="+strictMaxSize.permits());
         if( acquired == false )
            throw new EJBException("Failed to acquire the pool semaphore, strictTimeout="+strictTimeout);
      }

      synchronized (pool)
      {
         if ( pool.isEmpty() == false )
         {
            return (EnterpriseContext) pool.removeFirst();
         }
      }

      // Pool is empty, create an instance
      try
      {
         Object instance = container.createBeanClassInstance();
         return create(instance);
      }
      catch (Throwable e)
      {
         // Release the strict max size mutex if it exists
         if( strictMaxSize != null )
         {
            strictMaxSize.release();
         }
         // Don't wrap CreateExceptions
         if( e instanceof CreateException )
            throw (CreateException) e;

         // Wrap e in an Exception if needed
         Exception ex = null;
         if(e instanceof Exception)
         {
            ex = (Exception)e;
         } else
         {
            ex = new UndeclaredThrowableException(e);
         }
         throw new EJBException("Could not instantiate bean", ex);
      }
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
   public void free(EnterpriseContext ctx)
   {
      if( log.isTraceEnabled() )
      {
         String msg = pool.size() + "/" + maxSize+" Free instance:"+this
            +"#"+ctx.getId()
            +"#"+ctx.getTransaction()
            +"#"+reclaim
            +"#"+getContainer().getBeanClass();
         log.trace(msg);
      }

      ctx.clear();

      try
      {
         // If the pool is not full, add the unused context back into the pool,
         // otherwise, just discard the extraneous context and leave it for GC
         boolean addedToPool = false;
         
         synchronized (pool)
         {
            if (pool.size() < maxSize)
            {
               pool.addFirst(ctx);
               addedToPool = true;
            }
         }
         
         if (addedToPool)
         {
            // If we block when maxSize instances are in use, invoke release on strictMaxSize
            if(strictMaxSize != null)
            {
               strictMaxSize.release();
            }
         }
         else
         {
            // Get rid of the extraneous instance; strictMaxSize should be null
            // (otherwise we wouldn't have gotten the extra instance)
            discard(ctx);
         }
      }
      catch (Exception ignored)
      {
      }
   }

   public void discard(EnterpriseContext ctx)
   {
      if( log.isTraceEnabled() )
      {
         String msg = "Discard instance:"+this+"#"+ctx
            +"#"+ctx.getTransaction()
            +"#"+reclaim
            +"#"+getContainer().getBeanClass();
         log.trace(msg);
      }

      // If we block when maxSize instances are in use, invoke release on strictMaxSize
      if( strictMaxSize != null )
         strictMaxSize.release();

      // Throw away, unsetContext()
      try
      {
         ctx.discard();
      }
      catch (RemoteException e)
      {
         if( log.isTraceEnabled() )
            log.trace("Ctx.discard error", e);
      }
   }

   public void clear()
   {
      synchronized (pool)
      {
         freeAll();
      }
   }

   /**
    * XmlLoadable implementation
    */
   public void importXml(Element element) throws DeploymentException
   {
      String maximumSize = MetaData.getElementContent(MetaData.getUniqueChild(element, "MaximumSize"));
      try
      {
         this.maxSize = Integer.parseInt(maximumSize);
      }
      catch (NumberFormatException e)
      {
         throw new DeploymentException("Invalid MaximumSize value for instance pool configuration");
      }

      // Get whether the pool will block when MaximumSize instances are active
      String strictValue = MetaData.getElementContent(MetaData.getOptionalChild(element, "strictMaximumSize"));
      this.isStrict = Boolean.valueOf(strictValue);
      
      String delay = MetaData.getElementContent(MetaData.getOptionalChild(element, "strictTimeout"));
      try
      {
         if( delay != null )
            this.strictTimeout = Long.parseLong(delay);
      }
      catch (NumberFormatException e)
      {
         throw new DeploymentException("Invalid strictTimeout value for instance pool configuration");
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   protected abstract EnterpriseContext create(Object instance)
   throws Exception;

   protected void createService() throws Exception
   {
      if( this.isStrict == Boolean.TRUE )
         this.strictMaxSize = new FIFOSemaphore(this.maxSize);
   }
   
   protected void destroyService() throws Exception
   {
     freeAll();
     this.strictMaxSize = null;
   }

   // Private -------------------------------------------------------

   /**
    * At undeployment we want to free completely the pool.
    */
   private void freeAll()
   {
      LinkedList clone = (LinkedList)pool.clone();
      for (int i = 0; i < clone.size(); i++)
      {
         EnterpriseContext ec = (EnterpriseContext)clone.get(i);
         // Clear TX so that still TX entity pools get killed as well
         ec.clear();
         discard(ec);
      }
      pool.clear();
   }

   // Inner classes -------------------------------------------------

}
