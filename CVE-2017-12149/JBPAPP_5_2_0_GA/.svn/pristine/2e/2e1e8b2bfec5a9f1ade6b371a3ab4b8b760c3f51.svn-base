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
package org.jboss.security.deployers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.security.xacml.jaxb.PDP;

/**
 * Parsing Deployer for JBossXACML Config
 * @author Anil.Saldhana@redhat.com
 * @since Mar 17, 2009
 */
@SuppressWarnings("unchecked")
public class XacmlConfigParsingDeployer extends JAXBElementParsingDeployer<JAXBElement,PDP>
{ 
   public final static String XACML_ATTACHMENT_NAME = "xacml.config";
   
   protected JAXBContext context;
   
   public XacmlConfigParsingDeployer()
   {
      super(JAXBElement.class,PDP.class); 
      setName("jbossxacml-config.xml"); 
   } 
   

   /**
    * Get the Config File Name
    * @return
    */
   public String getConfigFileName()
   {
      return getName();
   }
   
   /**
    * Set the JBossXACML Config File Name
    * @param fileName
    */
   public void setConfigFileName(String fileName)
   {
      this.setName(fileName);
   }


   /**
    * Method overridden to make the attachment name to be not the same as
    * the deployment type but a custom name
    */
   @Override
   protected void createMetaData(DeploymentUnit unit, String name, String suffix) throws DeploymentException
   {
      createMetaData(unit, name, suffix, XACML_ATTACHMENT_NAME);
   } 
}
