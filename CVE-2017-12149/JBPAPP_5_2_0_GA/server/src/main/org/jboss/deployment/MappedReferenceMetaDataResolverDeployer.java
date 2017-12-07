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
package org.jboss.deployment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.dependency.ContainerDependencyMetaData;
import org.jboss.deployment.dependency.JndiDependencyMetaData;
import org.jboss.deployment.plugin.MappedDeploymentEndpointResolver;
import org.jboss.deployment.spi.DeploymentEndpointResolver;
import org.jboss.deployment.spi.EndpointInfo;
import org.jboss.deployment.spi.EndpointType;
import org.jboss.logging.Logger;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossEntityBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossGenericBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.DeploymentSummary;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.EjbDeploymentSummary;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.KnownInterfaces;
import org.jboss.metadata.ejb.jboss.jndipolicy.spi.PackagingType;
import org.jboss.metadata.ejb.spec.BusinessLocalsMetaData;
import org.jboss.metadata.ejb.spec.BusinessRemotesMetaData;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferencesMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.MessageDestinationMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferencesMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/**
 * A deployer which resolves references for parsing deployers using
 * a deployment map of all endpoints.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class MappedReferenceMetaDataResolverDeployer extends AbstractRealDeployer
{
   /** Key for Map<String, ContainerDependencyMetaData> attachment */
   public static final String ENDPOINT_MAP_KEY = "MappedReferenceMetaDataResolverDeployer.endpointMap";
   /** Key for Set<ContainerDependencyMetaData> attachment */
   public static final String DEPENDS_SET_KEY = "MappedReferenceMetaDataResolverDeployer.dependsSet";
   /** Key for Map<String, String> of alternate endpoint resolution to the endpointMap key */
   public static final String ALTERNATE_MAP_KEY = "MappedReferenceMetaDataResolverDeployer.endpointAlternateMap";
   private static Logger log = Logger.getLogger(MappedReferenceMetaDataResolverDeployer.class);
   /** */
   private boolean failOnUnresolvedRefs;

   public MappedReferenceMetaDataResolverDeployer()
   {
      setStage(DeploymentStages.POST_CLASSLOADER);
      HashSet<String> inputs = new HashSet<String>();
      inputs.add(JBossClientMetaData.class.getName());
      inputs.add(JBossMetaData.class.getName());
      inputs.add(JBossWebMetaData.class.getName());
      super.setInputs(inputs);
      super.setOutput(DeploymentEndpointResolver.class);
   }

   /**
    * We want to process the parent last
    */
   @Override
   public boolean isParentFirst()
   {
      return false;
   }

   public boolean isFailOnUnresolvedRefs()
   {
      return failOnUnresolvedRefs;
   }

   public void setFailOnUnresolvedRefs(boolean failOnUnresolvedRefs)
   {
      this.failOnUnresolvedRefs = failOnUnresolvedRefs;
   }

   /**
    * Look for ejb, web or client metadata to resolve references without
    * mapped names.
    */
   public void internalDeploy(DeploymentUnit unit)
      throws DeploymentException
   {
      JBossMetaData ejbMetaData = unit.getAttachment(JBossMetaData.class);
      JBossWebMetaData webMetaData = unit.getAttachment(JBossWebMetaData.class);
      JBossClientMetaData clientMetaData = unit.getAttachment(JBossClientMetaData.class);
      if(ejbMetaData == null && webMetaData == null && clientMetaData == null)
         return;

      // Create a map of the reference endpoints if it does not exist in the top unit
      DeploymentUnit top = unit.getTopLevel();
      Map<String, ContainerDependencyMetaData> endpointMap = top.getAttachment(ENDPOINT_MAP_KEY, Map.class);
      Map<String, String> endpointAlternateMap = top.getAttachment(ALTERNATE_MAP_KEY, Map.class);
      if(endpointMap == null)
      {
         endpointMap = new ConcurrentHashMap<String, ContainerDependencyMetaData>();
         endpointAlternateMap = new ConcurrentHashMap<String, String>();
         mapEndpoints(top, endpointMap, endpointAlternateMap);
         top.addAttachment(ENDPOINT_MAP_KEY, endpointMap, Map.class);
         top.addAttachment(ALTERNATE_MAP_KEY, endpointAlternateMap);
         DeploymentEndpointResolver resolver = new MappedDeploymentEndpointResolver(endpointMap, endpointAlternateMap, unit.getRelativePath());
         top.addAttachment(DeploymentEndpointResolver.class, resolver);
      }

      DeploymentEndpointResolver resolver = new MappedDeploymentEndpointResolver(
            endpointMap, endpointAlternateMap, unit.getRelativePath());

      List<String> unresolvedPaths = new ArrayList<String>();
      if(ejbMetaData != null)
      {
         JBossEnterpriseBeansMetaData beans = ejbMetaData.getEnterpriseBeans();
         // Process ejb references
         try
         {
            resolve(unit, endpointMap, beans, resolver, unresolvedPaths);
         }
         catch (Exception e)
         {
            throw new DeploymentException(e);
         }
         if(unresolvedPaths.size() > 0)
            log.warn("Unresolved references exist in JBossMetaData:"+unresolvedPaths);
      }
      if(webMetaData != null)
      {
         // Process web app references
         ContainerDependencyMetaData webAppCDMD = new ContainerDependencyMetaData(unit.getSimpleName(), "web-app", unit.getRelativePath());
         try
         {
            resolve(webAppCDMD, unit, endpointMap, webMetaData.getJndiEnvironmentRefsGroup(), resolver, unresolvedPaths);
         }
         catch (Exception e)
         {
            throw new DeploymentException(e);
         }
         if(unresolvedPaths.size() > 0)
            log.warn("Unresolved references exist in JBossWebMetaData:"+unresolvedPaths);
      }
      if(clientMetaData != null)
      {
         // Process client app references
         ContainerDependencyMetaData clientCDMD = new ContainerDependencyMetaData(unit.getSimpleName(), "client", unit.getRelativePath());
         try
         {
            resolve(clientCDMD, unit, endpointMap, clientMetaData.getJndiEnvironmentRefsGroup(), resolver, unresolvedPaths);
         }
         catch (Exception e)
         {
            throw new DeploymentException(e);
         }
         if(unresolvedPaths.size() > 0)
            log.warn("Unresolved references exist in JBossClientMetaData: "+unresolvedPaths);
      }
      // Add the unique set of ContainerDependencyMetaData
      Set<ContainerDependencyMetaData> depends = new HashSet<ContainerDependencyMetaData>();
      for(ContainerDependencyMetaData cdmd : endpointMap.values())
      {
         depends.add(cdmd);
      }
      top.addAttachment(DEPENDS_SET_KEY, depends, Set.class);

      unit.addAttachment(DeploymentEndpointResolver.class, resolver);
      dump(unit);
   }

   /**
    * Map the ejb and message-destination endpoints.
    *
    * @param unit - the deployment top level unit to start the mapping from
    * @param endpointMap - the endpoint
    */
   protected void mapEndpoints(DeploymentUnit unit, Map<String, ContainerDependencyMetaData> endpointMap,
         Map<String, String> endpointAlternateMap)
   {
      boolean trace = log.isTraceEnabled();
      // First map the ejbs
      mapEjbs(unit, endpointMap, endpointAlternateMap, trace);
      // Map all sources of message-destinations
      mapMessageDestinations(unit, endpointMap, endpointAlternateMap, trace);
      // Map persistence units
      mapPersistenceUnits(unit, endpointMap, trace);
      // Display the endpoint map for debugging
      displayEndpoints(unit.getName(), endpointMap);
   }

   /**
    * @param unit
    * @param endpointMap
    * @param trace
    */
   protected void mapPersistenceUnits(DeploymentUnit unit,
         Map<String, ContainerDependencyMetaData> endpointMap, boolean trace)
   {
      // TODO Auto-generated method stub
      String vfsPath = unit.getRelativePath();
      //PersistenceUnitsMetaData ejbMetaData = unit.getAttachment(PersistenceUnitsMetaData.class);

   }

   protected void mapMessageDestinations(DeploymentUnit unit,
         Map<String, ContainerDependencyMetaData> endpointMap,
         Map<String, String> endpointAlternateMap,
         boolean trace)
   {
      String vfsPath = unit.getRelativePath();
      JBossMetaData ejbMetaData = unit.getAttachment(JBossMetaData.class);
      MessageDestinationsMetaData msgDestinations = null;
      ClassLoader loader = unit.getClassLoader();
      if(ejbMetaData != null)
      {
         msgDestinations = ejbMetaData.getAssemblyDescriptor().getMessageDestinations();
         mapMessageDestinations(vfsPath, msgDestinations, endpointMap, endpointAlternateMap, loader, trace);
      }
      JBossWebMetaData webMetaData = unit.getAttachment(JBossWebMetaData.class);
      if(webMetaData != null)
      {
         msgDestinations = webMetaData.getMessageDestinations();
         mapMessageDestinations(vfsPath, msgDestinations, endpointMap, endpointAlternateMap, loader, trace);
      }
      JBossClientMetaData clientMetaData = unit.getAttachment(JBossClientMetaData.class);
      if(clientMetaData != null)
      {
         msgDestinations = clientMetaData.getMessageDestinations();
         mapMessageDestinations(vfsPath, msgDestinations, endpointMap, endpointAlternateMap, loader, trace);
      }

      // Process children
      List<DeploymentUnit> children = unit.getChildren();
      if(children != null)
      {
         for(DeploymentUnit child : children)
            mapMessageDestinations(child, endpointMap, endpointAlternateMap, trace);
      }
   }

   protected void mapMessageDestinations(String vfsPath, MessageDestinationsMetaData msgDestinations,
         Map<String, ContainerDependencyMetaData> endpointMap,
         Map<String, String> endpointAlternateMap,
         ClassLoader loader, boolean trace)
   {
      if(msgDestinations == null || msgDestinations.size() == 0)
         return;
      String prefix = "message-destination/" + vfsPath;
      for(MessageDestinationMetaData dest : msgDestinations)
      {
         String mappedName = dest.getMappedName();
         String destName = dest.getMessageDestinationName();
         if(mappedName == null || mappedName.length() == 0)
         {
            log.debug("Message-destination: "+destName+" has no mappedName");
            continue;
         }
         String destPath = prefix + "#" + destName;
         ContainerDependencyMetaData destMD = new ContainerDependencyMetaData(mappedName, destName, vfsPath);
         endpointMap.put(destPath, destMD);
         if(trace)
            log.trace("mapMessageDestinations: "+destPath+", mappedName: "+mappedName);
         
         // Create global message-destination/destName alt-mappings
         String destKey = EndpointType.MessageDestination  + "/" + destName;
         if(endpointAlternateMap.containsKey(destKey) == false)
         {
            endpointAlternateMap.put(destKey, destPath);
            if(trace)
               log.trace("mapMessageDestinations, added alternate root message-destination: "+destPath);
         }
         else
         {
            log.debug("Duplicate root ejb-name: "+destKey+" from: "+destPath);
         }

     }
   }

   /**
    * Creates a mapping of the unit relative path + ejb-name to the bean mapped name.
    * For beans with business locals/remotes, an entry of the form:
    * unit relative path / interface name to the ejb-name
    *
    * @param unit
    * @param endpointMap
    */
   protected void mapEjbs(DeploymentUnit unit, Map<String, ContainerDependencyMetaData> endpointMap,
         Map<String, String> endpointAlternateMap, boolean trace)
   {
      JBossMetaData ejbMetaData = unit.getAttachment(JBossMetaData.class);
      JBossEnterpriseBeansMetaData beans = null;
      if(ejbMetaData != null)
      {
         if(ejbMetaData.getDeploymentSummary() == null)
         {
            DeploymentSummary deploymentSummary = getDeploymentSummary(unit);
            ejbMetaData.setDeploymentSummary(deploymentSummary);
         }
         beans = ejbMetaData.getEnterpriseBeans();
      }

      if(beans != null)
      {
         String vfsPath = unit.getRelativePath();
         String prefix = "ejb/" + vfsPath;
         for(JBossEnterpriseBeanMetaData bean : beans)
         {
            // The unique id for this ejb in the deployment
            String ejbCompID = prefix + "#" + bean.getEjbName();
            //LegacyEjb3JndiPolicy policy = new LegacyEjb3JndiPolicy();
            String jndiName = bean.determineJndiName();
            ContainerDependencyMetaData cdmd = new ContainerDependencyMetaData(jndiName, bean.getEjbName(), vfsPath);
            cdmd.setEjb3X(ejbMetaData.isEJB3x());
            // TODO, this is a mess that should be simply from the metadata
            //ClassLoader loader = unit.getClassLoader();
            EjbDeploymentSummary unitSummary = getEjbDeploymentSummary(unit, bean);
            cdmd.setUnitSummary(unitSummary);

            endpointMap.put(ejbCompID, cdmd);
            // debug info for the mappings each ejb has
            ArrayList<String> mappings = new ArrayList<String>();
            // Alternate mappings
            String ejbNameKey = "ejb/" + bean.getEjbName();
            if(endpointAlternateMap.containsKey(ejbNameKey) == false)
            {
               endpointAlternateMap.put(ejbNameKey, ejbCompID);
               mappings.add(ejbNameKey);
               if(trace)
                  log.trace("mapEjbs, added alternate root ejb-name: "+ejbNameKey);
            }
            else
            {
               log.debug("Duplicate root ejb-name: "+ejbNameKey+" from: "+ejbCompID);
            }
            // Create mappings based on the bean business interfaces
            if(bean instanceof JBossSessionBeanMetaData)
            {
               JBossSessionBeanMetaData sbean = (JBossSessionBeanMetaData) bean;
               String ejbName = sbean.getEjbName();
               // home
               if(sbean.getHome() != null && sbean.getHome().length() > 0)
               {
                  cdmd.addJndiName(sbean.determineResolvedJndiName(KnownInterfaces.HOME));
                  // Add ejb/vfsPath@iface
                  String home = sbean.getHome();
                  String ifacePath = prefix + "@" + home;
                  if(endpointAlternateMap.containsKey(ifacePath))
                     log.debug(ejbName+" duplicates home: "+home+", existing: "+endpointAlternateMap.get(ifacePath));
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local-home: "+ifacePath+", ejbName: "+ejbName);
                  }
                  // Add ejb/iface
                  ifacePath = "ejb@" + home;
                  if(endpointMap.containsKey(ifacePath))
                  {
                     // TODO: may need to track the duplicates to create an error
                     log.debug(ejbName+" duplicates home: "+home+", existing: "+endpointMap.get(ifacePath));
                  }
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, home: "+ifacePath+", ejbName: "+ejbName);
                  }
               }
               // remote
               if(sbean.getRemote() != null && sbean.getRemote().length() > 0)
               {
                  // Add ejb/vfsPath@iface
                  String remote = sbean.getRemote();
                  String remoteJndiName = sbean.determineJndiName();
                  cdmd.addJndiName(remoteJndiName);
                  String ifacePath = prefix + "@" + remote;
                  if(endpointAlternateMap.containsKey(ifacePath))
                     log.debug(ejbName+" duplicates remote: "+remote+", existing: "+endpointAlternateMap.get(ifacePath));
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, remote: "+ifacePath+", ejbName: "+ejbName);
                  }
                  // Add ejb/iface
                  ifacePath = "ejb@" + remote;
                  if(endpointMap.containsKey(ifacePath))
                  {
                     // TODO: may need to track the duplicates to create an error
                     log.debug(ejbName+" duplicates remote: "+remote+", existing: "+endpointMap.get(ifacePath));
                  }
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local: "+ifacePath+", ejbName: "+ejbName);
                  }
               }
               // local-home
               if(sbean.getLocalHome() != null && sbean.getLocalHome().length() > 0)
               {
                  cdmd.addJndiName(sbean.determineResolvedJndiName(KnownInterfaces.LOCAL_HOME));
                  // Add ejb/vfsPath@iface
                  String local = sbean.getLocalHome();
                  String ifacePath = prefix + "@" + local;
                  if(endpointAlternateMap.containsKey(ifacePath))
                     log.debug(ejbName+" duplicates local-home: "+local+", existing: "+endpointAlternateMap.get(ifacePath));
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local-home: "+ifacePath+", ejbName: "+ejbName);
                  }
                  // Add ejb/iface
                  ifacePath = "ejb@" + local;
                  if(endpointMap.containsKey(ifacePath))
                  {
                     // TODO: may need to track the duplicates to create an error
                     log.debug(ejbName+" duplicates local-home: "+local+", existing: "+endpointMap.get(ifacePath));
                  }
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local-home: "+ifacePath+", ejbName: "+ejbName);
                  }
               }
               // local
               if(sbean.getLocal() != null && sbean.getLocal().length() > 0)
               {
                  // Add ejb/vfsPath@iface
                  String local = sbean.getLocal();
                  String localJndiName = sbean.determineLocalJndiName();
                  cdmd.addJndiName(localJndiName);
                  String ifacePath = prefix + "@" + local;
                  if(endpointAlternateMap.containsKey(ifacePath))
                     log.debug(ejbName+" duplicates local: "+local+", existing: "+endpointAlternateMap.get(ifacePath));
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local: "+ifacePath+", ejbName: "+ejbName);
                  }
                  // Add ejb/iface
                  ifacePath = "ejb@" + local;
                  if(endpointMap.containsKey(ifacePath))
                  {
                     // TODO: may need to track the duplicates to create an error
                     log.debug(ejbName+" duplicates local: "+local+", existing: "+endpointMap.get(ifacePath));
                  }
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local: "+ifacePath+", ejbName: "+ejbName);
                  }
               }

               BusinessLocalsMetaData locals = sbean.getBusinessLocals();
               if(locals != null && locals.size() > 0)
               {
                  String localBaseJndiName = sbean.determineResolvedJndiName(KnownInterfaces.LOCAL);
                  cdmd.addJndiName(localBaseJndiName);
                  for(String local : locals)
                  {
                     // Add a targeted jndi name
                     String localJndiName = sbean.determineResolvedJndiName(local);
                     cdmd.addJndiName(localJndiName);
                     // Add ejb/vfsPath@iface
                     String ifacePath = prefix + "@" + local;
                     if(endpointAlternateMap.containsKey(ifacePath))
                        log.debug(ejbName+" duplicates business local: "+local+", existing: "+endpointAlternateMap.get(ifacePath));
                     else
                     {
                        endpointAlternateMap.put(ifacePath, ejbCompID);
                        mappings.add(ifacePath);
                        if(trace)
                           log.trace("mapEjbs, business-local: "+ifacePath+", ejbName: "+ejbName);
                     }
                     // Add ejb/iface
                     ifacePath = "ejb@" + local;
                     if(endpointMap.containsKey(ifacePath))
                     {
                        // TODO: may need to track the duplicates to create an error
                        log.debug(ejbName+" duplicates business-local: "+local+", existing: "+endpointMap.get(ifacePath));
                     }
                     else
                     {
                        endpointAlternateMap.put(ifacePath, ejbCompID);
                        mappings.add(ifacePath);
                        if(trace)
                           log.trace("mapEjbs, business-local: "+ifacePath+", ejbName: "+ejbName);
                     }
                  }
               }
               BusinessRemotesMetaData remotes = sbean.getBusinessRemotes();
               if(remotes != null && remotes.size() > 0)
               {
                  String remoteBaseJndiName = sbean.determineResolvedJndiName(KnownInterfaces.REMOTE);
                  cdmd.addJndiName(remoteBaseJndiName);
                  for(String remote : remotes)
                  {
                     // Add a targeted jndi name
                     String remoteJndiName = sbean.determineResolvedJndiName(remote);
                     cdmd.addJndiName(remoteJndiName);
                     // Add ejb/vfsPath/iface
                     String ifacePath = prefix + "@" + remote;
                     if(endpointAlternateMap.containsKey(ifacePath))
                        log.debug(ejbName+" duplicates business remote: "+remote+", existing: "+endpointAlternateMap.get(ifacePath));
                     else
                     {
                        endpointAlternateMap.put(ifacePath, ejbCompID);
                        mappings.add(ifacePath);
                        if(trace)
                           log.trace("mapEjbs, business-remote: "+ifacePath+", ejbName: "+ejbName);
                     }
                     // Add ejb/iface
                     ifacePath = "ejb@" + remote;
                     if(endpointMap.containsKey(ifacePath))
                        log.debug(ejbName+" duplicates business-remote: "+remote+", existing: "+endpointMap.get(ifacePath));
                     else
                     {
                        endpointAlternateMap.put(ifacePath, ejbCompID);
                        mappings.add(ifacePath);
                        if(trace)
                           log.trace("mapEjbs, business-remote: "+ifacePath+", ejbName: "+ejbName);
                     }
                  }
               }
               if(trace)
                  log.trace("mapEjbs: "+ejbCompID+", mappings: "+mappings);
            }
            else if(bean instanceof JBossEntityBeanMetaData)
            {
               JBossEntityBeanMetaData ebean = (JBossEntityBeanMetaData) bean;
               String ejbName = ebean.getEjbName();
               // home
               if(ebean.getHome() != null && ebean.getHome().length() > 0)
               {
                  cdmd.addJndiName(ebean.determineResolvedJndiName(KnownInterfaces.HOME));
                  // Add ejb/vfsPath@iface
                  String home = ebean.getHome();
                  String ifacePath = prefix + "@" + home;
                  if(endpointAlternateMap.containsKey(ifacePath))
                     log.debug(ejbName+" duplicates home: "+home+", existing: "+endpointAlternateMap.get(ifacePath));
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local-home: "+ifacePath+", ejbName: "+ejbName);
                  }
                  // Add ejb/iface
                  ifacePath = "ejb@" + home;
                  if(endpointMap.containsKey(ifacePath))
                  {
                     // TODO: may need to track the duplicates to create an error
                     log.debug(ejbName+" duplicates home: "+home+", existing: "+endpointMap.get(ifacePath));
                  }
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, home: "+ifacePath+", ejbName: "+ejbName);
                  }
               }
               // remote
               if(ebean.getRemote() != null && ebean.getRemote().length() > 0)
               {
                  // Add ejb/vfsPath@iface
                  String remote = ebean.getRemote();
                  String ifacePath = prefix + "@" + remote;
                  if(endpointAlternateMap.containsKey(ifacePath))
                     log.debug(ejbName+" duplicates remote: "+remote+", existing: "+endpointAlternateMap.get(ifacePath));
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, remote: "+ifacePath+", ejbName: "+ejbName);
                  }
                  // Add ejb/iface
                  ifacePath = "ejb@" + remote;
                  if(endpointMap.containsKey(ifacePath))
                  {
                     // TODO: may need to track the duplicates to create an error
                     log.debug(ejbName+" duplicates remote: "+remote+", existing: "+endpointMap.get(ifacePath));
                  }
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local: "+ifacePath+", ejbName: "+ejbName);
                  }
               }
               // local-home
               if(ebean.getLocalHome() != null && ebean.getLocalHome().length() > 0)
               {
                  cdmd.addJndiName(ebean.determineResolvedJndiName(KnownInterfaces.LOCAL_HOME));
                  // Add ejb/vfsPath@iface
                  String local = ebean.getLocalHome();
                  String ifacePath = prefix + "@" + local;
                  if(endpointAlternateMap.containsKey(ifacePath))
                     log.debug(ejbName+" duplicates local-home: "+local+", existing: "+endpointAlternateMap.get(ifacePath));
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local-home: "+ifacePath+", ejbName: "+ejbName);
                  }
                  // Add ejb/iface
                  ifacePath = "ejb@" + local;
                  if(endpointMap.containsKey(ifacePath))
                  {
                     // TODO: may need to track the duplicates to create an error
                     log.debug(ejbName+" duplicates local-home: "+local+", existing: "+endpointMap.get(ifacePath));
                  }
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local-home: "+ifacePath+", ejbName: "+ejbName);
                  }
               }
               // local
               if(ebean.getLocal() != null && ebean.getLocal().length() > 0)
               {
                  // Add ejb/vfsPath@iface
                  String local = ebean.getLocal();
                  String ifacePath = prefix + "@" + local;
                  if(endpointAlternateMap.containsKey(ifacePath))
                     log.debug(ejbName+" duplicates local: "+local+", existing: "+endpointAlternateMap.get(ifacePath));
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local: "+ifacePath+", ejbName: "+ejbName);
                  }
                  // Add ejb/iface
                  ifacePath = "ejb@" + local;
                  if(endpointMap.containsKey(ifacePath))
                  {
                     // TODO: may need to track the duplicates to create an error
                     log.debug(ejbName+" duplicates local: "+local+", existing: "+endpointMap.get(ifacePath));
                  }
                  else
                  {
                     endpointAlternateMap.put(ifacePath, ejbCompID);
                     mappings.add(ifacePath);
                     if(trace)
                        log.trace("mapEjbs, local: "+ifacePath+", ejbName: "+ejbName);
                  }
               }
            }
            else if(bean instanceof JBossGenericBeanMetaData)
            {
               log.warn("JBossGenericBeanMetaData seen for: "+bean.getEjbName());
            }
         }
      }

      // Process children
      List<DeploymentUnit> children = unit.getChildren();
      if(children != null)
      {
         for(DeploymentUnit child : children)
            mapEjbs(child, endpointMap, endpointAlternateMap, trace);
      }
   }

   /**
    * 
    * @param cdmd
    * @param unit
    * @param endpointMap
    * @param env
    * @param resolver
    * @param unresolvedRefs
    * @throws Exception
    */
   protected void resolve(ContainerDependencyMetaData cdmd, DeploymentUnit unit,
         Map<String, ContainerDependencyMetaData> endpointMap,
         Environment env,
         DeploymentEndpointResolver resolver,
         List<String> unresolvedRefs)
      throws Exception
   {
      if(env == null)
         return;

      AnnotatedEJBReferencesMetaData annotatedRefs = env.getAnnotatedEjbReferences();
      resolveEjbAnnotatedRefs(cdmd, unit, endpointMap, annotatedRefs, resolver, unresolvedRefs);
      EJBLocalReferencesMetaData localRefs = env.getEjbLocalReferences();
      resolveEjbLocalRefs(cdmd, unit, endpointMap, localRefs, resolver, unresolvedRefs);
      EJBReferencesMetaData ejbRefs = env.getEjbReferences();
      resolveEjbRefs(cdmd, unit, endpointMap, ejbRefs, resolver, unresolvedRefs);
      MessageDestinationReferencesMetaData msgRefs = env.getMessageDestinationReferences();
      resolveMsgRefs(cdmd, unit, endpointMap, msgRefs, resolver, unresolvedRefs);
      // TODO, other references
   }
   protected void resolve(DeploymentUnit unit, Map<String, ContainerDependencyMetaData> endpointMap,
         JBossEnterpriseBeansMetaData beans,
         DeploymentEndpointResolver resolver,
         List<String> unresolvedPaths)
      throws Exception
   {
      if(beans == null || beans.size() == 0)
         return;

      String vfsPath = unit.getRelativePath();
      for(JBossEnterpriseBeanMetaData bean : beans)
      {
         // Find the container dependency metadata
         String ejbCompID = "ejb/" + vfsPath + "#" + bean.getEjbName();
         ContainerDependencyMetaData cdmd = endpointMap.get(ejbCompID);
         if(cdmd == null)
            throw new IllegalStateException("Failed to find ContainerDependencyMetaData for: "+ejbCompID);
         Environment env = bean.getJndiEnvironmentRefsGroup();
         resolve(cdmd, unit, endpointMap, env, resolver,  unresolvedPaths);
      }
   }

   protected void resolveEjbAnnotatedRefs(ContainerDependencyMetaData cdmd, DeploymentUnit unit,
      Map<String, ContainerDependencyMetaData> endpointMap,
      AnnotatedEJBReferencesMetaData annotatedRefs,
      DeploymentEndpointResolver resolver,
      List<String> unresolvedRefs)
   {
      if(annotatedRefs == null)
         return;

      String vfsContext = unit.getRelativePath();
      for(AnnotatedEJBReferenceMetaData ref : annotatedRefs)
      {
         Class iface = ref.getBeanInterface();
         String link = ref.getLink();
         EndpointInfo info = null;
         if(link != null)
            info = resolver.getEndpointInfo(link, EndpointType.EJB, vfsContext);
         if(info == null && iface != null)
            info = resolver.getEndpointInfo(iface, EndpointType.EJB, vfsContext);

         if(info != null)
         {
            ContainerDependencyMetaData target = endpointMap.get(info.getComponentKey());
            if(target != null)
            {
               cdmd.addDependency(target);
               // Determine the jndi name for the reference interface
               String ifaceName = iface != null ? iface.getName() : null;
               
               //LegacyEjb3JndiPolicy policy = new LegacyEjb3JndiPolicy();
               String containerJndiName = target.getBeanMetaData().determineResolvedJndiName(ifaceName);               
               if(containerJndiName != null)
                  ref.setResolvedJndiName(containerJndiName);
            }
            else
            {
               unresolvedRefs.add(cdmd.getComponentID()+":"+ref);
            }
         }
         else
         {
            unresolvedRefs.add(cdmd.getComponentID()+":"+ref);
         }
      }
   }

   /**
    *
    * @param unit
    * @param localRefs
    * @param unresolvedRefs
    */
   protected void resolveEjbLocalRefs(ContainerDependencyMetaData cdmd, DeploymentUnit unit,
         Map<String, ContainerDependencyMetaData> endpointMap,
         EJBLocalReferencesMetaData localRefs,
         DeploymentEndpointResolver resolver,
         List<String> unresolvedRefs)
      throws Exception
   {
      if(localRefs == null)
         return;

      String vfsContext = unit.getRelativePath();
      ClassLoader loader = unit.getClassLoader();
      for(EJBLocalReferenceMetaData ref : localRefs)
      {
         if (ref.getIgnoreDependency() != null)
         {
            log.debug("IGNORING <ejb-ref> DEPENDENCY: " + ref);
            return;
         }

         String link = ref.getLink();
         String mappedName = ref.getMappedName();
         // Use mapped name first
         if(mappedName == null || mappedName.length() == 0)
         {
            ContainerDependencyMetaData target = null;
            if(link != null)
            {
               EndpointInfo info = resolver.getEndpointInfo(link, EndpointType.EJB, vfsContext);
               if(info != null)
               {
                  target = endpointMap.get(info.getComponentKey());
               }
               else
               {
                  /* A non-local link without a # jar target. This is allowed
                     for java ee clients so we have to search all ejb deployments.
                     First get the vfspaths of the ejb deploymens.
                  */
                  List<String> ejbPaths = getEjbDeploymentPaths(unit);
                  for(String path : ejbPaths)
                  {
                     EndpointInfo altInfo = resolver.getEndpointInfo(link, EndpointType.EJB, path);
                     if(altInfo != null)
                        target = endpointMap.get(altInfo.getComponentKey());
                     if(target != null)
                        break;
                  }
               }
            }
            if(target == null && ref.getLocal() != null)
            {
               // Try the local interface type
               target = resolveEjbInterface(ref.getLocal(), unit,
                     endpointMap, resolver);
            }
            if(target == null)
               unresolvedRefs.add(cdmd.getComponentID()+":"+ref);
            else
            {
               
               // Need to look at the local jndi name
               String localInterface = ref.getLocal();
               JBossEnterpriseBeanMetaData md = target.getBeanMetaData();
               
               /*
                * If for a Session bean we've got a reference to an EJB2.x
                * Local Component interface, stop processing because these
                * are not bound in JNDI (only accessible via LocalHome.create()
                */
               
               // Session EJB?
               boolean useDefaultProxy = false;
               if(md.isSession())
               {
                  // Cast
                  JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData)md;
                  
                  // Get the name of the Component Local Interface
                  String ejb2xLocalInterface = smd.getLocal();
                  
                  // If the ejb-ref is to a EJB2.x Local Component Interface
                  if(localInterface.equals(ejb2xLocalInterface))
                  {
                     // Use the default proxy
                     useDefaultProxy = true;
                  }
               }
                
               // Get ejb-jar Metadata
               JBossMetaData ejbJarMd = md.getEnterpriseBeansMetaData().getEjbJarMetaData();
               
               // Resolve a local JNDI Name based on Spec type
               String localJndiName = null;
               if (ejbJarMd.isEJB3x() && !useDefaultProxy)
               {
                  localJndiName = md.determineResolvedJndiName(localInterface);
               }
               else
               {
                  localJndiName = md.determineLocalJndiName();
               }

               // If we've got a resolved JNDI Name 
               if (localJndiName != null)
               {
                  // Set it and forget it! 
                  // http://en.wikipedia.org/wiki/Ron_Popeil
                  ref.setResolvedJndiName(localJndiName);
               }
               
               // Add the dependency
               cdmd.addDependency(target);
            }
         }
         else
         {
            // Create a JNDI dependency
            ref.setResolvedJndiName(mappedName);
            JndiDependencyMetaData jdmd = new JndiDependencyMetaData(mappedName, loader);
            cdmd.addJndiDependency(jdmd);
         }
      }

   }

   protected void resolveEjbRefs(ContainerDependencyMetaData cdmd, DeploymentUnit unit,
         Map<String, ContainerDependencyMetaData> endpointMap,
         EJBReferencesMetaData ejbRefs,
         DeploymentEndpointResolver resolver,
         List<String> unresolvedRefs)
      throws Exception
   {
      if(ejbRefs == null)
         return;

      String vfsContext = unit.getRelativePath();
      ClassLoader loader = unit.getClassLoader();
      for(EJBReferenceMetaData ref : ejbRefs)
      {
         if (ref.getIgnoreDependency() != null)
         {
            log.debug("IGNORING <ejb-ref> DEPENDENCY: " + ref);
            return;
         }

         String link = ref.getLink();
         String mappedName = ref.getMappedName();
         // Use mapped name first
         if(mappedName == null || mappedName.length() == 0)
         {
            ContainerDependencyMetaData target = null;
            if(link != null)
            {
               EndpointInfo info = resolver.getEndpointInfo(link, EndpointType.EJB, vfsContext);
               if(info != null)
               {
                  target = endpointMap.get(info.getComponentKey());
               }
               else
               {
                  /* A non-local link without a # jar target. This is allowed
                     for java ee clients so we have to search all ejb deployments.
                     First get the vfspaths of the ejb deploymens.
                  */
                  List<String> ejbPaths = getEjbDeploymentPaths(unit);
                  for(String path : ejbPaths)
                  {
                     EndpointInfo altInfo = resolver.getEndpointInfo(link, EndpointType.EJB, path);
                     if(altInfo != null)
                        target = endpointMap.get(altInfo.getComponentKey());
                     if(target != null)
                        break;
                  }
               }
               if(target == null)
               {
                  unresolvedRefs.add(cdmd.getComponentID()+":"+ref);
                  continue;
               }
            }
            if(target == null && ref.getRemote() != null)
            {
               // Try the local interface type
               target = resolveEjbInterface(ref.getRemote(), unit,
                     endpointMap, resolver);
            }

            if(target == null)
               unresolvedRefs.add(cdmd.getComponentID()+":"+ref);
            else
            {
               
               // Obtain remote interface name
               String remoteInterface = ref.getRemote();
               
               // Get Metadata
               JBossEnterpriseBeanMetaData md = target.getBeanMetaData();
               
               /*
                * If for a Session bean we've got a reference to an EJB2.x
                * Remote Component interface, stop processing because these
                * are not bound in JNDI (only accessible via Home.create()
                */
               
               // Session EJB?
               boolean useDefaultProxy = false;
               if(md.isSession())
               {
                  // Cast
                  JBossSessionBeanMetaData smd = (JBossSessionBeanMetaData)md;
                  
                  // Get the name of the Component Remote Interface
                  String ejb2xRemoteInterface = smd.getRemote();
                  
                  // If the ejb-ref is to a EJB2.x Remote Component Interface
                  if(remoteInterface.equals(ejb2xRemoteInterface))
                  {
                     // Use the default proxy
                     useDefaultProxy = true;
                  }
               }
               
               // Get ejb-jar metadata
               JBossMetaData ejbMarMd = md.getEnterpriseBeansMetaData().getEjbJarMetaData();
               
               // Resolve a JNDI name
               String containerJndiName = null;
               if (ejbMarMd.isEJB3x() && !useDefaultProxy)
               {
                  containerJndiName = md.determineResolvedJndiName(remoteInterface);
               }
               else
               {
                  containerJndiName = md.determineJndiName();
               }
               
               // If we've got a resolved name
               if(containerJndiName != null)
               {
                  // Set it
                  ref.setResolvedJndiName(containerJndiName);
               }
               
               // Add the dependency
               cdmd.addDependency(target);
            }
         }
         else
         {
            // Create a JNDI dependency
            ref.setResolvedJndiName(mappedName);
            JndiDependencyMetaData jdmd = new JndiDependencyMetaData(mappedName, loader);
            cdmd.addJndiDependency(jdmd);
         }
      }
   }

   protected String getInterface(EJBReferenceMetaData ref)
   {
      String iface = ref.getHome();
      if(iface == null)
         iface = ref.getRemote();
      return iface;
   }

   protected void resolveMsgRefs(ContainerDependencyMetaData cdmd, DeploymentUnit unit,
         Map<String, ContainerDependencyMetaData> endpointMap,
         MessageDestinationReferencesMetaData msgRefs,
         DeploymentEndpointResolver resolver,
         List<String> unresolvedRefs)
   {
      if(msgRefs == null)
         return;

      ClassLoader loader = unit.getClassLoader();
      String vfsContext = unit.getRelativePath();
      for(MessageDestinationReferenceMetaData ref : msgRefs)
      {
         String mappedName = ref.getMappedName();
         if(mappedName == null || mappedName.length() == 0)
         {
            String link = ref.getLink();
            ContainerDependencyMetaData target = null;
            if(link != null)
            {
               EndpointInfo info = resolver.getEndpointInfo(link, EndpointType.MessageDestination, vfsContext);
               if(info != null)
               {
                  target = endpointMap.get(info.getComponentKey());
               }
            }
            if(target == null)
               unresolvedRefs.add(cdmd.getComponentID()+":"+ref);
            else
            {
               cdmd.addDependency(target);
               String containerJndiName = target.getContainerName();
               if(containerJndiName != null)
                  ref.setResolvedJndiName(containerJndiName);
            }
         }
         else
         {
            // Create a JNDI dependency
            ref.setResolvedJndiName(mappedName);
            JndiDependencyMetaData jdmd = new JndiDependencyMetaData(mappedName, loader);
            cdmd.addJndiDependency(jdmd);
         }
      }
   }

   protected ContainerDependencyMetaData resolveEjbInterface(String iface,
         DeploymentUnit unit, Map<String, ContainerDependencyMetaData> endpointMap,
         DeploymentEndpointResolver resolver)
      throws Exception
   {
      ClassLoader loader = unit.getClassLoader();
      Class<?> ifaceClass = loader.loadClass(iface);
      String vfsContext = unit.getRelativePath();
      EndpointInfo info = resolver.getEndpointInfo(ifaceClass, EndpointType.EJB, vfsContext);
      if(info == null)
         throw new IllegalStateException("Failed to find ContainerDependencyMetaData for interface: "+ iface);
      ContainerDependencyMetaData cdmd = endpointMap.get(info.getComponentKey());
      return cdmd;
   }

   private List<String> getEjbDeploymentPaths(DeploymentUnit unit)
   {
      ArrayList<String> paths = new ArrayList<String>();
      DeploymentUnit root = unit.getTopLevel();
      getEjbDeploymentPaths(root, paths);
      return paths;
   }
   
   private void getEjbDeploymentPaths(DeploymentUnit unit,
         ArrayList<String> paths)
   {
      if(unit.isAttachmentPresent(JBossMetaData.class))
         paths.add(unit.getRelativePath());
      // Process children
      List<DeploymentUnit> children = unit.getChildren();
      if(children != null)
      {
         for(DeploymentUnit child : children)
            getEjbDeploymentPaths(child, paths);
      }
   }

   private static <T> void getAllAttachments(DeploymentUnit unit, Class<T> type, ArrayList<T> attachments)
   {
      T attachment = unit.getAttachment(type);
      if(attachment != null)
         attachments.add(attachment);
      List<DeploymentUnit> children = unit.getChildren();
      if(children != null)
         for(DeploymentUnit child : children)
            getAllAttachments(child, type, attachments);
   }

   /*
    * Unused
    */
