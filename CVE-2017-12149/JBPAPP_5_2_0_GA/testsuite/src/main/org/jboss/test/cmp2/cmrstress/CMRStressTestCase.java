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
package org.jboss.test.cmp2.cmrstress;

import javax.ejb.DuplicateKeyException;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.cmp2.cmrstress.interfaces.Parent;
import org.jboss.test.cmp2.cmrstress.interfaces.ParentUtil;



/**
 * A test suite designed to stress test CMR operations by invoking lots of them
 * at around the same time from multiple threads.
 * 
 * In particular, we are testing the ability to load all the beans on the many
 * side of 1..many relationship, where the getter has been
 * marked <code>read-only</code>.
 * 
 * This code is based upon the original test case provided by Andrew May.
 *
 * @see  org.jboss.test.JBossTestCase.
 *
 * @version <tt>$Revision: 81036 $</tt>
 * @author  <a href="mailto:steve@resolvesw.com">Steve Coy</a>.
 *
 */
public class CMRStressTestCase extends JBossTestCase
{

   /**
    * This nested <code>Runnable</code> simply invokes a method of the specified
    * parent bean many times in a loop.
    * The parent bean method accesses its CMR collection of child beans and
    * iterates over it.
    */
   public static class CMRTest implements Runnable
   {
      public CMRTest(String parentpk, int loops) throws Exception
      {
         mLoops = loops;
         mParent = ParentUtil.getHome().findByPrimaryKey(parentpk);
      }

      public void run()
      {
         // Access the getter the specified number of times.
         for (int i = 0; i < mLoops; ++i)
         {
            try
            {
               // The following getter starts a new transaction
               // and accesses its CMR collection.
               Object map = mParent.getPropertyMap();
            }
            catch (Throwable e)
            {
               System.out.println(Thread.currentThread().getName());
               e.printStackTrace();
               mUnexpected = e;
            }
         }
         synchronized(mLock)
         {
            ++mCompleted;
            System.out.println("Completed " + mCompleted + " of " + mTarget);
            mLock.notifyAll();
         }
      }

      private final int mLoops;
      private final Parent mParent;

   }

   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(CMRStressTestCase.class, "cmp2-cmrstress.jar");
   }

   // Constructors --------------------------------------------------

   public CMRStressTestCase(String name)
      throws java.io.IOException
   {
      super(name);     
   }

   // Public --------------------------------------------------------

   /**
    * Launch threads which access the CMR collection of the parent bean created below.
    * @see #setup.
    */
   public void testRelations() throws Exception
   {
      mTarget = getThreadCount();
      getLog().info("testRelations started, count="+mTarget);
      for (int i = 0; i < mTarget; ++i)
      {
         Thread thread = new Thread(new CMRTest(PARENT_PK, getIterationCount()), "CMRTestThread-" + i);
         thread.start();
         getLog().info("Started thread: "+thread);
      }
      waitForCompletion();

      getLog().info("testRelations finished");
   }


   // Protected -------------------------------------------------------

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception
   {
      super.setUp();
      
      // Create a parent bean
      Parent parent;
      try
      {
         parent = ParentUtil.getHome().create(PARENT_PK);

         // Create some child beans
         for (int i = 0; i < getBeanCount(); ++i)
            parent.addChild(i, CHILD_FIELD1 + i, CHILD_FIELD2 + i);

      }
      catch (DuplicateKeyException e)
      {
         getLog().info("Parent bean already exists");
         parent = ParentUtil.getHome().findByPrimaryKey(PARENT_PK);
         
         // We'll assume that the tables were not removed before and keep going...
      }
      catch (Exception e)
      {
         getLog().error("Failed to create parent bean", e);
         throw e;
      }
   }

   
   // Private -------------------------------------------------------
 
   private void waitForCompletion() throws Exception
   {
      getLog().debug("Waiting for completion");
      synchronized (mLock)
      {
         while (mCompleted < mTarget)
         {
            mLock.wait();
            if (mUnexpected != null)
            {
               getLog().error("Unexpected exception", mUnexpected);
               fail("Unexpected exception");
            }
         }
      }
   }

   private static final String PARENT_PK = "arbitrary pk";
   private static final String CHILD_FIELD1 = "dummy field 1 - ";
   private static final String CHILD_FIELD2 = "dummy field 2 - ";
   
   private static final Object mLock = new Object();
   private static int mCompleted = 0;
   private static int mTarget;
   private static Throwable mUnexpected;
}
