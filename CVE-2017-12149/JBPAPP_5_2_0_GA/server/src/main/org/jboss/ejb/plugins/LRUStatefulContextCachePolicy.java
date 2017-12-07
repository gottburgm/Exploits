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

import java.util.TimerTask;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * Least Recently Used cache policy for StatefulSessionEnterpriseContexts.
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public class LRUStatefulContextCachePolicy extends LRUEnterpriseContextCachePolicy
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   /* The age after which a bean is automatically removed */
   private long m_maxBeanLife;
   /* The remover timer task */
   private TimerTask m_remover;
   /* The period of the remover's runs */
   private long m_removerPeriod;
   /**
    * The typed stateful cache
    */
   private StatefulSessionInstanceCache ssiCache;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   /**
    * Creates a LRU cache policy object given the instance cache that use this
    * policy object.
    */
   public LRUStatefulContextCachePolicy(AbstractInstanceCache eic)
   {
      super(eic);
      ssiCache = (StatefulSessionInstanceCache) eic;
   }

   // Public --------------------------------------------------------

   // Monitorable implementation ------------------------------------

   // Z implementation ----------------------------------------------
   public void start()
   {
      super.start();
      if (m_maxBeanLife > 0)
      {
         m_remover = new RemoverTask(m_removerPeriod);
         long delay = (long) (Math.random() * m_removerPeriod);
         tasksTimer.schedule(m_remover, delay, m_removerPeriod);
      }
   }

   public void stop()
   {
      if (m_remover != null)
      {
         m_remover.cancel();
      }
      super.stop();
   }

   /**
    * Reads from the configuration the parameters for this cache policy, that
    * are all optionals.
    */
   public void importXml(Element element) throws DeploymentException
   {
      super.importXml(element);

      String rp = MetaData.getElementContent(MetaData.getOptionalChild(element, "remover-period"));
      String ml = MetaData.getElementContent(MetaData.getOptionalChild(element, "max-bean-life"));
      try
      {
         if (rp != null)
         {
            int p = Integer.parseInt(rp);
            if (p <= 0)
            {
               throw new DeploymentException("Remover period can't be <= 0");
            }
            m_removerPeriod = p * 1000;
         }
         if (ml != null)
         {
            int a = Integer.parseInt(ml);
            if (a <= 0)
            {
               throw new DeploymentException("Max bean life can't be <= 0");
            }
            m_maxBeanLife = a * 1000;
         }
      }
      catch (NumberFormatException x)
      {
         throw new DeploymentException("Can't parse policy configuration", x);
      }
   }

   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   /**
    * This TimerTask removes beans that have not been called for a while.
    */
   protected class RemoverTask extends OveragerTask
   {
      protected RemoverTask(long period)
      {
         super(period);
      }

      protected String getTaskLogMessage()
      {
         return "Removing from cache bean";
      }

      protected void kickOut(LRUCacheEntry entry)
      {
         remove(entry.m_key);
      }

      protected long getMaxAge()
      {
         return m_maxBeanLife;
      }

      public void run()
      {
         if (ssiCache == null)
         {
            cancel();
            return;
         }

         synchronized (ssiCache.getCacheLock())
         {
            log.debug("Running RemoverTask");
            // Remove beans from cache and passivate them
            super.run();
            log.debug("RemoverTask, PassivatedCount=" + ssiCache.getPassivatedCount());
         }
         try
         {
            // Throw away any passivated beans that have expired
            ssiCache.removePassivated(getMaxAge() - super.getMaxAge());
            log.debug("RemoverTask, done");
         }
         catch (Throwable t)
         {
            log.debug("Ignored error trying to remove passivated beans from cache", t);
         }
      }
   }
}
