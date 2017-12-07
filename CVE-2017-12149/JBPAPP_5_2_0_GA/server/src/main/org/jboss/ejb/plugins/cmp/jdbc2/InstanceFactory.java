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
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.plugins.cmp.bridge.EntityBridgeInvocationHandler;
import org.jboss.ejb.plugins.cmp.bridge.FieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.EJBSelectBridge;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.Schema;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.EntityContainer;
import org.jboss.proxy.compiler.Proxy;
import org.jboss.proxy.compiler.InvocationHandler;
import org.jboss.deployment.DeploymentException;

import javax.ejb.FinderException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.Collection;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81030 $</tt>
 */
public class InstanceFactory
{
   private final Class beanClass;
   private final Constructor beanProxyConstructor;
   private final Map fieldMap;
   private final Map selectorMap;

   public InstanceFactory(JDBCStoreManager2 manager, JDBCEntityBridge2 entity)
      throws Exception
   {
      EntityContainer theContainer = manager.getContainer();
      beanClass = theContainer.getBeanClass();
      fieldMap = createFieldMap(entity);
      selectorMap = createSelectorMap(entity, manager.getQueryFactory());
      // use proxy generator to create one implementation
      EntityBridgeInvocationHandler handler = new EntityBridgeInvocationHandler(fieldMap, selectorMap, beanClass);
      Class[] classes = new Class[]{beanClass};
      ClassLoader classLoader = beanClass.getClassLoader();

      Object o = Proxy.newProxyInstance(classLoader, classes, handler);

      // steal the constructor from the object
      beanProxyConstructor = o.getClass().getConstructor(new Class[]{InvocationHandler.class});

      // now create one to make sure everything is cool
      newInstance();
   }

   public void destroy()
   {
      Proxy.forgetProxyForClass(beanClass);
   }

   public Object newInstance() throws Exception
   {
      EntityBridgeInvocationHandler handler = new EntityBridgeInvocationHandler(fieldMap, selectorMap, beanClass);
      return beanProxyConstructor.newInstance(new Object[]{handler});
   }

   private static Map getAbstractAccessors(Class beanClass)
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

   private static Map createFieldMap(JDBCEntityBridge2 entityBridge) throws DeploymentException
   {
      Map abstractAccessors = getAbstractAccessors(entityBridge.getMetaData().getEntityClass());

      List fields = entityBridge.getFields();
      Map map = new HashMap(fields.size() * 2);
      for(int i = 0; i < fields.size(); i++)
      {
         FieldBridge field = (FieldBridge) fields.get(i);

         // get the names
         String fieldName = field.getFieldName();
         String fieldBaseName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
         String getterName = "get" + fieldBaseName;
         String setterName = "set" + fieldBaseName;

         // get the accessor methods
         Method getterMethod = (Method) abstractAccessors.get(getterName);
         Method setterMethod = (Method) abstractAccessors.get(setterName);

         // getters and setters must come in pairs
         if(getterMethod != null && setterMethod == null)
         {
            throw new DeploymentException("Getter was found but, no setter was found for field: " + fieldName);
         }
         else if(getterMethod == null && setterMethod != null)
         {
            throw new DeploymentException("Setter was found but, no getter was found for field: " + fieldName);
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

   private static Map createSelectorMap(JDBCEntityBridge2 entityBridge, QueryFactory queryFactory)
      throws DeploymentException
   {
      Collection queries = entityBridge.getMetaData().getQueries();
      Map selectorsByMethod = new HashMap(queries.size());
      Iterator definedFinders = queries.iterator();
      while(definedFinders.hasNext())
      {
         JDBCQueryMetaData metadata = (JDBCQueryMetaData)definedFinders.next();
         if(metadata.getMethod().getName().startsWith("ejbSelect"))
         {
            try
            {
               QueryCommand queryCommand = queryFactory.getQueryCommand(metadata.getMethod());
               Schema schema = ((JDBCStoreManager2)entityBridge.getManager()).getSchema();
               EJBSelectBridge ejbSelectBridge = new EJBSelectBridge(entityBridge.getContainer(), schema, metadata, queryCommand);
               selectorsByMethod.put(metadata.getMethod(), ejbSelectBridge);
            }
            catch(FinderException e)
            {
               throw new DeploymentException(e.getMessage());
            }
         }
      }

      return selectorsByMethod;
   }
}
