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

import org.jboss.system.ListenerServiceMBean;

/**
 * MBean interface.
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author  <a href="mailto:peter.johnson2@unisys.com">Peter Johnson</a>
 * @version $Revision: 81038 $
 */
public interface DeploymentServiceMBean extends ListenerServiceMBean
{
   // Attributes ----------------------------------------------------

   /**
    * Sets the directory where templates are stored
    */
   void setTemplateDir(String templateDir);

   /**
    * Gets the directory where templates are stored
    */
   String getTemplateDir();

   /**
    * Sets the directory where new deployments are produced
    */
   void setUndeployDir(String undeployDir);

   /**
    * Gets the directory where new deployments are produced
    */
   String getUndeployDir();

   /**
    * Sets the directory where modules should be deployed
    */
   void setDeployDir(String deployDir);

   /**
    * Gets the directory where modules should be deployed
    */
   String getDeployDir();

   // Operations -----------------------------------------------------

   /**
    * Return the set of available deployment templates
    * Set<String>
    */
   Set listModuleTemplates();

   /**
    * Get property metadata information for a particular template
    * List<PropertyInfo>
    */
   List getTemplatePropertyInfo(String template) throws Exception;

   /**
    * Generate a new module based on the specified template
    * and the input properties
    */
   String createModule(String module, String template, HashMap properties) throws Exception;

   /**
    * Used primarily for testing through the jmx-console
    */
   String createModule(String module, String template, String[] properties) throws Exception;

   /**
    * Remove a module if exists
    */
   boolean removeModule(String module);

   /**
    * Update an existing module based on the specified template and the input
    * properties
    * @param data Data used to update the mbean descriptor. The name and
    * templateName properties are required.
    * @return True if MBean successfully updated, false otherwise.
    */
   boolean updateMBean(MBeanData data) throws Exception;

   /**
    * Update an existing data source based on the specified template
    * and the input properties.  Note that this method takes the exact same
    * parameters as the createModule method.  Thus the client code for creating
    * a new data source or updating an existing data source can be the same, and
    * only the code that makes the deployment service call needs to differentiate
    * between calling createModule and updateDataSource.
    * <p>
    * Before updating a data source, you will need to gather the properties for the
    * existing data source.  The properties can be found in the following MBeans
    * (where XXX is the data source jndi name, and YYY corresponds to the transaction
    * type: NoTxCM, LocalTxCM, XATxCM):
    * <ul>
    * <li>jboss.jca:name=XXX,service=DataSourceBinding</li>
    * <li>jboss.jca:name=XXX,service=YYY</li>
    * <li>jboss.jca:name=XXX,service=ManagedConnectionFactory</li>
    * <li>jboss.jca:name=XXX,service=ManagedConnectionPool</li>
    * <li>jboss.jdbc:datasource=XXX,service=metadata</li>
    * </ul> 
    * The service=metadata MBean provides the type-mapping, which should be provided
    * as a property named "type-mapping".
    * <p>
    * To find the dependencies, get the jboss.system:service=ServiceController MBean
    * and invoke the listDeployed method.  Then search through the returned results for
    * the jboss.jca:name=XXX,service=ManagedConnectionFactory MBean.  That entry will
    * contain the dependecies.  Note that there will be an extra implied dependency
    * named "jboss.jca:service=RARDeployment,name='jboss-ZZZ-jdbc.rar'", where ZZZ
    * is 'local' (local and no transaction types) or 'xa'.  Ignore this
    * extra dependency, do not provide it when doing an update. 
    *
    * @param module The name of the model that contains the data source definition.
    * For example, the default data source, DefaultDS, is typically found in a
    * module named "hsqldb-ds.xml", with "hsqldb" being an acceptable abbreviation.
    * @param template The name of the template to use to update the data source.
    * You must select the proper template based on the data source transaction type.
    * Use one of the following: "local-tx-datasource", "no-tx-datasource", or
    * "xa-datasource".  Alternatively, you could add the "-update" suffix; for
    * example, "local-tx-datasource-update" is the same as "local-tx-datasource".
    * @param MashMap The collection of properties used for the data source.  See
    * the template-config.xml file for the given template (in template directory)
    * for expected property names.
    * @return The full module name, with the suffix.  For example, "hsqldb-ds.xml".
    */
   String updateDataSource(String module, String template, HashMap properties) throws Exception;

   /**
    * Remove an existing data source based on the specified template
    * and the input properties.  This method takes the same parameters
    * as the updateDataSource method.  Refer to the comments of the
    * updateDataSource method for more descriptions.
    * @param module The name of the model that contains the data source definition.
    * See the module parameter under updateDataSource for more information. 
    * @param template There is only one delete template: "datasource".  Optionally,
    * you can use the template name "datasource-remove".
    * @param properties The key property to provide is "jndi-name".  This property
    * if used to determine which data source to remove.
    * @return The full module name, with the suffix.
    * @see #updateDataSource(String, String, HashMap)
    */
   String removeDataSource(String module, String template, HashMap properties) throws Exception;

   /**
    * Move a module to the deploy directory
    */
   void deployModuleAsynch(String module) throws Exception;

   /**
    * Get the URL of a deployed module
    */
   URL getDeployedURL(String module) throws Exception;

   /**
    * Move a module to the undeploy directory
    */
   void undeployModuleAsynch(String module) throws Exception;

   /**
    * Get the URL of an undeployed module
    */
   URL getUndeployedURL(String module) throws Exception;

   /**
    * Upload a new library to server lib dir. A different
    * filename may be specified, when writing the library.
    *
    * If the target filename exists, upload is not performed.
    *
    * @param src the source url to copy
    * @param filename the filename to use when copying (optional)
    * @return true if upload was succesful, false otherwise
    */
   public boolean uploadLibrary(URL src, String filename);

}
