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
package org.jboss.test.threading.mbean;

import javax.naming.InitialContext;
import org.jboss.test.threading.interfaces.EJBThreads;
import org.jboss.test.threading.interfaces.EJBThreadsHome;
import java.rmi.RemoteException;

import java.util.Random;

/**
*   This test is there to make sure that the multithreaded version doesn't lock the container
*   It works in VM and spawns many threads that will ping the server. 
*
*   @see <related>
*   @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
*   @version $Revision: 81036 $
*   
*   Revisions:
*
*   20010524 marc fleury: Initial version
*/

public class Threads
   implements ThreadsMBean
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());
   
   // Constants -----------------------------------------------------
	
   // Attributes ----------------------------------------------------
	
   private int numberOfThreads = 0;
   private int loops = 10;
   private long wait = 100;
   public  boolean runMe = true;
   private Random random = new Random();
   private Runnable test;
   private int threadsFinished = 0;
   // Static --------------------------------------------------------
	
   // Constructors --------------------------------------------------
	
	// Public --------------------------------------------------------
	
   public void setWait(long wait) {this.wait = wait;}
   public long getWait() {return wait;}
	
	
   public void setLoops(int loops) {this.loops = loops;}
   public int getLoops() {return loops;}
	
   public void setNumberOfThreads(int numberOfThreads) 
   {
      if (this.numberOfThreads > 0) stopMe();
			
      this.numberOfThreads=numberOfThreads;
      //restart
      try {
				
         if (numberOfThreads> 0) startMe();
      } 
      catch (Exception e) 
      {
         log.debug("failed", e);
      }
   }
   public int getNumberOfThreads() { return numberOfThreads;}
	
   public void startMe()
      throws Exception
   {
      runMe = true;
      threadsFinished = 0;
      if (numberOfThreads >0) {
			
         for (int i = 0; i < numberOfThreads ; i++) 
         {
            Thread t = new Thread(new Test());
            log.debug("started new thread " +t.hashCode());
				
            t.start();
			
         };
      }
   }
	
   public void stopMe()
   {
      log.debug("Stop called");
      runMe = false;
   };
	
   public class Test implements Runnable {
		
      public void run() {
			
         try {
				
            InitialContext ic = new InitialContext();
				
            EJBThreadsHome testHome = (EJBThreadsHome) ic.lookup("threads");
				
            EJBThreads ejbTest;
            while(runMe) 
            {
					
               ejbTest = null;
					
               try {
						
                  ejbTest = testHome.findByPrimaryKey("test1");
               } 
               catch (Exception e) 
               {
                  // Bean wasn't found create it 
                  try {
                     ejbTest = testHome.create("test1");
                  }
					
                  catch (Exception e2) 
                  {
                     log.debug("****Create exception: " + e2);
                  }
               }
					
               if (ejbTest != null) try {
						
                  // get a random value between 1 and 100
                  int value = random.nextInt(100);
						
                  // 10% removal
                  if (value <10) {
                     ejbTest.remove();
                  }
                  // 35% normal
                  else if (value<45) {
                     ejbTest.test();
                  }
                  // 15% business exception
                  else if (value<60) {
                     ejbTest.testBusinessException();
                  }
                  // 15 % runtime excpetion
                  else if (value <75) {
                     ejbTest.testRuntimeException();
                  }
                  // 15 % nonTransactional
                  else if (value <90) {
                     ejbTest.testNonTransactional();
                  }
                  // 10% timeout
                  else {
                     ejbTest.testTimeOut();
                  }
						
                  synchronized (this) {
                     //Thread.currentThread().yield();
                     this.wait(wait);
                  }
               }
               catch (NullPointerException ignored) {}
               catch (RemoteException ignored) {}
               catch (Exception ex)
               {
                  log.debug("***Exception thrown: " + ex);
               }
				
            } // while(runMe)
            log.debug(Thread.currentThread() + " is finished!!!!");
            log.debug("Num threads finished is: " + ++threadsFinished);
         }
         catch (Exception e) {log.debug("Exception for thread"+Thread.currentThread()); log.debug("failed", e);}
		
      }
   }
}
