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
package org.jboss.jmx.adaptor.snmp.config.notification;

/**
 * Simple POJO class to model XML data
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * 
 * @version $Revision: 81038 $
 */
public class VarBind
{
   // Private Data --------------------------------------------------

   private String tag;
   private String oid;

   // Constructors -------------------------------------------------
   
   /**
    * Default CTOR
    */
   public VarBind()
   {
      // empty
   }

   // Accessors/Modifiers -------------------------------------------
  
   public String getOid()
   {
      return oid;
   } 

   public String getTag()
   {
      return tag;
   }

   public void setOid(String oid)
   {
      this.oid = oid;
   }
   
   public void setTag(String tag)
   {
      this.tag = tag;
   }

   // Object overrides ----------------------------------------------
   
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(256);
      
      sbuf.append('[')
      .append("tag=").append(tag)
      .append(", oid=").append(oid)
      .append(']');
      
      return sbuf.toString();      
   }      
}
