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
package org.jboss.kernel.deployment.jboss;

import java.net.URL;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SchemaResolverSimpleSubDeployerSupport;
import org.jboss.kernel.spi.deployment.KernelDeployment;

/**
 * A bean deployer for use with the JBoss JMX Deployers framework 
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81038 $
 */
public class JBossBeanDeployer extends SchemaResolverSimpleSubDeployerSupport implements JBossBeanDeployerMBean
{
   protected String extension = ".beans";
   
   protected String metaDataURL = "META-INF/jboss-beans.xml";
   
   public JBossBeanDeployer()
   {
      setEnhancedSuffixes(new String[] { "200:.beans" });
   }
   
   public String getExtension()
   {
      return extension;
   }

   public void setExtension(String extension)
   {
      this.extension = extension;
   }
   
   public String getMetaDataURL()
   {
      return metaDataURL;
   }
   
   public void setMetaDataURL(String metaDataURL)
   {
      this.metaDataURL = metaDataURL;
   }

   public String getObjectName(DeploymentInfo di) throws DeploymentException
   {
      String name = di.shortName;
      di = di.parent;
      while (di != null)
      {
         name = di.shortName + "#" + name;
         di = di.parent;
      }
      return "jboss.beans:service=JBossBeanDeployment,name='" + name + "'";
   }

   public String getDeploymentClass()
   {
      return JBossBeanDeployment.class.getName();
   }

   protected void parseMetaData(DeploymentInfo di, URL url) throws DeploymentException
   {
      super.parseMetaData(di, url);
      KernelDeployment deployment = (KernelDeployment) di.metaData;
      deployment.setName(url.toString());
   }
}
