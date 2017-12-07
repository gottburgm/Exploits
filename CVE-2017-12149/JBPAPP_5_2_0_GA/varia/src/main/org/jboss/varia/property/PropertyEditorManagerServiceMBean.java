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
import java.util.Properties;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 * 
 * @version $Revision: 81038 $
 */
public interface PropertyEditorManagerServiceMBean extends ServiceMBean
{
   /** The default object name */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.varia:type=Service,name=PropertyEditorManager");

   // Attributes ----------------------------------------------------
   
   /**
    * Load property editors based on the given properties string.
    * @param props, A string representation of a editor.class=editor.type Properties map for the editors to load.
    */
   void setBootstrapEditors(String propsString) throws ClassNotFoundException, java.io.IOException;
   
   /**
    * Set property editors based on the given properties map.
    * @param props Map of <em>type name</em> to </em>editor type name</em>.
    */
   void setEditors(Properties props) throws ClassNotFoundException;
   
   /**
    * The package names that will be searched for property editors.
    */
   void setEditorSearchPath(String path);
   String getEditorSearchPath();

   /**
    * A list of registered editor classes.
    */
   Class[] getRegisteredEditors();

   // Operations ----------------------------------------------------
   
   /**
    * Locate a value editor for a given target type.
    * @param type The class of the object to be edited.
    * @return An editor for the given type or null if none was found.
    */
   PropertyEditor findEditor(Class type);

   /**
    * Locate a value editor for a given target type.
    * @param typeName The class name of the object to be edited.
    * @return An editor for the given type or null if none was found.
    */
   PropertyEditor findEditor(String typeName) throws ClassNotFoundException;

   /**
    * Register an editor class to be used to editor values of a given target class.
    * @param type The class of the objetcs to be edited.
    * @param editorType The class of the editor.
    */
   void registerEditor(Class type, Class editorType);

   /**
    * Register an editor class to be used to editor values of a given target class.
    * @param typeName The classname of the objetcs to be edited.
    * @param editorTypeName The class of the editor.
    */
   void registerEditor(String typeName, String editorTypeName) throws ClassNotFoundException;

}
