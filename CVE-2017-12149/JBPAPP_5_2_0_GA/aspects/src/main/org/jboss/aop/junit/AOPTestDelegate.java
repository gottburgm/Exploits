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
package org.jboss.aop.junit;

import java.net.URL;
import java.util.Iterator;

import org.jboss.aop.AspectXmlLoader;
import org.jboss.test.AbstractTestDelegate;

import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArrayList;

/**
 * AOPTestDelegate.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80997 $
 */
public class AOPTestDelegate extends AbstractTestDelegate
{
   /** The deployed urls */
   private static final CopyOnWriteArrayList urls = new CopyOnWriteArrayList();
   
   /**
    * Create a new AOPTestDelegate.
    * 
    * @param clazz the test class
    * @throws Exception for any error
    */
   public AOPTestDelegate(Class clazz) throws Exception
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
      testName = testName.replace('.', '/') + "-aop.xml";
      URL url = clazz.getClassLoader().getResource(testName);
      if (url != null)
         deploy(url);
      else
         log.debug("No test specific deployment " + testName);
   }
   
   protected void undeploy()
   {
      for (Iterator i = urls.iterator(); i.hasNext();)
      {
         URL url = (URL) i.next();
         undeploy(url);
      }
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
      urls.add(url);
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
         urls.remove(url);
         AspectXmlLoader.undeployXML(url);
      }
      catch (Exception e)
      {
         log.warn("Ignored error undeploying " + url, e);
      }
   }
}
