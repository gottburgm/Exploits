/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.refs.clientorb;

import javax.annotation.Resource;
import javax.ejb.EJB;

import org.jboss.test.refs.common.ServiceLocator;
import org.jboss.test.refs.resources.ResourceIF;
import org.omg.CORBA.ORB;

/**
 * Annotation only client with ORB injection
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class Client
{
   @EJB(beanName = "ResourceOnMethodBean")
   private static ResourceIF resourceMethodBean;

   @EJB(beanName = "ResourceOnFieldBean")
   private static ResourceIF resourceFieldBean;

   @EJB(beanName = "ResourcesOnClassBean")
   private static ResourceIF resourceClassBean;

   @Resource
   private static ORB orb;

   protected ResourceIF getResourceOnMethodBean()
   {
      return resourceMethodBean;
   }

   protected ResourceIF getResourceOnFieldBean()
   {
      return resourceFieldBean;
   }

   protected ResourceIF getResourcesOnClassBean()
   {
      return resourceClassBean;
   }

   public static void main(String[] args)
      throws Exception
   {
      Client client = new Client();
      client.clientOrbTest();
   }

   public void clientOrbTest() throws Exception
   {
      if (orb == null)
      {
         throw new Exception("orb is not injected");
      }
      System.out.print("ORB is injected: "+orb);
      ORB orb2 = (ORB) ServiceLocator.lookup("java:comp/ORB");
      if (orb2 == null)
      {
         throw new Exception("java:comp/ORB lookup is null");
      }
   }

}
