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
package org.jboss.wsf.container.jboss50.deployment.tomcat;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.metadata.javaee.spec.SecurityRoleMetaData;
import org.jboss.metadata.javaee.spec.SecurityRolesMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.integration.WebServiceDeclaration;
import org.jboss.wsf.spi.deployment.integration.WebServiceDeployment;

import javax.annotation.security.RolesAllowed;
import java.util.Iterator;

/**
 * Handle web app security meta data for EJB3 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public class SecurityHandlerEJB3 implements SecurityHandler
{
   public void addSecurityDomain(JBossWebMetaData jbossWeb, Deployment dep)
   {
      String securityDomain = null;

      WebServiceDeployment webServiceDeployment = dep.getAttachment(WebServiceDeployment.class);
      if (webServiceDeployment != null)
      {
         Iterator<WebServiceDeclaration> it = webServiceDeployment.getServiceEndpoints().iterator();
         while (it.hasNext())
         {
            WebServiceDeclaration container = it.next();
            SecurityDomain anSecurityDomain = container.getAnnotation(SecurityDomain.class);
            if (anSecurityDomain != null)
            {
               if (securityDomain != null && !securityDomain.equals(anSecurityDomain.value()))
                  throw new IllegalStateException("Multiple security domains not supported");

               securityDomain = anSecurityDomain.value();
            }
         }
      }

      if (securityDomain != null)
      {
         if (securityDomain.startsWith("java:/jaas/") == false)
            securityDomain = "java:/jaas/" + securityDomain;

         jbossWeb.setSecurityDomain(securityDomain);
      }
   }

   public void addSecurityRoles(JBossWebMetaData webApp, Deployment dep)
   {
      WebServiceDeployment webServiceDeployment = dep.getAttachment(WebServiceDeployment.class);
      if (webServiceDeployment != null)
      {
         Iterator<WebServiceDeclaration> it = webServiceDeployment.getServiceEndpoints().iterator();
         while (it.hasNext())
         {
            WebServiceDeclaration container = it.next();
            RolesAllowed anRolesAllowed = container.getAnnotation(RolesAllowed.class);
            if (anRolesAllowed != null)
            {
               SecurityRolesMetaData securityRoles = webApp.getSecurityRoles();
               for (String roleName : anRolesAllowed.value())
               {
                  SecurityRoleMetaData role = new SecurityRoleMetaData();
                  role.setRoleName(roleName);
                  securityRoles.add(role);
               }
            }
         }
      }
   }
}
