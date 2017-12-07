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
package org.jboss.test.cts.test;

import org.jboss.test.cts.interfaces.StrictlyPooledSession;
import org.jboss.test.cts.interfaces.StrictlyPooledSessionHome;

import org.jboss.logging.Logger;
import EDU.oswego.cs.dl.util.concurrent.CountDown;

/** Invoker thread for StatelessSession tests.
* @author Scott.Stark@jboss.org
* @version $Revision: 81036 $
*/
public class SessionInvoker extends Thread
{
   StrictlyPooledSessionHome home;
   Logger log;
   int id;
   CountDown done;
   Exception runEx;
   public SessionInvoker(StrictlyPooledSessionHome home, int id, CountDown done,
         Logger log)
   {
      super("SessionInvoker#"+id);
      this.home = home;
      this.id = id;
      this.done = done;
      this.log = log;
   }
   public void run()
   {
      log.debug("Begin run, this="+this);
      try
      {
         StrictlyPooledSession session = home.create();
         session.methodA();
         session.remove();
      }
      catch(Exception e)
      {
         runEx = e;
      }
      done.release();
      log.debug("End run, this="+this);
   }

}
