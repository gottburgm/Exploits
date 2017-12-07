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
package org.jboss.invocation.http.interfaces;

import java.io.IOException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ServerException;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationException;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.MarshalledValue;
import org.jboss.logging.Logger;

/** The client side Http invoker proxy that posts an invocation to the
 InvokerServlet using the HttpURLConnection created from the proxy
 externalURL.

* @author Scott.Stark@jboss.org
* @version $Revision: 81030 $
*/
public class HttpInvokerProxy
   implements Invoker, Externalizable
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(HttpInvokerProxy.class);

   /** Serial Version Identifier. */
   static final long serialVersionUID = -8249272784108192267L;

   // Attributes ----------------------------------------------------

   // URL to the remote JMX node invoker
   protected String externalURLValue;
   protected transient URL externalURL;

   // Constructors --------------------------------------------------
   public HttpInvokerProxy()
   {
      // For externalization to work
   }

   /**
   @param externalURL, the URL through which clients should contact the
    InvokerServlet.
   */
   public HttpInvokerProxy(String externalURLValue)
   {
      this.externalURLValue = externalURLValue;
   }

   // Public --------------------------------------------------------

   public String getServerHostName() throws Exception
   {
      if( externalURL == null )
         externalURL = Util.resolveURL(externalURLValue);
      return externalURL.getHost();
   }
   public String getExternalURLValue()
   {
      return externalURLValue;
   }
   public URL getExternalURL()
   {
      return externalURL;
   }

   public String toString()
   {
      StringBuffer tmp = new StringBuffer(super.toString());
      tmp.append('(');
      tmp.append("externalURL:");
      tmp.append(externalURL);
      tmp.append(')');
      return tmp.toString();
   }

   /** This method builds a MarshalledInvocation from the invocation passed
    in and then does a post to the target URL.
   */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      // We are going to go through a Remote invocation, switch to a Marshalled Invocation
      MarshalledInvocation mi = new MarshalledInvocation(invocation);

      if( externalURL == null )
         externalURL = Util.resolveURL(externalURLValue);
      try
      {
         Object value = Util.invoke(externalURL, mi);
         return value;
      }
      catch (InvocationException e)
      {
         // Unwrap the invoker servlet InvocationExceptions
         Throwable t = e.getTargetException();
         if( t instanceof Exception )
            throw (Exception) t;
         else if (t instanceof Error)
            throw (Error) t;
         throw new InvocationTargetException(t);
      }
      catch (IOException e)
      {
         throw new ServerException("IOE", e);
      }
   }

   /** Externalize this instance.
   */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   { 
      out.writeObject(externalURLValue);
   }

   /** Un-externalize this instance.
   */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      externalURLValue = (String) in.readObject();
      externalURL = Util.resolveURL(externalURLValue);
   }

}
