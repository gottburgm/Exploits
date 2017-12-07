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

import java.net.URL;

/** An extension of UnifiedClassLoader that manages a thread based loading
 * strategy to work around the locking problems associated with the VM
 * initiated locking due to the synchronized loadClassInternal method of
 * ClassLoader which cannot be overriden.

 * @author <a href="scott.stark@jboss.org">Scott Stark</a>
 * @version $Revision: 81022 $
*/
public class UnifiedClassLoader3 extends UnifiedClassLoader
   implements UnifiedClassLoader3MBean
{
   // Static --------------------------------------------------------

   // Attributes ----------------------------------------------------

   // Constructors --------------------------------------------------
   /**
    * Construct a <tt>UnifiedClassLoader</tt> without registering it to the
    * classloader repository.
    *
    * @param url   the single URL to load classes from.
    */
   public UnifiedClassLoader3(URL url)
   {
      this(url, null);
   }
   /**
    * Construct a <tt>UnifiedClassLoader</tt> without registering it to the
    * classloader repository.
    *
    * @param url   the single URL to load classes from.
    * @param origURL the possibly null original URL from which url may
    * be a local copy or nested jar.
    */
   public UnifiedClassLoader3(URL url, URL origURL)
   {
      super(url, origURL);
   }

   /** Construct a UnifiedClassLoader and associate it with the given
    * repository.
    * @param url The single URL to load classes from.
    * @param origURL the possibly null original URL from which url may
    * be a local copy or nested jar.
    * @param repository the repository this classloader delegates to
    */
   public UnifiedClassLoader3(URL url, URL origURL, LoaderRepository repository)
   {
      this(url, origURL);

      // set the repository reference
      this.setRepository(repository);
   }
   /** Construct a UnifiedClassLoader and associate it with the given
    * repository.
    * @param url The single URL to load classes from.
    * @param origURL the possibly null original URL from which url may
    * be a local copy or nested jar.
    * @param parent the parent class loader to use
    * @param repository the repository this classloader delegates to
    */
   public UnifiedClassLoader3(URL url, URL origURL, ClassLoader parent,
         LoaderRepository repository)
   {
      super(url, origURL, parent);

      // set the repository reference
      this.setRepository(repository);
   }

   // Public --------------------------------------------------------

   /**
   * Retruns a string representaion of this UCL.
   */
   public String toString()
   {
      StringBuffer tmp = new StringBuffer(super.toString());
      tmp.setCharAt(tmp.length()-1, ',');
      tmp.append("addedOrder=");
      tmp.append(getAddedOrder());
      tmp.append('}');
      return tmp.toString();
   }
}
