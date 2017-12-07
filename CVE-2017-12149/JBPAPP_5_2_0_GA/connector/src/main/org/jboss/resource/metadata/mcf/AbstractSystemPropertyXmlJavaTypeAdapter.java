/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.metadata.mcf;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;

/**
 * A AbstractSystemPropertyXmlJavaTypeAdapter.
 * 
 * @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision:  $
 */
public abstract class AbstractSystemPropertyXmlJavaTypeAdapter<T> extends XmlAdapter<String, T>
{
   private static Logger log = Logger.getLogger(AbstractSystemPropertyXmlJavaTypeAdapter.class);
   
   @Override
   public String marshal(T v)
   {
      return v.toString();
   }

   @Override
   public T unmarshal(String v)
   {
      if (v == null)
         return null;
      v = StringPropertyReplacer.replaceProperties(v);
      return convertType(v);
   }
   
   abstract T convertType(String v);
}
