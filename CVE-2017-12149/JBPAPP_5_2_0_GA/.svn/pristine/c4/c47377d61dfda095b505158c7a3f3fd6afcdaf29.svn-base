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
package org.jboss.test.spring.cluster;

import java.io.Serializable;

import org.jboss.cache.pojo.annotation.Replicable;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@Replicable
public class AnnotatedPojo implements Serializable
{
   private static final long serialVersionUID = 6101030433115572240L;

   private String host;
   private int port;
   private Object value;

   public String getHost()
   {
      return host;
   }

   public void setHost(String host)
   {
      this.host = host;
   }

   public int getPort()
   {
      return port;
   }

   public void setPort(int port)
   {
      this.port = port;
   }

   public Object getValue()
   {
      return value;
   }

   public void setValue(Object value)
   {
      this.value = value;
   }
}