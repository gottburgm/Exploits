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
package org.jboss.test.ejb3.ejbthree1597;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.cache.RegionFactory;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.SessionImplementor;

/**
 * @author Paul Ferraro
 *
 */
@Stateless(mappedName="ejb3/ejbthree1597/PersistenceUnitSettings")
public class PersistenceUnitSettingsBean implements PersistenceUnitSettings
{
   @PersistenceContext
   private EntityManager em;

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#getCacheProvider()
    */
   public String getCacheProvider()
   {
      RegionFactory factory = this.settings().getRegionFactory();
      
      try
      {
         // Extract CacheProvider from RegionFactoryCacheProviderBridge
         return factory.getClass().getMethod("getCacheProvider").invoke(factory).getClass().getName();
      }
      catch (Exception e)
      {
         return factory.getClass().getName();
      }
   }

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#getTransactionManagerLookup()
    */
   public String getTransactionManagerLookup()
   {
      return this.settings().getTransactionManagerLookup().getClass().getName();
   }

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#getConnectionReleaseMode()
    */
   public String getConnectionReleaseMode()
   {
      return this.settings().getConnectionReleaseMode().toString();
   }

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#getDialect()
    */
   public String getDialect()
   {
      return this.settings().getDialect().getClass().getName();
   }

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#getQueryTranslatorFactory()
    */
   public String getQueryTranslatorFactory()
   {
      return this.settings().getQueryTranslatorFactory().getClass().getName();
   }

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#isAutoCloseSessionEnabled()
    */
   public boolean isAutoCloseSessionEnabled()
   {
      return this.settings().isAutoCloseSessionEnabled();
   }

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#isAutoCreateSchema()
    */
   public boolean isAutoCreateSchema()
   {
      return this.settings().isAutoCreateSchema();
   }

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#isAutoDropSchema()
    */
   public boolean isAutoDropSchema()
   {
      return this.settings().isAutoDropSchema();
   }

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#isAutoUpdateSchema()
    */
   public boolean isAutoUpdateSchema()
   {
      return this.settings().isAutoUpdateSchema();
   }

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#isAutoValidateSchema()
    */
   public boolean isAutoValidateSchema()
   {
      return this.settings().isAutoValidateSchema();
   }

   /**
    * @{inheritDoc}
    * @see org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings#isFlushBeforeCompletionEnabled()
    */
   public boolean isFlushBeforeCompletionEnabled()
   {
      return this.settings().isFlushBeforeCompletionEnabled();
   }

   private Settings settings()
   {
      SessionImplementor session = (SessionImplementor) this.em.getDelegate();
      return session.getFactory().getSettings();
   }
}
