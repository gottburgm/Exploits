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

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;
import org.w3c.dom.Element;

/**
 * ServiceElementValueMetaData.
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
public class ServiceElementValueMetaData extends AbstractMetaDataVisitorNode
   implements ServiceValueMetaData, Serializable
{
   private static final long serialVersionUID = 1;

   /** The log */
   private static final Logger log = Logger.getLogger(ServiceElementValueMetaData.class); 
   
   /** The element */
   private Element element;

   /**
    * Create a new ServiceElementValueMetaData.
    */
   public ServiceElementValueMetaData()
   {
   }
   
   /**
    * Create a new ServiceElementValueMetaData.
    * 
    * @param element the element
    */
   public ServiceElementValueMetaData(Element element)
   {
      setElement(element);
   }

   /**
    * Get the element.
    * 
    * @return the element.
    */
   public Element getElement()
   {
      return element;
   }

   /**
    * Set the element.
    * 
    * @param element the element.
    */
   public void setElement(Element element)
   {
      if (element == null)
         throw new IllegalArgumentException("Null element");
      this.element = element;
   }

   public Object getValue(ServiceValueContext valueContext) throws Exception
   {         
      // Replace any ${x} references in the element text
      if (valueContext.isReplace())
      {
         PropertyEditor editor = PropertyEditorFinder.getInstance().find(Element.class);
         if (editor == null)
            log.warn("Cannot perform property replace on Element");
         else
         {
            editor.setValue(element);
            String text = editor.getAsText();
            text = StringPropertyReplacer.replaceProperties(text);
            editor.setAsText(text);
            return editor.getValue();
         }
      }

      return element;
   }
}
