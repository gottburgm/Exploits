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

package org.jboss.test.cluster.defaultcfg.test;

import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.jboss.ha.framework.server.JChannelFactory;
import org.jboss.logging.Logger;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.View;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;

/**
 * Tests classloader leak handling of {@link JChannelFactory}.
 * 
 * @author Brian Stansberry
 */
public class JChannelFactoryClassLoaderLeakTestCase extends TestCase
{
   private static final Logger log = Logger.getLogger(JChannelFactoryClassLoaderLeakTestCase.class);
   
   private static Method OBJECT_ARG = null;
   private static Method STRING_ARG = null;
   private static Method SIMPLE_MUX = null;
   private static Method COMPLEX_MUX = null;
   
   static
   {
      Class clazz = JChannelFactory.class;
      try
      {
         OBJECT_ARG = clazz.getDeclaredMethod("createChannel", new Class[] { Object.class });
         STRING_ARG = clazz.getDeclaredMethod("createChannel", new Class[] { String.class });
         SIMPLE_MUX = clazz.getDeclaredMethod("createMultiplexerChannel", new Class[] { String.class, String.class });
         COMPLEX_MUX = clazz.getDeclaredMethod("createMultiplexerChannel", new Class[] { String.class, String.class, boolean.class, String.class });
      }
      catch (NoSuchMethodException nsme)
      {
         log.error("Reflection failure", nsme);
      }
   }
   
   private JChannelFactory factory1;
   private JChannelFactory factory2;
   private Channel channel1;
   private Channel channel2;
   private String jgroups_bind_addr;
   private ClassLoader testLoader;
   
   /**
    * Create a new JChannelFactoryUnitTestCase.
    * 
    * @param name
    */
   public JChannelFactoryClassLoaderLeakTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      if (COMPLEX_MUX == null)
         throw new IllegalStateException("Reflection failed in class init; see logs");
      
      super.setUp();
      
      testLoader = new ClassLoader(Thread.currentThread().getContextClassLoader()){};
      
      String jgroups_bind_addr = System.getProperty("jgroups.bind_addr");
      if (jgroups_bind_addr == null)
      {
         System.setProperty("jbosstest.cluster.node0", System.getProperty("jbosstest.cluster.node0", "localhost"));
      }
      
      factory1 = new TestClassLoaderJChannelFactory();
      factory1.setMultiplexerConfig("cluster/channelfactory/stacks.xml");
      factory1.setAssignLogicalAddresses(false);
      factory1.setExposeChannels(false);
      factory1.setManageReleasedThreadClassLoader(true);
      factory1.create();
      factory1.start();
      factory2 = new TestClassLoaderJChannelFactory();
      factory2.setMultiplexerConfig("cluster/channelfactory/stacks.xml");
      factory2.setAssignLogicalAddresses(false);
      factory2.setExposeChannels(false);
      factory2.setManageReleasedThreadClassLoader(true);
      factory2.create();
      factory2.start();
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      testLoader = null;
      
      if (jgroups_bind_addr == null)
         System.clearProperty("jgroups.bind_addr");
      
      if (channel1 != null && channel1.isOpen())
         channel1.close();
      
      if (channel2 != null && channel2.isOpen())
         channel2.close();
      
