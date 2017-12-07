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

import java.io.StringReader;

import org.jboss.system.ServiceConfigurator;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;
import org.w3c.dom.Element;

/**
 * ServiceJBXBValueMetaData.
 * 
 * This class is based on the old ServiceConfigurator
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:hiram@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceJBXBValueMetaData extends ServiceElementValueMetaData
{
   private static final long serialVersionUID = 1;

   /**
    * Create a new ServiceJBXBValueMetaData.
    */
   public ServiceJBXBValueMetaData()
   {
      super();
   }

   /**
    * Create a new ServiceJBXBValueMetaData.
    * 
    * @param element the element
    */
   public ServiceJBXBValueMetaData(Element element)
   {
      super(element);
   }

   public Object getValue(ServiceValueContext valueContext) throws Exception
   {
      // Get the attribute element content in a parsable form
      StringBuffer buffer = ServiceConfigurator.getElementContent(getElement());

      Thread current = Thread.currentThread();
      ClassLoader oldTcl = current.getContextClassLoader();
      ClassLoader cl = valueContext.getClassloader();
      if (cl != null)
         current.setContextClassLoader(cl);
      try
      {
         // Parse the attribute element content
         SchemaBindingResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
         StringReader reader = new StringReader(buffer.toString());
         Object bean = unmarshaller.unmarshal(reader, resolver);
         return bean;
      }
      finally
      {
         if (cl != null)
            current.setContextClassLoader(oldTcl);
      }
   }
}
