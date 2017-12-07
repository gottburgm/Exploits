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
package org.jboss.spring.deployers;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployerWithInput;
import org.jboss.deployers.spi.deployer.helpers.DeploymentVisitor;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.spring.factory.Nameable;
import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.util.naming.Util;
import org.springframework.beans.factory.BeanFactory;

/**
 * Deploys SpringMetaData.
 * @see ApplicationContextDeployer
 * @see BeanFactoryDeployer
 *
 * @param <T> exact bean factory type
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractSpringMetaDataDeployer<T extends BeanFactory> extends AbstractRealDeployerWithInput<SpringMetaData>
{
   protected AbstractSpringMetaDataDeployer()
   {
      setDeploymentVisitor(createDeploymentVisitor());
   }

   /**
    * Create deployment visitor.
    *
    * @return the deployment visitor
    */
   protected abstract DeploymentVisitor<SpringMetaData> createDeploymentVisitor();

   protected abstract class SpringDeploymentVisitor implements DeploymentVisitor<SpringMetaData>
   {
      public Class<SpringMetaData> getVisitorType()
      {
         return SpringMetaData.class;
      }

      public void deploy(DeploymentUnit unit, SpringMetaData springMetaData) throws DeploymentException
      {
         ClassLoader classLoader = unit.getClassLoader();
         ClassLoader old = Thread.currentThread().getContextClassLoader();
         try
         {
            Thread.currentThread().setContextClassLoader(classLoader);
            T beanFactory = doCreate(springMetaData);
            String name = ((Nameable) beanFactory).getName();
            springMetaData.setName(name);
            bind(beanFactory, name);
            if (log.isTraceEnabled())
               log.trace("Bean factory [" + name + "] binded to local JNDI.");
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(old);
         }
      }

      /**
       * Do create BeanFactory instance.
       *
       * @param metaData the spring meta data
       * @return new bean factory instance
       */
      protected abstract T doCreate(SpringMetaData metaData);

      public void undeploy(DeploymentUnit unit, SpringMetaData springMetaData)
      {
         String name = springMetaData.getName();
         try
         {
            T beanFactory = lookup(name);
            if (beanFactory != null)
            {
               doClose(beanFactory);
               unbind(name);
               if (log.isTraceEnabled())
                  log.trace("Bean factory [" + name + "] unbinded from local JNDI.");
            }
         }
         catch (Exception e)
         {
            if (log.isTraceEnabled())
               log.trace("Exception finding BeanFactory instance named " + name, e);
         }
      }

      /**
       * Do close bean factory.
       *
       * @param beanFactory the bean factory to close
       */
      protected abstract void doClose(T beanFactory);
   }

   /**
    * Bind factory to non-serializable JNDI context.
    *
    * @param beanFactory the bean factory
    * @param name the jndi name
    * @throws DeploymentException for any error
    */
   protected void bind(T beanFactory, String name) throws DeploymentException
   {
      InitialContext ctx = null;
      try
      {
         ctx = new InitialContext();
         NonSerializableFactory.rebind(ctx, name, beanFactory);
      }
      catch (NamingException e)
      {
         throw new DeploymentException("Unable to bind BeanFactory into JNDI", e);
      }
      finally
      {
         if (ctx != null)
         {
            try
            {
               ctx.close();
            }
            catch (Throwable ignored)
            {
            }
         }
      }
   }

   /**
    * Unind factory from non-serializable JNDI context.
    *
    * @param name the jndi name
    */
   protected void unbind(String name)
   {
      InitialContext ctx = null;
      try
      {
         ctx = new InitialContext();
         ctx.unbind(name);
         NonSerializableFactory.unbind(name);
      }
      catch (NamingException e)
      {
         log.warn("Unable to unbind BeanFactory from JNDI", e);
      }
      finally
      {
         if (ctx != null)
         {
            try
            {
               ctx.close();
            }
            catch (Throwable ignored)
            {
            }
         }
      }
   }

   /**
    * Do a jndi lookup for bean factory.
    *
    * @param name the jndi name
    * @return bean factory instance
    * @throws Exception for any exception
    */
   protected T lookup(String name) throws Exception
   {
      Class<T> beanFactoryClass = getExactBeanFactoryClass();
      T beanFactory = beanFactoryClass.cast(Util.lookup(name, beanFactoryClass));
      if (log.isTraceEnabled())
         log.trace("Found Spring bean factory [" + name + "]: " + beanFactory);
      return beanFactory;
   }

   /**
    * Exact bean factory class.
    *
    * @return the bean factory class
    */
   protected abstract Class<T> getExactBeanFactoryClass();
}
