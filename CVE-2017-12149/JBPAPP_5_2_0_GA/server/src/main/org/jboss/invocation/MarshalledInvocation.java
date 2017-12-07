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

import org.jboss.remoting.serialization.IMarshalledValue;
import org.jboss.remoting.serialization.SerializationStreamFactory;

import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.WeakHashMap;
import javax.transaction.Transaction;

/**
 * The MarshalledInvocation is an invocation that travels.  As such it serializes
 * its payload because of lack of ClassLoader visibility.
 * As such it contains Marshalled data representing the byte[] of the Invocation object it extends
 * Besides handling the specifics of "marshalling" the payload, which could be done at the Invocation level
 * the Marshalled Invocation can hold optimization and needed code for distribution for example the
 * TransactionPropagationContext which is a serialization of the TX for distribution purposes as
 * well as the "hash" for the methods that we send, as opposed to sending Method objects.
 * Serialization "optimizations" should be coded here in the externalization implementation of the class
 *
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author Bill.Burke@jboss.org
 * @author Scott.Stark@jboss.org
 * @author Clebert.Suconic@jboss.org - added pluggable serialization
 * @version $Revision: 81179 $
 */
public class MarshalledInvocation
        extends Invocation
        implements java.io.Externalizable
{
   // Constants -----------------------------------------------------

   static
   {
      try
      {
          Class.forName("org.jboss.invocation.unified.interfaces.JavaSerializationManager");
      }
      catch (Exception e)
      {
      }
   }

   /** Serial Version Identifier. */
   static final long serialVersionUID = -718723094688127810L;
   /** A flag indicating if the full hash format that includes the interface
    * should be used
    */
   static boolean useFullHashMode = true;
   /** WeakHashMap<Class, HashMap<String, Long>> of declaring class to hashes */
   static Map hashMap = new WeakHashMap();

   /** The Transaction Propagation Context for distribution */
   protected Object tpc;

   /** The Map of methods used by this Invocation */
   protected transient Map methodMap;

   // These are here to avoid unneeded hash lookup
   protected transient long methodHash = 0;
   protected transient Object marshalledArgs = null;

    public long getMethodHash() {
        return methodHash;
    }

    public void setMethodHash(long methodHash) {
        this.methodHash = methodHash;
    }

    /** Get the full hash mode flag.
    * @return the full hash mode flag.
    */
   public static boolean getUseFullHashMode()
   {
      return useFullHashMode;
   }
   /** Set the full hash mode flag. When true, method hashes are calculated
    * using the getFullInterfaceHashes which is able to differentiate methods
    * by declaring class, return value, name and arg signature, and exceptions.
    * Otherwise, the getInterfaceHashes method uses, and this is only able to
    * differentiate classes by return value, name and arg signature. A
    * useFullHashMode = false is compatible with 3.2.3 and earlier.
    *
    * This needs to be set consistently on the server and the client.
    *
    * @param flag the full method hash calculation mode flag.
    */
   public static void setUseFullHashMode(boolean flag)
   {
      useFullHashMode = flag;
   }

   /** Calculate method hashes. This algo is taken from RMI with the
    * method string built from the method name + parameters + return type. Note
    * that this is not able to distinguish type compatible methods from
    * different interfaces.
    *
    * @param intf - the class/interface to calculate method hashes for.
    * @return Map<String, Long> mapping of method string to method desc hash
    */
   public static Map getInterfaceHashes(Class intf)
   {
      // Create method hashes
      Method[] methods = null;
      if( System.getSecurityManager() != null )
      {
         DeclaredMethodsAction action = new DeclaredMethodsAction(intf);
         methods = (Method[]) AccessController.doPrivileged(action);
      }
      else
      {
         methods = intf.getDeclaredMethods();
      }

      HashMap map = new HashMap();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         Class[] parameterTypes = method.getParameterTypes();
         String methodDesc = method.getName() + "(";
         for (int j = 0; j < parameterTypes.length; j++)
         {
            methodDesc += getTypeString(parameterTypes[j]);
         }
         methodDesc += ")" + getTypeString(method.getReturnType());

         try
         {
            long hash = 0;
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(512);
            MessageDigest messagedigest = MessageDigest.getInstance("SHA");
            DataOutputStream dataoutputstream = new DataOutputStream(new DigestOutputStream(bytearrayoutputstream, messagedigest));
            dataoutputstream.writeUTF(methodDesc);
            dataoutputstream.flush();
            byte abyte0[] = messagedigest.digest();
            for (int j = 0; j < Math.min(8, abyte0.length); j++)
               hash += (long) (abyte0[j] & 0xff) << j * 8;
            map.put(method.toString(), new Long(hash));
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      return map;
   }

   /** Calculate method full hashes. This algo is taken from RMI with the full
    * method string built from the method toString() which includes the
    * modifiers, return type, declaring class, name, parameters and exceptions.
    *
    * @param intf - the class/interface to calculate method hashes for.
    * @return Map<String, Long> mapping of method string to method desc hash
    */
   public static Map getFullInterfaceHashes(Class intf)
   {
      // Create method hashes
      Method[] methods = null;
      if( System.getSecurityManager() != null )
      {
         DeclaredMethodsAction action = new DeclaredMethodsAction(intf);
         methods = (Method[]) AccessController.doPrivileged(action);
      }
      else
      {
         methods = intf.getDeclaredMethods();
      }

      HashMap map = new HashMap();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         String methodDesc = method.toString();

         try
         {
            long hash = 0;
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(512);
            MessageDigest messagedigest = MessageDigest.getInstance("SHA");
            DataOutputStream dataoutputstream = new DataOutputStream(new DigestOutputStream(bytearrayoutputstream, messagedigest));
            dataoutputstream.writeUTF(methodDesc);
            dataoutputstream.flush();
            byte abyte0[] = messagedigest.digest();
            for (int j = 0; j < Math.min(8, abyte0.length); j++)
               hash += (long) (abyte0[j] & 0xff) << j * 8;
            map.put(method.toString(), new Long(hash));
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      return map;
   }

   /** Calculate method hashes. This algo is taken from RMI with the full
    * method string taken from the method.toString to include the declaring
    * class.
    *
    * @param c the class/interface to calculate method hashes for.
    * @return Map<Long, Method> mapping of method hash to the Method object.
    */
   public static Map methodToHashesMap(Class c)
   {
      // Create method hashes
      Method[] methods = null;
      if( System.getSecurityManager() != null )
      {
         DeclaredMethodsAction action = new DeclaredMethodsAction(c);
         methods = (Method[]) AccessController.doPrivileged(action);
      }
      else
      {
         methods = c.getDeclaredMethods();
      }

      HashMap map = new HashMap();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         String methodDesc = method.toString();

         try
         {
            long hash = 0;
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(512);
            MessageDigest messagedigest = MessageDigest.getInstance("SHA");
            DataOutputStream dataoutputstream = new DataOutputStream(new DigestOutputStream(bytearrayoutputstream, messagedigest));
            dataoutputstream.writeUTF(methodDesc);
            dataoutputstream.flush();
            byte abyte0[] = messagedigest.digest();
            for (int j = 0; j < Math.min(8, abyte0.length); j++)
               hash += (long) (abyte0[j] & 0xff) << j * 8;
            map.put(new Long(hash), method);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      return map;
   }

   static String getTypeString(Class cl)
   {
      if (cl == Byte.TYPE)
      {
         return "B";
      }
      else if (cl == Character.TYPE)
      {
         return "C";
      }
      else if (cl == Double.TYPE)
      {
         return "D";
      }
      else if (cl == Float.TYPE)
      {
         return "F";
      }
      else if (cl == Integer.TYPE)
      {
         return "I";
      }
      else if (cl == Long.TYPE)
      {
         return "J";
      }
      else if (cl == Short.TYPE)
      {
         return "S";
      }
      else if (cl == Boolean.TYPE)
      {
         return "Z";
      }
      else if (cl == Void.TYPE)
      {
         return "V";
      }
      else if (cl.isArray())
      {
         return "[" + getTypeString(cl.getComponentType());
      }
      else
      {
         return "L" + cl.getName().replace('.', '/') + ";";
      }
   }

   /*
   * The use of hashCode is not enough to differenciate methods
   * we override the hashCode
   *
   * The hashes are cached in a static for efficiency
   */
   public static long calculateHash(Method method)
   {
      Map methodHashes = (Map) hashMap.get(method.getDeclaringClass());

      if (methodHashes == null)
      {
         // Add the method hashes for the class
         if( useFullHashMode == true )
            methodHashes = getFullInterfaceHashes(method.getDeclaringClass());
         else
            methodHashes = getInterfaceHashes(method.getDeclaringClass());
         synchronized (hashMap)
         {
            hashMap.put(method.getDeclaringClass(), methodHashes);
         }
      }

      Long hash = (Long) methodHashes.get(method.toString());
      return hash.longValue();
   }


   /** Remove all method hashes for the declaring class
    * @param declaringClass a class for which a calculateHash(Method) was called
    */
   public static void removeHashes(Class declaringClass)
   {
      synchronized (hashMap)
      {
         hashMap.remove(declaringClass);
      }
   }

   // Constructors --------------------------------------------------
   public MarshalledInvocation()
   {
      // For externalization to work
   }

   public MarshalledInvocation(Invocation invocation)
   {
      this.payload = invocation.payload;
      this.as_is_payload = invocation.as_is_payload;
      this.method = invocation.getMethod();
      this.objectName = invocation.getObjectName();
      this.args = invocation.getArguments();
      this.invocationType = invocation.getType();
      this.transient_payload = invocation.transient_payload;
      this.invocationContext = invocation.invocationContext;
   }


   public MarshalledInvocation(
           Object id,
           Method m,
           Object[] args,
           Transaction tx,
           Principal identity,
           Object credential)
   {
      super(id, m, args, tx, identity, credential);
   }

   public Method getMethod()
   {
      if (this.method != null)
         return this.method;

      // Try the hash, the methodMap should be set
      this.method = (Method) methodMap.get(new Long(methodHash));

      // Keep it in the payload
      if (this.method == null)
      {
         throw new IllegalStateException("Failed to find method for hash:" + methodHash + " available=" + methodMap);
      }
      return this.method;
   }

   public void setMethodMap(Map methods)
   {
      methodMap = methods;
   }

   // The transaction propagation context for the Invocation that travels (distributed tx only)
   public void setTransactionPropagationContext(Object tpc)
   {
      this.tpc = tpc;
   }

   public Object getTransactionPropagationContext()
   {
      return tpc;
   }

   // Invocation overwrite -----------------------------------------

   /** A Marshalled invocation has serialized data in the form of
    MarshalledValue objects. We overwrite the "getValue" to deserialize the
    data, this assume that the thread context class loader has visibility
    on the classes.
    */
   public Object getValue(Object key)
   {

      Object value = super.getValue(key);

      // The map may contain serialized values of the fields
      if (value instanceof IMarshalledValue)
      {
         try
         {
            IMarshalledValue mv = (IMarshalledValue) value;
            value = mv.get();
         }
         catch (Exception e)
         {
            JBossLazyUnmarshallingException ise = new JBossLazyUnmarshallingException("getValue failed");
            ise.initCause(e);
            throw ise;
         }
      }
      return value;
   }

   /** A Marshalled invocation has serialized data in the form of
    MarshalledValue objects. We overwrite the "getValue" to deserialize the
    data, this assume that the thread context class loader has visibility
    on the classes.
    */
   public Object getPayloadValue(Object key)
   {

      Object value = getPayload().get(key);

      // The map may contain serialized values of the fields
       if (value instanceof MarshalledValue)
       {
          try
          {
             MarshalledValue mv = (MarshalledValue) value;
             value = mv.get();
          }
          catch (Exception e)
          {
             JBossLazyUnmarshallingException ise = new JBossLazyUnmarshallingException("getPayloadValue failed");
             ise.initCause(e);
             throw ise;
          }
       }
       else
       if (value instanceof IMarshalledValue)
       {
           try
           {
              IMarshalledValue mv = (IMarshalledValue) value;
              value = mv.get();
           }
           catch (Exception e)
           {
              JBossLazyUnmarshallingException ise = new JBossLazyUnmarshallingException("getPayloadValue failed");
              ise.initCause(e);
              throw ise;
           }
       }
      return value;
   }

   public Object[] getArguments()
   {
      if (this.args == null)
      {
    	  if (marshalledArgs instanceof MarshalledValue)
    	  {
             try
             {
                this.args = (Object[]) ((MarshalledValue)marshalledArgs).get();
             }
             catch (Exception e)
             {
                JBossLazyUnmarshallingException ise = new JBossLazyUnmarshallingException("getArguments failed");
                ise.initCause(e);
                throw ise;
             }
    	  }
    	  else
    	  if (marshalledArgs instanceof IMarshalledValue)
    	  {
              try
              {
                 this.args = (Object[]) ((IMarshalledValue)marshalledArgs).get();
              }
              catch (Exception e)
              {
                 JBossLazyUnmarshallingException ise = new JBossLazyUnmarshallingException("getArguments failed");
                 ise.initCause(e);
                 throw ise;
              }
    	  }
      }
      return args;
   }

   // Externalizable implementation ---------------------------------
   public void writeExternal(java.io.ObjectOutput out)
           throws IOException
   {
      // TODO invocationType should be removed from as is payload
      // for now, it is in there for binary compatibility
      getAsIsPayload().put(InvocationKey.TYPE, invocationType);
      // FIXME marcf: the "specific" treatment of Transactions should be abstracted.
      // Write the TPC, not the local transaction
      out.writeObject(tpc);

      long methodHash = this.methodHash;
      if(methodHash == 0)
      {
         methodHash = calculateHash(this.method);
      }

      out.writeLong(methodHash);

      out.writeObject(this.objectName);

      String serializationType = null;

      if (invocationContext!=null)
      {
          serializationType = (String)invocationContext.getValue("SERIALIZATION_TYPE");
      }

      if(this.args == null && this.marshalledArgs != null)
      {
         out.writeObject(this.marshalledArgs);
      }
      else
      {
         out.writeObject(createMarshalledValue(serializationType,this.args));
      }


      // Write out payload hashmap
      // Don't use hashmap serialization to avoid not-needed data being
      // marshalled
      // The map contains only serialized representations of every other object
      // Everything else is possibly tied to classloaders that exist inside the
      // server but not in the generic JMX land. they will travel in the  payload
      // as MarshalledValue objects, see the Invocation getter logic
      //
      if (payload == null)
         out.writeInt(0);
      else
      {
         out.writeInt(payload.size());
         Iterator keys = payload.keySet().iterator();
         while (keys.hasNext())
         {
            Object currentKey = keys.next();

            // This code could be if (object.getClass().getName().startsWith("java")) then don't serialize.
            // Bench the above for speed.

            out.writeObject(currentKey);
            Object value = payload.get(currentKey);
            // no reason to marshall an already marshalled value
            if(!(value instanceof MarshalledValue))
            {
               value = createMarshalledValue(serializationType,value);
            }

            out.writeObject(value);
         }
      }

      // This map is "safe" as is
      //out.writeObject(as_is_payload);
      if (as_is_payload == null)
         out.writeInt(0);
      else
      {
         out.writeInt(as_is_payload.size());

         Iterator keys = as_is_payload.keySet().iterator();
         while (keys.hasNext())
         {
            Object currentKey = keys.next();
            out.writeObject(currentKey);
            out.writeObject(as_is_payload.get(currentKey));
         }
      }
   }

    private Object createMarshalledValue(String serializationType, Object valueToBeMarshalled) throws IOException {
        if (serializationType!=null)
        {
            return (IMarshalledValue)SerializationStreamFactory.getManagerInstance(serializationType).createdMarshalledValue(valueToBeMarshalled);
        }
        else
        {
            return new MarshalledValue(valueToBeMarshalled);
        }
    }

    public void readExternal(java.io.ObjectInput in)
            throws IOException, ClassNotFoundException
    {
       tpc = in.readObject();
       this.methodHash = in.readLong();

       this.objectName = in.readObject();

       marshalledArgs = in.readObject();

       int payloadSize = in.readInt();
       if (payloadSize > 0)
       {
          payload = new HashMap();
          for (int i = 0; i < payloadSize; i++)
          {
             Object key = in.readObject();
             Object value = in.readObject();
             payload.put(key, value);
          }
       }

       int as_is_payloadSize = in.readInt();
       if (as_is_payloadSize > 0)
       {
          as_is_payload = new HashMap();
          for (int i = 0; i < as_is_payloadSize; i++)
          {
             Object key = in.readObject();
             Object value = in.readObject();
             as_is_payload.put(key, value);
          }
       }
       // TODO invocationType should be removed from as is payload
       // for now, it is in there for binary compatibility
       invocationType = (InvocationType)getAsIsValue(InvocationKey.TYPE);
    }

   /**
    * This is method is used for chaing the MarshalledAruments in a Call-By-Value operation.
    * */
   public void setMarshalledArguments(IMarshalledValue marshalledValue)
   {
      marshalledArgs = marshalledValue;
   }

   private static class DeclaredMethodsAction implements PrivilegedAction
   {
      Class c;
      DeclaredMethodsAction(Class c)
      {
         this.c = c;
      }
      public Object run()
      {
         Method[] methods = c.getDeclaredMethods();
         c = null;
         return methods;
      }
   }
}
