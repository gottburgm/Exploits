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
package org.jboss.test.cluster.multicfg.test;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.test.JBossClusteredTestCase;

/**
 * HA-JNDI clustering tests.
 *
 * @author Jerry Gauthier
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 106766 $
 */
public class HAJndiTestBase
      extends JBossClusteredTestCase      
{
   // ENVIRONMENT PROPERTIES
   protected static final String NODE0 = System.getProperty("node0");
   protected static final String NODE0_JNDI = System.getProperty("node0.jndi.url");
   protected static final String NODE1_JNDI = System.getProperty("node1.jndi.url");
   protected static final String NODE0_HAJNDI = System.getProperty("node0.hajndi.url");
   protected static final String NODE1_HAJNDI = System.getProperty("node1.hajndi.url");
   protected static final String DISCOVERY_GROUP = System.getProperty("jbosstest.udpGroup");
   protected static final String DISCOVERY_TTL = System.getProperty("jbosstest.udp.ip_ttl", "1");
   protected static final String DISCOVERY_PARTITION = System.getProperty("jbosstest.partitionName", "DefaultPartition");
   
   public HAJndiTestBase(String name)
   {
      super(name);
   }
  
   protected Context getContext(String url)
      throws Exception
   {
      Properties env = new Properties();        
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      env.setProperty(Context.PROVIDER_URL, url);
      env.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
		
      Context naming = new InitialContext (env);
      return naming;

   }
	
   protected void closeContext(Context context)
   {
      try 
      {
         context.close();		   
      }
      catch (NamingException e)
      {
         // no action required
      }
   }	
   
   protected Object lookup(Context context, String name, boolean failIfMissing)
   {	   
      try
      {
         Object o = context.lookup(name);
         log.info(name + " binding value: " + o);
         return o;
      }
      catch (NamingException e)
      {
         if (failIfMissing)
         {
           String msg =   "Name " + name + " not found. " + e.getLocalizedMessage();
           log.info(msg, e);
           fail(msg);
         }
         else
         {
            log.debug("Name " + name + " not found.");
         }
         return null;
      }	   
   }
   
   protected void validateUrls()
      throws Exception      
   {
      if (NODE0_JNDI == null)
         throw new Exception("node0.jndi.url not defined.");
         
      if (NODE1_JNDI == null)
         throw new Exception("node1.jndi.url not defined.");
         
      if (NODE0_HAJNDI == null)
         throw new Exception("node0.hajndi.url not defined.");
         
      if (NODE1_HAJNDI == null)
         throw new Exception("node1.hajndi.url not defined.");

   }

}
