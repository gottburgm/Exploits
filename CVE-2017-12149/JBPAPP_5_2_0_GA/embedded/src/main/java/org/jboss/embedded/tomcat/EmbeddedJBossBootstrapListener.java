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
package org.jboss.embedded.tomcat;

import java.net.URL;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.jboss.embedded.Bootstrap;
import org.jboss.embedded.tomcat.jndi.ENCFactory;
import org.jboss.embedded.tomcat.jndi.TomcatBridgeContextFactory;
import org.jboss.embedded.url.JavaProtocolHandlerPkgs;
import org.jboss.net.protocol.URLStreamHandlerFactory;

/**
 * Tomcat Listener that initializes embedded jboss.
 *
 * It resets the JNDI context factory as well as URL_PKG_PREFIXES
 *
 * It sets up URL.setURLStreamHandlerFactory to be a jboss supplied one but
 * it does append org.apache.naming.resources to the pkgs.
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class EmbeddedJBossBootstrapListener implements LifecycleListener
{
   public void lifecycleEvent(LifecycleEvent event)
   {
      if (Lifecycle.START_EVENT.equals(event.getType()))
      {                                                                                            
         innerStart();
      }
      else if (Lifecycle.AFTER_STOP_EVENT.equals(event.getType()))
      {
         innerStop();
      }
   }

   protected void innerStart()
   {
      System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TomcatBridgeContextFactory.class.getName());
      System.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming");

      String[] protocolPkgs = {
              "org.apache.naming.resources"
      };
      JavaProtocolHandlerPkgs.setupHandlerPkgs(protocolPkgs);
      URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory());
      try
      {
         Bootstrap.getInstance().bootstrap();
         InitialContext ctx = new InitialContext();
         ENCFactory.rebindComp(ctx);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   protected void innerStop()
   {
      Bootstrap.getInstance().shutdown();
   }
}
