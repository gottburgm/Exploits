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

import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.interfaces.HAConnectionSessionHome;
import org.jboss.resource.adapter.jdbc.local.LocalManagedConnectionFactory;

import java.util.Arrays;

import junit.framework.Test;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 113233 $</tt>
 */
public class HAConnectionFactoryUnitTestCase
   extends JBossTestCase
{
   /**
    * Constructor for the JBossTestCase object
    *
    * @param name Test case name
    */
   public HAConnectionFactoryUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      Test t;
      t = getDeploySetup(HAConnectionFactoryUnitTestCase.class, "jcatest.jar");
	  //t = getDeploySetup(t, "jboss-ha-local-jdbc.rar");
      //t = getDeploySetup(t, "jboss-ha-xa-jdbc.rar");	
      t = getDeploySetup(t, "test-ha-ds.xml");
      t = getDeploySetup(t, "test-ha-xa-ds.xml");
      t = getDeploySetup(t, "jbosstestadapter.rar");
	  t = getDeploySetup(t, "jca-support.sar");

      // this is for deploying the ha rar which is not deployed by default
      // FIXME: is there a better way to do it?
      //String connectorLib = System.getProperty("jbosstest.deploy.dir") + "/../../../connector/output/lib/";
      // these RARs are now in the all/default config?
      //t = getDeploySetup(t, new File(connectorLib + "jboss-ha-local-jdbc.rar").toURL().toString());
      //t = getDeploySetup(t, new File(connectorLib + "jboss-ha-xa-jdbc.rar").toURL().toString());
      return t;
   }

   public void testFailoverLocalMCF() throws Exception
   {
      HAConnectionSessionHome home = (HAConnectionSessionHome)getInitialContext().lookup("HAConnectionSession");
      home.create().testHaLocalConnection();
   }
   
   public void testFailoverXaMCF() throws Exception
   {
      HAConnectionSessionHome home = (HAConnectionSessionHome)getInitialContext().lookup("HAConnectionSession");
      home.create().testHaXaConnection();
   }


   public void testURLSelector() throws Exception
   {
      Object[] urls = new Object[]{"url1", "url2", "url3"};
      LocalManagedConnectionFactory.URLSelector selector = new LocalManagedConnectionFactory.URLSelector(
         Arrays.asList(urls)
      );

      String url = selector.getUrl();
      assertEquals(urls[0], url);
      url = selector.getUrl();
      assertEquals(urls[0], url);
      url = selector.getUrl();
      assertEquals(urls[0], url);

      selector.failedUrl(url);
      url = selector.getUrl();
      assertEquals(urls[1], url);
      url = selector.getUrl();
      assertEquals(urls[1], url);

      selector.failedUrl(url);
      url = selector.getUrl();
      assertEquals(urls[2], url);
      url = selector.getUrl();
      assertEquals(urls[2], url);

      for(int i = 0; i < 10; ++i)
      {
         selector.failedUrl(url);
         url = selector.getUrl();
         assertEquals(urls[i % 3], url);
      }
   }
}
