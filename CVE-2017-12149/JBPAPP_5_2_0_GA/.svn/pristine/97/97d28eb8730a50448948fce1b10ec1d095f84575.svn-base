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
package org.jboss.test.profileservice.persistenceformat.test;

import java.util.ArrayList;
import java.util.List;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.beans.metadata.spi.PropertyMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
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
import org.jboss.managed.plugins.factory.AbstractManagedObjectFactory;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;
import org.jboss.test.profileservice.persistenceformat.support.SimpleAnnotatedBean;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class JBossBeansPersistenceFormatTestCase extends AbstractPersistenceFormatTest
{
   
   /** The bootstrap. */
   private BasicBootstrap bootstrap;
   
   /** The kernel. */
   private Kernel kernel;
   
   /** The controller. */
   private KernelController controller;
   
   /** Some random bean names. */
   private final static String[] BEAN_NAMES = new String[] {"TestBean", "SimpleAnnotatedBean", "OtherBean"};

   public JBossBeansPersistenceFormatTestCase(String name)
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
      addComponentMapper(new TestMapper(getPersistenceFactory()));  
   }
   
   public void testUpdateComponent() throws Throwable
   {
      // Install
      KernelDeployment deployment = createKernelDeployment(BEAN_NAMES);
      // install
      installDeployment(deployment);
      
      // Create the managed objects
      ManagedObject deploymentMO = getMOF().initManagedObject(deployment, null);
      ManagedObject mo = getBeanMO("SimpleAnnotatedBean");
      assertNotNull("null mo", mo);
      assertTrue(mo.getAttachment() instanceof BeanMetaData);
      ManagedComponent component = createComponent(mo);
      assertNotNull(component);
      assertEquals(component.getAttachmentName(), SimpleAnnotatedBean.class.getName());

      // Change value
      component.getProperty("stringProperty").setValue(SimpleValueSupport.wrap("changedTestValue"));
      
      PersistenceRoot root = updateComponent(deploymentMO, component);
      assertNotNull(root);

      // Uninstall for offline attachment persistence
      uninstallDeployment(deployment);
      
      // Recreate the kernel deployment
      deployment = createKernelDeployment(BEAN_NAMES);
      getPersistenceFactory().restorePersistenceRoot(root, deployment, null);
      
      // check bean meta data
      BeanMetaData bmd = getBeanMetaData(deployment, "SimpleAnnotatedBean");
      boolean sawProperty = false;
      for(PropertyMetaData prop : bmd.getProperties())
      {
         if(prop.getName().equals("stringProperty"))
         {
            assertEquals("changedTestValue", prop.getValue().getUnderlyingValue());
            sawProperty = true;
         }
      }
      assertTrue(sawProperty);
      
      // install
      installDeployment(deployment);
      mo = getBeanMO("SimpleAnnotatedBean");
      assertNotNull(mo);
      
      assertEquals(SimpleValueSupport.wrap("changedTestValue"),
            mo.getProperty("stringProperty").getValue());

   }
   
   protected ManagedObject getBeanMO(String name)
   {
      KernelControllerContext ctx = (KernelControllerContext) controller.getInstalledContext(name);
      assertNotNull(ctx);
      BeanMetaData bmd = ctx.getBeanMetaData();
      assertNotNull("null BeanMetaData", bmd);
      MetaData metaData = kernel.getMetaDataRepository().getMetaData(ctx);
      assertNotNull("null MetaData", metaData);
      return getMOF().initManagedObject(bmd, null, metaData, name, null);
   }
   
   protected void installDeployment(KernelDeployment deployment) throws Throwable
   {
      for(BeanMetaDataFactory factory : deployment.getBeanFactories())
      {
         for(BeanMetaData bmd : factory.getBeans())
            controller.install(bmd);
      }
   }
   
   protected void uninstallDeployment(KernelDeployment deployment)
   {
      for(BeanMetaDataFactory factory : deployment.getBeanFactories())
      {
         for(BeanMetaData bmd : factory.getBeans())
            controller.uninstall(bmd.getName());
      }      
   }
   
   protected BeanMetaData getBeanMetaData(KernelDeployment deployment, String name)
   {
      BeanMetaData bmd = null;
      for(BeanMetaDataFactory factory : deployment.getBeanFactories())
      {
         for(BeanMetaData beanMetaData : factory.getBeans())
         {
            if(beanMetaData.getName().equals(name))
            {
               bmd = beanMetaData;
               break;
            }
         }
      }
      assertNotNull("null beanMetaData for " + name, bmd);
      return bmd;
   }
   
   protected KernelDeployment createKernelDeployment(String... beans)
   {
      AbstractKernelDeployment deployment = new AbstractKernelDeployment();
      List<BeanMetaDataFactory> beanFactories = new ArrayList<BeanMetaDataFactory>();
      for(String beanName : beans)
         beanFactories.add(createBeanDataFactory(beanName));
      deployment.setBeanFactories(beanFactories);
      return deployment;
   }
   
   protected BeanMetaDataFactory createBeanDataFactory(String name)
   {
      BeanMetaDataBuilder b = BeanMetaDataBuilder.createBuilder(name, SimpleAnnotatedBean.class.getName());
      b.addAnnotation("@org.jboss.managed.api.annotation.ManagementObject(name=\""+ name +"\")");
      b.addPropertyMetaData("stringProperty", "test" + name);
      return b.asBeanMetaDataFactory();
   }
 
   private static final class TestMapper extends KernelDeploymentComponentMapper
   {
      public TestMapper(PersistenceFactory persistenceFactory)
      {
         super(persistenceFactory);
      }
      
      protected PersistedComponent createComponent(Object attachment, ManagedComponent component)
      {
         // Note: this is using the TestMgtComponentImpl to get the MO
         ManagedObject mo = (ManagedObject) component.getParent();
         PersistedManagedObject persisted = getPersistencePlugin().createPersistedManagedObject(mo);
         PersistedComponent persistedComponent = new PersistedComponent(persisted);
         setComponentName(persistedComponent, mo);
         return persistedComponent;
      }
   }
   
}

