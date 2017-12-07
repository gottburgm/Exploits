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
package org.jboss.system.server.profile.repository.metadata;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jboss.profileservice.spi.metadata.ProfileDeploymentMetaData;
import org.jboss.profileservice.spi.metadata.SubProfileMetaData;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * The meta data defining a hot deployment profile.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 87932 $
 */
@JBossXmlSchema(namespace="urn:jboss:profileservice:profile:hotdeployment:1.0", elementFormDefault=XmlNsForm.QUALIFIED)
@XmlRootElement(name = "hotdeployment-profile")
@XmlType(name = "profileType", propOrder = {"source", "subprofiles"})
public class HotDeploymentProfileMetaData extends AbstractProfileMetaData
{

   /** The profile source. */
   private HotDeploymentProfileSourceMetaData source;
   
   public HotDeploymentProfileMetaData()
   {
      super();
   }
   
   public HotDeploymentProfileMetaData(String domain, String server, String name)
   {
      super(domain, server, name, null);
   }
   
   public HotDeploymentProfileMetaData(String domain, String server, String name, List<SubProfileMetaData> subProfiles)
   {
      super(domain, server, name, subProfiles);
   }
   
   @XmlElement(name = "profile-source", type = HotDeploymentProfileSourceMetaData.class)
   public HotDeploymentProfileSourceMetaData getSource()
   {
      return source;
   }

   public void setSource(HotDeploymentProfileSourceMetaData source)
   {
      this.source = source;
   }

   /**
    * There cannot be any specific deployments for a
    * hot deployment profile, as it will scan the folders.  
    */
   @XmlTransient
   public List<ProfileDeploymentMetaData> getDeployments()
   {
      return Collections.emptyList();
   }

}

