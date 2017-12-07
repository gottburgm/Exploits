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
package org.jboss.test.jmx.simpleservice;

import org.jboss.system.ServiceMBeanSupport;

/**
 * A simple mbean derived from ServiceMBean/ServiceMBeanSupport.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class SimpleService extends ServiceMBeanSupport
   implements SimpleServiceMBean
{
   // Private Data --------------------------------------------------
   
   /** A String attribute */
   private String aString;
  
   private boolean createCalled;
   private boolean startCalled;
   private boolean stopCalled;
   private boolean destroyCalled;
   
  // Constructors --------------------------------------------------
  
  /**
   * CTOR
  **/
  public SimpleService()
  {
     // empty
  }

  // Attributes -----------------------------------------------------
  
  public void setAStringAttr(String s)
  {
     this.aString = s;
  }
  
  public String getAStringAttr()
  {
     return aString;
  }
  
  public boolean getCreateCalled()
  {
     return createCalled;
  }
  
  public boolean getStartCalled()
  {
     return startCalled;
  }
  
  public boolean getStopCalled()
  {
     return stopCalled;
  }
  
  public boolean getDestroyCalled()
  {
     return destroyCalled;
  }
  
  // Operations -----------------------------------------------------
  
  public void resetLifecycleMemory()
  {
     createCalled = false;
     startCalled = false;
     stopCalled = false;
     destroyCalled = false;
  }
  
  // Lifecycle ------------------------------------------------------
  
  protected void createService() throws Exception
  {
     createCalled = true;
  }
  
  protected void startService() throws Exception
  {
     startCalled = true;
  }
  
  protected void stopService()
  {
     stopCalled = true;
  }
  
  protected void destroyService()
  {
     destroyCalled = true;
  }
  
}
