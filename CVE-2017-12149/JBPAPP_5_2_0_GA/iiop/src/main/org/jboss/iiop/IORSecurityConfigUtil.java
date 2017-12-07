/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.iiop;

import org.jboss.metadata.IorSecurityConfigMetaData;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * <p>
 * Utility class that creates {@code IorSecurityConfigMetaData} by parsing a XML configuration that contains
 * the IOR security settings.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class IORSecurityConfigUtil
{

   /**
    * <p>
    * Parses the specified XML element and creates an instance of {@code IorSecurityConfigMetaData} with the data
    * that has been extracted from the XML.
    * </p>
    * 
    * @param element the {@code Element} that contains the IOR security configuration.
    * @return the constructed {@code IorSecurityConfigMetaData} instance.
    */
   public static IorSecurityConfigMetaData parseIorSecurityConfigMetaData(Element element)
   {
      IorSecurityConfigMetaData metadata = new IorSecurityConfigMetaData();

      // parse the transport configuration.
      Element child = MetaData.getOptionalChild(element, "transport-config");
      if (child != null)
      {
         String integrity = MetaData.getUniqueChildContent(child, "integrity");
         String confidentiality = MetaData.getUniqueChildContent(child, "confidentiality");
         String establishTrustInTarget = MetaData.getUniqueChildContent(child, "establish-trust-in-target");
         String establishTrustInClient = MetaData.getUniqueChildContent(child, "establish-trust-in-client");
         String detectMisordering = MetaData.getOptionalChildContent(child, "detect-misordering");
         String detectReplay = MetaData.getOptionalChildContent(child, "detect-replay");
         metadata.setTransportConfig(metadata.new TransportConfig(integrity, confidentiality,
               establishTrustInTarget, establishTrustInClient, detectMisordering, detectReplay));
      }
      else
         throw new IllegalArgumentException("The IOR transport config cannot be null");

      // parse the authentication service configuration.
      child = MetaData.getOptionalChild(element, "as-context");
      if (child != null)
      {
         String authMethod = MetaData.getUniqueChildContent(child, "auth-method");
         String realm = MetaData.getUniqueChildContent(child, "realm");
         boolean required = Boolean.parseBoolean(MetaData.getUniqueChildContent(child, "required"));
         metadata.setAsContext(metadata.new AsContext(authMethod, realm, required));
      }
      else
         throw new IllegalArgumentException("The IOR AS context config cannot be null");
      
      // parse the security attribute service configuration.
      child = MetaData.getOptionalChild(element, "sas-context");
      if (child != null)
      {
         String callerPropagation = MetaData.getUniqueChildContent(child, "caller-propagation");
         metadata.setSasContext(metadata.new SasContext(callerPropagation));
      }
      else
         throw new IllegalArgumentException("The IOR SAS context config cannot be null");
      
      return metadata;
   }
}