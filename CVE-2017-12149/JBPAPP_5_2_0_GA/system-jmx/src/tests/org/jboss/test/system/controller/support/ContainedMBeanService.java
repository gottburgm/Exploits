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
package org.jboss.test.system.controller.support;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Hashtable;

import javax.management.ObjectName;

import org.jboss.system.ServiceMBeanSupport;

/**
 * An mbean that creates and starts mbeans outside of the SARDeployer.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class ContainedMBeanService extends ServiceMBeanSupport
   implements ContainedMBeanServiceMBean
{
   private String aString;
   private ObjectName simpleName;
   private Simple simpleMBean;

   /** Pass through AString for the container SimpleMBean */
   public String getAString()
   {
      return this.aString;
   }
   /** Pass through AString for the container SimpleMBean */
   public void setAString(String string)
   {
      this.aString = string;
   }

   /**
    * Create and register the contained SimpleMBean
    */
   @Override
   @SuppressWarnings("unchecked")
   protected void createService() throws Exception
   {
      simpleMBean = new Simple();
      simpleMBean.setAString(aString);
      Hashtable props = serviceName.getKeyPropertyList();
      props.put("contained", "SimpleMBean");
      simpleName = new ObjectName(serviceName.getDomain(), props);
      server.registerMBean(simpleMBean, simpleName);
      boolean expectError = "ERRORINCREATE".equals(aString);
      // Invoke create on simpleName
      Object[] params = {};
      String[] signature = {};
      server.invoke(simpleName, "create", params, signature);
      if( expectError )
         throw new Error("Did not see expected ERRORINCREATE from: "+simpleName);
   }

   @Override
   protected void startService() throws Exception
   {
      if( simpleName != null )
      {
         // Invoke start on simpleName
         boolean expectError = "ERRORINSTART".equals(aString);
         Object[] params = {};
         String[] signature = {};
         server.invoke(simpleName, "start", params, signature);
         if( expectError )
            throw new Error("Did not see expected ERRORINSTART from: "+simpleName);
      }
   }

   @Override
   protected void stopService() throws Exception
   {
      if( simpleName != null )
      {
         boolean expectError = "ERRORINSTOP".equals(aString);
         Object[] params = {};
         String[] signature = {};
         server.invoke(simpleName, "stop", params, signature);
         if( expectError )
            throw new Error("Did not see expected ERRORINSTOP from: "+simpleName);
      }
   }

   @Override
   protected void destroyService() throws Exception
   {
      if( simpleName != null )
      {
         boolean expectError = "ERRORINDESTROY".equals(aString);
         Object[] params = {};
         String[] signature = {};
         try
         {
            server.invoke(simpleName, "destroy", params, signature);
            if( expectError )
               throw new Error("Did not see expected ERRORINDESTROY from: "+simpleName);
         }
         catch(Throwable e)
         {
            if( expectError == false )
               throw new UndeclaredThrowableException(e);
         }
         server.unregisterMBean(simpleName);
      }
      simpleName = null;
   }

}
