/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.cluster.defaultcfg.clusteredentity.test;

import junit.framework.Test;

import org.jboss.test.cluster.testutil.DBSetup;



/**
 * Duplicates teh EntityQueryUnitTestCase, but with a
 * hibernate.cache.region_prefix specified in persistence.xml.
 * 
 * @author Brian Stansberry
 */
public class ProvidedPrefixEntityQueryUnitTestCase extends EntityQueryUnitTestCase
{
   public static final String EAR_NAME = "clusteredentity-providedprefix-test";
   public static final String PROVIDED_PREFIX = "myprefix";
   
   public ProvidedPrefixEntityQueryUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      return DBSetup.getDeploySetup(ProvidedPrefixEntityQueryUnitTestCase.class, 
                               EAR_NAME + ".ear");
   }

   @Override
   protected String createRegionName(String noPrefix)
   {
      return "/" + PROVIDED_PREFIX + "/" + noPrefix.replace('.', '/');
   }

   @Override
   protected String getEarName()
   {
      return EAR_NAME;
   }

   @Override
   protected String getJarName()
   {
      return EAR_NAME;
   }

   
   

}
