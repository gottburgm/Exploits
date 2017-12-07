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
package org.jboss.varia.property;

import java.beans.PropertyEditor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.system.ServiceMBeanSupport;


/**
 * A service to access <tt>org.jboss.common.beans.property.finder.PropertyEditorFinder</tt> service.
 *
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version <tt>$Revision: 113110 $</tt>
 */
public class PropertyEditorManagerService extends ServiceMBeanSupport
   implements PropertyEditorManagerServiceMBean
{
   /** 
    * List of registered editors (java.lang.Class).  
    * These will be removed in destroyService().
    * This is to prevent memory leakage of the class loader.
    */
   private final List registeredEditors = Collections.synchronizedList(new ArrayList());
   private final PropertyEditorFinder propertyEditorFinder = PropertyEditorFinder.getInstance();
   ///////////////////////////////////////////////////////////////////////////
   //                      PropertyEditorManager Access                     //
   ///////////////////////////////////////////////////////////////////////////

   /**
    * Locate a value editor for a given target type.
    *
    * @jmx:managed-operation
    *
    * @param type   The class of the object to be edited.
    * @return       An editor for the given type or null if none was found.
    */
   public PropertyEditor findEditor(final Class type)
   {
      return propertyEditorFinder.find(type);
   }

   /**
    * Locate a value editor for a given target type.
    *
    * @jmx:managed-operation
    *
    * @param typeName    The class name of the object to be edited.
    * @return            An editor for the given type or null if none was found.
    */
   public PropertyEditor findEditor(final String typeName)
      throws ClassNotFoundException
   {
      Class type = Class.forName(typeName);
      
      return propertyEditorFinder.find(type);
   }

   /**
    * Register an editor class to be used to editor values of a given target class.
    *
    * @jmx:managed-operation
    *
    * @param type         The class of the objetcs to be edited.
    * @param editorType   The class of the editor.
    */
   public void registerEditor(final Class type, final Class editorType)
   {
      registeredEditors.add(type);
      propertyEditorFinder.register(type, editorType);
   }

   /**
    * Register an editor class to be used to editor values of a given target class.
    *
    * @jmx:managed-operation
    *
    * @param typeName         The classname of the objetcs to be edited.
    * @param editorTypeName   The class of the editor.
    */
   public void registerEditor(final String typeName,
                              final String editorTypeName)
      throws ClassNotFoundException
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class type = cl.loadClass(typeName);
      Class editorType = cl.loadClass(editorTypeName);
      registerEditor(type, editorType);
   }

   /** Turn a string[] into an comma seperated list. */
   private String makeString(final String[] array)
   {
      StringBuffer buff = new StringBuffer();
      
      for (int i=0; i<array.length; i++) {
         buff.append(array[i]);
         if ((i + 1) < array.length) {
            buff.append(",");
         }
      }

      return buff.toString();
   }

   /** Turn an comma seperated list into a string[]. */
   private String[] makeArray(final String listspec)
   { 
      StringTokenizer stok = new StringTokenizer(listspec, ",");
      List list = new ArrayList();
      
      while (stok.hasMoreTokens()) {
         String url = stok.nextToken();
         list.add(url);
      }

      return (String[])list.toArray(new String[list.size()]);
   }

   /**
    * Gets the package names that will be searched for property editors.
    *
    * @jmx:managed-attribute
    *
    * @return   The package names that will be searched for property editors.
    */
   public String getEditorSearchPath()
   {
      return makeString(propertyEditorFinder.getEditorSearchPackages());
   }

   /**
    * Sets the package names that will be searched for property editors.
    *
    * @jmx:managed-attribute
    *
    * @param path   A comma sperated list of package names.
    */
   public void setEditorSearchPath(final String path)
   {
       propertyEditorFinder.setEditorSearchPackages(makeArray(path));
   }
   

   ///////////////////////////////////////////////////////////////////////////
   //                      JMX & Configuration Helpers                      //
   ///////////////////////////////////////////////////////////////////////////

   /**
    * Load property editors based on the given properties string.
    *
    * @jmx:managed-attribute
    *
    * @param props, A string representation of a editor.class=editor.type
    * Properties map for the editors to load.
    */
   public void setBootstrapEditors(final String propsString)
      throws ClassNotFoundException, IOException
   {
      Properties props = new Properties();
      ByteArrayInputStream stream = new ByteArrayInputStream(propsString.getBytes());
      props.load(stream);
      setEditors(props);
   }

   /**
    * Set property editors based on the given properties map.
    *
    * @jmx:managed-attribute
    *
    * @param props    Map of <em>type name</em> to </em>editor type name</em>.
    */
   public void setEditors(final Properties props) throws ClassNotFoundException
   {
      Iterator iter = props.keySet().iterator();
      while (iter.hasNext()) {
         String typeName = (String)iter.next();
         String editorTypeName = props.getProperty(typeName);

         registerEditor(typeName, editorTypeName);
      }
   }

   /**
    * Returns a list of registered editor classes.
    * @jmx:managed-attribute
    */
   public Class[] getRegisteredEditors()
   {
      return (Class [])registeredEditors.toArray(new Class[registeredEditors.size()]);
   }
   
   ///////////////////////////////////////////////////////////////////////////
   //                     ServiceMBeanSupport Overrides                     //
   ///////////////////////////////////////////////////////////////////////////

   protected ObjectName getObjectName(final MBeanServer server, final ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }
   
   /**
    * Removes the list of registered editors.
    */
   protected void destroyService() throws Exception
   {
      Iterator iter = registeredEditors.iterator();
      while (iter.hasNext())
      {
         Class type = (Class)iter.next();
         propertyEditorFinder.register(type, null);
         iter.remove();
      }
   }

}
