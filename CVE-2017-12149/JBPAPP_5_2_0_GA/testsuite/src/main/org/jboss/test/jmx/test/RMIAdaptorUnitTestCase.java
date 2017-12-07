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
package org.jboss.test.jmx.test;

import java.util.Iterator;

import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.test.JBossTestCase;

/** 
 * Tests over the RMIAdaptor
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version <tt>$Revision: 62212 $</tt>
 */
public class RMIAdaptorUnitTestCase extends JBossTestCase
{
   public RMIAdaptorUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Test that we can iterate and retrieve MBeanInfo
    * for all registered MBeans
    * 
    * @throws Exception
    */
   public void testMBeanInfoMarshalling() throws Exception
   {
      getLog().debug("+++ testMBeanInfoMarshalling");
      
      initURLHandlers();
      
      InitialContext ctx = getInitialContext();
      RMIAdaptor rmiAdaptor = (RMIAdaptor)ctx.lookup("jmx/invoker/RMIAdaptor");
      Iterator it = rmiAdaptor.queryNames(null, null).iterator();

      while (it.hasNext())   
      {
         ObjectName objectName = (ObjectName)it.next();
         try
         {
            MBeanInfo mbeanInfo = rmiAdaptor.getMBeanInfo(objectName);
         }
         catch (Throwable t)
         {
            getLog().error("Caught exception getting MBeanInfo for: " + objectName, t);
            super.fail("Failed to get MBeanInfo for bean named: " + objectName);
         }
      }      
   }

   /**
    * This is need to setup the "resource" protocol handler, to cater
    * for the ConfigurationURL attribute with default value "resource:log4j.xml" 
    * of jboss.system:service=Logging,type=Log4jService 
    */
   private void initURLHandlers()
   {
      // Include the default JBoss protocol handler package
      String handlerPkgs = System.getProperty("java.protocol.handler.pkgs");
      if (handlerPkgs != null)
      {
         handlerPkgs += "|org.jboss.net.protocol";
      }
      else
      {
         handlerPkgs = "org.jboss.net.protocol";
      }
      System.setProperty("java.protocol.handler.pkgs", handlerPkgs);
   }   
}
