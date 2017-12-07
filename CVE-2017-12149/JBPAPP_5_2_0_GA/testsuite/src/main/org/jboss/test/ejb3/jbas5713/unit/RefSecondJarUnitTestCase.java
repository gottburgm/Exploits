/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ejb3.jbas5713.unit;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * Let a bean from jar 1 reference a bean from jar 2 and see what happens.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class RefSecondJarUnitTestCase extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      return getDeploySetup(RefSecondJarUnitTestCase.class, "jbas5713.ear");
   }
   
   public RefSecondJarUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testServerFound() throws Exception
   {
      serverFound();
   }
}
