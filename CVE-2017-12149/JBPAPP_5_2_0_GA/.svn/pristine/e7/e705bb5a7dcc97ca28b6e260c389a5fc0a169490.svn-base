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
package org.jboss.test.profileservice.test;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.RunState;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.TableValue;
import org.jboss.test.ejb.proxy.beans.StatefulCounter;
import org.jboss.test.ejb.proxy.beans.StatefulCounterHome;
import org.jboss.test.ejb.proxy.beans.HandleRetrievalStatefulSessionInterceptor.RetrievalMethodHandle;
import org.jboss.test.ejb.proxy.test.ProxyLogicTestCase;

/**
 * Tests JMX components exposed outside the MC
 *
 * @author Jason T. Greene
 */
public class JMXMappingUnitTestCase extends AbstractProfileServiceTest
{
   public JMXMappingUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(JMXMappingUnitTestCase.class, "ejbproxy-test.jar");
   }

   public void testEjbMetrics() throws Exception
   {
      getLog().debug(getName());

      Object ref = getInitialContext().lookup("ejb/StatefulCounterEjb");
      StatefulCounterHome home = (StatefulCounterHome) PortableRemoteObject.narrow(ref, StatefulCounterHome.class);
      StatefulCounter counter = home.create();

      assertEquals(1, counter.count());
      assertEquals(2, counter.count());

      ManagementView mgtView = getManagementView();
      mgtView.load();
      Set<ManagedComponent> comps = mgtView.getComponentsForType(new ComponentType("EJB", "StatefulSession"));
      for (ManagedComponent comp : comps)
      {
         System.out.println(comp.getName());
         ManagedProperty property = comp.getProperty("DetypedInvocationStatistics");
         if ("jboss.j2ee:jndiName=ejb/StatefulCounterEjb,service=EJB".equals(comp.getName()))
         {
            MetaValue value = property.getValue();
            System.out.println("Value = " + value);
            CompositeValue methodStatsMap = (CompositeValue)((CompositeValue) value).get("methodStats");
            CompositeValue methodStats = (CompositeValue)methodStatsMap.get("count");
            assertEquals(2L, ((SimpleValue)methodStats.get("count")).getValue());
            return;
         }
      }

      fail("Could not find EJB!");
   }

   public void testWebApplicationManager()
      throws Exception
   {
      ManagementView mgtView = getManagementView();
      mgtView.load();
      Set<ManagedComponent> comps = mgtView.getComponentsForType(new ComponentType("MBean", "WebApplicationManager"));
      for (ManagedComponent comp : comps)
      {
         if ("jboss.web:host=localhost,path=/jmx-console,type=Manager".equals(comp.getName()))
         {
            assertEquals(16, ((SimpleValue)comp.getProperty("sessionIdLength").getValue()).getValue());
            assertEquals("MD5", ((SimpleValue)comp.getProperty("algorithm").getValue()).getValue());
            return;
         }
      }

      fail("Could not find jmx-console Manager");
   }

   public void testServlet() throws Exception
   {
      ManagementView mgtView = getManagementView();
      mgtView.load();
      Set<ManagedComponent> comps = mgtView.getComponentsForType(new ComponentType("MBean", "Servlet"));
      for (ManagedComponent comp : comps)
      {
         if ("jboss.web:J2EEApplication=none,J2EEServer=none,WebModule=//localhost/jmx-console,j2eeType=Servlet,name=HtmlAdaptor".equals(comp.getName()))
         {
            assertEquals("jboss.web", ((SimpleValue)comp.getProperty("engineName").getValue()).getValue());

            // Statistic
            int requests = (Integer)((SimpleValue)comp.getProperty("requestCount").getValue()).getValue();
            new URL("http://" + getServerHost() + ":8080/jmx-console/HtmlAdaptor").openStream().close();
            assertEquals(requests + 1, ((SimpleValue)comp.getProperty("requestCount").getValue()).getValue());
            return;
         }
      }

      fail("Could not find HtmlAdapor servlet");
   }

   public void testWebApplication() throws Exception
   {
      ManagementView mgtView = getManagementView();
      mgtView.load();
      Set<ManagedComponent> comps = mgtView.getComponentsForType(new ComponentType("MBean", "WebApplication"));
      for (ManagedComponent comp : comps)
      {
         if ("jboss.web:J2EEApplication=none,J2EEServer=none,j2eeType=WebModule,name=//localhost/jmx-console".equals(comp.getName()))
         {
            assertEquals("jboss", ((SimpleValue)comp.getProperty("server").getValue()).getValue());
            ((SimpleValue)comp.getProperty("docBase").getValue()).getValue().toString().endsWith("jmx-console.war/");

            return;
         }
      }

      fail("Could not find jmx-console Web Application");
   }

   public void testWebHost() throws Exception
   {
      ManagementView mgtView = getManagementView();
      mgtView.load();
      Set<ManagedComponent> comps = mgtView.getComponentsForType(new ComponentType("MBean", "WebHost"));
      for (ManagedComponent comp : comps)
      {
         if ("jboss.web:host=localhost,type=Host".equals(comp.getName()))
         {
            assertEquals("localhost", ((SimpleValue)comp.getProperty("name").getValue()).getValue());
            return;
         }
      }

      fail("Could not find localhost Host");
   }

   public void testConnector() throws Exception
   {
      ManagementView mgtView = getManagementView();
      mgtView.load();
      Set<ManagedComponent> comps = mgtView.getComponentsForType(new ComponentType("MBean", "WebRequestProcessor"));
      for (ManagedComponent comp : comps)
      {
         if (comp.getName().startsWith("jboss.web:name=http-"))
         {
            // Statistic
            int requests = (Integer)((SimpleValue)comp.getProperty("requestCount").getValue()).getValue();
            new URL("http://" + getServerHost() + ":8080/").openStream().close();
            assertEquals(requests + 1, ((SimpleValue)comp.getProperty("requestCount").getValue()).getValue());
            return;
         }
      }

      fail("Could not find connector!");
   }

   public void testContextMO() throws Exception
   {
      ManagementView mgtView = getManagementView();
      mgtView.load();
      Set<ManagedComponent> comps = mgtView.getComponentsForType(new ComponentType("WAR", "Context"));
      for (ManagedComponent comp : comps)
      {
         if (comp.getDeployment().getName().endsWith("jmx-console.war/"))
         {
            assertEquals("/jmx-console", ((SimpleValue)comp.getProperty("contextRoot").getValue()).getValue());
            return;
         }
      }

      fail("Could not find deployment context root!");
   }

   public void testRunState() throws Exception
   {
      ManagementView mgtView = getManagementView();
      Set<ManagedComponent> comps = mgtView.getComponentsForType(new ComponentType("MBean", "WebHost"));
      for (ManagedComponent comp : comps)
      {
         if ("jboss.web:host=localhost,type=Host".equals(comp.getName()))
         {
            assertEquals(RunState.RUNNING, comp.getRunState());
            return;
         }
      }

      fail("Could not find localhost Host");
   }

}