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
package org.jboss.test.system.deployers.test;

import java.util.Collections;
import java.util.Set;
import java.lang.reflect.Method;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.Test;
import org.jboss.dependency.plugins.AbstractController;
import org.jboss.deployers.client.plugins.deployment.AbstractDeployment;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.IncompleteDeploymentException;
import org.jboss.deployers.plugins.deployers.DeployersImpl;
import org.jboss.deployers.plugins.main.MainDeployerImpl;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractParsingDeployerWithOutput;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.StructuralDeployers;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.system.ServiceController;
import org.jboss.system.server.jmx.LazyMBeanServer;
import org.jboss.system.deployers.ServiceDeployer;
import org.jboss.system.deployers.ServiceDeploymentDeployer;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.test.AbstractSystemTest;
import org.jboss.test.system.deployers.support.CLDeployer;
import org.jboss.test.system.deployers.support.Tester;
import org.jboss.test.system.deployers.support.JmxCL;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Test component name usage.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ServiceUnitNameTestCase extends AbstractSystemTest
{
   public ServiceUnitNameTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
   {
      return suite(ServiceUnitNameTestCase.class);
   }

   public void testServiceDeployerComponentName() throws Exception
   {
      SecurityManager sm = suspendSecurity();
      try
      {
         MBeanServer mbeanServer = createMBeanServer("jboss");
         try
         {
            ServiceController serviceController = new ServiceController();
            ObjectName objectName = new ObjectName("jboss.system:service=ServiceController");
            mbeanServer.registerMBean(serviceController, objectName);
            try
            {
               ClassLoader loader = Thread.currentThread().getContextClassLoader();
               mbeanServer.registerMBean(new JmxCL(loader), new ObjectName("jboss:service=defaultClassLoader"));

               BasicBootstrap bootstrap = new BasicBootstrap();
               bootstrap.run();
               Kernel kernel = bootstrap.getKernel();
               AbstractController controller = (AbstractController)kernel.getController();

               serviceController.setMBeanServer(mbeanServer);
               serviceController.setKernel(kernel);

               MainDeployerImpl mainDeployer = new MainDeployerImpl();
               mainDeployer.setStructuralDeployers(new StructuralDeployers()
               {
                  public DeploymentContext determineStructure(Deployment deployment) throws DeploymentException
                  {
                     return new AbstractDeploymentContext("SMD", "");
                  }
               });
               DeployersImpl deployersImpl = new DeployersImpl(controller);
               deployersImpl.addDeployer(new ServiceDeployer(serviceController));
               deployersImpl.addDeployer(new ServiceDeploymentDeployer());
               deployersImpl.addDeployer(new CLDeployer());
               deployersImpl.addDeployer(new SMDParsingDeployer());
               mainDeployer.setDeployers(deployersImpl);

               Deployment deployment = new AbstractDeployment("SMD");
               mainDeployer.addDeployment(deployment);
               mainDeployer.process();

               mainDeployer.checkComplete(deployment);
               fail("Should not be here");
            }
            catch (Exception e)
            {
               assertInstanceOf(e, IncompleteDeploymentException.class);               
            }
            finally
            {
               mbeanServer.unregisterMBean(objectName);
            }
         }
         finally
         {
            MBeanServerFactory.releaseMBeanServer(mbeanServer);
         }
      }
      finally
      {
         resumeSecurity(sm);
      }
   }

   private class SMDParsingDeployer extends AbstractParsingDeployerWithOutput<ServiceDeployment>
   {
      public SMDParsingDeployer()
      {
         super(ServiceDeployment.class);
      }

      protected ServiceDeployment getServiceDeployment() throws Exception
      {
         ServiceMetaData metaData = new ServiceMetaData();
         metaData.setObjectName(new ObjectName("jboss.system:service=Tester"));
         metaData.setCode(Tester.class.getName());
         metaData.setConstructor(new ServiceConstructorMetaData());
         ServiceDependencyMetaData o = new ServiceDependencyMetaData();
         o.setIDependOn("somenonexistant");
         metaData.setDependencies(Collections.singletonList(o));

         ServiceDeployment serviceDeployment = new ServiceDeployment();
         serviceDeployment.setServices(Collections.singletonList(metaData));
         return serviceDeployment;
      }

      @Override
      protected ServiceDeployment parse(DeploymentUnit arg0, Set<String> arg1, ServiceDeployment arg2) throws Exception
      {
         return getServiceDeployment();
      }

      @Override
      protected ServiceDeployment parse(DeploymentUnit arg0, Set<String> arg1, String arg2, ServiceDeployment arg3) throws Exception
      {
         return getServiceDeployment();
      }

      protected ServiceDeployment parse(DeploymentUnit deploymentUnit, String s, ServiceDeployment deployment) throws Exception
      {
         return getServiceDeployment();
      }

      protected ServiceDeployment parse(DeploymentUnit deploymentUnit, String s, String s1, ServiceDeployment deployment) throws Exception
      {
         return getServiceDeployment();
      }
   }

   private MBeanServer createMBeanServer(String domain) throws Exception
   {
      MBeanServer server;

      String builder = System.getProperty(ServerConstants.MBEAN_SERVER_BUILDER_CLASS_PROPERTY, ServerConstants.DEFAULT_MBEAN_SERVER_BUILDER_CLASS);
      System.setProperty(ServerConstants.MBEAN_SERVER_BUILDER_CLASS_PROPERTY, builder);

      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class clazz = cl.loadClass("java.lang.management.ManagementFactory");
      Class[] sig = null;
      Method method = clazz.getMethod("getPlatformMBeanServer", sig);
      Object[] args = null;
      server = (MBeanServer)method.invoke(null, args);
      // Tell the MBeanServerLocator to point to this mbeanServer
      MBeanServerLocator.setJBoss(server);
      /* If the LazyMBeanServer was used, we need to reset to the jboss
      MBeanServer to use our implementation for the jboss services.
      */
      return LazyMBeanServer.resetToJBossServer(server);
   }
}
