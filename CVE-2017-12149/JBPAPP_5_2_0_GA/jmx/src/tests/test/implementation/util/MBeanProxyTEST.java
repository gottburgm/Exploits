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
package test.implementation.util;

import java.util.Set;

import java.lang.reflect.Method;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.RequiredModelMBean;

import test.implementation.util.support.Trivial;
import test.implementation.util.support.TrivialMBean;
import test.implementation.util.support.Trivial2;
import test.implementation.util.support.Trivial2MBean;
import test.implementation.util.support.ExtendedResource;
import test.implementation.util.support.MyInterface;
import test.implementation.util.support.MyInterface2;
import test.implementation.util.support.Resource;
import test.implementation.util.support.ResourceOverride;
import test.implementation.util.support.ResourceIncorrectInfo;

import junit.framework.TestCase;

import org.jboss.mx.util.AgentID;
import org.jboss.mx.util.DefaultExceptionHandler;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.ProxyContext;

import org.jboss.mx.modelmbean.XMBean;


/**
 * Tests for mbean proxy
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81022 $ 
 */
public class MBeanProxyTEST extends TestCase
{
   public MBeanProxyTEST(String s)
   {
      super(s);
   }

   public void testGetWithServer() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:name=test");
   
      server.registerMBean(new Trivial(), oname);
   
      TrivialMBean mbean = (TrivialMBean)MBeanProxy.get(
            TrivialMBean.class, oname, server);      
            
