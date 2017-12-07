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
package org.jboss.test.kernel.deployment.jboss.test;

import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;

import org.jboss.test.JBossTestCase;
import org.jboss.test.kernel.deployment.jboss.beans.configpojo.ConfigPOJO;

/**
 * Deployment tests.
 * 
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class ConfigPOJOUnitTestCase extends JBossTestCase
{
   public ConfigPOJOUnitTestCase(String test)
   {
      super(test);
   }
   
   public void testConfigPOJO() throws Exception
   {
      try
      {
         deploy("testkernel-configpojo.beans");
         
         InitialContext ctx = new InitialContext();
         ConfigPOJO config = (ConfigPOJO) ctx.lookup("test/kernel/deployment/config/pojo");
         assertEquals("joe", config.getUserId());
         assertEquals("secret", config.getPassword());
         List roles = config.getRoles();
         assertEquals("trader", (String)roles.get(0));
         assertEquals("manager", (String)roles.get(1));
      }
      catch (Exception e)
      {
         getLog().info("Caught exception", e);
         throw e;
      }
      finally
      {
         undeploy("testkernel-configpojo.beans");
      }
   }
   
   public void testConfigProperties() throws Exception
   {
      try
      {
         deploy("testkernel-configproperties.beans");
         
         InitialContext ctx = new InitialContext();
         Properties props = (Properties) ctx.lookup("test/kernel/deployment/config/properties");
         assertEquals("value1", props.getProperty("config.property1"));
         assertEquals("value2", props.getProperty("config.property2"));
         assertEquals("value3", props.getProperty("config.property3"));
      }
      catch (Exception e)
      {
         getLog().info("Caught exception", e);
         throw e;
      }      
      finally
      {
         undeploy("testkernel-configproperties.beans");
      }
   }   
}
