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
package org.jboss.test.aop.nonjunit;

import javax.naming.InitialContext;

import org.jboss.test.JBossTestCase;
import junit.framework.Test;
import java.net.InetAddress;
import javax.management.ObjectName;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
/**
* Sample client for the jboss container. 
*
* @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
* @version $Id: ClusterCacheTester.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
*/

public class ClusterCacheTester 
{
   static RMIAdaptor server1;
   static RMIAdaptor server2;
   public static void main(String args[] ) throws Exception
   {
      String serverName = InetAddress.getLocalHost().getHostName();
      String connectorName = "jmx:" + serverName + ":rmi";
      RMIAdaptor server1 = (RMIAdaptor)new InitialContext().lookup(connectorName);

      Properties p = new Properties();
      p.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      p.put(Context.URL_PKG_PREFIXES, "jboss.naming:org.jnp.interfaces");
      p.put(Context.PROVIDER_URL, "localhost:11099");
      RMIAdaptor server2 = (RMIAdaptor)new InitialContext(p).lookup(connectorName);
      ObjectName testerName = new ObjectName("jboss.aop:name=CacheTester");

      int baseAge = 32;
      

      for (int j = 0; j < 5; j++)
      {
         {
            // Just to be absolutely sure that server1 and server2 are different VMs
            Object[] params = {};
            String[] sig = {};
            System.out.println("vmid server1: " + server1.invoke(testerName, "getVMID", params, sig));
            System.out.println("vmid server2: " + server2.invoke(testerName, "getVMID", params, sig));
            
         }
         {
            Object[] params = {"Bill"};
            String[] sig = {"java.lang.String"};
            Integer age1 = (Integer)server1.invoke(testerName, "getAge", params, sig);
            System.out.println("server1 age: " + age1);
            
            Integer age2 = (Integer)server2.invoke(testerName, "getAge", params, sig);
            System.out.println("server2 age: " + age2);
         }
         {
            System.out.println("set age to " + ++baseAge + " on server1");
            Object[] params = {"Bill", new Integer(baseAge)};
            String[] sig = {"java.lang.String", "int"};
            server1.invoke(testerName, "setAge", params, sig);
         }
         {
            System.out.println("check age");
            Object[] params = {"Bill"};
            String[] sig = {"java.lang.String"};
            Integer age1 = (Integer)server1.invoke(testerName, "getAge", params, sig);
            System.out.println("server1 age: " + age1);
            
            Integer age2 = (Integer)server2.invoke(testerName, "getAge", params, sig);
            System.out.println("server2 age: " + age2);
         }
         {
            Object[] params = {"Bill"};
            String[] sig = {"java.lang.String"};
            List hobbies = (List)server1.invoke(testerName, "getHobbies", params, sig);
            System.out.println("server1 hobbies: ");
            for (int i = 0; i < hobbies.size(); i++)
            {
               System.out.println("   " + hobbies.get(i));
         }
            
            hobbies = (List)server2.invoke(testerName, "getHobbies", params, sig);
            System.out.println("server2 hobbies: ");
            for (int i = 0; i < hobbies.size(); i++)
            {
               System.out.println("   " + hobbies.get(i));
            }
         }
         {
            System.out.println("addHobby fishing");
            Object[] params = {"Bill", "fishing"};
            String[] sig = {"java.lang.String", "java.lang.String"};
            server1.invoke(testerName, "addHobby", params, sig);
         }
         {
            Object[] params = {"Bill"};
            String[] sig = {"java.lang.String"};
            List hobbies = (List)server1.invoke(testerName, "getHobbies", params, sig);
            System.out.println("server1 hobbies: ");
            for (int i = 0; i < hobbies.size(); i++)
            {
               System.out.println("   " + hobbies.get(i));
            }
            
            hobbies = (List)server2.invoke(testerName, "getHobbies", params, sig);
            System.out.println("server2 hobbies: ");
            for (int i = 0; i < hobbies.size(); i++)
            {
               System.out.println("   " + hobbies.get(i));
            }
         }
         {
            Object[] params = {"Bill"};
            String[] sig = {"java.lang.String"};
            String city1= (String)server1.invoke(testerName, "getCity", params, sig);
            System.out.println("server1 city: " + city1);
            
            String city2 = (String)server2.invoke(testerName, "getCity", params, sig);
            System.out.println("server2 city: " + city2);
         }
         {
            System.out.println("set city to Atlanta");
            Object[] params = {"Bill", "Atlanta" + baseAge};
            String[] sig = {"java.lang.String", "java.lang.String"};
            server1.invoke(testerName, "setCity", params, sig);
         }
         {
            Object[] params = {"Bill"};
            String[] sig = {"java.lang.String"};
            String city1= (String)server1.invoke(testerName, "getCity", params, sig);
            System.out.println("server1 city: " + city1);
            
            String city2 = (String)server2.invoke(testerName, "getCity", params, sig);
            System.out.println("server2 city: " + city2);
         }
      }
   }
}
