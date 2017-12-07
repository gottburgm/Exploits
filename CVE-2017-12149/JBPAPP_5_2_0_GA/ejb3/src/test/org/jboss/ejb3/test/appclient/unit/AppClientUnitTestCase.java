/*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.ejb3.test.appclient.unit;

import java.net.URL;
import java.util.Date;

import junit.framework.Test;

import org.jboss.ejb3.client.ClientLauncher;
import org.jboss.ejb3.metamodel.ApplicationClientDD;
import org.jboss.ejb3.test.appclient.client.HelloWorldClient;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.test.JBossTestCase;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class AppClientUnitTestCase extends JBossTestCase
{
   public AppClientUnitTestCase(String name)
   {
      super(name);
   }

   public void test1() throws Throwable
   {
      String mainClassName = HelloWorldClient.class.getName();
      String applicationClientName = "applicationclient_test"; // must match JNDI name in jboss-client.xml or display-name in application-client.xml
      String name = new Date().toString();
      String args[] = { name };
      
      ClientLauncher launcher = new ClientLauncher();
      launcher.launch(mainClassName, applicationClientName, args);
      
      {
         String actual = HelloWorldClient.getResult();
         String expected = "Hi " + name + ", how are you?";
         assertEquals(expected, actual);
      }
      
      {
         int actual = HelloWorldClient.getPostConstructCalls();
         int expected = 1;
         assertEquals("postConstruct should be called once", expected, actual);
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(AppClientUnitTestCase.class, "appclient-jms-service.xml,appclient-test.ear");
      //return getDeploySetup(AppClientUnitTestCase.class, "appclient-test.jar,appclient-test-client.jar");
   }
}
