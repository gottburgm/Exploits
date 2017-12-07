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
package org.jboss.test.jca.ejb;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.test.jca.interfaces.HAConnectionSession;
import org.jboss.test.jca.adapter.MockedXADataSource;
import org.jboss.system.ServiceMBean;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;
import java.rmi.RemoteException;
import java.sql.Connection;

/**
 * @ejb.bean
 *    name="HAConnectionSession"
 *    view-type="remote"
 *    type="Stateless"
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 113230 $</tt>
 */
public class HAConnectionSessionBean
   implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(HAConnectionSessionBean.class);

   private SessionContext ctx;

   /**
    * @ejb.interface-method
    * @ejb.transaction type="NotSupported"
    */
   public void testHaLocalConnection() throws Exception
   {
      //String url1 = "jdbc:hsqldb:hsql://localhost:1702";
      //String url2 = "jdbc:hsqldb:hsql://localhost:1703";
      String port1 = "1702";
      String port2 = "1703";
      ObjectName db1 = new ObjectName("jboss:service=Hypersonic,database=haLocalDB1");
      ObjectName db2 = new ObjectName("jboss:service=Hypersonic,database=haLocalDB2");

      MBeanServer server = MBeanServerLocator.locateJBoss();

      waitForState(server, db1, ServiceMBean.STARTED);
      waitForState(server, db2, ServiceMBean.STARTED);

      DataSource ds = (DataSource)new InitialContext().lookup("java:/TestHADefaultDS");

      stopDb(server, db2);
      String expectedPort = port1;

      for(int i = 0; i < 10; ++i)
      {
         HAConnectionSession me = (HAConnectionSession)ctx.getEJBObject();
         String conUrl = me.getHAConnectionURL(ds);
         log.debug("got connection to: " + conUrl);

         if(!conUrl.endsWith(expectedPort))
         {
            throw new Exception("Expected " + expectedPort + " but got " + conUrl);
         }

         if(conUrl.endsWith(port1))
         {
            stopDb(server, db1);
            startDb(server, db2);
            expectedPort = port2;
         }
         else if(conUrl.endsWith(port2))
         {
            stopDb(server, db2);
            startDb(server, db1);
            expectedPort = port1;
         }
         else
         {
            throw new Exception("Unexpected connection url: " + conUrl);
         }
      }
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="NotSupported"
    */
   public void testHaXaConnection() throws Exception
   {
      String[] urls = MockedXADataSource.getUrls();
      for(int i = 1; i < urls.length; ++i)
      {
         MockedXADataSource.stop(urls[i]);
      }

      DataSource ds = (DataSource)new InitialContext().lookup("java:/MockedHaXaDS");
      HAConnectionSession facade = (HAConnectionSession)ctx.getEJBObject();

      for(int i = 0; i < 3*urls.length; ++i)
      {
         String url = facade.getHAConnectionURL(ds);
         int urlIndex = i % urls.length;

         if(!url.equals(urls[urlIndex]))
         {
            throw new IllegalStateException("Connected to a wrong database: " + url + ", expected " + urls[urlIndex]);
         }

         MockedXADataSource.stop(url);

         urlIndex = (i + 1) % urls.length;
         MockedXADataSource.start(urls[urlIndex]);
      }
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type="RequiresNew"
    */
   public String getHAConnectionURL(DataSource ds) throws Exception
   {
      Connection con = null;
      try
      {
         con = ds.getConnection();
         return con.getMetaData().getURL();
      }
      finally
      {
         if(con != null)
         {
            con.close();
         }
      }
   }

   private void startDb(MBeanServer server, ObjectName db2)
      throws InstanceNotFoundException, MBeanException, ReflectionException, AttributeNotFoundException
   {
      server.invoke(db2, "start", null, null);
      waitForState(server, db2, ServiceMBean.STARTED);
   }

   private void stopDb(MBeanServer server, ObjectName db2)
      throws InstanceNotFoundException,
      MBeanException,
      ReflectionException,
      AttributeNotFoundException
   {
      server.invoke(db2, "stop", null, null);
      waitForState(server, db2, ServiceMBean.STOPPED);
   }

   private void waitForState(MBeanServer server, ObjectName db2, int state)
      throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException
   {
      Integer stateValue = (Integer)server.getAttribute(db2, "State");
      while(stateValue.intValue() != state)
      {
         try
         {
            Thread.sleep(500);
         }
         catch(InterruptedException e)
         {
         }
         stateValue = (Integer)server.getAttribute(db2, "State");
      }
      // The hypersonic MBean is not implementated properly
      // It goes into state STARTED when in reality there is
      // a background thread still recovering the server
      if (stateValue.intValue() == ServiceMBean.STARTED)
      {
         try
         {
            Thread.sleep(5000);
         }
         catch (InterruptedException e)
         {
         }
      }
   }

   /**
    * @throws javax.ejb.CreateException Description of Exception
    * @ejb.create-method
    */
   public void ejbCreate() throws CreateException
   {
   }

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
      this.ctx = ctx;
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   public void ejbActivate() throws EJBException, RemoteException
   {
   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }
}
