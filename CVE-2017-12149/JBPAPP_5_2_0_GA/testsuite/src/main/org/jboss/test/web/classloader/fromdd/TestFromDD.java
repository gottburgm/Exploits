/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.web.classloader.fromdd;

/**
 * Test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class TestFromDD implements TestFromDDMBean
{
   private Object caseA;
   private Object caseB;
   private Object caseC;
   private Object caseD;
   private Object caseE;
   private Object caseF;
   private Object caseG;

   public Object getCaseA()
   {
      return caseA;
   }
   
   public void setCaseA(Object caseA)
   {
      this.caseA = caseA;
   }

   public Object getCaseB()
   {
      return caseB;
   }

   public void setCaseB(Object caseB)
   {
      this.caseB = caseB;
   }

   public Object getCaseC()
   {
      return caseC;
   }

   public void setCaseC(Object caseC)
   {
      this.caseC = caseC;
   }

   public Object getCaseD()
   {
      return caseD;
   }

   public void setCaseD(Object caseD)
   {
      this.caseD = caseD;
   }

   public Object getCaseE()
   {
      return caseE;
   }

   public void setCaseE(Object caseE)
   {
      this.caseE = caseE;
   }

   public Object getCaseF()
   {
      return caseF;
   }

   public void setCaseF(Object caseF)
   {
      this.caseF = caseF;
   }

   public Object getCaseG()
   {
      return caseG;
   }

   public void setCaseG(Object caseG)
   {
      this.caseG = caseG;
   }
}
