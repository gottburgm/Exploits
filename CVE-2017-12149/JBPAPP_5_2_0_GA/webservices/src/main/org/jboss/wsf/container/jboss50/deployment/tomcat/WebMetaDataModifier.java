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
package org.jboss.wsf.container.jboss50.deployment.tomcat;

import org.jboss.wsf.spi.deployment.Deployment;

/**
 * Modifies the web app according to the stack requirements.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2007
 */
public interface WebMetaDataModifier
{            
   final static String PROPERTY_GENERATED_WEBAPP = "org.jboss.ws.generated.webapp";
   final static String PROPERTY_WEBAPP_CONTEXT_PARAMETERS = "org.jboss.ws.webapp.ContextParameterMap";
   final static String PROPERTY_WEBAPP_SERVLET_CLASS = "org.jboss.ws.webapp.ServletClass";
   final static String PROPERTY_WEBAPP_SERVLET_CONTEXT_LISTENER = "org.jboss.ws.webapp.ServletContextListener";
   final static String PROPERTY_WEBAPP_URL = "org.jboss.ws.webapp.url";

   RewriteResults modifyMetaData(Deployment dep) throws ClassNotFoundException;
}
