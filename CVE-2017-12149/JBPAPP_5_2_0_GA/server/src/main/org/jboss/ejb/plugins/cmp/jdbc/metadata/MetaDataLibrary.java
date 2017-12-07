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

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

import java.net.URL;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Set;
import java.util.Collections;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81030 $</tt>
 * @jmx:mbean name="jboss.jdbc:service=metadata"
 * extends="org.jboss.system.ServiceMBean"
 */
public class MetaDataLibrary
   extends ServiceMBeanSupport
   implements MetaDataLibraryMBean
{
   private final Hashtable typeMappings = new Hashtable();

   /**
    * @jmx.managed-operation
    */
   public JDBCTypeMappingMetaData findTypeMappingMetaData(String name)
   {
      return (JDBCTypeMappingMetaData)typeMappings.get(name);
   }

   /**
    * @jmx.managed-attribute
    */
   public Set getTypeMappingNames()
   {
      return Collections.unmodifiableSet(typeMappings.keySet());
   }

   public void startService() throws Exception
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      URL stdJDBCUrl = classLoader.getResource("standardjbosscmp-jdbc.xml");
      if(stdJDBCUrl == null)
      {
         throw new DeploymentException("No standardjbosscmp-jdbc.xml found");
      }

      boolean debug = log.isDebugEnabled();
      if(debug)
      {
         log.debug("Loading standardjbosscmp-jdbc.xml : " + stdJDBCUrl.toString());
      }
      Element stdJDBCElement = XmlFileLoader.getDocument(stdJDBCUrl, true).getDocumentElement();

      Element typeMaps = MetaData.getOptionalChild(stdJDBCElement, "type-mappings");
      if(typeMaps != null)
      {
         for(Iterator i = MetaData.getChildrenByTagName(typeMaps, "type-mapping"); i.hasNext();)
         {
            Element typeMappingElement = (Element)i.next();
            JDBCTypeMappingMetaData typeMapping = new JDBCTypeMappingMetaData(typeMappingElement);
            typeMappings.put(typeMapping.getName(), typeMapping);

            log.debug("added type-mapping: " + typeMapping.getName());
         }
      }
   }

   public void stopService() throws Exception
   {
      typeMappings.clear();
   }
}
