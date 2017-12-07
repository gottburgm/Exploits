/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.web.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.JSFDeployment;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.virtual.VirtualFile;

import java.util.List;

/**
 * Deployer which picks up .war deployments and parses the JSF faces configuration files listed in the
 * javax.faces.CONFIG_FILES context param of the web.xml of the deployment.
 * <p/>
 * Note that this deployer is just interested in the presence of any JSF managed beans in the faces configuration files
 * listed in the javax.faces.CONFIG_FILES and doesn't do any real parsing of the faces configuration files. The real and
 * complete parsing of the faces configuration files is left to the JSF implementation provider.
 *
 * @author Jaikiran Pai
 * @see https://issues.jboss.org/browse/JBAS-8318
 */
public class WebContextParamFacesConfigParsingDeployer extends AbstractDeployer
{
   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(WebContextParamFacesConfigParsingDeployer.class);

   /**
    * javax.faces.CONFIG_FILES context param
    */
   private static final String JAVAX_FACES_CONFIG_FILES_CONTEXT_PARAM_NAME = "javax.faces.CONFIG_FILES";

   /**
    * The default location /WEB-INF/faces-config.xml for an application
    */
   private static final String WEB_INF_FACES_CONFIG_XML = "/WEB-INF/faces-config.xml";

   /**
    *
    */
   public WebContextParamFacesConfigParsingDeployer()
   {
      // we depend on the parsed web.xml
      this.setInput(WebMetaData.class);
      // we output JSFDeployment
      this.setOutput(JSFDeployment.class);
      // we run in Parse phase
      this.setStage(DeploymentStages.PARSE);
   }


   /**
    * Checks for the presence of {@link WebMetaData} attachment in the deployment unit and then checks for
    * javax.faces.CONFIG_FILES context param in the web metadata. If found, each of the paths specified in the
    * context param value are resolved relative to the deployment unit root and the resulting file is parsed as
    * a faces configuration file, to check for the presence of any JSF managed beans. If any JSF managed beans are found,
    * those are then registered in a {@link JSFDeployment}, which is then attached to the deployment unit.
    *
    * @param unit The deployment unit being processed
    * @throws DeploymentException
    */
   public void deploy(final DeploymentUnit unit) throws DeploymentException
   {
      // we require a VFSDeploymentUnit, to be able to pick up context relative
      // config files
      if (unit instanceof VFSDeploymentUnit == false)
      {
         return;
      }
      final VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;
      // get hold of the parsed web.xml metadata
      WebMetaData webMetaData = unit.getAttachment(WebMetaData.class);
      // shouldn't really happen, because we have set WebMetaData as a required input.
      if (webMetaData == null)
      {
         return;
      }
      List<ParamValueMetaData> contextParams = webMetaData.getContextParams();
      if (contextParams == null || contextParams.isEmpty())
      {
         return;
      }
      JSFDeployment jsfDeployment = vfsDeploymentUnit.getAttachment(JSFDeployment.class);
      if (jsfDeployment == null)
      {
         // create and attach
         jsfDeployment = new JSFDeployment();
         vfsDeploymentUnit.addAttachment(JSFDeployment.class, jsfDeployment);
      }
      for (ParamValueMetaData contextParam : contextParams)
      {
         if (contextParam == null)
         {
            continue;
         }
         if (JAVAX_FACES_CONFIG_FILES_CONTEXT_PARAM_NAME.equals(contextParam.getParamName()))
         {
            try
            {
               logger.debug("Found " + JAVAX_FACES_CONFIG_FILES_CONTEXT_PARAM_NAME + " param with values: "
                       + contextParam.getParamValue() + " in unit " + vfsDeploymentUnit);
               // process each of the paths specified in the context param value
               this.processConfigFilesContextParamValue(vfsDeploymentUnit, jsfDeployment, contextParam.getParamValue());
            }
            catch (Exception e)
            {
               throw new DeploymentException(e);
            }
         }
      }
   }

   /**
    * Parses the context param value representing the relative paths to faces configuration files. Each of the file
    * represented by the path, is then parsed using {@link FacesConfigParsingUtil} to check for the presence of JSF
    * managed beans
    *
    * @param vfsDeploymentUnit            The deployment unit
    * @param jsfDeployment                The JSF deployment metadata
    * @param configFilesContextParamValue The javax.faces.CONFIG_FILES context param value
    * @throws Exception
    */
   private void processConfigFilesContextParamValue(final VFSDeploymentUnit vfsDeploymentUnit, final JSFDeployment jsfDeployment, final String configFilesContextParamValue) throws Exception
   {
      if (configFilesContextParamValue == null)
      {
         return;
      }
      // trim the context param value
      String trimmedConfigParamValues = configFilesContextParamValue.trim();
      // split the paths which are separated by "," delimiter
      String[] paths = trimmedConfigParamValues.split(",");
      for (String path : paths)
      {
         // trim each path
         path = path.trim();
         if (path.isEmpty())
         {
            continue;
         }
         // skip this path, since .war/WEB-INF/faces-config.xml is by default parsed
         // (by a separate deployer)
         if (WEB_INF_FACES_CONFIG_XML.equals(path))
         {
            continue;
         }
         // get hold of the file relative to the deployment unit root
         VirtualFile facesConfigXml = vfsDeploymentUnit.getRoot().getChild(path);
         // for a file which wasn't found, just log a WARN and move on to the next
         if (facesConfigXml == null)
         {
            logger.warn("Faces config xml not found at relative path: " + path + " in unit " + vfsDeploymentUnit.getRoot());
            continue;
         }
         logger.debug("Found faces config xml with relative path: " + path + " in unit " + vfsDeploymentUnit.getRoot());
         // parse the faces config file
         FacesConfigParsingUtil.parse(vfsDeploymentUnit, facesConfigXml.toURL(), jsfDeployment);
      }
   }
}
