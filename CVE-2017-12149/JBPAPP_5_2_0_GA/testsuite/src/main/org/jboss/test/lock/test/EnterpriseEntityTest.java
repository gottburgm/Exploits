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
package org.jboss.test.lock.test;

import java.rmi.*;

import java.util.HashMap;
import java.util.StringTokenizer;

import javax.ejb.FinderException;

import javax.naming.Context;
import javax.naming.InitialContext;
import junit.framework.Assert;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;

import org.jboss.test.JBossTestCase;
import org.jboss.test.lock.interfaces.EnterpriseEntity;

import org.jboss.test.lock.interfaces.EnterpriseEntityHome;

/**
 * #Description of the Class
 */
public abstract class EnterpriseEntityTest
       extends JBossTestCase
{
   /**
    * Description of the Field
    */
   public final static int DEFAULT_THREAD_COUNT = 20;
   /**
    * Description of the Field
    */
   public final static int DEFAULT_ITERATIONS = 10;

   private String jndiname;

   /**
    * The number of threads to test with.
    */
   private int nbThreads;
   private int completedThreads;

   /**
    * The number of iterations each thread will go through
    */
   private int iterations;

   private Worker[] threads;

   private HashMap param = new HashMap();

   private EnterpriseEntity entity;

   private boolean failed;

   /**
    * Constructor for the EnterpriseEntityTest object
    *
    * @param name      Description of Parameter
    * @param jndiname  Description of Parameter
    */
   public EnterpriseEntityTest(final String name,
         final String jndiname)
   {
      super(name);
      this.jndiname = jndiname;
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testSingleBean() throws Exception
   {
      getLog().debug("Spawning " + nbThreads + " threads for " +
            iterations + " iterations with single bean call");

      Task prototype =
         new Task()
         {
            /**
             * Main processing method for the EnterpriseEntityTest object
             *
             * @param name           Description of Parameter
             * @param i              Description of Parameter
             * @exception Exception  Description of Exception
             */
            public void run(String name, int i) throws Exception
            {
               entity.setField(name + " i=" + i);
            }
         };

      run(prototype);
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testB2B() throws Exception
   {
      getLog().debug("Spawning " + nbThreads + " threads for " +
            iterations + " iterations with bean to bean call");

      entity.setNextEntity("daniel");

      Task prototype =
         new Task()
         {
            /**
             * Main processing method for the EnterpriseEntityTest object
             *
             * @param name           Description of Parameter
             * @param i              Description of Parameter
             * @exception Exception  Description of Exception
             */
            public void run(String name, int i) throws Exception
            {
               entity.setAndCopyField(name + " i=" + i);
            }
         };

      run(prototype);
   }

   /**
    * The JUnit setup method
    *
    * @exception Exception  Description of Exception
    */
   protected void setUp() throws Exception
   {
      super.setUp();
      nbThreads = getThreadCount();//DEFAULT_THREAD_COUNT;
      iterations = getIterationCount();//DEFAULT_ITERATIONS;
      getLog().debug("+++ Setting up: " + getClass().getName() + " test: " + getName());
      EnterpriseEntityHome home =
          (EnterpriseEntityHome)getInitialContext().lookup(jndiname);

      try
      {
         entity = home.findByPrimaryKey("seb");
      }
      catch (FinderException e)
      {
         entity = home.create("seb");
      }

      // setup the threads
      threads = new Worker[nbThreads];
   }

   /**
    * Sets the Failed attribute of the EnterpriseEntityTest object
    */
   protected synchronized void setFailed()
   {
      failed = true;
   }


   /**
    * #Description of the Method
    *
    * @param prototype      Description of Parameter
    * @exception Exception  Description of Exception
    */
   protected void startAll(Task prototype) throws Exception
   {
      completedThreads = 0;
      for (int i = 0; i < nbThreads; i++)
      {
         Task task = (Task)prototype.clone();
         threads[i] = new Worker("Thread #" + (i + 1), task, getLog());
         threads[i].start();
      }
   }

   /**
    * #Description of the Method
    *
    * @exception Exception  Description of Exception
    */
   protected void joinAll() throws Exception
   {
      // wait for all the threads to finish
      for (int i = 0; i < nbThreads; i++)
      {
         threads[i].join();
      }
   }

   /**
    * Main processing method for the EnterpriseEntityTest object
    *
    * @param prototype      Description of Parameter
    * @exception Exception  Description of Exception
    */
   protected void run(Task prototype) throws Exception
   {
      startAll(prototype);
      joinAll();
      assertTrue(!failed);
   }

   /**
    * #Description of the Method
    *
    * @return   Description of the Returned Value
    */
   protected boolean hasFailed()
   {
      return failed;
   }


   /////////////////////////////////////////////////////////////////////////
   //                        Iteration Worker & Task                      //
   /////////////////////////////////////////////////////////////////////////

   /**
    * #Description of the Class
    */
   public abstract class Task
          implements Cloneable
   {
      /**
       * Main processing method for the Task object
       *
       * @param name           Description of Parameter
       * @param i              Description of Parameter
       * @exception Exception  Description of Exception
       */
      public abstract void run(String name, int i) throws Exception;

      /**
       * #Description of the Method
       *
       * @return   Description of the Returned Value
       */
      public Object clone()
      {
         try
         {
            return super.clone();
         }
         catch (CloneNotSupportedException e)
         {
            throw new InternalError();
         }
      }
   }

   /**
    * #Description of the Class
    */
   public class Worker
          extends Thread
   {
      /**
       * Description of the Field
       */
      public String name;
      /**
       * Description of the Field
       */
      public boolean running;
      /**
       * Description of the Field
       */
      public Task task;

      private Logger log;

      /**
       * Constructor for the Worker object
       *
       * @param name  Description of Parameter
       * @param task  Description of Parameter
       * @param log   Description of Parameter
       */
      public Worker(final String name, final Task task, Logger log)
      {
         this.name = name;
         this.task = task;
         this.log = log;
         running = true;
      }

      /**
       * Main processing method for the Worker object
       */
      public void run()
      {
         long start = System.currentTimeMillis();
         int i;

         for (i = 0; i < iterations; i++)
         {
            if (!running || hasFailed())
            {
               break;
            }

            try
            {
               task.run(name, i);
               //log.debug(name + " " + (i+1) + " iterations done");
            }
            catch (Throwable t)
            {
               log.error(name + " caught an exception, dying", t);
               t.printStackTrace();
               running = false;
               setFailed();
            }
         }

         synchronized (this)
         {
            completedThreads++;
         }
         long end = System.currentTimeMillis();
         log.debug(name + ": did " + i +
               " iterations in " + (end - start) + "ms, complete=" + completedThreads);
      }
   }
}

