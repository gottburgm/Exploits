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
package org.jboss.deployment.scanner;

import javax.management.ObjectName;

import org.jboss.deployment.Deployer;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanProxyInstance;
import org.jboss.system.MissingAttributeException;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.NullArgumentException;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * An abstract support class for implementing a deployment scanner.
 *
 * <p>Provides the implementation of period-based scanning, as well
 *    as Deployer integration.
 *
 * <p>Sub-classes only need to implement {@link DeploymentScanner#scan}.
 *
 * @version <tt>$Revision: 81033 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 */
public abstract class AbstractDeploymentScanner extends ServiceMBeanSupport
   implements DeploymentScanner, DeploymentScannerMBean
{
   /** The scan period in milliseconds */
   protected long scanPeriod = 5000;

   /** True if period based scanning is enabled. */
   protected boolean scanEnabled = true;

   /** The stop timeout */
   protected long stopTimeOut = 60000;
   
   /** A proxy to the deployer we are using. */
   protected Deployer deployer;

   protected MainDeployerMBean mainDeployer;

   /** The scanner thread. */
   protected ScannerThread scannerThread;

   /** HACK: Shutdown hook to get around problems with system service shutdown ordering. */
   private Thread shutdownHook;
   

   /////////////////////////////////////////////////////////////////////////
   //                           DeploymentScanner                         //
   /////////////////////////////////////////////////////////////////////////

   public void setDeployer(final ObjectName deployerName)
   {
      if (deployerName == null)
         throw new NullArgumentException("deployerName");

      deployer = (Deployer)
         MBeanProxyExt.create(Deployer.class, deployerName, server);
   }

   public ObjectName getDeployer()
   {
      return ((MBeanProxyInstance)deployer).getMBeanProxyObjectName();
   }

   /**
    * Period must be >= 0.
    */
   public void setScanPeriod(final long period)
   {
      if (period < 0)
         throw new IllegalArgumentException("ScanPeriod must be >= 0; have: " + period);

      this.scanPeriod = period;
   }

   public long getScanPeriod()
   {
      return scanPeriod;
   }

   public void setScanEnabled(final boolean flag)
   {
      this.scanEnabled = flag;
   }

   public boolean isScanEnabled()
   {
      return scanEnabled;
   }

   public long getStopTimeOut()
   {
      return stopTimeOut;
   }

   public void setStopTimeOut(long stopTimeOut)
   {
      this.stopTimeOut = stopTimeOut;
   }

   /** This is here to work around a bug in the IBM vm that causes an
    * AbstractMethodError to be thrown when the ScannerThread calls scan.
    * @throws Exception
    */
   public abstract void scan() throws Exception;

   /////////////////////////////////////////////////////////////////////////
   //                           Scanner Thread                            //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Should use Timer/TimerTask instead?  This has some issues with
    * interaction with ScanEnabled attribute.  ScanEnabled works only
    * when starting/stopping.
    */
   public class ScannerThread
      extends Thread
   {
      /** We get our own logger. */
      protected Logger scannerLog = Logger.getLogger(ScannerThread.class);

      /** True if the scan loop should run. */
      protected SynchronizedBoolean enabled = new SynchronizedBoolean(false);

      /** True if we are shutting down. */
      protected SynchronizedBoolean shuttingDown = new SynchronizedBoolean(false);

      /** Lock/notify object. */
      protected Object lock = new Object();

      /** Active synchronization. */
      protected SynchronizedBoolean active = new SynchronizedBoolean(false);

      public ScannerThread(boolean enabled)
      {
         super("ScannerThread");

         this.enabled.set(enabled);
      }

      public void setEnabled(boolean enabled)
      {
         this.enabled.set(enabled);

         synchronized (lock)
         {
            lock.notifyAll();
         }
         
         scannerLog.debug("Notified that enabled: " + enabled);
      }

      public void shutdown()
      {
         enabled.set(false);
         shuttingDown.set(true);

         synchronized (lock)
         {
            lock.notifyAll();
         }

         scannerLog.debug("Notified to shutdown");

         // jason: shall we also interrupt this thread?
      }
    
      public void run()
      {
         scannerLog.debug("Running");

         active.set(true);
         try
         {
            while (shuttingDown.get() == false)
            {
               // If we are not enabled, then wait
               if (enabled.get() == false)
               {
                  synchronized (active)
                  {
                     active.set(false);
                     active.notifyAll();
                  }
                  try
                  {
                     scannerLog.debug("Disabled, waiting for notification");
                     synchronized (lock)
                     {
                        lock.wait();
                     }
                  }
                  catch (InterruptedException ignore)
                  {
                  }
                  active.set(true);
               }

               loop();
            }
         }
         finally
         {
            synchronized (active)
            {
               active.set(false);
               active.notifyAll();
            }
         }
         
         scannerLog.debug("Shutdown");
      }

      protected void waitForInactive()
      {
         boolean interrupted = false;
         synchronized (active)
         {
            try
            {
               if (active.get() && stopTimeOut > 0)
                  active.wait(stopTimeOut);
            }
            catch (InterruptedException ignored)
            {
               interrupted = true;
            }
         }
         if (interrupted)
            Thread.currentThread().interrupt();
      }
      
      public void doScan()
      {
         // Scan for new/removed/changed/whatever
         try {
            scan();
         }
         catch (Exception e) {
            scannerLog.error("Scanning failed; continuing", e);
         }
      }
      
      protected void loop()
      {
         while (enabled.get() && shuttingDown.get() == false)
         {
            doScan();

            // Sleep for scan period
            try
            {
               scannerLog.trace("Sleeping...");
               Thread.sleep(scanPeriod);
            }
            catch (InterruptedException ignore) {}
         }
      }
   }


   /////////////////////////////////////////////////////////////////////////
   //                     Service/ServiceMBeanSupport                     //
   /////////////////////////////////////////////////////////////////////////

   protected void createService() throws Exception
   {
      if (deployer == null)
         throw new MissingAttributeException("Deployer");
      mainDeployer = (MainDeployerMBean)MBeanProxyExt.create(MainDeployerMBean.class, MainDeployerMBean.OBJECT_NAME, server);
      // setup + start scanner thread
      scannerThread = new ScannerThread(false);
      scannerThread.setDaemon(true);
      scannerThread.start();
      log.debug("Scanner thread started");

      // HACK
      // 
      // install a shutdown hook, as the current system service shutdown
      // mechanism will not call this until all other services have stopped.
      // we need to know soon, so we can stop scanning to try to avoid
      // starting new services when shutting down

      final ScannerThread _scannerThread = scannerThread;
      shutdownHook = new Thread("DeploymentScanner Shutdown Hook")
         {
            ScannerThread thread = _scannerThread;
            
            public void run()
            {
               thread.shutdown();
            }
         };
      
      try
      {
         Runtime.getRuntime().addShutdownHook(shutdownHook);
      }
      catch (Exception e)
      {
         log.warn("Failed to add shutdown hook", e);
      }
   }

   protected void startService() throws Exception 
   {
      synchronized( scannerThread )
      {
         // scan before we enable the thread, so JBoss version shows up afterwards
         scannerThread.doScan();

         // enable scanner thread if we are enabled
         scannerThread.setEnabled(scanEnabled);
      }
   }
   
   protected void stopService() throws Exception 
   {
      // disable scanner thread
      if( scannerThread != null )
      {
         scannerThread.setEnabled(false);
         scannerThread.waitForInactive();
      }
   }

   protected void destroyService() throws Exception 
   {
      // drop our ref to deployer, so scan will fail
      deployer = null;

      // shutdown scanner thread
      if( scannerThread != null )
      {
         synchronized( scannerThread )
         {
            scannerThread.shutdown();
         }
      }

      // HACK
      // 
      // remove the shutdown hook, we don't need it anymore
      try
      {
         Runtime.getRuntime().removeShutdownHook(shutdownHook);
      }
      catch (Exception ignore)
      {
      } // who cares really

      // help gc
      shutdownHook = null;
      scannerThread = null;
   }
}
