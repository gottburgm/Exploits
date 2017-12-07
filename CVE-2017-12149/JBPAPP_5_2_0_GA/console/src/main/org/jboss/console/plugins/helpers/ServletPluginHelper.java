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
package org.jboss.console.plugins.helpers;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * <description>
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81010 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>23 dec 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */
public class ServletPluginHelper
   extends javax.servlet.http.HttpServlet
{
   
   // Constants -----------------------------------------------------
   
   public static final String WRAPPER_CLASS_PARAM = "WrapperClass";
   
   // Attributes ----------------------------------------------------
   
   protected ServletConfig config = null;
   
   protected PluginWrapper wrapper = null;      

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
      
   // Public --------------------------------------------------------
   
   // Z implementation ----------------------------------------------
   
   // HttpServlet overrides -----------------------------------------
   
   public void init (ServletConfig config) throws ServletException
   {
      try
      {
         super.init (config);      
         
         this.config = config;
         
         wrapper = createPluginWrapper ();      
         wrapper.init (config);      
      }
      catch (Throwable e)
      {
         //throw new ServletException ("Failed to init plugin", e);
         // @todo, cleanup the logic to display an unavailable place holder
         log("Failed to init plugin, "+e.getMessage());
      }
   }


   public void destroy ()
   {
      if( wrapper != null )
      {
         wrapper.destroy ();
      }
      super.destroy ();
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   protected PluginWrapper createPluginWrapper () throws Exception
   {
      String tmp = config.getInitParameter(WRAPPER_CLASS_PARAM);
      if (tmp != null && !"".equals(tmp))
      {
         // These plugins do provide their own wrapper implementation
         //
         Class clazz = Thread.currentThread().getContextClassLoader().loadClass(tmp);
         return (PluginWrapper) (clazz.newInstance());
      }
      
      
      // Otherwise we make the hypothesis that the script provides
      // all required information
      //
      return new BasePluginWrapper ();
      
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}
