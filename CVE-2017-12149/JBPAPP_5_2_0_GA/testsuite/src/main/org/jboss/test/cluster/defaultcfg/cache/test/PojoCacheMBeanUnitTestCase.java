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
package org.jboss.test.cluster.defaultcfg.cache.test;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cluster.cache.bean.TreeCacheAopMBeanTester;
import org.jboss.test.cluster.cache.bean.TreeCacheAopMBeanTesterHome;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;
import java.util.Properties;

/**
 * Tests transactional access to a local TreeCache MBean service.
 *
 * @version $Revision: 85945 $
 */
public class PojoCacheMBeanUnitTestCase extends JBossTestCase
{
   TreeCacheAopMBeanTesterHome cache_home;
   TreeCacheAopMBeanTester cache1 = null, cache2 = null;
   Properties p_ = new Properties();


   public PojoCacheMBeanUnitTestCase(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      super.setUp();
        mySetup();
   }

   public void tearDown() throws Exception
   {
      if (cache2 != null)
         cache2.remove(); // calls stop()
      if (cache1 != null)
         cache1.remove();
   }

   public void mySetup() throws Exception
   {
      Object obj;

      p_.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      p_.put(Context.URL_PKG_PREFIXES, "jboss.naming:org.jnp.interfaces");
      p_.put(Context.PROVIDER_URL, "localhost:1099");
      obj = new InitialContext(p_).lookup(TreeCacheAopMBeanTesterHome.JNDI_NAME);
      cache_home = (TreeCacheAopMBeanTesterHome) PortableRemoteObject.narrow(obj, TreeCacheAopMBeanTesterHome.class);
   }


   public void testSetup()
   {
      assertNotNull("TreeCacheAopTesterHome ", cache_home);
   }

   public void testPutObjectTx()
   {
      UserTransaction tx = null;

      try {
         tx = (UserTransaction) new InitialContext(p_).lookup("UserTransaction");
         assertNotNull("UserTransaction should not be null ", tx);
         // Note that to set tree cache properties, you can do it here
         // or go to transient-cache-service.xml.
         cache1 = cache_home.create();

//         tx.begin();
         log("Create person ...");
         cache1.createPerson("/aop/person", "Benito", 38);
//         tx.commit();

//         tx.begin();
         log("check equalityu ...");
         assertEquals(38, cache1.getAge("/aop/person"));
//         tx.commit();

      } catch (Throwable t) {
         fail(t.toString());
         try {
            tx.rollback();
         } catch (Throwable t2) {
            ;
         }
         fail(t.toString());
      }
   }


   void _sleep(long timeout)
   {
      try {
         Thread.sleep(timeout);
      } catch (InterruptedException e) {
      }
   }

   void log(String msg)
   {
      getLog().info("-- [" + Thread.currentThread() + "]: " + msg);
   }


   public static Test suite() throws Exception
   {
//        return getDeploySetup(MBeanUnitTestCase.class, "cachetest.sar");
      // Deploy the package recursively. The jar contains ejb and the sar file contains
      // tree cache MBean service
        return getDeploySetup(getDeploySetup(PojoCacheMBeanUnitTestCase.class, "cachetest.jar"),
            "cacheAoptest.sar");
//      return new TestSuite(MBeanUnitTestCase.class);
   }

   public static void main(String[] args) throws Exception
   {
      junit.textui.TestRunner.run(suite());
   }


}
