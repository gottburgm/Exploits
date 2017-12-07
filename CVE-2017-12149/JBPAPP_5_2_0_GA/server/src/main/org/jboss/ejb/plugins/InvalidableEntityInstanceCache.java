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

import org.jboss.cache.invalidation.InvalidationManagerMBean;
import org.jboss.cache.invalidation.InvalidationGroup;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.system.Registry;
import org.jboss.ejb.EnterpriseContext;

/**
 * Cache implementation that registers with an InvalidationManager when in
 * commit option A or D. Information is found in the EB meta-data (IM name,
 * IG name and commit-option)
 *
 * @see org.jboss.cache.invalidation.InvalidationManagerMBean
 * @see org.jboss.cache.invalidation.triggers.EntityBeanCacheBatchInvalidatorInterceptor
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81030 $
 */
public class InvalidableEntityInstanceCache
      extends org.jboss.ejb.plugins.EntityInstanceCache
      implements org.jboss.cache.invalidation.Invalidatable
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   protected InvalidationManagerMBean invalMgr = null;
   protected InvalidationGroup ig = null;

   protected boolean isTraceEnabled = false;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public InvalidableEntityInstanceCache()
   {
      super();
   }

   // Public --------------------------------------------------------

   // Invalidatable implementation ----------------------------------------------

   public void areInvalid(java.io.Serializable[] keys)
   {
      if (this.isTraceEnabled)
         log.trace("Invalidating entry in cache. Quantity: " + keys.length);

      for (int i = 0; i < keys.length; i++)
      {
         try
         {
            doInvalidate(keys[i]);
         }
         catch (Exception ignored)
         {
            log.debug(ignored);
         }
      }
   }

   public void isInvalid(java.io.Serializable key)
   {
      try
      {
         doInvalidate(key);
      }
      catch (Exception ignored)
      {
         log.debug(ignored);
      }
   }

   public void invalidateAll()
   {
      flush();
   }

   // ServiceMBeanSupport overrides ---------------------------------------------------

   public void start() throws Exception
   {
      super.start();

      log.debug("Starting InvalidableEntityInstanceCache...");

      EntityMetaData emd = ((EntityMetaData) this.getContainer().getBeanMetaData());

      boolean participateInDistInvalidations = emd.doDistributedCacheInvalidations();
      byte co = emd.getContainerConfiguration().getCommitOption();

      if (participateInDistInvalidations &&
            (co == ConfigurationMetaData.A_COMMIT_OPTION || co == ConfigurationMetaData.D_COMMIT_OPTION)
      )
      {
         // we are interested in receiving cache invalidation callbacks
         //
         String groupName = emd.getDistributedCacheInvalidationConfig().getInvalidationGroupName();
         String imName = emd.getDistributedCacheInvalidationConfig().getInvalidationManagerName();

         this.invalMgr = (InvalidationManagerMBean) Registry.lookup(imName);
         this.ig = this.invalMgr.getInvalidationGroup(groupName);
         this.ig.register(this);
         this.isTraceEnabled = log.isTraceEnabled();
      }

   }

   public void stop()
   {
      try
      {
         this.ig.unregister(this);
         this.ig = null;
         this.invalMgr = null;
      }
      catch (Exception e)
      {
         log.debug(e);
      }

      super.stop();
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   protected void doInvalidate(java.io.Serializable key)
   {
      if (key != null)
      {
         synchronized (getCacheLock())
         {
            EnterpriseContext ctx = (EnterpriseContext)getCache().peek(key);
            if(ctx != null)
            {
               release(ctx);
            }
         }
      }
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
