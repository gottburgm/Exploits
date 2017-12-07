/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jbossts.taskdefs;

import java.util.Map;

import junit.framework.TestCase;

/**
 * Abstract JUnit-based client test.
 * 
 * @author <a href="istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision: 1.1 $
 */
public abstract class JUnitClientTest extends TestCase
{
   protected ASTestConfig config = null;
   protected Map<String, String> params = null;
   protected boolean isDebug = false;

   public JUnitClientTest()
   {
      super();
   }

   public JUnitClientTest(String name)
   {
      super(name);
   }
   
   public void init(ASTestConfig config, Map<String, String> params, boolean debug)
   {
      this.config = config;
      this.params = params;
      this.isDebug = debug;
   }

   /**
    * The test method.
    * Should be overridden.
    */
   public abstract void testAction();

   
   protected void print(String msg)
   {
      System.out.println(msg);
   }
   
   public void suspendFor(int millis)
   {
      try
      {
         Thread.sleep(millis);
      }
      catch (InterruptedException e)
      {
         System.out.println("Test " + getName() + " interupted");
      }
   }

}
