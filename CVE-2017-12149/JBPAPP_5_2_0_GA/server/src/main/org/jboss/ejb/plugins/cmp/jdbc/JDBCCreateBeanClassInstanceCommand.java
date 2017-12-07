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
package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.bridge.EntityBridgeInvocationHandler;
import org.jboss.ejb.plugins.cmp.bridge.FieldBridge;
import org.jboss.ejb.plugins.cmp.bridge.SelectorBridge;
import org.jboss.proxy.compiler.Proxy;
import org.jboss.proxy.compiler.InvocationHandler;
import org.jboss.deployment.DeploymentException;

/**
 * JDBCBeanClassInstanceCommand creates instance of the bean class. For
 * CMP 2.0 it creates an instance of a subclass of the bean class, as the
 * bean class is abstract.
 * <p/>
 * <FIX-ME>should not generat a subclass for ejb 1.1</FIX-ME>
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 81030 $
 */

public final class JDBCCreateBeanClassInstanceCommand
{
   private final JDBCEntityBridge entityBridge;
   private final Class beanClass;
   private final Constructor beanProxyConstructor;
   private final Map fieldMap;
   private final Map selectorMap;

   public JDBCCreateBeanClassInstanceCommand(JDBCStoreManager manager)
      throws Exception
   {
      EntityContainer theContainer = manager.getContainer();
      entityBridge = (JDBCEntityBridge) manager.getEntityBridge();
      beanClass = theContainer.getBeanClass();
      fieldMap = createFieldMap();
      selectorMap = createSelectorMap();
      // use proxy generator to create one implementation
      EntityBridgeInvocationHandler handler = new EntityBridgeInvocationHandler(fieldMap, selectorMap, beanClass);
      Class[] classes = new Class[]{beanClass};
      ClassLoader classLoader = beanClass.getClassLoader();

      Object o = Proxy.newProxyInstance(classLoader, classes, handler);

      // steal the constructor from the object
      beanProxyConstructor = o.getClass().getConstructor(new Class[]{InvocationHandler.class});

      // now create one to make sure everything is cool
      execute();
   }

   public void destroy()
   {
      Proxy.forgetProxyForClass(beanClass);
   }

   public Object execute() throws Exception
   {
      EntityBridgeInvocationHandler handler = new EntityBridgeInvocationHandler(fieldMap, selectorMap, beanClass);
      return beanProxyConstructor.newInstance(new Object[]{handler});
   }

   private Map getAbstractAccessors()
   {
      Method[] methods = beanClass.getMethods();
      Map abstractAccessors = new HashMap(methods.length);

      for(int i = 0; i < methods.length; i++)
      {
         if(Modifier.isAbstract(methods[i].getModifiers()))
         {
            String methodName = methods[i].getName();
            if(methodName.startsWith("get") || methodName.startsWith("set"))
            {
               abstractAccessors.put(methodName, methods[i]);
            }
         }
      }
      return abstractAccessors;
   }

   private Map createFieldMap() throws DeploymentException
   {
      Map abstractAccessors = getAbstractAccessors();

      List fields = entityBridge.getFields();
      Map map = new HashMap(fields.size() * 2);
      for(int i = 0; i < fields.size(); i++)
      {
         FieldBridge field = (FieldBridge) fields.get(i);

         // get the names
         String fieldName = field.getFieldName();
         String fieldBaseName = Character.toUpperCase(fieldName.charAt(0)) +
            fieldName.substring(1);
         String getterName = "get" + fieldBaseName;
         String setterName = "set" + fieldBaseName;

         // get the accessor methods
         Method getterMethod = (Method) abstractAccessors.get(getterName);
         Method setterMethod = (Method) abstractAccessors.get(setterName);

         // getters and setters must come in pairs
         if(getterMethod != null && setterMethod == null)
         {
            throw new DeploymentException("Getter was found but no setter was found for field " + fieldName
               + " in entity " + entityBridge.getEntityName());
         }
         else if(getterMethod == null && setterMethod != null)
         {
            throw new DeploymentException("Setter was found but no getter was found for field " + fieldName
               + " in entity " + entityBridge.getEntityName());
         }
         else if(getterMethod != null && setterMethod != null)
         {
            // add methods
            map.put(getterMethod.getName(), new EntityBridgeInvocationHandler.FieldGetInvoker(field));
            map.put(setterMethod.getName(), new EntityBridgeInvocationHandler.FieldSetInvoker(field));

            // remove the accessors (they have been used)
            abstractAccessors.remove(getterName);
            abstractAccessors.remove(setterName);
         }
      }
      return Collections.unmodifiableMap(map);
   }

   private Map createSelectorMap()
   {
      Collection selectors = entityBridge.getSelectors();
      Map map = new HashMap(selectors.size());
      for(Iterator iter = selectors.iterator(); iter.hasNext();)
      {
         SelectorBridge selector = (SelectorBridge) iter.next();
         //map.put(selector.getMethod().getName(), selector);
         map.put(selector.getMethod(), selector);
      }
      return Collections.unmodifiableMap(map);
   }
}
