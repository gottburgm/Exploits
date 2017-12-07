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
package test.performance.standard.support;

import javax.management.Attribute;

/**
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81022 $
 *   
 */
public class Standard
   implements StandardMBean
{

   private int counter = 0;
   

   public void bogus1() {}
   public void bogus2() {}
   public void bogus3() {}
   public void bogus4() {}
   public void bogus5() {}


   public void methodInvocation() {}
   
   public void counter() {
     //++counter;
   }
   
   public int getCount() {
      return counter;
   }
      
   Integer int1;
   int int2;
   Object[][][] space;
   Attribute attr;
   
   public void mixedArguments(Integer int1, int int2, Object[][][] space, Attribute attr) {
     ++counter;
     
     this.int1 = int1;
     this.int2 = int2;
     this.space = space;
     this.attr = attr;
   }
   
   public void bogus6() {}
   public void bogus7() {}
   public void bogus8() {}
   public void bogus9() {}
   public void bogus10() {}
   
}
      



