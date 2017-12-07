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

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.deployment.JSFDeployment;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilterWithAttributes;
import org.jboss.virtual.VisitorAttributes;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Parses a faces-config.xml file, present in WEB-INF or META-INF folders, for the presence of JSF managed beans and
 * updates the {@link JSFDeployment} metadata with this information.
 * <p/>
 * Note that this deployer is just interested in the presence of any JSF managed beans in the faces configuration files
 * and doesn't do any real parsing of the faces configuration files. The real and complete parsing of the faces
 * configuration files is left to the JSF implementation provider.
 *
 * @author Jaikiran Pai
 * @see https://issues.jboss.org/browse/JBAS-8318
 */
public class FacesConfigParsingDeployer extends AbstractVFSParsingDeployer<JSFDeployment>
{

   /**
    * Logger
    */
   private static final Logger logger = Logger.getLogger(FacesConfigParsingDeployer.class);

   /**
    * Create a new FacesConfigParsingDeployer.
    */
   public FacesConfigParsingDeployer()
   {
      // we output JSFDeployment
      super(JSFDeployment.class);
      // we parse faces-config.xml file
      this.setName("faces-config.xml");
      // allow multiple faces-config.xml in the deployment unit
      this.setAllowMultipleFiles(true);
   }

   @Override
   protected boolean allowsReparse()
   {
      return true;
   }

   /**
    * Finds the files with the passed <code>name</code>, under the metadata locations, in the deployment unit and passes
    * each such file to {@link #parse(org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit, org.jboss.virtual.VirtualFile, org.jboss.deployment.JSFDeployment)}
    * method of this deployer.
    * <p/>
    * Note that we override this method due to a bug in the parsing deployers, which results in only 1 file with the "name" being picked up
    * from the metadata locations of the unit, even if there are more than one files with that name.
    * For example: If a .war had faces-config.xml in .war/WEB-INF and .war/lib/somejar.jar/META-INF folders, then ideally both these
    * files should have been picked up. But due to a bug in the parsing deployers, only the .war/WEB-INF/faces-config.xml would be picked up.
    * Hence we override this method to fix the problem, by picking all available faces-config.xml from the metadata locations and parsing
    * each of those.
    *
    * @param unit   The deployment unit
    * @param name   The name of the file the deployer is interested in
    * @param output The metadata output
    * @return
    * @throws Exception
    */
   @Override
   protected JSFDeployment parse(DeploymentUnit unit, String name, JSFDeployment output) throws Exception
   {
      if (unit instanceof VFSDeploymentUnit == false)
      {
         return null;
      }

      if (ignoreName(unit, name))
      {
         return null;
      }

      VFSDeploymentUnit vfsDeploymentUnit = (VFSDeploymentUnit) unit;
      List<VirtualFile> facesConfigXmlFiles = vfsDeploymentUnit.getMetaDataFiles(new FacesConfigXmlFileNameMatchFilter());
      if (facesConfigXmlFiles == null || facesConfigXmlFiles.isEmpty())
      {
         return null;
      }
      JSFDeployment jsfDeployment = vfsDeploymentUnit.getAttachment(JSFDeployment.class);
      for (VirtualFile facesConfigXmlFile : facesConfigXmlFiles)
      {
         if (this.ignoreFile(vfsDeploymentUnit, facesConfigXmlFile))
         {
            continue;
         }
         jsfDeployment = this.parse(vfsDeploymentUnit, facesConfigXmlFile, jsfDeployment);
      }
      return jsfDeployment;
   }

   /**
    * Parse the passed faces configuration <code>file</code> and update the <code>jsfDeployment</code> with the class names
    * of the managed beans configured in that file.
    *
    * @param unit          The deployment unit
    * @param file          The faces configuration file
    * @param jsfDeployment The {@link JSFDeployment} metadata
    * @return
    * @throws Exception
    */
   @Override
   protected JSFDeployment parse(VFSDeploymentUnit unit, VirtualFile file, JSFDeployment jsfDeployment) throws Exception
   {
      URL facesConfigURL = file.toURL();
      if (jsfDeployment == null)
      {
         // create the jsf deployment. Note that we don't have to attach it to the deployment unit, since that part
         // will be done by the AbstractVFSParsingDeployer which will attach the return value of this method to the unit.
         jsfDeployment = new JSFDeployment();
      }
      // parse the xml file and update the jsf deployment
      FacesConfigParsingUtil.parse(unit, facesConfigURL, jsfDeployment);
      // return the updated jsf deployment
      return jsfDeployment;
   }

   /**
    * A file name based virtual file filter which accepts only files named faces-config.xml which
    * are present in a META-INF folder or a WEB-INF folder.
    */
   private class FacesConfigXmlFileNameMatchFilter implements VirtualFileFilterWithAttributes
   {

      public VisitorAttributes getAttributes()
      {
         return VisitorAttributes.LEAVES_ONLY;
      }

      /**
       * Returns true if the file is named faces-config.xml and is present in META-INF or WEB-INF folder.
       * Else returns false.
       *
       * @param file
       * @return
       */
      public boolean accepts(VirtualFile file)
      {
         if (file == null)
         {
            return false;
         }
         String fileName = file.getName();
         if (fileName.equals("faces-config.xml"))
         {
            // file name matches, now make sure its parent is META-INF or WEB-INF
            try
            {
               VirtualFile parent = file.getParent();
               if (parent == null)
               {
                  return false;
               }
               String parentName = parent.getName();
               if (parentName.equals("META-INF") || parentName.equals("WEB-INF"))
               {
                  // it's a match!
                  return true;
               }
            }
            catch (IOException ioe)
            {
               // ignore and just log a debug level message and return false.
               logger.debug("Ignorning faces-config.xml file: " + file + " because of exception while trying to find its parent", ioe);
            }
         }
         return false;
      }
   }
}
