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
package org.jboss.test.refs.clientpath;

import javax.ejb.EJB;

import org.jboss.test.refs.clientpath.ejbs.Iface1;
import org.jboss.test.refs.clientpath.ejbs.Iface2;

/**
 * Annotation only client with path relative ejb-refs
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class Client
{
   @EJB(beanName = "refspath-ejb1.jar#EJB")
   private static Iface1 ejb1;

   @EJB(beanName = "refspath-ejb2.jar#EJB")
   private static Iface2 ejb2;

   public static void main(String[] args)
      throws Exception
   {
      if(ejb1 == null)
         throw new IllegalStateException("ejb1 is null");
      if(ejb2 == null)
         throw new IllegalStateException("ejb2 is null");
      ejb1.method1();
      ejb2.method2();
   }

}
