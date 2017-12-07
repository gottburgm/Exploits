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
package org.jboss.test.messagedriven.test;

import org.jboss.test.messagedriven.support.SimpleMessageDrivenUnitTest;

/**
 * Basic tests of message driven beans with no destination type specified as it is now
 * optional.
 *
 * The destinationType will be an empty string such that the JmsActivation
 * must resolve the property based on the deployed destination.
 *
 * Based on SimpleQueueMessageDrivenUnitTestCase by Adrian Brock.
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version <tt>$Revision: 1.4</tt>
 */
public class SimpleQueueMessageDrivenNoDestinationTypeUnitTestCase extends SimpleMessageDrivenUnitTest
{
   public SimpleQueueMessageDrivenNoDestinationTypeUnitTestCase(String name)
   {
      super(name, testQueue, testQueueNoDestinationTypeProps);
   }
}
