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
package org.jboss.test.cluster.defaultcfg.test;

import java.util.ArrayList;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.hapartition.rpc.Person;
import org.jboss.test.cluster.hapartition.rpc.PersonQuery;

/** Tests of clustered RPC calls
 *
 * @author Jerry Gauthier
 * @version $Revision: 85945 $
 */
public class RPCTestCase extends JBossClusteredTestCase
{
   // must match service names in rpc-tests.sar
   private static final String RPC_SERVICE = "jboss.test:service=RPCTestCase";
   private static final String RPC_ONENODE_SERVICE = "jboss.test:service=RPCOneNodeTestCase";
   private static final String RPC_CLASSLOADER_SERVICE = "jboss.test:service=RPCClassLoaderTestCase";
   private static final String RPC_ONENODE_CLASSLOADER_SERVICE = "jboss.test:service=RPCOneNodeClassLoaderTestCase";
   
   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(RPCTestCase.class, "rpc-tests.sar, rpc-cl-tests.sar");
      return t1;
   }

   public RPCTestCase(String name)
   {
      super(name);
   }

   public void testMethodOnCluster() throws Exception
   {
      log.debug("+++ testMethodOnCluster");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      MBeanServerConnection server0 = adaptors[0];
      ObjectName rpcService = new ObjectName(RPC_SERVICE);

      Object obj0 = server0.invoke(rpcService, "runRetrieveAll", null, null);
      assertNotNull("expected ArrayList as result type, got null", obj0);
      assertTrue( "expected ArrayList as result type, got " +obj0.getClass().getName(), obj0 instanceof ArrayList);
      ArrayList responses = (ArrayList)obj0;
      
      // there should be two Person responses, the attributes should differ
      assertEquals("Result should contain two responses; ", 2, responses.size());      
      for (int i = 0; i < responses.size(); i++)
      {
         Object response = responses.get(i);
         if (response instanceof Exception)
            fail("received exception response: " + ((Exception)response).toString());
         assertTrue("expected Person as response type, got " +response.getClass().getName(), response instanceof Person);
      }
      Person person0 = (Person)responses.get(0);
      Person person1 = (Person)responses.get(1);
      assertFalse("expected different person names, got " + person0.getName(),
                  person0.getName().equals(person1.getName()));

   }
   
   public void testParmMethodOnCluster() throws Exception
   {
      log.debug("+++ testParmMethodOnCluster");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      MBeanServerConnection server1 = adaptors[1];
      ObjectName rpcService = new ObjectName(RPC_SERVICE);
      
      // try using a custom class as parameter
      String employer = "WidgetsRUs";
      PersonQuery query = new PersonQuery();
      query.setEmployer(employer);
      Object[] parms = new Object[]{query};
      String[] types = new String[]{PersonQuery.class.getName()};
      
      Object obj1 = server1.invoke(rpcService, "runRetrieveQuery", parms, types);
      assertNotNull("expected ArrayList as result type, got null", obj1);
      assertTrue( "expected ArrayList as result type, got " +obj1.getClass().getName(), obj1 instanceof ArrayList);
      ArrayList responses = (ArrayList)obj1;
      
      // there should be two responses, a Person and a null value
      assertEquals("Result should contain two responses; ", 2, responses.size());
      for (int i = 0; i < responses.size(); i++)
      {
         Object response = responses.get(i);
         if (response instanceof Exception)
            fail("received exception response: " + ((Exception)response).toString());
         if (response != null)
         {
            assertTrue("expected Person as response type, got " +response.getClass().getName(), response instanceof Person);
            String respEmpl = ((Person)response).getEmployer();
            assertTrue("expected " + employer + " as selected response value, got " + respEmpl,
                        (employer.equalsIgnoreCase(respEmpl)));
         }
      }

   }
   
   public void testAsynchMethodOnCluster() throws Exception
   {
      log.debug("+++ testAsynchMethodOnCluster");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      MBeanServerConnection server0 = adaptors[0];
      MBeanServerConnection server1 = adaptors[1];
      ObjectName rpcService = new ObjectName(RPC_SERVICE);
      
      // this will set the 'notified' attribute to true for all Person objects in RPCUser
      server0.invoke(rpcService, "runNotifyAllAsynch", null, null);
      Thread.sleep(5000);
      
      // confirm the attribute has been set successfully on each node
      Object obj1 = server1.invoke(rpcService, "runRetrieveAll", null, null);
      assertNotNull("expected ArrayList as result type, got null", obj1);
      assertTrue( "expected ArrayList as result type, got " +obj1.getClass().getName(), obj1 instanceof ArrayList);
      ArrayList responses = (ArrayList)obj1;
      
      // there should be two Person responses, the 'notified' attribute should be true
      assertEquals("Result should contain two responses; ", 2, responses.size());      
      for (int i = 0; i < responses.size(); i++)
      {
         Object response = responses.get(i);
         if (response instanceof Exception)
            fail("received exception response: " + ((Exception)response).toString());
         assertTrue("expected Person as response type, got " +response.getClass().getName(), response instanceof Person);
         Person person = (Person)response;
         assertTrue("expected true as response value, got false for " + person.getName(),
                     (person.getNotified() == true));
         
      }
   }
   
   public void testMethodOnCoordinatorNode() throws Exception
   {
      log.debug("+++ testMethodOnCoordinatorNode");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      MBeanServerConnection server0 = adaptors[0];
      ObjectName rpcService = new ObjectName(RPC_SERVICE);
      
      Object obj0 = server0.invoke(rpcService, "runRetrieveFromCoordinator", null, null);
      assertNotNull("expected ArrayList as result type, got null", obj0);
      assertTrue( "expected ArrayList as result type, got " +obj0.getClass().getName(), obj0 instanceof ArrayList);
      ArrayList responses = (ArrayList)obj0;

      // there should be one Person response
      assertEquals("Result should contain one response; ", 1, responses.size());  
      Object response = responses.get(0);
      if (response instanceof Exception)
         fail("received exception response: " + ((Exception)response).toString());
      assertTrue("expected Person as response type, got " +response.getClass().getName(), response instanceof Person);

      String employer = "WidgetsRUs";
      String respEmpl = ((Person)response).getEmployer();
      assertTrue("expected " + employer + " as selected response value, got " + respEmpl,
                  (employer.equalsIgnoreCase(respEmpl)));
   }
   
   public void testMethodOnOneNode() throws Exception
   {
      log.debug("+++ testMethodOnOneNode");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      MBeanServerConnection server0 = adaptors[0];
      ObjectName rpcService = new ObjectName(RPC_ONENODE_SERVICE);

      Object obj0 = server0.invoke(rpcService, "runRetrieveAll", null, null);
      assertNotNull("expected ArrayList as result type, got null", obj0);
      assertTrue( "expected ArrayList as result type, got " +obj0.getClass().getName(), obj0 instanceof ArrayList);
      ArrayList responses = (ArrayList)obj0;
      
      // there should be one response as the service is only registered on one node
      assertEquals("Result should contain one response; ", 1, responses.size());      
      for (int i = 0; i < responses.size(); i++)
      {
         Object response = responses.get(i);
         if (response instanceof Exception)
            fail("received exception response: " + ((Exception)response).toString());
         assertTrue("expected Person as response type, got " +response.getClass().getName(), response instanceof Person);
      }

   }
   
   public void testClassLoaderParmMethodOnOneNode() throws Exception
   {
      log.debug("+++ testClassLoaderParmMethodOnOneNode");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      MBeanServerConnection server1 = adaptors[1];
      ObjectName rpcService = new ObjectName(RPC_ONENODE_CLASSLOADER_SERVICE);
      
      // try using a custom class as parameter
      String employer = "WidgetsRUs";
      PersonQuery query = new PersonQuery();
      query.setEmployer(employer);
      Object[] parms = new Object[]{query};
      String[] types = new String[]{PersonQuery.class.getName()};
      
      Object obj1 = server1.invoke(rpcService, "runRetrieveQuery", parms, types);
      assertNotNull("expected ArrayList as result type, got null", obj1);
      assertTrue( "expected ArrayList as result type, got " +obj1.getClass().getName(), obj1 instanceof ArrayList);
      ArrayList responses = (ArrayList)obj1;
      
      // there should be one response as the service is only registered on one node
      assertEquals("Result should contain one response; ", 1, responses.size());
      for (int i = 0; i < responses.size(); i++)
      {
         Object response = responses.get(i);
         if (response instanceof Exception)
            fail("received exception response: " + ((Exception)response).toString());
         if (response != null)
         {
            assertTrue("expected Person as response type, got " +response.getClass().getName(), response instanceof Person);
            String respEmpl = ((Person)response).getEmployer();
            assertTrue("expected " + employer + " as selected response value, got " + respEmpl,
                        (employer.equalsIgnoreCase(respEmpl)));
         }
      }

   }
   
   public void testClassLoaderParmMethodOnCluster() throws Exception
   {
      log.debug("+++ testClassLoaderParmMethodOnCluster");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      MBeanServerConnection server1 = adaptors[1];
      ObjectName rpcService = new ObjectName(RPC_CLASSLOADER_SERVICE);
      
      // try using a custom class as parameter
      String employer = "WidgetsRUs";
      PersonQuery query = new PersonQuery();
      query.setEmployer(employer);
      Object[] parms = new Object[]{query};
      String[] types = new String[]{PersonQuery.class.getName()};
      
      Object obj1 = server1.invoke(rpcService, "runRetrieveQuery", parms, types);
      assertNotNull("expected ArrayList as result type, got null", obj1);
      assertTrue( "expected ArrayList as result type, got " +obj1.getClass().getName(), obj1 instanceof ArrayList);
      ArrayList responses = (ArrayList)obj1;
      
      // there should be two responses, a Person and a null value
      assertEquals("Result should contain two responses; ", 2, responses.size());
      for (int i = 0; i < responses.size(); i++)
      {
         Object response = responses.get(i);
         if (response instanceof Exception)
            fail("received exception response: " + ((Exception)response).toString());
         if (response != null)
         {
            assertTrue("expected Person as response type, got " +response.getClass().getName(), response instanceof Person);
            String respEmpl = ((Person)response).getEmployer();
            assertTrue("expected " + employer + " as selected response value, got " + respEmpl,
                        (employer.equalsIgnoreCase(respEmpl)));
         }
      }

   }
   
   public void testClassLoaderMethodOnCluster() throws Exception
   {
      log.debug("+++ testClassLoaderMethodOnCluster");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      MBeanServerConnection server0 = adaptors[0];
      ObjectName rpcService = new ObjectName(RPC_CLASSLOADER_SERVICE);

      Object obj0 = server0.invoke(rpcService, "runRetrieveAll", null, null);
      assertNotNull("expected ArrayList as result type, got null", obj0);
      assertTrue( "expected ArrayList as result type, got " +obj0.getClass().getName(), obj0 instanceof ArrayList);
      ArrayList responses = (ArrayList)obj0;
      
      // there should be two Person responses, the attributes should differ
      assertEquals("Result should contain two responses; ", 2, responses.size());      
      for (int i = 0; i < responses.size(); i++)
      {
         Object response = responses.get(i);
         if (response instanceof Exception)
            fail("received exception response: " + ((Exception)response).toString());
         assertTrue("expected Person as response type, got " +response.getClass().getName(), response instanceof Person);
      }
      Person person0 = (Person)responses.get(0);
      Person person1 = (Person)responses.get(1);
      assertFalse("expected different person names, got " + person0.getName(),
                  person0.getName().equals(person1.getName()));

   }

}
