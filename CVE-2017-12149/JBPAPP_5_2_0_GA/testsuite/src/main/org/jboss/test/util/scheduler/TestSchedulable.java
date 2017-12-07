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
package org.jboss.test.util.scheduler;

import java.util.Date;

import org.jboss.logging.Logger;
import org.jboss.varia.scheduler.Schedulable;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class TestSchedulable
   implements Schedulable
{
   private static final Logger log = Logger.getLogger(TestSchedulable.class);

   private String name;
   private int data;

   public TestSchedulable(String name, int data)
   {
      this.name = name;
      this.data = data;
   }

   /**
    * Just log the call
    */
   public void perform(Date time, long remaining)
   {
      log.info("Schedulable Examples is called at: " + time +
         ", remaining repetitions: " + remaining +
         ", name: " + name + ", data: " + data);
   }
}