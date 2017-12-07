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
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List; 
import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * Imutable class which holds a list of the properties for a dependent value
 * class.
 *     
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *   @version $Revision: 81030 $
 */
public final class JDBCValueClassMetaData {
   private final Class javaType;
   private final List properties;

   /**
    * Constructs a value class metadata class with the data contained in 
    * the dependent-value-class xml element from a jbosscmp-jdbc xml file.
    *
    * @param classElement the xml Element which contains the metadata about
    *       this value class
    * @param classLoader the ClassLoader which is used to load this value class 
    * @throws DeploymentException if the xml element is not semantically correct
    */
   public JDBCValueClassMetaData(Element classElement, ClassLoader classLoader) throws DeploymentException {      
      String className = MetaData.getUniqueChildContent(classElement, "class");
      try {
         javaType = classLoader.loadClass(className);
      } catch (ClassNotFoundException e) {
         throw new DeploymentException("dependent-value-class not found: " + className);
      }
      
      List propertyList = new ArrayList();
      Iterator iterator = MetaData.getChildrenByTagName(classElement, "property");
      while(iterator.hasNext()) {
         Element propertyElement = (Element)iterator.next();
      
         propertyList.add(new JDBCValuePropertyMetaData(propertyElement, javaType));
      }
      properties = Collections.unmodifiableList(propertyList);
   }

   /**
    * Gets the Java Class of this value class.
    *
    * @return the java Class of this value class
    */
   public Class getJavaType() {
      return javaType;
   }

   /**
    * Gets the properties of this value class which are to be saved into the database.
    *
    * @return an unmodifiable list which contains the JDBCValuePropertyMetaData objects
    */
   public List getProperties() {
      return properties;
   }
}
