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
package org.jboss.system.metadata;

import java.util.Properties;

import javax.management.MBeanAttributeInfo;

import org.jboss.common.beans.property.BeanUtils;
import org.jboss.deployment.DeploymentException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ServiceJavaBeanValueMetaData.
 * 
 * This class is based on the old ServiceConfigurator
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:hiram@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 113110 $
 */
@SuppressWarnings("deprecation")
public class ServiceJavaBeanValueMetaData extends ServiceElementValueMetaData
{
   private static final long serialVersionUID = 1;

   /**
    * Create a new ServiceJavaBeanValueMetaData.
    */
   public ServiceJavaBeanValueMetaData()
   {
      super();
   }

   /**
    * Create a new ServiceJavaBeanValueMetaData.
    * 
    * @param element the element
    */
   public ServiceJavaBeanValueMetaData(Element element)
   {
      super(element);
   }

   public Object getValue(ServiceValueContext valueContext) throws Exception
   {
      MBeanAttributeInfo attributeInfo = valueContext.getAttributeInfo();
      ClassLoader cl = valueContext.getClassloader();
      boolean trim = valueContext.isTrim();
      boolean replace = valueContext.isReplace();
      
      // Extract the property elements
      Element element = getElement();
      String attributeClassName = element.getAttribute("attributeClass");
      if( attributeClassName == null || attributeClassName.length() == 0 )
         attributeClassName = attributeInfo.getType();
      if (attributeClassName == null)
         throw new DeploymentException("AttributeInfo for " + attributeInfo.getName() + " has no type");
      Class attributeClass = cl.loadClass(attributeClassName);
      // Create the bean instance
      Object bean = attributeClass.newInstance();
      // Get the JavaBean properties
      NodeList properties = element.getElementsByTagName("property");
      Properties beanProps = new Properties();
      for(int n = 0; n < properties.getLength(); n ++)
      {
         // Skip over non-element nodes
         Node node = properties.item(n);
         if (node.getNodeType() != Node.ELEMENT_NODE)
         {
            continue;
         }
         Element property = (Element) node;
         String name = property.getAttribute("name");
         String value = ServiceMetaDataParser.getElementTextContent(property, trim, replace);
         beanProps.setProperty(name, value);
      }

      // Apply the properties to the bean
      BeanUtils.mapJavaBeanProperties(bean, beanProps);
      return bean;
   }
}
