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
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.QueryMetaData;
import org.jboss.util.Classes;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCQueryManager;
import org.jboss.logging.Logger;

/**
 * JDBCQueryMetaDataFactory constructs a JDBCQueryMetaData object based
 * on the query specifiection type.
 * 
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 81030 $
 */
public class JDBCQueryMetaDataFactory
{
   private static final Logger log = Logger.getLogger(JDBCQueryMetaDataFactory.class);

   private JDBCEntityMetaData entity;

   public JDBCQueryMetaDataFactory(JDBCEntityMetaData entity)
   {
      this.entity = entity;
   }

   public Map createJDBCQueryMetaData(QueryMetaData queryData)
      throws DeploymentException
   {

      Method[] methods = getQueryMethods(queryData);
      Map queries = new HashMap(methods.length);
      for(int i = 0; i < methods.length; i++)
      {
         queries.put(
            methods[i],
            new JDBCQlQueryMetaData(queryData, methods[i], entity.getQLCompiler(), false)
         );
      }
      return queries;
   }

   public Map createJDBCQueryMetaData(Element queryElement,
                                      Map defaultValues,
                                      JDBCReadAheadMetaData readAhead) throws DeploymentException
   {

      // get the query methods
      Method[] methods = getQueryMethods(queryElement);

      // read-ahead
      Element readAheadElement =
         MetaData.getOptionalChild(queryElement, "read-ahead");
      if(readAheadElement != null)
      {
         readAhead = new JDBCReadAheadMetaData(readAheadElement, readAhead);
      }

      Map queries = new HashMap(methods.length);
      for(int i = 0; i < methods.length; i++)
      {
         JDBCQueryMetaData defaultValue =
            (JDBCQueryMetaData) defaultValues.get(methods[i]);

         if(defaultValue == null && !entity.isCMP1x() && !methods[i].getName().equals("findByPrimaryKey"))
         {
            //throw new DeploymentException("Unknown query method : " + methods[i]);
            log.warn("The query method is not defined in ejb-jar.xml: " + methods[i]);
         }

         JDBCQueryMetaData jdbcQueryData = createJDBCQueryMetaData(
            defaultValue,
            queryElement,
            methods[i],
            readAhead
         );

         queries.put(methods[i], jdbcQueryData);
      }
      return queries;
   }

   public static JDBCQueryMetaData createJDBCQueryMetaData(JDBCQueryMetaData jdbcQueryMetaData,
                                                           JDBCReadAheadMetaData readAhead,
                                                           Class qlCompiler)
      throws DeploymentException
   {

      // RAW-SQL
      if(jdbcQueryMetaData instanceof JDBCRawSqlQueryMetaData)
      {
         return new JDBCRawSqlQueryMetaData(jdbcQueryMetaData.getMethod(), qlCompiler, false);
      }

      // JBOSS-QL
      if(jdbcQueryMetaData instanceof JDBCJBossQLQueryMetaData)
      {
         return new JDBCJBossQLQueryMetaData(
            (JDBCJBossQLQueryMetaData) jdbcQueryMetaData,
            readAhead, qlCompiler, false
         );
      }

      // DYNAMIC-SQL
      if(jdbcQueryMetaData instanceof JDBCDynamicQLQueryMetaData)
      {
         return new JDBCDynamicQLQueryMetaData(
            (JDBCDynamicQLQueryMetaData) jdbcQueryMetaData,
            readAhead, qlCompiler, false
         );
      }

      // DECLARED-SQL
      if(jdbcQueryMetaData instanceof JDBCDeclaredQueryMetaData)
      {
         return new JDBCDeclaredQueryMetaData(
            (JDBCDeclaredQueryMetaData) jdbcQueryMetaData,
            readAhead, qlCompiler, false
         );
      }

      // EJB-QL: default
      if(jdbcQueryMetaData instanceof JDBCQlQueryMetaData)
      {
         return new JDBCQlQueryMetaData(
            (JDBCQlQueryMetaData) jdbcQueryMetaData,
            readAhead, qlCompiler, false
         );
      }

      throw new DeploymentException(
         "Error in query specification for method " +
         jdbcQueryMetaData.getMethod().getName()
      );
   }


