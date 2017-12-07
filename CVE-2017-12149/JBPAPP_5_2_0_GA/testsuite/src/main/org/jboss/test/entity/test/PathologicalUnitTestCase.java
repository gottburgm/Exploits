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

import org.jboss.test.entity.interfaces.EntitySession;
import org.jboss.test.entity.interfaces.EntitySessionHome;

/**
 * Some entity bean tests.
 *
 * @author    Adrian.Brock@HappeningTimes.com
 * @version   $Revision: 81036 $
 */
public class PathologicalUnitTestCase
   extends JBossTestCase
{
   public PathologicalUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(PathologicalUnitTestCase.class, "jboss-test-pathological-entity.jar");
   }

   public void testErrorFromEjbCreate()
      throws Exception
   {
      getLog().debug("Retrieving session");
      EntitySession session = getEntitySessionEJB();

      getLog().debug("Testing error from ejbCreate");
      session.createPathological("ejbCreate", true);
   }

   public void testErrorFromRemove()
      throws Exception
   {
      getLog().debug("Retrieving session");
      EntitySession session = getEntitySessionEJB();

      getLog().debug("Creating entity");
      session.createPathological("remove", false);

      getLog().debug("Testing error from remove");
      session.removeHomePathological("remove", true);
   }

   public void testErrorFromEjbRemove()
      throws Exception
   {
      getLog().debug("Retrieving session");
      EntitySession session = getEntitySessionEJB();

      getLog().debug("Creating entity");
      session.createPathological("remove", false);

      getLog().debug("Testing error from remove");
      session.removePathological("remove", true);
   }

   public void testErrorFromFind()
      throws Exception
   {
      getLog().debug("Retrieving session");
      EntitySession session = getEntitySessionEJB();

      getLog().debug("Creating entity");
      session.createPathological("find", false);

      getLog().debug("Testing error from find");
      session.findPathological("find", true);
   }

   public void testErrorFromGet()
      throws Exception
   {
      getLog().debug("Retrieving session");
      EntitySession session = getEntitySessionEJB();

      getLog().debug("Creating entity");
      session.createPathological("get", false);

      getLog().debug("Testing error from get");
      session.getPathological("get", true);
   }

   public void testErrorFromSet()
      throws Exception
   {
      getLog().debug("Retrieving session");
      EntitySession session = getEntitySessionEJB();

      getLog().debug("Creating entity");
      session.createPathological("set", false);

      getLog().debug("Testing error from set");
      session.setPathological("set", true);
   }

   private EntitySession getEntitySessionEJB()
      throws Exception
   {
      return ((EntitySessionHome) getInitialContext().lookup("EntitySessionEJB")).create();
   }
}
