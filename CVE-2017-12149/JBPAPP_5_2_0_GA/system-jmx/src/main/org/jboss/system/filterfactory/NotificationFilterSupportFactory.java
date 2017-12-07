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

import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;

import org.jboss.system.NotificationFilterFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Factory for NotificationFilterSupport filters.
 * 
 * The produced filter filters-in zero or more Notification types,
 * so you need to explicitly enable the types you want.
 *
 * The passed filterConfig xml element fragment should look like:
 * 
 * <filter factory="NotificationFilterSupportFactory">
 *   <enable type="some.notification.type"/>
 *   <enable type="another.notification.type"/>
 *   ...
 * </filter>
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
**/
public class NotificationFilterSupportFactory
   implements NotificationFilterFactory
{
   // Constants -----------------------------------------------------
   
   /** the xml element and attribute supported by this factory */
   public static final String ENABLE_ELEMENT = "enable";
   public static final String ENABLE_TYPE_ATTRIBUTE = "type";
   
   /**
    * Default public CTOR (necessary)
    */
   public NotificationFilterSupportFactory()
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
      NotificationFilterSupport filter = new NotificationFilterSupport();
      
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
            else
            {
               throw new Exception("'" + ENABLE_ELEMENT + "' element must have a '"
                     + ENABLE_TYPE_ATTRIBUTE + "' attribute");
            }
         }
      }
      // we are done
      return filter;
   }
}
