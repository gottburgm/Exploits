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
package org.jboss.embedded.tutorial.junit.beans;

import org.jboss.annotation.ejb.Depends;

import javax.ejb.MessageDriven;
import javax.ejb.ActivationConfigProperty;
import javax.jms.MessageListener;
import javax.jms.Message;

@MessageDriven(activationConfig =
{
        @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
        @ActivationConfigProperty(propertyName="destination", propertyValue="queue/example")
})
@Depends("jboss.mq.destination:name=example,service=Queue") // @Depends currently requires a canonical name for JMX beans
public class ExampleMDB implements MessageListener
{
   public static boolean executed = false;

   public void onMessage(Message recvMsg)
   {
      System.out.println("----------------");
      System.out.println("Received message");
      executed = true;
      System.out.println("----------------");
   }

}
