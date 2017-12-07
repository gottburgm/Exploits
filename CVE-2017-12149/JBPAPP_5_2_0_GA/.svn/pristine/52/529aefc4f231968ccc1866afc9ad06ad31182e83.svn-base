/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.as.integration.hornetq.management;

import java.util.Collections;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.dependency.plugins.AbstractScopeInfo;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerMode;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.dependency.spi.ErrorHandlingMode;
import org.jboss.dependency.spi.ScopeInfo;
import org.jboss.dependency.spi.dispatch.InvokeDispatchContext;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.util.JBossObject;
import org.jboss.util.JBossStringBuilder;
import org.jboss.util.NotImplementedException;

/**
 * A runtime dispatcher plugin, used to delegate requests from the ProfileService
 * {@code RuntimeComponentDispatcher} to the MBeanServer. This is a needed when
 * MBeans are directly installed using the MBeanServer, not using the mc-jmx-int. 
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class HornetQControlRuntimeDispatchPlugin extends JBossObject implements KernelRegistryEntry, InvokeDispatchContext
{
   
   /** The object name. */
   private final ObjectName objectName;
   
   /** The mbean server. */
   private final MBeanServer mbeanServer;

   public HornetQControlRuntimeDispatchPlugin(ObjectName objectName, MBeanServer mbeanServer)
   {
      if(objectName == null)
      {
         throw new IllegalArgumentException("null object name");
      }
      if(mbeanServer == null)
      {
         throw new IllegalArgumentException("null mbean server");
      }
      this.objectName = objectName;
      this.mbeanServer = mbeanServer;
   }
   
   public Object get(String name) throws Throwable
   {
      return mbeanServer.getAttribute(objectName, getAttributeName(name));
   }
   
   public void set(String name, Object value) throws Throwable
   {
      Attribute attribute = new Attribute(getAttributeName(name), value);
      mbeanServer.setAttribute(objectName, attribute);
   }
   
   public Object invoke(String name, Object[] parameters, String[] signature) throws Throwable
   {
      return mbeanServer.invoke(objectName, name, parameters, signature);
   }

   public ClassLoader getClassLoader() throws Throwable
   {
      return mbeanServer.getClassLoader(objectName);
   }
   
   public Object getName()
   {
      return this.objectName;
   }
   
   public void setName(Object name)
   {
      // 
   }

   protected static String getAttributeName(String name)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("Illegal name: " + name);

      char firstCharacter = name.charAt(0);
      if (Character.isLowerCase(firstCharacter))
      {
         String attributeName = String.valueOf(Character.toUpperCase(firstCharacter));
         if (name.length() > 1)
            attributeName += name.substring(1);
         return attributeName;
      }
      return name;
   }
   
   public Set<Object> getAliases()
   {
      return Collections.emptySet();
   }
   
   public Controller getController()
   {
      throw new NotImplementedException("getController");
   }

   public DependencyInfo getDependencyInfo()
   {
      return null;
   }

   public ScopeInfo getScopeInfo()
   {
      return new AbstractScopeInfo(getName(), null);
   }
   
   public Object getTarget()
   {
      return null;
   }
   
   public Throwable getError()
   {
      throw new NotImplementedException("getError");
   }

   public ControllerState getState()
   {
      // Specify this for the runtime state mapping
      boolean registered = mbeanServer.isRegistered(objectName);
      return registered ? ControllerState.INSTALLED : ControllerState.NOT_INSTALLED;
   }
   
   public void setState(ControllerState state)
   {
      throw new org.jboss.util.NotImplementedException("setState");
   }

   public ControllerState getRequiredState()
   {
      // Specify this for the runtime state mapping
      return ControllerState.INSTALLED;
   }

   public void setRequiredState(ControllerState state)
   {
      throw new NotImplementedException("setRequiredState");
   }
   
   public ControllerMode getMode()
   {
      return ControllerMode.MANUAL;
   }
   
   public void setMode(ControllerMode mode)
   {
      throw new NotImplementedException("setMode");
   }

   public ErrorHandlingMode getErrorHandlingMode()
   {
      return ErrorHandlingMode.DISCARD;
   }

   public void install(ControllerState fromState, ControllerState toState) throws Throwable
   {
      throw new NotImplementedException("install");
   }

   public void setController(Controller controller)
   {
      throw new NotImplementedException("setController");
   }

   public void setError(Throwable error)
   {
      throw new NotImplementedException("NYI setError");
   }

   public void uninstall(ControllerState fromState, ControllerState toState)
   {
      throw new NotImplementedException("uninstall");
   }

   public void toString(JBossStringBuilder buffer)
   {
      buffer.append("target=").append(objectName);
   }
   
   public void toShortString(JBossStringBuilder buffer)
   {
      buffer.append(objectName);
   }
   
}

