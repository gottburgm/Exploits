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
package org.jboss.resource.adapter.jdbc.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.management.ObjectName;
import javax.naming.BinaryRefAddr;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.resource.Referenceable;
import javax.sql.DataSource;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;
import org.jboss.proxy.ClientMethodInterceptor;
import org.jboss.proxy.GenericProxyFactory;
import org.jboss.resource.connectionmanager.ConnectionFactoryBindingService;
import org.jboss.system.Registry;
import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.util.naming.Util;
import org.jboss.util.Classes;


/** 
 * An mbean service that pvovides the detached invoker ops for the
 * javax.sql.DataSource and related java.sql.* interfaces.
 *
 * TODO this does not belong in the resource adapter
 * @author Scott.Stark@jboss.org
 * @author Tom.Elrod@jboss.org
 * @author adrian@jboss.com
 * @version $Revision: 113217 $
 */
public class WrapperDataSourceService extends ConnectionFactoryBindingService  implements WrapperDataSourceServiceMBean
{
   private static Logger log = Logger.getLogger(WrapperDataSourceService.class);

   private ObjectName jmxInvokerName;
   private Invoker delegateInvoker;
   private Object theProxy;
   private Map marshalledInvocationMapping = new HashMap();
   private Map connectionMap = Collections.synchronizedMap(new HashMap());
   private Map statementMap = Collections.synchronizedMap(new HashMap());
   private Map resultSetMap = Collections.synchronizedMap(new HashMap());
   private Map lobMap = Collections.synchronizedMap(new HashMap());
   private Map databaseMetaDataMap = Collections.synchronizedMap(new HashMap());
   private boolean trace = log.isTraceEnabled();

   protected void startService() throws Exception
   {
      determineBindName();
      createConnectionFactory();
      if( jmxInvokerName != null )
      {
         createProxy();
         calculateMethodHases();
         bindConnectionFactory();
      }
      else
      {
         super.bindConnectionFactory();
      }
   }

   protected void stopService() throws Exception
   {
      unbindConnectionFactory();
      if( jmxInvokerName != null )
      destroyProxy();
   }
   
   protected void bindConnectionFactory() throws Exception
   {
      InitialContext ctx = new InitialContext();
      try
      {
         log.debug("Binding object '" + cf + "' into JNDI at '" + bindName + "'");
         // Associated the local cf with the NonSerializable factory
         NonSerializableFactory.rebind(bindName, cf);
         /* Create a reference that uses the the DataSourceFactory as the
         reference factory class. This class detects whether the lookup
         is being done locally or remotely and returns either the just bound
         connection factory, or a DataSource proxy that uses the detached
         invoker framework to expose remote proxies to the server side
         DataSource and related elements.
         */
         Referenceable referenceable = (Referenceable) cf;
         // Set the DataSource proxy as the ProxyData ref address
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(theProxy);
         oos.close();
         byte[] proxyBytes = baos.toByteArray();
         BinaryRefAddr dsAddr = new BinaryRefAddr("ProxyData", proxyBytes);
         String factory = DataSourceFactory.class.getName();
         Reference dsRef = new Reference("javax.sql.DataSource", dsAddr, factory, null);
         referenceable.setReference(dsRef);
         // Set the VMID as the address local/remote indicator
         baos.reset();
         ObjectOutputStream oos2 = new ObjectOutputStream(baos);
         oos2.writeObject(DataSourceFactory.vmID);
         oos2.close();
         byte[] id = baos.toByteArray();
         BinaryRefAddr localAddr = new BinaryRefAddr("VMID", id);
         dsRef.add(localAddr);
         /* Bind the Referenceable connection factory into JNDI and set the
         JndiName value of the reference address for use by the DataSourceFactory
         when looking up the local factory from the NonSerializableFactory.
         */
         StringRefAddr jndiRef = new StringRefAddr("JndiName", bindName);
         dsRef.add(jndiRef);
         Util.rebind(ctx, bindName, cf);
         log.info("Bound ConnectionManager '" + serviceName + "' to JNDI name '" + bindName + "'");
      }
      catch (NamingException ne)
      {
         throw new DeploymentException("Could not bind ConnectionFactory into jndi: " + bindName, ne);
      }
      finally
      {
         ctx.close();
      }
   }

