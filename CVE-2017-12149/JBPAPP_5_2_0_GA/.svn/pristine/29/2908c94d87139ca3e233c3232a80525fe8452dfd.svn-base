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
package org.jboss.wsf.container.jboss50.deployer;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;
import javax.naming.Context;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb.deployers.EjbDeployment;
import org.jboss.ejb.deployers.MergedJBossMetaDataDeployer;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.javaee.JavaEEComponentHelper;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.wsf.spi.deployment.integration.WebServiceDeclaration;
import org.jboss.wsf.spi.deployment.integration.WebServiceDeployment;

/**
 * This it the web service deployer for EJB. Adopts EJB deployments to
 * {@link org.jboss.wsf.spi.deployment.integration.WebServiceDeclaration} an passes it to a chain of
 * {@link org.jboss.wsf.container.jboss50.deployer.DeployerHook}'s.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.com
 *
 * @since 24-Apr-2007
 */
public class WebServiceDeployerEJB extends AbstractWebServiceDeployer
{
   private static final Logger log = Logger.getLogger(WebServiceDeployerEJB.class);

   public WebServiceDeployerEJB()
   {
      addInput(MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME);   
   
      addInput(EjbDeployment.class);
      
      addInput(Ejb3Deployment.class);

      // Input for the TomcatDeployer
      addOutput(JBossWebMetaData.class);
      
      addOutput(WebServiceDeployment.class);

   }

   @Override
   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      JBossMetaData beans = (JBossMetaData)unit.getAttachment(
        MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME
      );
      Ejb3Deployment ejb3Deployment = unit.getAttachment(Ejb3Deployment.class);
      
      if(beans!=null)
      {
         WebServiceDeploymentAdapter wsDeployment = new WebServiceDeploymentAdapter();   
         
         Iterator<JBossEnterpriseBeanMetaData> iterator = beans.getEnterpriseBeans().iterator();
         while(iterator.hasNext())
         {
            JBossEnterpriseBeanMetaData ejb = iterator.next();
            EJBContainer ejbContainer = null;
            if (ejb3Deployment != null && !ejb.isEntity())
            {
               ObjectName objName = null;
               try
               {
                  objName = new ObjectName(ejb.determineContainerName());
               }
               catch (Exception e)
               {
                  throw new DeploymentException(e);
               }
               ejbContainer = (EJBContainer)ejb3Deployment.getContainer(objName);
            }
            if(ejb.getEjbClass()!=null)
            	wsDeployment.getEndpoints().add( new WebServiceDeclarationAdapter(ejb, ejbContainer, unit.getClassLoader()) );
            else
               log.warn("Ingore ejb deployment with null classname: " + ejb);
         }

         unit.addAttachment(WebServiceDeployment.class, wsDeployment);

         super.internalDeploy(unit);
      }
   }

   @Override
   public void internalUndeploy(DeploymentUnit unit)
   {
      super.internalUndeploy(unit);
   }

   /**
    * Adopts EJB3 bean meta data to a {@link org.jboss.wsf.spi.deployment.integration.WebServiceDeclaration}
    */
   private class WebServiceDeclarationAdapter implements WebServiceDeclaration
   {

      private JBossEnterpriseBeanMetaData ejbMetaData;
      private EJBContainer ejbContainer;
      private ClassLoader loader;      

      public WebServiceDeclarationAdapter(JBossEnterpriseBeanMetaData ejbMetaData, EJBContainer ejbContainer, ClassLoader loader)
      {
         this.ejbMetaData = ejbMetaData;
         this.ejbContainer = ejbContainer;
         this.loader = loader;
      }

      public String getContainerName()
      {
         return ejbMetaData.determineContainerName();
      }
      
      public Context getContext()
      {
         return ejbContainer.getEnc();
      }

      public String getComponentName()
      {
         return ejbMetaData.getName();
      }

      public String getComponentClassName()
      {
         return ejbMetaData.getEjbClass();
      }

      public <T extends Annotation> T getAnnotation(Class<T> annotation)
      {
         T result = ejbContainer != null ? ejbContainer.getAnnotation(annotation) : null;
         if (result == null)
         {
            Class bean = getComponentClass();
            if(bean.isAnnotationPresent(annotation))
            {
               result = (T)bean.getAnnotation(annotation);
            }
         }
         return result;
      }

      private Class getComponentClass()
      {
         try
         {
            return loader.loadClass(getComponentClassName());
         } catch (ClassNotFoundException e)
         {
            throw new RuntimeException("Failed to load component class "+ getComponentClassName()+". Loader:" + this.loader);
         }
      }
   }

   /**
    * Adopts an EJB deployment to a {@link org.jboss.wsf.spi.deployment.integration.WebServiceDeployment} 
    */
   private class WebServiceDeploymentAdapter implements WebServiceDeployment
   {
      private List<WebServiceDeclaration> endpoints = new ArrayList<WebServiceDeclaration>();

      public List<WebServiceDeclaration> getServiceEndpoints()
      {
         return endpoints;  
      }

      public List<WebServiceDeclaration> getEndpoints()
      {
         return endpoints;
      }
   }
}
