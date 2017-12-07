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

import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.ServiceUnavailableException;
import org.jboss.logging.Logger;
import org.jboss.naming.NamingContextFactory;
import org.jboss.proxy.Interceptor;

/** An interceptor that will retry failed invocations by restoring the 
 * InvocationContext invoker. This is triggered by a ServiceUnavailableException
 * which causes the interceptor to fall into a while loop that retries the
 * lookup of the transport invoker using the jndi name obtained from the
 * invocation context under the key InvocationKey.JNDI_NAME, with the additional
 * extension of "-RemoteInvoker" if the invocation type is InvocationType.REMOTE
 * and "-HomeInvoker" if the invocation type is InvocationType.HOME.
 * 
 * The JNDI environment used for the lookup can be set via the setRetryEnv.
 * Typically this is an HA-JNDI configuration with one or more bootstrap
 * urls. If not set, an attempt will be made to use
 * {@link NamingContextFactory#getInitialContext(Hashtable)} to find the 
 * JNDI environment.  This will only be useful if java.naming.factory.initial
 * was set to org.jboss.naming.NamingContextFactory.  If neither of the above
 * steps yield a set of naming environment properties, a default InitialContext
 * will be used. 
 * 
 * @author Scott.Stark@jboss.org 
 * @author brian.stansberry@jboss.org
 * 
 * @version $Revision: 81030 $
 */
public class RetryInterceptor extends Interceptor
{
   /** Serial Version Identifier. @since 1.0 */
   private static final long serialVersionUID = 1;
   /** The current externalized data version */
   private static final int EXTERNAL_VERSION = 1;
   private static Logger log = Logger.getLogger(RetryInterceptor.class);
   /** The HA-JNDI environment used to restore the invoker proxy */
   private static Properties retryEnv;

   /** A flag that can be set to abort the retry loop */
   private transient boolean retry;
   /** The logging trace flag */
   private transient boolean trace;
   /** Max number of retries. -1 means retry until sucessful */
   private transient int maxRetries = -1;
   /** Number of ms to sleep before each attempt to reestablish the invoker */
   private transient long sleepTime = 1000;

   /**
    * Set the HA-JNDI InitialContext env used to lookup the invoker proxy
    * @param env the InitialContext env used to lookup the invoker proxy
    */ 
   public static void setRetryEnv(Properties env)
   {
      retryEnv = env;
   }

   /**
    * No-argument constructor for externalization.
    */
   public RetryInterceptor()
   {}
   
   /**
    * Create a new RetryInterceptor that will retry the specified
    * number of times.
    * 
    * @param maxRetries the maximum number of retries to attempt. -1 
    *                   (the default) means retry until successful.
    * @param sleepTime  number of ms to pause between each retry attempt
    */
   protected RetryInterceptor(int maxRetries, long sleepTime)
   {
      this.maxRetries = maxRetries;
      this.sleepTime = sleepTime;
   }
   
   // Public --------------------------------------------------------

   public void setRetry(boolean flag)
   {
      this.retry = flag;
   }
   public boolean getRetry()
   {
      return this.retry;
   }

   /**
    * Gets the maximum number of retries that will be attempted.
    */
   public int getMaxRetries()
   {
      return maxRetries;
   }

   /**
    * Sets the maximum number of retries that will be attempted.
    * 
    * @param maxRetries the maximum number of retries to attempt. -1 
    *                   (the default) means retry until successful.
    */
   public void setMaxRetries(int maxRetries)
   {
      this.maxRetries = maxRetries;
   }
   
   /**
    * Gets the number of ms of sleep between each retry attempt.
    */
   public long getSleepTime()
   {
      return sleepTime;
   }
   
   /**
    * Sets the number of ms of sleep between each retry attempt.
    */
   public void setSleepTime(long sleepTime)
   {
      this.sleepTime = sleepTime;
   }

