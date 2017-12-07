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
package org.jboss.resource.adapter.mail.inflow;

import java.lang.reflect.Method;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.work.Work;
import javax.mail.Message;

import org.jboss.resource.adapter.mail.MailResourceAdapter;
import org.jboss.logging.Logger;

/**
 * The MailActivation encapsulates a MailResourceAdapter#endpointActivation
 * {@link javax.resource.spi.ResourceAdapter#endpointActivation(MessageEndpointFactory,javax.resource.spi.ActivationSpec)}
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 71554 $
 */
public class MailActivation
   implements Comparable, Work
{
   private static final Logger log = Logger.getLogger(MailActivation.class);
   /**
    * The MailListener.onMessage method
    */
   public static final Method ON_MESSAGE;

   static
   {
      try
      {
         Class[] sig = {Message.class};
         ON_MESSAGE = MailListener.class.getMethod("onMessage", sig);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /** A flag indicated if the unit of work has been released */ 
   private boolean released;
   /** The logging trace level flag */
   private boolean trace;
   /** The time at which the next new msgs check should be performed */
   private long nextNewMsgCheckTime;
   /** The resource adapter */
   protected MailResourceAdapter ra;
   /** The activation spec for the mail folder */
   protected MailActivationSpec spec;
   /** The message endpoint factory */
   protected MessageEndpointFactory endpointFactory;

   public MailActivation(MailResourceAdapter ra, MessageEndpointFactory endpointFactory,
      MailActivationSpec spec)
   {
      this.ra = ra;
      this.endpointFactory = endpointFactory;
      this.spec = spec;
      this.trace = log.isTraceEnabled();
   }

   public long getNextNewMsgCheckTime()
   {
      return nextNewMsgCheckTime;
   }
   public void updateNextNewMsgCheckTime(long now)
   {
      nextNewMsgCheckTime = now + spec.getPollingInterval();
   }

   public int compareTo(Object obj)
   {
      MailActivation ma = (MailActivation) obj;
      long compareTo = nextNewMsgCheckTime - ma.getNextNewMsgCheckTime();
      return (int) compareTo;
   }

   public boolean isReleased()
   {
      return released;
   }

   // --- Begin Work interface
   public void release()
   {
      released = true;
      if( trace )
         log.trace("released");
   }

   public void run()
   {
      released = false;
      if( trace )
         log.trace("Begin new msgs check");
      try
      {
         MailFolder mailFolder = MailFolder.getInstance(spec);
         mailFolder.open();
         while(mailFolder.hasNext())
         {
            Message msg = (Message) mailFolder.next();
            deliverMsg(msg);
         }
         mailFolder.close();
      }
      catch (Exception e)
      {
         log.error("Failed to execute folder check, spec="+spec);
      }
      if( trace )
         log.trace("End new msgs check");
   }
   // --- End Work interface

   private void deliverMsg(Message msg)
   {
      MessageEndpoint endpoint = null;
      try
      {
         endpoint = endpointFactory.createEndpoint(null);
         if (endpoint != null)
         {
            if( trace )
               log.trace("deliverMsg, msg subject="+msg.getSubject());
            MailListener listener = (MailListener) endpoint;
            listener.onMessage(msg);
         }
      }
      catch (Throwable e)
      {
         log.debug("onMessage delivery failure", e);
      }
      finally
      {
         if (endpoint != null)
         {
            endpoint.release();
         }
      }
   }

}
