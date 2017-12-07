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
package org.jboss.jmx.connector.invoker;
 
import java.util.HashSet;
import java.util.Properties; 
import java.util.StringTokenizer;

import org.jboss.logging.Logger;
import org.jboss.security.SimplePrincipal;

//$Id: ExternalizableRolesAuthorization.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  JBAS-3203: Delegate for Authorization Interceptor for RMIAdaptor should have roles configurable
 *  Authorization Delegate used by the AuthorizationInterceptor
 *  that gets its predefined roles from a properties file
 *  @see org.jboss.jmx.connector.invoker.AuthorizationInterceptor
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  May 10, 2006
 *  @version $Revision: 85945 $
 */
public class ExternalizableRolesAuthorization extends RolesAuthorization
{
   private static Logger log = Logger.getLogger(ExternalizableRolesAuthorization.class);
   private boolean trace = log.isTraceEnabled();
   
   public ExternalizableRolesAuthorization()
   {
      //Load the roles from a properties file 
      Properties props = new Properties();
      try
      {
         props.load(getTCL().getResourceAsStream("jmxinvoker-roles.properties")); 
         this.setRequiredRoles(getSetOfRoles(props.getProperty("roles")));
      }
      catch (Exception e)
      {
         log.error("Error reading roles from jmxinvoker-roles.properties:",e);
      } 
   } 
   
   /**
    * Get a HashSet of roles as SimplePrincipal
    * 
    * @param assignedRoles a comma seperated list of roles
    * @return
    */
   private HashSet getSetOfRoles(String assignedRoles)
   {
      if(trace)
         log.trace("AssignedRolesString="+assignedRoles);
      HashSet set = new HashSet();
      StringTokenizer st = new StringTokenizer(assignedRoles,",");
      while(st.hasMoreTokens())
      {
         String aRole = st.nextToken();
         set.add(new SimplePrincipal(aRole));
      }
      if(trace)
         log.trace("roles set="+set);
      return set;
   } 
   
   private ClassLoader getTCL()
   {
      return Thread.currentThread().getContextClassLoader();
   }
}
