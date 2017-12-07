/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.test.web.classloader.fromdd;

/**
 * TestMBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public interface TestFromDDMBean
{
   Object getCaseA();
   
   void setCaseA(Object caseA);

   Object getCaseB();

   void setCaseB(Object caseB);

   Object getCaseC();

   void setCaseC(Object caseC);

   Object getCaseD();

   void setCaseD(Object caseD);

   Object getCaseE();

   void setCaseE(Object caseE);

   Object getCaseF();

   void setCaseF(Object caseF);

   Object getCaseG();

   void setCaseG(Object caseG);
}
