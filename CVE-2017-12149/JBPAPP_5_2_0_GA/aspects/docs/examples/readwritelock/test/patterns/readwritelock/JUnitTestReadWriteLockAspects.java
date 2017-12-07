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
package test.patterns.readwritelock;
import junit.framework.TestCase;

/**
 *
 * @version <tt>$Revision: 80997 $</tt>
 * @author  <a href="mailto:chussenet@yahoo.com">{Claude Hussenet Independent Consultant}</a>.
 */
public class JUnitTestReadWriteLockAspects
	extends TestCase
	  {
	public JUnitTestReadWriteLockAspects(String arg0) {
		super(arg0);

	}

	public static void testProcessReadWriteLockPattern()
	{

	Account accountWithoutReadWriteLock = new Account(1000);
	processReadWriteLockPattern(accountWithoutReadWriteLock);

    assertTrue("No Concurrent Issue without ReadWriteLock Aspect !",1000!=accountWithoutReadWriteLock.getBalance());
  	Account accountWithReadWriteLock = new AccountWithReadWriteLock(1000);
	processReadWriteLockPattern(accountWithReadWriteLock);
    assertTrue("Concurrent Issue with ReadWriteLock Aspect !",1000==accountWithReadWriteLock.getBalance());

	}

	   public static void processReadWriteLockPattern(Account account)
	   {

	     new Thread(new ProcessTransaction(account,100)).start();
	     new Thread(new ProcessTransaction(account,300)).start();
	     new Thread(new ProcessTransaction(account,-150)).start();
	     new Thread(new ProcessTransaction(account,-250)).start();

	     try
	     {
			 Thread.currentThread().sleep(500);
	     }
	     catch (Exception e)
	     {
	       System.out.println(e);
		 }
	   }








}
