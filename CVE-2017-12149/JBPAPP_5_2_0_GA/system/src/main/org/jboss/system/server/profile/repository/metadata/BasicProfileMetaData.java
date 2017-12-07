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

import javax.xml.bind.annotation.XmlElement;

import org.jboss.profileservice.spi.metadata.ProfileDeploymentMetaData;
import org.jboss.profileservice.spi.metadata.ProfileSourceMetaData;
import org.jboss.profileservice.spi.metadata.SubProfileMetaData;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86174 $
 */
public class BasicProfileMetaData extends AbstractProfileMetaData
{
   
   /** The profile source. */
   private ProfileSourceMetaData source;
   
   /** The deployments. */
   private List<ProfileDeploymentMetaData> deployments;
   
   public BasicProfileMetaData()
   {
      super();
   }
   
   public BasicProfileMetaData(String domain, String server, String name)
   {
      super(domain, server, name, null);
   }
   
   public BasicProfileMetaData(String domain, String server, String name, List<SubProfileMetaData> subProfiles)
   {
      super(domain, server, name, subProfiles);
   }
   
   @XmlElement(name = "profile-source", type = ImmutableProfileSourceMetaData.class)
   public ProfileSourceMetaData getSource()
   {
      return source;
   }

   public void setSource(ProfileSourceMetaData source)
   {
      this.source = source;
   }   

   @XmlElement(name = "deployment", type = BasicProfileDeploymentMetaData.class)
   public List<ProfileDeploymentMetaData> getDeployments()
   {
      return deployments;
   }
   
   public void setDeployments(List<ProfileDeploymentMetaData> deployments)
   {
      this.deployments = deployments;
   }
   
}
