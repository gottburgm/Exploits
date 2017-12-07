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

import java.beans.PropertyEditor;
import java.io.Serializable;

import javax.management.MBeanAttributeInfo;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.deployment.DeploymentException;
import org.jboss.util.Classes;
import org.jboss.util.propertyeditor.BlockingModeEditor;
import org.jboss.util.threadpool.BlockingMode;

/**
 * ServiceTextValueMetaData.
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
public class ServiceTextValueMetaData extends AbstractMetaDataVisitorNode
   implements ServiceValueMetaData, Serializable
{
    static
    {
        //Ugly hack. Here lies PropertyEditors.init(). Below code is progeny. It is required to properly instantiate
        //ThreadPools. The threadpools dont do that for themselves. ThreadPools are in common-core.
       try
       {
           PropertyEditorFinder.getInstance().register(BlockingMode.class, BlockingModeEditor.class);
       }
       catch (Exception ignored)
       {
       }
    }
   private static final long serialVersionUID = 1;
   
   /** The text */
   private String text;

   /**
    * Create a new ServiceTextValueMetaData.
    * 
    * @param text the text
    */
   public ServiceTextValueMetaData(String text)
   {
      setText(text);
   }

   /**
    * Get the text.
    * 
    * @return the text.
    */
   public String getText()
   {
      return text;
   }

   /**
    * Set the text.
    * 
    * @param text the text.
    */
   public void setText(String text)
   {
      if (text == null)
         throw new IllegalArgumentException("Null text");
      this.text = text;
   }

   public Object getValue(ServiceValueContext valueContext) throws Exception
   {
      MBeanAttributeInfo attributeInfo = valueContext.getAttributeInfo();
      ClassLoader cl = valueContext.getClassloader();

      String typeName = attributeInfo.getType();
      if (typeName == null)
         throw new DeploymentException("AttributeInfo for " + attributeInfo.getName() + " has no type");

      // see if it is a primitive type first
      Class typeClass = Classes.getPrimitiveTypeForName(typeName);
      if (typeClass == null)
      {
         // nope try look up
         try
         {
            typeClass = cl.loadClass(typeName);
         }
         catch (ClassNotFoundException e)
         {
            throw new DeploymentException("Class not found for attribute: " + attributeInfo.getName(), e);
         }
      }

      PropertyEditor editor = PropertyEditorFinder.getInstance().find(typeClass);
      if (editor == null)
         throw new DeploymentException("No property editor for attribute: " + attributeInfo.getName() + "; type=" + typeClass.getName());

      // JBAS-1709, temporarily switch the TCL so that property
      // editors have access to the actual deployment ClassLoader.
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(cl);
      try 
      {
         editor.setAsText(text);
         return editor.getValue();
      }
      finally 
      {
         Thread.currentThread().setContextClassLoader(tcl);
      }
   }
}
