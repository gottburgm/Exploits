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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossEntityBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
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
 * A deployer which resolves references for parsing deployers
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ReferenceMetaDataResolverDeployer extends AbstractRealDeployer
{
   private static Logger log = Logger.getLogger(ReferenceMetaDataResolverDeployer.class);
   /** */
   private boolean failOnUnresolvedRefs;

   /**
    * A map of vfs paths to reference endpoint mappedNames
    */
   private ConcurrentHashMap<String, String> mappedNameMap = new ConcurrentHashMap<String, String>();

   public ReferenceMetaDataResolverDeployer()
   {
      setStage(DeploymentStages.POST_CLASSLOADER);
      HashSet<String> inputs = new HashSet<String>();
      inputs.add(JBossClientMetaData.class.getName());
      inputs.add(JBossMetaData.class.getName());
      inputs.add(JBossWebMetaData.class.getName());
      super.setInputs(inputs);
   }

   /**
    * We want to process the parent last
    */
   @Override
   public boolean isParentFirst()
   {
      return false;
   }

   /**
    * Look for ejb, web or client metadata to resolve references without
    * mapped names.
    */
   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      JBossMetaData ejbMetaData = unit.getAttachment(JBossMetaData.class);
      JBossWebMetaData webMetaData = unit.getAttachment(JBossWebMetaData.class);
      JBossClientMetaData clientMetaData = unit.getAttachment(JBossClientMetaData.class);
      if(ejbMetaData == null && webMetaData == null && clientMetaData == null)
         return;

      // Create a map of the ejbs
      dump(unit);
      if(ejbMetaData != null)
      {
         JBossEnterpriseBeansMetaData beans = ejbMetaData.getEnterpriseBeans();
         // Map the ejbs
         this.mapEjbs(unit.getRelativePath(), beans);
         // Process ejb references
         List<String> unresolvedPaths = resolve(unit, beans);
         if(unresolvedPaths != null && unresolvedPaths.size() > 0)
            log.warn("Unresolved references exist in JBossMetaData: "+unresolvedPaths);         
      }
      if(webMetaData != null)
      {
         // Process web app references
         List<String> unresolvedPaths = new ArrayList<String>();
         resolve(unit, webMetaData.getJndiEnvironmentRefsGroup(), unresolvedPaths);         
         if(unresolvedPaths != null && unresolvedPaths.size() > 0)
            log.warn("Unresolved references exist in JBossWebMetaData: "+unresolvedPaths);
      }
      if(clientMetaData != null)
      {
         // Process client app references
         List<String> unresolvedPaths = new ArrayList<String>();
         resolve(unit, clientMetaData.getJndiEnvironmentRefsGroup(), unresolvedPaths);
         if(unresolvedPaths != null && unresolvedPaths.size() > 0)
            log.warn("Unresolved references exist in JBossClientMetaData: "+unresolvedPaths);
      }
   }

   protected void mapEjbs(String vfsPath, JBossEnterpriseBeansMetaData beans)
   {
      if(beans == null || beans.size() == 0)
         return;
      for(JBossEnterpriseBeanMetaData bean : beans)
      {
         String ejbPath = vfsPath + "/" + bean.getEjbName();
         // TODO: endpoints based on business interface, etc.
         String mappedName = bean.getMappedName();
         if(mappedName != null)
            mappedNameMap.put(ejbPath, mappedName);
         if(bean instanceof JBossSessionBeanMetaData)
         {
            JBossSessionBeanMetaData sbean = (JBossSessionBeanMetaData) bean;
            BusinessLocalsMetaData locals = sbean.getBusinessLocals();
            if(locals != null)
            {
               for(String local : locals)
               {
                  // TODO: what is the correct key name
               }
            }
            BusinessRemotesMetaData remotes = sbean.getBusinessRemotes();
            if(remotes != null)
            {
               for(String remote : remotes)
               {
                  // TODO: what is the correct key name                  
               }
            }
         }
      }
   }

   protected void resolve(DeploymentUnit unit, Environment env, List<String> unresolvedRefs)
   {
      if(env == null)
         return;
      
      EJBLocalReferencesMetaData localRefs = env.getEjbLocalReferences();
      resolveEjbLocalRefs(unit, localRefs, unresolvedRefs);
      AnnotatedEJBReferencesMetaData annotatedRefs = env.getAnnotatedEjbReferences();
      resolveAnnotatedRefs(unit, annotatedRefs, unresolvedRefs);
      EJBReferencesMetaData ejbRefs = env.getEjbReferences();
      resolveEjbRefs(unit, ejbRefs, unresolvedRefs);
      MessageDestinationReferencesMetaData msgRefs = env.getMessageDestinationReferences();
      resolveMsgRefs(unit, msgRefs, unresolvedRefs);
      // TODO, other references
   }
   protected List<String> resolve(DeploymentUnit unit, JBossEnterpriseBeansMetaData beans)
   {
      ArrayList<String> unresolvedRefs = new ArrayList<String>();
      if(beans == null || beans.size() == 0)
         return unresolvedRefs;

      for(JBossEnterpriseBeanMetaData bean : beans)
      {
         Environment env = bean.getJndiEnvironmentRefsGroup();
         resolve(unit, env, unresolvedRefs);
      }
      return unresolvedRefs;
   }

   protected void resolveEjbLocalRefs(DeploymentUnit unit, EJBLocalReferencesMetaData localRefs, List<String> unresolvedRefs)
   {
      if(localRefs == null)
         return;
      ArrayList<String> searched = new ArrayList<String>();
      for(EJBLocalReferenceMetaData ref : localRefs)
      {
         String link = ref.getLink();
         String target = findLocalEjbLink(unit, link, searched);
         if(target == null)
            unresolvedRefs.add(ref.getEjbRefName()+"/ejb-local-ref/"+link +" available: " + searched);
         else
            ref.setResolvedJndiName(target);
      }      
   }
   protected void resolveAnnotatedRefs(DeploymentUnit unit, AnnotatedEJBReferencesMetaData annotatedRefs, List<String> unresolvedRefs)
   {
      if(annotatedRefs == null)
         return;

      ArrayList<String> searched = new ArrayList<String>();
      for(AnnotatedEJBReferenceMetaData ref : annotatedRefs)
      {
         String mappedName = ref.getMappedName();
         if(mappedName == null || mappedName.length() == 0)
         {
            mappedName = null;
            String link = ref.getLink();
            String target = null;
            if(link != null)
               target = findEjbLink(unit, link, searched);
            if(target == null)
               unresolvedRefs.add(ref.getEjbRefName()+"/ejb-ref/"+link + " available: " + searched);
            else
               mappedName = target;
         }
         ref.setResolvedJndiName(mappedName);
      }
   }
   protected void resolveEjbRefs(DeploymentUnit unit, EJBReferencesMetaData ejbRefs, List<String> unresolvedRefs)
   {
      if(ejbRefs == null)
         return;
      ArrayList<String> searched = new ArrayList<String>();
      for(EJBReferenceMetaData ref : ejbRefs)
      {
         String mappedName = ref.getMappedName();
         if(mappedName == null || mappedName.length() == 0)
         {
            mappedName = null;
            String link = ref.getLink();
            String target = null;
            if(link != null)
               target = findEjbLink(unit, link, searched);
            if(target == null)
               unresolvedRefs.add(ref.getEjbRefName()+"/ejb-ref/"+link + " available: " + searched);
            else
               mappedName = target;
         }
         ref.setResolvedJndiName(mappedName);
      }      
   }
   protected void resolveMsgRefs(DeploymentUnit unit, MessageDestinationReferencesMetaData msgRefs, List<String> unresolvedRefs)
   {
      if(msgRefs == null)
         return;
      ArrayList<MessageDestinationMetaData> searched = new ArrayList<MessageDestinationMetaData>();
      for(MessageDestinationReferenceMetaData ref : msgRefs)
      {
         String mappedName = ref.getMappedName();
         if(mappedName == null || mappedName.length() == 0)
         {
            String link = ref.getLink();
            MessageDestinationMetaData target = null;
            if(link != null)
               target = findMessageDestination(unit, link, searched);
            if(target == null)
               unresolvedRefs.add(ref.getMessageDestinationRefName()+"/message-destination-ref/"+link + " available: " + searched);
            else
               mappedName = target.getMappedName();
         }
         ref.setResolvedJndiName(mappedName);
      }
   }

   /**
    * Resolves an &lt;ejb-link&gt; target for an &lt;ejb-ref&gt; entry and
    * returns the name of the target in the JNDI tree.
    *
    * @param unit DeploymentUnit
    * @param link Content of the &lt;ejb-link&gt; entry.
    * @param searched the list of ejbs searched
    *
    * @return The JNDI Entry of the target bean; <code>null</code> if
    *         no appropriate target could be found.
    */
   public static String findEjbLink(DeploymentUnit unit, String link, ArrayList<String> searched)
   {
      return resolveLink(unit, link, searched, false);
   }

   /**
    * Resolves an &lt;ejb-link&gt; target for an &lt;ejb-local-ref&gt; entry
    * and returns the name of the target in the JNDI tree.
    *
    * @param unit DeploymentUnit
    * @param link Content of the &lt;ejb-link&gt; entry.
    * @param searched the searched ejbs
    *
    * @return The JNDI Entry of the target bean; <code>null</code> if
    *         no appropriate target could be found.
    */
   public static String findLocalEjbLink(DeploymentUnit unit, String link, ArrayList<String> searched)
   {
      return resolveLink(unit, link, searched, true);
   }

   /**
    * Resolves a &lt;message-destination&gt; target for a &lt;message-destination-link&gt; 
    * entry and returns the name of the target in the JNDI tree.
    *
    * @param di DeploymentUnit
    * @param link Content of the &lt;message-driven-link&gt; entry.
    *
    * @return The JNDI Entry of the target; <code>null</code> if
    *         no appropriate target could be found.
    */
   public static MessageDestinationMetaData findMessageDestination(DeploymentUnit di, String link, ArrayList<MessageDestinationMetaData> searched)
   {
      return resolveMessageDestination(di, link, searched);
   }

   private static String resolveLink(DeploymentUnit di, String link, ArrayList<String> searched, boolean isLocal)
   {
      if (link == null)
      {
         return null;
      }

      if (log.isTraceEnabled())
      {
         log.trace("resolveLink( {" + di + "}, {" + link + "}, {" + isLocal + "}");
      }

      if (di == null)
      {
         // We should throw an IllegalArgumentException here probably?
         return null;
      }

      if (link.indexOf('#') != -1)
      {
         // <ejb-link> is specified in the form path/file.jar#Bean
         return resolveRelativeLink(di, link, searched, isLocal);
      }
      else
      {
         // <ejb-link> contains a Bean Name, scan the DeploymentUnit tree
         DeploymentUnit top = di.getTopLevel();
         return resolveAbsoluteLink(top, link, searched, isLocal);
      }
   }

   private static String resolveRelativeLink(DeploymentUnit unit, String link, ArrayList<String> searched, boolean isLocal)
   {
      DeploymentUnit top = unit.getTopLevel();
      String path = link.substring(0, link.indexOf('#'));
      String ejbName = link.substring(link.indexOf('#') + 1);

      
      if (log.isTraceEnabled())
      {
         log.trace("Resolving relative link: " + link);
      }
      DeploymentUnit targetUnit = findLinkPath(top, path);
      if (targetUnit == null)
      {
         log.warn("Can't locate DeploymentUnit for target: " + path);
         return null;
      }

      String linkTarget = null;
      if (targetUnit.getAttachment(JBossMetaData.class) != null)
      {
         JBossMetaData appMD = targetUnit.getAttachment(JBossMetaData.class);
         JBossEnterpriseBeanMetaData beanMD = appMD.getEnterpriseBean(ejbName);

         if (beanMD != null)
         {
            linkTarget = getJndiName(beanMD, isLocal);
            if (linkTarget == null)
            {
               if (isLocal)
                  log.warn("Unable to determine local jndi name for " + beanMD.getEjbName());
               else
                  log.warn("Unable to determine jndi name for " + beanMD.getEjbName());
               searched.add(beanMD.getEjbName());
            }
         }
         else
         {
            log.warn("No Bean named '" + ejbName + "' found in '" + path + "'!");
            for (JBossEnterpriseBeanMetaData md : appMD.getEnterpriseBeans())
               searched.add(md.getEjbName());
         }
      }
      else
      {
         log.warn("DeploymentUnit " + targetUnit + " is not an EJB .jar " + "file!");
         searched.add(targetUnit.getName());
      }

      return linkTarget;
   }

   private static String resolveAbsoluteLink(DeploymentUnit unit, String link, ArrayList<String> searched, boolean isLocal)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Resolving absolute link, di: " + unit);
      }

      String ejbName = null;

      // Search all ejb DeploymentUnits
      List<JBossMetaData> ejbMetaData = getAllAttachments(unit, JBossMetaData.class);
      for(JBossMetaData ejbMD : ejbMetaData)
      {
         JBossEnterpriseBeanMetaData beanMD = ejbMD.getEnterpriseBean(link);
         if (beanMD != null)
         {
            ejbName = getJndiName(beanMD, isLocal);
            if (ejbName == null)
            {
               if (isLocal)
                  log.warn("Unable to determine local jndi name for " + beanMD.getEjbName());
               else
                  log.warn("Unable to determine jndi name for " + beanMD.getEjbName());
               searched.add(beanMD.getEjbName());
            }
            if (log.isTraceEnabled())
            {
               log.trace("Found Bean: " + beanMD + ", resolves to: " + ejbName);
            }

            return ejbName;
         }
         else if (log.isTraceEnabled())
         {
            // Dump the ejb module ejbNames
            log.trace("No match for ejb-link: " + link + ", module names:");
            for(JBossEnterpriseBeanMetaData md : ejbMD.getEnterpriseBeans())
            {
               String beanEjbName = getJndiName(md, isLocal);
               searched.add(md.getEjbName());
               log.trace("... ejbName: " + beanEjbName);
            }
         }
      }

      return ejbName;
   }

   private static <T> List<T> getAllAttachments(DeploymentUnit unit, Class<T> type)
   {
      ArrayList<T> attachments = new ArrayList<T>();
      DeploymentUnit top = unit.getTopLevel();
      getAllAttachments(top, type, attachments);
      return attachments;
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

   private static String getJndiName(JBossEnterpriseBeanMetaData beanMD, boolean isLocal)
   {
      String jndiName = null;
      if (isLocal)
      {
         // Validate that there is a local home associated with this bean
         jndiName = beanMD.determineLocalJndiName();
         if (jndiName == null)
         {
            log.warn("LocalHome jndi name requested for: '" + beanMD.getEjbName() + "' but there is no LocalHome class");
         }
      }
      else
      {
         // FIXME the session.getHomeJndiName should be moved to the JBossSessionMetaData.determineJndiName()
         // and these if statements replaced with md.determineJndiName()
         if( beanMD.isEntity() )
         {
            JBossEntityBeanMetaData md = (JBossEntityBeanMetaData) beanMD;
            jndiName = md.determineJndiName();
         }
         else if( beanMD.isSession())
         {
            JBossSessionBeanMetaData md = (JBossSessionBeanMetaData) beanMD;
            jndiName = md.getHomeJndiName();
            if(jndiName == null)
               jndiName = md.determineJndiName();
         }
      }
      return jndiName;
   }

   private static MessageDestinationMetaData resolveMessageDestination(DeploymentUnit di, String link, ArrayList<MessageDestinationMetaData> searched)
   {
      if (link == null)
         return null;

      if (log.isTraceEnabled())
         log.trace("resolveLink( {" + di + "}, {" + link + "})");

      if (di == null)
         // We should throw an IllegalArgumentException here probably?
         return null;

      if (link.indexOf('#') != -1)
         // link is specified in the form path/file.jar#Bean
         return resolveRelativeMessageDestination(di, link, searched);
      else
      {
         // link contains a Bean Name, scan the DeploymentUnit tree
         DeploymentUnit top = di.getTopLevel();
         return resolveAbsoluteMessageDestination(top, link, searched);
      }
   }

   private static MessageDestinationMetaData resolveRelativeMessageDestination(DeploymentUnit unit, String link, ArrayList<MessageDestinationMetaData> searched)
   {
      String path = link.substring(0, link.indexOf('#'));
      String destinationName = link.substring(link.indexOf('#') + 1);

      if (log.isTraceEnabled())
      {
         log.trace("Resolving relative message-destination-link: " + link);
      }
      DeploymentUnit top = unit.getTopLevel();
      DeploymentUnit targetUnit = findLinkPath(top, path);
      if (targetUnit == null)
      {
         log.warn("Can't locate DeploymentUnit for target: " + path);
         return null;
      }

      if (log.isTraceEnabled())
         log.trace("Found appropriate DeploymentUnit: " + targetUnit);

      MessageDestinationMetaData md = null;
      MessageDestinationsMetaData mds = null;
      if (targetUnit.getAttachment(JBossMetaData.class) != null)
      {
         JBossMetaData appMD = targetUnit.getAttachment(JBossMetaData.class);
         mds = appMD.getAssemblyDescriptor().getMessageDestinations();
         md = mds.get(destinationName);
      }
      if (targetUnit.getAttachment(JBossWebMetaData.class) != null)
      {
         JBossWebMetaData webMD = targetUnit.getAttachment(JBossWebMetaData.class);
         mds = webMD.getMessageDestinations();
         md = mds.get(destinationName);
      }

      if(md == null)
      {
         log.warn("DeploymentUnit " + targetUnit + " is not an EJB .jar " + "file!");
         if(mds != null)
            searched.addAll(mds);
      }
      return md;
   }

   private static MessageDestinationMetaData resolveAbsoluteMessageDestination(DeploymentUnit unit, String link, ArrayList<MessageDestinationMetaData> searched)
   {
      if (log.isTraceEnabled())
         log.trace("Resolving absolute link, di: " + unit);

      // Search current DeploymentUnit
      MessageDestinationMetaData md = null;
      MessageDestinationsMetaData mds = null;
      if (unit.getAttachment(JBossMetaData.class) != null)
      {
         JBossMetaData appMD = unit.getAttachment(JBossMetaData.class);
         mds = appMD.getAssemblyDescriptor().getMessageDestinations();
         if(mds != null)
         {
            searched.addAll(mds);
            md = mds.get(link);
            if (md != null)
               return md;
         }
      }

      if (unit.getAttachment(JBossWebMetaData.class) != null)
      {
         JBossWebMetaData webMD = unit.getAttachment(JBossWebMetaData.class);
         mds = webMD.getMessageDestinations();
         if(mds != null)
         {
            searched.addAll(mds);
            md = mds.get(link);
            if (md != null)
               return md;
         }
      }

      // Search each subcontext
      Iterator<DeploymentUnit> it = unit.getChildren().iterator();
      while (it.hasNext())
      {
         DeploymentUnit child = it.next();
         MessageDestinationMetaData mdMD = resolveAbsoluteMessageDestination(child, link, searched);
         if (mdMD != null)
            return mdMD;
      }

      // Not found
      return null;
   }

   private static DeploymentUnit findLinkPath(DeploymentUnit top, String path)
   {
      List<DeploymentUnit> children = top.getChildren();
      DeploymentUnit targetUnit = null;
      if(children != null)
      {
         for(DeploymentUnit child : children)
         {
            String childPath = child.getRelativePath();
            if(childPath.endsWith(path))
            {
               targetUnit = child;
            }
         }
      }
   
      if (targetUnit == null)
      {
         return null;
      }
   
      if (log.isTraceEnabled())
      {
         log.trace("Found appropriate DeploymentUnit: " + targetUnit);
      }
      return targetUnit;
   }

   private void dump(DeploymentUnit unit)
   {
      DeploymentUnit top = unit.getTopLevel();
      StringBuffer tmp = new StringBuffer();
      dump(top, tmp, 0);
      log.debug("Processing unit:\n"+tmp);
   }
   private void dump(DeploymentUnit unit, StringBuffer tmp, int depth)
   {
      for(int n = 0; n < depth; n ++)
         tmp.append('+');
      tmp.append(unit.getRelativePath());
      JBossMetaData metaData = unit.getAttachment(JBossMetaData.class);
      if(metaData != null)
      {
         JBossEnterpriseBeansMetaData beans = metaData.getEnterpriseBeans();
         if(beans != null)
         {
            for(JBossEnterpriseBeanMetaData bean : beans)
            {
               tmp.append(",ejbName=");
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
               }               
            }
         }
      }
      tmp.append('\n');
      List<DeploymentUnit> children = unit.getChildren();
      if(children != null)
      {
         for(DeploymentUnit child : children)
            dump(child, tmp, depth+1);
      }
   }
}
