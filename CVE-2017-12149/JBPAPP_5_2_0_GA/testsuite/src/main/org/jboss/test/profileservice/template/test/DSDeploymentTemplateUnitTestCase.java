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
package org.jboss.test.profileservice.template.test;

import java.util.Collection;
import java.util.Collections;

import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.resource.deployers.management.DsDataSourceTemplateInfo;
import org.jboss.resource.metadata.mcf.LocalDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.NoTxConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.NoTxDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.TxConnectionFactoryDeploymentMetaData;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class DSDeploymentTemplateUnitTestCase extends AbstractTemplateTest
{

   public DSDeploymentTemplateUnitTestCase(String name)
   {
      super(name);
   }

   public boolean isDebug()
   {
      return debug;
   }
   
   @Override
   protected Collection<String> getExcludes()
   {
      return Collections.singleton("dsType");
   }
   
   public void testLocalTxDataSourceTemplateInfo() throws Exception
   {
      assertTemplate("LocalTxDataSourceTemplateInfo",
            LocalDataSourceDeploymentMetaData.class);
   }

   public void testNoTxDataSourceTemplateInfo() throws Exception
   {
      assertTemplate("NoTxDataSourceTemplateInfo",
            NoTxDataSourceDeploymentMetaData.class);
   }
   
   public void testTxConnectionFactoryTemplateInfo() throws Exception
   {
      assertTemplate("TxConnectionFactoryTemplateInfo",
            TxConnectionFactoryDeploymentMetaData.class);
   }

   public void testNoTxConnectionFactoryTemplateInfo() throws Exception
   {
      assertTemplate("NoTxConnectionFactoryTemplateInfo",
            NoTxConnectionFactoryDeploymentMetaData.class);
   }
   
   protected DeploymentTemplateInfo createDeploymentInfo(String name, Class<?> attachment) throws Exception
   {
      DeploymentTemplateInfo info = getFactory().createTemplateInfo(DsDataSourceTemplateInfo.class, attachment, name, null);
      // populate the values
      ((DsDataSourceTemplateInfo) info).start();
      return info;
   }
   
}
