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
package org.jboss.ha.singleton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * A clustered singleton service that calls a configurable
 * method on a target (m)bean, whenever the current node becomes
 * the master. Correspondingly, it calls a configurable method
 * on the target (m)bean, whenever the current node resigns from
 * being the master.
 * 
 * Optional string arguments may be passed to those methods.
 * 
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:mr@gedoplan.de">Marcus Redeker</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author Brian Stansberry
 * 
 * @version $Revision: 81001 $
 */
public class HASingletonController extends HASingletonSupport
   implements HASingletonControllerMBean
{
   // Private Data --------------------------------------------------
   
   private ObjectName mSingletonMBean;
   private Object mSingleton;
   private String mSingletonStartMethod = "startSingleton";
   private String mSingletonStopMethod  = "stopSingleton";
   private String mSingletonStartMethodArgument;
   private String mSingletonStopMethodArgument;

   private static final Object[] NO_ARGS = new Object[0];
   private static final String[] NO_TYPE_NAMES = new String[0];
   private static final Class<?>[] NO_TYPES = new Class[0];
   
   // Attributes ----------------------------------------------------

   public Object getTarget()
   {
      return this.mSingleton;
   }
   
   public void setTarget(Object target)
   {
      this.mSingleton = target;
   }
   
   public ObjectName getTargetName()
   {
      return this.mSingletonMBean;
   }

   public void setTargetName(ObjectName targetObjectName)
   {
      this.mSingletonMBean = targetObjectName;
   }

   public String getTargetStartMethod()
   {
      return this.mSingletonStartMethod;
   }

   public void setTargetStartMethod(String targetStartMethod)
      throws InvalidParameterException
   {
      if (targetStartMethod != null)
      {
         this.mSingletonStartMethod = targetStartMethod;
      }
   }


   public String getTargetStopMethod()
   {
      return this.mSingletonStopMethod;
   }

   public void setTargetStopMethod(String targetStopMethod)
      throws InvalidParameterException
   {
      if (targetStopMethod != null)
      {
         this.mSingletonStopMethod = targetStopMethod;
      }
   }

   public String getTargetStartMethodArgument()
   {
      return this.mSingletonStartMethodArgument ;
   }

   public void setTargetStartMethodArgument(String targetStartMethodArgument)
   {
      this.mSingletonStartMethodArgument = targetStartMethodArgument;
   }

   public String getTargetStopMethodArgument()
   {
      return this.mSingletonStopMethodArgument ;
   }

   public void setTargetStopMethodArgument(String targetStopMethodArgument)
   {
      this.mSingletonStopMethodArgument =  targetStopMethodArgument;
   }
  
   // HASingleton implementation ------------------------------------
   
   /**
    * Call the target start method
    * 
    * @see org.jboss.ha.singleton.HASingletonSupport#startSingleton()
    */
   @Override
   public void startSingleton()
   {
      super.startSingleton();

      try
      {
         if (this.mSingleton != null)
         {
            this.invokeSingletonMethod( this.mSingleton, this.mSingletonStartMethod, this.mSingletonStartMethodArgument);
         }
         else if (this.mSingletonMBean != null)
         {
            this.invokeSingletonMBeanMethod( this.mSingletonMBean, this.mSingletonStartMethod, this.mSingletonStartMethodArgument);
         }
         else
         {
            this.log.warn("No singleton configured; cannot start");
         }
      }
      catch (Exception e)
      {
         this.log.error("Controlled Singleton failed to become master", e);
      }
   }

   /**
    * Call the target stop method
    * 
    * @see org.jboss.ha.singleton.HASingletonSupport#stopSingleton()
    */
   @Override
   public void stopSingleton()
   {
      super.stopSingleton();

      try
      {
         if (this.mSingleton != null)
         {
            this.invokeSingletonMethod( this.mSingleton, this.mSingletonStopMethod, this.mSingletonStopMethodArgument);
         }
         else if (this.mSingletonMBean != null)
         {
            this.invokeSingletonMBeanMethod( this.mSingletonMBean, this.mSingletonStopMethod, this.mSingletonStopMethodArgument);
         }
         else
         {
            this.log.warn("No singleton configured; cannot start");
         }
      }
      catch (Exception e)
      {
         this.log.error("Controlled Singleton failed to resign from master position", e);
      }
   }

   // Protected -----------------------------------------------------
   
   protected Object invokeSingletonMethod(Object target,
      String operationName, Object param)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
   {
      if (target != null && operationName != null)
      {
         Object[] params;
         Class<?>[] types;
         
         if (param != null)
         {
            params = new Object[] { param };
            types = new Class[] { param.getClass() };
            
            this.log.debug("Calling operation: " + operationName + "(" + param + "), on target: '" + target + "'");
         }
         else
         {
            params = NO_ARGS;
            types = NO_TYPES;
            
            this.log.debug("Calling operation: " + operationName + "(), on target: '" + target + "'");
         }
         
         Method method = getTargetMethod(target, operationName, types);
         
         return method.invoke(target, params);
      }

      this.log.debug("No configured target mbean or operation to call");
      
      return null;
   }
   
   protected Object invokeSingletonMBeanMethod(ObjectName target,
      String operationName, Object param)
      throws InstanceNotFoundException, MBeanException, ReflectionException
   {
      if (target != null && operationName != null)
      {
         Object[] params;
         String[] signature;
         
         if (param != null)
         {
            params = new Object[] { param };
            signature = new String[] { param.getClass().getName() };
            
            this.log.debug("Calling operation: " + operationName +
                  "(" + param + "), on target: '" + target + "'");
         }
         else
         {
            params = NO_ARGS;
            signature = NO_TYPE_NAMES;
            
            this.log.debug("Calling operation: " + operationName +
                  "(), on target: '" + target + "'");
         }

         return this.server.invoke(target, operationName, params, signature);
      }

      this.log.debug("No configured target mbean or operation to call");
      
      return null;
   }
   
   public static Method getTargetMethod(Object target, String methodName, Class<?>[] types)
         throws NoSuchMethodException
   {
      Class<?> clazz = target.getClass();
      NoSuchMethodException nsme = null;
      while (clazz != null)
      {
         try
         {
            Method method = clazz.getDeclaredMethod(methodName, types);
            return method;
         }
         catch (NoSuchMethodException e)
         {
            // Cache the one from the top level class
            if (nsme == null)
            {
               nsme = e;
            }
           // Keep searching
           clazz = clazz.getSuperclass();
         }
      }
      throw nsme;
   }
}
