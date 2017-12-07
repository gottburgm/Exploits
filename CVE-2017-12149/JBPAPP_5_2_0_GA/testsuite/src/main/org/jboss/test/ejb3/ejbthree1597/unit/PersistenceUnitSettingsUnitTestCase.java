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
package org.jboss.test.ejb3.ejbthree1597.unit;

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.ejb3.ejbthree1597.PersistenceUnitSettings;

/**
 * Validates that EntityManager is created using the expected default settings.
 *
 * @author Paul Ferraro
 */
public class PersistenceUnitSettingsUnitTestCase extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      return getDeploySetup(PersistenceUnitSettingsUnitTestCase.class, "ejbthree1597.jar");
   }

   public PersistenceUnitSettingsUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testDefaultSettings() throws Exception
   {
      InitialContext ctx = this.getInitialContext();
      PersistenceUnitSettings settings = (PersistenceUnitSettings) ctx.lookup("ejb3/ejbthree1597/PersistenceUnitSettings");

      assertEquals("org.hibernate.cache.HashtableCacheProvider", settings.getCacheProvider());
      assertEquals("org.hibernate.transaction.JBossTransactionManagerLookup", settings.getTransactionManagerLookup());
      assertEquals("after_statement", settings.getConnectionReleaseMode());
      assertEquals("org.hibernate.dialect.HSQLDialect", settings.getDialect());
      assertEquals("org.hibernate.hql.ast.ASTQueryTranslatorFactory", settings.getQueryTranslatorFactory());
      assertFalse(settings.isAutoCloseSessionEnabled());
      assertFalse(settings.isAutoCreateSchema());
      assertFalse(settings.isAutoDropSchema());
      assertFalse(settings.isAutoUpdateSchema());
      assertFalse(settings.isAutoValidateSchema());
      assertFalse(settings.isFlushBeforeCompletionEnabled());
   }
}
