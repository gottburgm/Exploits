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
package org.jboss.mx.loading;

import java.util.Set;

/** The UnifiedLoaderRepository3 (ULR) management interface
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81022 $
 */
public interface UnifiedLoaderRepository3MBean extends UnifiedLoaderRepositoryMBean
{
   /** Called by LoadMgr to obtain all class loaders for the given className
    *@return LinkedList<UnifiedClassLoader3>, may be null
    */
   public Set getPackageClassLoaders(String className);

   /** A utility method that iterates over all repository class loader and
    * display the class information for every UCL that contains the given
    * className
    */
   public String displayClassInfo(String className);

   /** Get the number of classes loaded into the ULR cache.
    * @return the classes cache size.
    */
   public int getCacheSize();
   /** Get the number of UnifiedClassLoader3s (UCLs) in the ULR
    * @return the number of UCLs in the ULR
    */
   public int getClassLoadersSize();
   /** Flush the ULR classes cache
    */
   public void flush();
}
