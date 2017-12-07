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
package org.jboss.varia.deployment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.ObjectNameConverter;
import org.jboss.system.ServiceControllerMBean;

/**
 * A deployer that takes a bean shell script file and creates a JBoss
 * MBean service wrapper for the script.
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version $Revision: 81038 $
 *
 * @jmx.mbean name="jboss.system:service=BeanShellSubDeployer"
 *            extends="org.jboss.deployment.SubDeployerMBean"
 */
@Deprecated
public class BeanShellSubDeployer extends SubDeployerSupport
   implements BeanShellSubDeployerMBean
{
   // Constants -----------------------------------------------------
   
   //public static final String BEANSHELL_EXTENSION = ".bsh";
   public static final String BASE_SCRIPT_OBJECT_NAME = "jboss.scripts:type=BeanShell";

   /** The suffixes we accept, along with their relative order */
   private static final String[] DEFAULT_ENHANCED_SUFFIXES = new String[] {
         "800:.bsh"
   };
   
   // Attributes ----------------------------------------------------
   
   protected ServiceControllerMBean serviceController;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   /**
    * Default contructor used to set default values to the Suffixes and RelativeOrder
    * attributes. Those are read at subdeployer registration time by the MainDeployer
    * to alter its SuffixOrder.
    */
   public BeanShellSubDeployer()
   {
      setEnhancedSuffixes(DEFAULT_ENHANCED_SUFFIXES);    
   }  
   
   // Public --------------------------------------------------------

   // Z implementation ----------------------------------------------
   
   // ServiceMBeanSupport overrides ---------------------------------------------------
   
   /**
    * Get a reference to the ServiceController
    */
   protected void startService() throws Exception
   {
      serviceController = (ServiceControllerMBean)
         MBeanProxyExt.create(ServiceControllerMBean.class,
           ServiceControllerMBean.OBJECT_NAME, server);

      // register with MainDeployer
      super.startService();
   }

   // SubDeployerSupport overrides ---------------------------------------------------

   protected void processNestedDeployments(DeploymentInfo di) throws DeploymentException 
   {
      // no sub-deployment!      
   }

   /**
    * Returns true if this deployer can deploy the given DeploymentInfo.
    *
    * @return   True if this deployer can deploy the given DeploymentInfo.
    * 
    * @jmx:managed-operation
    */
   public boolean accepts(DeploymentInfo sdi)
   {
      return super.accepts(sdi);
   }

   /**
    * Describe <code>init</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx:managed-operation
    */
   public void init(DeploymentInfo di)
      throws DeploymentException
   {
      super.init(di);
      di.watch = di.url;
   }

   /**
    * Describe <code>create</code> method here.
    *
    * @param di a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    * @jmx:managed-operation
    */
   public void create(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         // install the MBeans in this descriptor
         log.debug("Deploying BeanShell script, create step: url " + di.url);
         
         String lURL = di.url.toString();
         int lIndex = lURL.lastIndexOf( "/" );
         di.shortName = lURL.substring( lIndex >= 0 ? lIndex + 1 : 0 );
                  
         BeanShellScript script = new BeanShellScript (di);
         ObjectName bshScriptName = script.getPreferedObjectName();
         ObjectName[] depends = script.getDependsServices();
         
         if (bshScriptName == null)
         {            
            bshScriptName = ObjectNameConverter.convert(
               BASE_SCRIPT_OBJECT_NAME + ",url=" + di.url);
         }

         di.deployedObject = bshScriptName;
         try
         {
            server.unregisterMBean(bshScriptName);
         } catch(Exception e) { log.info(e);}
         server.registerMBean(script, bshScriptName);

         log.debug( "Deploying: " + di.url );

         // Init application
         if (depends == null)
            serviceController.create(bshScriptName);
         else
            serviceController.create(bshScriptName, Arrays.asList(depends));
         super.create(di);
      }
      catch (Exception e)
      {
         destroy(di);
         DeploymentException de = new DeploymentException("create operation failed for script "
            + di.url, e);
         throw de;
      }
   }

   public synchronized void start(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         // Start application
         log.debug( "start script, deploymentInfo: " + di +
                    ", short name: " + di.shortName +
                    ", parent short name: " +
                    (di.parent == null ? "no parent" : di.parent.shortName) );

         serviceController.start(di.deployedObject);

         log.debug( "Deployed: " + di.url );
         super.start(di);
      }
      catch (Exception e)
      {
         throw new DeploymentException( "Could not deploy " + di.url, e );
      }
   }

   public void stop(DeploymentInfo di)
      throws DeploymentException
   {
      try
      {
         serviceController.stop(di.deployedObject);
         super.stop(di);
      }
      catch (Exception e)
      {
         throw new DeploymentException( "problem stopping ejb module: " +
            di.url, e );
      }
   }

   public void destroy(DeploymentInfo di) 
      throws DeploymentException
   {
      try
      {
         serviceController.destroy( di.deployedObject );
         serviceController.remove( di.deployedObject );
         super.destroy(di);
      }
      catch (Exception e)
      {
         throw new DeploymentException( "problem destroying BSH Script: " +
            di.url, e );
      }
   }

   /** Create a bsh deployment given the script content and name. This creates
    * a temp file using File.createTempFile(scriptName, ".bsh") and then
    * deploys this script via the main deployer.
    *
    * @param bshScript the bsh script content
    * @param scriptName the bsh script name to use
    * @return the URL of the temporary file used as the deployment script
    *
    * @throws DeploymentException thrown on failure to create the bsh
    * script or deploy it.
    *
    * @jmx:managed-operation
    */
   public URL createScriptDeployment(String bshScript, String scriptName)
      throws DeploymentException
   {
      try
      {
         File scriptFile = File.createTempFile(scriptName, ".bsh");
         FileWriter fw = new FileWriter(scriptFile);
         try
         {
            fw.write(bshScript);
         }
         finally
         {
            fw.close();
         }

         URL scriptURL = scriptFile.toURL();
         mainDeployer.deploy(scriptURL);
         return scriptURL;
      }
      catch(IOException e)
      {
         throw new DeploymentException("Failed to deploy: "+scriptName, e);
      }
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}
