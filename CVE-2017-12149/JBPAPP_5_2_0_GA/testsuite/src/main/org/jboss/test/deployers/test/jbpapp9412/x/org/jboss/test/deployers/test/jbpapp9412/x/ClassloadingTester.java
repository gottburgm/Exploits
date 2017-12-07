/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.test.jbpapp9412.x;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author bmaxwell
 *
 */
public class ClassloadingTester extends ServiceMBeanSupport implements ClassloadingTesterMBean
{	
   private Logger log = Logger.getLogger(this.getClass().getName());
	/**
	 * 
	 */
	public ClassloadingTester()
	{
	}
	
	@Override
	protected void startService() throws Exception
	{	   
	   Object[] results = new Util().runTest();
	   for(int i=0; i<results.length; i++)
	   {
	      if(results[i] instanceof String)
	         log.info( (String) results[i]);
	      else if (results[i] instanceof Exception)
	         throw ((Exception) results[i]);
	      else 
	         throw new Exception("results[" + i + "] is of type: " + results[i].getClass().getName() + " which should not happen");
	   }
	}
}