      if (factory1 != null)
      {
         factory1.stop();
         factory1.destroy();
      }
      if (factory2 != null)
      {
         factory2.stop();
         factory2.destroy();
      }
   }
   
   public void testClassLoaderLeakObjectShared() throws Exception
   {  
      Object[] args1 = { factory1.getConfig("shared1") };
      Object[] args2 = { factory2.getConfig("shared2") };
      classloaderLeakTest(OBJECT_ARG, args1, args2);
   }
   
   public void testClassLoaderLeakObjectUnshared() throws Exception
   {  
      Object[] args1 = { factory1.getConfig("unshared1") };
      Object[] args2 = { factory2.getConfig("unshared2") };
      classloaderLeakTest(OBJECT_ARG, args1, args2);
   }
   
   public void testClassLoaderLeakStringShared() throws Exception
   {  
      Object[] args1 = { "shared1" };
      Object[] args2 = { "shared2" };
      classloaderLeakTest(STRING_ARG, args1, args2);
   }
   
   public void testClassLoaderLeakStringUnshared() throws Exception
   {  
      Object[] args1 = { "unshared1" };
      Object[] args2 = { "unshared2" };
      classloaderLeakTest(STRING_ARG, args1, args2);
   }
   
   public void testClassLoaderLeakSimpleMuxShared() throws Exception
   {  
      Object[] args1 = { "shared1", "leaktest" };
      Object[] args2 = { "shared2", "leaktest" };
      classloaderLeakTest(SIMPLE_MUX, args1, args2);
   }
   
   public void testClassLoaderLeakSimpleMuxUnshared() throws Exception
   {  
      Object[] args1 = { "unshared1", "leaktest" };
      Object[] args2 = { "unshared2", "leaktest" };
      classloaderLeakTest(SIMPLE_MUX, args1, args2);
   }
   
   public void testClassLoaderLeakComplexMuxShared() throws Exception
   {  
      Object[] args1 = { "shared1", "leaktest", Boolean.FALSE, null };
      Object[] args2 = { "shared2", "leaktest", Boolean.FALSE, null };
      classloaderLeakTest(COMPLEX_MUX, args1, args2);
   }
   
   public void testClassLoaderLeakComplexMuxUnshared() throws Exception
   {  
      Object[] args1 = { "unshared1", "leaktest", Boolean.FALSE, null };
      Object[] args2 = { "unshared2", "leaktest", Boolean.FALSE, null };
      classloaderLeakTest(COMPLEX_MUX, args1, args2);
   }
   
   public void testClassLoaderLeakNonConcurrent() throws Exception
   {  
      Object[] args1 = { "nonconcurrent1" };
      Object[] args2 = { "nonconcurrent2" };
      classloaderLeakTest(STRING_ARG, args1, args2);
   }
   
   private void classloaderLeakTest(Method factoryMeth, Object[] factory1Args, 
                                    Object[] factory2Args) 
      throws Exception
   {
      int numThreads = 8;
      int numLoops = 100;
      
      Semaphore semaphore = new Semaphore(numThreads);
      
      ThreadGroup runnerGroup = new ThreadGroup("TestRunners");
      
      ClassLoader ours = Thread.currentThread().getContextClassLoader();
      
      // The classloader we want channel threads to use
      ClassLoaderLeakHandler handler = new ClassLoaderLeakHandler(testLoader, semaphore, runnerGroup);
      
      MessageDispatcher[] dispatchers = new MessageDispatcher[2];
      
//      Thread.currentThread().setContextClassLoader(testLoader);    
//      try
//      {
         channel1 = (Channel) factoryMeth.invoke(factory1, factory1Args);
         dispatchers[0] = new MessageDispatcher(channel1, handler, handler, handler);
         
         channel1.connect("leaktest");
         assertEquals("No classloader leak on channel1 connect", null, handler.getLeakedClassLoader());
         
         channel2 = (Channel) factoryMeth.invoke(factory2, factory2Args);
         dispatchers[1] = new MessageDispatcher(channel2, handler, handler, handler);
         
         channel2.connect("leaktest");
         assertEquals("No classloader leak on channel2 connect", null, handler.getLeakedClassLoader());
//      }
//      finally
//      {
//         Thread.currentThread().setContextClassLoader(ours);
//      }
      
      log.info("Channels connected");
      
      ClassLoaderLeakRunner[] runners = new ClassLoaderLeakRunner[numThreads];      
      
      for (int i = 0; i < runners.length; i++)
      {
         MessageDispatcher disp = dispatchers[i % 2];
            
         runners[i] = new ClassLoaderLeakRunner(disp, numLoops, runnerGroup, semaphore);         
      }

      semaphore.acquire(numThreads);
      
      for (int i = 0; i < runners.length; i++)
      {
         runners[i].start();
      }
      
      semaphore.release(numThreads);
      
      try
      {
         assertTrue("messages received within 15 seconds", semaphore.tryAcquire(numThreads, 15, TimeUnit.SECONDS));
         
         log.info("Messages received");
      }
      finally
      {
         for (int i = 0; i < runners.length; i++)
         {
            runners[i].stop();
         }
      }
      
      log.info("Sender threads stopped");
      
      assertEquals("No classloader leak", null, handler.getLeakedClassLoader());
   }

   private class ClassLoaderLeakRunner implements Runnable
   {
      private Thread thread;
      private final MessageDispatcher dispatcher;
      private final int numMsgs;
      private final ThreadGroup threadGroup;
      private final Semaphore semaphore;
      private boolean stopped;
      private Exception exception;
      
      ClassLoaderLeakRunner(MessageDispatcher dispatcher, int numMsgs, ThreadGroup group, Semaphore semaphore)
      {
         this.dispatcher = dispatcher;
         this.numMsgs = numMsgs;
         this.threadGroup = group;
         this.semaphore = semaphore;
      }
      
      public void run()
      {
         boolean acquired = false;
         ClassLoader cl = new ClassLoader(Thread.currentThread().getContextClassLoader()){};
         try
         {
            semaphore.acquire();         
            acquired = true;
            
            log.info(Thread.currentThread().getName() + " starting");        
            
            Thread.currentThread().setContextClassLoader(cl);
            for (int i = 0; i < numMsgs && !stopped && !Thread.interrupted(); i++)
            {
               Message msg = new Message(null, null, String.valueOf(i));
               // sending this way calls receive()
               dispatcher.send(msg);
               // sending this way calls handle()
               dispatcher.castMessage(null, msg, GroupRequest.GET_ALL, 0, false);
            }
            
            log.info(Thread.currentThread().getName() + " done");
         }
         catch (Exception e)
         {
            this.exception = e;
         }
         finally
         {
            if (acquired)
               semaphore.release();
            
            Thread.currentThread().setContextClassLoader(cl.getParent());
         }
      }

      public Exception getException()
      {
         return exception;
      }

      public void start()
      {
         thread = new Thread(this.threadGroup, this);
         thread.setDaemon(true);
         thread.start();
      }
      
      public void stop()
      {
         stopped = true;
         if (thread != null && thread.isAlive())
         {
            try
            {
               thread.join(100);
            }
            catch (InterruptedException e)
            {
            }
            if (thread.isAlive())
               thread.interrupt();
         }
      }
   }
   
   private class ClassLoaderLeakHandler 
      implements MembershipListener, MessageListener, RequestHandler
   {
      private final Semaphore semaphore;
      private final ClassLoader expected;
      private final ThreadGroup runnerGroup;
      private final Thread main;
      private ClassLoader leakedClassLoader;
      private final int numPermits;
      
      ClassLoaderLeakHandler(ClassLoader expected, Semaphore semaphore, ThreadGroup runnerGroup)
      {
         this.expected = expected;
         this.semaphore = semaphore;
         this.runnerGroup = runnerGroup;
         this.numPermits = this.semaphore.availablePermits();
         this.main = Thread.currentThread();
      }

      public Object handle(Message msg)
      {
         log.debug("handled(): " + msg.getObject());
         checkClassLoader(true, "handle()");
         return null;
      }

      public void block()
      {
         checkClassLoader(false, "block()");
      }

      public void suspect(Address suspected_mbr)
      {
         checkClassLoader(false, "suspect()");         
      }

      public void viewAccepted(View new_view)
      {
         checkClassLoader(false, "viewAccepted()");     
         log.info("viewAccepted(): " + new_view);
      }

      public byte[] getState()
      {
         checkClassLoader(false, "getState()");  
         return new byte[1];
      }

      public void receive(Message msg)
      {
         checkClassLoader(false, "receive()");  
      }

      public void setState(byte[] state)
      {
         checkClassLoader(false, "setState()");   
      }

      public ClassLoader getLeakedClassLoader()
      {
         return leakedClassLoader;
      } 
      
      private void checkClassLoader(boolean fromHandle, String method)
      {         
         if (leakedClassLoader == null) // ignore msgs once we found a leak
         {
            // ignore runner threads that loop all the way back up 
            Thread current = Thread.currentThread();
            if (current == main || current.getThreadGroup().equals(runnerGroup))
            {               
               return; 
            }
            
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            if (!expected.equals(tccl))               
            {
               leakedClassLoader = tccl;
               semaphore.release(numPermits);
               log.info("ClassLoader leaked in " + method + ": " + tccl + " leaked to " + Thread.currentThread().getName());
            }
         }         
      }
      
   }
   
   private class TestClassLoaderJChannelFactory
   extends JChannelFactory
   {

      @Override
      protected ClassLoader getDefaultChannelThreadContextClassLoader()
      {
         return testLoader;
      }
      
   }
}
