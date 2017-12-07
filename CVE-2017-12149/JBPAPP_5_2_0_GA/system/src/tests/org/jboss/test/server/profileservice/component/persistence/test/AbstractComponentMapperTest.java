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
package org.jboss.test.server.profileservice.component.persistence.test;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.system.server.profileservice.persistence.AbstractPersistenceFactory;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.component.ComponentMapper;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.jboss.test.BaseTestCase;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class AbstractComponentMapperTest extends BaseTestCase
{

   /** The managed object factory. */
   private ManagedObjectFactory managedObjectFactory = ManagedObjectFactory.getInstance();
   
   /** The persistence factory. */
   private AbstractPersistenceFactory persistenceFactory = new AbstractPersistenceFactory();
   
   public AbstractComponentMapperTest(String name)
   {
      super(name);
   }

   protected ManagedObjectFactory getMOF()
   {
      return managedObjectFactory;
   }
   
   protected PersistenceFactory getPersistenceFactory()
   {
      return persistenceFactory;
   }
   
   protected void addComponentMapper(ComponentMapper mapper)
   {
      persistenceFactory.addComponentMapper(mapper);
   }
   
   protected PersistenceRoot restore(PersistenceRoot root) throws Exception
   {
      File file = File.createTempFile("test", null);
      serialize(root, file);
      return deserialize(file);
   }
   
   protected void serialize(PersistenceRoot moElement, File file) throws Exception
   {
      JAXBContext ctx = JAXBContext.newInstance(PersistenceRoot.class);
      Marshaller marshaller = ctx.createMarshaller();
      marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
      marshaller.marshal(moElement, file);
      marshaller.marshal(moElement, System.out);
   }
   
   protected PersistenceRoot deserialize(File file) throws Exception
   {
      JAXBContext ctx = JAXBContext.newInstance(PersistenceRoot.class);
      Unmarshaller unmarshaller = ctx.createUnmarshaller();
      return (PersistenceRoot) unmarshaller.unmarshal(file);
   }

}

