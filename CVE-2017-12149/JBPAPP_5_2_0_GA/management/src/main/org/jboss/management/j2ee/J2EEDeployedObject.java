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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.InvalidParameterException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss JSR-77 implementation of J2EEDeployedObject.
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="thomas.diesler@jboss.org">Thomas Diesler</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81025 $
 */
public abstract class J2EEDeployedObject extends J2EEManagedObject
   implements J2EEDeployedObjectMBean
{
   // Constants -----------------------------------------------------

   public static final int APPLICATION = 0;
   public static final int WEB = 1;
   public static final int EJB = 2;
   public static final int RAR = 3;
   public static final int SAR = 4;
   public static final int JBOSS = 5;
   public static final int JAWS = 6;
   public static final int CMP = 7;
   public static final int JBOSS_WEB = 8;

   /** The logger */
   private static final Logger log = Logger.getLogger(J2EEDeployedObject.class);
   
   private static final String[] sDescriptors = new String[]{
      "META-INF/application.xml",
      "WEB-INF/web.xml",
      "META-INF/ejb-jar.xml",
      "META-INF/ra.xml",
      "META-INF/jboss-service.xml",
      "META-INF/jboss.xml",
      "META-INF/jaws.xml",
      "META-INF/jbosscmp-jdbc.xml",
      "WEB-INF/jboss-web.xml",
   };

   // Attributes ----------------------------------------------------

   private String mDeploymentDescriptor;

   // Static --------------------------------------------------------

   public static String getDeploymentDescriptor(URL pJarUrl, int pType)
   {
      return getDeploymentDescriptor(pJarUrl, sDescriptors[pType]);
   }

   /**
    * Loads and returns in a string form the deployment descriptor
    * for a module. If the descriptor relative path is null the
    * baseJarUrl is used as the descriptor itself. Otherwise baseJarUrl
    * and descriptor are combined to form the final descriptor.
    */
   public static String getDeploymentDescriptor(URL baseJarUrl, String descriptor)
   {
      if (baseJarUrl == null)
      {
         // Return if the given URL is null         
         return null;
      }
      String lDD = null;
      Reader lInput = null;
      StringWriter lOutput = null;
      try
      {
         if (descriptor == null)
         {
            // Use the baseJarUrl as the descriptor
            lInput = new InputStreamReader(baseJarUrl.openStream());
         }
         else
         {
            // Look for an embedded descriptor
            log.debug("File: " + baseJarUrl + ", descriptor: " + descriptor);
            ClassLoader localCl = new URLClassLoader(new URL[]{baseJarUrl});
            InputStream lStream = localCl.getResourceAsStream(descriptor);
            if (lStream == null)
            {
               // If DD not found then return a null indicating the file is not available
               return null;
            }
            lInput = new InputStreamReader(lStream);
         }
         lOutput = new StringWriter();
         char[] lBuffer = new char[1024];
         int lLength = 0;
         while ((lLength = lInput.read(lBuffer)) > 0)
         {
            lOutput.write(lBuffer, 0, lLength);
         }
         lDD = lOutput.toString();
      }
      catch (Exception e)
      {
         log.error("failed to get deployment descriptor", e);
      }
      finally
      {
         if (lInput != null)
         {
            try
            {
               lInput.close();
            }
            catch (Exception e)
            {
            }
         }
         if (lOutput != null)
         {
            try
            {
               lOutput.close();
            }
            catch (Exception e)
            {
            }
         }
      }
      return lDD;
   }
   
   // Constructors --------------------------------------------------

   /**
    * Constructor taking the Name of this Object
    *
    * @param pName                 Name to be set which must not be null
    * @param pDeploymentDescriptor
    * @throws InvalidParameterException If the given Name is null
    */
   public J2EEDeployedObject(String pType,
                             String pName,
                             ObjectName pParent,
                             String pDeploymentDescriptor)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(pType, pName, pParent);
      mDeploymentDescriptor = pDeploymentDescriptor;
   }

   // Public --------------------------------------------------------

   // javax.management.j2ee.J2EEDeployedObject implementation -------

   /**
    * @jmx:managed-attribute
    */
   public String getdeploymentDescriptor()
   {
      return mDeploymentDescriptor;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getserver()
   {
      //[TODO] Need to be implemented
      return "unknown server name";
   }

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "J2EEDeployedObject { " + super.toString() + " } [ " +
              "deployment descriptor: " + mDeploymentDescriptor +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
