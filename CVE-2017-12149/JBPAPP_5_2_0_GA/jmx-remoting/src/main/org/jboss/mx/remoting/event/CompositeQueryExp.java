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
 * CompositeQueryExp is a composite QueryExp that allows multiple QueryExp implementations to be
 * chained together (a little easier than trying to use Query class).
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81084 $
 */
public class CompositeQueryExp implements QueryExp, Serializable
{
   static final long serialVersionUID = 6918797787135545210L;

   public static final int AND = 0;
   public static final int OR = 1;

   private int operator;
   private QueryExp exps[];

   /**
    * create a composite QueryExp with the default <tt>AND</tt> operator
    *
    * @param exp
    */
   public CompositeQueryExp(QueryExp exp[])
   {
      this(exp, AND);
   }

   public CompositeQueryExp(QueryExp exp[], int operator)
   {
      this.exps = exp;
      this.operator = operator;
   }

   public boolean apply(ObjectName objectName) throws BadStringOperationException, BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException
   {
      for(int c = 0; c < exps.length; c++)
      {
         if(exps[c] != null)
         {
            boolean value = exps[c].apply(objectName);
            if(value && operator == OR)
            {
               return true;
            }
            else if(!value && operator == AND)
            {
               return false;
            }
         }
      }
      return (operator == AND) ? true : false;
   }

   public void setMBeanServer(MBeanServer mBeanServer)
   {
      for(int c = 0; c < exps.length; c++)
      {
         if(exps[c] != null)
         {
            exps[c].setMBeanServer(mBeanServer);
         }
      }
   }
}
