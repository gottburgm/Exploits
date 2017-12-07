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
package org.jboss.wsf.container.jboss50.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.AuthConstraintMetaData;
import org.jboss.metadata.web.spec.LoginConfigMetaData;
import org.jboss.metadata.web.spec.SecurityConstraintMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.jboss.metadata.web.spec.TransportGuaranteeType;
import org.jboss.metadata.web.spec.UserDataConstraintMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionsMetaData;
import org.jboss.wsf.spi.annotation.WebContext;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.WSFDeploymentException;
import org.jboss.wsf.spi.metadata.j2ee.EJBArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBMetaData;
import org.jboss.wsf.spi.metadata.j2ee.EJBSecurityMetaData;
import org.jboss.wsf.container.jboss50.deployment.tomcat.SecurityHandler;

/**
 * A deployment aspect that generates a webapp for an EJB endpoint 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 13-Oct-2007
 */
public class WebAppGeneratorDeploymentAspect extends DeploymentAspect
{
   private SecurityHandler securityHandlerEJB21;
   private SecurityHandler securityHandlerEJB3;

   public void setSecurityHandlerEJB21(SecurityHandler handler)
   {
      this.securityHandlerEJB21 = handler;
   }

   public void setSecurityHandlerEJB3(SecurityHandler handler)
   {
      this.securityHandlerEJB3 = handler;
   }

   @Override
   public void create(Deployment dep)
   {
      String typeStr = dep.getType().toString();
      if (typeStr.endsWith("EJB21"))
      {
         JBossWebMetaData jbwmd = generatWebDeployment((ArchiveDeployment)dep, securityHandlerEJB21);
         dep.addAttachment(JBossWebMetaData.class, jbwmd);
      }
      else if (typeStr.endsWith("EJB3"))
      {
         JBossWebMetaData jbwmd = generatWebDeployment((ArchiveDeployment)dep, securityHandlerEJB3);
         dep.addAttachment(JBossWebMetaData.class, jbwmd);
      }
      else
      {
         JBossWebMetaData jbwmd = generatWebDeployment((ArchiveDeployment)dep, null);
         dep.addAttachment(JBossWebMetaData.class, jbwmd);
      }
   }

   protected JBossWebMetaData generatWebDeployment(ArchiveDeployment dep, SecurityHandler securityHandler)
   {
      JBossWebMetaData jbwmd = new JBossWebMetaData();
      createWebAppDescriptor(dep, jbwmd, securityHandler);
      createJBossWebAppDescriptor(dep, jbwmd, securityHandler);
      return jbwmd;
   }

   protected void createWebAppDescriptor(Deployment dep, JBossWebMetaData jbwmd, SecurityHandler securityHandler)
   {
      /*
       <servlet>
       <servlet-name>
       <servlet-class>
       </servlet>
       */
      JBossServletsMetaData servlets = jbwmd.getServlets();
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         JBossServletMetaData servlet = new JBossServletMetaData();
         servlet.setServletName(ep.getShortName());
         servlet.setServletClass(ep.getTargetBeanName());
         servlets.add(servlet);
      }

