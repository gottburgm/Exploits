/*
 * JBoss, Home of Professional Open Source.
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
package org.jboss.ejb;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.main.MainDeployerStructure;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEntityBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.util.Strings;

/** Utility methods for resolving ejb-ref and ejb-local-ref within the
 * scope of a deployment.
 *
 * @author <a href="mailto:criege@riege.com">Christian Riege</a>
 * @author Scott.Stark@jboss.org
 * @author Thomas.Diesler@jboss.org
 *
 * @version $Revision: 85945 $
 */
public final class EjbUtil50
{
   private static final Logger log = Logger.getLogger(EjbUtil50.class);

   /**
    * Resolves an &lt;ejb-link&gt; target for an &lt;ejb-ref&gt; entry and
    * returns the name of the target in the JNDI tree.
    *
    * @param server the main deployer
    * @param unit DeploymentUnit
    * @param link Content of the &lt;ejb-link&gt; entry.
    *
    * @return The JNDI Entry of the target bean; <code>null</code> if
    *         no appropriate target could be found.
    */
   public static String findEjbLink(MainDeployerStructure server, DeploymentUnit unit, String link)
   {
      return resolveLink(server, unit, link, false);
   }

   /**
    * Resolves an &lt;ejb-link&gt; target for an &lt;ejb-local-ref&gt; entry
    * and returns the name of the target in the JNDI tree.
    *
    * @param server the main deployer
    * @param unit DeploymentUnit
    * @param link Content of the &lt;ejb-link&gt; entry.
    *
    * @return The JNDI Entry of the target bean; <code>null</code> if
    *         no appropriate target could be found.
    */
   public static String findLocalEjbLink(MainDeployerStructure server, DeploymentUnit unit, String link)
   {
      return resolveLink(server, unit, link, true);
   }

   /**
    * Resolves a &lt;message-destination&gt; target for a &lt;message-destination-link&gt; 
    * entry and returns the name of the target in the JNDI tree.
    *
    * @param server the main deployer
    * @param di DeploymentUnit
    * @param link Content of the &lt;message-driven-link&gt; entry.
    *
    * @return The JNDI Entry of the target; <code>null</code> if
    *         no appropriate target could be found.
    */
   public static MessageDestinationMetaData findMessageDestination(MainDeployerStructure server, DeploymentUnit di, String link)
   {
      return resolveMessageDestination(server, di, link);
   }

