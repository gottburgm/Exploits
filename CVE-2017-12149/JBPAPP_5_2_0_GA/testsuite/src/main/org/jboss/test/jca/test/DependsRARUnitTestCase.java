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
package org.jboss.test.jca.test;

import javax.management.ObjectName;

import org.jboss.test.JBossTestCase;
import org.jboss.deployers.spi.DeploymentException;

/**
 * DependsRARUnitTestCase.
 * 
 * @author <a href="vicky.kak@jboss.com">Vicky Kak</a>
 * 
 */
public class DependsRARUnitTestCase extends JBossTestCase
{

   public DependsRARUnitTestCase(String name)
   {
      super(name);
   }

   public void testDependsRAR() throws Exception
   {
	  try
	  {
		  deploy("rardependentonmbean.rar");
		  fail("The RAR with unsatisfied dependency is Deployed!");
	  }
	  catch(DeploymentException e)
	  {
		  // We expect this Exception		  
	  }
	  catch(Exception e)
	  {
		  fail("Unexpected exception crops in "+e);
	  }
	  ObjectName rarName = new ObjectName("jboss.test.jcaprops:name=PropertyTestResourceAdapter");
	  // check if the rarName is deployed in the MBeanServer
	  boolean state = getServer().isRegistered(rarName);
	  assertFalse(state);      
	  try
	  {
		  deploy("sardependsonmbean.sar");
		  try
		  {
			  // check if the rarName is deployed now , it *MUST* be 
			  state = getServer().isRegistered(rarName);
			  assertTrue(state);
		  }
		  finally
		  {
			  undeploy("rardependentonmbean.rar");
		  }
	  }
	  finally
	  {
		  undeploy("sardependsonmbean.sar");
	  }
   }
}
