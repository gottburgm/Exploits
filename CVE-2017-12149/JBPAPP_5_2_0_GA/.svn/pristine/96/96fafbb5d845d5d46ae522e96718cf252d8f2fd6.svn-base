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
package org.jboss.test.profileservice.persistenceformat.test;

import java.util.List;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.plugins.managed.BeanMetaDataICF;
import org.jboss.deployers.plugins.managed.KernelDeploymentComponentMapper;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.plugins.ManagedComponentImpl;
import org.jboss.managed.plugins.factory.AbstractManagedObjectFactory;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.services.binding.ServiceBindingManager;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ServiceBindingMgrPersistenceFormatTestCase extends AbstractPersistenceFormatTest
{
   
   /** The bootstrap. */
   private BasicBootstrap bootstrap;
   
   /** The kernel. */
   private Kernel kernel;
   
   /** The controller. */
   private KernelController controller;
   
   /** Unmarshaller factory */
   private static final UnmarshallerFactory factory = UnmarshallerFactory.newInstance();

   /** The resolver */
   private static final SchemaBindingResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
   
   public ServiceBindingMgrPersistenceFormatTestCase(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      // Bootstrap
      bootstrap = new BasicBootstrap();
      bootstrap.run();
      //
      kernel = bootstrap.getKernel();
      //
      controller = kernel.getController();
      
      // setUp beanICF
      BeanMetaDataICF beanICF = new BeanMetaDataICF();
      beanICF.setController(controller);
      beanICF.setDelegateICF(((AbstractManagedObjectFactory) 
            getMOF()).getDefaultInstanceFactory());
      
      getMOF().addInstanceClassFactory(beanICF);
      addComponentMapper(new KernelDeploymentComponentMapper(getPersistenceFactory()));
   }
   
   public void test() throws Throwable
   {
      // Set the jboss.bind.address
      System.setProperty("jboss.bind.address", "127.0.0.1");
      // Parse
      KernelDeployment deployment = parse(Thread.currentThread().getContextClassLoader().getResource("profileservice/persistence/testbindings-jboss-beans.xml").toString());
      // Deploy
      deploy(deployment);
      // Get the BeanMetaData
      KernelControllerContext ctx = (KernelControllerContext) controller.getContext("ServiceBindingManagementObject", null);
      assertNotNull(ctx);
      BeanMetaData bmd = ctx.getBeanMetaData();
      assertNotNull(bmd);

      // Create the ManagedObjects
      ManagedObject deploymentMO = getMOF().initManagedObject(deployment, null);
      ManagedObject mo = getMOF().initManagedObject(bmd, null);
      assertNotNull(mo);
      // Change the value
      CollectionValue bindingSets = (CollectionValue) mo.getProperty("standardBindings").getValue(); 
      assertNotNull(bindingSets);
      setPortValue("HttpsConnector", bindingSets, 13245);

      // Persist
      ManagedComponent component = new ManagedComponentImpl(null, null, mo);
      PersistenceRoot root = getPersistenceFactory().updateComponent(deploymentMO, component);
      root.setClassName(AbstractKernelDeployment.class.getName());
      root = restore(root);
      
      // Undeploy
      undeploy(deployment);
      // Restore
      getPersistenceFactory().restorePersistenceRoot(root, deployment, null);
      
      deploy(deployment);

      // Check if the values were changed.
      ServiceBindingManager service = (ServiceBindingManager) kernel.getRegistry().getEntry("ServiceBindingManager").getTarget();
      assertNotNull(service);
      assertEquals(13245, service.getIntBinding("jboss.web:service=WebServer", "HttpsConnector"));
   }
   
   protected void setPortValue(String name, CollectionValue values, int port)
   {
      boolean found = false;
      for(MetaValue v : values.getElements())
      {
         MapCompositeValueSupport c = (MapCompositeValueSupport) v;
         SimpleValue bindingName = (SimpleValue)c.get("bindingName");
         if(bindingName != null && bindingName.getValue().equals(name))
         {
            found = true;
            c.put("port", SimpleValueSupport.wrap(port));
         }
      }
      assertTrue("found "+ name, found);
   }
   
   protected void deploy(KernelDeployment deployment) throws Throwable
   {
      List<BeanMetaData> beans = deployment.getBeans();
      for(BeanMetaData bmd : beans)
         controller.install(bmd);
   }
   
   protected void undeploy(KernelDeployment deployment)
   {
      List<BeanMetaData> beans = deployment.getBeans();
      for(BeanMetaData bmd : beans)
         controller.uninstall(bmd.getName());
   }
   
   
   protected KernelDeployment parse(String name) throws Throwable
   {
      Unmarshaller unmarshaller = factory.newUnmarshaller();
      KernelDeployment deployment = (KernelDeployment) unmarshaller.unmarshal(name, resolver);
      deployment.setName(name);
      return deployment;
   }
   
}

