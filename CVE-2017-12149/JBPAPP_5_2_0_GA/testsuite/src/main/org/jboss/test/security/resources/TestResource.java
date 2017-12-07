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
package org.jboss.test.security.resources;

import java.util.HashMap;
import java.util.Map;

import org.jboss.security.authorization.Resource;
import org.jboss.security.authorization.ResourceType;

/**
 * <p>
 * A implementation of {@code Resource} for testing purposes.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class TestResource implements Resource
{

   private final Map<String, Object> contextMap;

   private final int id;

   /**
    * <p>
    * Creates an instance of {@code TestResource} with the specified id.
    * </p>
    * 
    * @param id an {@code int} representing the unique id of the resource being built.
    */
   public TestResource(int id)
   {
      this.id = id;
      this.contextMap = new HashMap<String, Object>();
   }

   /**
    * <p>
    * Obtains the id of this resource.
    * </p>
    * 
    * @return an {@code int} representing the resource's id.
    */
   public int getId()
   {
      return this.id;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.authorization.Resource#getLayer()
    */
   public ResourceType getLayer()
   {
      return ResourceType.ACL;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.security.authorization.Resource#getMap()
    */
   public Map<String, Object> getMap()
   {
      return this.contextMap;
   }

}
