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
package org.jboss.test.system.controller.integration.support;

import org.jboss.test.system.controller.support.Order;
import org.jboss.test.system.controller.support.Simple;

/**
 * SimpleBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class SimpleBean
{
   /** Injection */
   private Simple simple;
   
   public int createOrder;
   public int startOrder;
   public int stopOrder;
   public int destroyOrder;

   /**
    * Create a new SimpleBean.
    */
   public SimpleBean()
   {
   }
   
   /**
    * Create a new SimpleBean.
    * 
    * @param simple the simple object
    */
   public SimpleBean(Simple simple)
   {
      this.simple = simple;
   }

   /**
    * Get the simple.
    * 
    * @return the simple.
    */
   public Simple getSimple()
   {
      return simple;
   }

   /**
    * Set the simple.
    * 
    * @param simple the simple.
    */
   public void setSimple(Simple simple)
   {
      this.simple = simple;
   }
   
   public void create()
   {
      createOrder = Order.getOrder();
   }
   
   public void start()
   {
      startOrder = Order.getOrder();
   }
   
   public void create(Simple simple)
   {
      this.simple = simple;
      createOrder = Order.getOrder();
   }
   
   public void start(Simple simple)
   {
      this.simple = simple;
      startOrder = Order.getOrder();
   }
   
   public void install(Simple simple)
   {
      this.simple = simple;
   }
}
