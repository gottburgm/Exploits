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
package org.jboss.test.security.test; 

import java.net.URL;

import javax.management.Attribute; 
import javax.management.MBeanServerConnection; 
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;
  
import org.jboss.system.ServiceContext;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup; 
import org.jboss.test.util.ServiceControllerUtil;

//$Id: DynamicLoginConfigServiceUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  Unit tests for the Dynamic Login Config Service
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  May 12, 2006
 *  @version $Revision: 81036 $
 */
public class DynamicLoginConfigServiceUnitTestCase extends JBossTestCase
{ 
   private String login_config = "<policy>\n<application-policy name='test-dyn'>"+
                "<authentication>"+
                "<login-module code='org.jboss.security.auth.spi.UsersRolesLoginModule'" +
                " flag = 'required' /> </authentication></application-policy></policy>";
   
   private ServiceControllerUtil sutil = null;
   public DynamicLoginConfigServiceUnitTestCase(String name)
   {
      super(name); 
   }
   
   /**
    * JBAS-3210: DynamicLoginConfig service fails absolute login-config.xml url
    * @throws Exception
    */
   public void testAbsoluteLoginConfigURL() throws Exception
   {
      try
      {
         sutil = new ServiceControllerUtil(this.getServer());
      }
      catch (Exception e)
      {
         fail("Exception thrown in creating util class"+e.getLocalizedMessage());
      }
      MBeanServerConnection server = getServer(); 
      ObjectName oname = new ObjectName("jboss:service=TempFileCreator");  
      URL confURL = (URL)server.invoke(oname,"createTempFile",
            new Object[]{"test-dyn",login_config},
            new String[] {"java.lang.String", "java.lang.String"});
      assertNotNull(" config url != null",confURL); 
      ObjectName serviceName = new ObjectName("jboss:service=TestDynamicLoginConfig");
      if(server.isRegistered(serviceName))
         server.unregisterMBean(serviceName);
      prepareTestDynamicLoginConfig(server,serviceName,confURL.toExternalForm()); 
      sutil.createAService(serviceName);
      assertTrue("Created?", sutil.isCreated(serviceName));
      sutil.startAService(serviceName);
      assertTrue("Started?", sutil.isStarted(serviceName)); 
      //Restart the service 
      sutil.stopAService(serviceName);
      assertTrue("Stopped?", sutil.isStopped(serviceName)); 
      sutil.startAService(serviceName);
      assertTrue("Started?", sutil.isStarted(serviceName));  
      
      String authConfig = (String)server.getAttribute(serviceName,"AuthConfig");
      assertEquals(confURL + "matches", confURL.toExternalForm(), authConfig); 
      sutil.stopAService(serviceName);
      assertTrue("Stopped?", sutil.isStopped(serviceName)); 
      sutil.startAService(serviceName);
      assertTrue("Started?", sutil.isStarted(serviceName));
      sutil.destroyAService(serviceName);
      assertEquals("state is Destroyed", ServiceContext.getStateString(ServiceContext.DESTROYED),
            sutil.getStateString(serviceName));
      sutil.removeAService(serviceName);
      if(server.isRegistered(serviceName))
         server.unregisterMBean(serviceName);
   } 
   
   /**
    * JBAS-3422: Do not allow Null AuthConfig or login-config.xml
    * @throws Exception
    */
   public void testAuthConf() throws Exception
   {
      try
      {
         sutil = new ServiceControllerUtil(this.getServer());
      }
      catch (Exception e)
      {
         fail("Exception thrown in creating util class"+e.getLocalizedMessage());
      }
      MBeanServerConnection server = getServer(); 
      ObjectName serviceName = new ObjectName("jboss:service=TestDynamicLoginConfig");
      if(server.isRegistered(serviceName))
      {
         server.unregisterMBean(serviceName);
      }
      prepareTestDynamicLoginConfig(server, 
            new ObjectName("jboss:service=TestDynamicLoginConfig"), null); 
      try
      { 
         sutil.createAService(serviceName);
         assertEquals("state is Created", ServiceContext.getStateString(ServiceContext.CREATED),
               sutil.getStateString(serviceName));
         sutil.startAService(serviceName);
         assertFalse("Should not Start", sutil.isStarted(serviceName));  
      }
      catch(Exception  t)
      {
         fail("Exception thrown:"+t.getLocalizedMessage()); 
      }
      finally
      {
         if(sutil.isStarted(serviceName)) 
         {
            sutil.stopAService(serviceName);
            assertEquals("state is Stopped", ServiceContext.getStateString(ServiceContext.STOPPED),
                  sutil.getStateString(serviceName));
         }
         sutil.destroyAService(serviceName);
         sutil.removeAService(serviceName);
         if(server.isRegistered(serviceName))
            server.unregisterMBean(serviceName);
      }  
   } 
   
   private void prepareTestDynamicLoginConfig(MBeanServerConnection server, 
         ObjectName serviceOName, String confURL) throws Exception
   {
      server.createMBean("org.jboss.security.auth.login.DynamicLoginConfig", 
            serviceOName); 
      if(confURL != null)
      {
         Attribute attr = new Attribute("AuthConfig", confURL);
         server.setAttribute(serviceOName,attr);  
      } 
      
      ObjectName lcs = new ObjectName("jboss.security:service=XMLLoginConfig");
      Attribute attrLCS = new Attribute("LoginConfigService", lcs);
      server.setAttribute(serviceOName,attrLCS); 
   } 
   
   public static Test suite()
   throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(DynamicLoginConfigServiceUnitTestCase.class));
      JBossTestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            deploymentException = null;
            try
            {
               super.setUp();
               this.delegate.init();
               this.redeploy("tempfilecreator.jar");
               this.redeploy(getResourceURL("jmx/tempFileCreator-service.xml")); 
            }
            catch (Exception ex)
            {
               // Throw this in testServerFound() instead.
               deploymentException = ex;
            }
         }
         
         protected void tearDown() throws Exception
         {
            this.undeploy(getResourceURL("jmx/tempFileCreator-service.xml"));
            this.undeploy("tempfilecreator.jar");
            super.tearDown();
         }
      };
      return wrapper; 
   }
}
