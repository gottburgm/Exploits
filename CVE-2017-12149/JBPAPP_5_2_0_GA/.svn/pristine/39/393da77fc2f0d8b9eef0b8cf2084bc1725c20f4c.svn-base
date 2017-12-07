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
package org.jboss.spring.deployment;

import java.io.File;
import java.net.URL;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployer;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.spring.loader.BeanFactoryLoader;

/**
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 */
@Deprecated
public abstract class SpringDeployer extends SubDeployerSupport implements
      SubDeployer, SpringDeployerMBean
{

   protected abstract BeanFactoryLoader createBeanFactoryLoader();

   private BeanFactoryLoader beanFactoryLoader = createBeanFactoryLoader();

   /**
    * Default CTOR used to set default values to the Suffixes and RelativeOrder
    * attributes. Those are read at subdeployer registration time by the MainDeployer
    * to alter its SuffixOrder.
    */
   public SpringDeployer()
   {
      initializeMainDeployer();
   }

   protected void initializeMainDeployer()
   {
      setSuffixes(new String[]{".spring", "-spring.xml"});
      setRelativeOrder(350); //after -ds, before ejb3
   }

   /**
    * Returns true if this deployer can deploy the given DeploymentInfo.
    *
    * @return True if this deployer can deploy the given DeploymentInfo.
    * @jmx:managed-operation
    */
   public boolean accepts(DeploymentInfo di)
   {
      String urlStr = di.url.toString();
      return urlStr.endsWith(".spring") || urlStr.endsWith(".spring/") ||
            urlStr.endsWith("-spring.xml");
   }

   /**
    * Describe <code>init</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    * @jmx:managed-operation
    */
   public void init(DeploymentInfo di) throws DeploymentException
   {
      try
      {
         if (di.watch == null)
         {
            // resolve the watch
            if (di.url.getProtocol().equals("file"))
            {
               File file = new File(di.url.getFile());
               // If not directory we watch the package
               if (!file.isDirectory())
               {
                  di.watch = di.url;
               }
               // If directory we watch the xml files
               else
               {
                  di.watch = new URL(di.url, "META-INF/jboss-spring.xml");
               }
            }
            else
            {
               // We watch the top only, no directory support
               di.watch = di.url;
            }
         }
      }
      catch (Exception e)
      {
         log.error("failed to parse Spring context document: ", e);
         throw new DeploymentException(e);
      }
      super.init(di);
   }

   /**
    * Describe <code>create</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    * @jmx:managed-operation
    */
   public void create(DeploymentInfo di) throws DeploymentException
   {
      try
      {
         beanFactoryLoader.create(di);
         emitNotification("Spring Deploy", di);
         log.info("Deployed Spring: " + di.url);
      }
      catch (Exception e)
      {
         throw new DeploymentException(e);
      }
   }

   /**
    * The <code>start</code> method starts all the mbeans in this DeploymentInfo..
    *
    * @param di a <code>DeploymentInfo</code> value
    * @throws DeploymentException if an error occurs
    * @jmx:managed-operation
    */
   public void start(DeploymentInfo di) throws DeploymentException
   {
      beanFactoryLoader.start(di);
   }

   /**
    * Undeploys the package at the url string specified. This will: Undeploy
    * packages depending on this one. Stop, destroy, and unregister all the
    * specified mbeans Unload this package and packages this package deployed
    * via the classpath tag. Keep track of packages depending on this one that
    * we undeployed so that they can be redeployed should this one be
    * redeployed.
    *
    * @param di the <code>DeploymentInfo</code> value to stop.
    * @jmx:managed-operation
    */
   public void stop(DeploymentInfo di)
   {
      log.info("Undeploying Spring: " + di.url);
      try
      {
         beanFactoryLoader.stop(di);
         emitNotification("Spring Undeploy", di);
      }
      catch (Exception e)
      {
         log.error("Failed to stop bean factory: " + di.url);
      }
   }

   /**
    * Describe <code>destroy</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @jmx:managed-operation
    */
   public void destroy(DeploymentInfo di)
   {
      try
      {
         beanFactoryLoader.destroy(di);
         emitNotification("Spring Destroy", di);
      }
      catch (DeploymentException e)
      {
         log.error("Failed to destroy deployer: " + di);
      }
   }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
         throws MalformedObjectNameException
   {
      return name == null ? getDefaultObjectName() : name;
   }

   protected abstract ObjectName getDefaultObjectName();

}
