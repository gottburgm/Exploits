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
package org.jboss.system.metadata;

import java.io.Serializable;

import org.jboss.managed.api.annotation.ManagementObject;

/**
 * ServiceDeploymentClassPath.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
@ManagementObject
public class ServiceDeploymentClassPath
   implements Serializable
{
   private static final long serialVersionUID = 1;

   /** The code base */
   private String codeBase;
   
   /** The archives */
   private String archives;
   
   /**
    * Create a new ServiceDeploymentClassPath.
    * 
    * @param codeBase the code base
    * @param archives the archives
    * @throws IllegalArgumentException for a null codebase
    */
   public ServiceDeploymentClassPath(String codeBase, String archives)
   {
      if (codeBase == null)
         throw new IllegalArgumentException("Null codebase");
      this.codeBase = codeBase;
      this.archives = archives;
   }

   /**
    * Get the archives.
    * 
    * @return the archives.
    */
   public String getArchives()
   {
      return archives;
   }

   /**
    * Set the archives.
    * 
    * @param archives the archives.
    */
   public void setArchives(String archives)
   {
      this.archives = archives;
   }

   /**
    * Get the codeBase.
    * 
    * @return the codeBase.
    */
   public String getCodeBase()
   {
      return codeBase;
   }

   /**
    * Set the codeBase.
    * 
    * @param codeBase the codeBase.
    * @throws IllegalArgumentException for a null codebase
    */
   public void setCodeBase(String codeBase)
   {
      if (codeBase == null)
         throw new IllegalArgumentException("Null codebase");
      this.codeBase = codeBase;
   }
}
