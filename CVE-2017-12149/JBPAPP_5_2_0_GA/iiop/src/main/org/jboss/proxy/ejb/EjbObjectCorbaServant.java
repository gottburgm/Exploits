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
package org.jboss.proxy.ejb;

import java.security.Principal;
import java.util.Map;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.transaction.Transaction;

import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.UnknownException;
import org.omg.PortableServer.Current;
import org.omg.PortableServer.POA;

import org.jboss.iiop.CorbaORB;
import org.jboss.iiop.csiv2.SASCurrent;
import org.jboss.iiop.rmi.RmiIdlUtil;
import org.jboss.iiop.rmi.marshal.strategy.SkeletonStrategy;
import org.jboss.iiop.tm.InboundTransactionCurrent;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.PayloadKey;
import org.jboss.invocation.iiop.ReferenceData;
import org.jboss.invocation.iiop.ServantWithMBeanServer;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SimplePrincipal;
import org.jboss.naming.ENCFactory;


/**
 * CORBA servant class for the <code>EJBObject</code>s of a given bean. An 
 * instance of this class "implements" the bean's set of <code>EJBObject</code>
 * instances by forwarding to the bean container all IIOP invocations on any
 * of the bean's <code>EJBObject</code>s. Such invocations are routed through 
 * the JBoss <code>MBean</code> server, which delivers them to the target 
 * container. 
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class EjbObjectCorbaServant
   extends ServantWithMBeanServer
   implements InvokeHandler, LocalIIOPInvoker
{

   /**
    * The <code>MBean</code> name of this servant's container.
    */
   private final ObjectName containerName;

   /**
    * The classloader of this servant's container.
    */
   private final ClassLoader containerClassLoader;

   /**
    * Thread-local <code>Current</code> object from which we get the target oid
    * in an incoming IIOP request.
    */
   private final Current poaCurrent;

   /**
    * Mapping from bean methods to <code>SkeletonStrategy</code> instances.
    */
   private final Map methodInvokerMap;

   /**
    * CORBA repository ids of the RMI-IDL interfaces implemented by the bean 
    * (<code>EJBObject</code> instance).
    */
   private final String[] repositoryIds;

   /**
    * CORBA reference to an IR object representing the bean's remote interface.
    */
   private final InterfaceDef interfaceDef;

   /**
    * This servant's logger.
    */
   private final Logger logger;

   /**
    * True is the trace logging level is enabled.
    */
   private final boolean traceEnabled;

   /**
    * A reference to the JBoss <code>MBean</code> server.
    */
   private MBeanServer mbeanServer;

   /**
    * A reference to the SASCurrent, or null if the SAS interceptors are not 
    * installed.
    */
   private SASCurrent sasCurrent;
   
   /** 
    * A reference to the InboundTransactionCurrent, or null if OTS interceptors
    * are not installed.
    */
   private InboundTransactionCurrent inboundTxCurrent;

   /**
    * Constructs an <code>EjbObjectCorbaServant></code>.
    */
   public EjbObjectCorbaServant(ObjectName containerName,
      ClassLoader containerClassLoader,
      Current poaCurrent,
      Map methodInvokerMap,
      String[] repositoryIds,
      InterfaceDef interfaceDef,
      Logger logger)
   {
      this.containerName = containerName;
      this.containerClassLoader = containerClassLoader;
      this.poaCurrent = poaCurrent;
      this.methodInvokerMap = methodInvokerMap;
      this.repositoryIds = repositoryIds;
      this.interfaceDef = interfaceDef;
      this.logger = logger;
      this.traceEnabled = logger.isTraceEnabled();
      try
      {
         this.sasCurrent = (SASCurrent)
            CorbaORB.getInstance().resolve_initial_references("SASCurrent");
      }
      catch (InvalidName invalidName)
      {
         this.sasCurrent = null;
      }
      try
      {
         this.inboundTxCurrent = (InboundTransactionCurrent)
            CorbaORB.getInstance().resolve_initial_references(InboundTransactionCurrent.NAME);
      }
      catch (InvalidName invalidName)
      {
         this.inboundTxCurrent = null;
      }
   }

   // Implementation of method declared as abstract in the superclass ------

   /**
    * Sets this servant's <code>MBeanServer</code>.
    */
   public void setMBeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }
   
   // This method overrides the one in org.omg.PortableServer.Servant ------

   /**
    * Returns an IR object describing the bean's remote interface.
    */
   public org.omg.CORBA.Object _get_interface_def()
   {
      if (interfaceDef != null)
         return interfaceDef;
      else
         return super._get_interface_def();
   }
   
   // Implementation of org.omg.CORBA.portable.InvokeHandler ---------------

   /**
    * Returns an array with the CORBA repository ids of the RMI-IDL interfaces 
    * implemented by this servant's <code>EJBObject</code>s.
    */
   public String[] _all_interfaces(POA poa, byte[] objectId)
   {
      return (String[]) repositoryIds.clone();
   }

   /**
    * Receives IIOP requests to this servant's <code>EJBObject</code>s
    * and forwards them to the bean container, through the JBoss 
    * <code>MBean</code> server. 
    */
   public OutputStream _invoke(String opName,
      InputStream in,
      ResponseHandler handler)
   {

      if (traceEnabled)
      {
         logger.trace("EJBObject invocation: " + opName);
      }

      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(containerClassLoader);
      try
      {
         SkeletonStrategy op = (SkeletonStrategy) methodInvokerMap.get(opName);
         if (op == null)
         {
            logger.debug("Unable to find opname '" + opName + "' valid operations:" + methodInvokerMap.keySet());
            throw new BAD_OPERATION(opName);
         }

         Object id;
         try
         {
            id = ReferenceData.extractObjectId(poaCurrent.get_object_id());
            if (traceEnabled && id != null)
               logger.trace("                      id class is "
                  + id.getClass().getName());
         }
         catch (Exception e)
         {
            logger.error("Error getting EJBObject id", e);
            throw new UnknownException(e);
         }

         // make java:comp work
         ENCFactory.pushContextId(containerName);

         org.omg.CORBA_2_3.portable.OutputStream out;
         try
         {
            Object retVal;

            if (opName.equals("_get_handle"))
            {
               javax.ejb.EJBObject obj = 
                  (javax.ejb.EJBObject)javax.rmi.PortableRemoteObject.narrow(_this_object(), javax.ejb.EJBObject.class);
               retVal = new HandleImplIIOP(obj); 
            }
            else
            {
               Transaction tx = null;
               if (inboundTxCurrent != null)
                  tx = inboundTxCurrent.getCurrentTransaction();
               SimplePrincipal principal = null;
               char[] password = null;
               if (sasCurrent != null)
               {
                  byte[] username = sasCurrent.get_incoming_username();
                  byte[] credential = sasCurrent.get_incoming_password();
                  String name = new String(username, "UTF-8");
                  int domainIndex = name.indexOf('@');
                  if (domainIndex > 0)
                     name = name.substring(0, domainIndex);
                  if (name.length() == 0)
                  {
                     byte[] incomingName = 
                        sasCurrent.get_incoming_principal_name();
                     if (incomingName.length > 0)
                     {
                        name = new String(incomingName, "UTF-8");
                        domainIndex = name.indexOf('@');
                        if (domainIndex > 0)
                           name = name.substring(0, domainIndex);
                        principal = new SimplePrincipal(name);
                        // username==password is a hack until 
                        // we have a real way to establish trust
                        password = name.toCharArray();                  
                     }
                  }
                  else
                  {
                     principal = new SimplePrincipal(name);
                     password = new String(credential, "UTF-8").toCharArray();
                  }

               }
               Object[] params =
                  op.readParams((org.omg.CORBA_2_3.portable.InputStream) in);
               Invocation inv = new Invocation(id,
                  op.getMethod(),
                  params,
                  tx,
                  principal, /* identity */
                  password  /* credential */);
               inv.setValue(InvocationKey.INVOKER_PROXY_BINDING,
                  "iiop",
                  PayloadKey.AS_IS);
               inv.setType(InvocationType.REMOTE);

               SecurityContext sc = SecurityContextFactory.createSecurityContext("CORBA_REMOTE");
               sc.getUtil().createSubjectInfo(principal, password, null); 
               inv.setSecurityContext(sc);

               retVal = mbeanServer.invoke(containerName,
                  "invoke",
                  new Object[]{inv},
                  Invocation.INVOKE_SIGNATURE);

            }
            out = (org.omg.CORBA_2_3.portable.OutputStream)
               handler.createReply();
            if (op.isNonVoid())
            {
               op.writeRetval(out, retVal);
            }
         }
         catch (Exception e)
         {
            if (traceEnabled)
            {
               logger.trace("Exception in EJBObject invocation", e);
            }
            if (e instanceof MBeanException)
            {
               e = ((MBeanException) e).getTargetException();
            }
            RmiIdlUtil.rethrowIfCorbaSystemException(e);
            out = (org.omg.CORBA_2_3.portable.OutputStream)
               handler.createExceptionReply();
            op.writeException(out, e);
         }
         return out;
      }
      finally
      {
         // pop ENC context
         ENCFactory.popContextId();
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
   
   // Implementation of the interface LocalIIOPInvoker ------------------------

   /**
    * Receives intra-VM invocations on this servant's <code>EJBObject</code>s
    * and forwards them to the bean container, through the JBoss 
    * <code>MBean</code> 
    * server. 
    */
   public Object invoke(String opName,
      Object[] arguments,
      Transaction tx,
      Principal identity,
      Object credential)
      throws Exception
   {
      if (traceEnabled)
      {
         logger.trace("EJBObject local invocation: " + opName);
      }

      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(containerClassLoader);

      try
      {
         SkeletonStrategy op = (SkeletonStrategy) methodInvokerMap.get(opName);
         if (op == null)
         {
            throw new BAD_OPERATION(opName);
         }

         Object id;
         try
         {
            id = ReferenceData.extractObjectId(poaCurrent.get_object_id());
            if (traceEnabled && id != null)
            {
               logger.trace("                      id class is "
                            + id.getClass().getName());
            }
         }
         catch (Exception e)
         {
            logger.error("Error getting EJBObject id", e);
            throw new UnknownException(e);
         }

         Invocation inv = new Invocation(id,
            op.getMethod(),
            arguments,
            tx,
            null, /* identity */
            null  /* credential */);
         inv.setValue(InvocationKey.INVOKER_PROXY_BINDING,
            "iiop",
            PayloadKey.AS_IS);
         inv.setType(InvocationType.REMOTE);
         return mbeanServer.invoke(containerName,
            "invoke",
            new Object[]{inv},
            Invocation.INVOKE_SIGNATURE);
      }
      catch (MBeanException e)
      {
         throw e.getTargetException();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

}
