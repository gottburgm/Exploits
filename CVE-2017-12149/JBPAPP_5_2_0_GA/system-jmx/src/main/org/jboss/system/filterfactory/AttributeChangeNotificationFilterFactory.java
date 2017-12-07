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

import javax.management.AttributeChangeNotificationFilter;
import javax.management.NotificationFilter;

import org.jboss.system.NotificationFilterFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Factory for AttributeChangeNotificationFilter filters.
 * 
 * The produced filter filters-in AttributeChangeNotifications 
 * for zero or more attributes, so you need to explicitly enable
 * the attribute names you are interested in.
 *
 * The passed filterConfig xml element fragment should look like:
 * 
 * <filter factory="AttributeChangeNotificationFilterFactory">
 *   <enable attribute-name="State"/>
 *   ...
 * </filter>
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
**/
public class AttributeChangeNotificationFilterFactory
   implements NotificationFilterFactory
{
   // Constants -----------------------------------------------------
   
   /** the xml element and attribute supported by this factory */
   public static final String ENABLE_ELEMENT = "enable";
   public static final String ENABLE_ATTRNAME_ATTRIBUTE = "attribute-name";
   
   /**
    * Default public CTOR (necessary)
    */
   public AttributeChangeNotificationFilterFactory()
   {
      // empty
   }
   
   /**
    * The actual filter factory implementation
    */
   public NotificationFilter createNotificationFilter(Element filterConfig)
      throws Exception
   {
      // start off with a filter that does not allow any named attribute
      AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
      
      // filterConfig should point to the <filter factory="..."> element,
      // we are interested in its 'enable' children to configure the filter 
      NodeList filterChildren = filterConfig.getChildNodes();
      
      for (int i = 0; i < filterChildren.getLength(); i++) 
      {
         Node filterChildNode = filterChildren.item(i);
      
         // check if this is an 'enable' element, ignore everything else
         if (filterChildNode.getNodeName().equals(ENABLE_ELEMENT)) 
         {
            // look for 'attribute-name' attribute
            if (((Element)filterChildNode).hasAttribute(ENABLE_ATTRNAME_ATTRIBUTE)) 
            {
               String attributeName = ((Element)filterChildNode).getAttribute(ENABLE_ATTRNAME_ATTRIBUTE);
               // enable this type in the filter
               filter.enableAttribute(attributeName);
            }
            else
            {
               throw new Exception("'" + ENABLE_ELEMENT + "' element must have a '"
                     + ENABLE_ATTRNAME_ATTRIBUTE + "' attribute");
            }
         }
      }
      // we are done
      return filter;
   }
}
