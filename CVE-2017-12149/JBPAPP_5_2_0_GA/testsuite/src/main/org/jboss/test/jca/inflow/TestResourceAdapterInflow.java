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
package org.jboss.test.jca.inflow;

import javax.resource.spi.endpoint.MessageEndpoint;

/**
 * Management interface of TestResourceAdapter.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 67777 $
 */
public class TestResourceAdapterInflow
{
   TestResourceAdapter adapter;
   public TestResourceAdapterInflow(TestResourceAdapter adapter)
   {
      this.adapter = adapter;
   }
   
   public TestResourceAdapterInflowResults run() throws Exception
   {
      TestResourceAdapterInflowResults results = new TestResourceAdapterInflowResults();
      try
      {
         basicTest();
         results.basicTest.pass();
      }
      catch (Throwable t)
      {
         results.basicTest.fail(t);
      }

      return results;
   }
   
   public void basicTest() throws Exception
   {
      MessageEndpoint endpoint = adapter.getEndpoint("testInflow");
      if (endpoint == null)
         throw new Exception("Null endpoint");
      TestMessage message = new TestMessage();
      ((TestMessageListener) endpoint).deliverMessage(message);
      if (message.acknowledged == false)
         throw new Exception("MDB did not acknowledge the message");
      message = new TestMessage();
      ((TestMessageListener) endpoint).deliverMessageNoTransaction(message);
      if (message.acknowledged == false)
         throw new Exception("MDB did not acknowledge the message");
   }
}
