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
package org.jboss.test.binding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.jmx.adaptor.rmi.RMIAdaptorExt;
import org.jboss.management.j2ee.StateManageable;
import org.jboss.system.BarrierController;
import org.jboss.system.ServiceMBean;
import org.jboss.test.JBossTestCase;

/** Tests of the effect of the binding manager service on a two jboss instances.
 * This needs the configurations created by the test-example-binding-manager
 * target running to pass.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85592 $
 */
public class BindingServiceUnitTestCase
   extends JBossTestCase
{
   static final String SERVER0_JNDI_URL = "jnp://" + System.getProperty("jbosstest.server.host", "localhost") + ":1199";
   static final String SERVER1_JNDI_URL = "jnp://" + System.getProperty("jbosstest.server.host", "localhost") + ":1299";
   static HashSet VALID_STATES = new HashSet();

   static
   {
      // JSR-77 running state
      VALID_STATES.add("j2ee.state.running");
      // JBoss mbean service started state
      VALID_STATES.add("Started");
   }
   public BindingServiceUnitTestCase(String name)
   {
      super(name);
   }

   /** Query for all mbeans in the jnp://localhost:1199 and jnp://localhost:1299
    * servers and assert that every mbean with a State attribute has reached
    * the ServiceMBean.STARTED state.
    * 
    * @throws Exception
    */
   public void testAvailableServicesServer0()
      throws Exception
   {
      int count = testAvailableServices(SERVER0_JNDI_URL);
      log.info("server0 service count:"+count);
   }
   public void testAvailableServicesServer1()
      throws Exception
   {
      int count = testAvailableServices(SERVER1_JNDI_URL);
      log.info("server1 service count:"+count);
   }

   private int testAvailableServices(String jndiURL)
      throws Exception
   {
      log.info("+++ testAvailableServices, jndiURL="+jndiURL);

      Properties env = new Properties();
      env.setProperty(Context.PROVIDER_URL, jndiURL);
      InitialContext ctx = new InitialContext(env);
      RMIAdaptorExt server = (RMIAdaptorExt) ctx.lookup("jmx/invoker/RMIAdaptor");
      ObjectName all = new ObjectName("*:*");
      Set allNames = server.queryNames(all, null);
      ArrayList serverErrors = new ArrayList();
      Iterator names = allNames.iterator();
      int serviceCount = 0;
      while( names.hasNext() )
      {
         ObjectName name = (ObjectName) names.next();
         try
         {
            // BarrierController Barriers can be in CREATED or STOPPED state
            // e.g. if the controller's startup notification hasn't been received,
            // so log a message and exclude them from the search.
            boolean isBarrier = server.isInstanceOf(name, BarrierController.Barrier.class.getName());
            if( isBarrier )
            {
               log.debug("Skipping BarrierController.Barrier service: '" + name
                     + "', in state: " + (String) server.getAttribute(name, "StateString"));
               continue;
            }
            /* If this is a JSR-77 mbean, only the StateManageable types
             have a meaningful state string
            */
            boolean jsr77State = server.isInstanceOf(name, StateManageable.class.getName());
            if( jsr77State )
            {
               // the stateManageable also needs to be true
               Boolean flag = (Boolean) server.getAttribute(name, "stateManageable");
               jsr77State = flag.booleanValue();
            }
            boolean mbeanService = server.isInstanceOf(name, ServiceMBean.class.getName());
            if( jsr77State == true || mbeanService == true )
            {
               serviceCount ++;
               String state = (String) server.getAttribute(name, "StateString");               
               if( VALID_STATES.contains(state) == false )
               {
                  // EJB3 mbeans don't set their state correctly, so ignore...
                  String serviceKey = name.getKeyProperty("service");
                  if(serviceKey != null && serviceKey.equals("EJB3"))
                     continue;
                  String msg = name+" is not Started, state="+state;
                  log.error(msg);
                  serverErrors.add(msg);
               }
            }
         }
         catch(AttributeNotFoundException e)
         {
            // Ignore as a non-service
         }
      }
      assertTrue("All services are started, errors="
         +serverErrors.size(), serverErrors.size() == 0);
      return serviceCount;
   }

   /**
    * Override to ignore 
    */
   public void testServerFound()
   {
   }
}
