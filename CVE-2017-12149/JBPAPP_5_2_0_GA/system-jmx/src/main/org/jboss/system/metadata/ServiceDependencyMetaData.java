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
package org.jboss.system.metadata;

import java.io.Serializable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.system.microcontainer.LifecycleDependencyItem;
import org.jboss.system.microcontainer.ServiceControllerContext;

/**
 * ServiceDependencyMetaData.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceDependencyMetaData extends AbstractMetaDataVisitorNode
   implements Serializable
{
   private static final long serialVersionUID = 1;

   /** The dependency */
   private String iDependOn;

   /** The dependency */
   private ObjectName iDependOnObjectName;

   /**
    * Get the iDependOn.
    * 
    * @return the iDependOn.
    */
   public String getIDependOn()
   {
      if (iDependOn == null)
         return iDependOnObjectName.getCanonicalName();
      return iDependOn;
   }

   /**
    * Set the iDependOn.
    * 
    * @param iDependOn the iDependOn.
    */
   public void setIDependOn(String iDependOn)
   {
      if (iDependOn == null)
         throw new IllegalArgumentException("Null iDependOn");
      this.iDependOn = iDependOn;
      this.iDependOnObjectName = null;
   }

   /**
    * Get the iDependOn.
    * 
    * @return the iDependOn.
    * @throws MalformedObjectNameException if the string was set with an invalid object name
    */
   public ObjectName getIDependOnObjectName() throws MalformedObjectNameException
   {
      if (iDependOnObjectName == null)
      {
         if (iDependOn.trim().length() == 0)
            throw new MalformedObjectNameException("Missing object name in depends");
         ObjectName objectName = new ObjectName(iDependOn);
         if (objectName.isPattern())
            throw new MalformedObjectNameException("ObjectName patterns are not allowed in depends: " + iDependOn);
         iDependOnObjectName = objectName;
         iDependOn = null;
      }
      return iDependOnObjectName;
   }

   /**
    * Set the iDependOn.
    * 
    * @param iDependOn the iDependOn.
    */
   public void setIDependOnObjectName(ObjectName iDependOn)
   {
      if (iDependOn == null)
         throw new IllegalArgumentException("Null iDependOn");
      this.iDependOnObjectName = iDependOn;
   }

   public void visit(ServiceMetaDataVisitor visitor)
   {
      ServiceControllerContext context = visitor.getControllerContext();
      Object name = context.getName();
      Object other = iDependOn;
      try
      {
         other = getIDependOnObjectName().getCanonicalName();
      }
      catch (MalformedObjectNameException ignored)
      {
      }
      visitor.addDependency(new LifecycleDependencyItem(name, other, ControllerState.CREATE));
      visitor.addDependency(new LifecycleDependencyItem(name, other, ControllerState.START));
      visitor.visit(this);
   }
}
