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
package org.jboss.harness;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.io.InputStream;
import java.util.Properties;

/**
 *  This class is the test client used with the HAJNDITestHarness.
 *  
 * @author <a href="mailto:jgauthier@novell.com">Jerry Gauthier</a>
 * @version $Revision: 81036 $
 */
public class HATestClient
{
   
   public Object lookup(String name, Class className) throws Exception
   {
      try
      {
         InitialContext jndiContext = new InitialContext(getPropAsResource(HAJNDITestHarness.SIMPLE_CONFIG_SERVER_PROP));
         Object ref = jndiContext.lookup(name);
         return PortableRemoteObject.narrow(ref, className);
      }
      catch (NamingException ex)
      {
         throw new Exception("Object is not bound in the context: " + name);
      }
   }
   
   private Properties getPropAsResource(String name) throws Exception
   {
      InputStream is = getClass().getResourceAsStream("/META-INF/" + name);
      if (is == null)
      {
         throw new Exception("Unable to locate resource: " + name);
      }
      Properties confProp = new Properties();
      confProp.load(is);
      return confProp;
   }

}
