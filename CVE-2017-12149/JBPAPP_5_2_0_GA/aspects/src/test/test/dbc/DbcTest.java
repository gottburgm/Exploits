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
package test.dbc;

import junit.framework.TestCase;
import test.dbc.java.Sorter;
import test.dbc.office.Computer;
import test.dbc.office.Developer;
import test.dbc.office.OfficeManager;
import test.dbc.stack.Stack;
import test.dbc.stack.StackImpl;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 80997 $
 */
public class DbcTest extends TestCase
{
	public DbcTest(String name) {
		super(name);

	}

	public void testOffice()throws Exception
	{
	   System.out.println("****************** TEST OFFICE ******************");
	   OfficeManager officeManager = new OfficeManager();

	   Computer compA = officeManager.createComputer("comp A");
	   //officeManager.createComputer("comp B");
	   Developer kabir = officeManager.createDeveloper("Kabir");

	   officeManager.assignComputer(compA, kabir);

	   
	   Developer bill = officeManager.createDeveloper("Bill");

	   Computer compB = officeManager.createComputer("comp B");
	   officeManager.assignComputer(compB, bill);
            
	   try
	   {
	      officeManager.createDeveloper(null);
	      if (true)throw new Exception("Did not validate developer null name");
	   }
	   catch(RuntimeException e)
	   {  
	   }
	   
	}
	
   public void testStack()throws Exception
   {
	   System.out.println("****************** TEST STACK ******************");
      Stack s = new StackImpl();
      s.push("one");
      s.push("two");
      s.pop();

      s.push("two");
      s.push("three");
      s.pop();
      s.pop();
      s.pop(); 
      try
      {
         s.pop(); 
	      throw new Exception("Did not validate empty stack before pop");
      }
      catch(RuntimeException e)
      {
         System.out.println(e.getMessage());
      }
   }
   
   public void testJavaExpression()throws Exception
   {
	   System.out.println("****************** TEST SORTER ******************");
	   
	   int[] unsorted = new int[]{4, 1, 5, 3};
	   Sorter.sort(unsorted);
	   
	   try
	   {
	      Sorter.brokenSort(unsorted);
	      throw new Exception("Did not validate list was not sorted");
	   }
	   catch(RuntimeException e)
	   {
         System.out.println(e.getMessage());
	   }
   }
   

}