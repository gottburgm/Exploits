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
package org.jboss.naming;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.Hashtable;
import java.lang.reflect.InvocationTargetException;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.jboss.invocation.InvocationException;
import org.jboss.invocation.MarshalledValue;
import org.jboss.invocation.http.interfaces.Util;
import org.jboss.security.SecurityConstants;
import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.jboss.logging.Logger;
import org.jnp.interfaces.Naming;
import org.jnp.interfaces.NamingContext;



/** A naming provider InitialContextFactory implementation that obtains a
 Naming proxy from an HTTP URL.

 @see javax.naming.spi.InitialContextFactory

 @author Scott.Stark@jboss.org
 @version $Revision: 83020 $
 */
public class HttpNamingContextFactory
   implements InitialContextFactory, ObjectFactory
{
   private static Logger log = Logger.getLogger(HttpNamingContextFactory.class);

   // InitialContextFactory implementation --------------------------
   public Context getInitialContext(Hashtable env)
      throws NamingException
   {
      // Parse the Context.PROVIDER_URL
      String provider = (String) env.get(Context.PROVIDER_URL);
      if( provider.startsWith("jnp:") == true )
         provider = "http:" + provider.substring(4);
      else if( provider.startsWith("jnps:") == true )
         provider = "https:" + provider.substring(5);
      else if( provider.startsWith("jnp-http:") == true )
         provider = "http:" + provider.substring(9);
      else if( provider.startsWith("jnp-https:") == true )
         provider = "https:" + provider.substring(10);

      tryLogin(env);

      URL providerURL = null;
      Naming namingServer = null;
      try
      {
         providerURL = new URL(provider);
         // Retrieve the Naming interface
         namingServer = getNamingServer(providerURL);
      }
      catch(Exception e)
      {
         NamingException ex = new NamingException("Failed to retrieve Naming interface for provider " + provider);
         ex.setRootCause(e);
         throw ex;
      }

      // Copy the context env
      env = (Hashtable) env.clone();
      return new NamingContext(env, null, namingServer);
   }

   /**
    * if anyone bothers to set the JNDI style authentication stuff then let's use it or 
    * just ignore it if they don't (they can still use JAAS style if they want)
    */
   private void tryLogin(Hashtable env) throws NamingException {
      // Get the login configuration name to use, initially set to default.
      String protocol = SecurityConstants.DEFAULT_APPLICATION_POLICY;
      Object prop = env.get(Context.SECURITY_PROTOCOL);
      if( prop != null )
         protocol = prop.toString();

      // Get the login principal and credentials from the JNDI env
      Object credentials = env.get(Context.SECURITY_CREDENTIALS);
      Object principal = env.get(Context.SECURITY_PRINCIPAL);
      if(principal == null || credentials == null) {
           return;  //don't bother and don't throw any exceptions
      }
      try
      {
         // Get the principal username
         String username;
         if( principal instanceof Principal )
         {
            Principal p = (Principal) principal;
            username = p.getName();
         }
         else
         {
            username = principal.toString();
         }
    
         UsernamePasswordHandler handler = new UsernamePasswordHandler(username,
            credentials);
         Configuration conf = getConfiguration();
         // Do the JAAS login
         LoginContext lc = new LoginContext(protocol, null, handler, conf);
         lc.login();
      }
      catch(LoginException e)
      {
         AuthenticationException ex = new AuthenticationException("Failed to login using protocol="+protocol);
         ex.setRootCause(e);
         throw ex;
      }

   }

   /**
    * Either call Configuration.getConfiguration() like LoginContext does, or if that fails due to no
    * auth.conf or whatever config file, then return DummyConfiguration which does what we expect for 
    * UsernamePasswordHandler. 
    */
   private Configuration getConfiguration() {
      Configuration conf = null;
      try {
        conf = Configuration.getConfiguration(); 
      } catch (Exception e) {
        if(e.getCause() instanceof IOException) { //no auth.conf or whatever so we make our own dummy
            conf = new DummyConfiguration();
        }
      }
      return conf;
   }

   // ObjectFactory implementation ----------------------------------
   public Object getObjectInstance(Object obj, Name name, Context nameCtx,
      Hashtable env)
      throws Exception
   {
      Context ctx = getInitialContext(env);
      Reference ref = (Reference) obj;
      RefAddr addr = ref.get("URL");
      String path = (String) addr.getContent();
      return ctx.lookup(path);
   }

   /** Obtain the JNDI Naming stub by reading its marshalled object from the
    * servlet specified by the providerURL
    * 
    * @param providerURL the naming factory servlet URL
    * @return
    * @throws ClassNotFoundException throw during unmarshalling
    * @throws IOException thrown on any trasport failure
    * @throws InvocationTargetException throw on failure to install a JSSE host verifier
    * @throws IllegalAccessException throw on failure to install a JSSE host verifier
    */ 
   private Naming getNamingServer(URL providerURL)
      throws ClassNotFoundException, IOException, InvocationTargetException,
         IllegalAccessException
   {
      // Initialize the proxy Util class to integrate JAAS authentication
      Util.init();
      if( log.isTraceEnabled() )
         log.trace("Retrieving content from : "+providerURL);

      HttpURLConnection conn = (HttpURLConnection) providerURL.openConnection();
      Util.configureHttpsHostVerifier(conn);
      Util.configureSSLSocketFactory(conn);
      int length = conn.getContentLength();
      String type = conn.getContentType();
      if( log.isTraceEnabled() )
         log.trace("ContentLength: "+length+"\nContentType: "+type);

      InputStream is = conn.getInputStream();
      ObjectInputStream ois = new ObjectInputStream(is);
      MarshalledValue mv = (MarshalledValue) ois.readObject();
      ois.close();

      Object obj = mv.get();
      if( (obj instanceof Naming) == false )
      {
         String msg = "Invalid reply content seen: "+obj.getClass();
         Throwable t = null;
         if( obj instanceof Throwable )
         {
            t = (Throwable) obj;
            if( t instanceof InvocationException )
               t = ((InvocationException)t).getTargetException();
         }
         if( t != null )
            log.warn(msg, t);
         else
            log.warn(msg);
         IOException e = new IOException(msg);
         throw e;
      }
      Naming namingServer = (Naming) obj;
      return namingServer;
   }
}

/**
 * When no configuration file is found (we get IOException as the cause of a SecurityException),
 * we make this dummy that uses the default ClientLoginModule as required with no options.  
 *
 */
class DummyConfiguration extends Configuration {
  public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return new AppConfigurationEntry[] {
           new AppConfigurationEntry("org.jboss.security.ClientLoginModule",AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, new java.util.HashMap())
        }; //return a big dummy entry saying use the jboss login module that takes username/password
  }
  public void refresh() {
           //do nothing 
  } 
}
