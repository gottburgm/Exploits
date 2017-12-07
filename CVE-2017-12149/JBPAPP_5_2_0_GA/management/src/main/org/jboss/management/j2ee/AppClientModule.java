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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.security.InvalidParameterException;
import java.util.Hashtable;

/**
 * Root class of the JBoss JSR-77 implementation of AppClientModule.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 */
public class AppClientModule extends J2EEModule
   implements AppClientModuleMBean
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * Constructor taking the Name of this Object
    *
    * @param pName                 Name to be set which must not be null
    * @param pDeploymentDescriptor
    * @throws InvalidParameterException If the given Name is null
    */
   public AppClientModule(String pName, ObjectName pApplication, String[] pJVMs, String pDeploymentDescriptor)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.AppClientModule, pName, pApplication, pJVMs, pDeploymentDescriptor);
   }
   
   // Public --------------------------------------------------------
   
   // Object overrides ---------------------------------------------------
   
   public String toString()
   {
      return "AppClientModule { " + super.toString() +
              " } []";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
    * @return A hashtable with the J2EE-Application and J2EE-Server as parent
    */
   protected Hashtable getParentKeys(ObjectName pParent)
   {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put(J2EETypeConstants.J2EEApplication, lProperties.get("name"));
      // J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put(J2EETypeConstants.J2EEServer, lProperties.get(J2EETypeConstants.J2EEServer));

      return lReturn;
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}
