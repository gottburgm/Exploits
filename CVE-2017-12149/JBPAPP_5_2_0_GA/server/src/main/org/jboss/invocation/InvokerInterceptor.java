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
package org.jboss.invocation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.transaction.Transaction;

import org.jboss.proxy.Interceptor;
import org.jboss.proxy.ClientContainer;

import org.jboss.system.Registry;
import org.jboss.util.id.GUID;
import org.jboss.remoting.serialization.IMarshalledValue;
import org.jboss.remoting.serialization.SerializationStreamFactory;
import org.jboss.serial.objectmetamodel.safecloning.SafeClone;

/**
 * A very simple implementation of it that branches to the local stuff.
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 111929 $
 */
public class InvokerInterceptor
   extends Interceptor
   implements Externalizable
{
   /** Serial Version Identifier. @since 1.2 */
   private static final long serialVersionUID = 2548120545997920357L;

   /** The value of our local Invoker.ID to detect when we are local. */
   private GUID invokerID = Invoker.ID;

   /** Invoker to the remote JMX node. */
   protected Invoker remoteInvoker;

   /** Static references to local invokers. */
   protected static Invoker localInvoker;

   /** The InvokerProxyHA class */
   protected static Class invokerProxyHA;
   
   // JBPAPP-6428 when determining isLocal, if this is true take into account the cluster partition name
   private static final boolean isLocalCheckPartitionName = Boolean.parseBoolean( System.getProperty("org.jboss.invocation.use.partition.name", "false") );
   // JBPAPP-6428 - these are static variables to cache the reflection methods needed for JBPAPP-6428  
   private static Method getFamilyClusterInfo;
   private static Method getFamilyName;

   static
   {
       try
       {
          // Using Class.forName() to avoid security problems in the client
          invokerProxyHA = Class.forName("org.jboss.invocation.InvokerProxyHA");
       }
       catch (Throwable ignored)
       {
       }

       try
       {
          // To guarantee backwards compatibility, we need to make sure this SerializationManager is being used
          Class.forName("org.jboss.invocation.unified.interfaces.JavaSerializationManager");
       }
       catch (Throwable ignored)
       {
       }

   }

   /**
    * Get the local invoker reference, useful for optimization.
    */
   public static Invoker getLocal()
   {
      return localInvoker;
   }

   /**
    * Set the local invoker reference, useful for optimization.
    */
   public static void setLocal(Invoker invoker)
   {
      localInvoker = invoker;
   }

   /**
    * Exposed for externalization.
    */
   public InvokerInterceptor()
   {
      super();
   }

   /**
    * Returns wether we are local to the originating container or not.
    *
    * @return true when we have the same GUID
    */
   public boolean isLocal()
   {
      return invokerID.equals(Invoker.ID);
   }

   /**
    * Whether the target is local
    *
    * @param invocation the invocation
    * @return true when the target is local
    */
   public boolean isLocal(Invocation invocation)
   {
      // No local invoker, it must be remote
      if (localInvoker == null)
         return false;

      // The proxy was downloaded from a remote location
      if (isLocal() == false)
      {
         // It is not clustered so we go remote
         if (isClustered(invocation) == false)
            return false;
      }

      // JBPAPP-6428 - EJB2 InvokerInterceptor isLocal returns true when it should return false
      // Only check the cluster names if it is clustered
      if (isLocalCheckPartitionName == true)
      {
         if (isClustered(invocation) == true)
         {
        	 try
        	 {
	           String containerPartitionName = System.getProperty("jboss.partition.name");
	           
	           Object invokerProxyHA = invocation.getInvocationContext().getInvoker();
	           
	           // reflection to org.jboss.invocation.InvokerProxyHA.getFamilyClusterInfo().getFamilyName()
	           String destinationPartitionName = getFamilyName(invokerProxyHA);
	           
	           if (containerPartitionName != null)
	           {
	             if (! destinationPartitionName.startsWith(containerPartitionName + "/"))
	               return false;
	           }
        	 }
           // Catch any reflection exception - which should not occur since isClustered == true
        	 catch ( Exception e )
        	 {
        		 e.printStackTrace();
        	 }
         }
      }
      // end JBPAPP-6428
      
      // See whether we have a local target
      return hasLocalTarget(invocation);
   }

   private String getFamilyName(Object invokerProxyHA) throws Exception
   {
      if(getFamilyClusterInfo == null)
         getFamilyClusterInfo = invokerProxyHA.getClass().getMethod("getFamilyClusterInfo", new Class[0]);
      
      Object familyClusterInfo = getFamilyClusterInfo.invoke(invokerProxyHA, new Object[0]);
      
      if(getFamilyName == null)
         getFamilyName = familyClusterInfo.getClass().getMethod("getFamilyName", new Class[0]);
               
      return (String) getFamilyName.invoke(familyClusterInfo, new Object[0]);      
   }
   
   
   /**
    * Whether we are in a clustered environment<p>
    *
    * NOTE: This should be future compatible under any
    * new design where a prior target chooser interceptor
    * picks a non HA target than that code being
    * inside a ha invoker.
    *
    * @param invocation the invocation
    * @return true when a clustered invoker
    */
   public boolean isClustered(Invocation invocation)
   {
      // No clustering classes
      if (invokerProxyHA == null)
         return false;

      // Is the invoker a HA invoker?
      InvocationContext ctx = invocation.getInvocationContext();
      Invoker invoker = ctx.getInvoker();
      return invoker != null && invokerProxyHA.isAssignableFrom(invoker.getClass());
   }

   /**
    * Whether there is a local target
    *
    * @param invocation
    * @return true when in the registry
    */
   public boolean hasLocalTarget(Invocation invocation)
   {
      return Registry.lookup(invocation.getObjectName()) != null;
   }

   /**
    * The invocation on the delegate, calls the right invoker.
    * Remote if we are remote, local if we are local.
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      // optimize if calling another bean in same server VM
      if (isLocal(invocation))
         return invokeLocal(invocation);
      else
         return invokeInvoker(invocation);
   }

   /**
    * Invoke using local invoker
    *
    * @param invocation the invocation
    * @return the result
    * @throws Exception for any error
    */
   protected Object invokeLocal(Invocation invocation) throws Exception
   {
      return localInvoker.invoke(invocation);
   }

   /**
    * Invoke using local invoker and marshalled
    *
    * @param invocation the invocation
    * @return the result
    * @throws Exception for any error
    */
   protected Object invokeMarshalled(Invocation invocation) throws Exception
   {
      MarshalledInvocation mi = new MarshalledInvocation(invocation);
      MarshalledValue copy = new MarshalledValue(mi);
      Invocation invocationCopy = (Invocation) copy.get();

      // copy the Tx
      Transaction tx = invocation.getTransaction();
      invocationCopy.setTransaction(tx);

      try
      {
         Object rtnValue = localInvoker.invoke(invocationCopy);
         MarshalledValue mv = new MarshalledValue(rtnValue);
         return mv.get();
      }
      catch(Throwable t)
      {
         MarshalledValue mv = new MarshalledValue(t);
         Throwable t2 = (Throwable) mv.get();
         if( t2 instanceof Exception )
            throw (Exception) t2;
         else
            throw new UndeclaredThrowableException(t2);
      }
   }


   /** These objects are safe to reuse in callByValue operations */
   static final SafeClone safeToReuse = new SafeClone(){
                 public boolean isSafeToReuse(Object obj) {
                     if (obj==null)
                     {
                         return false;
                     }

                     if (obj instanceof ClientContainer ||
                         obj instanceof String ||
                         obj instanceof Number ||
                         obj instanceof BigDecimal ||
                         obj instanceof BigInteger ||
                         obj instanceof Byte ||
                         obj instanceof Double ||
                         obj instanceof Float ||
                         obj instanceof Integer ||
                         obj instanceof Long ||
                         obj instanceof Short)
                     {
                         return true;
                     }
                     else
                     {
                         return false;
                     }
                 }
             };


   protected IMarshalledValue createMarshalledValueForCallByValue(Object value) throws IOException
   {
	   return SerializationStreamFactory.getManagerInstance().createdMarshalledValue(value);
   }
             
   /** This method is for local calls when using pass-by-value*/
   protected Object invokeLocalMarshalled(Invocation invocation) throws Exception
   {

       IMarshalledValue value = createMarshalledValueForCallByValue(invocation.getArguments());
       MarshalledInvocation invocationCopy = createInvocationCopy(invocation, value);

      // copy the Tx
      Transaction tx = invocation.getTransaction();
      invocationCopy.setTransaction(tx);

      try
      {
         Object rtnValue = localInvoker.invoke(invocationCopy);
         IMarshalledValue mv = createMarshalledValueForCallByValue(rtnValue);
         return mv.get();
      }
      catch(Throwable t)
      {
         IMarshalledValue mv = SerializationStreamFactory.getManagerInstance().createdMarshalledValue(t);
         Throwable t2 = (Throwable) mv.get();
         if( t2 instanceof Exception )
            throw (Exception) t2;
         else
            throw new UndeclaredThrowableException(t2);
      }
   }

    /**  It is too expensive to serialize this entire object just to get class isolation
         and MarshalledValues on local calls.
         We are creating an in-memory copy without using serialization for that matter. 
     * @throws ClassNotFoundException 
     * @throws IOException */
    private MarshalledInvocation createInvocationCopy(Invocation invocation, IMarshalledValue value) throws IOException, ClassNotFoundException {
    	
    		
        MarshalledInvocation invocationCopy = new MarshalledInvocation(invocation);
        invocationCopy.setMethod(null);
        invocationCopy.setMethodHash(MarshalledInvocation.calculateHash(invocation.getMethod()));
        invocationCopy.setMarshalledArguments(value);
        invocationCopy.setArguments(null);
        
    	InvocationContext copyContext = null;
    	if (invocation.getInvocationContext()!=null)
    	{
    		copyContext = (InvocationContext)createMarshalledValueForCallByValue(invocation.getInvocationContext()).get();
    	}
        invocationCopy.setInvocationContext(copyContext);
        
        Map payLoad = invocation.getPayload();
        Map payloadCopy = new HashMap();
        
        if (payLoad!=null && payLoad.size()!=0)
        {
        	
            Iterator keys = payLoad.keySet().iterator();
            while (keys.hasNext())
            {
               Object currentKey = keys.next();
               Object valueSource = payLoad.get(currentKey);
               
               payloadCopy.put(currentKey,this.createMarshalledValueForCallByValue(valueSource));
            }
        }

        invocationCopy.payload = payloadCopy;
        
        

        return invocationCopy;
    }

    /**
     * Invoke using invoker
     *
     * @param invocation the invocation
     * @return the result
     * @throws Exception for any error
     */
    protected Object invokeInvoker(Invocation invocation) throws Exception
    {
       InvocationContext ctx = invocation.getInvocationContext();
       Invoker invoker = ctx.getInvoker();
       return invoker.invoke(invocation);
    }

   /**
    * Externalize this instance.
    *
    * <p>
    * If this instance lives in a different VM than its container
    * invoker, the remote interface of the container invoker is
    * not externalized.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      out.writeObject(invokerID);
   }

   /**
    * Un-externalize this instance.
    *
    * <p>
    * We check timestamps of the interfaces to see if the instance is in the original
    * VM of creation
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      invokerID = (GUID)in.readObject();
   }
}
