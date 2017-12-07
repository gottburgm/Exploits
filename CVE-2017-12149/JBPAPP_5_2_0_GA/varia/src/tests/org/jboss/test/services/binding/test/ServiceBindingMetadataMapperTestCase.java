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

package org.jboss.test.services.binding.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.jboss.metatype.api.values.MetaValue;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceConfig;
import org.jboss.services.binding.impl.StringReplacementServiceBindingValueSourceImpl;
import org.jboss.services.binding.managed.ServiceBindingMetadataMapper;

/**
 * Unit test of {@link ServiceBindingMetadataMapper}.
 * 
 * @author Brian Stansberry
 *
 */
public class ServiceBindingMetadataMapperTestCase extends TestCase
{

   /**
    * Create a new ServiceBindingMetadataMapperTestCase.
    * 
    * @param name
    */
   public ServiceBindingMetadataMapperTestCase(String name)
   {
      super(name);
   }
   
   public void testRoundTrip() throws Exception
   {
      Set<ServiceBindingMetadata> input = new HashSet<ServiceBindingMetadata>();
      ServiceBindingMetadata complete = new ServiceBindingMetadata("complete", "binding", "host", 10, true, true);
      complete.setDescription("desc");
      complete.setServiceBindingValueSource(new StringReplacementServiceBindingValueSourceImpl());
      complete.setServiceBindingValueSourceConfig(new StringReplacementServiceBindingValueSourceConfig());
      input.add(complete);
      
      ServiceBindingMetadata nulls = new ServiceBindingMetadata("nulls", null, null, 20);
      input.add(nulls);
      
      ServiceBindingMetadataMapper mapper = new ServiceBindingMetadataMapper();
      MetaValue wrapped = mapper.createMetaValue(null, input);
      Set<ServiceBindingMetadata> output = mapper.unwrapMetaValue(wrapped);
      
      assertEquals(input, output);
      for (ServiceBindingMetadata md : output)
      {
         if ("complete".equals(md.getServiceName()))
         {
            assertEquals(complete.getFullyQualifiedName(), md.getFullyQualifiedName());
            assertEquals(complete.getBindingName(), md.getBindingName());
            assertEquals(complete.getDescription(), md.getDescription());
            assertEquals(complete.getHostName(), md.getHostName());
            assertEquals(complete.getPort(), md.getPort());
            assertEquals(complete.isFixedHostName(), md.isFixedHostName());
            assertEquals(complete.isFixedPort(), md.isFixedPort());
            // We expect null for the following, but if the impl changes these can change
            assertNull(md.getServiceBindingValueSourceClassName());
            assertNull(md.getServiceBindingValueSourceConfig());
         }
         else if ("nulls".equals(md.getServiceName()))
         {
            assertEquals(nulls.getFullyQualifiedName(), md.getFullyQualifiedName());
            assertNull(md.getBindingName());
            assertNull(md.getDescription());
            assertNull(md.getHostName());
            assertEquals(nulls.getPort(), md.getPort());
            assertFalse(md.isFixedHostName());
            assertFalse(md.isFixedPort());
            assertNull(md.getServiceBindingValueSourceClassName());
            assertNull(md.getServiceBindingValueSourceConfig());
            
         }
         else
         {
            fail("Unexpected member " + md.getFullyQualifiedName());
         }
      }
   }
}