   private static String resolveLink(MainDeployerStructure server, DeploymentUnit di, String link, boolean isLocal)
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
         return resolveRelativeLink(server, di, link, isLocal);
      }
      else
      {
         // <ejb-link> contains a Bean Name, scan the DeploymentUnit tree
         DeploymentUnit top = di.getTopLevel();
         return resolveAbsoluteLink(top, link, isLocal);
      }
   }

   private static String resolveRelativeLink(MainDeployerStructure server, DeploymentUnit unit, String link, boolean isLocal)
   {

      String path = link.substring(0, link.indexOf('#'));
      String ejbName = link.substring(link.indexOf('#') + 1);
      String us = unit.getName();

      // Remove the trailing slash for unpacked deployments
      if (us.charAt(us.length() - 1) == '/')
         us = us.substring(0, us.length() - 1);

      String ourPath = us.substring(0, us.lastIndexOf('/'));

      if (log.isTraceEnabled())
      {
         log.trace("Resolving relative link: " + link);
         log.trace("Looking for: '" + link + "', we're located at: '" + ourPath + "'");
      }

      for (StringTokenizer st = new StringTokenizer(path, "/"); st.hasMoreTokens();)
      {
         String s = st.nextToken();
         if (s.equals(".."))
         {
            ourPath = ourPath.substring(0, ourPath.lastIndexOf('/'));
         }
         else
         {
            ourPath += "/" + s;
         }
      }

      URL target = null;

      try
      {
         target = Strings.toURL(ourPath);
      }
      catch (MalformedURLException mue)
      {
         log.warn("Can't construct URL for: " + ourPath);
         return null;
      }

      DeploymentUnit targetUnit = null;
      try
      {
         DeploymentContext ctx = server.getDeploymentContext(target.toString());
         targetUnit = ctx.getDeploymentUnit();
      }
      catch (Exception e)
      {
         log.warn("Got Exception when looking for DeploymentUnit: " + e);
         return null;
      }

      if (targetUnit == null)
      {
         log.warn("Can't locate DeploymentUnit for target: " + target);
         return null;
      }

      if (log.isTraceEnabled())
      {
         log.trace("Found appropriate DeploymentUnit: " + targetUnit);
      }

      String linkTarget = null;
      if (targetUnit.getAttachment(JBossMetaData.class) != null)
      {
         JBossMetaData appMD = targetUnit.getAttachment(JBossMetaData.class);
         JBossEnterpriseBeanMetaData beanMD = appMD.getEnterpriseBean(ejbName);

         if (beanMD != null)
         {
            linkTarget = getJndiName(beanMD, isLocal);
         }
         else
         {
            log.warn("No Bean named '" + ejbName + "' found in '" + path + "'!");
         }
      }
      else
      {
         log.warn("DeploymentUnit " + targetUnit + " is not an EJB .jar " + "file!");
      }

      return linkTarget;
   }

   private static String resolveAbsoluteLink(DeploymentUnit unit, String link, boolean isLocal)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Resolving absolute link, di: " + unit);
      }

      String ejbName = null;

      // Search current DeploymentUnit
      if (unit.getAttachment(JBossMetaData.class) != null)
      {
         JBossMetaData appMD = unit.getAttachment(JBossMetaData.class);
         JBossEnterpriseBeanMetaData beanMD = appMD.getEnterpriseBean(link);
         if (beanMD != null)
         {
            ejbName = getJndiName(beanMD, isLocal);
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
            for(JBossEnterpriseBeanMetaData md : appMD.getEnterpriseBeans())
            {
               String beanEjbName = getJndiName(md, isLocal);
               log.trace("... ejbName: " + beanEjbName);
            }
         }
      }
      else if (unit.getAttachment("EJB_DEPLOYMENTS") != null)
      {
         log.debug("Saw EJB3 module, cannot resolve it");
      }

      // Search each subcontext
      Iterator<DeploymentUnit> it = unit.getChildren().iterator();
      while (it.hasNext() && ejbName == null)
      {
         DeploymentUnit child = it.next();
         ejbName = resolveAbsoluteLink(child, link, isLocal);
      }

      return ejbName;
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
         if( beanMD.isEntity() )
         {
            JBossEntityBeanMetaData md = (JBossEntityBeanMetaData) beanMD;
            jndiName = md.determineJndiName();
         }
         else if( beanMD.isSession())
         {
            JBossSessionBeanMetaData md = (JBossSessionBeanMetaData) beanMD;
            jndiName = md.determineJndiName();
         }
      }
      return jndiName;
   }

   private static MessageDestinationMetaData resolveMessageDestination(MainDeployerStructure server, DeploymentUnit di, String link)
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
         return resolveRelativeMessageDestination(server, di, link);
      else
      {
         // link contains a Bean Name, scan the DeploymentUnit tree
         DeploymentUnit top = di.getTopLevel();
         return resolveAbsoluteMessageDestination(top, link);
      }
   }

   private static MessageDestinationMetaData resolveRelativeMessageDestination(MainDeployerStructure server, DeploymentUnit unit, String link)
   {
      String path = link.substring(0, link.indexOf('#'));
      String destinationName = link.substring(link.indexOf('#') + 1);
      String us = unit.getName();

      // Remove the trailing slash for unpacked deployments
      if (us.charAt(us.length() - 1) == '/')
         us = us.substring(0, us.length() - 1);

      String ourPath = us.substring(0, us.lastIndexOf('/'));

      if (log.isTraceEnabled())
      {
         log.trace("Resolving relative message-destination-link: " + link);
         log.trace("Looking for: '" + link + "', we're located at: '" + ourPath + "'");
      }

      for (StringTokenizer st = new StringTokenizer(path, "/"); st.hasMoreTokens();)
      {
         String s = st.nextToken();
         if (s.equals(".."))
            ourPath = ourPath.substring(0, ourPath.lastIndexOf('/'));
         else ourPath += "/" + s;
      }

      URL target = null;
      try
      {
         target = Strings.toURL(ourPath);
      }
      catch (MalformedURLException mue)
      {
         log.warn("Can't construct URL for: " + ourPath);
         return null;
      }

      DeploymentUnit targetUnit = null;
      try
      {
         DeploymentContext ctx = server.getDeploymentContext(target.toString());
         targetUnit = ctx.getDeploymentUnit();
      }
      catch (Exception e)
      {
         log.warn("Got Exception when looking for DeploymentUnit: " + e);
         return null;
      }

      if (targetUnit == null)
      {
         log.warn("Can't locate DeploymentUnit for target: " + target);
         return null;
      }

      if (log.isTraceEnabled())
         log.trace("Found appropriate DeploymentUnit: " + targetUnit);

      if (targetUnit.getAttachment(JBossMetaData.class) != null)
      {
         JBossMetaData appMD = targetUnit.getAttachment(JBossMetaData.class);
         return appMD.getAssemblyDescriptor().getMessageDestination(destinationName);
      }
      if (targetUnit.getAttachment(JBossWebMetaData.class) != null)
      {
         JBossWebMetaData webMD = targetUnit.getAttachment(JBossWebMetaData.class);
         return webMD.getMessageDestination(destinationName);
      }
      else
      {
         log.warn("DeploymentUnit " + targetUnit + " is not an EJB .jar " + "file!");
         return null;
      }
   }

   private static MessageDestinationMetaData resolveAbsoluteMessageDestination(DeploymentUnit unit, String link)
   {
      if (log.isTraceEnabled())
         log.trace("Resolving absolute link, di: " + unit);

      // Search current DeploymentUnit
      if (unit.getAttachment(JBossMetaData.class) != null)
      {
         JBossMetaData appMD = unit.getAttachment(JBossMetaData.class);
         MessageDestinationMetaData mdMD = appMD.getAssemblyDescriptor().getMessageDestination(link);
         if (mdMD != null)
            return mdMD;
      }
      if (unit.getAttachment(JBossWebMetaData.class) != null)
      {
         JBossWebMetaData webMD = unit.getAttachment(JBossWebMetaData.class);
         return webMD.getMessageDestination(link);
      }

      // Search each subcontext
      Iterator<DeploymentUnit> it = unit.getChildren().iterator();
      while (it.hasNext())
      {
         DeploymentUnit child = it.next();
         MessageDestinationMetaData mdMD = resolveAbsoluteMessageDestination(child, link);
         if (mdMD != null)
            return mdMD;
      }

      // Not found
      return null;
   }
}