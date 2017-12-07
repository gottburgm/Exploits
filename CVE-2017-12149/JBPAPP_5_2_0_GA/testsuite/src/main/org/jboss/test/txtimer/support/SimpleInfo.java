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
package org.jboss.test.txtimer.support;

import java.io.Serializable;

/**
 * Simple custom class to be associated and stored with a timer.
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class SimpleInfo implements Serializable
{
   private String name;
   
   public SimpleInfo(String name)
   {
      this.name = name != null ? name : "";
   }
   
   public String getName()
   {
      return name;
   }
   
   public int hashCode()
   {
      return name.hashCode();
   }
   
   public boolean equals(Object obj)
   {
      if (obj == this) return true;
      
      if (obj instanceof SimpleInfo)
      {
         SimpleInfo other = (SimpleInfo)obj;
         return name.equals(other.name);
      }
      return false;
   }
   
   public String toString()
   {
      return "SimpleInfo[name=" + name + "]";
   }
}