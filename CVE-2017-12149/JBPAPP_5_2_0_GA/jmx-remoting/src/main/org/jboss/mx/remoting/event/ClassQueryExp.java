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
package org.jboss.mx.remoting.event;

import java.io.Serializable;
import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InvalidApplicationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.QueryExp;

/**
 * ClassQueryExp is a QueryExp implementation that allows you
 * to check the ObjectName on a query against one or more
 * class names to make sure that they are the instanceof one or more
 * classes.   <P>
 * <p/>
 * Example code:
 * <p/>
 * <CODE><pre>
 *   ClassQueryExp query=new ClassQueryExp(MyMBean.class);
 *   Set beans=mbeanserver.queryMBeans(new ObjectName("*:*"),query);
 * </pre></CODE>
 * <p/>
 * The query in the above example will only return MBean ObjectInstances that
 * are an instanceof <tt>MyMBean</tt> class.
 *
 * @author <a href="jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class ClassQueryExp implements QueryExp, Serializable
{
   private static final long serialVersionUID = -4099952623687795850L;

   public static final int AND = 1;
   public static final int OR = 2;

   String classes[];
   int operator;
   transient MBeanServer mBeanServer;

   /**
    * default will create using a AND operator
    *
    * @param cl
    */
   public ClassQueryExp(Class cl[])
   {
      this(cl, OR);
   }

   /**
    * default will create using a AND operator
    *
    * @param cl
    */
   public ClassQueryExp(Class cl)
   {
      this(new Class[]{cl}, AND);
   }

   public ClassQueryExp(Class cl, int operator)
   {
      this(new Class[]{cl}, operator);
   }

   public ClassQueryExp(Class cl[], int operator)
   {
      this.classes = new String[cl.length];
      for(int c = 0; c < cl.length; c++)
      {
         this.classes[c] = cl[c].getName();
      }
      this.operator = operator;
   }

   public boolean apply(ObjectName objectName) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      try
      {
         for(int c = 0; c < classes.length; c++)
         {
            boolean value = mBeanServer.isInstanceOf(objectName, classes[c]);
            if(value && operator == OR)
            {
               return true;
            }
            else if(!value && operator == AND)
            {
               return false;
            }
         }
         return (operator == OR ? false : true);
      }
      catch(Exception ex)
      {
         return false;
      }
   }

   /**
    * called by MBeanServer prior to apply
    *
    * @param mBeanServer
    */
   public void setMBeanServer(MBeanServer mBeanServer)
   {
      this.mBeanServer = mBeanServer;
   }
}
