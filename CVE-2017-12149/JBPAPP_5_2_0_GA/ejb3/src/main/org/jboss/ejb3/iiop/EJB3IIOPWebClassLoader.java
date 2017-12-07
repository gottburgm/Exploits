/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.iiop;

import javax.management.ObjectName;

import org.jboss.classloading.spi.RealClassLoader;
import org.jboss.iiop.WebCL;

/**
 * The getKey method of the WebCL class is not working in EJB3, this class fixes that.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author adrian@jboss.org
 * @version $Revision: 97163 $
 */
public class EJB3IIOPWebClassLoader extends WebCL
{
   private String jndiName;
   
   public EJB3IIOPWebClassLoader(ObjectName container, RealClassLoader parent, String jndiName)
   {
      super(container, parent);
      this.jndiName = jndiName;
   }

   @Override
   public String getKey()
   {
      String className = getClass().getName();
      int dot = className.lastIndexOf('.');
      if( dot >= 0 )
          className = className.substring(dot+1);
      String key =  className + '[' + jndiName + ']';
      return key;
   }
}
