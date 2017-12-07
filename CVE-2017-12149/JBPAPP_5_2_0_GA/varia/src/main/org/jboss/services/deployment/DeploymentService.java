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
package org.jboss.services.deployment;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.jboss.system.ListenerServiceMBeanSupport;

/**
 * @jmx:mbean
 *    extends="org.jboss.system.ListenerServiceMBean"
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class DeploymentService
   extends ListenerServiceMBeanSupport
   implements DeploymentServiceMBean
{
   // Constants -----------------------------------------------------

   /** where to look for templates */
   public static final String DEFAULT_TEMPLATE_DIR = "conf/templates";

   /** where modules are created/removed */
   public static final String DEFAULT_UNDEPLOY_DIR = "undeploy";

   /** where modules are moved for hot deployment */
   public static final String DEFAULT_DEPLOY_DIR = "deploy";

   // Private Data --------------------------------------------------

   /** delegate responsible for doing the dirty work */
   private DeploymentManager manager;

   /** where factory templates should be found */
   private String templateDir;

   /** the directory to use for creating modules */
   private String undeployDir;

   /** the directory to use for deploying modules */
   private String deployDir;

   // Constructors --------------------------------------------------

   /**
    * CTOR
   **/
   public DeploymentService()
   {
      templateDir = DEFAULT_TEMPLATE_DIR;
      undeployDir = DEFAULT_UNDEPLOY_DIR;
      deployDir   = DEFAULT_DEPLOY_DIR;
   }

   // MBean Attributes ----------------------------------------------

   /**
    * @jmx:managed-attribute
    *
    * @param templateDir The templateDir to set.
    */
   public void setTemplateDir(String templateDir)
   {
      this.templateDir = templateDir;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return Returns the templateDir.
    */
   public String getTemplateDir()
   {
      return templateDir;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getUndeployDir()
   {
      return undeployDir;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setUndeployDir(String undeployDir)
   {
      this.undeployDir = undeployDir;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getDeployDir()
   {
      return deployDir;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setDeployDir(String deployDir)
   {
      this.deployDir = deployDir;
   }

   // MBean Operations ----------------------------------------------

   /**
    * @jmx:managed-operation
    */
   public Set listModuleTemplates()
   {
      return manager.listModuleTemplates();
   }

   /**
    * @jmx:managed-operation
    */
   public List getTemplatePropertyInfo(String template)
      throws Exception
   {
      return manager.getTemplatePropertyInfo(template);
   }

   /**
    * @jmx:managed-operation
    */
   public String createModule(String module, String template, HashMap properties)
      throws Exception
   {
      return manager.createModule(module, template, properties);
   }

   /**
    * Used primarily for testing through the jmx-console
    *
    * @jmx:managed-operation
    */
   public String createModule(String module, String template, String[] properties)
      throws Exception
   {
      // load a hashmap with all key/values
      HashMap map = new HashMap();

      for (int i = 0; i < properties.length; i++)
      {
         StringTokenizer st = new StringTokenizer(properties[i], "=");

         String key = st.nextToken();
         String value = st.nextToken();

         if (value.indexOf('|') >= 0)
         {
            // treat value as a String array
            StringTokenizer st2 = new StringTokenizer(value, "|");

            int tokens = st2.countTokens();
            String[] array = new String[tokens];
            for (int j = 0; j < tokens; j++)
               array[j] = st2.nextToken();

            map.put(key, array);
         }
         else
         {
            map.put(key, value);
         }
      }
      return manager.createModule(module, template, map);
   }

   /**
    * @jmx:managed-operation
    */
   public boolean removeModule(String module)
   {
      return manager.removeModule(module);
   }

   /**
    * @jmx:managed-operation
    */
   public boolean updateMBean(MBeanData data) throws Exception
   {
      return manager.updateMBean(data);
   }

   /**
    * @jmx:managed-operation
    */
   public String updateDataSource(String module, String template, HashMap properties)
      throws Exception
   {
      return manager.updateDataSource(module, template, properties);
   }

   /**
    * @jmx:managed-operation
    */
   public String removeDataSource(String module, String template, HashMap properties)
      throws Exception
   {
      return manager.removeDataSource(module, template, properties);
   }

   /**
    * @jmx:managed-operation
    */
   public void deployModuleAsynch(String module)
      throws Exception
   {
      manager.moveToDeployDir(module);
   }

   /**
    * @jmx:managed-operation
    */
   public URL getDeployedURL(String module)
      throws Exception
   {
      return manager.getDeployedURL(module);
   }

   /**
    * @jmx:managed-operation
    */
   public void undeployModuleAsynch(String module)
      throws Exception
   {
      manager.moveToModuleDir(module);
   }

   /**
    * @jmx:managed-operation
    */
   public URL getUndeployedURL(String module)
      throws Exception
   {
      return manager.getUndeployedURL(module);
   }

   /**
    * Upload a new library to server lib dir. A different
    * filename may be specified, when writing the library.
    *
    * If the target filename exists, upload is not performed.
    *
    * @jmx:managed-operation
    *
    * @param src the source url to copy
    * @param filename the filename to use when copying (optional)
    * @return true if upload was succesful, false otherwise
    */
   public boolean uploadLibrary(URL src, String filename)
   {
      return LibraryManager.getInstance().uploadLibrary(src, filename);
   }

   // MBean Lifecycle  ----------------------------------------------

   public void startService()
      throws Exception
   {
      manager = new DeploymentManager(templateDir, undeployDir, deployDir, log);
   }

   public void stopService()
   {
      manager = null;
   }

}
