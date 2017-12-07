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
package org.jboss.test.server.profileservice.persistence.test;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.metatype.api.types.MetaTypeFactory;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.system.server.profileservice.persistence.DelegatingPersistencePlugin;
import org.jboss.system.server.profileservice.persistence.ManagedObjectPersistencePlugin;
import org.jboss.system.server.profileservice.persistence.ManagedObjectRecreationHelper;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.test.BaseTestCase;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88906 $
 */
public abstract class AbstractPersistenceFormatTest extends BaseTestCase
{

   /** The managed object factory */
   private static final ManagedObjectFactory managedObjectFactory = ManagedObjectFactory.getInstance();
   
   /** A helper. */
   private static final ManagedObjectRecreationHelper helper;
   
   /** The meta value factory */
   private MetaValueFactory metaValueFactory = MetaValueFactory.getInstance();
   
   /** The meta type factory */
   private MetaTypeFactory metaTypeFactory = MetaTypeFactory.getInstance();
   
   /** The persistence plugin. */
   private DelegatingPersistencePlugin plugin = new DelegatingPersistencePlugin(helper);
   
   static
   {
      helper = new ManagedObjectRecreationHelper(managedObjectFactory);
   }
   
   public AbstractPersistenceFormatTest(String name)
   {
      super(name);
   }

   protected ManagedObjectPersistencePlugin getPersistencePlugin()
   {
      return this.plugin;
   }
   
   protected ManagedObjectFactory getMOF()
   {
      return managedObjectFactory;
   }
   
   protected MetaValueFactory getMVF()
   {
      return this.metaValueFactory;
   }
   
   protected MetaTypeFactory getMTF()
   {
      return this.metaTypeFactory;
   }
   
   protected ManagedObject initManagedObject(Object o)
   {
      return getMOF().initManagedObject(o, null);
   }
   
   protected PersistedManagedObject restore(ManagedObject mo) throws Exception
   {
      PersistedManagedObject moElement = plugin.createPersistedManagedObject(mo);
      
      File file = File.createTempFile("test", null);
      serialize(moElement, file);
      return deserialize(file);
   }
   
   protected void serialize(PersistedManagedObject moElement, File file) throws Exception
   {
      JAXBContext ctx = JAXBContext.newInstance(PersistedManagedObject.class);
      Marshaller marshaller = ctx.createMarshaller();
      marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
      marshaller.marshal(moElement, file);
      marshaller.marshal(moElement, System.out);
   }
   
   protected PersistedManagedObject deserialize(File file) throws Exception
   {
      JAXBContext ctx = JAXBContext.newInstance(PersistedManagedObject.class);
      Unmarshaller un = ctx.createUnmarshaller();
      return (PersistedManagedObject) un.unmarshal(file);
   }
   
   protected ManagedObject update(Object attachment, PersistedManagedObject moElement)
   {
      ManagedObject mo = getMOF().initManagedObject(attachment, null);
      return update(mo, moElement);
   }
   
   protected ManagedObject update(ManagedObject mo, PersistedManagedObject persisted)
   {
      return getPersistencePlugin().updateManagedObject(persisted, mo);
   }
   
}

