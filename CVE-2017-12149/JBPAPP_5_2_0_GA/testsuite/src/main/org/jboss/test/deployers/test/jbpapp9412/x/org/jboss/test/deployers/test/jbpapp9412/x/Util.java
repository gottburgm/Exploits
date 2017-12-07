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

public class Util
{
	public Object[] runTest()
	{
	   Object[] results = new Object[2];
	   
	   String[] classes = { "org.jboss.test.deployers.test.jbpapp9412.x.X", "org.jboss.test.deployers.test.jbpapp9412.y.Y" };
	   
       for(int i=0; i<classes.length; i++)
       {
         try
         {
           results[i] = new String(Class.forName(classes[i]).newInstance().toString());
         }
         catch(Exception e)
         {
           //e.printStackTrace();           
           results[i] = e;
         }
       }
       return results;
	}
}
