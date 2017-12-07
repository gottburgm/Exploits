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
package org.jboss.test.cts.test;

import javax.naming.InitialContext;

import org.jboss.test.cts.jms.ContainerMBox;
import org.jboss.test.cts.interfaces.CtsCmp2Session;
import org.jboss.test.cts.interfaces.CtsCmp2SessionHome;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;

/** Tests of versioned deployments using ear scoped class loader.
 *  @author Scott.Stark@jboss.org
 *  @version $Revision: 81036 $
 */
public class CtsCmp2UnitTestCase extends JBossTestCase
{
   public CtsCmp2UnitTestCase(String name)
   {
      super(name);
   }

   public void testV1() throws Exception
   {
      try 
      {
         deploy("cts-v1cmp.ear");
         InitialContext ctx = new InitialContext();
         CtsCmp2SessionHome home = (CtsCmp2SessionHome) ctx.lookup("v1/CtsCmp2SessionBean");
         CtsCmp2Session session = home.create();
         session.testV1();
         log.info("Invoked CtsCmp2Session.testV1");
      }
      finally
      {
         undeploy("cts-v1cmp.ear");
      } // end of try-catch
   }

   public void testV2() throws Exception
   {
      try 
      {
         deploy("cts-v1cmp.ear");
         try 
         {
            deploy("cts-v2cmp.ear");
            InitialContext ctx = new InitialContext();
            CtsCmp2SessionHome home = (CtsCmp2SessionHome) ctx.lookup("v2/CtsCmp2SessionBean");
            CtsCmp2Session session = home.create();
            session.testV2();
            log.info("Invoked CtsCmp2Session.testV2");
         }
         finally
         {
            undeploy("cts-v2cmp.ear");
         } // end of finally
      }
      finally
      {
         undeploy("cts-v1cmp.ear");
      } // end of try-catch
   }

   public void testV1Sar() throws Exception
   {
      try 
      {
         deploy("cts-v1cmp-sar.ear");
         InitialContext ctx = new InitialContext();
         CtsCmp2SessionHome home = (CtsCmp2SessionHome) ctx.lookup("v1/CtsCmp2SessionBean");
         CtsCmp2Session session = home.create();
         session.testV1();
      }
      finally
      {
         undeploy("cts-v1cmp-sar.ear");
      } // end of try-catch
   }

   public void testV2Sar() throws Exception
   {
      try 
      {
         getLog().debug("Deploying cts-v1cmp-sar.ear");
         deploy("cts-v1cmp-sar.ear");
         getLog().debug("Deployed cts-v1cmp-sar.ear");
         try 
         {
            getLog().debug("Deploying cts-v2cmp-sar.ear");
            deploy("cts-v2cmp-sar.ear");
            getLog().debug("Deployed cts-v2cmp-sar.ear");
            InitialContext ctx = new InitialContext();
            CtsCmp2SessionHome home = (CtsCmp2SessionHome) ctx.lookup("v2/CtsCmp2SessionBean");
            getLog().debug("Found CtsCmp2SessionHome");
            CtsCmp2Session session = home.create();
            getLog().debug("Created CtsCmp2Session");
            session.testV2();
            getLog().debug("Invoked CtsCmp2Session.testV2()");
         }
         finally
         {
            undeploy("cts-v2cmp-sar.ear");
         } // end of finally
      }
      finally
      {
         undeploy("cts-v1cmp-sar.ear");
      } // end of try-catch
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new CtsCmp2UnitTestCase("testV1"));
      suite.addTest(new CtsCmp2UnitTestCase("testV2"));
      suite.addTest(new CtsCmp2UnitTestCase("testV1Sar"));
      suite.addTest(new CtsCmp2UnitTestCase("testV2Sar"));
      return suite;
   }
}
