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
package org.jboss.test.classloader.test;

import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.jboss.test.JBossTestCase;
import org.jboss.test.classloader.scoping.override.ejb.log4j113.StatelessSession;
import org.jboss.test.classloader.scoping.override.ejb.log4j113.StatelessSessionHome;
import org.jboss.test.util.web.HttpUtils;
import org.jboss.system.ServiceMBean;
import org.jboss.mx.loading.HeirarchicalLoaderRepository3;
import org.jboss.mx.loading.RepositoryClassLoader;
import org.jboss.mx.loading.UnifiedLoaderRepository3;
import org.jboss.mx.loading.ClassLoaderUtils;

/** Unit tests for class and resource scoping
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class ScopingUnitTestCase extends JBossTestCase
{
   public ScopingUnitTestCase(String name)
   {
      super(name);
   }

   /** Test the scoping of singleton classes in two independent service
    * deployments
    */
   public void testSingletons() throws Exception
   {
      getLog().debug("+++ testSingletons");
      try
      {
         deploy("singleton1.sar");
         getLog().info("Deployed singleton1.sar");
         ObjectName testObjectName = new ObjectName("jboss.test:service=TestService,version=V1");
         boolean isRegistered = getServer().isRegistered(testObjectName);
         assertTrue("jboss.test:loader=singleton.sar,version=V1 isRegistered", isRegistered);
         Integer state = (Integer) getServer().getAttribute(testObjectName, "State");
         assertTrue("state.intValue() == ServiceMBean.STARTED",
               state.intValue() == ServiceMBean.STARTED);
         Object[] args = {"V1"};
         String[] sig = {"java.lang.String"};
         Boolean matches = (Boolean) getServer().invoke(testObjectName, "checkVersion", args, sig);
         assertTrue("checkVersion(V1) is true", matches.booleanValue());
      }
      catch(Exception e)
      {
         getLog().info("Failed to validate singleton1.sar", e);
         throw e;
      }

      try
      {
         deploy("singleton2.sar");
         getLog().info("Deployed singleton2.sar");
         ObjectName testObjectName = new ObjectName("jboss.test:service=TestService,version=V2");
         boolean isRegistered = getServer().isRegistered(testObjectName);
         assertTrue("jboss.test:loader=singleton.sar,version=V2 isRegistered", isRegistered);
         Integer state = (Integer) getServer().getAttribute(testObjectName, "State");
         assertTrue("state.intValue() == ServiceMBean.STARTED",
               state.intValue() == ServiceMBean.STARTED);
         Object[] args = {"V2"};
         String[] sig = {"java.lang.String"};
         Boolean matches = (Boolean) getServer().invoke(testObjectName, "checkVersion", args, sig);
         assertTrue("checkVersion(V2) is true", matches.booleanValue());
      }
      catch(Exception e)
      {
         getLog().info("Failed to validate singleton2.sar", e);
         throw e;
      }
      finally
      {
         undeploy("singleton1.sar");
         getLog().info("Undeployed singleton1.sar");
         undeploy("singleton2.sar");
         getLog().info("Undeployed singleton2.sar");         
      }
   }

   /** Test the ability to override the server classes with war local versions
    * of log4j classes
    */
   public void testWarLog4jOverrides() throws Exception
   {
      getLog().debug("+++ testWarOverrides");
      try
      {
         deploy("log4j113.war");
         URL log4jServletURL = new URL("http://" + getServerHost() + ":8080/log4j113/Log4jServlet/");
         InputStream reply = (InputStream) log4jServletURL.getContent();
         getLog().debug("Accessed http://" + getServerHost() + ":8080/log4j113/Log4jServlet/");
         logReply(reply);

         URL encServletURL = new URL("http://" + getServerHost() + ":8080/log4j113/ENCServlet/");
         reply = (InputStream) encServletURL.getContent();
         getLog().debug("Accessed http://" + getServerHost() + ":8080/log4j113/ENCServlet/");
         logReply(reply);
      }
      catch(Exception e)
      {
         getLog().info("Failed to access Log4jServlet in log4j113.war", e);
         throw e;
      }
      finally
      {
         undeploy("log4j113.war");
      }
   }

   /** Test the ability to override the server classes with war local versions
    * of log4j classes when using commons-logging.
    */
   public void testWarCommonsLoggingLog4jOverrides() throws Exception
   {
      getLog().debug("+++ testWarCommonsLoggingLog4jOverrides");
      try
      {
         deploy("common-logging.war");
         URL log4jServletURL = new URL("http://" + getServerHost() + ":8080/common-logging/Log4jServlet/");
         InputStream reply = (InputStream) log4jServletURL.getContent();
         getLog().debug("Accessed http://" + getServerHost() + ":8080/common-logging/Log4jServlet/");
         logReply(reply);
      }
      catch(Exception e)
      {
         getLog().info("Failed to access Log4jServlet in common-logging.war", e);
         throw e;
      }
      finally
      {
         undeploy("common-logging.war");
      }
   }

   /** Test the ability to override the server classes with war local versions
    * of xml parser classes.
    * This test is invalid as of jdk1.4+ due to the bundling of the xerces
    * parser with the jdk
    */
   public void badtestWarXmlOverrides() throws Exception
   {
      getLog().debug("+++ testWarOverrides");
      try
      {
         deploy("oldxerces.war");
         URL servletURL = new URL("http://" + getServerHost() + ":8080/oldxerces/");
         InputStream reply = (InputStream) servletURL.getContent();
         getLog().debug("Accessed http://" + getServerHost() + ":8080/oldxerces/");
         logReply(reply);
      }
      catch(Exception e)
      {
         getLog().info("Failed to access oldxerces.war", e);
         throw e;
      }
      finally
      {
         undeploy("oldxerces.war");
      }
   }

   /** Test the ability to override the server classes with ejb local versions
    */
   public void testEjbOverrides() throws Exception
   {
      getLog().debug("+++ testEjbOverrides");
      try
      {
         deploy("log4j113-ejb.jar");
         InitialContext ctx = new InitialContext();
         StatelessSessionHome home = (StatelessSessionHome) ctx.lookup("Log4j113StatelessBean");
         StatelessSession bean = home.create();
         Throwable error = bean.checkVersion();
         getLog().debug("StatelessSession.checkVersion returned:", error);
         assertTrue("checkVersion returned null", error == null);
      }
      catch(Exception e)
      {
         getLog().info("Failed to access Log4j113StatelessBean in log4j113-ejb.jar", e);
         throw e;
      }
      finally
      {
         undeploy("log4j113-ejb.jar");
      }
   }

   /**
    * Deploy a sar with nested wars that reference jars in
    * the sar through the war manifest classpath.
    */
   public void testNestedWarManifest()
      throws Exception
   {
      getLog().debug("+++ testNestedWarManifest");
      String baseURL = HttpUtils.getBaseURL();
      URL url = new URL(baseURL+"staticarray-web1/validate.jsp"
            + "?Sequencer.info.expected=1,2,3,4,5,6,7,8,9,10"
            + "&op=set"
            + "&array=1,2,3,4,5,6,7,8,9,10");
      try
      {
         deploy("staticarray.sar");
         // Set the static array value to a non-default from war1
         HttpMethodBase request = HttpUtils.accessURL(url);
         Header errors = request.getResponseHeader("X-Error");
         log.info("war1 X-Error: "+errors);
         assertTrue("war1 X-Error("+errors+") is null", errors == null);
         // Validate that war2 sees the changed values
         url = new URL(baseURL+"staticarray-web2/validate.jsp"
               + "?Sequencer.info.expected=1,2,3,4,5,6,7,8,9,10");
         request = HttpUtils.accessURL(url);
         errors = request.getResponseHeader("X-Error");
         log.info("war2 X-Error: "+errors);
         assertTrue("war2 X-Error("+errors+") is null", errors == null);
         
      }
      catch(Exception e)
      {
         getLog().info("Failed to access: "+url, e);
         throw e;
      }
      finally
      {
         undeploy("staticarray.sar");
      }
   }

   /** Tests for accessing java system classes from scoped
    * repositories.
    * 
    * @throws Exception
    */ 
   public void testSystemClasses() throws Exception
   {
      log.info("+++ Begin testSystemClasses");
      UnifiedLoaderRepository3 parent = new UnifiedLoaderRepository3();
      HeirarchicalLoaderRepository3 repository0 = new HeirarchicalLoaderRepository3(parent);
      URL j0URL = getDeployURL("tests-dummy.jar");
      RepositoryClassLoader ucl0 = repository0.newClassLoader(j0URL, true);

      Class c0 = ucl0.loadClass("java.sql.SQLException");
      StringBuffer info = new StringBuffer();
      ClassLoaderUtils.displayClassInfo(c0, info);
      log.info("Loaded c0: "+info);

      HeirarchicalLoaderRepository3 repository1 = new HeirarchicalLoaderRepository3(parent);
      repository1.setUseParentFirst(false);
      RepositoryClassLoader ucl1 = repository1.newClassLoader(j0URL, true);      
      Class c1 = ucl1.loadClass("java.sql.SQLException");
      info.setLength(0);
      ClassLoaderUtils.displayClassInfo(c1, info);
      log.info("Loaded c1: "+info);

      Class c2 = ucl1.loadClass("java.sql.SQLWarning");
      info.setLength(0);
      ClassLoaderUtils.displayClassInfo(c2, info);
      log.info("Loaded c2: "+info);
   }

   /** Tests for accessing java system classes from scoped
    * repositories that have system class packages.
    * 
    * @throws Exception
    */ 
   public void testSystemClasses2() throws Exception
   {
      log.info("+++ Begin testSystemClasses2");
      UnifiedLoaderRepository3 parent = new UnifiedLoaderRepository3();
      HeirarchicalLoaderRepository3 repository0 = new HeirarchicalLoaderRepository3(parent);
      URL j0URL = getDeployURL("java-sql.jar");
      RepositoryClassLoader ucl0 = repository0.newClassLoader(j0URL, true);

      Class c0 = ucl0.loadClass("java.sql.SQLException");
      StringBuffer info = new StringBuffer();
      ClassLoaderUtils.displayClassInfo(c0, info);
      log.info("Loaded c0: "+info);

      HeirarchicalLoaderRepository3 repository1 = new HeirarchicalLoaderRepository3(parent);
      repository1.setUseParentFirst(false);
      RepositoryClassLoader ucl1 = repository1.newClassLoader(j0URL, true);      
      Class c1 = ucl1.loadClass("java.sql.SQLException");
      info.setLength(0);
      ClassLoaderUtils.displayClassInfo(c1, info);
      log.info("Loaded c1: "+info);

      Class c2 = ucl1.loadClass("java.sql.SQLWarning");
      info.setLength(0);
      ClassLoaderUtils.displayClassInfo(c2, info);
      log.info("Loaded c2: "+info);
   }

   /** Test the interaction through jndi of a service which binds a custom
    * object into jndi and a servlet which looks up the custom object when
    * the service and servlet have different class loader scopes that both
    * have the custom object. 
    */
   public void testSharedJNDI() throws Exception
   {
      getLog().debug("+++ testSharedJNDI");
      try
      {
         deploy("shared-jndi.sar");
         deploy("shared-jndi.war");
         URL servletURL = new URL("http://" + getServerHost() + ":8080/shared-jndi/LookupServlet");
         InputStream reply = (InputStream) servletURL.getContent();
         getLog().debug("Accessed: "+servletURL);
         logReply(reply);
      }
      catch(Exception e)
      {
         getLog().info("Failed to access LookupServlet", e);
         throw e;
      }
      finally
      {
         undeploy("shared-jndi.war");
         undeploy("shared-jndi.sar");
      }
   }

   private void logReply(InputStream reply) throws IOException
   {
      getLog().debug("Begin reply");
      byte[] tmp = new byte[256];
      while( reply.read(tmp) > 0 )
         getLog().debug(new String(tmp));
      reply.close();
      getLog().debug("End reply");
   }
}
