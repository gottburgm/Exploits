/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.iiop.test;

import org.omg.CORBA.ORB;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import org.jboss.test.iiop.jbpapp6462.generated.TestServant;
import org.jboss.test.iiop.jbpapp6462.generated.TestServantHelper;

import org.jboss.test.JBossTestCase;

/**
 * Test for JBPAPP-6462.
 *
 * A CORBA client which calls a CORBA servant which calls an in-EAR EJB when
 * the related EAR is isolated.
 *
 * @author jiwils
 */
public class JBPAPP6462TestCase extends JBossTestCase
{
   private final String DEPLOYMENT_URL = "jbpapp6462.ear";

   public JBPAPP6462TestCase(String name)
   {
      super(name);
   }

   /* TestCase Overrides */

   protected void setUp()
   throws Exception
   {
      super.setUp();
      deploy(DEPLOYMENT_URL);
   }

   protected void tearDown()
   throws Exception
   {
      super.tearDown();
      undeploy(DEPLOYMENT_URL);
   }

   public void testJBPAPP6462()
   throws org.omg.CORBA.ORBPackage.InvalidName, CannotProceed,
          InvalidName, NotFound
   {
      ORB orb = ORB.init(new String[0], null);

      org.omg.CORBA.Object nameServiceRef =
         orb.resolve_initial_references("NameService");

      NamingContextExt nce = NamingContextExtHelper.narrow(nameServiceRef);
      org.omg.CORBA.Object testServantRef = nce.resolve_str("jbpapp6462");
      TestServant testServant = TestServantHelper.narrow(testServantRef);
      testServant.testServantMethod();
      orb.shutdown(true);
   }
}