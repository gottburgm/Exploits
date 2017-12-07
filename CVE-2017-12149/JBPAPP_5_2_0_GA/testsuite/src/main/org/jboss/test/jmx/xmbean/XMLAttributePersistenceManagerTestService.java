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
package org.jboss.test.jmx.xmbean;

import java.io.File;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.mx.persistence.AttributePersistenceManager;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.pm.XMLAttributePersistenceManager;
import org.jboss.system.server.ServerConfigLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A test service that wraps an XMLAttributePersistenceManager
 * configured to write to a random directory containing a space
 * in its name, e.g. "./tmp/XmlApmXXXXXTest .dir"
 * 
 * @see org.jboss.test.jmx.test.MLAttributePersistenceManagerUnitTestCase
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class XMLAttributePersistenceManagerTestService
   extends ServiceMBeanSupport
{
   private AttributePersistenceManager apm;
   private File storeDir;
   
   protected void startService()
      throws Exception
   {
      File tmpDir = ServerConfigLocator.locate().getServerTempDir();
      boolean result;
      
      // Get a temporary file in the server tmp dir, with a space in its name
      storeDir = File.createTempFile("XmlApm", "Test .dir", tmpDir);
      
      // Remove the tmp file
      result = storeDir.delete();

      // Recreate it as directory
      result = storeDir.mkdir();
      log.info("Created 'bad' store dir: " + storeDir + ", " + result);
      
      String dirURL = storeDir.toURL().toString();
      log.info("Dir URL: " + dirURL);      
      
      apm = new XMLAttributePersistenceManager();

      // Initialize an XMLAttributePeristenceManager and
      // configure it to point to the "bad" directory
      apm.create(null, prepareConfig(dirURL));
   }
   
   protected void stopService()
      throws Exception
   {
      if (apm != null)
      {
         apm.removeAll();
         apm.destroy();
         log.info("Destroyed AttributePersistenceManager");
      }
      if (storeDir != null)
      {
         boolean result = storeDir.delete();
         log.info("Removed: " +  storeDir + ", " + result);
      }
   }
   
   public void store(String id, AttributeList atlist) throws Exception
   {
      apm.store(id, atlist);
   }
   
   public AttributeList load(String id) throws Exception
   {
      return apm.load(id);
   }
   
   public void selftest() throws Exception
   {
      // Store some attributes under an id
      AttributeList alist = new AttributeList();
      String storeId = "bananarama";
      
      Integer anInteger = new Integer(666);
      String aString = new String("Evil Test");
      alist.add(new Attribute("Attr1", anInteger));
      alist.add(new Attribute("Attr2", aString));
      apm.store(storeId, alist);
      
      // Read them back
      AttributeList alist2 = apm.load(storeId);      
   }
   
   private Element prepareConfig(String dir) throws Exception
   {
      // build the config XML Element in memory using DOM
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = builder.newDocument();

      // Create config element
      Element config = doc.createElement(XMLAttributePersistenceManager.DATA_DIR_ELEMENT);
      
      // Insert a text node with the directory name
      Node text = doc.createTextNode(dir);
      
      config.appendChild(text);
      
      // Return the config
      return config;
   }   
}
