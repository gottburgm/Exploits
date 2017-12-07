/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployment;

import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.metadata.web.spec.WebMetaData;

/**
 * An ObjectModelFactoryDeployer for translating web.xml descriptors into
 * WebMetaData instances.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class WebAppParsingDeployer extends SchemaResolverDeployer<WebMetaData>
{
   public WebAppParsingDeployer()
   {
      super(WebMetaData.class);
      setName("web.xml");
   }

   /**
    * Get the virtual file path for the web-app descriptor in the
    * DeploymentContext.getMetaDataPath.
    * 
    * @return the current virtual file path for the web-app descriptor
    */
   public String getWebXmlPath()
   {
      return getName();
   }
   /**
    * Set the virtual file path for the web-app descriptor in the
    * DeploymentContext.getMetaDataLocation. The standard path is web.xml
    * to be found in the WEB-INF metdata path.
    * 
    * @param webXmlPath - new virtual file path for the web-app descriptor
    */
   public void setWebXmlPath(String webXmlPath)
   {
      setName(webXmlPath);
   }

}
