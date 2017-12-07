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
package org.jboss.test.system.controller.integration.test;

import java.net.URL;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.AbstractBootstrap;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.xml.BasicXMLDeployer;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.system.ServiceController;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.test.system.controller.NewControllerTestDelegate;

/**
 * IntegrationTestDelegate.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class IntegrationTestDelegate extends NewControllerTestDelegate
{
   /** The kernel */
   protected Kernel kernel;

   /** The deployer */
   protected BasicXMLDeployer beanDeployer;

   public IntegrationTestDelegate(Class clazz)
   {
      super(clazz);
   }

   /**
    * Overriden to extend setUp to include creation of a BasicXMLDeployer
    * that is used to deploy and validate the mc.
    * @see #deployMC()
    * @see #validateMC()
    */
   public void setUp() throws Exception
   {
      super.setUp();
      
      try
      {
         // Create the deployer
         beanDeployer = new BasicXMLDeployer(kernel);
         
         // Deploy
         deployMC();
         
         validateMC();
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable e)
      {
         throw new RuntimeException(e);
      }
   }

   // TODO Temporary
   protected boolean isValidateAtSetUp()
   {
      return false;
   }
   
   public ServiceControllerMBean createServiceController() throws Exception
   {
      try
      {
         // Bootstrap the kernel
         AbstractBootstrap bootstrap = getBootstrap();
         bootstrap.run();
         kernel = bootstrap.getKernel();
         
         // Create the service controller
         ServiceController result = new ServiceController();
         result.setKernel(kernel);
         return result;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable e)
      {
         throw new RuntimeException(e);
      }
   }

   public void tearDown() throws Exception
   {
      undeployMC();
      super.tearDown();
   }
   
   /**
    * Get the kernel bootstrap
    * 
    * @return the bootstrap
    * @throws Exception for any error
    */
   protected AbstractBootstrap getBootstrap() throws Exception
   {
      return new BasicBootstrap();
   }
   
   /**
    * Get a bean
    *
    * @param name the name of the bean
    * @param state the state of the bean
    * @return the bean
    * @throws IllegalStateException when the bean does not exist at that state
    */
   protected Object getBean(final Object name, final ControllerState state)
   {
      ControllerContext context = getControllerContext(name, state);
      return context.getTarget();
   }
   
   /**
    * Get a context
    *
    * @param name the name of the bean
    * @param state the state of the bean
    * @return the context
    * @throws IllegalStateException when the context does not exist at that state
    */
   protected ControllerContext getControllerContext(final Object name, final ControllerState state)
   {
      KernelController controller = kernel.getController();
      ControllerContext context = controller.getContext(name, state);
      if (context == null)
         throw new IllegalStateException("Context not found: " + name);
      return context;
   }

   /**
    * Validate
    * 
    * @throws Exception for any error
    */
   protected void validateMC() throws Exception
   {
      try
      {
         beanDeployer.validate();
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
   }

   /**
    * Deploy a url
    *
    * @param url the deployment url
    * @return the deployment
    * @throws Exception for any error  
    */
   protected KernelDeployment deployMC(URL url) throws Exception
   {
      try
      {
         log.debug("Deploying " + url);
         KernelDeployment deployment = beanDeployer.deploy(url);
         log.trace("Deployed " + url);
         return deployment;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t);
      }
   }
   
   /**
    * Undeploy a deployment
    * 
    * @param url the url
    */
   protected void undeployMC(URL url)
   {
      log.debug("Undeploying " + url);
      try
      {
         beanDeployer.undeploy(url);
         log.trace("Undeployed " + url);
      }
      catch (Throwable t)
      {
         log.warn("Error during undeployment: " + url, t);
      }
   }
   
   /**
    * Undeploy a deployment
    * 
    * @param deployment the deployment
    */
   protected void undeployMC(KernelDeployment deployment)
   {
      log.debug("Undeploying " + deployment.getName());
      try
      {
         beanDeployer.undeploy(deployment);
         log.trace("Undeployed " + deployment.getName());
      }
      catch (Throwable t)
      {
         log.warn("Error during undeployment: " + deployment.getName(), t);
      }
   }

   /**
    * Deploy the beans by looking for an xml descriptor named
    * clazz.getName().replace('.', '/') + "-mc.xml";
    * 
    * @throws Exception for any error
    */
   protected void deployMC() throws Exception
   {
      String testName = clazz.getName();
      testName = testName.replace('.', '/') + "-mc.xml";
      URL url = clazz.getClassLoader().getResource(testName);
      if (url != null)
         deployMC(url);
      else
         log.debug("No test specific deployment " + testName);
   }

   /**
    * Undeploy all
    */
   protected void undeployMC()
   {
      log.debug("Undeploying " + beanDeployer.getDeploymentNames());
      beanDeployer.shutdown();
   }

}
