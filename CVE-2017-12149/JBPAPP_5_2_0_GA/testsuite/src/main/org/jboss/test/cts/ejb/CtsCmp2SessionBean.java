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
package org.jboss.test.cts.ejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import javax.naming.InitialContext;

import org.jboss.test.cts.interfaces.CtsCmp2Local;
import org.jboss.test.cts.interfaces.CtsCmp2LocalHome;
import org.jboss.test.util.ejb.SessionSupport;

/**
 * Class StatelessSessionBean
 *
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class CtsCmp2SessionBean extends SessionSupport
{

   public void ejbCreate ()
   {
   }

   public void testV1() throws RemoteException
   {
      try
      {
         InitialContext ctx = new InitialContext();
         CtsCmp2LocalHome home = (CtsCmp2LocalHome) ctx.lookup("java:comp/env/ejb/CtsCmp2LocalHome");
         CtsCmp2Local bean = home.create("key1", "data1");
         System.out.print("java:comp/env/ejb/CtsCmp2LocalHome bean: "+bean);
         String data = bean.getData();
         System.out.print("Called getData: "+data);
         bean.remove();
         System.out.print("java:comp/env/ejb/CtsCmp2LocalHome passed");
      }
      catch(Exception e)
      {
         throw new ServerException("testV1 failed", e);
      }
   }
   public void testV2() throws RemoteException
   {
      try
      {
         InitialContext ctx = new InitialContext();
         CtsCmp2LocalHome home = (CtsCmp2LocalHome) ctx.lookup("java:comp/env/ejb/CtsCmp2LocalHome");
         CtsCmp2Local bean = home.create("key1", "data1");
         System.out.print("java:comp/env/ejb/CtsCmp2LocalHome bean: "+bean);
         String data = bean.getData();
         Class[] sig = {};
         Method getMoreData = bean.getClass().getMethod("getMoreData", sig);
         Object[] args = {};
         data = (String) getMoreData.invoke(bean, args);
         System.out.print("Called getMoreData: "+data);
         bean.remove();
         System.out.print("java:comp/env/ejb/CtsCmp2LocalHome passed");
      }
      catch(Exception e)
      {
         throw new ServerException("testV2 failed", e);
      }
   }
}
