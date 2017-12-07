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
package org.jboss.test.ejb.lifecycle.test;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.jboss.ejb.Container;
import org.jboss.test.cts.test.IndependentJarsUnitTestCase;

/**
 * LifecycleTest based on the cts TestCase
 * @see {@linkplain IndependentJarsUnitTestCase}
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class IndependentJarLifeCycleUnitTestCase extends AbstractLifeCycleTestWrapper
{

   /** The package */
   private static final String PACKAGE = "cts.jar";
   
   /** The callerBean jmxName */
   private static final String callerBean = Container.BASE_EJB_CONTAINER_NAME + ",jndiName=CallerSessionHome";
   
   public IndependentJarLifeCycleUnitTestCase(String name)
   {
      super(name);
   }

   public void testRestartContainer() throws Exception
   {
      restart(callerBean);
   }
   
   public void testRestartPool() throws Exception
   {
      String poolName = callerBean + ",plugin=pool";
      restart(poolName);
   }
   
   protected Enumeration<TestCase> getTests()
   {
      return getTestCases(IndependentJarsUnitTestCase.class);
   }

   @Override
   protected String getPackage()
   {
      return PACKAGE;
   }

}

