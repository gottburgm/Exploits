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

import javax.management.ObjectName;

import org.jboss.system.ServiceMBean;
import org.jboss.test.JBossTestCase;

/**
 * ScopedRARNoJBossAppUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: 85945 $
 */
public class ScopedRARNoJBossAppUnitTestCase extends JBossTestCase
{

   public ScopedRARNoJBossAppUnitTestCase(String name)
   {
      super(name);
   }

   public void testScopedRAR() throws Exception
   {
      deploy("testscopedrar-nojbossapp.ear");
      try
      {
         deploy("testscopedrar-nojbossapp.jar");
         try
         {
            ObjectName name = new ObjectName("jboss.test:test=Scoped");
            int state = (Integer) getServer().getAttribute(name, "State");
            assertEquals(ServiceMBean.STARTED, state);
         }
         finally
         {
            undeploy("testscopedrar-nojbossapp.jar");
         }
      }
      finally
      {
         undeploy("testscopedrar-nojbossapp.ear");
      }
   }
}
