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
package org.jboss.aspects.library;

import org.jboss.aop.deployers.AbstractAspectManager;
import org.jboss.logging.Logger;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85943 $
 */
public class JBossAspectLibrary
{
   private static final Logger log = Logger.getLogger(JBossAspectLibrary.class);
   AbstractAspectManager aspectManager;

   public AbstractAspectManager getAspectManager()
   {
      return aspectManager;
   }

   public void setAspectManager(AbstractAspectManager aspectManagerBean)
   {
      this.aspectManager = aspectManagerBean;
   }
   
   /**
    * @throws Exception
    * @see org.jboss.system.ServiceMBeanSupport#start()
    */
   public void start() throws Exception
   {
      //Use the loader of this class so that we can find base-aspects.xml in the resources
      //using the new loaders
      ClassLoader tcl = SecurityActions.getThreadContextClassLoader();
      try
      {
         ClassLoader mycl = SecurityActions.getClassLoader(this.getClass());
         SecurityActions.setThreadContextClassLoader(mycl);
         aspectManager.deployBaseAspects();
      }
      finally
      {
         SecurityActions.setThreadContextClassLoader(tcl);
      }
   }
   
   public void stop()
   {
      //Use the loader of this class so that we can find base-aspects.xml in the resources
      //using the new loaders
      ClassLoader tcl = SecurityActions.getThreadContextClassLoader();
      try
      {
         ClassLoader mycl = SecurityActions.getClassLoader(this.getClass());
         SecurityActions.setThreadContextClassLoader(mycl);
         aspectManager.undeployBaseAspects();
      }
      finally
      {
         SecurityActions.setThreadContextClassLoader(tcl);
      }
   }
}