   public ObjectName getJMXInvokerName()
   {
      return jmxInvokerName;
   }

   public void setJMXInvokerName(ObjectName jmxInvokerName)
   {
      this.jmxInvokerName = jmxInvokerName;
   }

   public Object invoke(Invocation invocation) throws Exception
   {
      // Set the method hash to Method mapping
      if (invocation instanceof MarshalledInvocation)
      {
         MarshalledInvocation mi = (MarshalledInvocation) invocation;
         mi.setMethodMap(marshalledInvocationMapping);
      }
      // Invoke the Naming method via reflection
      Method method = invocation.getMethod();
      Class methodClass = method.getDeclaringClass();
      Object[] args = invocation.getArguments();
      Object value = null;

      try
      {
         if( methodClass.isAssignableFrom(DataSource.class) )
         {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(bindName);
            value = doDataSourceMethod(ds, method, args);
         }
         else if( methodClass.isAssignableFrom(Connection.class) )
         {
            Object id = invocation.getId();
            Connection conn = (Connection) connectionMap.get(id);
            if( conn == null )
            {
               if (method.getName().equals("isClosed"))
                  return true;

               throw new IllegalAccessException("Failed to find connection: "+id);
            }
            value = doConnectionMethod(id, conn, method, args);
         }
         else if( methodClass.isAssignableFrom(Statement.class) ||
            methodClass.isAssignableFrom(PreparedStatement.class) ||
            methodClass.isAssignableFrom(CallableStatement.class))
         {
            Object id = invocation.getId();
            Statement stmt = (Statement) statementMap.get(id);
            if( stmt == null )
            {
               throw new SQLException("Failed to find Statement: " + id);
            }
            value = doStatementMethod(id, stmt, method, args);
         }
         else if( methodClass.isAssignableFrom(ResultSet.class) )
         {
            Object id = invocation.getId();
            ResultSet results = (ResultSet) resultSetMap.get(id);
            if( results == null )
            {
               throw new IllegalAccessException("Failed to find ResultSet: "+id);
            }
            value = doResultSetMethod(id, results, method, args);
         }
         else if (methodClass.isAssignableFrom(DatabaseMetaData.class))
         {
            Object id = invocation.getId();
            DatabaseMetaData dbMetaData = (DatabaseMetaData) databaseMetaDataMap.get(id);
            if(dbMetaData == null)
            {
               throw new IllegalAccessException("Failed to find DatabaseMetaData: " + id);
            }
            value = doDatabaseMetaDataMethod(dbMetaData, method, args);
         }
         else
         {
            throw new UnsupportedOperationException("Do not know how to handle method="+method);
         }
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if (t instanceof Exception)
            throw (Exception) t;
         else
            throw new UndeclaredThrowableException(t, method.toString());
      }

      return value;
   }
   
   /**
    * Create the proxy
    * 
    * TODO this should be external configuration
    */
   protected void createProxy() throws Exception
   {
      /* Create an JRMPInvokerProxy that will be associated with a naming JMX
      invoker given by the jmxInvokerName.
      */
     delegateInvoker = (Invoker) Registry.lookup(jmxInvokerName);
     log.debug("Using delegate: " + delegateInvoker
        + " for invoker=" + jmxInvokerName);
     ObjectName targetName = getServiceName();
     Integer nameHash = new Integer(targetName.hashCode());
     Registry.bind(nameHash, targetName);

     Object cacheID = null;
     String proxyBindingName = null;
     String jndiName = null;
     Class[] ifaces = {javax.sql.DataSource.class};
     /* Initialize interceptorClasses with default client interceptor list
        if no client interceptor configuration was provided */
     ArrayList interceptorClasses = new ArrayList();
     interceptorClasses.add(ClientMethodInterceptor.class);
     interceptorClasses.add(InvokerInterceptor.class);
     ClassLoader loader = Thread.currentThread().getContextClassLoader();
     GenericProxyFactory proxyFactory = new GenericProxyFactory();
     theProxy = proxyFactory.createProxy(cacheID, targetName,
        delegateInvoker, jndiName, proxyBindingName, interceptorClasses,
        loader, ifaces);
     log.debug("Created proxy for invoker=" + jmxInvokerName
        + ", targetName=" + targetName + ", nameHash=" + nameHash);
   }
   
