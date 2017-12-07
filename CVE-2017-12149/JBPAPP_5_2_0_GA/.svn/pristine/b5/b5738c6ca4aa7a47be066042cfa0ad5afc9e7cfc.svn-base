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
package org.jboss.test.jca.test;

import javax.management.Attribute;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.interfaces.UnshareableConnectionSession;
import org.jboss.test.jca.interfaces.UnshareableConnectionSessionHome;

/**
 * Tests unshared connections
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 */

public class UnsharedConnectionUnitTestCase extends JBossTestCase
{

   private UnshareableConnectionSessionHome home;
   private UnshareableConnectionSession bean;

   public UnsharedConnectionUnitTestCase (String name)
   {
      super(name);
   }

   protected void setSpecCompliant(Boolean value)
      throws Exception
   {
      ObjectName CCM = new ObjectName("jboss.jca:service=CachedConnectionManager");
      getServer().setAttribute(CCM, new Attribute("SpecCompliant", value));
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      setSpecCompliant(Boolean.TRUE);
      home = (UnshareableConnectionSessionHome)getInitialContext().lookup("UnshareableStateless");
      bean = home.create();
   }

   protected void tearDown() throws Exception
   {
      setSpecCompliant(Boolean.FALSE);
      super.tearDown();
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(UnsharedConnectionUnitTestCase.class, "jcatest-unshared.jar");
   }

   public void testUnsharedConnection() throws Exception
   {
      bean.runTest();
   }
}
