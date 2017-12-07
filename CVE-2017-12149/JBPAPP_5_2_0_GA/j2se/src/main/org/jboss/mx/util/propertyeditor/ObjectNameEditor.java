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
package org.jboss.mx.util.propertyeditor;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.jboss.util.NestedRuntimeException;
import org.jboss.util.propertyeditor.TextPropertyEditorSupport;

/**
 * A property editor for {@link javax.management.ObjectName}.
 *
 * @version <tt>$Revision: 113110 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ObjectNameEditor
   extends TextPropertyEditorSupport
{
    //TODO: push to beans
   /**
    * Returns a ObjectName for the input object converted to a string.
    *
    * @return a ObjectName object
    *
    * @throws org.jboss.util.NestedRuntimeException   An MalformedObjectNameException occured.
    */
   public Object getValue()
   {
      try {
         return new ObjectName(getAsText());
      }
      catch (MalformedObjectNameException e) {
         throw new NestedRuntimeException(e);
      }
   }
}
