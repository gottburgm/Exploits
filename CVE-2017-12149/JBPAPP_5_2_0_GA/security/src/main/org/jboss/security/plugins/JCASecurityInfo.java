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
package org.jboss.security.plugins;

import java.security.Provider;
import java.security.Security;
import java.util.Set;

//$Id: JCASecurityInfo.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Utility class that provides the Java Cryptography Architecture(JCA)
 *  information about the JVM
 *  @author Anil.Saldhana@redhat.com
 *  @since  Mar 29, 2007 
 *  @version $Revision: 85945 $
 */
public class JCASecurityInfo
{ 
   private String DELIMITER = ";";
   
   public JCASecurityInfo()
   { 
   }
   /**
    * Get information on all the JCA Providers
    * @return
    */
   public String getJCAProviderInfo()
   { 
      StringBuilder sb = new StringBuilder();
      sb.append("Providers=");
      Provider[] providers = Security.getProviders();
      for(Provider p:providers)
      {
         sb.append(p.toString()).append(DELIMITER);
      }
      return sb.toString();
   }
   
   /**
    * Get the set of algorithms for a particular service
    * (Cipher,Signature,KeyFactory,SecretKeyFactory,AlgorithmParameters 
    *  MessageDigest,Mac)
    * @param serviceName
    * @return
    */
   public String getJCAAlgorithms(String serviceName)
   {
      StringBuilder sb = new StringBuilder();
      Set<String> md2 = Security.getAlgorithms(serviceName);
      sb.append(serviceName).append(":algorithms=").append(md2.size()).append("["); 
      
      for(String algo:md2)
      {
         sb.append(algo).append(DELIMITER);
      }
      sb.append("]");
      
      return sb.toString(); 
   } 
}
