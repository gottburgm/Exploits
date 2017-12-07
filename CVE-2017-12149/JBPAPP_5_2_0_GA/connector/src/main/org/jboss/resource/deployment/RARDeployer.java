/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.management.ObjectName;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.ObjectModelFactorySimpleSubDeployerSupport;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.resource.metadata.JBossRAMetaData;
import org.jboss.resource.metadata.RARDeploymentMetaData;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

/**
 * A resource adapter deployer
 *
 * @author  <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 76129 $
 */
public class RARDeployer extends ObjectModelFactorySimpleSubDeployerSupport
   implements RARDeployerMBean
{
   
   /** The JBOSS_RA_XML */
   private static final String JBOSS_RA_XML = "META-INF/jboss-ra.xml";
   
   /** The work manager name */
   protected ObjectName workManagerName;
   
   /** The work manager */
   protected WorkManager workManager;

   /** The xa terminator */
   protected XATerminator xaTerminator;

   /** The xa terminator name */
   protected ObjectName xaTerminatorName;
   
   public RARDeployer()
   {
      setEnhancedSuffixes(new String[] { "250:.rar" });
   }
   
   @SuppressWarnings("deprecation")
   protected void parseMetaData(DeploymentInfo di, URL url) throws org.jboss.deployment.DeploymentException
   {     
      super.parseMetaData(di, url);      
      
      InputStream is = di.localCl.getResourceAsStream(JBOSS_RA_XML);
      RARDeploymentMetaData rdmd = new RARDeploymentMetaData();
      rdmd.setConnectorMetaData((ConnectorMetaData)di.metaData);
      di.metaData = rdmd;
      
      try
      {
         if(is != null)
         {
            
            Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
            ObjectModelFactory factory = getExtendedObjectModelFactory();
            JBossRAMetaData ramd = (JBossRAMetaData)unmarshaller.unmarshal(is, factory, (Object)null);
            rdmd.setRaXmlMetaData(ramd);
            
         }
      
      }
      
      catch (Throwable t)
      {
         
         org.jboss.deployment.DeploymentException.rethrowAsDeploymentException("Error parsing meta data " + url, t);

      }finally
      {
         try
         {
            if(is != null)
            {
               is.close();
            }
         }
         catch (IOException e)
         {
         }
      }
      
         
   }
   public String getExtension()
   {
      return ".rar";
   }

   public String getMetaDataURL()
   {
      return "META-INF/ra.xml";
   }
   
   public String getObjectName(DeploymentInfo di) throws org.jboss.deployment.DeploymentException
   {
      String name = di.shortName;
      di = di.parent;
      while (di != null)
      {
         name = di.shortName + "#" + name;
         di = di.parent;
      }
      return "jboss.jca:service=RARDeployment,name='" + name + "'";
   }

   public String getDeploymentClass()
   {
      return RARDeployment.class.getName();
   }

   public ObjectModelFactory getObjectModelFactory()
   {
      return new ResourceAdapterObjectModelFactory();
   }

   public ObjectName getWorkManagerName()
   {
      return workManagerName;
   }

   public void setWorkManagerName(ObjectName workManagerName)
   {
      this.workManagerName = workManagerName;
   }

   public ObjectName getXATerminatorName()
   {
      return xaTerminatorName;
   }

   public void setXATerminatorName(ObjectName xaTerminatorName)
   {
      this.xaTerminatorName = xaTerminatorName;
   }
   
   protected void startService() throws Exception
   {
      workManager = (WorkManager) server.getAttribute(workManagerName, "Instance");
      xaTerminator = (XATerminator) server.getAttribute(xaTerminatorName, "XATerminator");
      super.startService();
   }

   private ObjectModelFactory getExtendedObjectModelFactory()
   {
      return new JBossRAObjectModelFactory();
   }
}
