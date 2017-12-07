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
package org.jboss.test.jca.mbean;

import javax.naming.InitialContext;

import org.jboss.classloader.plugins.ClassLoaderUtils;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.test.jca.adapter.TestConnectionFactory;

/**
 * RaDependsOnMe.
 * 
 * @author <a href="vicky.kak@jboss.com">Vicky Kak</a>
 * 
 */
public class RaDependsOnMe extends ServiceMBeanSupport implements RaDependsOnMeMBean
{
   @Override
   protected void createService() throws Exception
   {
	   log.info("Overriding the createService in RaDependsOnMe");
   }
   
   @Override
   protected void startService() throws Exception
   {
	   log.info("Overriding the startService in RaDependsOnMe");
   }   
}
