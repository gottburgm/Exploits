/*
* JBoss, Home of Professional Open Source
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
package org.jboss.system.server.security;

import java.net.URL;
import java.security.Policy;

import org.jboss.beans.metadata.api.annotations.Start;

/**
 * SecurityPolicy.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85526 $
 */
public class SecurityPolicy
{
   /** Whether to install the security manager */
   private SecurityManager securityManager;
   
   /** The policy url */
   private URL policyURL;

   /**
    * Get the securityManager.
    * 
    * @return the securityManager.
    */
   public SecurityManager getSecurityManager()
   {
      return securityManager;
   }

   /**
    * Set the securityManager.
    * 
    * @param securityManager the securityManager.
    */
   public void setSecurityManager(SecurityManager securityManager)
   {
      this.securityManager = securityManager;
   }

   /**
    * Get the policyURL.
    * 
    * @return the policyURL.
    */
   public URL getPolicyURL()
   {
      return policyURL;
   }

   /**
    * Set the policyURL.
    * 
    * @param policyURL the policyURL.
    */
   public void setPolicyURL(URL policyURL)
   {
      this.policyURL = policyURL;
   }
   
   @Start
   public void start()
   {
      if (policyURL != null)
         System.setProperty("java.security.policy", policyURL.toExternalForm());
      Policy.getPolicy().refresh();
      
      if (securityManager != null)
         System.setSecurityManager(securityManager);
   }
   
   public void stop()
   {
      if (securityManager != null)
         System.setSecurityManager(null);
   }
}
