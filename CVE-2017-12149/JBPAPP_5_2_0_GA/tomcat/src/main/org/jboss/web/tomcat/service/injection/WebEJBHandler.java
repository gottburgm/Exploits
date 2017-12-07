/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.web.tomcat.service.injection;

import java.util.Collection;
import java.util.Map;

import org.jboss.deployment.dependency.ContainerDependencyMetaData;
import org.jboss.deployment.spi.DeploymentEndpointResolver;
import org.jboss.injection.InjectionContainer;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferenceMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/**
 * Process all ejb references. The non local references are processed
 * by inheritance. Forked to allow for web specific processing.
 *
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class WebEJBHandler<X extends Environment> extends WebEJBRemoteHandler<X>
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(WebEJBHandler.class);

   public WebEJBHandler(JBossWebMetaData webDD,
         DeploymentEndpointResolver resolver,
         Map<String, ContainerDependencyMetaData> endpoints,
         String vfsContext)
   {
      super(webDD, resolver, endpoints, vfsContext);
   }

   public void loadXml(X xml, InjectionContainer container)
   {
      super.loadXml(xml, container);
      if (xml != null)
      {
         // local references
         log.trace("localEjbRefs:" + xml.getEjbLocalReferences());
         if (xml.getEjbLocalReferences() != null)
            loadEjbLocalXml(xml.getEjbLocalReferences(), container);
         // annotated references
         log.trace("annotatedEjbRefs:" + xml.getEjbLocalReferences());
         if (xml.getAnnotatedEjbReferences() != null)
            loadEjbRefXml(xml.getAnnotatedEjbReferences(), container);
      }
   }

   protected void loadEjbLocalXml(Collection<EJBLocalReferenceMetaData> refs, InjectionContainer container)
   {
      for (EJBLocalReferenceMetaData ref : refs)
      {
         String interfaceName = ref.getLocal();
         String errorType = "<ejb-local-ref>";

         ejbRefXml(ref, interfaceName, container, errorType);
      }
   }
   
   protected void loadEjbRefXml(AnnotatedEJBReferencesMetaData refs, InjectionContainer container)
   {
      for (AnnotatedEJBReferenceMetaData ref : refs)
      {
         String interfaceName = ref.getBeanInterface().getName();
         String errorType = "@EJB";

         ejbRefXml(ref, interfaceName, container, errorType);
      }      
   }
}
