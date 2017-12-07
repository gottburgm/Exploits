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
package org.jboss.test.jmx.xmbean;

import java.beans.PropertyEditorSupport;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class CustomTypeEditor extends PropertyEditorSupport
{
   /** Convert "x.y" text to a CustomType(x, y)
    *
    * @param text
    * @throws IllegalArgumentException
    */
   public void setAsText(String text) throws IllegalArgumentException
   {
      int dot = text.indexOf('.');
      if( dot < 0 )
         throw new IllegalArgumentException("CustomType text must be 'x.y'");
      int x = Integer.parseInt(text.substring(0, dot));
      int y = Integer.parseInt(text.substring(dot+1));
      setValue(new CustomType(x, y));
   }

   public String getAsText()
   {
      CustomType type = (CustomType) getValue();
      return type.getX() + "." + type.getY();
   }
}
