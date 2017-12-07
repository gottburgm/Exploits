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

import org.jboss.metadata.common.ejb.IAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.javaee.spec.SecurityRolesMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;

/**
 * Handle web app security meta data for EJB21 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public class SecurityHandlerEJB21 implements SecurityHandler
{
   public void addSecurityDomain(JBossWebMetaData jbossWeb, Deployment dep)
   {
      EJBArchiveMetaData ejbMetaData = dep.getAttachment(EJBArchiveMetaData.class);
      if (ejbMetaData == null)
         throw new IllegalStateException("Cannot obtain application meta data");

      String securityDomain = ejbMetaData.getSecurityDomain();
      if (securityDomain != null)
      {
         if (securityDomain.startsWith("java:/jaas/") == false)
            securityDomain = "java:/jaas/" + securityDomain;

         jbossWeb.setSecurityDomain(securityDomain);
      }
   }

   public void addSecurityRoles(JBossWebMetaData webApp, Deployment dep)
   {
      JBossMetaData jbmd = dep.getAttachment(JBossMetaData.class);
      IAssemblyDescriptorMetaData assemblyDescriptor = jbmd.getAssemblyDescriptor();
      if (assemblyDescriptor != null)
      {
         SecurityRolesMetaData securityRoles = assemblyDescriptor.getSecurityRoles();
         if (securityRoles != null)
            webApp.setSecurityRoles(securityRoles);
      }
   }
}
