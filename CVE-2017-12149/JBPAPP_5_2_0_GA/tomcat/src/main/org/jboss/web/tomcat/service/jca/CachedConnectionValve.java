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
package org.jboss.web.tomcat.service.jca;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.ResourceException;
import javax.servlet.ServletException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.catalina.util.LifecycleSupport;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.servlet.http.HttpEvent;

/**
 * This valve checks for unclosed connections on a servlet request
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CachedConnectionValve extends ValveBase implements Lifecycle
{
   /**
    * The log
    */
   private static final Logger log = Logger.getLogger(CachedConnectionValve.class);

   /**
    * The info string for this Valve
    */
   private static final String info = "CachedConnectionValve/1.0";

   /**
    * Valve-lifecycle helper object
    */
   protected LifecycleSupport support = new LifecycleSupport(this);

   /**
    * The object name of the cached connection manager
    */
   protected String ccmName;

   /**
    * The cached connection manager
    */
   protected CachedConnectionManager ccm;

   /**
    * The object name of the transaction manager service
    */
   protected String tmName;

   /**
    * The transaction manager
    */
   protected TransactionManager tm;

   /**
    * The unshareable resources
    */
   protected Set unsharableResources = new HashSet();

   /**
    * Create a new valve
    *
    * @param ccm the cached connection manager for the valve
    */
   public CachedConnectionValve()
   {
      super();
   }

   /**
    * Get information about this Valve.
    */
   public String getInfo()
   {
      return info;
   }

   /**
    * Get the cached connection manager object name
    */
   public String getCachedConnectionManagerObjectName()
   {
      return ccmName;
   }

   /**
    * Set the cached connection manager object name
    */
   public void setCachedConnectionManagerObjectName(String ccmName)
   {
      this.ccmName = ccmName;
   }

   /**
    * Get the transaction manager object name
    */
   public String getTransactionManagerObjectName()
   {
      return tmName;
   }

   /**
    * Set the transaction manager object name
    */
   public void setTransactionManagerObjectName(String tmName)
   {
      this.tmName = tmName;
   }

   public void invoke(Request request, Response response) throws IOException, ServletException
   {
      if(ccm == null)
         throw new IllegalStateException("Uncomment the dependency on CachedConnectionManager"
               + " in META-INF/jboss-service.xml of jbossweb-tomcatxxx.sar");
      try
      {
         ccm.pushMetaAwareObject(this, unsharableResources);
         try
         {
            getNext().invoke(request, response);
         }
         finally
         {
            try
            {
               ccm.popMetaAwareObject(unsharableResources);
            }
            finally
            {
               checkTransactionComplete(request);
            }
         }
      }
      catch (ResourceException e)
      {
         throw new ServletException("Error invoking cached connection manager", e);
      }
   }

   public void event(Request request, Response response, HttpEvent event)
      throws IOException, ServletException
   {
      try
      {
         ccm.pushMetaAwareObject(this, unsharableResources);
         try
         {
            getNext().event(request, response, event);
         }
         finally
         {
            try
            {
               ccm.popMetaAwareObject(unsharableResources);
            }
            finally
            {
               checkTransactionComplete(request);
            }
         }
      }
      catch (ResourceException e)
      {
         throw new ServletException("Error invoking cached connection manager", e);
      }
   }

   // Lifecycle-interface
   public void addLifecycleListener(LifecycleListener listener)
   {
      support.addLifecycleListener(listener);
   }

   public void removeLifecycleListener(LifecycleListener listener)
   {
      support.removeLifecycleListener(listener);
   }

   public LifecycleListener[] findLifecycleListeners()
   {
      return support.findLifecycleListeners();
   }

   public void start() throws LifecycleException
   {
      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ccm = (CachedConnectionManager) server.getAttribute(new ObjectName(ccmName), "Instance");
         tm = (TransactionManager) server.getAttribute(new ObjectName(tmName), "TransactionManager");
      }
      catch (Exception e)
      {
         throw new LifecycleException(e);
      }
      
      // TODO unshareable resources
      support.fireLifecycleEvent(START_EVENT, this);
   }

   public void stop() throws LifecycleException
   {
      support.fireLifecycleEvent(STOP_EVENT, this);
      unsharableResources.clear();
   }

   protected void checkTransactionComplete(Request request)
   {
      int status = Status.STATUS_NO_TRANSACTION;

      try
      {
         status = tm.getStatus();
      }
      catch (SystemException ex)
      {
         log.error("Failed to get status", ex);
      }

      try
      {
         switch (status)
         {
            case Status.STATUS_ACTIVE:
            case Status.STATUS_COMMITTING:
            case Status.STATUS_MARKED_ROLLBACK:
            case Status.STATUS_PREPARING:
            case Status.STATUS_ROLLING_BACK:
               try
               {
                  tm.rollback();
               }
               catch (Exception ex)
               {
                  log.error("Failed to rollback", ex);
               }
               // fall through...
            case Status.STATUS_PREPARED:
               String servletName = "<Unknown>";
               try
               {
                  Wrapper servlet = request.getWrapper();
                  if (servlet != null)
                  {
                     servletName = servlet.getName();
                     if (servlet.getJspFile() != null)
                        servletName = servlet.getJspFile();
                  }
               }
               catch (Throwable ignored)
               {
               }

               String msg = "Application error: " + servletName + " did not complete its transaction";
               log.error(msg);
         }
      }
      finally
      {
         try
         {
            Transaction tx = tm.suspend();
            if (tx != null)
            {
               String servletName = "<Unknown>";
               try
               {
                  Wrapper servlet = request.getWrapper();
                  if (servlet != null)
                  {
                     servletName = servlet.getName();
                     if (servlet.getJspFile() != null)
                        servletName = servlet.getJspFile();
                  }
               }
               catch (Throwable ignored)
               {
               }
               String msg = "Application error: " + servletName +
                      " did not complete its transaction suspended tx=" + tx ;
               log.error(msg);
            }
         }
         catch (SystemException ex)
         {
            log.error("Failed to suspend transaction", ex);
         }
      }
   }

}
