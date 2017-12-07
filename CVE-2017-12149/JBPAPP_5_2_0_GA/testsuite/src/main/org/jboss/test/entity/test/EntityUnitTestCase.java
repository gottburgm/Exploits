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
package org.jboss.test.entity.test;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

import org.jboss.test.entity.interfaces.TestEntity;
import org.jboss.test.entity.interfaces.TestEntityHome;
import org.jboss.test.entity.interfaces.TestEntityUtil;
import org.jboss.test.entity.interfaces.TestEntityValue;

/**
 * Some entity bean tests.
 *
 * @author    Adrian.Brock@HappeningTimes.com
 * @version   $Revision: 81036 $
 */
public class EntityUnitTestCase
   extends JBossTestCase
{
   public EntityUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(EntityUnitTestCase.class, "jboss-test-entity.jar");
   }

   public void testExternalRemoveAfterCreateThenRecreate()
      throws Exception
   {
      getLog().debug("Retrieving home");
      TestEntityHome home = TestEntityUtil.getHome();

      getLog().debug("Creating entity");
      TestEntityValue value = new TestEntityValue("key1", null);
      home.create(value);

      getLog().debug("Removing entity externally");
      home.removeExternal("key1");

      getLog().debug("Recreating the entity");
      home.create(value);
   }

   public void testInternalHomeRemoveAfterCreateThenRecreate()
      throws Exception
   {
      getLog().debug("Retrieving home");
      TestEntityHome home = TestEntityUtil.getHome();

      getLog().debug("Creating entity");
      TestEntityValue value = new TestEntityValue("key2", null);
      home.create(value);

      getLog().debug("Removing entity internally");
      home.remove("key2");

      getLog().debug("Recreating the entity");
      home.create(value);
   }

   public void testInternalBeanRemoveAfterCreateThenRecreate()
      throws Exception
   {
      getLog().debug("Retrieving home");
      TestEntityHome home = TestEntityUtil.getHome();

      getLog().debug("Creating entity");
      TestEntityValue value = new TestEntityValue("key3", null);
      TestEntity bean = home.create(value);

      getLog().debug("Removing entity internally");
      bean.remove();

      getLog().debug("Recreating the entity");
      home.create(value);
   }

   public void testChangeReadOnlyField()
      throws Exception
   {
      getLog().debug("Retrieving home");
      TestEntityHome home = TestEntityUtil.getHome();

      getLog().debug("Creating entity");
      TestEntityValue value = new TestEntityValue("key4", "original");
      TestEntity bean = home.create(value);

      getLog().debug("Access the value");
      assertEquals("original", bean.getValue1());

      getLog().debug("Change the value");
      home.changeValue1("key4", "changed");
      assertEquals("changed", bean.getValue1());
   }
}
