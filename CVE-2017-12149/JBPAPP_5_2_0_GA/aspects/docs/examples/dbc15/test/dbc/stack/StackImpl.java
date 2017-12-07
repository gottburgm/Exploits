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
package test.dbc.stack;

import java.util.LinkedList;

/**
 * 
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan </a>
 * @version $Revision: 80997 $
 */
public class StackImpl implements Stack
{
   private final LinkedList elements = new LinkedList();

   public void push(Object o)
   {
      elements.add(o);
   }

   public Object pop()
   {
      //For this we need $old functionality
      //* @@org.jboss.aspects.dbc.PostCond ({"$tgt.top() == $rtn"})
      final Object popped = top();
      elements.removeLast();
      return popped;
   }

   public Object top()
   {
      if (elements.size() == 0)
      {
         return null;
      }
      return elements.getLast();
   }

   public boolean isEmpty()
   {
      return elements.size() == 0;
   }
}

