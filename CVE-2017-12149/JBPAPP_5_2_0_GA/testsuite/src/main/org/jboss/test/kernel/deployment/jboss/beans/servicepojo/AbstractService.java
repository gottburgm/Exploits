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
package org.jboss.test.kernel.deployment.jboss.beans.servicepojo;

/**
 * Supposedly a base class for services
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public abstract class AbstractService
{
   public static final String CONSTRUCTED = "CONSTRUCTED";
   public static final String CREATED     = "CREATED";
   public static final String STARTED     = "STARTED";
   public static final String STOPPED     = "STOPPED";
   public static final String DESTROYED   = "DESTROYED";
   public static final String FAILED      = "FAILED";
   
   // Protected -----------------------------------------------------
   
   protected String name;
   protected String state = CONSTRUCTED;
   
   // Constructor ---------------------------------------------------
   
   public AbstractService(String name)
   {
      this.name = name;
      log("CTOR");
   }
   
   // Accessors -----------------------------------------------------

   public String getName()
   {
      return name;
   }
   
   public String getState()
   {
      return state;
   }
   
   // Lifecycle -----------------------------------------------------
   
   public void create() throws Exception
   {
      state = CREATED;
      log("create()");
   }
   
   public void start() throws Exception
   {
      state = STARTED;
      log("start()");
   }
   
   public void stop() throws Exception
   {
      state = STOPPED;
      log("stop()");
   }
   
   public void destroy() throws Exception
   {
      state = DESTROYED;
      log("destroy()");
   }
   
   // Just to avoid org.jboss.logging -------------------------------
   
   public void log(Object message)
   {
      System.out.println(getName() + " - " + message);
   }
   
   // Overrides -----------------------------------------------------
   
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer();
      sbuf
      .append(getClass().getName())
      .append("[ name=").append(name)
      .append(", state=").append(state)
      .append(" ]");
      
      return sbuf.toString();
   }
}
