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
package org.jboss.test.cluster.web;

import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSession;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class SessionListener
   implements HttpSessionListener, HttpSessionActivationListener
{
   public void sessionCreated(HttpSessionEvent event)
   {
      HttpSession session = event.getSession();
      System.out.println("SessionListener.sessionCreated, id="+session.getId());
      session.setAttribute("SessionListener", this.toString());
   }

   public void sessionDestroyed(HttpSessionEvent event)
   {
      HttpSession session = event.getSession();
      System.out.println("SessionListener.sessionDestroyed, id="+session.getId());
   }

   public void sessionWillPassivate(HttpSessionEvent event)
   {
      HttpSession session = event.getSession();
      System.out.println("SessionListener.sessionWillPassivate, id="+session.getId());
   }

   public void sessionDidActivate(HttpSessionEvent event)
   {
      HttpSession session = event.getSession();
      System.out.println("SessionListener.sessionDidActivate, id="+session.getId());
   }
}