      mbean.doOperation();
   }
   
   public void testGetWithAgentID() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      String agentID     = AgentID.get(server);
      ObjectName oname   = new ObjectName("test:name=test");
      
      server.registerMBean(new Trivial(), oname);

      TrivialMBean mbean = (TrivialMBean)MBeanProxy.get(
            TrivialMBean.class, oname, agentID);
            
      mbean.doOperation();
   }
   
   public void testCreateWithServer() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      TrivialMBean mbean = (TrivialMBean)MBeanProxy.create(
            Trivial.class, TrivialMBean.class, oname, server);
            
      mbean.doOperation();
   }
   
   public void testCreateWithAgentID() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      String agentID     = AgentID.get(server);
      
      TrivialMBean mbean = (TrivialMBean)MBeanProxy.create(
            Trivial.class, TrivialMBean.class, oname, agentID);
            
      mbean.doOperation();
   }
   
   public void testProxyInvocations() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:name=test");
      
      server.registerMBean(new Trivial(), oname);
      
      TrivialMBean mbean = (TrivialMBean)MBeanProxy.get(
            TrivialMBean.class, oname, AgentID.get(server));
      
      mbean.doOperation();
      mbean.setSomething("JBossMX");
      
      assertEquals("JBossMX", mbean.getSomething());
   }

   public void testProxyInvocationWithConflictingMBeanAndContextMethods() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      server.registerMBean(new Trivial(), oname);
      
      TrivialMBean mbean = (TrivialMBean)MBeanProxy.get(
            TrivialMBean.class, oname, AgentID.get(server));
            
      mbean.getMBeanServer();
      assertTrue(mbean.isGMSInvoked());
   }
   
   public void testContextAccess() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      Trivial2MBean mbean = (Trivial2MBean)MBeanProxy.create(
            Trivial2.class, Trivial2MBean.class, oname, server
      );
    
      ProxyContext ctx = (ProxyContext)mbean;  
      
      ctx.getMBeanServer();
   }
   
   public void testProxyInvocationBetweenServers() throws Exception
   {
      MBeanServer server1 = MBeanServerFactory.createMBeanServer();
      MBeanServer server2 = MBeanServerFactory.createMBeanServer();
      ObjectName oname1   = new ObjectName("test:name=target");
      ObjectName oname2   = new ObjectName("test:name=proxy");
      
      // createMBean on server1 and retrieve a proxy to it
      Trivial2MBean mbean = (Trivial2MBean)MBeanProxy.create(
            Trivial2.class, Trivial2MBean.class, oname1, server1
      );
      
      //bind the proxy to server2
      server2.registerMBean(mbean, oname2);
      
      // invoke on server2
      server2.invoke(oname2, "doOperation", null, null);
      
      // check that server1 received the invocation
      assertTrue(((Boolean)server1.getAttribute(oname1, "OperationInvoked")).booleanValue());
   }
   
   public void testSimultaneousTypedAndDetypedInvocations() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      Trivial2MBean mbean = (Trivial2MBean)MBeanProxy.create(
            Trivial2.class, Trivial2MBean.class, oname,server
      );
      
      // typed proxy interface
      mbean.setSomething("Kissa");
      assertTrue(mbean.getSomething().equals("Kissa"));
      
      // detyped proxy interface
      DynamicMBean mbean2 = (DynamicMBean)mbean;
      mbean2.setAttribute(new Attribute("Something", "Koira"));
      assertTrue(mbean2.getAttribute("Something").equals("Koira"));
      
      // direct local server invocation
      server.setAttribute(oname, new Attribute("Something", "Kissa"));
      assertTrue(server.getAttribute(oname, "Something").equals("Kissa"));
            
      // typed proxy interface invocation
      mbean.doOperation();
      assertTrue(mbean.isOperationInvoked());
      
      mbean.reset();
      
      // detyped proxy invocation
      mbean2.invoke("doOperation", null, null);
      assertTrue(((Boolean)mbean2.getAttribute("OperationInvoked")).booleanValue());
      
      mbean2.invoke("reset", null, null);
      
      // direct local server invocation
      server.invoke(oname, "doOperation", null, null);
      assertTrue(((Boolean)server.getAttribute(oname, "OperationInvoked")).booleanValue());
   }
   
   public void testContextAccessToMBeanServer() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      Trivial2MBean mbean = (Trivial2MBean)MBeanProxy.create(
            Trivial2.class, Trivial2MBean.class, oname, server
      );
      
      // query the server this mbean is registered to
      ProxyContext ctx = (ProxyContext)mbean;
      MBeanServer srvr = ctx.getMBeanServer();
      
      Set mbeans = srvr.queryMBeans(new ObjectName("test:*"), null);
      ObjectInstance oi = (ObjectInstance)mbeans.iterator().next();
      
      assertTrue(oi.getObjectName().equals(oname));
      
      assertTrue(srvr.getAttribute(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"),
            "ImplementationName"
      ).equals("JBossMX"));
      
   }
   
   public void testArbitraryInterfaceWithProxy() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
    
      RequiredModelMBean rmm = new RequiredModelMBean();
      Resource resource      = new Resource();
      
      rmm.setManagedResource(resource, "ObjectReference");
      rmm.setModelMBeanInfo(resource.getMBeanInfo());
    
      server.registerMBean(rmm, oname);
      
      MyInterface mbean  = (MyInterface)MBeanProxy.get(
            MyInterface.class, oname, server
      );
      
      mbean.setAttributeName("foo");
      mbean.setAttributeName2("bar");
      
      assertTrue(mbean.getAttributeName2().equals("bar"));
      assertTrue(mbean.doOperation().equals("tamppi"));
   }
   
   /**
    * This test shows how to override the default exception handling for
    * proxy invocations. The default handling for exceptions that are not
    * declared as part of the proxy typed interface (such as InstanceNotFound
    * or AttributeNotFound exceptions) are rethrown as unchecked 
    * RuntimeProxyExceptions. See org.jboss.mx.proxy.DefaultExceptionHandler
    * for details. This behavior can be changed at runtime through the proxy
    * context interface setExceptionHandler() method.
    */
   public void testCustomExceptionHandler() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      ObjectName oname2  = new ObjectName("test:test=test2");
      
      RequiredModelMBean rmm = new RequiredModelMBean();
      Resource resource      = new Resource();
      
      rmm.setManagedResource(resource, "ObjectReference");
      rmm.setModelMBeanInfo(resource.getMBeanInfo());
      
      // create two identical mbeans
      server.registerMBean(rmm, oname);
      server.registerMBean(rmm, oname2);
      
      ProxyContext ctx  = (ProxyContext)MBeanProxy.get(
            MyInterface.class, oname, server
      );

      // override InstanceNotFound exception to redirect from test=test instance
      // to test=test2 instance
      ctx.setExceptionHandler(new DefaultExceptionHandler() 
      {
         public Object handleInstanceNotFound(ProxyContext proxyCtx, InstanceNotFoundException e, Method m, Object[] args) throws Exception
         {
            return proxyCtx.getMBeanServer().invoke(new ObjectName("test:test=test2"), m.getName(), args, null);
         }
      });
         
      // unregister mbean 1
      server.unregisterMBean(oname);
      
      // invocation attempt to mbean1 should now redirect to mbean2
      MyInterface mbean = (MyInterface)ctx;
      assertTrue(mbean.doOperation().equals("tamppi"));      
   }
   
   public void testObjectToStringOnProxy() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      RequiredModelMBean rmm = new RequiredModelMBean();
      Resource resource      = new Resource();
      
      rmm.setManagedResource(resource, "ObjectReference");
      rmm.setModelMBeanInfo(resource.getMBeanInfo());
      
      server.registerMBean(rmm, oname);
      
      MyInterface mbean = (MyInterface)MBeanProxy.get(
            MyInterface.class, oname, server
      );
      
      mbean.toString();
      
      Object o = (Object)mbean;
      o.toString();
   }
   
   public void testObjectToStringOverride() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      RequiredModelMBean rmm = new RequiredModelMBean();
      ResourceOverride resource = new ResourceOverride();
      
      rmm.setManagedResource(resource, "ObjectReference");
      rmm.setModelMBeanInfo(resource.getMBeanInfo());
      
      server.registerMBean(rmm, oname);
      
      MyInterface mbean = (MyInterface)MBeanProxy.get(
            MyInterface.class, oname, server
      );
      
      assertTrue(mbean.toString().equals("Resource"));
      Object o = (Object)mbean;
      assertTrue(o.toString().equals("Resource"));
   }
   
   public void testObjectHashCodeOnProxy() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      RequiredModelMBean rmm = new RequiredModelMBean();
      Resource resource      = new Resource();
      
      rmm.setManagedResource(resource, "ObjectReference");
      rmm.setModelMBeanInfo(resource.getMBeanInfo());
      
      server.registerMBean(rmm, oname);
      
      MyInterface mbean = (MyInterface)MBeanProxy.get(
            MyInterface.class, oname, server
      );
      
      mbean.hashCode();
      
      Object o = (Object)mbean;
      o.toString();
   }
   
   public void testObjectHashCodeOverride() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      RequiredModelMBean rmm = new RequiredModelMBean();
      ResourceOverride resource = new ResourceOverride();
      
      rmm.setManagedResource(resource, "ObjectReference");
      rmm.setModelMBeanInfo(resource.getMBeanInfo());
      
      server.registerMBean(rmm, oname);
      
      MyInterface mbean = (MyInterface)MBeanProxy.get(
            MyInterface.class, oname, server
      );

      assertTrue(mbean.hashCode() == 10);
      Object o = (Object)mbean;
      assertTrue(o.hashCode() == 10);
   }

   public void testObjectEqualsOnProxy() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");

      ModelMBean mmbean = new XMBean();
      Resource resource = new Resource();

      mmbean.setManagedResource(resource, "ObjectReference");
      mmbean.setModelMBeanInfo(resource.getMBeanInfo());

      server.registerMBean(mmbean, oname);

      MyInterface mbean = (MyInterface)MBeanProxy.get(
            MyInterface.class, oname, server
      );
      
      MyInterface mbean2 = (MyInterface)MBeanProxy.get(
            MyInterface.class, oname, server
      );
      
      assertTrue(mbean.equals(mbean));
      assertTrue(!mbean.equals(mbean2));
      assertTrue(!mbean2.equals(mbean));
   }
   
   public void testObjectEqualsOverride() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      ModelMBean rmm = new RequiredModelMBean();
      ResourceOverride resource  = new ResourceOverride("state");
      
      rmm.setManagedResource(resource, "ObjectReference");
      rmm.setModelMBeanInfo(resource.getMBeanInfo());
      
      server.registerMBean(rmm, oname);
            
      MyInterface mbean = (MyInterface)MBeanProxy.get(
            MyInterface.class, oname, server
      );
      
      assertTrue(mbean.equals(mbean));
   }
   
   public void testAttributeNotFoundOnTypedProxy() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      ModelMBean mmbean = new XMBean();
      ResourceIncorrectInfo resource = new ResourceIncorrectInfo();
      
      mmbean.setManagedResource(resource, "ObjectReference");
      mmbean.setModelMBeanInfo(resource.getMBeanInfo());
      
      server.registerMBean(mmbean, oname);
      
      MyInterface mbean = (MyInterface)MBeanProxy.get(
            MyInterface.class, oname, server
      );
      
      ProxyContext ctx = (ProxyContext)mbean;
      ctx.setExceptionHandler(new DefaultExceptionHandler());
      
      try
      {
         mbean.setAttributeName2("some name");
      }
      catch (IllegalArgumentException e)
      {
         // expected
         
         // by default, if no such attribute 'AttributeName2' exists in the
         // MBean metadata (as is the case with ResourceIncorrectInfo), the
         // MBeanProxy invocation handler falls back attempting to execute
         // MBeanServer.invoke() which fails with IAE.
      }
   }

   public void testAttributeNotFoundOnDeTypedProxy() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      ModelMBean mmbean = new XMBean();
      ResourceIncorrectInfo resource = new ResourceIncorrectInfo();
      
      mmbean.setManagedResource(resource, "ObjectReference");
      mmbean.setModelMBeanInfo(resource.getMBeanInfo());
      
      server.registerMBean(mmbean, oname);
      
      DynamicMBean mbean = (DynamicMBean)MBeanProxy.get(oname, server);
      
      ProxyContext ctx = (ProxyContext)mbean;
      ctx.setExceptionHandler(new DefaultExceptionHandler());
      
      try
      {
         mbean.setAttribute(new Attribute("AttributeName2", "some name"));
      }
      catch (AttributeNotFoundException e)
      {
         // expected
      }
   }
   
   public void testInheritanceInTypedProxyArgs() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      XMBean mmbean = new XMBean();
      ExtendedResource resource = new ExtendedResource();
      
      mmbean.setManagedResource(resource, "ObjectReference");
      mmbean.setModelMBeanInfo(resource.getMBeanInfo());
      
      server.registerMBean(mmbean, oname);
      
      MyInterface2 mbean = (MyInterface2)MBeanProxy.get(
            MyInterface2.class, oname, server);
            
      assertTrue(mbean.doOperation().equals("doOperation"));
      
      try
      {
         assertTrue(mbean.executeThis("executeThis").equals("executeThis"));
      }
      catch (ClassCastException e) {
         fail("KNOWN ISSUE: proxy generates incorrect JMX invocation " +
              "signature in case argument subclasses are used.");
      }
   }
   
   public void testInheritanceInProxyAttribute() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      XMBean mmbean = new XMBean();
      ExtendedResource resource = new ExtendedResource();
      
      mmbean.setManagedResource(resource, "ObjectReference");
      mmbean.setModelMBeanInfo(resource.getMBeanInfo());
      
      server.registerMBean(mmbean, oname);
      
      MyInterface2 mbean = (MyInterface2)MBeanProxy.get(
            MyInterface2.class, oname, server);
            
      mbean.setAttribute3("Attribute3");
      
      assertTrue(mbean.getAttribute3().equals("Attribute3"));
   }
   
   public void testInheritanceInProxyReturnType() throws Exception
   {
      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName oname   = new ObjectName("test:test=test");
      
      XMBean mmbean = new XMBean();
      ExtendedResource resource = new ExtendedResource();
      
      mmbean.setManagedResource(resource, "ObjectReference");
      mmbean.setModelMBeanInfo(resource.getMBeanInfo());
      
      server.registerMBean(mmbean, oname);
      
      MyInterface2 mbean = (MyInterface2)MBeanProxy.get(
            MyInterface2.class, oname, server);
            
      assertTrue(mbean.runMe("runMe").equals("runMe"));
   }
   
}
