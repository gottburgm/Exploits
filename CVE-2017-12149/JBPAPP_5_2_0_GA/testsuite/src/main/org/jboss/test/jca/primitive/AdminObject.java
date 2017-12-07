/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jca.primitive;

import java.io.Serializable;

import javax.jms.JMSException;

public class AdminObject implements javax.jms.Queue, Serializable
{
   private static final long serialVersionUID = 1L;

   private int someIntProperty;

   private Integer someIntegerObjectProperty;

   private boolean someBooleanProperty;

   private Boolean someBooleanObjectProperty;

   public int getSomeIntProperty()
   {
      return someIntProperty;
   }

   public void setSomeIntProperty(int someIntProperty)
   {
      this.someIntProperty = someIntProperty;
   }

   public Integer getSomeIntegerObjectProperty()
   {
      return someIntegerObjectProperty;
   }

   public void setSomeIntegerObjectProperty(Integer someIntegerObjectProperty)
   {
      this.someIntegerObjectProperty = someIntegerObjectProperty;
   }

   public boolean isSomeBooleanProperty()
   {
      return someBooleanProperty;
   }

   public void setSomeBooleanProperty(boolean someBooleanProperty)
   {
      this.someBooleanProperty = someBooleanProperty;
   }

   public Boolean getSomeBooleanObjectProperty()
   {
      return someBooleanObjectProperty;
   }

   public void setSomeBooleanObjectProperty(Boolean someBooleanObjectProperty)
   {
      this.someBooleanObjectProperty = someBooleanObjectProperty;
   }

   public String getQueueName() throws JMSException
   {
      return null;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("#======= TEST AdminObject ========#").append("\n");
      sb.append("Integer Primitive: " + someIntProperty).append("\n");
      sb.append("Integer Object: " + someIntegerObjectProperty).append("\n");
      sb.append("Boolean Primitive: " + someBooleanProperty).append("\n");
      sb.append("Boolean Object: " + someBooleanObjectProperty).append("\n");
      return sb.toString();
   }
}
