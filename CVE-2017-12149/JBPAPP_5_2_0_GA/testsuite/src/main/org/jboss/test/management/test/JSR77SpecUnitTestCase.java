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
package org.jboss.test.management.test;

import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.j2ee.ListenerRegistration;
import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;
import javax.management.j2ee.statistics.StatelessSessionBeanStats;
import javax.management.j2ee.statistics.Statistic;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.management.j2ee.J2EEManagedObject;
import org.jboss.management.j2ee.J2EETypeConstants;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Test of JSR-77 specification conformance using the Management interface.
 * These test the basic JSR-77 handling and access.
 *
 * @author Andreas Schaefer
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105907 $
 */
public class JSR77SpecUnitTestCase
   extends JBossTestCase
{
   // Constants -----------------------------------------------------
   public static final String TEST_DATASOURCE = "DefaultDS";
   public static final String TEST_MAIL = "DefaultMail";
   private String jsr77Domain;

   // Constructors --------------------------------------------------

   public JSR77SpecUnitTestCase(String name)
   {
      super(name);
   }

   // Public --------------------------------------------------------
   
   /** Test that the JSR77 MEJB is available
    * @throws Exception
    */
   public void testConnect() throws Exception
   {
      log.debug("+++ testConnect");
      Management jsr77MEJB = getManagementEJB();
      String lDomain = jsr77MEJB.getDefaultDomain();
      log.debug("+++ testConnect, domain: " + lDomain);
      jsr77MEJB.remove();
   }

   /** Test the JSR-77 J2EEDomain availability
    * @throws Exception
    */
   public void testJ2EEDomain()
      throws
      Exception
   {
      getLog().debug("+++ testJ2EEDomain");
      Management jsr77MEJB = getManagementEJB();
      String domain = jsr77MEJB.getDefaultDomain();
      log.debug("domain=("+domain+")");
      String nameStr = domain
         + ":" + J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEDomain
         + ",name="+domain;
      log.debug("nameStr=("+nameStr+")");
      ObjectName domainQuery = new ObjectName(nameStr);
      log.debug("domainQuery=("+domainQuery+")");
      Set names = jsr77MEJB.queryNames(domainQuery, null);
      if (names.isEmpty())
      {
         fail("Could not find JSR-77 J2EEDomain '" + J2EETypeConstants.J2EEDomain + "'");
      }
      if (names.size() > 1)
      {
         fail("Found more than one JSR-77 J2EEDomain '" + J2EETypeConstants.J2EEDomain + "'");
      }
      ObjectName jsr77MEJBDomain = (ObjectName) names.iterator().next();
      getLog().debug("+++ testJ2EEDomain, root: " + jsr77MEJBDomain);
      jsr77MEJB.remove();
   }

   /** Test the JSR-77 J2EEServer availability
    * @throws Exception
    */
   public void testJ2EEServer() throws Exception
   {
      getLog().debug("+++ testJ2EEServer");
      Management jsr77MEJB = getManagementEJB();
      String domainName = jsr77MEJB.getDefaultDomain();
      ObjectName queryName = new ObjectName(domainName + ":" +
         J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEServer + "," + "*");

      Set names = jsr77MEJB.queryNames(queryName, null);
      if (names.isEmpty())
      {
         fail("Could not find JSR-77 J2EEServer '" + J2EETypeConstants.J2EEServer + "'");
      }
      Iterator iter = names.iterator();
      ObjectName serverName = null;
      while (iter.hasNext())
      {
         serverName = (ObjectName) iter.next();
         getLog().debug("J2EEServer: " + serverName);
      }

      // Get the server info
      String vendor = (String) jsr77MEJB.getAttribute(serverName, "serverVendor");
      getLog().debug("ServerVendor: " + vendor);
      String version = (String) jsr77MEJB.getAttribute(serverName, "serverVersion");
      getLog().debug("ServerVersion: " + version);

      // Get the list of JVMs
      String[] jvms = (String[]) jsr77MEJB.getAttribute(serverName, "javaVMs");
      if (jvms == null || jvms.length == 0)
         fail("Failed to find any JavaVMs");
      ObjectName jvm = new ObjectName(jvms[0]);
      getLog().debug("JavaVMs[0]: " + jvms[0]);
      String javaVendor = (String) jsr77MEJB.getAttribute(jvm, "javaVendor");
      getLog().debug("JavaVendor: " + javaVendor);
      String javaVersion = (String) jsr77MEJB.getAttribute(jvm, "javaVersion");
      getLog().debug("JavaVersion: " + javaVersion);
      String node = (String) jsr77MEJB.getAttribute(jvm, "node");
      getLog().debug("Node: " + node);

      jsr77MEJB.remove();
   }

   /** Test the JSR-77 JNDIResource availability
    * @throws Exception
    */
   public void testJNDIResource() throws Exception
   {
      getLog().debug("+++ testJNDIResource");
      Management jsr77MEJB = getManagementEJB();
      String domainName = jsr77MEJB.getDefaultDomain();
      ObjectName queryName = new ObjectName(domainName + ":" + J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JNDIResource + "," + "*");
      // TODO - this used to work, Scott's new naming required?
/*
      Set names = jsr77MEJB.queryNames(queryName, null);
      if (names.isEmpty())
      {
         fail("Could not find JSR-77 JNDIResource '" + J2EETypeConstants.JNDIResource + "'");
      }
      Iterator iter = names.iterator();
      while (iter.hasNext())
         getLog().debug("JNDIResource: " + iter.next());
*/
      jsr77MEJB.remove();
   }

   /** Test JavaMailResource availability.
    * @throws Exception
    */
   public void testJavaMailResource() throws Exception
   {
      getLog().debug("+++ testJavaMailResource");
      Management jsr77MEJB = getManagementEJB();
      String domainName = jsr77MEJB.getDefaultDomain();
      ObjectName queryName = new ObjectName(domainName + ":" +
         J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JavaMailResource + "," + "*");
      Set names = jsr77MEJB.queryNames(queryName, null);
      if (names.isEmpty())
      {
         fail("Could not find JSR-77 JavaMailResource '" + J2EETypeConstants.JavaMailResource + "'");
      }
      Iterator iter = names.iterator();
      while (iter.hasNext())
         getLog().debug("JavaMailResource: " + iter.next());
      jsr77MEJB.remove();
   }

// NYI - see JBAS-5545
//
//   /** Test JCAResource availability.
//    * @throws Exception
//    */
//   public void testJCAResource() throws Exception
//   {
//      getLog().debug("+++ testJCAResource");
//      Management jsr77MEJB = getManagementEJB();
//      String domainName = jsr77MEJB.getDefaultDomain();
//      ObjectName queryName = new ObjectName(domainName + ":" +
//         J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JCAResource + "," + "*");
//      Set names = jsr77MEJB.queryNames(queryName, null);
//      if (names.isEmpty())
//      {
//         fail("Could not find JSR-77 JCAResource '" + J2EETypeConstants.JCAResource + "'");
//      }
//      Iterator iter = names.iterator();
//      while (iter.hasNext())
//         getLog().debug("JCAResource: " + iter.next());
//      jsr77MEJB.remove();
//   }

   /** Test JTAResource availability.
    * @throws Exception
    */
   public void testJTAResource() throws Exception
   {
      getLog().debug("+++ testJTAResource");
      Management jsr77MEJB = getManagementEJB();
      String domainName = jsr77MEJB.getDefaultDomain();
      ObjectName queryName = new ObjectName(domainName + ":" +
         J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JTAResource + "," + "*");
      Set names = jsr77MEJB.queryNames(queryName, null);
      if (names.isEmpty())
      {
         fail("Could not find JSR-77 JTAResource '" + J2EETypeConstants.JTAResource + "'");
      }
      Iterator iter = names.iterator();
      while (iter.hasNext())
         getLog().debug("JTAResource: " + iter.next());
      jsr77MEJB.remove();
   }

   /** Test JMSResource availability.
    * @throws Exception
    */
   public void testJMSResource() throws Exception
   {
      getLog().debug("+++ testJMSResource");
      Management jsr77MEJB = getManagementEJB();
      String domainName = jsr77MEJB.getDefaultDomain();
      ObjectName queryName = new ObjectName(domainName + ":" +
         J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JMSResource + "," + "*");
      Set names = jsr77MEJB.queryNames(queryName, null);
      if (names.isEmpty())
      {
         fail("Could not find JSR-77 JMSResource '" + J2EETypeConstants.JMSResource + "'");
      }
      Iterator iter = names.iterator();
      while (iter.hasNext())
         getLog().debug("JMSResource: " + iter.next());
      jsr77MEJB.remove();
   }

// NYI - see JBAS-5545
//
//   /** Test the default JCAConnectionFactory availability.
//    * @throws Exception
//    */
//   public void testJCAConnectionFactory()
//      throws
//      Exception
//   {
//      getLog().debug("+++ testJCAConnectionFactory");
//      Management jsr77MEJB = getManagementEJB();
//      Set names = jsr77MEJB.queryNames(
//         getConnectionFactoryName(jsr77MEJB),
//         null
//      );
//      if (names.isEmpty())
//      {
//         fail("Could not found JSR-77 JCAConnectionFactory named '"
//            + TEST_DATASOURCE + "'");
//      }
//      if (names.size() > 1)
//      {
//         fail("Found more than one JSR-77 JCAConnectionFactory named '"
//            + TEST_DATASOURCE + "'");
//      }
//      ObjectName factory = (ObjectName) names.iterator().next();
//      getLog().debug("+++ testJCAConnectionFactory, " + TEST_DATASOURCE
//         + ": " + factory);
//      jsr77MEJB.remove();
//   }

// NYI - see JBAS-5545
//
   /** Test EJBModule for the ejb-management.jar
    * @throws Exception
    */
   public void testEJBModule() throws Exception
   {
      getLog().debug("+++ testEJBModule");
      Management jsr77MEJB = getManagementEJB();
      String domainName = jsr77MEJB.getDefaultDomain();
      ObjectName mejbModuleName = new ObjectName(domainName + ":" +
         "J2EEServer=Local,J2EEApplication=null,"
         + J2EEManagedObject.TYPE + "=" + J2EETypeConstants.EJBModule
         + ",name=ejb-management.jar");
      boolean isRegistered = jsr77MEJB.isRegistered(mejbModuleName);
      assertTrue(mejbModuleName + " is not registered", isRegistered);
      String[] ejbs = (String[]) jsr77MEJB.getAttribute(mejbModuleName, "ejbs");
      // TODO assertTrue("ejb-management.jar.Ejbs.length == 0", ejbs.length > 0);
      for (int n = 0; n < ejbs.length; n++)
      {
         ObjectName ejb = new ObjectName(ejbs[n]);
         getLog().debug("Ejbs[" + n + "]=" + ejb);
         StatelessSessionBeanStats stats = (StatelessSessionBeanStats)
            jsr77MEJB.getAttribute(ejb, "stats");
         String[] statNames = stats.getStatisticNames();
         for (int s = 0; s < statNames.length; s++)
         {
            Statistic theStat = stats.getStatistic(statNames[s]);
            getLog().debug(theStat);
         }
      }
      jsr77MEJB.remove();
   }

   /**
    * Query for the *:j2eeType=WebModule,* bean attributes. Assert each has:
    * - objectName
    * - stateManageable
    * - statisticsProvider
    * - eventProvider
    * - deploymentDescriptor
    * - servlets ; may be null
    * - javaVMs
    * - server
    */
   public void testWebModules()
      throws Exception
   {
      String[] attributes = {"deploymentDescriptor",
            "objectName",
            "servlets",
            "statisticsProvider",
            "javaVMs",
            "stateManageable",
            "eventProvider",
            "server"
      };
      getLog().debug("+++ testWebModules");
      Management jsr77MEJB = getManagementEJB();
      String domainName = jsr77MEJB.getDefaultDomain();
      ObjectName webModules = new ObjectName(domainName+":j2eeType=WebModule,*");
      Set<ObjectName> names = jsr77MEJB.queryNames(webModules, null);
      assertTrue("", names.size() > 0);
      getLog().debug("Found web modules: "+names);
      boolean sawNullAttributes = false;
      for(ObjectName name : names)
      {
         getLog().debug("Checking module: "+name);
         for(String attrName : attributes)
         {
            Object attr = jsr77MEJB.getAttribute(name, attrName);
            if(attr == null)
            {
               getLog().error(name+" has null attribute: "+attrName);
               sawNullAttributes = true;
            }
         }
      }
      assertFalse("All web modules had non-null attributes", sawNullAttributes);
   }

   /** A test of accessing all StatelessSessionBean stats
    * @throws Exception
    */
   public void testEJBStats() throws Exception
   {
      getLog().debug("+++ testEJBStats");
      Management jsr77MEJB = getManagementEJB();
      String beanName = null;
      String query = "*:j2eeType=StatelessSessionBean,*";
      log.info(query);
      ObjectName ejbName = new ObjectName(query);
      Set managedObjects = jsr77MEJB.queryNames(ejbName,  null);
      log.info("Found " + managedObjects.size() + " objects");
      Iterator i = managedObjects.iterator();
      while (i.hasNext())
      {
         ObjectName oName = (ObjectName) i.next();
         beanName = oName.getKeyProperty("name");
         StatelessSessionBeanStats stats =
            (StatelessSessionBeanStats) jsr77MEJB.getAttribute(oName,
               "stats");
         Statistic[] allStats = stats.getStatistics();
         for (int s = 0; s < allStats.length; s++)
         {
            Statistic theStat = allStats[s];
            getLog().debug(theStat);
         }
      }
      jsr77MEJB.remove();
   }

// NYI - see JBAS-5545
//
   /** Test WebModule for the jmx-console.war
    * @throws Exception
    */
   public void testWebModule() throws Exception
   {
      getLog().debug("+++ testWebModule");
      Management jsr77MEJB = getManagementEJB();
      String domainName = jsr77MEJB.getDefaultDomain();
      ObjectName webModuleName = new ObjectName(domainName + ":" +
         "J2EEServer=Local,J2EEApplication=null,"
         + J2EEManagedObject.TYPE + "=" + J2EETypeConstants.WebModule
         + ",name=jmx-console.war");
      boolean isRegistered = jsr77MEJB.isRegistered(webModuleName);
      assertTrue(webModuleName + " is not registered", isRegistered);
      String[] servlets = (String[]) jsr77MEJB.getAttribute(webModuleName, "servlets");
      // TODO assertTrue("jmx-console.war.Servlets.length == 0", servlets.length > 0);
      for (int n = 0; n < servlets.length; n++)
         getLog().debug("Servlets[" + n + "]=" + servlets[n]);
      jsr77MEJB.remove();
   }

// NYI - see JBAS-5545
//
   /** Test ResourceAdapterModule for the jboss-local-jdbc.rar
    * @throws Exception
    */
   public void testResourceAdapterModule() throws Exception
   {
      getLog().debug("+++ testResourceAdapterModule");
      Management jsr77MEJB = getManagementEJB();
      String domainName = jsr77MEJB.getDefaultDomain();
      ObjectName rarModuleName = new ObjectName(domainName + ":" +
         "J2EEServer=Local,J2EEApplication=null,"
         + J2EEManagedObject.TYPE + "=" + J2EETypeConstants.ResourceAdapterModule
         + ",name=jboss-local-jdbc.rar");
      boolean isRegistered = jsr77MEJB.isRegistered(rarModuleName);
      assertTrue(rarModuleName + " is not registered", isRegistered);
      String[] ras = (String[]) jsr77MEJB.getAttribute(rarModuleName, "resourceAdapters");
      // TODO assertTrue("jboss-local-jdbc.rar.ResourceAdapters.length == 0", ras.length > 0);
      for (int n = 0; n < ras.length; n++)
         getLog().debug("ResourceAdapters[" + n + "]=" + ras[n]);
      jsr77MEJB.remove();
   }

   /**
    * Test the notification delivery by restarting Default DataSource
    */
   public void testNotificationDeliver()
      throws Exception
   {
      try
      {
         getLog().debug("+++ testNotificationDeliver");
         Management jsr77MEJB = getManagementEJB();
         Set names = jsr77MEJB.queryNames(getMailName(jsr77MEJB), null);
         if (names.isEmpty())
         {
            fail("Could not found JSR-77 JavaMailResource'" + TEST_MAIL + "'");
         }
         ObjectName lMail = (ObjectName) names.iterator().next();
         Listener lLocalListener = new Listener();
         ListenerRegistration lListenerFactory = jsr77MEJB.getListenerRegistry();
         getLog().debug("+++ testNotificationDeliver, add Notification Listener to " + TEST_MAIL +
            " with Listener Registry: " + lListenerFactory);
         lListenerFactory.addNotificationListener(
            lMail,
            lLocalListener,
            null,
            null
         );
         getLog().debug("+++ testNotificationDeliver, stop " + TEST_MAIL + "");
         jsr77MEJB.invoke(lMail, "stop", new Object[]{}, new String[]{});
         getLog().debug("+++ testNotificationDeliver, start " + TEST_MAIL + "");
         jsr77MEJB.invoke(lMail, "start", new Object[]{}, new String[]{});
         // Wait 5 seconds to ensure that the notifications are delivered
         Thread.sleep(5000);
         if (lLocalListener.getNumberOfNotifications() < 2)
         {
            fail("Not enough notifications received: " + lLocalListener.getNumberOfNotifications());
         }
         getLog().debug("+++ testNotificationDeliver, remove Notification Listener from " + TEST_MAIL + "");
         lListenerFactory.removeNotificationListener(
            lMail,
            lLocalListener
         );
         jsr77MEJB.remove();
      }
      catch (Exception e)
      {
         log.debug("failed", e);
         throw e;
      }
   }

   /**
    * Test the Navigation through the current JSR-77 tree
    */
   public void testNavigation()
      throws Exception
   {
      log.info("+++ testNavigation");
      Management jsr77MEJB = null;
      try
      {
         // Get Management EJB and then the management domain
         jsr77MEJB = getManagementEJB();
         String domain = jsr77MEJB.getDefaultDomain();
         ObjectName domainQuery = new ObjectName(domain
            + ":" + J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEDomain
            + ",name="+domain);
         Set names = jsr77MEJB.queryNames(domainQuery, null);

         if (names.isEmpty())
         {
            fail("Could not find any J2EEDomain");
         }
         if (names.size() > 1)
         {
            fail("Found more than one J2EEDomain, "+names);
         }

         ObjectName jsr77MEJBDomain = (ObjectName) names.iterator().next();
         log.info("J2EEDomain: "+jsr77MEJBDomain);
         jsr77Domain = jsr77MEJBDomain.getDomain();
         // Report the attributes and references
         report(jsr77MEJB, jsr77MEJBDomain, new HashSet());
      }
      catch (Exception e)
      {
         log.debug("failed", e);
         throw e;
      }
      catch (Error err)
      {
         log.debug("failed", err);
         throw err;
      }
      finally
      {
         if (jsr77MEJB != null)
         {
            jsr77MEJB.remove();
         }
      }
   }

   private void report(Management jsr77EJB, ObjectName mbean, HashSet reportedNames)
      throws JMException,
      RemoteException
   {
      if (mbean == null)
         return;
      
      if (reportedNames.contains(mbean))
      {
         log.debug("Skipping already reported MBean: " + mbean);
         return;
      }

      log.debug("Begin Report Object: " + mbean);
      reportedNames.add(mbean);
      MBeanInfo mbeanInfo = jsr77EJB.getMBeanInfo(mbean);
      MBeanAttributeInfo[] attrInfo = mbeanInfo.getAttributes();
      String[] attrNames = new String[attrInfo.length];
      // First just report all attribute names and types
      for (int i = 0; i < attrInfo.length; i++)
      {
         String name = attrInfo[i].getName();
         String type = attrInfo[i].getType();
         boolean readable = attrInfo[i].isReadable();
         log.debug("Attribute: " + name + ", " + type + ", readable: " + readable);
         attrNames[i] = attrInfo[i].getName();
      }

      // Now try to obtain the values
      for (int i = 0; i < attrNames.length; i++)
      {
         String name = attrNames[i];
         Object value = null;
         try
         {
            if (attrInfo[i].isReadable() == true)
               value = jsr77EJB.getAttribute(mbean, name);
         }
         catch (UndeclaredThrowableException e)
         {
            Throwable ex = e.getUndeclaredThrowable();
            log.debug("Failed to access attribute: " + name + ", " + ex.getMessage());
         }
         catch (Exception e)
         {
            // HACK: Ignore moved attribute error for message cache on the persistence manager
            if (name.equals("MessageCache"))
               continue;

            /* This is not a fatal exception as not all attributes are remotable
            but all javax.management.* and org.jboss.management.j2ee.* types
            should be.
            */
            log.debug("Failed to access attribute: " + name, e);
            String type = attrInfo[i].getType();
            boolean isJSR77Type = type.startsWith("javax.management") ||
               type.startsWith("org.jboss.management.j2ee");
            assertTrue("Bad attribute(" + name + ") is not a JSR77 type", isJSR77Type == false);
         }

         if (value == null)
         {
            log.debug("Attribute: " + name + " is empty");
         }
         else if (ObjectName.class.getName().equals(attrInfo[i].getType()))
         {
            // Check if this attribute should not be followed
            ObjectName toName = (ObjectName) value;
            if (checkBlock(toName, name))
            {
               log.debug("Blocked Attribute: " + name + " contains: " + toName);
               continue;
            }
            // Report this Object's attribute first
            log.debug("Attribute: " + name + ", value: " + value + ", is reported");
            report(jsr77EJB, (ObjectName) value, reportedNames);
         }
         else if (ObjectName[].class.getName().equals(attrInfo[i].getType()))
         {
            ObjectName[] names = (ObjectName[]) value;
            for (int j = 0; j < names.length; j++)
            {
               ObjectName toName = names[j];
               // Check if this name should not be followed
               if (checkBlock(toName, name))
               {
                  log.debug("Blocked ObjectName: " + toName);
                  continue;
               }
               log.debug("Attribute: " + name + ", value: " + toName + ", is reported");
               report(jsr77EJB, toName, reportedNames);
            }
         }
         else
         {
            log.debug("Attribute: " + name + " contains: " + value);
         }
      }
      log.debug("End Report Object: " + mbean);
   }

   /**
    * @return True if the given attribute must be blocked to avoid
    *         an endless loop in the graph of JSR-77 object name
    *         references (like J2EEServer refences J2EEDeployedObjects
    *         and this references J2EEServer)
    */
   private boolean checkBlock(ObjectName name, String attrName)
   {
      // If the mbean is not a jsr77 mean ignore the attribute
      String domain = name == null ? "" : name.getDomain();
      if( domain.equals(this.jsr77Domain) == false )
         return true;
      
      String type = (String) name.getKeyPropertyList().get(J2EEManagedObject.TYPE);
      if (J2EETypeConstants.EJBModule.equals(type) ||
         J2EETypeConstants.WebModule.equals(type) ||
         J2EETypeConstants.ResourceAdapterModule.equals(type) ||
         J2EETypeConstants.ServiceModule.equals(type))
      {
         if ("Server".equals(attrName))
         {
            // Block Attribute Server for any J2EE Deployed Objects
            return true;
         }
      }
      return "Parent".equals(attrName) ||
         "ObjectName".equals(attrName);
   }

   private Management getManagementEJB()
      throws
      Exception
   {
      getLog().debug("+++ getManagementEJB()");
      Object lObject = getInitialContext().lookup("ejb/mgmt/MEJB");
      ManagementHome home = (ManagementHome) PortableRemoteObject.narrow(
         lObject,
         ManagementHome.class
      );
      getLog().debug("Found JSR-77 Management EJB (MEJB)");
      return home.create();
   }

   private ObjectName getConnectionFactoryName(Management jsr77MEJB) throws Exception
   {
      String domainName = jsr77MEJB.getDefaultDomain();
      return new ObjectName(domainName + ":" +
         J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JCAConnectionFactory + "," +
         "name=" + TEST_DATASOURCE + "," +
         "*"
      );
   }

   private ObjectName getMailName(Management jsr77MEJB) throws Exception
   {
      String domainName = jsr77MEJB.getDefaultDomain();
      return new ObjectName(domainName + ":" +
         J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JavaMailResource + "," +
         "*"
      );
   }
   // Inner classes -------------------------------------------------

   private class Listener implements NotificationListener
   {

      private int mNrOfNotifications = 0;

      public int getNumberOfNotifications()
      {
         return mNrOfNotifications;
      }

      public void handleNotification(Notification pNotification, Object pHandbank)
      {
         mNrOfNotifications++;
      }
   }

   public static Test suite() throws Exception
   {
       
       TestSuite suite = new TestSuite();
       
       suite.addTest(new JSR77SpecUnitTestCase("testConnect"));
       suite.addTest(new JSR77SpecUnitTestCase("testJ2EEDomain"));
       suite.addTest(new JSR77SpecUnitTestCase("testJ2EEServer"));
       suite.addTest(new JSR77SpecUnitTestCase("testJNDIResource"));
       suite.addTest(new JSR77SpecUnitTestCase("testJavaMailResource"));
       suite.addTest(new JSR77SpecUnitTestCase("testJTAResource"));
       if (JMSDestinationsUtil.isJBM())
       {
           // This test is only supported on JBM
           suite.addTest(new JSR77SpecUnitTestCase("testJMSResource"));
       }
       suite.addTest(new JSR77SpecUnitTestCase("testEJBModule"));
       suite.addTest(new JSR77SpecUnitTestCase("testWebModules"));
       suite.addTest(new JSR77SpecUnitTestCase("testEJBStats"));
       suite.addTest(new JSR77SpecUnitTestCase("testWebModule"));
       suite.addTest(new JSR77SpecUnitTestCase("testResourceAdapterModule"));
       suite.addTest(new JSR77SpecUnitTestCase("testNotificationDeliver"));
       suite.addTest(new JSR77SpecUnitTestCase("testNavigation"));

      return getDeploySetup(suite, "ejb-management.jar");
   }

}