   /**
    * Destroy the proxy
    */
   protected void destroyProxy() throws Exception
   {
      ObjectName name = getServiceName();
      Integer nameHash = new Integer(name.hashCode());
      Registry.unbind(nameHash);
   }
   
   /**
    * Calculate the method hashes
    */
   protected void calculateMethodHases() throws Exception
   {
      Method[] methods = DataSource.class.getMethods();
      for(int m = 0; m < methods.length; m ++)
      {
         Method method = methods[m];
         Long hash = new Long(MarshalledInvocation.calculateHash(method));
         marshalledInvocationMapping.put(hash, method);
      }

      // Get the Long to Method mappings
      Map m = MarshalledInvocation.methodToHashesMap(Connection.class);
      displayHashes(m);
      marshalledInvocationMapping.putAll(m);
      m = MarshalledInvocation.methodToHashesMap(Statement.class);
      displayHashes(m);
      marshalledInvocationMapping.putAll(m);
      m = MarshalledInvocation.methodToHashesMap(CallableStatement.class);
      displayHashes(m);
      marshalledInvocationMapping.putAll(m);
      m = MarshalledInvocation.methodToHashesMap(PreparedStatement.class);
      displayHashes(m);
      marshalledInvocationMapping.putAll(m);
      m = MarshalledInvocation.methodToHashesMap(ResultSet.class);
      displayHashes(m);
      marshalledInvocationMapping.putAll(m);
      m = MarshalledInvocation.methodToHashesMap(DatabaseMetaData.class);
      displayHashes(m);
      marshalledInvocationMapping.putAll(m);
   }

   private Object doDataSourceMethod(DataSource ds, Method method, Object[] args)
      throws InvocationTargetException, IllegalAccessException
   {
      Object value = method.invoke(ds, args);
      if( value instanceof Connection )
      {
         value = createConnectionProxy(value);
      }
      else if( value != null && (value instanceof Serializable) == false )
      {
         throw new IllegalAccessException("Method="+method+" does not return Serializable");
      }
      return value;
   }

   private Object doConnectionMethod(Object id, Connection conn, Method method, Object[] args)
         throws InvocationTargetException, IllegalAccessException, SQLException
   {
      if( trace )
      {
         log.trace("doConnectionMethod, conn="+conn+", method="+method);
      }

      // Remove the connection and handle Connection.isClosed() in invoke()
      if( method.getName().equals("close") )
      {
         connectionMap.remove(id);
         log.debug("Closed Connection="+id);
      }

      Object value = method.invoke(conn, args);
      if( value instanceof Statement )
      {
         value = createStatementProxy(value);
      }
      else if(value instanceof DatabaseMetaData)
      {
         value = createDatabaseMetaData(value);
      }
      else if( value != null && (value instanceof Serializable) == false )
      {
         throw new IllegalAccessException("Method="+method+" does not return Serializable");
      }
      return value;
   }

   private Object doStatementMethod(Object id, Statement stmt, Method method, Object[] args)
         throws InvocationTargetException, IllegalAccessException, SQLException
   {
      if( trace )
      {
         log.trace("doStatementMethod, conn="+stmt+", method="+method);
      }

      if( method.getName().equals("close") )
      {
         statementMap.remove(id);
         log.debug("Closed Statement="+id);
      }

      Object value = method.invoke(stmt, args);
      if( value instanceof ResultSet )
      {
         value = createResultSetProxy(value);
      }
      else if( value instanceof Connection )
      {
         value = createConnectionProxy(value);
      }
      else if( value instanceof ResultSetMetaData )
      {
         ResultSetMetaData rmd = (ResultSetMetaData) value;
         value = new SerializableResultSetMetaData(rmd);
      }
      else if ( value instanceof ParameterMetaData )
      {
         ParameterMetaData pmd = (ParameterMetaData) value;
         value = new SerializableParameterMetaData(pmd);
      }
      else if( value != null && (value instanceof Serializable) == false )
      {
         throw new IllegalAccessException("Method="+method+" does not return Serializable");
      }
      return value;
   }

