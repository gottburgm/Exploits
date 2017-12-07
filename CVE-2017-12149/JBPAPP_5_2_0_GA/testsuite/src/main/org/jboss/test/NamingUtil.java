 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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

package org.jboss.test;

import java.net.URI;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.log4j.Logger;

/**
 * Utility class for tests using naming in a special way.
 * 
 * @author  <a href="mailto:pskopek@redhat.com">Peter Skopek</a>
 *
 */
public class NamingUtil
{
   
   public static Logger log = Logger.getLogger(NamingUtil.class);
   
   public static final String JNDI_INVOKER = "invoker/JNDIFactory";
   public static final String HAJNDI_INVOKER = "invoker/HAJNDIFactory";
   
   /**
    * Create test data. It needs to use org.jboss.naming.HttpNamingContextFactory since JNDI through RMI
    * is already secured and disallow bind/unbind/rebind operations.
    * 
    *   
    *   
    * @param jndiName JNDI path where to bind data.
    * @param dataKey key under which data is bound
    * @param data Data object to bind. In case data is null last path element is considered subContext. 
    * @param serverHost host on which to create binding 
    * @throws Exception
    */
   public static void createTestJNDIBinding(String jndiName, String dataKey,
         Object data, String serverHost, boolean useHAJNDI) throws Exception {

      log.debug("XXX");
      
      Context ctx = null;
      if (useHAJNDI) {
         ctx = NamingUtil.getFullHAInitialContext(serverHost);
      } else {
         ctx = NamingUtil.getFullInitialContext(serverHost);
      }

      String[] path = jndiName.split("/");
      String subPath = "";
      for (int i = 0; i < path.length; i++) {

         if (path[i].equals("")) {
            continue;
         }

         subPath = subPath + "/" + path[i];
         log.debug("creating subcontext="+subPath);
         try {
            ctx.createSubcontext(subPath);
            log.debug("subcontext="+subPath+" created.");
         }
         catch (NameAlreadyBoundException e) {
            // ignore
         }
      }
      
      if (data != null) {
         log.debug("bind s="+subPath+", dataKey="+dataKey+", data="+data);
         ctx.bind(subPath + "/" + dataKey, data);
         log.debug(data + " bound.");
      }

      ctx.close();
      
   }
   
   /**
    * Returns initial context which is able to perform all JNDI operations.
    * @param serverHost - use getServerHostForURL() from inside JBoss Testsuite
    * @param jndiFactoryUrlSuffix - URL suffix to get proper invoker invoker/JNDIFactory or invoker/HAJNDIFactory
    * @return
    * @throws Exception
    */
   public static InitialContext getFullInitialContext(String serverHost, String jndiFactoryUrlSuffix) 
         throws Exception 
   {

      if (jndiFactoryUrlSuffix == null) {
         jndiFactoryUrlSuffix = JNDI_INVOKER;
      }
      
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.HttpNamingContextFactory");

      env.setProperty(Context.PROVIDER_URL, "http://" + serverHost + ":8080/" + jndiFactoryUrlSuffix);
      log.debug("Creating InitialContext with env="+env);
      InitialContext ctx = new InitialContext(env);
      
      return ctx;
   }
   
   
   
   /**
    * Returns initial context which is able to perform all JNDI operations.
    * @param serverHost - use getServerHostForURL() from inside JBoss Testsuite
    * @return
    * @throws Exception
    */
   public static InitialContext getFullInitialContext(String serverHost) 
         throws Exception 
   {
      return getFullInitialContext(serverHost, JNDI_INVOKER);
   }
   
   /**
    * Returns initial context which is able to perform all JNDI operations.
    * @param serverHost - use getServerHostForURL() from inside JBoss Testsuite
    * @return
    * @throws Exception
    */
   public static InitialContext getFullHAInitialContext(String serverHost) 
         throws Exception 
   {
      return getFullInitialContext(serverHost, HAJNDI_INVOKER);
   }
   
   /**
    * Extract hostname from jndiURL parameter and get InitialContext using @see getFullInitialContext
    * @param jndiUrl
    * @return
    * @throws Exception
    */
   public static InitialContext getFullInitialContextFromUrl(String jndiUrl) 
         throws Exception 
   {
      return NamingUtil.getFullInitialContext(NamingUtil.extractHostnameFromUrl(jndiUrl));
   }
   
   /**
    * Obtain hostname from URL.
    * @param url
    * @return
    */
   public static String extractHostnameFromUrl(String url) throws Exception {
      log.debug("URL = " + url);
      URI uri = new URI(url);
      log.debug("host="+uri.getHost());
      return uri.getHost();
   }
   
   /**
    * Extract hostname from jndiURL parameter and get InitialContext using @see getFullInitialContext
    * @param jndiUrl
    * @return
    * @throws Exception
    */
   public static InitialContext getFullHAInitialContextFromUrl(String jndiUrl) 
         throws Exception 
   {
      log.debug("jndiUrl = " + jndiUrl);
      URI uri = new URI(jndiUrl);
      log.debug("host="+uri.getHost());
      return NamingUtil.getFullHAInitialContext(uri.getHost());
   }
   

   /**
    * This methods calls servlet which must be deployed at server to create JNDI objects remotely to byepass security.
    * 
    * @param jndiName
    * @param dataKey
    * @param data
    * @param useHAJNDI
    * @throws Exception
    */
   public static void createRemoteTestJNDIBinding(String jndiName, String dataKey,
         Object data, String serverHost, boolean useHAJNDI) throws Exception {
      
      HttpClient httpClient = new HttpClient();
      String url = "http://" + serverHost + ":8080/naming-util/naming-util-servlet";
      
      HttpMethodParams params = new HttpMethodParams();
      params.setParameter("jndiName", jndiName);
      if (data != null) {
         params.setParameter("dataKey", dataKey);
         params.setParameter("data", data);
      }
      params.setBooleanParameter("useHAJndi", useHAJNDI);

      
      url = url + "?jndiName=" + jndiName;
      url = url + "&dataKey=" + dataKey;
      url = url + "&data=" + data;
      url = url + "&useHAJndi=" + Boolean.toString(useHAJNDI);
      
      
      GetMethod jndiGet = new GetMethod(url);
      //jndiGet.setParams(params);
      int responseCode = httpClient.executeMethod(jndiGet);
      String body = jndiGet.getResponseBodyAsString();

      if(responseCode != HttpURLConnection.HTTP_OK || !body.contains("OK")) {
         throw new Exception(body);
      }
      
   }

   
}
