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

import com.cluster.simple.sessionbeans.SimpleSession;
import com.cluster.simple.sessionbeans.SimpleSessionHome;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 * @version $Revision: 81036 $
 */
public class TestClient
{
   
   public static String SIMPLE_CONFIG_SERVER_PROP = "services.properties";
   SimpleSessionHome simHome;
   SimpleSession simBean;

   public static void main(String args[])
   {
      try
      {
         TestClient client = new TestClient();
         if (args.length > 0)
         {
            System.out.println(client.execute(args[0]));
         }
         else
         {
            System.out.println(client.execute(""));
         }
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   public String execute(String obj) throws Exception
   {
      String res = null;
      String xmlString = (String) obj;
      try
      {
         init(obj);
         res = simBean.processRequest(xmlString);
      }
      catch (Exception ex)
      {
         res = ex.getMessage();
         throw new Exception(ex.getMessage());
      }
      return res;
   }

   public void init(String obj) throws Exception
   {
      try
      {
         simHome = (SimpleSessionHome)
               lookup(obj + "SimpleSession", SimpleSessionHome.class);
         simBean = simHome.create();
      }
      catch (CreateException ex)
      {
         throw new Exception(ex.getMessage());
      }
   }

   public Properties getPropAsResource(String name) throws Exception
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

   public Object lookup(String name, Class className) throws Exception
   {
      Object retVal = null;
      try
      {
         InitialContext jndiContext = new InitialContext(getPropAsResource(SIMPLE_CONFIG_SERVER_PROP));
         Object ref = jndiContext.lookup(name);
         retVal = PortableRemoteObject.narrow(ref, className);
      }
      catch (NamingException ex)
      {
         ex.printStackTrace();
         throw new Exception("Object is not Bound in the Context : " + name);
      }
      return retVal;
   }


}