   private Object doResultSetMethod(Object id, ResultSet results, Method method, Object[] args)
         throws InvocationTargetException, IllegalAccessException, SQLException, IOException
   {
      if( trace )
      {
         log.trace("doResultSetMethod, results="+results+", method="+method);
      }

      if( method.getName().equals("close") )
      {
         resultSetMap.remove(id);
         log.debug("Closed ResultSet="+id);
      }

      Object value = method.invoke(results, args);
      if( value instanceof ResultSetMetaData )
      {
         ResultSetMetaData rmd = (ResultSetMetaData) value;
         value = new SerializableResultSetMetaData(rmd);
      }
      // Need to create serializable version of ascii stream returned by result set
      if(("getBinaryStream".equals(method.getName()) || "getAsciiStream".equals(method.getName())) && value instanceof InputStream)
      {
         InputStream ins = (InputStream)value;
         value = new SerializableInputStream(ins);
      }
      else if ("getCharacterStream".equals(method.getName()) && value instanceof java.io.Reader)
      {
         java.io.Reader ins = (java.io.Reader)value;
         value = new SerializableReader(ins);
      }
      else if("getClob".equals(method.getName()) || "getBlob".equals(method.getName()))
      {
      	value = createLobProxy(value);
      }

      if( value != null && (value instanceof Serializable) == false )
      {
         throw new IllegalAccessException("Method="+method+" does not return Serializable");
      }
      return value;
   }

   private Object doDatabaseMetaDataMethod(DatabaseMetaData dbMetaData, Method method, Object[] args)
         throws InvocationTargetException, IllegalAccessException
   {
      if( trace )
      {
         log.trace("doDatabaseMetaDataMethod, dbMetaData="+dbMetaData+", method="+method);
      }

      Object value = method.invoke(dbMetaData, args);
      if( value instanceof ResultSet )
      {
         value = createResultSetProxy(value);
      }
      else if( value instanceof Connection )
      {
         value = createConnectionProxy(value);
      }
      if( value != null && (value instanceof Serializable) == false )
      {
         throw new IllegalAccessException("Method="+method+" does not return Serializable");
      }
      return value;
   }

   private Object createConnectionProxy(Object conn)
   {
      Object cacheID = UUID.randomUUID();
      ObjectName targetName = getServiceName();
      String proxyBindingName = null;
      String jndiName = null;
      Class[] ifaces = {java.sql.Connection.class};
      ArrayList interceptorClasses = new ArrayList();
      interceptorClasses.add(ClientMethodInterceptor.class);
      interceptorClasses.add(InvokerInterceptor.class);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      GenericProxyFactory proxyFactory = new GenericProxyFactory();
      Object connProxy = proxyFactory.createProxy(cacheID, targetName,
         delegateInvoker, jndiName, proxyBindingName, interceptorClasses,
         loader, ifaces);
      connectionMap.put(cacheID, conn);
      log.debug("Created Connection proxy for invoker=" + jmxInvokerName
         + ", targetName=" + targetName + ", cacheID=" + cacheID);
      return connProxy;
   }

   private Object createStatementProxy(Object stmt)
   {
      Object cacheID = UUID.randomUUID();
      ObjectName targetName = getServiceName();
      String proxyBindingName = null;
      String jndiName = null;
      // Filter out all but java* interfaces
      Class[] ifaces = getJavaInterfaces(stmt.getClass());
      ArrayList interceptorClasses = new ArrayList();
      interceptorClasses.add(StatementInterceptor.class);
      interceptorClasses.add(ClientMethodInterceptor.class);
      interceptorClasses.add(InvokerInterceptor.class);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      GenericProxyFactory proxyFactory = new GenericProxyFactory();
      Object stmtProxy = proxyFactory.createProxy(cacheID, targetName,
         delegateInvoker, jndiName, proxyBindingName, interceptorClasses,
         loader, ifaces);
      statementMap.put(cacheID, stmt);
      log.debug("Created Statement proxy for invoker=" + jmxInvokerName
         + ", targetName=" + targetName + ", cacheID=" + cacheID);
      return stmtProxy;
   }

