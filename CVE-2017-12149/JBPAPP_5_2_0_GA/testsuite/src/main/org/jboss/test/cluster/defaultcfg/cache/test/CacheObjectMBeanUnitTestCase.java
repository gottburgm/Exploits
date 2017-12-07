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

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;
import org.jboss.test.cluster.cache.bean.CacheObjectMeanTester;
import org.jboss.test.cluster.cache.bean.CacheObjectMeanTesterHome;
import org.jboss.test.JBossTestCase;

/**
 * 
 * 
 * @author Andrew D. May
 */
public class CacheObjectMBeanUnitTestCase extends JBossTestCase
{

   public static void main(String[] args) throws Exception
   {
      junit.textui.TestRunner.run(suite());
   }

   /**
    * Constructor for CacheObjectMBeanUnitTestCase.
    * @param arg0
    */
   public CacheObjectMBeanUnitTestCase(String arg0)
   {
      super(arg0);
   }

   public void testBinding() throws Exception
   {
      Hashtable props = new Hashtable();
      props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      props.put(Context.PROVIDER_URL, "localhost:1099");
      Context ctx = new InitialContext(props);
      CacheObjectMeanTesterHome home = (CacheObjectMeanTesterHome)ctx.lookup(
              CacheObjectMeanTesterHome.JNDI_NAME);
      CacheObjectMeanTester cacheTest = home.create();
      cacheTest.bind("id12345");
   }


   public static Test suite() throws Exception
   {
        return getDeploySetup(getDeploySetup(CacheObjectMBeanUnitTestCase.class, "cachetest.jar"),
            "cacheAoptest.sar");
   }

}
