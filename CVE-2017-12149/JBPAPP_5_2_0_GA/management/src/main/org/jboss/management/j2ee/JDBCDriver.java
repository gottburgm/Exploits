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
package org.jboss.management.j2ee;

import org.jboss.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.JDBCDataSource JDBCDataSource}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 81025 $
 */
public class JDBCDriver extends J2EEManagedObject
   implements JDBCDriverMBean
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(JDBCDriver.class);

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer pServer, String pName, ObjectName pService)
   {
      ObjectName lServer = null;
      try
      {
         lServer = (ObjectName) pServer.queryNames(new ObjectName(J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEServer + "," +
                 "*"),
                 null).iterator().next();
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 JNDI: " + pName, e);
         return null;
      }
      try
      {
         // Now create the JNDI Representant
         return pServer.createMBean("org.jboss.management.j2ee.JDBCDriver",
                 null,
                 new Object[]{
                    pName,
                    lServer
                 },
                 new String[]{
                    String.class.getName(),
                    ObjectName.class.getName()
                 }).getObjectName();
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 JNDI: " + pName, e);
         return null;
      }
   }

   public static void destroy(MBeanServer pServer, String pName)
   {
      try
      {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JDBCDriver + "," +
                 "name=" + pName + "," +
                 "*");
         ObjectName lJNDI = (ObjectName) pServer.queryNames(lSearch,
                 null).iterator().next();
         // Now remove the J2EEApplication
         pServer.unregisterMBean(lJNDI);
      }
      catch (Exception e)
      {
         log.error("Could not destroy JSR-77 JNDI: " + pName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param pName Name of the JDBCDataSource
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    */
   public JDBCDriver(String pName, ObjectName pServer)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.JDBCDriver, pName, pServer);
   }

   // Public --------------------------------------------------------

   // org.jboss.ServiceMBean overrides ------------------------------------

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "JDBCDriver { " + super.toString() + " } [ " +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
