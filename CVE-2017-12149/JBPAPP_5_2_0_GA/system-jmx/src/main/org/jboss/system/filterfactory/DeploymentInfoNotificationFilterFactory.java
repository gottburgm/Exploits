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
package org.jboss.system.filterfactory;

import java.util.Set;
import java.util.Vector;

import javax.management.Notification;
import javax.management.NotificationFilter;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.mx.util.JBossNotificationFilterSupport;
import org.jboss.system.NotificationFilterFactory;
import org.jboss.util.collection.CollectionsFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Factory for {@link DeploymentInfoNotificationFilter} filters.
 * (check the inner class).
 * 
 * The produced filter is really meant for Notifications
 * emitted by SubDeployers. The types of interest are:
 * 
 * org.jboss.deployment.SubDeployer.init
 * org.jboss.deployment.SubDeployer.create
 * org.jboss.deployment.SubDeployer.start
 * org.jboss.deployment.SubDeployer.stop
 * org.jboss.deployment.SubDeployer.destroy
 * 
 * The above subdeployer notifications carry a DeploymentInfo
 * instance in their UserData. We can further filter based on
 * DeploymentInfo.shortName, but is important to explicitly
 * enable both the notification types and the desired shortNames.
 * 
 * In practice, you'll be able to receive notifications when a
 * particular deployment unit (e.g. my-app.ear) gets processed
 * (e.g. started, stopped, etc.) by a subdeployer.
 * 
 * The passed filterConfig xml element fragment should look like:
 * 
 * <filter factory="DeploymentInfoNotificationFilterFactory">
 *   <enable type="org.jboss.deployment.SubDeployer.start"/>
 *   <enable type="org.jboss.deployment.SubDeployer.stop"/>
 *   ...
 *   <enable short-name="my-app.ear"/>
 *   <enable short-name="my-service.xml"/>
 *   ...
 * </filter>
 * 
 * Note: org.jboss.deployment.SubDeployer yields all five
 * SubDeployer notifications.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
 * @since 4.0.3
**/
public class DeploymentInfoNotificationFilterFactory
   implements NotificationFilterFactory
{
   // Constants -----------------------------------------------------
   
   /** the xml element and attribute supported by this factory */
   public static final String ENABLE_ELEMENT = "enable";
   public static final String ENABLE_TYPE_ATTRIBUTE = "type";
   public static final String ENABLE_SHORTNAME_ATTRIBUTE = "short-name";   
   
   /**
    * Default public CTOR (necessary)
    */
   public DeploymentInfoNotificationFilterFactory()
   {
      // empty
   }
   
   /**
    * The actual filter factory implementation
    */
   public NotificationFilter createNotificationFilter(Element filterConfig)
      throws Exception
   {
      // start off with a filter that does not allow any type
      DeploymentInfoNotificationFilter filter = new DeploymentInfoNotificationFilter();
      
      // filterConfig should point to the <filter factory="..."> element,
      // we are interested in its 'enable' children to configure the filter 
      NodeList filterChildren = filterConfig.getChildNodes();
      
      for (int i = 0; i < filterChildren.getLength(); i++) 
      {
         Node filterChildNode = filterChildren.item(i);
      
         // check if this is an 'enable' element, ignore everything else
         if (filterChildNode.getNodeName().equals(ENABLE_ELEMENT)) 
         {
            // look for 'type' attribute
            if (((Element)filterChildNode).hasAttribute(ENABLE_TYPE_ATTRIBUTE)) 
            {
               String type = ((Element)filterChildNode).getAttribute(ENABLE_TYPE_ATTRIBUTE);
               // enable this type in the filter
               filter.enableType(type);
            }
            else if (((Element)filterChildNode).hasAttribute(ENABLE_SHORTNAME_ATTRIBUTE))
            {
               String shortName = ((Element)filterChildNode).getAttribute(ENABLE_SHORTNAME_ATTRIBUTE);
               // enable this shortName in the filter
               filter.enableShortName(shortName);
            }
            else
            {
               throw new Exception("'" + ENABLE_ELEMENT + "' element must have a '"
                     + ENABLE_TYPE_ATTRIBUTE + "' or a '" + ENABLE_SHORTNAME_ATTRIBUTE + "' attribute");
            }
         }
      }
      // we are done
      return filter;
   }
   
   /**
    * A NotificationFilter that can filter Notifications that
    * carry a DeploymentInfo payload in the UserData field.
    * 
    * The Notification is filtered first on its type,
    * then on its DeploymentInfo.shortName.
    * 
    * Uses copy-on-write semantics for fast unsynchronized access.
    */
   public static class DeploymentInfoNotificationFilter extends JBossNotificationFilterSupport
   {
      private static final long serialVersionUID = -5067618040005609685L;
      
      /** The short names that will pass the filter */
      private Set enabledShortNames;
      
      // Constructors -----------------------------------------------
      
      /**
       * Default CTOR.
       * 
       * Create a filter that filters out all notification types/sortnames.
       */
      public DeploymentInfoNotificationFilter()
      {
         super();
         enabledShortNames = CollectionsFactory.createCopyOnWriteSet();
      }
      
      /**
       * Disable all shortNames. Rejects all notifications.
       */
      public void disableAllShortNames()
      {
         enabledShortNames.clear();
      }      
      
      /**
       * Disable a shortName.
       *
       * @param name the shortName to disable.
       */
      public void disableShortName(String name)
      {
         enabledShortNames.remove(name);
      }
      
      /**
       * Enable a shortName.
       *
       * @param name the shortName to enable.
       * @exception IllegalArgumentException for a null name.
       */
      public void enableShortName(String name) throws IllegalArgumentException
      {
         if (name == null)
         {
            throw new IllegalArgumentException("null shortName");
         }
         enabledShortNames.add(name);
      }
      
      /**
       * Get all the enabled short names.<p>
       *
       * Returns a vector of enabled short names.<br>
       * An empty vector means all short names disabled.
       *
       * @return the vector of enabled short names.
       */
      public Vector getEnabledShortNames()
      {
         return new Vector(enabledShortNames);
      }
      
      /**
       * @return human readable string.
       */
      public String toString()
      {
         StringBuffer sb = new StringBuffer(100);
         
         sb.append(getClass().getName()).append(':');
         sb.append(" enabledTypes=").append(getEnabledTypes());
         sb.append(" enabledShortNames=").append(getEnabledShortNames());
         
         return sb.toString();
      }
      
      // NotificationFilter implementation ---------------------------

      /**
       * Test to see whether this notification is enabled
       *
       * @param notification the notification to filter
       * @return true when the notification should be sent, false otherwise
       * @exception IllegalArgumentException for null notification.
       */
      public boolean isNotificationEnabled(Notification notification)
      {
         // check if the notification type is not enabled in the super class
         if (super.isNotificationEnabled(notification) == false)
         {  
            return false;
         }
         
         // Check the shortName in the payload. We assume that proper
         // filtering on notification type, ensures that the
         // notification actually carries a DeploymentInfo.
         DeploymentInfo di = (DeploymentInfo)notification.getUserData();
         String shortName = di.shortName;

         // Return true if the shortName is enabled, false otherwise
         return enabledShortNames.contains(shortName);
      }
   }
}
