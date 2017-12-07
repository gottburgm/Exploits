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
package org.jboss.console.remote;
/** Common client utility methods
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81010 $
*/
public class Util
{
   /** A serialized RemoteMBeanInvocation */
   private static String REQUEST_CONTENT_TYPE =
      "application/x-java-serialized-object; class=org.jboss.console.remote.RemoteMBeanInvocation";
   private static org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(Util.class);

   static
   {
      /** TODO solve authentication issues!!
      // Install the java.net.Authenticator to use
      try
      {
         java.net.Authenticator.setDefault(new org.jboss.security.SecurityAssociationAuthenticator());
      }
      catch(Exception e)
      {
         log.warn("Failed to install SecurityAssociationAuthenticator", e);
      }
       */
   }

   /** Post the Invocation as a serialized MarshalledInvocation object. This is
    using the URL class for now but this should be improved to a cluster aware
    layer with full usage of HTTP 1.1 features, pooling, etc.
   */
   public static Object invoke (java.net.URL externalURL, RemoteMBeanInvocation mi) throws Exception
   {
      if( log.isTraceEnabled() )
         log.trace("invoke, externalURL="+externalURL);
      /* Post the MarshalledInvocation data. This is using the URL class
       for now but this should be improved to a cluster aware layer with
       full usage of HTTP 1.1 features, pooling, etc.
       */
      java.net.HttpURLConnection conn = (java.net.HttpURLConnection) externalURL.openConnection();
      //if( conn instanceof com.sun.net.ssl.HttpsURLConnection )
      //{
         // See if the org.jboss.security.ignoreHttpsHost property is set
         /*
         if( Boolean.getBoolean("org.jboss.security.ignoreHttpsHost") == true )
         {
            com.sun.net.ssl.HttpsURLConnection sconn = (com.sun.net.ssl.HttpsURLConnection) conn;
            AnyhostVerifier verifier = new AnyhostVerifier();
            sconn.setHostnameVerifier(verifier);
         }
          **/
      //}
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setRequestProperty("ContentType", REQUEST_CONTENT_TYPE);
      conn.setRequestMethod("POST");
      java.io.OutputStream os = conn.getOutputStream();
      java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(os);
      oos.writeObject(mi);
      oos.flush();

      // Get the response MarshalledValue object
      java.io.InputStream is = conn.getInputStream();
      java.io.ObjectInputStream ois = new java.io.ObjectInputStream(is);
      org.jboss.invocation.MarshalledValue mv = (org.jboss.invocation.MarshalledValue) ois.readObject();
      ois.close();
      oos.close();

      // If the encoded value is an exception throw it
      Object value = mv.get();
      
      if( value instanceof org.jboss.invocation.InvocationException )
         throw (Exception) (((org.jboss.invocation.InvocationException)value).getTargetException ());

      if( value instanceof Exception )
         throw (Exception) value;

      return value;
   }

   public static Object getAttribute (java.net.URL externalURL, RemoteMBeanAttributeInvocation mi) throws Exception
   {
      if( log.isTraceEnabled() )
         log.trace("invoke, externalURL="+externalURL);
      /* Post the MarshalledInvocation data. This is using the URL class
       for now but this should be improved to a cluster aware layer with
       full usage of HTTP 1.1 features, pooling, etc.
       */
      java.net.HttpURLConnection conn = (java.net.HttpURLConnection) externalURL.openConnection();
      //if( conn instanceof com.sun.net.ssl.HttpsURLConnection )
      //{
         // See if the org.jboss.security.ignoreHttpsHost property is set
         /*
         if( Boolean.getBoolean("org.jboss.security.ignoreHttpsHost") == true )
         {
            com.sun.net.ssl.HttpsURLConnection sconn = (com.sun.net.ssl.HttpsURLConnection) conn;
            AnyhostVerifier verifier = new AnyhostVerifier();
            sconn.setHostnameVerifier(verifier);
         }
          **/
      //}
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setRequestProperty("ContentType", REQUEST_CONTENT_TYPE);
      conn.setRequestMethod("POST");
      java.io.OutputStream os = conn.getOutputStream();
      java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(os);
      oos.writeObject(mi);
      oos.flush();

      // Get the response MarshalledValue object
      java.io.InputStream is = conn.getInputStream();
      java.io.ObjectInputStream ois = new java.io.ObjectInputStream(is);
      org.jboss.invocation.MarshalledValue mv = (org.jboss.invocation.MarshalledValue) ois.readObject();
      ois.close();
      oos.close();

      // If the encoded value is an exception throw it
      Object value = mv.get();

      if( value instanceof org.jboss.invocation.InvocationException )
         throw (Exception) (((org.jboss.invocation.InvocationException)value).getTargetException ());

      if( value instanceof Exception )
         throw (Exception) value;

      return value;
   }
}
