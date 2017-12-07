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
package org.jboss.test.cluster.classloader.leak.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.classloader.leak.clstore.ClassLoaderTrackerMBean;

/**
 * Abstract superclass of the EJB2 and EJB3 versions of the classloader leak
 * tests.
 * 
 * @author Brian Stansberry
 */
public abstract class ClassloaderLeakTestBase extends JBossClusteredTestCase
{
   public static final String WEBAPP = "WEBAPP";
   public static final String SERVLET = "SERVLET";
   public static final String SERVLET_TCCL = "SERVLET_TCCL";
   public static final String JSP = "JSP";
   public static final String JSP_TCCL = "JSP_TCCL";
   
   public static final String[] WEB = new String[]{ SERVLET, SERVLET_TCCL, WEBAPP };
   
   private static final Set<String> deployments = new HashSet<String>();
   
   private String baseURL = null;

   public ClassloaderLeakTestBase(String name)
   {
      super(name);
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      cleanDeployments();
      
      for (int i = 0; i < WEB.length; i++)
         removeClassLoader(WEB[i]);
      
      String[] ejbs = getEjbKeys();
      for (int i = 0; i < ejbs.length; i++)
         removeClassLoader(ejbs[i]);
      
      baseURL = "/" + getWarContextPath() + "/";
   }
   
   protected void tearDown() throws Exception
   {
      cleanDeployments();
      
      for (int i = 0; i < WEB.length; i++)
         removeClassLoader(WEB[i]);
      
      String[] ejbs = getEjbKeys();
      for (int i = 0; i < ejbs.length; i++)
         removeClassLoader(ejbs[i]);
      
      super.tearDown();
   }
   
   protected abstract String getWarContextPath();
   
   protected abstract String[] getEjbKeys();
   
   protected void cleanDeployments() throws Exception
   {
      Iterator it = deployments.iterator();
      while (it.hasNext())
      {
         undeployComponent((String) it.next(), false);
         it = deployments.iterator();
      }
   }
   
   protected void undeployComponent(String deployment, boolean propagateFailure)
         throws Exception
   {
      try
      {         
         undeploy(deployment);
         deployments.remove(deployment);
      }
      catch (Exception e)
      {
         if (propagateFailure)
            throw e;
         else
         {
            log.error("Exception during undeploy of " + deployment, e);
            deployments.remove(deployment);
         }
      }
   }
   
   private void deployComponent(String deployment) throws Exception
   {
      deploy(deployment);
      deployments.add(deployment);
   }
   
   protected void warTest(String deployment) throws Exception
   {
      // Ensure we are starting with a clean slate
      checkCleanKeys(WEB);
      
      deployComponent(deployment);      

      makeWebRequest(baseURL + "SimpleServlet", WEBAPP);
      
      // Make sure the expected registrations were done
      checkKeyRegistration(WEB);
      
      // This sleep is a workaround to JBAS-4060
      sleep(500);
      
      undeployComponent(deployment, true);
      
      // TODO - probably not needed anymore; remove
      flushSecurityCache("HsqlDbRealm");
      
      sleep(500);
      
      // Confirm the classloaders were released
      String unregistered = checkClassLoaderRelease(WEB);
      
      if (unregistered.trim().length() > 0)
      {
         fail("Classloaders unregistered: " + unregistered);
      }
   }
   
   protected void ejbTest(String deployment) throws Exception
   {
      // Ensure we are starting with a clean slate
      checkCleanKeys(getEjbKeys());
      
      deployComponent(deployment);
      
      makeEjbRequests();
      
      // Make sure the expected registrations were done
      checkKeyRegistration(getEjbKeys());
      
      undeployComponent(deployment, true);
      
      // TODO - probably not needed anymore; remove
      flushSecurityCache("HsqlDbRealm");
      
      sleep(500);
      
      // Confirm the classloaders were released
      String unregistered = checkClassLoaderRelease(getEjbKeys());
      
      if (unregistered.length() > 0)
      {
         fail("Classloaders unregistered: " + unregistered);
      }
   }
   
   protected void earTest(String deployment) throws Exception
   {
      // Ensure we are starting with a clean slate
      checkCleanKeys(WEB);      
      checkCleanKeys(getEjbKeys());
      
      deployComponent(deployment);
      
      // Simple web requests
      makeWebRequest(baseURL + "SimpleServlet", WEBAPP);
      
      // Make sure the expected registrations were done      
      checkKeyRegistration(WEB);
      
      // EJB related requests
      makeWebRequest(baseURL + "EJBServlet", "EJB");
      
      makeEjbRequests();
      
      // Make sure the expected registrations were done      
      checkKeyRegistration(getEjbKeys());
      
      // This sleep is a workaround to JBAS-4060
      sleep(500);
      
      undeployComponent(deployment, true);
      
      // TODO - probably not needed anymore; remove
      flushSecurityCache("HsqlDbRealm");
      
      sleep(500);
      
      // Confirm the classloaders were released
      String unregistered = checkClassLoaderRelease(WEB);
      
      unregistered += checkClassLoaderRelease(getEjbKeys());
      
      if (unregistered.trim().length() > 0)
      {
         fail("Classloaders unregistered: " + unregistered);
      }
   }
   
