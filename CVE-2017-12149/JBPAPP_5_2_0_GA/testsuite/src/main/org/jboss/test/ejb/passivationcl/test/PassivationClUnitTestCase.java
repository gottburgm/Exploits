/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ejb.passivationcl.test;

import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.ejb.passivationcl.stateful.StatefulSession;
import org.jboss.test.ejb.passivationcl.stateful.StatefulSessionHome;

/**
 * 
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public class PassivationClUnitTestCase extends JBossTestCase
{
   public PassivationClUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(PassivationClUnitTestCase.class, "passivationcl.ear");
   }

   public void testMain() throws Exception
   {
      Object ref = getInitialContext().lookup("StatefulSession");
      StatefulSessionHome home = (StatefulSessionHome) PortableRemoteObject.narrow(ref, StatefulSessionHome.class);
      StatefulSession stateful = home.create();
      stateful.test();

      int i = 5;
      while(!stateful.isPassivated())
      {
         if(i == 0)
            fail("Gave up waiting for passivation.");
         try
         {
            Thread.sleep(1500);
         }
         catch(InterruptedException e)
         {
            //
         }
         --i;
      }

      stateful.remove();
   }
}