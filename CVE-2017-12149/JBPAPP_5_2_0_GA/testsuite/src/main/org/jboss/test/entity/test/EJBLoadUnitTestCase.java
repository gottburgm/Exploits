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

import org.jboss.test.entity.interfaces.EJBLoad;
import org.jboss.test.entity.interfaces.EJBLoadHome;

/**
 * Test that ejbLoad is called.
 *
 * @author    Adrian.Brock@HappeningTimes.com
 * @version   $Revision: 81036 $
 */
public class EJBLoadUnitTestCase
   extends JBossTestCase
{
   public EJBLoadUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(EJBLoadUnitTestCase.class, "jboss-test-ejbload.jar");
   }

   public void testNoTransactionCommitB()
      throws Exception
   {
      getLog().debug("Retrieving enitity");
      EJBLoad entity = getEJBLoadHomeB().findByPrimaryKey("Entity");
      entity.wasEJBLoadCalled();

      getLog().debug("Testing that ejb load is invoked again");
      entity.noTransaction();
      assertTrue("Should reload for option b after access outside a transaction", entity.wasEJBLoadCalled());
   }

   public void testNoTransactionCommitC()
      throws Exception
   {
      getLog().debug("Retrieving enitity");
      EJBLoad entity = getEJBLoadHomeC().findByPrimaryKey("Entity");
      entity.wasEJBLoadCalled();

      getLog().debug("Testing that ejb load is invoked again");
      entity.noTransaction();
      assertTrue("Should reload for option c after access outside a transaction", entity.wasEJBLoadCalled());
   }

   private EJBLoadHome getEJBLoadHomeB()
      throws Exception
   {
      return (EJBLoadHome) getInitialContext().lookup("EJBLoadB");
   }

   private EJBLoadHome getEJBLoadHomeC()
      throws Exception
   {
      return (EJBLoadHome) getInitialContext().lookup("EJBLoadC");
   }
}
