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
package org.jboss.test.ejb3.jbas6161.unit;

import java.util.Date;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.ejb3.jbas6161.Greeter;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class SameNamedJarUnitTestCase extends JBossTestCase
{
   public static Test suite() throws Exception
   {
      return getDeploySetup(SameNamedJarUnitTestCase.class, "jbas6161-A.ear,jbas6161-B.ear");
   }
   
   public SameNamedJarUnitTestCase(String name)
   {
      super(name);
   }

   protected <T> T lookup(String name, Class<T> cls) throws Exception
   {
      return cls.cast(getInitialContext().lookup(name));
   }
   
   /**
    * Do some simple beans to both session beans in each ear.
    */
   public void testBeanCalls() throws Exception
   {
      String earNames[] = { "jbas6161-A", "jbas6161-B" };
      for(String earName : earNames)
      {
         Greeter bean = lookup(earName + "/SimpleSessionBean/remote", Greeter.class);
         String name = new Date().toString();
         String actual = bean.sayHiTo(name);
         assertEquals("Hi " + name, actual);
      }
   }
   
   public void testServerFound() throws Exception
   {
      serverFound();
   }
}
