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
package org.jboss.deployment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * IncompleteDeploymentException
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
 */
public class IncompleteDeploymentException extends DeploymentException
{
   /** @since 4.0.2 */
   private static final long serialVersionUID = 1428860525880893167L;
   
   /** non-serializable info */
   private transient final Collection mbeansWaitingForClasses;
   private transient final Collection mbeansWaitingForDepends;
   private transient final Collection rootCause;
   private transient final Collection incompletePackages;
   private transient final Collection waitingForDeployer;

   /** only serializable info */
   private String string;

   /**
    * CTOR
    * 
    * @param mbeansWaitingForClasses
    * @param mbeansWaitingForDepends
    * @param rootCause
    * @param incompletePackages
    * @param waitingForDeployer
    */
   public IncompleteDeploymentException(final Collection mbeansWaitingForClasses, 
                                        final Collection mbeansWaitingForDepends,
                                        final Collection rootCause,
                                        final Collection incompletePackages,
                                        final Collection waitingForDeployer) 
   {
      if (mbeansWaitingForClasses == null 
          || mbeansWaitingForDepends == null
          || rootCause == null
          ||incompletePackages == null
          || waitingForDeployer == null) 
      {
         throw new IllegalArgumentException("All lists in IncompleteDeploymentException constructor must be supplied");
      } // end of if ()
      
      this.mbeansWaitingForClasses = mbeansWaitingForClasses;
      this.mbeansWaitingForDepends = mbeansWaitingForDepends;
      this.rootCause = rootCause;
      this.incompletePackages = incompletePackages;
      this.waitingForDeployer = waitingForDeployer;
   }

   /**
    * Get the MbeansWaitingForClasses value.
    * @return the MbeansWaitingForClasses value.
    */
   public Collection getMbeansWaitingForClasses()
   {
      return mbeansWaitingForClasses;
   }

   /**
    * Get the MbeansWaitingForDepends value.
    * @return the MbeansWaitingForDepends value.
    */
   public Collection getMbeansWaitingForDepends()
   {
      return mbeansWaitingForDepends;
   }

   /**
    * Get the IncompletePackages value.
    * @return the IncompletePackages value.
    */
   public Collection getIncompletePackages()
   {
      return incompletePackages;
   }

   /**
    * Get the WaitingForDeployer value.
    * @return the WaitingForDeployer value.
    */
   public Collection getWaitingForDeployer()
   {
      return waitingForDeployer;
   }

   /**
    * @return true is no information is contained at all
    */
   public boolean isEmpty()
   {
      return mbeansWaitingForClasses.size() == 0 
         && mbeansWaitingForDepends.size() == 0
         && rootCause.size() == 0
         && incompletePackages.size() == 0
         && waitingForDeployer.size() == 0;
   }

   /**
    * Convert to String and cache the deployment information
    */
   public String toString()
   {
      if (string != null) 
      {
         return string;
      }
      
      StringBuffer result = new StringBuffer("Incomplete Deployment listing:\n\n");
      if (waitingForDeployer.size() != 0) 
      {
         result.append("--- Packages waiting for a deployer ---\n");
         appendCollection(result, waitingForDeployer);
      }
      
      if (incompletePackages.size() != 0) 
      {
         result.append("--- Incompletely deployed packages ---\n");
         appendCollection(result, incompletePackages);
      }
      
      if (mbeansWaitingForClasses.size() != 0) 
      {
         result.append("--- MBeans waiting for classes ---\n");
         appendCollection(result, mbeansWaitingForClasses);
      }
      
      if (mbeansWaitingForDepends.size() != 0) 
      {
         result.append("--- MBeans waiting for other MBeans ---\n");
         appendCollection(result, mbeansWaitingForDepends);
      }
      
      if (rootCause.size() != 0) 
      {
         result.append("--- MBEANS THAT ARE THE ROOT CAUSE OF THE PROBLEM ---\n");
         appendCollection(result, rootCause);
      }
      
      string = result.toString();
      return string;      
   }
      
   private void appendCollection(StringBuffer result, Collection c)
   {
      for (Iterator i = c.iterator(); i.hasNext();)
         result.append(i.next().toString()).append('\n');
   }

   /**
    * Read-in the string-fied information produced by writeObject()
    */
   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException
   {
      s.defaultReadObject();
   }

   /**
    * String-ify the contained information when serializing
    */
   private void writeObject(ObjectOutputStream s) throws IOException
   {
      toString();
      s.defaultWriteObject();
   }
   
}
