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
package org.jboss.management.j2ee.deployers;

import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.management.j2ee.JMSResource;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * JMS resource jsr77 deployer.
 * 
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class JMSResourceJSR77Deployer extends AbstractJSR77Deployer<ServiceMetaData>
{
   private Set<String> jmsCodes;

   public JMSResourceJSR77Deployer()
   {
      super(ServiceMetaData.class);
      setComponentsOnly(true);
   }

   /**
    * Is service metadata jms resource.
    *
    * @param metaData the service metadata
    * @return true if service metadata code matches one of jms codes
    */
   protected boolean isJMSServiceMetaData(ServiceMetaData metaData)
   {
      if (jmsCodes == null)
      {
         // fill in jms codes
         jmsCodes = new HashSet<String>();
         jmsCodes.add("org.jboss.jms.server.destination.QueueService");
         jmsCodes.add("org.jboss.jms.server.destination.TopicService");
      }

      String code = metaData.getCode();
      return jmsCodes.contains(code);
   }

   protected void deployJsr77(MBeanServer server, DeploymentUnit unit, ServiceMetaData metaData) throws Throwable
   {
      if (isJMSServiceMetaData(metaData))
      {
         ObjectName serviceName = metaData.getObjectName();
         JMSResource.create(server, "LocalJMS", serviceName);
      }
   }

   protected void undeployJsr77(MBeanServer server, DeploymentUnit unit, ServiceMetaData metaData)
   {
      if (isJMSServiceMetaData(metaData))
      {
         JMSResource.destroy(server, "LocalJMS");  
      }
   }

   /**
    * Set jms codes.
    *
    * @param jmsCodes the jms codes
    */
   public void setJmsCodes(Set<String> jmsCodes)
   {
      this.jmsCodes = jmsCodes;
   }
}