   protected void checkCleanKeys(String[] keys) throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      for (MBeanServerConnection adaptor : adaptors)
      {
         for (int i = 0; i < keys.length; i++)
         {
            if (hasClassLoader(keys[i], adaptor))
               throw new IllegalStateException("Classloader already registered for " + keys[i]);
         }
      }
   }
   
   protected void checkKeyRegistration(String[] keys) throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      int nodeNum = 0;
      for (MBeanServerConnection adaptor : adaptors)
      {
         for (int i = 0; i < keys.length; i++)
         {
            assertTrue(keys[i] + " classloader registered with node " + nodeNum, hasClassLoader(keys[i], adaptor));
         }
         nodeNum++;
      }
   }
   
   protected String checkClassLoaderRelease(String[] keys) throws Exception
   {
      String faillist = "";
      MBeanServerConnection[] adaptors = getAdaptors();
      int nodeNum = 0;
      for (MBeanServerConnection adaptor : adaptors)
      {
         for (int i = 0; i < keys.length; i++)
         {
            if (!hasClassLoaderBeenReleased(keys[i], adaptor))
            {
               faillist += "node" + nodeNum + "/" + keys[i] + " ";
               return faillist; // don't check the others; don't want multiple reports
            }
         }
         
         nodeNum++;
      }
      
      return faillist;
   }
   
   private boolean hasClassLoader(String key, MBeanServerConnection adaptor) throws Exception
   {
      ObjectName on = new ObjectName(ClassLoaderTrackerMBean.OBJECT_NAME);
      Object[] params = { key };
      String[] signature = new String[] { String.class.getName() };
      return ((Boolean) adaptor.invoke(on, "hasClassLoader", params, signature)).booleanValue();
   }
   
   private boolean hasClassLoaderBeenReleased(String key, MBeanServerConnection adaptor) throws Exception
   {
      ObjectName on = new ObjectName(ClassLoaderTrackerMBean.OBJECT_NAME);
      Object[] params = { key };
      String[] signature = new String[] { String.class.getName() };
      return ((Boolean) adaptor.invoke(on, "hasClassLoaderBeenReleased", params, signature)).booleanValue();
   }
   
   private void removeClassLoader(String key) throws Exception
   {
      try
      {
         ObjectName on = new ObjectName(ClassLoaderTrackerMBean.OBJECT_NAME);
         Object[] params = { key };
         String[] signature = new String[] { String.class.getName() };
         MBeanServerConnection[] adaptors = getAdaptors();
         for (MBeanServerConnection adaptor : adaptors)
         {
            adaptor.invoke(on, "removeClassLoader", params, signature);
         }
      }
      catch (Exception e)
      {
         log.error("Caught exception removing classloader under key " + key, e);
      }
   }
   
   private void flushSecurityCache(String domain) throws Exception
   {
      log.debug("Flushing security cache " + domain);
      ObjectName on = new ObjectName(ClassLoaderTrackerMBean.OBJECT_NAME);
      Object[] params = { domain };
      String[] signature = new String[] { String.class.getName() };
      MBeanServerConnection[] adaptors = getAdaptors();
      for (MBeanServerConnection adaptor : adaptors)
      {
         adaptor.invoke(on, "flushSecurityCache", params, signature);
      }
   }
   
   private void makeWebRequest(String url, String responseContent) throws Exception
   {
      HttpClient client = new HttpClient();
      GetMethod method = new GetMethod(getHttpURLs()[0] + url);
      checkWebRequest(client, method, url, responseContent);
      
      setCookieDomainToThisServer(client, getHttpURLs()[1]);
      method = new GetMethod(getHttpURLs()[1] + url);
      checkWebRequest(client, method, url, responseContent);
   }

   private void setCookieDomainToThisServer(HttpClient client, String server)
   {
      // Get the session cookie
      Cookie sessionID = getSessionCookie(client, server);
      // Reset the domain so that the cookie will be sent to server1
      sessionID.setDomain(server);
      client.getState().addCookie(sessionID);
   }
   
   protected Cookie getSessionCookie(HttpClient client, String server)
   {
      // Get the state for the JSESSIONID
      HttpState state = client.getState();
      // Get the JSESSIONID so we can reset the host
      Cookie[] cookies = state.getCookies();
      Cookie sessionID = null;
      for(int c = 0; c < cookies.length; c ++)
      {
         Cookie k = cookies[c];
         if( k.getName().equalsIgnoreCase("JSESSIONID") )
            sessionID = k;
      }
      if(sessionID == null)
      {
         fail("setCookieDomainToThisServer(): fail to find session id. Server name: " +server);
      }
      log.info("Saw JSESSIONID="+sessionID);
      return sessionID;
   }

   /**
    * FIXME Comment this
    * 
    * @param client
    * @param method
    * @param url
    * @param responseContent
    */
   private void checkWebRequest(HttpClient client, GetMethod method, String url, String responseContent)
   {
      int responseCode = 0;
      try
      {
         responseCode = client.executeMethod(method);
         
         assertTrue("Get OK with url: " +url + " responseCode: " +responseCode
               , responseCode == HttpURLConnection.HTTP_OK);
         
         InputStream rs = method.getResponseBodyAsStream();
         InputStreamReader reader = new InputStreamReader(rs);
         StringWriter writer = new StringWriter();
         int c;
         while ((c = reader.read())  != -1)
            writer.write(c);
         
         String rsp = writer.toString();
         
         assertTrue("Response contains " + responseContent, rsp.indexOf(responseContent) >= 0);
      } 
      catch (IOException e)
      {
         e.printStackTrace();
         fail("HttpClient executeMethod fails." +e.toString());
      }
      finally
      {
         method.releaseConnection();
      }
   }
   
   protected abstract void makeEjbRequests() throws Exception;
}
