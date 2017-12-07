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
package org.jboss.web.jsf.integration.config;

import com.sun.faces.config.ConfigureListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.jboss.logging.Logger;

/**
 * This ServletContextListener sets up a JBoss-specific environment for JSF
 * and then delegates the rest of the setup to the JSF RI.
 *
 * @author Stan Silvert
 */
public class JBossJSFConfigureListener extends ConfigureListener 
{
    private static final String WAR_BUNDLES_JSF_IMPL = "org.jboss.jbossfaces.WAR_BUNDLES_JSF_IMPL";
   
    private static Logger LOG = Logger.getLogger(JBossJSFConfigureListener.class);
    
    private ServletContext servletContext;
    
    private boolean initialized = false;
    
    public static boolean warBundlesJSFImpl(ServletContext servletContext)
    {
       String bundledJSFImpl = servletContext.getInitParameter(WAR_BUNDLES_JSF_IMPL);
       return (bundledJSFImpl != null) && bundledJSFImpl.equalsIgnoreCase("true");
    }

    @Override
    public void contextInitialized(ServletContextEvent event) 
    {
        this.servletContext = event.getServletContext();
        if (warBundlesJSFImpl(this.servletContext)) return;
  
        checkForMyFaces();
        initializeJspRuntime();
        initialized = true;
        super.contextInitialized(event);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent event) 
    {
        if (initialized)
        {
           initialized = false;
           super.contextDestroyed(event);
        }
    }
    
    // This method accounts for a peculiar problem with Jasper that pops up from time
    // to time.  In some cases, if the JspRuntimeContext is not loaded then the JspFactory
    // will not be initialized for JSF.  This method assures that it will always be
    // be loaded before JSF is initialized.
    private static void initializeJspRuntime() 
    {

        try 
        {
            Class.forName("org.apache.jasper.compiler.JspRuntimeContext");
        }  
        catch (ClassNotFoundException cnfe) 
        {
            // do nothing 
        }
    }

    private void checkForMyFaces()
    {
        try
        {
            Thread.currentThread()
                  .getContextClassLoader()
                  .loadClass("org.apache.myfaces.webapp.StartupServletContextListener");
            LOG.warn("MyFaces JSF implementation found!  This version of JBoss AS ships with the java.net implementation of JSF.  There are known issues when mixing JSF implementations.  This warning does not apply to MyFaces component libraries such as Tomahawk.  However, myfaces-impl.jar and myfaces-api.jar should not be used without disabling the built-in JSF implementation.  See the JBoss wiki for more details.");
        }
        catch (ClassNotFoundException e)
        {
            // ignore - this is a good thing
        }
    }

}
