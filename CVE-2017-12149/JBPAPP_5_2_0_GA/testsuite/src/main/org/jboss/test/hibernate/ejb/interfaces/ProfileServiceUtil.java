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
package org.jboss.test.hibernate.ejb.interfaces;

/**
 * Utility class for ProfileService.
 */
public class ProfileServiceUtil
{

   private static Object lookupHome(java.util.Hashtable environment, String jndiName, Class narrowTo) throws javax.naming.NamingException {
      // Obtain initial context
      javax.naming.InitialContext initialContext = new javax.naming.InitialContext(environment);
      try {
         Object objRef = initialContext.lookup(jndiName);
         // only narrow if necessary
         if (narrowTo.isInstance(java.rmi.Remote.class))
            return javax.rmi.PortableRemoteObject.narrow(objRef, narrowTo);
         else
            return objRef;
      } finally {
         initialContext.close();
      }
   }

   // Home interface lookup methods

   /**
    * Obtain remote home interface from default initial context
    * @return Home interface for ProfileService. Lookup using JNDI_NAME
    */
   public static org.jboss.test.hibernate.ejb.interfaces.ProfileServiceHome getHome() throws javax.naming.NamingException
   {
      Object home = lookupHome(null, org.jboss.test.hibernate.ejb.interfaces.ProfileServiceHome.JNDI_NAME, org.jboss.test.hibernate.ejb.interfaces.ProfileServiceHome.class);
      return (org.jboss.test.hibernate.ejb.interfaces.ProfileServiceHome) home;
   }

   /**
    * Obtain remote home interface from parameterised initial context
    * @param environment Parameters to use for creating initial context
    * @return Home interface for ProfileService. Lookup using JNDI_NAME
    */
   public static org.jboss.test.hibernate.ejb.interfaces.ProfileServiceHome getHome( java.util.Hashtable environment ) throws javax.naming.NamingException
   {
       return (org.jboss.test.hibernate.ejb.interfaces.ProfileServiceHome) lookupHome(environment, org.jboss.test.hibernate.ejb.interfaces.ProfileServiceHome.JNDI_NAME, org.jboss.test.hibernate.ejb.interfaces.ProfileServiceHome.class);
   }

}