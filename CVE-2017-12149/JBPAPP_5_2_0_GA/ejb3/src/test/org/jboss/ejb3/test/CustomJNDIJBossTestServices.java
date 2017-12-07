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
package org.jboss.ejb3.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.InitialContext;

import org.jboss.test.JBossTestServices;

/**
 * This class provides services for JBoss unit tests.
 * If found it uses custom.jndi.properties to establish a connection with
 * the Main Deployer.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
public class CustomJNDIJBossTestServices extends JBossTestServices
{

   public CustomJNDIJBossTestServices(Class clazz)
   {
      super(clazz);
   }
   public CustomJNDIJBossTestServices(String className)
   {
      super(className);
   }

   protected Properties getCustomJNDIProperties() throws IOException
   {
      InputStream in = getClass().getClassLoader().getResourceAsStream("custom.jndi.properties");
      if(in == null)
      {
         log.debug("no custom.jndi.properties found using defaults");
         return null;
      }
      Properties props = new Properties();
      props.load(in);
      in.close();
      return props;
   }
   
   @Override
   public void init() throws Exception
   {
      if(initialContext == null)
      {
         initialContext = new InitialContext(getCustomJNDIProperties());
         log.debug("initialContext.getEnvironment()=" + initialContext.getEnvironment());
         jndiEnv = initialContext.getEnvironment();
         
         log.debug("server = " + getServer());
      }
   }
      
   protected boolean isSecureTest()
   {
      return Boolean.getBoolean("jbosstest.secure");
   }
}