      /*
       <servlet-mapping>
       <servlet-name>
       <url-pattern>
       </servlet-mapping>
       */
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         List<ServletMappingMetaData> servletMappings = jbwmd.getServletMappings();
         if (servletMappings == null)
         {
            servletMappings = new ArrayList<ServletMappingMetaData>();
            jbwmd.setServletMappings(servletMappings);
         }
         ServletMappingMetaData servletMapping = new ServletMappingMetaData();
         servletMapping.setServletName(ep.getShortName());
         servletMapping.setUrlPatterns(Arrays.asList(new String[] { ep.getURLPattern() }));
         servletMappings.add(servletMapping);
      }

      String authMethod = null;

      // Add web-app/security-constraint for each port component
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         String ejbName = ep.getShortName();

         Boolean secureWSDLAccess = null;
         String transportGuarantee = null;
         String beanAuthMethod = null;

         WebContext anWebContext = (WebContext)ep.getTargetBeanClass().getAnnotation(WebContext.class);
         if (anWebContext != null)
         {
            if (anWebContext.authMethod().length() > 0)
               beanAuthMethod = anWebContext.authMethod();
            if (anWebContext.transportGuarantee().length() > 0)
               transportGuarantee = anWebContext.transportGuarantee();
            if (anWebContext.secureWSDLAccess())
               secureWSDLAccess = anWebContext.secureWSDLAccess();
         }

         EJBArchiveMetaData appMetaData = dep.getAttachment(EJBArchiveMetaData.class);
         if (appMetaData != null && appMetaData.getBeanByEjbName(ejbName) != null)
         {
            EJBMetaData bmd = appMetaData.getBeanByEjbName(ejbName);
            EJBSecurityMetaData smd = bmd.getSecurityMetaData();
            if (smd != null)
            {
               beanAuthMethod = smd.getAuthMethod();
               transportGuarantee = smd.getTransportGuarantee();
               secureWSDLAccess = smd.getSecureWSDLAccess();
            }
         }

         if (beanAuthMethod != null || transportGuarantee != null)
         {
            /*
             <security-constraint>
             <web-resource-collection>
             <web-resource-name>TestUnAuthPort</web-resource-name>
             <url-pattern>/HSTestRoot/TestUnAuth/*</url-pattern>
             </web-resource-collection>
             <auth-constraint>
             <role-name>*</role-name>
             </auth-constraint>
             <user-data-constraint>
             <transport-guarantee>NONE</transport-guarantee>
             </user-data-constraint>
             </security-constraint>
             */
            List<SecurityConstraintMetaData> securityContraints = jbwmd.getSecurityContraints();
            if (securityContraints == null)
            {
               securityContraints = new ArrayList<SecurityConstraintMetaData>();
               jbwmd.setSecurityContraints(securityContraints);
            }
            SecurityConstraintMetaData securityConstraint = new SecurityConstraintMetaData();
            securityContraints.add(securityConstraint);
            
            WebResourceCollectionsMetaData resourceCollections = securityConstraint.getResourceCollections();
            if (resourceCollections == null)
            {
               resourceCollections = new WebResourceCollectionsMetaData();
               securityConstraint.setResourceCollections(resourceCollections);
            }
            WebResourceCollectionMetaData resourceCollection = new WebResourceCollectionMetaData();
            resourceCollections.add(resourceCollection);
            
            resourceCollection.setWebResourceName(ejbName);
            resourceCollection.setUrlPatterns(Arrays.asList(new String[] { ep.getURLPattern() }));
            ArrayList<String> httpMethods = new ArrayList<String>();
            resourceCollection.setHttpMethods(httpMethods);
            if (Boolean.TRUE.equals(secureWSDLAccess))
            {
               httpMethods.add("GET");
            }
            httpMethods.add("POST");

            // Optional auth-constraint
            if (beanAuthMethod != null)
            {
               // Only the first auth-method gives the war login-config/auth-method
               if (authMethod == null)
                  authMethod = beanAuthMethod;

               AuthConstraintMetaData authConstraint = new AuthConstraintMetaData();
               authConstraint.setRoleNames(Arrays.asList(new String[] { "*" }));
               securityConstraint.setAuthConstraint(authConstraint);
            }
            // Optional user-data-constraint
            if (transportGuarantee != null)
            {
               UserDataConstraintMetaData userDataConstraint = new UserDataConstraintMetaData();
               userDataConstraint.setTransportGuarantee(TransportGuaranteeType.valueOf(transportGuarantee));
               securityConstraint.setUserDataConstraint(userDataConstraint);
            }
         }
      }

      // Optional login-config/auth-method
      if (authMethod != null && securityHandler != null)
      {
         LoginConfigMetaData loginConfig = jbwmd.getLoginConfig();
         if (loginConfig == null)
         {
            loginConfig = new LoginConfigMetaData();
            jbwmd.setLoginConfig(loginConfig);
         }
         loginConfig.setAuthMethod(authMethod);
         loginConfig.setRealmName("EJBServiceEndpointServlet Realm");

         securityHandler.addSecurityRoles(jbwmd, dep);
      }
   }

   protected void createJBossWebAppDescriptor(Deployment dep, JBossWebMetaData jbwmd, SecurityHandler securityHandler)
   {
      /* Create a jboss-web
       <jboss-web>
       <security-domain>java:/jaas/cts</security-domain>
       <virtual-host>some.domain.com</virtual-host>
       </jboss-web>
       */
      if (securityHandler != null)
         securityHandler.addSecurityDomain(jbwmd, dep);

      String[] virtualHosts = dep.getService().getVirtualHosts();
      if (virtualHosts != null && virtualHosts.length > 0)
      {
         jbwmd.setVirtualHosts(Arrays.asList(virtualHosts));
      }
   }
}