//   private static String getJndiName(JBossEnterpriseBeanMetaData beanMD, boolean isLocal,
//         DeploymentUnit unit)
//   {
//      String jndiName = beanMD.getMappedName();
//      if (isLocal && jndiName == null)
//      {
//         // Validate that there is a local home associated with this bean
//         if(jndiName == null)
//            jndiName = beanMD.determineLocalJndiName();
//         if (jndiName == null)
//         {
//            log.warn("LocalHome jndi name requested for: '" +beanMD.getEjbName() + "' but there is no LocalHome class");
//         }
//      }
//      else if(jndiName == null)
//      {
//         // TODO, this is a mess that should be simply from the metadata
//         ClassLoader loader = unit.getClassLoader();
//         EjbDeploymentSummary unitSummary = getUnitSummary(unit, beanMD);
//         Class<? extends DefaultJndiBindingPolicy> defaultPolicyClass = null;
//         try
//         {
//            if(beanMD.getJBossMetaData().isEJB3x())
//            {
//               String policyClassName = "org.jboss.ejb3.jndipolicy.impl.PackagingBasedJndiBindingPolicy";
//               defaultPolicyClass = (Class<? extends DefaultJndiBindingPolicy>) loader.loadClass(policyClassName);
//            }
//            else if(beanMD.isSession())
//               defaultPolicyClass = SessionJndiBindingPolicy.class;
//            DefaultJndiBindingPolicy policy = beanMD.createPolicy(loader, defaultPolicyClass);
//            // Run through the options for which jndi name
//            if(beanMD.isSession())
//            {
//               JBossSessionBeanMetaData sbeanMD = (JBossSessionBeanMetaData) beanMD;
//               jndiName = sbeanMD.getJndiName();
//               if(jndiName == null)
//               {
//                  if(sbeanMD.getBusinessRemotes() != null && sbeanMD.getBusinessRemotes().size() > 0)
//                     jndiName = policy.getDefaultRemoteJndiName(unitSummary);
//                  else if(sbeanMD.getHome() != null)
//                     jndiName = policy.getDefaultRemoteHomeJndiName(unitSummary);
//                  else if(sbeanMD.getRemote() != null)
//                     jndiName = policy.getDefaultRemoteJndiName(unitSummary);
//                  else if(sbeanMD.getLocalHome() != null)
//                     jndiName = policy.getDefaultLocalHomeJndiName(unitSummary);
//                  else if(sbeanMD.getLocal() != null)
//                     jndiName = policy.getDefaultLocalJndiName(unitSummary);
//                  else
//                     jndiName = policy.getJndiName(unitSummary);
//               }
//            }
//            else if(policy != null)
//            {
//               jndiName = policy.getJndiName(unitSummary);
//            }
//            else if(beanMD.isEntity())
//            {
//               JBossEntityBeanMetaData ebeanMD = (JBossEntityBeanMetaData) beanMD;
//               jndiName = ebeanMD.determineJndiName();
//            }
//         }
//         catch(Exception e)
//         {
//            log.warn("Failed to obtain jndi name for bean: "+beanMD.getEjbName(), e);
//            jndiName = beanMD.getEjbName();
//         }
//      }
//      return jndiName;
//   }

   private static EjbDeploymentSummary getEjbDeploymentSummary(DeploymentUnit unit, JBossEnterpriseBeanMetaData beanMD)
   {
      DeploymentSummary dSummary = getDeploymentSummary(unit);
      
      EjbDeploymentSummary summary = new EjbDeploymentSummary(beanMD,dSummary);
      return summary;
   }
   
   private static DeploymentSummary getDeploymentSummary(DeploymentUnit unit)
   {
      DeploymentSummary dSummary = new DeploymentSummary();
      dSummary.setDeploymentName(unit.getSimpleName());
      String baseName = unit.getTopLevel().getSimpleName();
      int dot = baseName.lastIndexOf('.');
      if(dot > 0)
      {
         baseName = baseName.substring(0, dot);
      }
      dSummary.setDeploymentScopeBaseName(baseName);
      
      /*
       * Determine the packaging type (JAR or EAR, Standalone File not 
       * supported by this deployer) 
       */
      
      // Initialize to JAR
      PackagingType packagingType = PackagingType.JAR;
      
      // Determine if EAR
      boolean isEar = unit != unit.getTopLevel();
      if(isEar)
      {
         packagingType = PackagingType.EAR;
      }
      
      // Set type
      dSummary.setPackagingType(packagingType);
      
      // Return
      return dSummary;
   }
   
   private void dump(DeploymentUnit unit)
   {
      DeploymentUnit top = unit.getTopLevel();
      StringBuffer tmp = new StringBuffer();
      dump(top, tmp, 0);
      log.debug("Processing unit="+unit.getSimpleName()+", structure:\n"+tmp);
   }
   private void dump(DeploymentUnit unit, StringBuffer tmp, int depth)
   {
      for(int n = 0; n < depth; n ++)
         tmp.append('+');
      if(depth == 0)
         tmp.append(unit.getSimpleName());
      else
         tmp.append(unit.getRelativePath());
      tmp.append('\n');
      JBossMetaData metaData = unit.getAttachment(JBossMetaData.class);
      if(metaData != null)
      {
         JBossEnterpriseBeansMetaData beans = metaData.getEnterpriseBeans();
         if(beans != null)
         {
            for(JBossEnterpriseBeanMetaData bean : beans)
            {
               for(int n = 0; n < depth+1; n ++)
                  tmp.append('+');
               tmp.append("EjbEndpoint:ejbName=");
               tmp.append(bean.getEjbName());
               if(bean.getEjbClass() != null)
               {
                  tmp.append(",ejbClass=");
                  tmp.append(bean.getEjbClass());
               }
               if(bean instanceof JBossSessionBeanMetaData)
               {
                  JBossSessionBeanMetaData sbean = (JBossSessionBeanMetaData) bean;
                  if(sbean.getHome() != null)
                  {
                     tmp.append(",home=");
                     tmp.append(sbean.getHome());
                  }
                  if(sbean.getRemote() != null)
                  {
                     tmp.append(",remote=");
                     tmp.append(sbean.getRemote());
                  }
                  BusinessLocalsMetaData locals = sbean.getBusinessLocals();
                  if(locals != null)
                  {
                     tmp.append(",BusinessLocals: ");
                     tmp.append(locals);
                  }
                  BusinessRemotesMetaData remotes = sbean.getBusinessRemotes();
                  if(remotes != null)
                  {
                     tmp.append(",BusinessRemotes: ");
                     tmp.append(remotes);
                  }
                  dumpEnv(bean.getJndiEnvironmentRefsGroup(), tmp, depth+1);
               }
               tmp.append('\n');
            }
         }
      }
      else if(unit.isAttachmentPresent(JBossWebMetaData.class))
      {
         JBossWebMetaData webMD = unit.getAttachment(JBossWebMetaData.class);
         dumpEnv(webMD.getJndiEnvironmentRefsGroup(), tmp, depth++);        
      }
      else if(unit.isAttachmentPresent(JBossClientMetaData.class))
      {
         JBossClientMetaData clientMD = unit.getAttachment(JBossClientMetaData.class);
         dumpEnv(clientMD.getJndiEnvironmentRefsGroup(), tmp, depth++);
      }
      tmp.append('\n');
      List<DeploymentUnit> children = unit.getChildren();
      if(children != null)
      {
         for(DeploymentUnit child : children)
            dump(child, tmp, depth+1);
      }
   }
   private void dumpEnv(Environment env, StringBuffer tmp, int depth)
   {
      if(env == null)
         return;

      AnnotatedEJBReferencesMetaData annotatedRefs = env.getAnnotatedEjbReferences();
      if(annotatedRefs != null)
      {
         for(AnnotatedEJBReferenceMetaData ref : annotatedRefs)
         {
            for(int n = 0; n < depth+1; n ++)
               tmp.append('+');
            tmp.append("@EJB(");
            tmp.append(ref.getEjbRefName());
            tmp.append(") -> mappedName=");
            tmp.append(ref.getMappedName());
            tmp.append("| resolvedJndiName=");
            tmp.append(ref.getResolvedJndiName());
            tmp.append('\n');
         }
      }
      EJBLocalReferencesMetaData localRefs = env.getEjbLocalReferences();
      if(localRefs != null)
      {
         for(EJBLocalReferenceMetaData ref : localRefs)
         {
            for(int n = 0; n < depth+1; n ++)
               tmp.append('+');
            tmp.append("ejb-local-ref(");
            tmp.append(ref.getEjbRefName());
            tmp.append(") -> mappedName=");
            tmp.append(ref.getMappedName());
            tmp.append("| resolvedJndiName=");
            tmp.append(ref.getResolvedJndiName());
            tmp.append('\n');
         }
      }
      EJBReferencesMetaData ejbRefs = env.getEjbReferences();
      if(ejbRefs != null)
      {
         for(EJBReferenceMetaData ref : ejbRefs)
         {
            for(int n = 0; n < depth+1; n ++)
               tmp.append('+');
            tmp.append("ejb-ref(");
            tmp.append(ref.getEjbRefName());
            tmp.append(") -> mappedName=");
            tmp.append(ref.getMappedName());
            tmp.append("| resolvedJndiName=");
            tmp.append(ref.getResolvedJndiName());
            tmp.append('\n');
         }
      }
      MessageDestinationReferencesMetaData msgRefs = env.getMessageDestinationReferences();
   }
   private void displayEndpoints(String unitName, Map<String, ContainerDependencyMetaData> endpointMap)
   {
      StringBuilder tmp = new StringBuilder(unitName+" endpoint mappings:\n");
      for(Map.Entry<String, ContainerDependencyMetaData> entry : endpointMap.entrySet())
      {
         tmp.append("  + "+entry.getKey()+" -> "+entry.getValue());
         tmp.append('\n');
      }
      log.debug(tmp.toString());
   }
}
