/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.client;

import java.util.Properties;

import org.jboss.beans.metadata.plugins.AbstractDependencyValueMetaData;
import org.jboss.beans.metadata.spi.MetaDataVisitor;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.reflect.spi.TypeInfo;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class JndiDependencyValueMetaData extends AbstractDependencyValueMetaData
{
   private static final long serialVersionUID = 1;

   /** The demand jndi name */
   private String jndiName;
   private Properties env;
   private String classLoaderName;
   private JndiDependencyItem depends;

   public JndiDependencyValueMetaData(String jndiName, Properties env, String classLoaderName)
   {
      this.jndiName = jndiName;
      this.env = env;
      this.classLoaderName = classLoaderName;
   }

   @Override
   public void initialVisit(MetaDataVisitor visitor)
   {
      depends = new JndiDependencyItem(jndiName, env, classLoaderName);
      visitor.addDependency(depends);
      visitor.initialVisit(this);
   }

   @Override
   public Object getValue(TypeInfo info, ClassLoader cl) throws Throwable
   {
      return depends.getIDependOn();
   }

}
