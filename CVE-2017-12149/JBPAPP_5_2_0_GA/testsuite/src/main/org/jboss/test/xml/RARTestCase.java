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
package org.jboss.test.xml;

import org.jboss.resource.deployment.ResourceAdapterObjectModelFactory;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

import junit.framework.TestCase;

import java.net.URL;


/**
 * Test the unmarshalling of ra.xml files
 *
 * @author Thomas.Diesler@jboss.org
 */
public class RARTestCase
   extends TestCase
{
   private Unmarshaller unmarshaller;
   private String  systemId;

   public RARTestCase(String name)
   {
      super(name);
   }

   // Setup the Unmarshaller & resource stream
   protected void setUp() throws Exception
   {
      super.setUp();

      unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      String resourceName = "xml/" + getName() + ".xml";
      final URL resource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
      assertNotNull("Null resource", resource);
      systemId = resource.toString();
   }

   /**
    * Test if we can unmarshal a ra.xml without DTD
    */
   public void testRARwithoutDTD() throws Exception
   {
      ObjectModelFactory factory = new ResourceAdapterObjectModelFactory();
      Object metaData = unmarshaller.unmarshal(systemId, factory, null);
      assertNotNull("Null meta data", metaData);
   }

   /**
    * Test if we can unmarshal a ra.xml with DTD
    */
   public void testRARwithDTD() throws Exception
   {
      ObjectModelFactory factory = new ResourceAdapterObjectModelFactory();
      Object metaData = unmarshaller.unmarshal(systemId, factory, null);
      assertNotNull("Null meta data", metaData);
   }

   /**
    * Test if we can unmarshal a ra.xml with DTD
    */
   public void testRARwithSchema() throws Exception
   {
      ObjectModelFactory factory = new ResourceAdapterObjectModelFactory();
      Object metaData = unmarshaller.unmarshal(systemId, factory, null);
      assertNotNull("Null meta data", metaData);
   }
}
