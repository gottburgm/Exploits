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
package org.jboss.naming;

import javax.naming.NamingException;

import org.jboss.system.ServiceMBean;
import org.jboss.system.ServiceMBeanSupport;

/**
 * A simple utility mbean that allows one to create an alias in
 * the form of a LinkRef from one JNDI name to another.
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @version $Revision: 81030 $
 */
public class NamingAlias
   extends ServiceMBeanSupport
   implements NamingAliasMBean
{
   private String fromName;
   private String toName;

   public NamingAlias()
   {
      this(null, null);
   }
   
   public NamingAlias(final String fromName, final String toName)
   {
      this.fromName = fromName;
      this.toName = toName;
   }
   
   /**
    * Get the from name of the alias. This is the location where the
    * LinkRef is bound under JNDI.
    *
    * @jmx:managed-attribute
    * 
    * @return the location of the LinkRef
    */
   public String getFromName()
   {
      return fromName;
   }

   /**
    * Set the from name of the alias. This is the location where the
    * LinkRef is bound under JNDI.
    *
    * @jmx:managed-attribute
    * 
    * @param name, the location where the LinkRef will be bound
    */
   public void setFromName(String name) throws NamingException
   {
      removeLinkRef(fromName);
      this.fromName = name;
      createLinkRef();        
   }

   /**
    * Get the to name of the alias. This is the target name to
    * which the LinkRef refers. The name is a URL, or a name to be resolved
    * relative to the initial context, or if the first character of the name
    * is ".", the name is relative to the context in which the link is bound.
    *
    * @jmx:managed-attribute
    * 
    * @return the target JNDI name of the alias.
    */
   public String getToName()
   {
      return toName;
   }

   /**
    * Set the to name of the alias. This is the target name to
    * which the LinkRef refers. The name is a URL, or a name to be resolved
    * relative to the initial context, or if the first character of the name
    * is ".", the name is relative to the context in which the link is bound.
    *
    * @jmx:managed-attribute
    * 
    * @param name, the target JNDI name of the alias.
    */
   public void setToName(String name) throws NamingException
   {
      this.toName = name;
      
      createLinkRef();
   }
   
   protected void startService() throws Exception
   {
      if( fromName == null )
         throw new IllegalStateException("fromName is null");
      if( toName == null )
         throw new IllegalStateException("toName is null");
      createLinkRef();
   }
   
   protected void stopService() throws Exception
   {
      removeLinkRef(fromName);
   }
   
   private void createLinkRef() throws NamingException
   {
      if( super.getState() == ServiceMBean.STARTING || super.getState() == ServiceMBean.STARTED )
         Util.createLinkRef(fromName, toName);
   }
   
   /**
    * Unbind the name value if we are in the STARTED state.
    */
   private void removeLinkRef(String name) throws NamingException
   {
      if(super.getState() == ServiceMBean.STOPPING)
         Util.removeLinkRef(name);
   }
}
