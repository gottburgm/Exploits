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
package org.jboss.test.excepiiop.ejb;

import javax.ejb.EJBException;

import org.jboss.test.util.ejb.SessionSupport;
import org.jboss.test.excepiiop.interfaces.ExceptionThrower;
import org.jboss.test.excepiiop.interfaces.JavaException;
import org.jboss.test.excepiiop.interfaces.IdlException;

public class ExceptionThrowerBean
   extends SessionSupport
{
   public void throwException(int i)
      throws JavaException,IdlException  
   {
      if (i > 0)
         throw new JavaException(i, "" + i + " is positive");
      else if (i < 0)
         throw new IdlException(i, "" + i + " is negative");
      else
         return; // no exception
   }
}
