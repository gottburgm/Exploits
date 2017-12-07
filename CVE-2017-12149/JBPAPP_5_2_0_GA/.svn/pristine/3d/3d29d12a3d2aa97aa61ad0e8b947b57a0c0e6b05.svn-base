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
package org.jboss.test.cluster.apache_tomcat;

import java.io.IOException;
import java.net.HttpURLConnection;

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpRecoverableException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.JBossRMIAdaptorHelper;

import javax.management.ObjectName;
import javax.management.MBeanInfo;

/**
 * This testcase is written to test HttpSessionReplication with one apache webserver
 * loadbalancing multiple tomcat/JBoss instances.
 *
 * @author <a href="mailto:anil.saldhana@jboss.com">Anil Saldhana</a>.
 * @version $Revision: 1.0
 * @see org.jboss.test.cluster.apache_tomcat.HttpSessionReplicationTestCase
 */
public class HttpSessionReplicationTestCase
   extends JBossClusteredTestCase
{
   private String apacheurl = null;

   public HttpSessionReplicationTestCase(String name)
   {
      super(name);
/* try{
	      	 this.getPropertiesFile();
	         String numin = prop.getProperty("NumOfInstances");
			 numInstances = Integer.parseInt( numin );
			 if( numInstances < 2 ) fail( "Atleast two nodes needed");
             this.getLog().debug("Number of nodes="+numInstances);

			 //Lets build up the jndi server urls now
			 //this.setServerNames(servernames);
			}catch( Exception e){
				fail( e.getMessage());
			}  */
   }

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(HttpSessionReplicationTestCase.class,
         "http-sr.war");
      return t1;
   }

   /**
    * Tests connection to the Apache Server.
    * Note: We deal with just one Apache Server. We can bounce the different
    * JBoss/Tomcat servers and Apache will loadbalance.
    *
    * @throws Exception
    */
   public void testApacheConnection()
      throws Exception
   {
      getLog().debug("Enter testApacheConnection");
      try
      {
         /*String apacheurl =  prop.getProperty("ApacheUrl");
       getLog().debug(apacheurl);
assertTrue("Apache Up?", this.checkURL(apacheurl));*/
         apacheurl = System.getProperty("apache.url");
         getLog().debug(apacheurl);
         assertTrue("Apache Up?", this.checkURL(apacheurl));
      }
      catch (Exception e)
      {
         getLog().debug(e.getMessage());
      }
      getLog().debug("Exit testApacheConnection");
   }

   /**
    * Main method that deals with the Http Session Replication Test
    *
    * @throws Exception
    */
   public void testHttpSessionReplication()
      throws Exception
   {
      String attr = "";
      getLog().debug("Enter testHttpSessionReplication");

      apacheurl = System.getProperty("apache.url");
      String urlname = apacheurl + System.getProperty("apache.set.url");
      String geturlname = apacheurl + System.getProperty("apache.get.url");

      getLog().debug(urlname + ":::::::" + geturlname);

// Create an instance of HttpClient.
      HttpClient client = new HttpClient();

// Create a method instance.
      HttpMethod method = new GetMethod(geturlname);

//    Get the Attribute set by testsessionreplication.jsp
      attr = makeGet(client, method);

//    Shut down the first tomcat instance
      this.shutDownTomcatInstance(1);
      getLog().debug("Brought down the first tomcat instance");

      String[] httpURLs = super.getHttpURLs();
      String httpurl = httpURLs[0];
      String tmsg = "Is 1st Tomcat really down?Tomcat Up(" + httpurl + ")=";
      getLog().debug(tmsg + checkURL(httpurl));

//Give 30 seconds for things to stabilize.
      sleepThread(30);

//    Make connection
      method = new GetMethod(geturlname);
      String attr2 = makeGet(client, method);
      this.sleepThread(10);
      getLog().debug("Will Start the Tomcat MBean back");
      this.startTomcatInstance(1);
      this.sleepThread(10);
      getLog().debug("Tomcat Up=" + checkURL(httpurl));
      String tstr = "attr1=" + attr + " and attr2=" + attr2;
      if (!attr2.equals(attr)) fail("Http Session Replication failed with " + tstr);
      getLog().debug("Http Session Replication has happened");
      getLog().debug("Exit testHttpSessionReplication");
   }


   /**
    * Starts the Tomcat MBean running on a particular node
    *
    * @param instancenum Instance Number of the node
    * @throws Exception
    */
   private void startTomcatInstance(int instancenum)
      throws Exception
   {
      String jndi = getJNDIUrl(instancenum);
      getLog().debug("JNDI URL Obtained=  " + jndi);
      JBossRMIAdaptorHelper server = new JBossRMIAdaptorHelper(jndi);
      //Get the MBeanInfo for the Tomcat MBean
      ObjectName name = new ObjectName("jboss.web:service=WebServer");
      MBeanInfo info = server.getMBeanInfo(name);
      System.out.println("Tomcat MBean:" + info.getClassName());

      getLog().debug("Going to start tomcat  ");
      //Going to stop the Tomcat Instance
      server.invokeOperation(name, "start", null, null);
      this.sleepThread(10);
      server.invokeOperation(name, "startConnectors", null, null);
   }

   /**
    * Shuts down the Tomcat MBean running on a particular node
    *
    * @param instancenum Instance Number of the node
    * @throws Exception
    */
   private void shutDownTomcatInstance(int instancenum)
      throws Exception
   {
      String jndi = getJNDIUrl(instancenum);
      getLog().debug("JNDI URL Obtained=  " + jndi);
      JBossRMIAdaptorHelper server = new JBossRMIAdaptorHelper(jndi);
      //Get the MBeanInfo for the Tomcat MBean
      ObjectName name = new ObjectName("jboss.web:service=WebServer");
      MBeanInfo info = server.getMBeanInfo(name);
      System.out.println("Tomcat MBean:" + info.getClassName());

      getLog().debug("Going to stop tomcat  ");
      //Going to stop the Tomcat Instance
      server.invokeOperation(name, "stop", null, null);
   }


   /**
    * Generate the JNDI Url for the JBoss Instance with instance number
    *
    * @param instancenum
    * @return
    */
   private String getJNDIUrl(int instancenum)
   {
      String jndi = "";
      try
      {
         int num = instancenum - 1; //node0,node1 etc
         String key = "node" + num + ".jndi.url"; // node0.jndiurl
         jndi = System.getProperty(key);
      }
      catch (Exception e)
      {
         fail("getJNDIUrl Failed with:" + e.getMessage());
      }

      return jndi;
   }

   /**
    * Sleep for specified time
    *
    * @param secs
    * @throws Exception
    */
   private void sleepThread(long secs)
      throws Exception
   {
      Thread.sleep(1000 * secs);
   }


   /**
    * Makes a http call to the jsp that retrieves the attribute stored on the
    * session. When the attribute values mathes with the one retrieved earlier,
    * we have HttpSessionReplication.
    * Makes use of commons-httpclient library of Apache
    *
    * @param client
    * @param method
    * @return session attribute
    */
   private String makeGet(HttpClient client, HttpMethod method)
      throws IOException
   {
      try
      {
         client.executeMethod(method);
      }
      catch (HttpRecoverableException e)
      {
         log.debug("A recoverable exception occurred, retrying." +
            e.getMessage());
      }
      catch (IOException e)
      {
         log.debug(e);
         e.printStackTrace();
         System.exit(-1);
      }

      // Read the response body.
      byte[] responseBody = method.getResponseBody();

      // Release the connection.
      method.releaseConnection();

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      return new String(responseBody);
   }

   /**
    * Checks whether the url is valid or not
    *
    * @param url The URL which should be checked
    * @return whether the url is up or not
    */
   private boolean checkURL(String url)
   {
      boolean ok = false;
      if (url != null) url = url.trim();
      try
      {
         HttpClient httpConn = new HttpClient();
         GetMethod g = new GetMethod(url);
         int responseCode = httpConn.executeMethod(g);
         log.debug("Response Code for " + url + " is=" + responseCode);
         ok = responseCode == HttpURLConnection.HTTP_OK;
      }
      catch (Exception e)
      {
         log.debug("Exception for checking url=" + url);
         log.debug(e);
         ok = false;
      }
      return ok;
   }

}
