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

import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.system.microcontainer.ServiceControllerContext;

/**
 * ServiceMetaDataVisitor.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public interface ServiceMetaDataVisitor
{
   /**
    * Get the controller context
    * 
    * @return the context
    */
   ServiceControllerContext getControllerContext();

   /**
    * Get the context state
    * 
    * @return the context state
    */
   ControllerState getContextState();

   /**
    * Set the context state
    * 
    * @param contextState the context state
    */
   void setContextState(ControllerState contextState);

   /**
    * Add a dependency
    * 
    * @param dependency the dependency
    */
   void addDependency(DependencyItem dependency);
   
   /**
    * Visit the node
    * 
    * @param node the node
    */
   void visit(ServiceMetaDataVisitorNode node);
}
