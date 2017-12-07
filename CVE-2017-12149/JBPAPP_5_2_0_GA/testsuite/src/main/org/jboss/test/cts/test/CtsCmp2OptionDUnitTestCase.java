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

import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.cts.interfaces.CtsCmpHome;
import org.jboss.test.cts.keys.AccountPK;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/** 
 * Tests OptionD.
 * 
 * @author Adrian Brock
 * @version $Revision: 105321 $
 */
public class CtsCmp2OptionDUnitTestCase extends JBossTestCase
{
   public CtsCmp2OptionDUnitTestCase(String name)
   {
      super(name);
   }

   public void testOptionD() throws Exception
   {
      InitialContext ctx = new InitialContext();
      CtsCmpHome home = (CtsCmpHome) ctx.lookup("ejbcts/CMPBeanOptionD");
      AccountPK pk = new AccountPK("testOptionD-1");
      home.create(pk, "name1");
      pk = new AccountPK("testOptionD-2");
      home.create(pk, "name2");
      
      ObjectName cache = new ObjectName("jboss.j2ee:service=EJB,jndiName=ejbcts/CMPBeanOptionD,plugin=cache");
      Long cacheSize = (Long) getServer().getAttribute(cache, "CacheSize");
      assertEquals(2, cacheSize.longValue());
      
      Thread.sleep(15000); // 15 seconds
      
      cacheSize = (Long) getServer().getAttribute(cache, "CacheSize");
      assertEquals(0, cacheSize.longValue());
   }

   public static Test suite() throws Exception
   {
      return new JBossTestSetup(new TestSuite(CtsCmp2OptionDUnitTestCase.class))
      {
         public void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            deploy("cts.jar");
            
         }
         
         public void tearDown() throws Exception
         {
            undeploy("cts.jar");
            JMSDestinationsUtil.destroyDestinations();
         }
      };
      
   }
}
