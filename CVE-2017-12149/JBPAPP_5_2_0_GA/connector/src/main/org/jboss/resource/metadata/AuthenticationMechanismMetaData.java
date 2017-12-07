/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.metadata;

/**
 * Authentication mechanism meta data
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class AuthenticationMechanismMetaData extends DescriptionMetaDataContainer
{
   static final long serialVersionUID = 1562443409483033688L;

   /** The authentication mechanism type */
   private String authenticationMechanismType;

   /** The credential interface class */
   private String credentialInterfaceClass;

   /**
    * Get the authentication mechanism type
    * 
    * @return the authentication mechanism type
    */
   public String getAuthenticationMechansimType()
   {
      return authenticationMechanismType;
   }

   /**
    * Set the authentication mechanism type
    * 
    * @param authenticationMechanismType the type
    */
   public void setAuthenticationMechansimType(String authenticationMechanismType)
   {
      this.authenticationMechanismType = authenticationMechanismType;
   }

   /**
    * Get the credential interface class
    * 
    * @return the credential interface class
    */
   public String getCredentialInterfaceClass()
   {
      return credentialInterfaceClass;
   }

   /**
    * Set the credential interface class
    * 
    * @param credentialInterfaceClass the class
    */
   public void setCredentialInterfaceClass(String credentialInterfaceClass)
   {
      this.credentialInterfaceClass = credentialInterfaceClass;
   }
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("AuthenticationMechanismMetaData").append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[authenticationMechanismType=").append(authenticationMechanismType);
      buffer.append(" credentialInterfaceClass=").append(credentialInterfaceClass);
      buffer.append(" descriptions=").append(getDescriptions());
      buffer.append(']');
      return buffer.toString();
   }
}
