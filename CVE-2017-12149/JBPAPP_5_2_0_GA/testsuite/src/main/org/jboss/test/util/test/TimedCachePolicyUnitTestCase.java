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
package org.jboss.test.util.test;

import org.jboss.test.JBossTestCase;
import org.jboss.util.TimedCachePolicy;

/**
 * Unit tests for jboss TimedCachePolicy utility class
 *
 * @see org.jboss.util.TimedCachePolicy
 * 
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class TimedCachePolicyUnitTestCase extends JBossTestCase
{
   public TimedCachePolicyUnitTestCase(String name)
   {
      super(name);
   }

   /**
    Test setResolution
    @throws Exception
    */
   public void testSetResolution() throws Exception
   {
      TimedCachePolicy tcp = new TimedCachePolicy(5, true, 1);
      tcp.create();
      tcp.start();
      tcp.insert("key", "value");
      tcp.setResolution(2);
      Object value = tcp.get("key");
      assertTrue("key has a value", value != null);
      Thread.sleep(8000);
      value = tcp.get("key");
      tcp.stop();
      assertTrue("key has no value", value == null);
   }

   /**
    Test start/stop/start sequence.
    @throws Exception
    */
   public void testStopStart() throws Exception
   {
      TimedCachePolicy tcp = new TimedCachePolicy(5, true, 1);
      tcp.create();
      tcp.start();
      tcp.stop();
      tcp.start();
      tcp.insert("key", "value");
      tcp.setResolution(2);
      Object value = tcp.get("key");
      assertTrue("key has a value", value != null);
      Thread.sleep(8000);
      value = tcp.get("key");
      tcp.stop();
      assertTrue("key has no value", value == null);
   }
}
