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
package org.jboss.test.jbossmx.compliance.modelmbean.support;

/**
 * A resource that implements an MBean interface at the same time.
 * 
 * Used to test that the fix for JBAS-1704 doesn't cause a problem
 * when a target resource *with* an mbean interface, too, registers
 * through a model mbean and exposes methods/attribute that are not
 * declared on the mbean interface.
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class Resource2 implements Resource2MBean
{
   private int mbeanAttribute = 666;
   private int pojoAttribute  = 777;
   
   // Resource2MBean implementation - Won't expose those through the model mbean
   
   public int getmbeanAttribute()
   {
      return mbeanAttribute;
   }
   
   public void setmbeanAttribute(int mbeanAttribute)
   {
      this.mbeanAttribute = mbeanAttribute;
   }
   
  public boolean mbeanOperation()
  {
     return true;
  }
  
  // Methods & Attributes to be exposed through the model mbean
  
   public int getpojoAttribute()
   {
      return pojoAttribute;
   }
   
   public void setpojoAttribute(int pojoAttribute)
   {
      this.pojoAttribute = pojoAttribute;
   }
   
   public boolean pojoOperation()
   {
      return true;
   }
  
}