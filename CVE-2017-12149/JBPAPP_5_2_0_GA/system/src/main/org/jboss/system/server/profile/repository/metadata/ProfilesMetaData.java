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

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.profileservice.spi.metadata.ProfileKeyMetaData;
import org.jboss.profileservice.spi.metadata.ProfileMetaData;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * The a container xml format for profile meta data.
 * 
 * <profiles xmlns="urn:jboss:profileservice:profiles:1.0" />
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 87932 $
 */
@XmlRootElement(name = "profiles")
@JBossXmlSchema(
      ignoreUnresolvedFieldOrClass=false,
      namespace= "urn:jboss:profileservice:profiles:1.0",
      elementFormDefault=XmlNsForm.QUALIFIED,
      normalizeSpace=true)
@XmlType(name = "profilesType", propOrder = {"name", "server", "domain", "profiles"})
public class ProfilesMetaData implements ProfileKeyMetaData
{
   /** The name. */
   private String name;
   
   /** The server. */
   private String server;
   
   /** The domain. */
   private String domain;
   
   /** The profiles. */
   private List<ProfileMetaData> profiles;
   
   @XmlAttribute(name = "name")
   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   @XmlAttribute(name = "server")
   public String getServer()
   {
      return server;
   }

   public void setServer(String server)
   {
      this.server = server;
   }

   @XmlAttribute(name = "domain")
   public String getDomain()
   {
      return domain;
   }

   public void setDomain(String domain)
   {
      this.domain = domain;
   }
   
   @XmlElements({
      @XmlElement(name = "profile", type = FilteredProfileMetaData.class),
      @XmlElement(name = "hotdeployment-profile", type = HotDeploymentProfileMetaData.class)
   })
   @XmlAnyElement
   public List<ProfileMetaData> getProfiles()
   {
      return profiles;
   }
   
   public void setProfiles(List<ProfileMetaData> profiles)
   {
      this.profiles = profiles;
   }
   
}

