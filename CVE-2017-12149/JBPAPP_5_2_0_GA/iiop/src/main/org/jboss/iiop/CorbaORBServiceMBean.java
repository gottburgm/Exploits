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
package org.jboss.iiop;

import org.omg.CORBA.ORB;
import org.w3c.dom.Element;
import javax.ejb.spi.HandleDelegate;

/**
 *   Mbean interface for the JBoss CORBA ORB service.
 *      
 *   @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *   @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 *   @version $Revision: 110576 $
 */
public interface CorbaORBServiceMBean
   extends org.jboss.system.ServiceMBean
{
   public ORB getORB();

   public HandleDelegate getHandleDelegate();
   
   public String getORBClass();
   public void setORBClass(String orbClass);

   public String getORBSingletonClass();
   public void setORBSingletonClass(String orbSingletonClass);

   public String getORBSingletonDelegate();
   public void setORBSingletonDelegate(String orbSingletonDelegate);

   public void setORBPropertiesFileName(String orbPropertiesFileName);
   public String getORBPropertiesFileName();

   public Element getPortableInterceptorInitializers();
   public void setPortableInterceptorInitializers(
                                      Element portableInterceptorInitializers);

   public Element getDefaultIORSecurityConfig();
   public void setDefaultIORSecurityConfig(Element defaultIORSecurityConfig);

   public void setPort(int port);
   public int getPort();

   public void setSSLPort(int sslPort);
   public int getSSLPort();

   public void setSecurityDomain(String sslDomain);
   public String getSecurityDomain();

   boolean getSSLComponentsEnabled();
   void setSSLComponentsEnabled(boolean sslComponentsEnabled);

   boolean getSendSASAcceptWithExceptionEnabled();
   void setSendSASAcceptWithExceptionEnabled(boolean value);

   boolean getOTSContextPropagationEnabled();
   void setOTSContextPropagationEnabled(boolean value);

   boolean getSunJDK14IsLocalBugFix();
   void setSunJDK14IsLocalBugFix(boolean sunJDK14IsLocalBugFix);
   
   public void setORBGracefulShutdown(boolean value);
   public boolean getORBGracefulShutdown();
   
}

