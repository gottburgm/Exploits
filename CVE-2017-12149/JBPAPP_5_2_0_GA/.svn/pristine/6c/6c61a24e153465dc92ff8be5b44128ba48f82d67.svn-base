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
 * Executes the superclass tests, but with Hibernate and JBoss Cache
 * configured for optimistic locking.
 * 
 * @author Brian Stansberry
 * 
 * @version $Id: ExtendedPersistenceUnitTestCase.java 60065 2007-01-27 23:05:44Z bstansberry@jboss.com $
 */
public class OptimisticEmbeddedIdClassloaderTestCase extends EmbeddedIdClassloaderTestCase
{

   public OptimisticEmbeddedIdClassloaderTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return DBSetup.getDeploySetup(OptimisticEmbeddedIdClassloaderTestCase.class, 
                               "clusteredentity-embeddedid-optimistic-test.ear");
   }
   
   @Override
   protected String getEarName()
   {
      return "clusteredentity-embeddedid-optimistic-test";
   }   
}
