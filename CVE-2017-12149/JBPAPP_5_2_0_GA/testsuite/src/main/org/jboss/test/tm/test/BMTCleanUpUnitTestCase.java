/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.tm.test;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.tm.interfaces.BMTCleanUp;
import org.jboss.test.tm.interfaces.BMTCleanUpHome;

/**
 * Tests for BMT CleanUp
 * 
 * @author adrian@jboss.com
 * @version $Revision: 85945 $
 */
public class BMTCleanUpUnitTestCase  extends JBossTestCase
{
   public BMTCleanUpUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return new JBossTestSetup(getDeploySetup(BMTCleanUpUnitTestCase.class, "bmtcleanuptest.jar"));
   }
   
   public void testIncomplete() throws Exception
   {
      BMTCleanUp bean = getBean();
      bean.testIncomplete();
   }
   
   public void testTxTimeout() throws Exception
   {
      BMTCleanUp bean = getBean();
      bean.testTxTimeout();
   }

   private BMTCleanUp getBean() throws Exception
   {
      BMTCleanUpHome home = (BMTCleanUpHome) getInitialContext().lookup("BMTCleanUp");
      return home.create();
   }
}
