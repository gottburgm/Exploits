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
package org.jboss.deployment.plugin;

import java.util.Map;

import org.jboss.deployment.dependency.ContainerDependencyMetaData;
import org.jboss.deployment.spi.DeploymentEndpointResolver;
import org.jboss.deployment.spi.EndpointInfo;
import org.jboss.deployment.spi.EndpointType;
import org.jboss.logging.Logger;

/**
 * A DeploymentEndpointResolver implementation that relies on the endpoint
 * map produced by the MappedReferenceMetaDataResolverDeployer
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class MappedDeploymentEndpointResolver implements
      DeploymentEndpointResolver
{
   private static Logger log = Logger.getLogger(MappedDeploymentEndpointResolver.class);
   /** The deployment wide reference map */
   private Map<String, ContainerDependencyMetaData> endpointMap;
   private Map<String, String> endpointAlternateMap;

   public MappedDeploymentEndpointResolver(Map<String, ContainerDependencyMetaData> endpointMap,
         Map<String, String> endpointAlternateMap,
         String unitPath)
   {
      this.endpointMap = endpointMap;
      this.endpointAlternateMap = endpointAlternateMap;
   }

   /**
    * @param businessIntf
    * @param type - 
    * @param vfsContext - The path of the unit this resolver is associated with. Used as the
    * starting point for link resolution.
    * 
    */
   public EndpointInfo getEndpointInfo(Class businessIntf, String type, String vfsContext)
   {
      // First look for a unit specific mapping
      String altKey = "ejb/" + vfsContext + "@" + businessIntf.getName();
      String key = this.endpointAlternateMap.get(altKey);
      if(key == null)
      {
         // Look for a top level binding
         altKey = "ejb@"  + businessIntf.getName();
         key = this.endpointAlternateMap.get(altKey);
      }
      EndpointInfo info = null;
      if(key != null)
      {
         ContainerDependencyMetaData cdmd = endpointMap.get(key);
         info = new EndpointInfo(cdmd.getDeploymentPath(), cdmd.getComponentName(), type);
      }
      return info;
   }

   /* 

    * @see org.jboss.deployment.spi.DeploymentEndpointResolver#getEndpointInfo(java.lang.String, org.jboss.deployment.spi.EndpointType)
    * @see EndpointType
    */
   public EndpointInfo getEndpointInfo(String ref, String type, String vfsContext)
   {
      String prefix = type;
      // Parse the ref to obtain the path and endpoint name
      String unitPath = vfsContext;
      String endpointName = ref;
      if (ref.indexOf('#') != -1)
      {
         // <ejb-link> is of the form relative-path/file.jar#Bean
         String path = ref.substring(0, ref.indexOf('#'));
         // resolve any ../* prefix
         if(path.startsWith("../"))
         {
            String[] deploymentPaths = unitPath.split("/");
            int count = 0;
            while(path.startsWith("../"))
            {
               path = path.substring(3);
               count ++;
            }
            // build the relative path from the root
            String rootPath = "";
            for(int n = 0; n < (deploymentPaths.length - count); n ++)
               rootPath += deploymentPaths + "/";
            unitPath = rootPath + path;
         }
         else
         {
            unitPath = path;
         }
         // 
         // Get the endpoint name
         endpointName = ref.substring(ref.indexOf('#') + 1);
      }

      EndpointInfo info = null;
      String key = prefix + "/" + unitPath +"#" + endpointName;
      ContainerDependencyMetaData cdmd = endpointMap.get(key);
      if(cdmd != null)
      {
         info = new EndpointInfo(unitPath, endpointName, type);
         return info;
      }
      
      // It could not be found in the unit itself, let's search globally
      if(ref.indexOf('#') == -1)
      {
         // See MappedReferenceMetaDataResolverDeployer.mapEjbs
         if(type.equals(EndpointType.EJB))
         {
            key = "ejb/" + ref;
         }
         else if(type.equals(EndpointType.MessageDestination))
         {
            key = "message-destination/" + ref;            
         }

         String ejbCompID = endpointAlternateMap.get(key);
         if(ejbCompID != null)
         {
            cdmd = endpointMap.get(ejbCompID);
            if(cdmd == null)
               throw new IllegalStateException("endpoint mapping is corrupt, can't find '" + ejbCompID + "' in " + endpointMap);
            info = new EndpointInfo(cdmd.getDeploymentPath(), cdmd.getComponentName(), type);
            return info;
         }
      }
      else
      {
         log.debug("Failed to find mapping for ref: "+ref+" path: "+vfsContext);
         if(log.isTraceEnabled())
            log.trace("Available keys: "+endpointMap.keySet());
      }
      return info;
   }

}
