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
package org.jboss.jmx.adaptor.snmp.config.attribute;

/**
 * An attribute mapping, by default readonly.
 * 
 * @author <a href="mailto:hwr@pilhuhn.de">Heiko W. Rupp</a>
 * @version $Revision: 81038 $
 */
public class MappedAttribute
{
	String name;
	String oid;
	boolean isReadWrite = false;
	
	/** Attribute name */
	public String getName()
   {
		return name;
	}
   
	public void setName(String name)
   {
		this.name = name;
	}
	
	/** Attribute oid */
	public String getOid()
   {
		return oid;
	}
   
	public void setOid(String oid)
   {
		this.oid = oid;
	}

   /** Attribute mode (ro/rw) */
   public boolean isReadWrite()
   {
      return isReadWrite;
   }
   
   public void setReadWrite(boolean mode)
   {
      isReadWrite = mode;
   }
   
	public String toString()
   {
		StringBuffer buf = new StringBuffer();
		buf.append("[name=").append(name);
		buf.append(", oid=").append(oid);
		buf.append(", rw=").append(isReadWrite);
		buf.append("]");
		return buf.toString();
	}
}