   /**
    * InvocationHandler implementation.
    *
    * @throws Throwable    Any exception or error thrown while processing.
    */
   public Object invoke(Invocation invocation)
      throws Throwable
   {
      Object result = null;
      InvocationContext ctx = invocation.getInvocationContext();
      retry = true;
      int retryCount = 0;
      while( retry == true )
      {
         Interceptor next = getNext();
         try
         {
            if( trace )
               log.trace("invoke, method="+invocation.getMethod());
            result = next.invoke(invocation);
            break;
         }
         catch(ServiceUnavailableException e)
         {
            if( trace )
               log.trace("Invocation failed", e);
            
            InvocationType type = invocation.getType();
            if ((maxRetries > -1 && retryCount >= maxRetries)
                  || reestablishInvokerProxy(ctx, type) == false)
            {
               throw e;
            }
            retryCount++;
         }
      }
      return result;
   }

   /**
    * Loop trying to lookup the proxy invoker from jndi. Continue trying until 
    * successful or {@link #getMaxRetries() maxRetries} attempts have been made
    * without success. This sleeps 1 second between lookup operations. 
    * 
    * @param ctx - the invocation context to populate with the new invoker
    * @param type - the type of the invocation, InvocationType.REMOTE or
    *    InvocationType.HOME
    *    
    * @return <code>true</code> if a lookup was successful, <code>false</code>
    *         if {@link #getMaxRetries() maxRetries} attempts were made
    *         without success.
    */
   private boolean reestablishInvokerProxy(InvocationContext ctx, InvocationType type)
   {
      if( trace )
         log.trace("Begin reestablishInvokerProxy");
      
      boolean isRemote = type == InvocationType.REMOTE;
      String jndiName = (String) ctx.getValue(InvocationKey.JNDI_NAME);
      if( isRemote == true )
         jndiName += "-RemoteInvoker";
      else
         jndiName += "-HomeInvoker";
      Hashtable retryProps = retryEnv;
      if (retryProps == null)
      {
         retryProps = (Hashtable) NamingContextFactory.lastInitialContextEnv.get();
         if ( trace )
         {
            if (retryProps != null)
               log.trace("Using retry properties from NamingContextFactory");
            else
               log.trace("No retry properties available");
         }
      }
      else if ( trace )
      {
         log.trace("Using static retry properties");
      }
      
      int retryCount = 0;
      Invoker newInvoker = null;
      while( retry == true )
      {
         InitialContext namingCtx = null;
         try
         {
            Thread.sleep(sleepTime);
            namingCtx = new InitialContext(retryProps);
            if( trace )
               log.trace("Looking for invoker: "+jndiName);
            newInvoker = (Invoker) namingCtx.lookup(jndiName);
            if( trace )
               log.trace("Found invoker: "+newInvoker);
            ctx.setInvoker(newInvoker);
            break;
         }
         catch(Throwable t)
         {
            retryCount++;
            if( trace )
               log.trace("Retry attempt " + retryCount + 
                         ": Failed to lookup proxy", t);
            if (maxRetries > -1 && retryCount >= maxRetries)
            {
               if ( trace) 
                  log.trace("Maximum retry attempts made");
               break;
            }
         }
         finally
         {
            // JBAS-5906 -- clean up after ourselves
            if (namingCtx != null)
            {
               try
               {
                  namingCtx.close();
               }
               catch (NamingException e)
               {
                  log.warn("Problem closing naming context used for reaquiring invoker: " + 
                        e.getClass() + " -- " + e.getLocalizedMessage());
               }
            }
         }
      }
      if( trace )
         log.trace("End reestablishInvokerProxy");
      
      return (newInvoker != null);
   }

   /**
    * Writes the next interceptor.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      super.writeExternal(out);
      // Write out a version identifier for future extensibility
      out.writeInt(EXTERNAL_VERSION);
      // There is no additional data currently
   }

   /**
    * Reads the next interceptor.
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      // Read the version identifier
      int version = in.readInt();
      if( version == EXTERNAL_VERSION )
      {
         // This version has no additional data
      }
      // Set the logging trace level
      trace = log.isTraceEnabled();
   }
}
