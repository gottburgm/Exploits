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
package org.jboss.ha.framework.server.util;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ha.framework.server.util.TopologyMonitorService.AddressPort;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/** A utility MBean that can be used as the trigger target of the
 * TopologyMonitorService to probe the state of JNDI on the cluster nodes.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81001 $
 */
public class PingJndi extends ServiceMBeanSupport implements PingJndiMBean
{
   private volatile String urlPrefix;
   private volatile String urlSuffix;
   private volatile String urlPattern;
   private volatile String[] lookupNames;

   /** Get the names of JNDI bindings that should be queried on each host
    * @return the array of target names to test
    * @jmx:managed-attribute
    */
   public String[] getLookupNames()
   {
      return this.lookupNames;
   }

   /** Set the names of JNDI bindings that should be queried on each host
    * @param names
    * @jmx:managed-attribute
    */
   public void setLookupNames(String[] names)
   {
      this.lookupNames = names;
   }

   /** Get the Context.PROVIDER_URL regular expression.
    * @return the regular expression containing the host, for example
    * 'jnp://(host):1099/'
    * @jmx:managed-attribute
    */
   public String getProviderURLPattern()
   {
      return this.urlPattern;
   }

   /** Set the regular expression containing the hostname/IP address of
    * the JNDI provider. This expression is used to build the JNDI
    * Context.PROVIDER_URL for each node in the cluster. The expression
    * should contain a "(host)" component that will be replaced with the
    * cluster node hostname.
    *
    * @param regex the regular expression containing the host, for example
    * 'jnp://(host):1099/'
    * @jmx:managed-attribute
    */
   public void setProviderURLPattern(String regex)
   {
      this.urlPattern = regex;
      this.urlPrefix = regex;
      this.urlSuffix = "";
      String hostExp = "{host}";
      int hostIndex = regex.indexOf(hostExp);
      if (hostIndex >= 0)
      {
         this.urlPrefix = regex.substring(0, hostIndex);
         int endIndex = hostIndex + hostExp.length();
         this.urlSuffix = regex.substring(endIndex);
      }
   }

   /** The TopologyMonitorService trigger callback operation.
    *
    * @param removed ArrayList<AddressPort> of nodes that were removed
    * @param added ArrayList<AddressPort> of nodes that were added
    * @param members ArrayList<AddressPort> of nodes currently in the cluster
    * @param logLoggerName the log4j category name used by the
    * TopologyMonitorService. This is used for logging to integrate with
    * the TopologyMonitorService output.
    */
   public void membershipChanged(ArrayList removed, ArrayList added, ArrayList members, String logLoggerName)
   {
      this.log.debug("membershipChanged");
      Logger tmsLog = Logger.getLogger(logLoggerName);
      
      try
      {
         InitialContext localCtx = new InitialContext();
         Hashtable localEnv = localCtx.getEnvironment();

         tmsLog.info("Checking removed hosts JNDI binding");
         this.doLookups(localEnv, tmsLog, removed);
         tmsLog.info("Checking added hosts JNDI binding");
         this.doLookups(localEnv, tmsLog, added);
         tmsLog.info("Checking members hosts JNDI binding");
         this.doLookups(localEnv, tmsLog, members);
      }
      catch (NamingException e)
      {
         tmsLog.error("Failed to obtain InitialContext env", e);
         return;
      }
   }

   private void doLookups(Hashtable localEnv, Logger tmsLog, ArrayList nodes)
   {
      for (int n = 0; n < nodes.size(); n++)
      {
         AddressPort addrInfo = (AddressPort) nodes.get(n);
         String providerURL = this.urlPrefix + addrInfo.getHostName() + this.urlSuffix;
         Hashtable env = new Hashtable(localEnv);
         env.put(Context.PROVIDER_URL, providerURL);
         tmsLog.info("Checking names on: " + addrInfo);
         try
         {
            InitialContext ctx = new InitialContext(env);
            for (String name: this.lookupNames)
            {
               Object value = ctx.lookup(name);
               tmsLog.info("lookup(" + name + "): " + value);
            }
         }
         catch (Exception e)
         {
            tmsLog.error("Failed lookups on: " + addrInfo, e);
         }
      }
   }
}
