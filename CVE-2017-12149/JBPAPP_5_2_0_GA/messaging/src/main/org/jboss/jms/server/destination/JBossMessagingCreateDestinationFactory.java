/*
* JBoss, Home of Professional Open Source
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
package org.jboss.jms.server.destination;

import java.util.HashSet;
import java.util.Set;

import javax.jms.Queue;
import javax.jms.Topic;
import javax.management.ObjectName;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb.deployers.CreateDestinationFactory;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.spec.ActivationConfigMetaData;
import org.jboss.metadata.ejb.spec.ActivationConfigPropertiesMetaData;
import org.jboss.metadata.ejb.spec.ActivationConfigPropertyMetaData;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceTextValueMetaData;

/**
 * JBossMessagingCreateDestinationFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 105789 $
 */
public class JBossMessagingCreateDestinationFactory implements CreateDestinationFactory
{
   /** The log */
   private static final Logger log = Logger.getLogger(JBossMessagingCreateDestinationFactory.class);

   /** The server peer name */
   private String serverPeerName;
   
   /** The post office name */
   private String postOfficeName;
   
   /**
    * Get the serverPeerName.
    * 
    * @return the serverPeerName.
    */
   public String getServerPeerName()
   {
      return serverPeerName;
   }

   /**
    * Set the serverPeerName.
    * 
    * @param serverPeerName the serverPeerName.
    */
   public void setServerPeerName(String serverPeerName)
   {
      this.serverPeerName = serverPeerName;
   }

   /**
    * Get the postOfficeName.
    * 
    * @return the postOfficeName.
    */
   public String getPostOfficeName()
   {
      return postOfficeName;
   }

   /**
    * Set the postOfficeName.
    * 
    * @param postOfficeName the postOfficeName.
    */
   public void setPostOfficeName(String postOfficeName)
   {
      this.postOfficeName = postOfficeName;
   }

   public void create()
   {
      if (serverPeerName == null)
         throw new IllegalStateException("serverPeerName has not been set");
      if (postOfficeName == null)
         throw new IllegalStateException("postOfficeName has not been set");
   }
   
   public Object create(DeploymentUnit unit, JBossMessageDrivenBeanMetaData mdb) throws DeploymentException
   {
      String destinationJNDIName = null;
      String destinationType = null;
      
      ActivationConfigMetaData activationConfig = mdb.getActivationConfig();
      if (activationConfig != null)
      {
         ActivationConfigPropertiesMetaData properties = activationConfig.getActivationConfigProperties();
         if (properties != null)
         {
            destinationJNDIName = getActivationConfigProperty(properties, "destination");
            destinationType = getActivationConfigProperty(properties, "destinationType");
         }
      }

      // TODO message-destination-link?
      
      if (destinationJNDIName == null || destinationJNDIName.trim().length() == 0)
      {
         log.warn("Unable to determine destination for " + mdb.getName());
         return null;
      }
      boolean isTopic = false;
      if (destinationType == null)
      {
         log.warn("Unable to determine destination type for " + mdb.getName());
         return null;
      }
      else if (destinationType.equals(Queue.class.getName()))
      {
         // Its a queue
      }
      else if (destinationType.equals(Topic.class.getName()))
      {
         isTopic = true;
      }
      else
      {
         log.warn("Unknown destination type '" + destinationType + "' for " + mdb.getName());
         return null;
      }
      
      ServiceMetaData result = new ServiceMetaData();
      result.setConstructor(new ServiceConstructorMetaData());
      ObjectName objectName;
      // See https://jira.jboss.org/jira/browse/JBPAPP-3026
      String destinationName = this.createDestinationNameFromJNDIName(destinationJNDIName);
      if (isTopic)
      {
    	  
    	  
         objectName = ObjectNameFactory.create("jboss.mq.destination:service=Topic,name=" + destinationName);
         result.setCode("org.jboss.jms.server.destination.TopicService");
         result.setXMBeanDD("xmdesc/Topic-xmbean.xml");
      }
      else
      {
          objectName = ObjectNameFactory.create("jboss.mq.destination:service=Queue,name=" + destinationName);
         result.setCode("org.jboss.jms.server.destination.QueueService");
         result.setXMBeanDD("xmdesc/Queue-xmbean.xml");
      }
      result.setObjectName(objectName);
      
      ServiceAttributeMetaData attribute = new ServiceAttributeMetaData();
      attribute.setName("JNDIName");
      attribute.setValue(new ServiceTextValueMetaData(destinationJNDIName));
      result.addAttribute(attribute);

      attribute = new ServiceAttributeMetaData();
      attribute.setName("ServerPeer");
      attribute.setValue(new ServiceTextValueMetaData(serverPeerName));
      result.addAttribute(attribute);
      
      ServiceDependencyMetaData dependency = new ServiceDependencyMetaData();
      dependency.setIDependOn(serverPeerName);
      result.addDependency(dependency);

      dependency = new ServiceDependencyMetaData();
      dependency.setIDependOn(postOfficeName);
      result.addDependency(dependency);

      // Add this mbean to the mdbs depends
      Set<String> depends = mdb.getDepends();
      if (depends == null)
      {
         depends = new HashSet<String>();
         mdb.setDepends(depends);
      }
      depends.add(objectName.getCanonicalName());
      
      return result;
   }

   /**
    * Get an activation config property
    * 
    * @param properties the properties
    * @param name the name
    * @return the property or null if not found
    */
   protected static String getActivationConfigProperty(ActivationConfigPropertiesMetaData properties, String name)
   {
      ActivationConfigPropertyMetaData property = properties.get(name);
      if (property == null)
         return null;
      return property.getValue();
   }
   
   /**
    * JBoss Messaging does not allow the use of forward slashes in the destination name (which is used in the 
    * MBean ObjectName for the destination). However, the destination *jndi* name is allowed to have forward
    * slashes. So create a proper destination name out of the destination *jndi* name.
    * See https://jira.jboss.org/jira/browse/JBPAPP-3026 for more details
    * 
    * This method, just replaces all the forward slashes with a dot.
    * 
    * @param destinationJNDIName The jndi name of the destination
    * @return Returns the destination name, created out of the destination jndi name.
    *   Returns null if the <code>destinationJNDIName</code> is null.
    */
   private String createDestinationNameFromJNDIName(String destinationJNDIName)
   {
      if (destinationJNDIName == null)
      {
         return null;
      }
      return destinationJNDIName.replace('/', '.');
   }
}
