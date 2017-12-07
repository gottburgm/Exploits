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
package org.jboss.ejb3.iiop;

import org.jboss.ejb3.core.proxy.spi.CurrentRemoteProxyFactory;
import org.jboss.ejb3.endpoint.Endpoint;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.iiop.CorbaORB;
import org.jboss.iiop.csiv2.SASCurrent;
import org.jboss.iiop.rmi.AttributeAnalysis;
import org.jboss.iiop.rmi.InterfaceAnalysis;
import org.jboss.iiop.rmi.OperationAnalysis;
import org.jboss.iiop.rmi.RmiIdlUtil;
import org.jboss.iiop.rmi.marshal.strategy.SkeletonStrategy;
import org.jboss.iiop.tm.InboundTransactionCurrent;
import org.jboss.invocation.iiop.ReferenceData;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.tm.TransactionManagerLocator;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.PortableServer.Current;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBTransactionRequiredException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashMap;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 112294 $
 */
public class BeanCorbaServant extends Servant
   implements InvokeHandler
{
   private static final Logger log = Logger.getLogger(BeanCorbaServant.class);

   private final IORFactory factory;
   private final Current poaCurrent;
   private final Endpoint container;
   private final ClassLoader containerClassLoader;
   private final InterfaceDef interfaceDef;
   private final String repositoryIds[];
   private SASCurrent sasCurrent;
   private InboundTransactionCurrent inboundTxCurrent;

   private HashMap<String, SkeletonStrategy> methodMap;
   
   protected BeanCorbaServant(IORFactory factory, Current poaCurrent, Endpoint container, ClassLoader containerClassLoader, InterfaceDef interfaceDef, InterfaceAnalysis interfaceAnalysis)
   {
      assert factory != null : "factory is null";
      assert poaCurrent != null : "poaCurrent is null";
      assert container != null : "container is null";
      assert container instanceof SessionContainer : "only SessionContainer supported"; // see invoke
      //assert interfaceDef != null : "interfaceDef is null";  // assertion breaks iiop unit test
      assert interfaceAnalysis != null : "interfaceAnalysis is null";
      
      this.factory = factory;
      this.poaCurrent = poaCurrent;
      this.container = container;
      this.containerClassLoader = containerClassLoader;
      this.interfaceDef = interfaceDef;
      this.repositoryIds = interfaceAnalysis.getAllTypeIds();
      
      try
      {
         this.sasCurrent = (SASCurrent) CorbaORB.getInstance().resolve_initial_references("SASCurrent");
      }
      catch (InvalidName e)
      {
         log.warn("Can't find SASCurrent");
         this.sasCurrent = null;
      }
      try
      {
         this.inboundTxCurrent = (InboundTransactionCurrent)
            CorbaORB.getInstance().resolve_initial_references(InboundTransactionCurrent.NAME);
      }
      catch (InvalidName e)
      {
         log.warn("Can't find InboundTransactionCurrent");
         this.inboundTxCurrent = null;
      }
      
      this.methodMap = new HashMap<String, SkeletonStrategy>();
      AttributeAnalysis[] attrs = interfaceAnalysis.getAttributes();
      for (int i = 0; i < attrs.length; i++) {
         OperationAnalysis op = attrs[i].getAccessorAnalysis();

         log.debug("    " + op.getJavaName() + ": " + op.getIDLName());
         methodMap.put(op.getIDLName(),
                           new SkeletonStrategy(op.getMethod()));
         op = attrs[i].getMutatorAnalysis();
         if (op != null) {
            log.debug("    " + op.getJavaName() + ": " + op.getIDLName());
            methodMap.put(op.getIDLName(),
                              new SkeletonStrategy(op.getMethod()));
         }
      }

      OperationAnalysis[] ops = interfaceAnalysis.getOperations();
      for (int i = 0; i < ops.length; i++) {
         log.debug("    " + ops[i].getJavaName() + ": " + ops[i].getIDLName());
         methodMap.put(ops[i].getIDLName(),
                           new SkeletonStrategy(ops[i].getMethod()));
      }
   }
   
   @Override
   public String[] _all_interfaces(POA poa, byte[] objectId)
   {
      return (String[]) repositoryIds.clone();
   }

   /**
    * Returns an IR object describing the bean's remote interface.
    */
   @Override
   public org.omg.CORBA.Object _get_interface_def()
   {
      if (interfaceDef != null)
         return interfaceDef;
      else
         return super._get_interface_def();
   }
   
   public OutputStream _invoke(String opName, InputStream in, ResponseHandler handler) throws SystemException
   {
      log.trace("invoke: " + opName);
      
      SkeletonStrategy op = (SkeletonStrategy) methodMap.get(opName);
      if (op == null)
      {
         log.debug("Unable to find opname '" + opName + "' valid operations:" + methodMap.keySet());
         throw new BAD_OPERATION(opName);
      }

      org.omg.CORBA_2_3.portable.OutputStream out;
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(containerClassLoader);

         Object id = ReferenceData.extractObjectId(poaCurrent.get_object_id());
         log.trace("id = " + id);
         
         Transaction tx = null;
         if (inboundTxCurrent != null)
            tx = inboundTxCurrent.getCurrentTransaction();
         log.trace("tx = " + tx);
         
         if(sasCurrent != null)
         {
            byte username[] = sasCurrent.get_incoming_username();
            byte credentials[] = sasCurrent.get_incoming_password();
            byte principalName[] = sasCurrent.get_incoming_principal_name();
            
            if(username != null && username.length > 0)
            {
               String name = new String(username, "UTF-8");
               int domainIndex = name.lastIndexOf("@");
               if(domainIndex > 0)
                  name = name.substring(0, domainIndex);
               log.debug("username = " + name);
               Principal principal = new SimplePrincipal(name);
               SecurityAssociation.setPrincipal(principal);
            }
            
            if(credentials != null && credentials.length > 0)
            {
               SecurityAssociation.setCredential(new String(credentials, "UTF-8").toCharArray());
            }
            
            if(principalName != null && principalName.length > 0)
               log.warn("principalName = " + new String(principalName, "UTF-8")); // FIXME: implement principalName support
         }
         
         Object args[] = op.readParams((org.omg.CORBA_2_3.portable.InputStream) in);
         
         CurrentRemoteProxyFactory.set(factory.getRemoteProxyFactory());
         Object retVal;
         try
         {
            retVal = invoke(tx, id, op.getMethod(), args);
         }
         finally
         {
            CurrentRemoteProxyFactory.remove();
         }
         
         out = (org.omg.CORBA_2_3.portable.OutputStream) handler.createReply();
         if(op.isNonVoid())
            op.writeRetval(out, retVal);
      }
      catch(Throwable t)
      {
         // TODO: we should really get the correct EJB 2.1 view exceptions from the container
         if(t instanceof EJBAccessException)
            throw new NO_PERMISSION(t.getMessage());
         if(t instanceof EJBTransactionRequiredException)
            throw new TRANSACTION_REQUIRED(t.getMessage());
         
         // TODO: check log level before stacktrace?
         t.printStackTrace();
         if(t instanceof Exception)
         {
            Exception e = (Exception) t;
            RmiIdlUtil.rethrowIfCorbaSystemException(e);
            out = (org.omg.CORBA_2_3.portable.OutputStream) handler.createExceptionReply();
            op.writeException(out, e);
         }
         else
            throw new RuntimeException("NYI");
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
      return out;
   }

   private TransactionManager getTransactionManager()
   {
      //return TxUtil.getTransactionManager();
      return TransactionManagerLocator.getInstance().locate();
   }
   
   private Object invoke(Object id, Method method, Object args[]) throws Throwable
   {
      Serializable session = (Serializable) id;
      // TODO: get the proper invoked interface
      Class<?> invokedBusinessInterface = null;
      return container.invoke(session, invokedBusinessInterface, method, args);
   }
   
   private Object invoke(Transaction tx, Object id, Method method, Object args[]) throws Throwable
   {
      if(tx == null)
         return invoke(id, method, args);
      
      // FIXME: refactor TxServerInterceptor so that it pushed the tpc into the invocation like ClientTxPropegationInterceptor
      // this would require the localInvoke to be also refactored, so that it uses invocation instead of localInvoke.
      TransactionManager tm = getTransactionManager();
      
      // see TxPropagationInterceptor
      if(tm.getTransaction() != null)
         throw new RuntimeException("cannot import a transaction context when a transaction is already associated with the thread");
      tm.resume(tx);
      try
      {
         return invoke(id, method, args);
      }
      finally
      {
         tm.suspend();
      }
   }
}
