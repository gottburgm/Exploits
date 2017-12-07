/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb.txtimer;

// $Id: BigIntegerTimerIdGenerator.java 65455 2007-09-18 17:26:49Z dimitris@jboss.org $

import java.math.BigInteger;

/**
 * A timerId generator that uses a BigInteger count.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 65455 $
 * @since 10-Sep-2004
 */
public class BigIntegerTimerIdGenerator implements TimerIdGenerator
{
   // The next timer identity
   // JBAS-3379, seed using currentTimeMillis to avoid duplicate ids upon restart
   private BigInteger nextTimerId = BigInteger.valueOf(System.currentTimeMillis());

   /**
    * Get the next timer id
    */
   public synchronized String nextTimerId()
   {
      nextTimerId = nextTimerId.add(BigInteger.ONE);
      return nextTimerId.toString();
   }
}
