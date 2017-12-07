/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.refs.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.jboss.deployment.dependency.ContainerDependencyMetaData;
import org.jboss.deployment.plugin.MappedDeploymentEndpointResolver;
import org.jboss.deployment.spi.EndpointInfo;
import org.jboss.deployment.spi.EndpointType;
import org.jboss.test.refs.common.EjbLinkIF;

/**
 * Tests of the MappedDeploymentEndpointResolver
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class MappedResolverUnitTestCase extends TestCase
{
   private Map<String, ContainerDependencyMetaData> endpointMap
      = new HashMap<String, ContainerDependencyMetaData>();
   private Map<String, String> endpointAlternateMap
   = new HashMap<String, String>();

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }
   
   public void testEjbRefs()
   {
      String deploymentPath = "ejb1.jar";
      MappedDeploymentEndpointResolver resolver = new MappedDeploymentEndpointResolver(
            endpointMap, endpointAlternateMap, deploymentPath);
      // Add ejb mappings to ejb1.jar
      endpointMap.put("ejb/ejb1.jar#Ejb1InJar1", new ContainerDependencyMetaData("Ejb1InJar1", "Ejb1InJar1", "ejb1.jar"));
      endpointMap.put("ejb/ejb1.jar#Ejb2InJar1", new ContainerDependencyMetaData("Ejb2InJar1", "Ejb2InJar1", "ejb1.jar"));
      // the alternate mapping (See MappedReferenceMetaDataResolverDeployer.mapEjbs)
      endpointAlternateMap.put("ejb/Ejb1InJar1", "ejb/ejb1.jar#Ejb1InJar1");
      endpointAlternateMap.put("ejb/Ejb2InJar1", "ejb/ejb1.jar#Ejb2InJar1");
      // A business-local mapping for Ejb1InJar1
      endpointAlternateMap.put("ejb/ejb1.jar@"+EjbLinkIF.class.getName(), "ejb/ejb1.jar#Ejb1InJar1");
      EndpointInfo info = resolver.getEndpointInfo("Ejb1InJar1", EndpointType.EJB, "ejb1.jar");
      assertNotNull(info);
      assertEquals("Ejb1InJar1", info.getName());
      assertEquals("ejb1.jar", info.getPathName());

      info = resolver.getEndpointInfo("../ejb1.jar#Ejb2InJar1", EndpointType.EJB, "ejb2.jar");
      assertNotNull(info);
      assertEquals("Ejb2InJar1", info.getName());
      assertEquals("ejb1.jar", info.getPathName());

      // 
      info = resolver.getEndpointInfo(EjbLinkIF.class, EndpointType.EJB, "ejb1.jar");
      assertNotNull(info);
      assertEquals("Ejb1InJar1", info.getName());
      assertEquals("ejb1.jar", info.getPathName());
      
      // Test finding a bean with only a bean name
      info = resolver.getEndpointInfo("Ejb2InJar1", EndpointType.EJB, "ejb2.jar");
      assertNotNull(info);
      assertEquals("Ejb2InJar1", info.getName());
      assertEquals("ejb1.jar", info.getPathName());
   }
}
