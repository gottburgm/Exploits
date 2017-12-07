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

import java.io.ByteArrayInputStream;
import java.util.Properties;

import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfigParser;

/** The LoaderRepositoryConfigParser implementation for the HeirarchicalLoaderRepository3.
 * This implementation supports the single java2ParentDelegation property which
 * indicates whether the HeirarchicalLoaderRepository3 should load classes from
 * its scope first followed by its parent repository (java2ParentDelegation=true).
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81022 $
 */
public class HeirarchicalLoaderRepository3ConfigParser
   implements LoaderRepositoryConfigParser
{
   /** Set the HeirarchicalLoaderRepository3.UseParentFirst attribute based on
    * the value of the java2ParentDelegation property found in the config.
    *
    * @param repository the HeirarchicalLoaderRepository3 to set the
    * UseParentFirst attribute on.
    * @param config A string representation of a Properties file
    * @throws Exception
    */
   public void configure(LoaderRepository repository, String config)
      throws Exception
   {
      HeirarchicalLoaderRepository3 hlr3 = (HeirarchicalLoaderRepository3) repository;
      Properties props = new Properties();
      ByteArrayInputStream bais = new ByteArrayInputStream(config.getBytes());
      props.load(bais);
      String java2ParentDelegation = props.getProperty("java2ParentDelegation");
      if( java2ParentDelegation == null )
      {
         // Check for previous mis-spelled property name
         java2ParentDelegation = props.getProperty("java2ParentDelegaton", "false");
      }
      boolean useParentFirst = Boolean.valueOf(java2ParentDelegation).booleanValue();
      hlr3.setUseParentFirst(useParentFirst);
   }
}
