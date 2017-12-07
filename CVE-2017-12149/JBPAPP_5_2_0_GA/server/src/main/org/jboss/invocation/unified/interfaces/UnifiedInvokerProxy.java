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
package org.jboss.invocation.unified.interfaces;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.serialization.IMarshalledValue;

/**
 * This represents the client side of the EJB invoker.  This invoker uses
 * the remoting framework for making invocations.
 *
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class UnifiedInvokerProxy implements Invoker, Externalizable
{
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

   static final long serialVersionUID = -1108158470271861548L;

   private transient Client client;

   private InvokerLocator locator;

   private boolean strictRMIException = false;

   private String subsystem = "invoker";

   protected final Logger log = Logger.getLogger(getClass());

   static final int VERSION_5_0 = 500;
   static final int CURRENT_VERSION = VERSION_5_0;


   public UnifiedInvokerProxy()
   {
      super();
   }

   public UnifiedInvokerProxy(InvokerLocator locator)
   {
      init(locator);
   }

   public UnifiedInvokerProxy(InvokerLocator locator, boolean isStrictRMIException)
   {
      this.strictRMIException = isStrictRMIException;
      init(locator);
   }

   protected void init(InvokerLocator locator)
   {
      this.locator = locator;

      try
      {
         client = createClient(locator, getSubSystem());
         client.connect();
      }
      catch(Exception e)
      {
         log.fatal("Could not initialize UnifiedInvokerProxy.", e);
      }

   }

   public String getSubSystem()
   {
      return subsystem;
   }

   public void setSubSystem(String subsystem)
   {
      this.subsystem = subsystem;
   }


   public boolean isStrictRMIException()
   {
      return strictRMIException;
   }

   protected Client getClient()
   {
      return client;
   }

   protected InvokerLocator getLocator()
   {
      return locator;
   }

   protected void setLocator(InvokerLocator locator)
   {
      this.locator = locator;
   }

   protected void setStrictRMIException(boolean strictRMIException)
   {
      this.strictRMIException = strictRMIException;
   }


   /**
    * A free form String identifier for this delegate invoker, can be clustered or target node
    * This should evolve in a more advanced meta-inf object.
    * <p/>
    * This will return the host supplied by the invoker locator if locator is not null.  Otherwise, if the locator is null, will
    * return null.
    */
   public String getServerHostName() throws Exception
   {
      if(locator != null)
      {
         return locator.getHost();
      }
      else
      {
         return null;
      }
   }

   /**
    * @param invocation A pointer to the invocation object
    * @return Return value of method invocation.
    * @throws Exception Failed to invoke method.
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      Object response = null;

      // Earlier versions of InvokerLocator don't have a findSerializationType() method.
      try
      {
         invocation.getInvocationContext().setValue("SERIALIZATION_TYPE",locator.findSerializationType());
      }
      catch (NoSuchMethodError e)
      {
         invocation.getInvocationContext().setValue("SERIALIZATION_TYPE", "java");
      }
      
      try
      {
         response = client.invoke(invocation, null);

         if(response instanceof Exception)
         {
            throw ((Exception) response);
         }
         if(response instanceof MarshalledObject)
         {
            return ((MarshalledObject) response).get();
         }
         if (response instanceof IMarshalledValue)
         {
             return ((IMarshalledValue)response).get();
         }
         return response;

      }
      catch(RemoteException aex)
      {
         // per Jira issue JBREM-61
         if(strictRMIException)
         {
            throw new ServerException(aex.getMessage(), aex);
         }
         else
         {
            throw aex;
         }
      }
      catch(Throwable throwable)
      {
         // this is somewhat of a hack as remoting throws throwable,
         // so will let Exception types bubble up, but if Throwable type,
         // then have to wrap in new Exception, as this is the signature
         // of this invoke method.
         if(throwable instanceof Exception)
         {
            throw (Exception) throwable;
         }
         throw new Exception(throwable);
      }
   }

   /**
    * Externalize this instance and handle obtaining the remoteInvoker stub
    */
   public void writeExternal(final ObjectOutput out)
         throws IOException
   {
      out.writeInt(CURRENT_VERSION);

      out.writeUTF(locator.getOriginalURI());
      out.writeBoolean(strictRMIException);
   }

   /**
    * Un-externalize this instance.
    */
   public void readExternal(final ObjectInput in)
         throws IOException, ClassNotFoundException
   {
      int version = in.readInt();
      // Read in and map the version of the serialized data seen
      switch(version)
      {
         case VERSION_5_0:
            locator = new InvokerLocator(in.readUTF());
            strictRMIException = in.readBoolean();
            init(locator);
            break;
         default:
            throw new StreamCorruptedException("Unknown version seen: " + version);
      }
   }

   protected Client createClient(InvokerLocator locator, String subSystem) throws Exception
   {
      return new Client(locator, subSystem);
   }
}
