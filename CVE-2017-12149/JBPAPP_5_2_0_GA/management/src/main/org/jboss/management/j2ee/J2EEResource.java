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
package org.jboss.management.j2ee;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Root class of the JBoss JSR-77 J2EEResources
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 81025 $
 */
public abstract class J2EEResource extends J2EEManagedObject
   implements J2EEResourceMBean
{
   /**
    * @param type       the j2eeType key value
    * @param name       Name of the J2EEResource
    * @param parentName the object name of the parent resource
    */
   public J2EEResource(String type, String name, ObjectName parentName)
           throws MalformedObjectNameException, InvalidParentException
   {
      super(type, name, parentName);
   }

   // Protected -----------------------------------------------------
   
   /**
    * Extract the name attribute from parent and return J2EEServer=name
    *
    * @param parentName the parent ObjectName
    * @return A hashtable with the J2EE Server name
    */
   protected Hashtable getParentKeys(ObjectName parentName)
   {
      Hashtable keys = new Hashtable();
      Hashtable properties = parentName.getKeyPropertyList();
      keys.put(J2EETypeConstants.J2EEServer, properties.get("name"));

      return keys;
   }
}
