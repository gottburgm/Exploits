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
package org.jboss.test.proxyfactory;

import java.net.URL;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.test.AbstractTestDelegate;

/**
 * AbstractProxyTestDelegate.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80997 $
 */
public class AbstractProxyTestDelegate extends AbstractTestDelegate
{
   /**
    * Create a new AbstractProxyTestDelegate.
    * 
    * @param clazz the test class
    * @throws Exception for any error
    */
   public AbstractProxyTestDelegate(Class clazz) throws Exception
   {
      super(clazz);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      
      try
      {
         deploy();
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw e;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable e)
      {
         throw new RuntimeException(e);
      }
   }

   public void tearDown() throws Exception
   {
      super.tearDown();
      undeploy();
   }
   
   /**
    * Deploy the aop config
    * 
    * @throws Exception for any error
    */
   protected void deploy() throws Exception
   {
      String testName = clazz.getName();
      testName = testName.replace('.', '/') + ".xml";
      URL url = clazz.getClassLoader().getResource(testName);
      if (url != null)
         deploy(url);
      else
         throw new RuntimeException("No test specific deployment " + testName);
   }

   /**
    * Undeploy the aop config
    */
   protected void undeploy()
   {
      String testName = clazz.getName();
      testName = testName.replace('.', '/') + ".xml";
      URL url = clazz.getClassLoader().getResource(testName);
      if (url != null)
         undeploy(url);
      else
         log.debug("No test specific deployment " + testName);
   }
   
   /**
    * Get the test url
    * 
    * @return the test url
    */
   protected URL getTestURL()
   {
      String testName = clazz.getName();
      testName = testName.replace('.', '/') + ".xml";
      return clazz.getClassLoader().getResource(testName);
   }
   
   /**
    * Deploy the aop config
    *
    * @param url the url
    * @throws Exception for any error
    */
   protected void deploy(URL url) throws Exception
   {
      log.debug("Deploying " + url);
      AspectXmlLoader.deployXML(url);
   }

   /**
    * Undeploy the aop config
    * 
    * @param url the url
    */
   protected void undeploy(URL url)
   {
      try
      {
         log.debug("Undeploying " + url);
         AspectXmlLoader.undeployXML(url);
      }
      catch (Exception e)
      {
         log.warn("Ignored error undeploying " + url, e);
      }
   }
}
