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
package org.jboss.system.server.profileservice.attachments;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.system.server.profileservice.persistence.PersistenceConstants;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Metadata describing the persisted deployment attachments.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88716 $
 */
@JBossXmlSchema(namespace = PersistenceConstants.REPOSITORY_NAMESPACE_1_0, elementFormDefault = XmlNsForm.QUALIFIED,
      xmlns = { @XmlNs(namespaceURI = "http://www.w3.org/2001/XMLSchema", prefix = "xs") })
@XmlRootElement(name = "attachments-metadata")
public class RepositoryAttachmentMetaData
{

   /** The deployment */
   private String deploymentName;
   
   /** The last update timestamp */
   private long lastModified;
   
   /** The children */
   private List<RepositoryAttachmentMetaData> children;
   
   /** The attachments */
   private List<AttachmentMetaData> attachments;
   
   /** The deployment structure */
   private DeploymentStructureMetaData deploymentStructure;

   @XmlElement(name = "deployment-name")
   public String getDeploymentName()
   {
      return deploymentName;
   }

   public void setDeploymentName(String deploymentName)
   {
      this.deploymentName = deploymentName;
   }
   
   @XmlElement(name = "last-modified")
   public long getLastModified()
   {
      return lastModified;
   }
   
   public void setLastModified(long lastModified)
   {
      this.lastModified = lastModified;
   }
   
   @XmlElement(name = "deployment-structure")
   public DeploymentStructureMetaData getDeploymentStructure()
   {
      return deploymentStructure;
   }
   
   public void setDeploymentStructure(DeploymentStructureMetaData deploymentStructure)
   {
      this.deploymentStructure = deploymentStructure;
   }

   @XmlElement(name = "child")
   public List<RepositoryAttachmentMetaData> getChildren()
   {
      return children;
   }

   public void setChildren(List<RepositoryAttachmentMetaData> children)
   {
      this.children = children;
   }

   @XmlElement(name = "attachment")
   public List<AttachmentMetaData> getAttachments()
   {
      return attachments;
   }

   public void setAttachments(List<AttachmentMetaData> attachments)
   {
      this.attachments = attachments;
   }

}

