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
package org.jboss.jmx.adaptor.html;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.jboss.logging.Logger;

/**
 * @author pskopek
 *
 */
public class JmxConsoleSessionListener implements HttpSessionListener {
   
   private static Logger log = Logger.getLogger(JmxConsoleSessionListener.class);
   
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent event) {
	   HttpSession session = event.getSession();
	   session.setAttribute(CSRFUtil.CSRF_TOKEN, CSRFUtil.generateCSRFToken(session));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent event) {
      HttpSession session = event.getSession();
      session.removeAttribute(CSRFUtil.CSRF_TOKEN);
      if (log.isTraceEnabled()) {
         log.trace("Removing CSRF toke from the session id="+session.getId());
      }
	}

}
