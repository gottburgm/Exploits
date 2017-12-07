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

import java.util.HashMap;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.util.CachePolicy;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.monitor.Monitorable;
import org.jboss.monitor.client.BeanCacheSnapshot;
import org.w3c.dom.Element;

/**
 * Implementation of a no passivation cache policy.
 *
 * @see AbstractInstanceCache
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 81030 $
 */
public class NoPassivationCachePolicy
   implements CachePolicy, Monitorable, XmlLoadable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private HashMap m_map;

   /** Whether you can flush the cache */
   private boolean flushEnabled = false;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   /**
    * Creates a no passivation cache policy object given the instance
    * cache that use this policy object, that btw is not used.
    */
   public NoPassivationCachePolicy(AbstractInstanceCache eic)
   {
   }

   // Public --------------------------------------------------------

   // Monitorable implementation

   public void sample(Object s)
   {
      if(m_map == null)
         return;

      synchronized(m_map)
      {
         BeanCacheSnapshot snapshot = (BeanCacheSnapshot)s;
         snapshot.m_passivatingBeans = 0;
         snapshot.m_cacheMinCapacity = 0;
         snapshot.m_cacheMaxCapacity = Integer.MAX_VALUE;
         snapshot.m_cacheCapacity = Integer.MAX_VALUE;
         snapshot.m_cacheSize = m_map.size();
      }
   }

   // Z implementation ----------------------------------------------
   public void create() throws Exception
   {
      m_map = new HashMap();
   }

   public void start() throws Exception
   {
   }

   public void stop()
   {
   }

   public void destroy()
   {
      synchronized (m_map)
      {
         m_map.clear();
      }
   }

   public Object get(Object key)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("Requesting an object using a null key");
      }
      EnterpriseContext ctx = null;
      synchronized (m_map)
      {
         ctx = (EnterpriseContext) m_map.get(key);
      }
      return ctx;
   }

   public Object peek(Object key)
   {
      return get(key);
   }

   public void insert(Object key, Object ctx)
   {
      if (ctx == null)
      {
         throw new IllegalArgumentException("Cannot insert a null object in the cache");
      }
      if (key == null)
      {
         throw new IllegalArgumentException("Cannot insert an object in the cache with null key");
      }

      synchronized (m_map)
      {
         Object obj = m_map.get(key);
         if (obj == null)
         {
            m_map.put(key, ctx);
         }
         else
         {
            throw new IllegalStateException("Attempt to put in the cache an object that is already there");
         }
      }
   }

   public void remove(Object key)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("Removing an object using a null key");
      }

      synchronized (m_map)
      {
         Object value = m_map.get(key);
         if (value != null)
         {
            m_map.remove(key);
         }
         else
         {
            throw new IllegalArgumentException("Cannot remove an object that isn't in the cache");
         }
      }
   }

   public void flush()
   {
      if (flushEnabled)
      {
         synchronized (m_map)
         {
            m_map.clear();
         }
      }
   }

   public int size()
   {
      synchronized (m_map)
      {
         return m_map.size();
      }
   }

   public void importXml(Element element) throws DeploymentException
   {
      String flushString = MetaData.getElementContent(MetaData.getOptionalChild(element, "flush-enabled"));
      flushEnabled = Boolean.valueOf(flushString).booleanValue();
   }

   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
