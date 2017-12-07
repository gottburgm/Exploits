/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.deployment.dependency;

import java.util.Iterator;

import org.jboss.beans.metadata.spi.DependencyMetaData;
import org.jboss.beans.metadata.spi.MetaDataVisitor;
import org.jboss.beans.metadata.spi.MetaDataVisitorNode;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;
import org.jboss.util.JBossObject;
import org.jboss.util.JBossStringBuilder;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class JndiDependencyMetaData extends JBossObject
   implements DependencyMetaData
{
   private static final long serialVersionUID = 1L;
   
   private static final Logger log = Logger.getLogger(JndiDependencyMetaData.class);

   /** The demand jndi name */
   private String jndiName;
   /** The jndi name lookup value */
   private Object demand;
   /** The container class loader to use during lookup */
   private ClassLoader loader;
   private ControllerState whenRequired = ControllerState.INSTALLED;
   
   /**
    * Create a demand for a jndi name lookup using the given class loader.
    * 
    * @param jndiName - the name to lookup
    * @param loader - the ClassLoader to use as the TCL during lookup.
    */
   public JndiDependencyMetaData(String jndiName, ClassLoader loader)
   {
      this(jndiName, loader, ControllerState.INSTALLED);
   }
   public JndiDependencyMetaData(String jndiName, ClassLoader loader,
         ControllerState whenRequired)
   {
      this.jndiName = jndiName;
      this.loader = loader;
      this.whenRequired = whenRequired;
   }

   public Object getDependency()
   {
      return demand;
   }

   public ControllerState getWhenRequired()
   {
      return whenRequired;
   }

   public void describeVisit(MetaDataVisitor vistor)
   {
      vistor.describeVisit(this);
   }

   public Iterator<? extends MetaDataVisitorNode> getChildren()
   {
      return null;
   }

   public void initialVisit(MetaDataVisitor visitor)
   {
      KernelControllerContext context = visitor.getControllerContext();
      DependencyItem item = new JndiDependencyItem(jndiName, loader, whenRequired);
      visitor.addDependency(item);
      visitor.initialVisit(this);
   }

   @Override
   protected void toString(JBossStringBuilder buffer)
   {
      buffer.append(jndiName);
   }
   @Override
   public String toShortString()
   {
      JBossStringBuilder buffer = new JBossStringBuilder();
      toShortString(buffer);
      return buffer.toString();
   }
   @Override
   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(jndiName);
   }
}
