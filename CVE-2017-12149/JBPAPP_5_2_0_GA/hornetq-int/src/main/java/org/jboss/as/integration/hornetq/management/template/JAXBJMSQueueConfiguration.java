/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.as.integration.hornetq.management.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hornetq.jms.server.config.JMSQueueConfiguration;

/**
 * A JAXBJMSQueueConfiguration.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"bindings", "selector"})
public class JAXBJMSQueueConfiguration implements JMSQueueConfiguration
{
   @XmlAttribute
   private String name;
   
   @XmlElement(name="entry")
   @XmlJavaTypeAdapter(BindingEntryAdapter.class)
   private String[] bindings;
   
   @XmlElement
   private String selector;
   
   public String[] getBindings()
   {
      return bindings;
   }

   public void setBindings(String[] bindings)
   {
      this.bindings = bindings;
   }
   
   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }

   public String getSelector()
   {
      return selector;
   }

   public void setSelector(String selector)
   {
      this.selector = selector;
   }

   public boolean isDurable()
   {
      // TODO Auto-generated method stub
      return false;
   }
}
