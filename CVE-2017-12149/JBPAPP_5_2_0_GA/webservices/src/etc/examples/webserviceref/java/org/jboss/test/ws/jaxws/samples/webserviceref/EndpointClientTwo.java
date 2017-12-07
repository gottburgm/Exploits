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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.jboss.logging.Logger;
import org.jboss.test.ws.jaxws.samples.webserviceref.Endpoint;
import org.jboss.test.ws.jaxws.samples.webserviceref.EndpointService;
//import org.jboss.ws.core.ConfigProvider;

//Test on type
@WebServiceRef(name = "Service1")
// Test multiple on type
@WebServiceRefs( { @WebServiceRef(name = "Service2"), @WebServiceRef(name = "Port1", type = Endpoint.class) })
public class EndpointClientTwo
{
   // provide logging
   private static final Logger log = Logger.getLogger(EndpointClientTwo.class);
   
   // Test on field
   @WebServiceRef(name = "Service3")
   static Service service3;

   // Test on field
   @WebServiceRef(name = "Service4")
   static EndpointService service4;

   // Test on field
   @WebServiceRef(name = "Port2")
   static Endpoint port2;

   // Test on field
   @WebServiceRef(name = "Port3")
   static Endpoint port3;

   static InitialContext iniCtx;
   static Map<String, String> testResult = new HashMap<String, String>();

   public static void main(String[] args) throws Exception
   {
      String testName = args[0];
      EndpointClientTwo client = new EndpointClientTwo();
      Method method = EndpointClientTwo.class.getMethod(testName, new Class[] { String.class });
      try
      {
         String retStr = (String)method.invoke(client, testName);
         testResult.put(testName, retStr);
      }
      catch (InvocationTargetException ex)
      {
         log.error("Invocation error", ex);
         testResult.put(testName, ex.getTargetException().toString());
      }
      catch (Exception ex)
      {
         log.error("Error", ex);
         testResult.put(testName, ex.toString());
      }
   }

   /**
    * Customize service-class-name, service-qname
    */
   public String testService1(String reqStr) throws Exception
   {
      EndpointService service = (EndpointService)iniCtx.lookup("java:comp/env/Service1");
      Endpoint port = service.getEndpointPort();
      return port.echo(reqStr);
   }

   /**
    * Customize config-name, config-file
    */
   public String testService2(String reqStr) throws Exception
   {
      Service service = (Service)iniCtx.lookup("java:comp/env/Service2");

      Endpoint port = service.getPort(Endpoint.class);
      //verifyConfig((ConfigProvider)port);
      
      return port.echo(reqStr);
   }

   /**
    * Customize service-class-name, service-qname
    */
   public String testService3(String reqStr) throws Exception
   {
      Endpoint port = ((EndpointService)service3).getEndpointPort();
      String resStr1 = port.echo(reqStr);
      
      EndpointService service = (EndpointService)iniCtx.lookup("java:comp/env/Service3");
      port = service.getEndpointPort();
      
      String resStr2 = port.echo(reqStr);
      
      return resStr1 + resStr2; 
   }

   /**
    * Customize config-name, config-file
    */
   public String testService4(String reqStr) throws Exception
   {
      Endpoint port = service4.getEndpointPort();
      String resStr1 = port.echo(reqStr);
      //verifyConfig((ConfigProvider)port);
      
      EndpointService service = (EndpointService)iniCtx.lookup("java:comp/env/Service4");
      port = service.getEndpointPort();
      //verifyConfig((ConfigProvider)port);
      
      String resStr2 = port.echo(reqStr);
      
      return resStr1 + resStr2; 
   }

   /**
    * Customize port-info: port-qname, config-name, config-file 
    */
   public String testPort1(String reqStr) throws Exception
   {
      Endpoint port = (Endpoint)iniCtx.lookup("java:comp/env/Port1");
      //verifyConfig((ConfigProvider)port);
      
      return port.echo(reqStr);
   }

   /**
    * Customize port-info: service-endpoint-interface, config-name, config-file 
    */
   public String testPort2(String reqStr) throws Exception
   {
      //verifyConfig((ConfigProvider)port2);
      String resStr1 = port2.echo(reqStr);

      Endpoint port = (Endpoint)iniCtx.lookup("java:comp/env/Port2");
      //verifyConfig((ConfigProvider)port);
      
      String resStr2 = port.echo(reqStr);
      
      return resStr1 + resStr2; 
   }

   /**
    * Customize port-info: service-endpoint-interface, port-qname, stub-property 
    */
   public String testPort3(String reqStr) throws Exception
   {
      String resStr1 = port3.echo(reqStr);
      
      BindingProvider bp = (BindingProvider)port3;
      verifyProperties(bp.getRequestContext());

      Endpoint port = (Endpoint)iniCtx.lookup("java:comp/env/Port3");
      String resStr2 = port.echo(reqStr);
      
      return resStr1 + resStr2; 
   }

   private void verifyProperties(Map<String, Object> ctx)
   {
      String username = (String)ctx.get(BindingProvider.USERNAME_PROPERTY);
      if ("kermit".equals(username) == false) 
         throw new RuntimeException("Invalid username: " + username);
      
      String password = (String)ctx.get(BindingProvider.PASSWORD_PROPERTY);
      if ("thefrog".equals(password) == false) 
         throw new RuntimeException("Invalid password: " + password);
   }

   /*private void verifyConfig(ConfigProvider cp)
   {
      if ("Custom Client".equals(cp.getConfigName()) == false)
         throw new RuntimeException("Invalid config name: " + cp.getConfigName());

      if ("META-INF/jbossws-client-config.xml".equals(cp.getConfigFile()) == false)
         throw new RuntimeException("Invalid config file: " + cp.getConfigFile());
   } */
}
