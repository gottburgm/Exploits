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
package org.jboss.spring.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.TreeMap;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.logging.Logger;
import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.util.naming.Util;
import org.jboss.spring.factory.Nameable;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * Abstract bean factory loader.
 *
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
@Deprecated
public abstract class AbstractBeanFactoryLoader implements BeanFactoryLoader
{
   private final Logger log = Logger.getLogger(getClass());

   protected Map beanFactoryNamesMap = new TreeMap();

   private URL getDocUrl(DeploymentInfo di) throws DeploymentException
   {
      URL docURL = di.localUrl;
      if (!di.isXML)
      {
         URLClassLoader localCL = di.localCl;
         docURL = localCL.findResource("META-INF/jboss-spring.xml");
      }
      // Validate that the descriptor was found
      if (docURL == null)
      {
         throw new DeploymentException("Failed to find META-INF/jboss-spring.xml");
      }
      return docURL;
   }

   private String getDefaultName(DeploymentInfo di)
   {
      String shortName = di.shortName;
      int p = shortName.indexOf(".spring");
      if (p > 0)
      {
         return shortName.substring(0, p);
      }
      p = shortName.indexOf("-spring.xml");
      if (p > 0)
      {
         return shortName.substring(0, p);
      }
      return null;
   }

   public void create(DeploymentInfo di) throws DeploymentException
   {
      URL docURL = getDocUrl(di);
      String defaultName = getDefaultName(di);
      BeanFactory beanFactory = null;
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(di.ucl);
         beanFactory = createBeanFactory(defaultName, new UrlResource(docURL));
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(cl);
      }
      String name = ((Nameable) beanFactory).getName();
      bind(beanFactory, name);
      log.info("Bean factory [" + name + "] binded to local JNDI.");
      beanFactoryNamesMap.put(docURL.toString(), name);
   }

   protected abstract BeanFactory createBeanFactory(String defaultName, Resource resource);

   public void start(DeploymentInfo di) throws DeploymentException
   {
   }

   public void stop(DeploymentInfo di) throws DeploymentException
   {
      try
      {
         URL docURL = getDocUrl(di);
         String name = (String) beanFactoryNamesMap.remove(docURL.toString());
         BeanFactory beanFactory = lookup(name);
         doClose(beanFactory);
         unbind(name);
         log.info("Bean factory [" + name + "] unbinded from local JNDI.");
      }
      catch (Exception e)
      {
         throw new DeploymentException(e);
      }
   }

   protected abstract void doClose(BeanFactory beanFactory);

   public void destroy(DeploymentInfo di) throws DeploymentException
   {
   }

   // JNDI stuff

   public static void bind(BeanFactory beanFactory, String name) throws BeansException
   {
      InitialContext ctx = null;
      try
      {
         ctx = new InitialContext();
         NonSerializableFactory.rebind(ctx, name, beanFactory);
      }
      catch (NamingException e)
      {
         throw new FatalBeanException("Unable to bind BeanFactory into JNDI", e);
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

   public static void unbind(String name) throws BeansException
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
         throw new FatalBeanException("Unable to unbind BeanFactory from JNDI", e);
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

   public BeanFactory lookup(String name) throws Exception
   {
      BeanFactory beanFactory = (BeanFactory) Util.lookup(name, getExactBeanFactoryClass());
      log.debug("Found Spring bean factory [" + name + "]: " + beanFactory);
      return beanFactory;
   }

   protected abstract Class getExactBeanFactoryClass();

}
