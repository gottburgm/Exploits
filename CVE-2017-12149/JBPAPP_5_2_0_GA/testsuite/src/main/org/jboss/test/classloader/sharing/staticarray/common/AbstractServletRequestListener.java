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
package org.jboss.test.classloader.sharing.staticarray.common;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.jboss.test.classloader.sharing.staticarray.common.Sequencer;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public abstract class AbstractServletRequestListener implements
      ServletRequestListener
{
   
   public void requestDestroyed(ServletRequestEvent sre)
   {
   }

   public void requestInitialized(ServletRequestEvent sre)
   {
      ServletRequest sr = sre.getServletRequest();
      String op = sr.getParameter("op");
      if(op != null)
      {
         if(op.equalsIgnoreCase("set"))
         {
            String value = sr.getParameter("array");
            if(value == null)
               throw new IllegalStateException("op=set requires an array=x,y,z... value");
            String[] values = value.split(",");
            int[] array = new int[values.length];
            for(int n = 0; n < values.length; n ++)
               array[n] = Integer.parseInt(values[n]);
            setArray(array);
         }
      }
      // Copy the current info array to the Sequencer.info attribute
      Integer[] info = Sequencer.info;
      sr.setAttribute("Sequencer.info", info);
   }

   protected void setArray(int[] array)
   {
      Sequencer.setInfo(array);
   }
}
