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
package org.jboss.resource.deployers.management;

import java.lang.reflect.Type;

import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.EnumMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.EnumValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.plugins.types.MutableCompositeMetaType;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jboss.resource.metadata.mcf.ApplicationManagedSecurityMetaData;
import org.jboss.resource.metadata.mcf.SecurityDeploymentType;
import org.jboss.resource.metadata.mcf.SecurityDomainApplicationManagedMetaData;
import org.jboss.resource.metadata.mcf.SecurityDomainMetaData;
import org.jboss.resource.metadata.mcf.SecurityMetaData;

/**
 * A security-domain meta mapper.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class SecurityDomainMetaMapper extends MetaMapper<SecurityMetaData>
{

   /** The composite names. */
   private static final String DOMAIN = "domain";
   private static final String DeploymentTypeName = "securityDeploymentType";
   
   /** The meta value factory. */
   private static final MetaValueFactory metaValueFactory = MetaValueFactory.getInstance();
   
   /** The meta type. */
   private static final MutableCompositeMetaType metaType;
   
   static
   {
      // Create the type manually, as the creation from the metaTypeFactory doesn't work
      EnumMetaType enumMetaType = new EnumMetaType(SecurityDeploymentType.values());
      metaType = new MutableCompositeMetaType(SecurityMetaData.class.getName(), "The security domain meta data");
      metaType.addItem(DOMAIN, "the security domain", SimpleMetaType.STRING);
      metaType.addItem(DeploymentTypeName, "the security deployment type", enumMetaType);
      metaType.freeze();
   }
   
   @Override
   public Type mapToType()
   {
      return SecurityMetaData.class;
   }
   
   @Override
   public MetaType getMetaType()
   {
      return metaType;
   }
   
   @Override
   public MetaValue createMetaValue(MetaType metaType, SecurityMetaData object)
   {
      if(object == null)
         return null;
      if(metaType instanceof CompositeMetaType)
      {
         // Extract the meta types
         CompositeMetaType composite = (CompositeMetaType) metaType;
         EnumMetaType enumMetaType= (EnumMetaType) composite.getType(DeploymentTypeName);
         // Create the composite value
         CompositeValueSupport securityDomain = new CompositeValueSupport(composite);
         // Set a default deplooymentType
         SecurityDeploymentType deploymentType = object.getSecurityDeploymentType();
         if(deploymentType == null)
            deploymentType = SecurityDeploymentType.NONE;
         
         // Set domain and deployment type
         securityDomain.set(DOMAIN, SimpleValueSupport.wrap(object.getDomain()));
         securityDomain.set(DeploymentTypeName, new EnumValueSupport(enumMetaType, deploymentType));
         //
         return securityDomain;
      }
      throw new IllegalArgumentException("Cannot convert securityDomain " + object);
   }

   @Override
   public SecurityMetaData unwrapMetaValue(MetaValue metaValue)
   {
      if(metaValue == null)
         return null;
      
      if(metaValue instanceof CompositeValue)
      {
         CompositeValue compositeValue = (CompositeValue) metaValue;

         String domainName = (String) metaValueFactory.unwrap(compositeValue.get(DOMAIN));         
         SecurityDeploymentType deploymentType = (SecurityDeploymentType) metaValueFactory.unwrap(compositeValue.get(DeploymentTypeName));
         if(deploymentType == null)
            deploymentType = SecurityDeploymentType.NONE;
         
         SecurityMetaData securityDomain = null;
         switch(deploymentType)
         {
            case APPLICATION:
               securityDomain = new ApplicationManagedSecurityMetaData();
               break;
            case DOMAIN:
               securityDomain = new SecurityDomainMetaData();
               break;
            case DOMAIN_AND_APPLICATION:
               securityDomain = new SecurityDomainApplicationManagedMetaData();
               break;
            default:
            case NONE:
               securityDomain = new SecurityMetaData();
               break;
         }
         // Sanity check
         if(securityDomain.requiresDomain() && domainName == null)
            throw new IllegalStateException("null security domain not allowed for SecurityDeploymentType " + deploymentType);
         // Set the domain
         securityDomain.setDomain(domainName);
         //
         return securityDomain;
      }
      throw new IllegalStateException("Unable to unwrap securityDomain " + metaValue);
   }

}

