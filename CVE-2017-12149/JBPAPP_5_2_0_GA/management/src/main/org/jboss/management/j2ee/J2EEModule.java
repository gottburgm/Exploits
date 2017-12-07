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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Root class of the JBoss JSR-77 implementation of J2EEModule.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public abstract class J2EEModule extends J2EEDeployedObject
   implements J2EEModuleMBean
{
   // Attributes ----------------------------------------------------

   // list of object names as strings
   private List mJVMs = new ArrayList();

   // Constructors --------------------------------------------------

   /**
    * Constructor taking the Name of this Object
    *
    * @param pName                 Name to be set which must not be null
    * @param pParent               Object Name of its parent which can either
    *                              be a J2EEApplication or J2EEServer if a
    *                              standalone module (not packed into an EAR file)
    * @param pDeploymentDescriptor
    * @throws InvalidParameterException If the given Name is null
    */
   public J2EEModule(String pType,
                     String pName,
                     ObjectName pParent,
                     String[] pJVMs,
                     String pDeploymentDescriptor)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(pType, pName, pParent, pDeploymentDescriptor);
      mJVMs = new ArrayList(Arrays.asList(pJVMs == null ? new String[0] : pJVMs));
   }

   // Public --------------------------------------------------------

   /**
    * @jmx:managed-attribute
    */
   public String[] getjavaVMs()
   {
      return (String[]) mJVMs.toArray(new String[mJVMs.size()]);
   }

   /**
    * @jmx:managed-operation
    */
   public String getjavaVM(int pIndex)
   {
      if (pIndex >= 0 && pIndex < mJVMs.size())
      {
         return (String) mJVMs.get(pIndex);
      }
      else
      {
         return null;
      }
   }
}
