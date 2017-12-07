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
package org.jboss.test.profileservice.test;

import java.util.Map;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;

/**
 * Additional tests of managing DataSources
 * @author Scott.Stark@jboss.org
 * @version $Revision: 93155 $
 */
public class DataSourceUnitTestCase extends AbstractProfileServiceTest
{
   public DataSourceUnitTestCase(String s)
   {
      super(s);
   }

   /**
    * JBAS-6672 related testing of properties having default values
    * @throws Exception
    */
   public void testRemovedProperties() throws Exception
   {
      ManagementView mgtView = getManagementView();
      String templateName = "LocalTxDataSourceTemplate";
      String jndiName = "testRemovedPropertiesDS";
      DeploymentTemplateInfo dsInfo = mgtView.getTemplate(templateName);
      assertNotNull("template " + templateName + " found", dsInfo);
      log.info(dsInfo.getProperties().keySet());
      Map<String, ManagedProperty> props = dsInfo.getProperties();

      // Set key property values
      ManagedProperty jndiNameMP = props.get("jndi-name");
      jndiNameMP.setValue(SimpleValueSupport.wrap(jndiName));
      ManagedProperty driverClass = props.get("driver-class");
      driverClass.setValue(SimpleValueSupport.wrap("org.hsqldb.jdbcDriver"));
      ManagedProperty connUrl = props.get("connection-url");
      connUrl.setValue(SimpleValueSupport.wrap("jdbc:hsqldb:."));
      ManagedProperty userName = props.get("user-name");
      userName.setValue(SimpleValueSupport.wrap("sa"));
      ManagedProperty password = props.get("password");
      password.setValue(SimpleValueSupport.wrap(""));

      // Remove the 
      ManagedProperty useJavaCtx = props.get("use-java-context");
      SimpleValue nullBoolean = SimpleValueSupport.wrap(false);
      ((SimpleValueSupport)nullBoolean).setValue(null);
      useJavaCtx.setValue(nullBoolean);
      useJavaCtx.setRemoved(true);
      
      mgtView.applyTemplate("testRemovedProperties", dsInfo);

      // reload the view and new datasource component
      ComponentType componentType = new ComponentType("DataSource", "LocalTx");
      activeView = null;
      mgtView = getManagementView();
      ManagedComponent dsMC = getManagedComponent(mgtView, componentType, jndiName);
      assertNotNull(dsMC);

      // Validate that the use-java-context value is true
      useJavaCtx = dsMC.getProperty("use-java-context");
      assertNotNull(useJavaCtx);
      assertEquals(SimpleValueSupport.wrap(Boolean.TRUE), useJavaCtx.getValue());
      
      // Update the use-java-context value
      ManagedProperty minPoolSize = dsMC.getProperty("min-pool-size");
      MetaValue oldValue = minPoolSize.getValue();
      minPoolSize.setValue(SimpleValueSupport.wrap(1));
      minPoolSize.setRemoved(true);
      ManagedProperty maxPoolSize = dsMC.getProperty("max-pool-size");
      maxPoolSize.setValue(SimpleValueSupport.wrap(999));
      activeView.updateComponent(dsMC);
      dsMC = getManagedComponent(mgtView, componentType, jndiName);
      assertNotNull(dsMC);
      minPoolSize = dsMC.getProperty("min-pool-size");
      assertEquals(oldValue, minPoolSize.getValue());
      maxPoolSize = dsMC.getProperty("max-pool-size");
      assertEquals(SimpleValueSupport.wrap(999), maxPoolSize.getValue());
      
      // Remove the deployment
      removeDeployment("testRemovedProperties-ds.xml");
   }

}