   private Object createResultSetProxy(Object results)
   {
      Object cacheID = UUID.randomUUID(); 
      ObjectName targetName = getServiceName();
      String proxyBindingName = null;
      String jndiName = null;
      // Filter out all but java* interfaces
      Class[] ifaces = getJavaInterfaces(results.getClass());

      ArrayList interceptorClasses = new ArrayList();
      interceptorClasses.add(ClientMethodInterceptor.class);
      interceptorClasses.add(InvokerInterceptor.class);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      GenericProxyFactory proxyFactory = new GenericProxyFactory();
      Object resultsProxy = proxyFactory.createProxy(cacheID, targetName,
         delegateInvoker, jndiName, proxyBindingName, interceptorClasses,
         loader, ifaces);
      resultSetMap.put(cacheID, results);
      log.debug("Created ResultSet proxy for invoker=" + jmxInvokerName
         + ", targetName=" + targetName + ", cacheID=" + cacheID);
      return resultsProxy;
   }

   private Object createLobProxy(Object results)
   {
      Object cacheID = UUID.randomUUID(); 
      ObjectName targetName = getServiceName();
      String proxyBindingName = null;
      String jndiName = null;
      Class[] ifaces = results.getClass().getInterfaces();
      ArrayList interceptorClasses = new ArrayList();
      interceptorClasses.add(ClientMethodInterceptor.class);
      interceptorClasses.add(InvokerInterceptor.class);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      GenericProxyFactory proxyFactory = new GenericProxyFactory();
      Object resultsProxy = proxyFactory.createProxy(cacheID, targetName,
         delegateInvoker, jndiName, proxyBindingName, interceptorClasses,
         loader, ifaces);
      lobMap.put(cacheID, results);
      log.debug("Created LOB proxy for invoker=" + jmxInvokerName
         + ", targetName=" + targetName + ", cacheID=" + cacheID);
      return resultsProxy;
   }

   private Object createDatabaseMetaData(Object dbMetaData)
   {
      Object cacheID = UUID.randomUUID(); 
      ObjectName targetName = getServiceName();
      String proxyBindingName = null;
      String jndiName = null;
      Class[] ifaces = {java.sql.DatabaseMetaData.class};
      ArrayList interceptorClasses = new ArrayList();
      interceptorClasses.add(ClientMethodInterceptor.class);
      interceptorClasses.add(InvokerInterceptor.class);
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      GenericProxyFactory proxyFactory = new GenericProxyFactory();
      Object dbMetaDataProxy = proxyFactory.createProxy(cacheID, targetName,
         delegateInvoker, jndiName, proxyBindingName, interceptorClasses,
         loader, ifaces);
      databaseMetaDataMap.put(cacheID, dbMetaData);
      log.debug("Created DatabaseMetadata proxy for invoker=" + jmxInvokerName
         + ", targetName=" + targetName + ", cacheID=" + cacheID);
      return dbMetaDataProxy;

   }

   private void displayHashes(Map m)
   {
      if( trace == false )
         return;

      Iterator keys = m.keySet().iterator();
      while( keys.hasNext() )
      {
         Long key = (Long) keys.next();
         log.trace(key+"="+m.get(key));
      }
   }

   private Class[] getJavaInterfaces(Class clazz)
   {
      ArrayList tmp = new ArrayList();
      Classes.getAllInterfaces(tmp, clazz);
      Iterator iter = tmp.iterator();
      while( iter.hasNext() )
      {
         Class c = (Class) iter.next();
         if( c.getName().startsWith("java") == false )
            iter.remove();
      }
      Class[] ifaces = new Class[tmp.size()];
      return (Class[]) tmp.toArray(ifaces);
   }
}
