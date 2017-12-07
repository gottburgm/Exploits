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
package org.jboss.test.aop.bean;

import org.jboss.aspects.patterns.observable.Observer;
import org.jboss.aspects.patterns.observable.Subject;
import org.jboss.system.ServiceMBeanSupport;
/**
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class ObservableTester
   extends ServiceMBeanSupport
   implements ObservableTesterMBean
{
   // Constants ----------------------------------------------------
   
   // Attributes ---------------------------------------------------

   // Static -------------------------------------------------------
   
   // Constructors -------------------------------------------------

   public ObservableTester()
   {
   }
   
   // Public -------------------------------------------------------

   public void testAll() throws Exception 
   {
      Temperature temperature = new Temperature();
      LogUtil logUtil = new LogUtil();
      
      Subject subject = (Subject) temperature;
      Observer observer = (Observer) logUtil;
      
      subject.addObserver(observer);
      
      temperature.setTemperature(10);
      assertEquals("Temperature=10", logUtil.lastLog);
   }

   public void assertEquals(Object object1, Object object2) throws Exception
   {
      if (object1.equals(object2) == false)
         throw new Exception("Expected " + object1 + " got " + object2);
   }
   
   // Inner classes -------------------------------------------------
}

