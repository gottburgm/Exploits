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
package org.jboss.test.jmx.test;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jmx.xmbean.CustomType;

/**
 * Tests for XMLAttributePersistenceManager
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class XMLAttributePersistenceManagerUnitTestCase extends JBossTestCase
{
   public XMLAttributePersistenceManagerUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * JBAS-3463, persist custom mbean attributes using the default setup
    * of the AttributePersistenceService / XMLAttributePersistenceManager.
    */
   public void testPersistCustomMBeanAttributes() throws Exception
   {
      Logger log = getLog();
      log.info("+++ testPersistCustomMBeanAttributes");
      
      String testService = "xmbean-custom-attr-pers.sar";
      String customMBean = "jboss.test:service=ServiceUsingCustomAttribute";
      
      MBeanServerConnection server = super.getServer();
      // This works when AttributePersistenceService is setup by default
      ObjectName aps = new ObjectName("jboss:service=AttributePersistenceService");
      // Cleanup persisted image
      server.invoke(aps, "apmRemove", new Object[] { customMBean }, new String[] { "java.lang.String" });
      try
      {
         deploy(testService);
         ObjectName target = new ObjectName(customMBean);
         CustomType ct = new CustomType(777, 888);
         // Attribute must be set and persisted
         server.setAttribute(target, new Attribute("Attr", ct));
         // redeploy
         undeploy(testService);
         // this fails if deserialization of the custom attribute fails
         deploy(testService);
         // otherwise we should be aple to read back the persisted attribute
         ct = (CustomType)server.getAttribute(target, "Attr");
         assertTrue("CustomType.x == 777", ct.getX() == 777);
         assertTrue("CustomType.y == 888", ct.getY() == 888);
         // Cleanup persisted image
         server.invoke(aps, "apmRemove", new Object[] { customMBean }, new String[] { "java.lang.String" });          
      }
      catch (Exception e)
      {
         getLog().warn("Caught exception", e);
         fail("Unexcepted Exception, see the Log file");
      }      
      finally
      {
         undeploy(testService);
      }    
   }
   
   /**
    * JBAS-1988, test we can write/read to a directory that contains spaces in its name. 
    * 
    * @see org.jboss.test.jmx.xmbean.XMLAttributePersistenceManagerTestService
    */
   public void testWriteToPathNameContainingSpaces() throws Exception
   {
      Logger log = getLog();
      log.info("+++ testWriteToPathNameContainingSpaces");

      String testService = "xmlapm-xmbean.sar";
      
      try
      {
         deploy(testService);
         
         MBeanServerConnection server = super.getServer();
         ObjectName target = new ObjectName("jboss.test:service=XMLAttributePersistenceManagerTestService");
         
         // Store some attributes under an id
         AttributeList atlist = new AttributeList();
         String storeId = "testId";
         Integer anInteger = new Integer(666);
         String aString = new String("Evil Test");
         atlist.add(new Attribute("Attr1", anInteger));
         atlist.add(new Attribute("Attr2", aString));
       
         getLog().info("Storing AttributeList");
         server.invoke(
               target,
               "store",
               new Object[] { storeId, atlist },
               new String[] { storeId.getClass().getName(), atlist.getClass().getName() }
               );
         
         getLog().info("Loading AttributeList");
         server.invoke(
               target,
               "load",
               new Object[] { storeId },
               new String[] { storeId.getClass().getName() }
               );         
         
      }
      catch (Exception e)
      {
         getLog().warn("Caught exception", e);
         fail("Unexcepted Exception, see the Log file");
      }
      finally
      {
         undeploy(testService);
      }
   }     
}