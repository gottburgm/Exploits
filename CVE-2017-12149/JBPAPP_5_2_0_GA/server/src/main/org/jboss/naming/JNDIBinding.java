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
package org.jboss.naming;

import java.beans.PropertyEditor;

import org.jboss.common.beans.property.BeanUtils;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;

/**
 * A representation of a binding into JNDI.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 113110 $
 */
public class JNDIBinding
{
   /** The jndi name to bind under */
   private String name;
   /** The binding text */
   private String text;
   /** The optional binding type the text should be converted to */
   private String type;
   /** The optional explicit PropertyEditor implementation class */
   private String editor;
   /** The actual binding value */
   private Object value;
   /** A flag indicating if the text should be trimmed */
   private boolean trim;

   /**
    * The JNDI name to bind under
    * @return
    */ 
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * The text representation of the binding
    * @return
    */ 
   public String getText()
   {
      return text;
   }
   /**
    * Set the text representation of the binding. If the replace attribute
    * is true, the text will be searched for system property references of the
    * form ${x}.
    * 
    * @param text
    */ 
   public void setText(String text)
   {
      if( trim == true )
         text = text.trim();
      this.text = text;     
   }

   /**
    * The optional type the text representation should be converted to.
    * 
    * @return
    */ 
   public String getType()
   {
      return type;
   }
   /**
    * 
    * @param type - type the text representation should be converted to.
    */ 
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * The optional PropertyEditor implementation class name.
    * @return
    */ 
   public String getEditor()
   {
      return editor;
   }
   /**
    * 
    * @param editor - the optional PropertyEditor implementation class name.
    */ 
   public void setEditor(String editor)
   {
      this.editor = editor;
   }

   /**
    * Object the binding value. If there is a binding from an external xml
    * fragment it will be whatever that was. If there is a type it will be the
    * value as obtained by converting the text of the value element to an object
    * using the type PropertyEditor. If there is an explicit PropertyEditor
    * given by the editor attribute that will be used to convert the text into
    * an object.
    * 
    * @return the value binding
    * @throws Exception - on failure to load/use the PropertyEditor
    */ 
   public Object getValue() throws Exception
   {
      if( value == null && text != null )
      {
         // If there is a property editor set, transform text to value
         if( editor != null )
         {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class editorClass = loader.loadClass(editor);
            PropertyEditor pe = (PropertyEditor) editorClass.newInstance();
            pe.setAsText(text);
            value = pe.getValue();            
         }
         else if( type != null )
         {
            PropertyEditor pe = PropertyEditorFinder.getInstance().find(BeanUtils.findClass(type));
            pe.setAsText(text);
            value = pe.getValue();
         }
         else
         {
            value = text;
         }
      }
      return value;
   }
   /**
    * Set the raw value binding
    * @param value
    */ 
   public void setValue(Object value)
   {
      this.value = value;
   }

   /**
    * 
    * @return flag indicating if the text should be trimmed
    */ 
   public boolean isTrim()
   {
      return trim;
   }
   /**
    * 
    * @param trim - flag indicating if the text should be trimmed
    */ 
   public void setTrim(boolean trim)
   {
      this.trim = trim;
   }
}
