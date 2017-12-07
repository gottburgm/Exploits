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
package org.jboss.test.ejb3.iiop;

import java.rmi.RemoteException;

import javax.annotation.PreDestroy;
import javax.ejb.CreateException;
import javax.ejb.Init;
import javax.ejb.RemoteHome;
import javax.ejb.Remove;
import javax.ejb.RemoveException;
import javax.ejb.Stateful;

import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 97163 $
 */
@Stateful
//@Remote(MyStateful.class) // EJB 3.0 4.6.6, no longer allowed
@RemoteHome(MyStatefulHome.class)
//@RemoteBinding(factory=RemoteBindingDefaults.PROXY_FACTORY_IMPLEMENTATION_IOR) // JBMETA-117
public class MyStatefulBean
{
   private static final Logger log = Logger.getLogger(MyStatefulBean.class);
   
   private String name;
   
   @Init
   public void ejbCreate() throws CreateException, RemoteException
   {
      name = "anonymous";
   }
   
   @Init
   public void ejbCreate(String name) throws CreateException, RemoteException
   {
      this.name = name;
   }
   
   @Remove
   public void ejbRemove() throws RemoveException, RemoteException
   {
      log.info("remove bean");
   }
   
   public String getName() throws RemoteException
   {
      return name;
   }
   
   @PreDestroy
   public void preDestroy()
   {
      log.info("pre destroy");
   }
   
   public String sayHello() throws RemoteException
   {
      return "Hello " + name;
   }
   
   public void setName(String name) throws RemoteException
   {
      this.name = name;
   }
}
