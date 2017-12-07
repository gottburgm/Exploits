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
import java.util.Set;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployment.DeploymentException;
import org.jboss.util.UnreachableStatementException;

/**
 * ServiceAttributeMetaData.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceAttributeMetaData extends AbstractMetaDataVisitorNode
   implements Serializable
{
   private static final long serialVersionUID = 1;

   /** The attribute name */
   private String name;
   
   /** Whether to trim the value */
   private boolean trim;
   
   /** Whether to do property replacement */
   private boolean replace;
   
   /** The value */
   private ServiceValueMetaData value;

   /**
    * Get the name.
    * 
    * @return the name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set the name.
    * 
    * @param name the name.
    */
   public void setName(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      this.name = name;
   }

   /**
    * Get the replace.
    * 
    * @return the replace.
    */
   public boolean isReplace()
   {
      return replace;
   }

   /**
    * Set the replace.
    * 
    * @param replace the replace.
    */
   public void setReplace(boolean replace)
   {
      this.replace = replace;
   }

   /**
    * Get the trim.
    * 
    * @return the trim.
    */
   public boolean isTrim()
   {
      return trim;
   }

   /**
    * Set the trim.
    * 
    * @param trim the trim.
    */
   public void setTrim(boolean trim)
   {
      this.trim = trim;
   }

   /**
    * Get the value.
    * 
    * @return the value.
    */
   public ServiceValueMetaData getValue()
   {
      return value;
   }

   /**
    * Set the value.
    * 
    * @param value the value.
    */
   public void setValue(ServiceValueMetaData value)
   {
      if (value == null)
         throw new IllegalArgumentException("Null value");
      this.value = value;
   }
   
   /**
    * Get the value
    * 
    * @param valueContext the value context
    * @return the value
    * @throws Exception for any error
    */
   public Object getValue(ServiceValueContext valueContext) throws Exception
   {
      valueContext.setTrim(isTrim());
      valueContext.setReplace(isReplace());
      try
      {
         return value.getValue(valueContext);
      }
      catch (Throwable t)
      {
         DeploymentException.rethrowAsDeploymentException("Error configuring attribute " + name, t);
         throw new UnreachableStatementException();
      }
   }
   
   public void visit(ServiceMetaDataVisitor visitor)
   {
      visitor.setContextState(ControllerState.CONFIGURED);
      visitor.visit(this);
   }

   protected void addChildren(Set<ServiceMetaDataVisitorNode> children)
   {
      // Only add the value if its not null
      if( value != null )
         children.add(value);
   }
}
