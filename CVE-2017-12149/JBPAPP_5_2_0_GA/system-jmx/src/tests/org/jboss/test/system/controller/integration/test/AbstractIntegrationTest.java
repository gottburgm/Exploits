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
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.system.controller.AbstractControllerTest;

/**
 * An integration Test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class AbstractIntegrationTest extends AbstractControllerTest
{
   /**
    * Create a new integration test.
    * 
    * @param name the test name
    */
   public AbstractIntegrationTest(String name)
   {
      super(name);
   }
   
   public static AbstractTestDelegate getDelegate(Class clazz) throws Exception
   {
      IntegrationTestDelegate delegate = new IntegrationTestDelegate(clazz);
      // @todo delegate.enableSecurity = true;
      return delegate;
   }

   /**
    * Get a bean
    * 
    * @param name the bean name
    * @return the bean
    * @throws IllegalStateException when the bean does not exist
    */
   protected Object getBean(Object name)
   {
      return getBean(name, ControllerState.INSTALLED);
   }
   
   /**
    * Get a bean
    * 
    * @param name the name of the bean
    * @param state the state of the bean
    * @return the bean
    * @throws IllegalStateException when the bean does not exist at that state
    */
   protected Object getBean(Object name, ControllerState state)
   {
      return getIntegrationDelegate().getBean(name, state);
   }

   /**
    * Get a context
    * 
    * @param name the bean name
    * @return the context
    * @throws IllegalStateException when the context does not exist 
    */
   protected ControllerContext getControllerContext(Object name)
   {
      return getControllerContext(name, ControllerState.INSTALLED);
   }
   
   /**
    * Get a context
    * 
    * @param name the name of the bean
    * @param state the state of the bean
    * @return the context
    * @throws IllegalStateException when the context does not exist at that state
    */
   protected ControllerContext getControllerContext(Object name, ControllerState state)
   {
      return getIntegrationDelegate().getControllerContext(name, state);
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
      return getIntegrationDelegate().deployMC(url);
   }
   
   /**
    * Deploy a resource
    *
    * @param resource the deployment resource
    * @return the deployment
    * @throws Exception for any error  
    */
   protected KernelDeployment deployMC(String resource) throws Exception
   {
      URL url = getClass().getResource(resource);
      if (url == null)
         throw new IllegalArgumentException("Resource not found: " + resource);
      return getIntegrationDelegate().deployMC(url);
   }
   
   /**
    * Undeploy a deployment
    *
    * @param deployment the deployment
    */
   protected void undeployMC(KernelDeployment deployment)
   {
      getIntegrationDelegate().undeployMC(deployment);
   }
   
   /**
    * Undeploy a deployment
    *
    * @param resource the url
    */
   protected void undeployMC(String resource)
   {
      URL url = getClass().getResource(resource);
      if (url == null)
         throw new IllegalArgumentException("Resource not found: " + resource);
      getIntegrationDelegate().undeployMC(url);
   }
   
   /**
    * Validate
    * 
    * @throws Exception for any error
    */
   protected void validateMC() throws Exception
   {
      getIntegrationDelegate().validateMC();
   }

   protected IntegrationTestDelegate getIntegrationDelegate()
   {
      return (IntegrationTestDelegate) getDelegate();
   }
}
