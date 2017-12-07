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
package org.jboss.profileservice.management.templates;

import java.io.ObjectStreamException;
import java.util.Map;

import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.managed.plugins.BasicDeploymentTemplateInfo;
import org.jboss.managed.plugins.DefaultFieldsImpl;
import org.jboss.managed.plugins.ManagedPropertyImpl;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.SimpleValueSupport;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version <tt>$Revision: 90815 $</tt>
 */
public class JmsDestinationTemplateInfo extends BasicDeploymentTemplateInfo
{
   private static final long serialVersionUID = 1;
   private String destinationType = "QueueTemplate";
   
   protected static String checkNullName(String name)
   {
      if(name == null)
         throw new IllegalArgumentException("Null name.");
      return name;
   }
   
   public JmsDestinationTemplateInfo(String name, String description, Map<String, ManagedProperty> arg2)
   {
      super(checkNullName(name), description, arg2);      
      super.setRootManagedPropertyName("services");
   }

   public void setDestinationType(String destinationType)
   {
      this.destinationType = destinationType;
   }
   
   public String getDestinationType()
   {
      return destinationType;
   }

   public void start()
   {
      populate();
   }

   @Override
   public JmsDestinationTemplateInfo copy()
   {
      JmsDestinationTemplateInfo copy = new JmsDestinationTemplateInfo(getName(), getDescription(), getProperties());
      copy.setDestinationType(getDestinationType());
      super.copy(copy);
      copy.populate();
      return copy;
   }

   private void populate()
   {
      // Add the destination type as a ManagedProperty 
      DefaultFieldsImpl fields = new DefaultFieldsImpl("destinationType");
      fields.setDescription("The destination type");
      fields.setMetaType(SimpleMetaType.STRING);
      fields.setValue(SimpleValueSupport.wrap(destinationType));
      fields.setField(Fields.READ_ONLY, Boolean.TRUE);
      ManagedPropertyImpl destTypeMP = new ManagedPropertyImpl(fields);
      addProperty(destTypeMP);

      // FIXME
      if(getProperties() == null) return;
      for(ManagedProperty property : getProperties().values())
      {
         // Create a new (non-writethrough) managed property
         Fields f = property.getFields();
         
         ManagedPropertyImpl newProperty = new ManagedPropertyImpl(f);
         
         // Skip non configuration properties except clustered
         if(newProperty.hasViewUse(ViewUse.CONFIGURATION) == false
               && property.getName().equals("clustered") == false)
            continue;
         
         // Override
         addProperty(newProperty);
      }
   }

   /**
    * Expose only plain BasicDeploymentTemplateInfo to avoid leaking server types.
    *
    * @return simpler ManagedPropertyImpl
    * @throws java.io.ObjectStreamException for any error
    */
   private Object writeReplace() throws ObjectStreamException
   {
      BasicDeploymentTemplateInfo info = new BasicDeploymentTemplateInfo(getName(), getDescription(), getProperties());
      return info;
   }
}
