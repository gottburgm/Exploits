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
package org.jboss.system.pm;

import org.jboss.mx.persistence.AttributePersistenceManager;
import org.jboss.system.ServiceMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.w3c.dom.Element;

/**
 * AttributePersistenceService
 * 
 * Works in conjuction with
 * org.jboss.mx.persistence.DelegatingPersistenceManager
 * that upon creation, consults this service for an
 * implementations of the interface
 * org.jboss.mx.persistence.AttributePersistenceManager
 * 
 * The service will instantiate and initialize the actual
 * persistence manager and return it whenever asked.
 * 
 * It also introduces the notion of a 'version', when
 * creating the persistent manager, so that persisted
 * data from different versions are kept separately.
 * 
 * The service can be stopped, the version can be changed
 * and the service re-started, resulting in a new
 * persistent manager being instantiated and all
 * XMBeans created thereafter using this instead.
 * 
 * So the goal is really to have a way to plug-in
 * external and manageable persistent managers that
 * that support versioning, too.
 *  
 * @jmx:mbean
 *    extends="org.jboss.system.ServiceMBean"
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
**/
public class AttributePersistenceService
   extends ServiceMBeanSupport
   implements AttributePersistenceServiceMBean
{
   // Constants -----------------------------------------------------
   
   /** The default AttributePersistenceManager implementation */
   public static final String DEFAULT_APM = "org.jboss.system.pm.XMLAttributePersistenceManager";
   
   /** The default behaviour for destroying the APM when stopping */ 
   public static final boolean DEFAULT_DESTROY_APM_ON_STOP = false;
   
   // Private Data --------------------------------------------------

   /** the actual AttributePersistenceManager (APM) implementation */ 
   private AttributePersistenceManager apm 	= null;
   
   /** the APM XML configuration */
   private Element apmConfig 				= null;
   
   /** the name of the APM implementation class */
   private String  apmClass 				= DEFAULT_APM;
   
   /** whether to destroy() the APM upon stop, or leave it servicing XMBeans that use it */
   private boolean apmDestroyOnStop 		= DEFAULT_DESTROY_APM_ON_STOP;
   
   /** indicate the active version configured to the APM */
   private String  versionTag 				= null;   
   
   // Constructors -------------------------------------------------
    
   /**
    * Constructs a <tt>AttributePersistenceService</tt>.
    */
   public AttributePersistenceService()
   {
      // setup logger
      super(AttributePersistenceService.class);
   }

   // Attributes ----------------------------------------------------

   /**
    * @jmx:managed-attribute
    * @return Returns the versionTag.
    */
   public String getVersionTag() 
   {
      return versionTag;
   }
   
   /**
    * @jmx:managed-attribute
    * @param versionTag The versionTag to set.
    */
   public void setVersionTag(String versionTag) 
   {
      checkNotStarted();
      this.versionTag = versionTag;
   }
   
   /**
    * @jmx:managed-attribute
    * @return Returns the apmClass.
    */
   public String getAttributePersistenceManagerClass() 
   {
      return apmClass;
   }
   
   /**
    * @jmx:managed-attribute
    * @param apmClass The apmClass to set.
    */
   public void setAttributePersistenceManagerClass(String apmClass) 
   {
      checkNotStarted();
      this.apmClass = apmClass;
   }
   
   /**
    * @jmx:managed-attribute
    * @return Returns the apmConfig.
    */
   public Element getAttributePersistenceManagerConfig()
   {
      return apmConfig;
   }
   
   /**
    * @jmx:managed-attribute
    * @param apmConfig The apmConfig to set.
    */
   public void setAttributePersistenceManagerConfig(Element apmConfig)
   {
      checkNotStarted();
      this.apmConfig = apmConfig;
   }
   
   /**
    * @jmx:managed-attribute
    * @return Returns the apmDestroyOnStop.
    */
   public boolean getApmDestroyOnServiceStop()
   {
      return apmDestroyOnStop;
   }
   /**
    * @jmx:managed-attribute
    * @param apmDestroyOnStop The apmDestroyOnStop to set.
    */
   public void setApmDestroyOnServiceStop(boolean apmDestroyOnStop)
   {
      checkNotStarted();
      this.apmDestroyOnStop = apmDestroyOnStop;
   }
   
   // ServiceMBeanSupport overrides ---------------------------------

   public void startService()
      throws Exception
   {
      // Instantiate the APM trough the MBeanServer
      this.apm = (AttributePersistenceManager)this.getServer().instantiate(this.apmClass);
      
      // Initialize it
      this.apm.create(this.versionTag, this.apmConfig);
   }

   public void stopService()
   {
      // if we destroy it non can be used it
      if (this.apmDestroyOnStop) {
         this.apm.destroy();
      }
      
      // forget about this apm
      this.apm = null;
   }
   
   // Operations ----------------------------------------------------
   
   /**
    * @jmx:managed-operation
    */
   public AttributePersistenceManager apmCreate()
   {
      checkStarted();
      
      return this.apm;
   }
   
   /**
    * @jmx:managed-operation
    */
   public boolean apmExists(String id)
      throws Exception
   {
      checkStarted();
      
      return this.apm.exists(id);
   }
   
   /**
    * @jmx:managed-operation
    */
   public void apmRemove(String id)
      throws Exception
   {
      checkStarted();
      
      this.apm.remove(id);
   }
   
   /**
    * @jmx:managed-operation
    */
   public void apmRemoveAll()
      throws Exception
   {
      checkStarted();
      
      this.apm.removeAll();
   }
   
   /**
    * @jmx:managed-operation
    */
   public String[] apmListAll()
      throws Exception
   {
      checkStarted();
      
      return this.apm.listAll();
   }

   /**
    * @jmx:managed-operation
    */
   public String apmListAllAsString()
      throws Exception
   {
      checkStarted();
      
      StringBuffer sbuf = new StringBuffer(1024);
      String[] list = this.apm.listAll();
      
      for (int i = 0; i < list.length; i++) {
         sbuf.append(list[i]).append("\n");
      }
      return sbuf.toString();
   }
   
   // Private -------------------------------------------------------

   private void checkStarted()
   {
      int state = this.getState();
      
      if (state != ServiceMBean.STARTED) {
         throw new IllegalStateException("Cannot perform operations unless service is started");
      }
   }
   
   private void checkNotStarted()
   {
      int state = this.getState();
      
      if (state == ServiceMBean.STARTING || state == ServiceMBean.STARTED) {
         throw new IllegalStateException("Cannot modify attributes while service is started");
      }
   }
}