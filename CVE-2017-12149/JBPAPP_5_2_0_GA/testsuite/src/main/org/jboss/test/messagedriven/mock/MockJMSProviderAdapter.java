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
package org.jboss.test.messagedriven.mock;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.jms.jndi.JMSProviderAdapter;
import org.w3c.dom.Element;

/**
 * A MockJMSProviderAdapter.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 81084 $
 */
public class MockJMSProviderAdapter implements JMSProviderAdapter, JmsMockObject
{
   //TODO create mock provider adapter xml file.
   public String getFactoryRef()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Context getInitialContext() throws NamingException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Properties getProperties()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getQueueFactoryRef()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getTopicFactoryRef()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setFactoryRef(String newFactoryRef)
   {
      // TODO Auto-generated method stub

   }

   public void setName(String name)
   {
      // TODO Auto-generated method stub

   }

   public void setProperties(Properties properties)
   {
      // TODO Auto-generated method stub

   }

   public void setQueueFactoryRef(String newQueueFactoryRef)
   {
      // TODO Auto-generated method stub

   }

   public void setTopicFactoryRef(String newTopicFactoryRef)
   {
      // TODO Auto-generated method stub

   }

   public void load(String xml)
   {
      // TODO Auto-generated method stub
      
   }

   public void load(Element xml)
   {
      // TODO Auto-generated method stub
      
   }

   public void load()
   {
      // TODO Auto-generated method stub
      
   }

}