   private JDBCQueryMetaData createJDBCQueryMetaData(JDBCQueryMetaData jdbcQueryMetaData,
                                                     Element queryElement,
                                                     Method method,
                                                     JDBCReadAheadMetaData readAhead)
      throws DeploymentException
   {
      final Class qlCompiler = JDBCQueryManager.getQLCompiler(queryElement, entity);
      final boolean isResultTypeMappingLocal = (jdbcQueryMetaData == null ?
         false : jdbcQueryMetaData.isResultTypeMappingLocal());

      final boolean lazyResultSetLoading = Collection.class.isAssignableFrom(method.getReturnType()) &&
         MetaData.getOptionalChildBooleanContent(queryElement, "lazy-resultset-loading");

      // RAW-SQL
      Element rawSql = MetaData.getOptionalChild(queryElement, "raw-sql");
      if(rawSql != null)
      {
         return new JDBCRawSqlQueryMetaData(method, qlCompiler, lazyResultSetLoading);
      }

      // JBOSS-QL
      Element jbossQL =
         MetaData.getOptionalChild(queryElement, "jboss-ql");
      if(jbossQL != null)
      {
         return new JDBCJBossQLQueryMetaData(
            isResultTypeMappingLocal,
            jbossQL,
            method,
            readAhead,
            qlCompiler,
            lazyResultSetLoading
         );
      }

      // DYNAMIC-SQL
      Element dynamicQL = MetaData.getOptionalChild(queryElement, "dynamic-ql");
      if(dynamicQL != null)
      {
         return new JDBCDynamicQLQueryMetaData(isResultTypeMappingLocal, method, readAhead, qlCompiler, lazyResultSetLoading);
      }

      // DECLARED-SQL
      Element delcaredSql = MetaData.getOptionalChild(queryElement, "declared-sql");
      if(delcaredSql != null)
      {
         return new JDBCDeclaredQueryMetaData(
            isResultTypeMappingLocal,
            delcaredSql,
            method,
            readAhead,
            qlCompiler,
            lazyResultSetLoading
         );
      }

      // EJB-QL: default
      if(jdbcQueryMetaData instanceof JDBCQlQueryMetaData)
      {
         return new JDBCQlQueryMetaData(
            (JDBCQlQueryMetaData) jdbcQueryMetaData,
            method,
            readAhead
         );
      }

      throw new DeploymentException("Error in query specification for method " + method.getName());
   }

   private Method[] getQueryMethods(Element queryElement)
      throws DeploymentException
   {
      // query-method sub-element
      Element queryMethod = MetaData.getUniqueChild(queryElement, "query-method");

      // method name
      String methodName =
         MetaData.getUniqueChildContent(queryMethod, "method-name");

      // method params
      ArrayList methodParams = new ArrayList();
      Element methodParamsElement =
         MetaData.getUniqueChild(queryMethod, "method-params");
      Iterator iterator =
         MetaData.getChildrenByTagName(methodParamsElement, "method-param");
      while(iterator.hasNext())
      {
         methodParams.add(MetaData.getElementContent((Element) iterator.next()));
      }

      try
      {
         Class[] parameters = Classes.convertToJavaClasses(methodParams.iterator(), entity.getClassLoader());

         return getQueryMethods(methodName, parameters);
      }
      catch(ClassNotFoundException cnfe)
      {
         throw new DeploymentException(cnfe.getMessage());
      }

   }

   private Method[] getQueryMethods(QueryMetaData queryData)
      throws DeploymentException
   {
      String methodName = queryData.getMethodName();

      try
      {
         Class[] parameters = Classes.convertToJavaClasses(queryData.getMethodParams(), entity.getClassLoader());

         return getQueryMethods(methodName, parameters);
      }
      catch(ClassNotFoundException cnfe)
      {
         throw new DeploymentException(cnfe.getMessage());
      }
   }

   private Method[] getQueryMethods(String methodName,
                                    Class parameters[]) throws DeploymentException
   {

      // find the query and load the xml
      ArrayList methods = new ArrayList(2);
      if(methodName.startsWith("ejbSelect"))
      {
         // bean method
         Method method = getQueryMethod(methodName, parameters, entity.getEntityClass());
         if(method != null)
         {
            methods.add(method);
         }
      }
      else
      {
         // remote home
         Class homeClass = entity.getHomeClass();
         if(homeClass != null)
         {
            Method method = getQueryMethod(methodName, parameters, homeClass);
            if(method != null)
            {
               methods.add(method);
            }
         }
         // local home
         Class localHomeClass = entity.getLocalHomeClass();
         if(localHomeClass != null)
         {
            Method method = getQueryMethod(methodName, parameters, localHomeClass);
            if(method != null)
            {
               methods.add(method);
            }
         }
      }

      if(methods.size() == 0)
      {
         StringBuffer sb = new StringBuffer(300);
         sb.append("Query method not found: ")
            .append(methodName).append('(');
         for(int i = 0; i < parameters.length; i++)
         {
            if(i > 0)
            {
               sb.append(',');
            }
            sb.append(parameters[i].getName());
         }
         sb.append(')');
         throw new DeploymentException(sb.toString());
      }
      return (Method[]) methods.toArray(new Method[methods.size()]);
   }

   private static Method getQueryMethod(String queryName,
                                        Class[] parameters,
                                        Class clazz)
   {

      try
      {
         Method method = clazz.getMethod(queryName, parameters);

         // is the method abstract?
         // (remember interface methods are always abstract)
         if(Modifier.isAbstract(method.getModifiers()))
         {
            return method;
         }
      }
      catch(NoSuchMethodException e)
      {
         // that's cool
      }
      return null;
   }

}
