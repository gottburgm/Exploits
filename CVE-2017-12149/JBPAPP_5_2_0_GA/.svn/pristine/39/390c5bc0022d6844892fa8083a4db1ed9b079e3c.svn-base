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
/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.jboss.test.classloader.leak.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ClassloaderLeakContextListener implements ServletContextListener
{
//   private Class[] RELEASE_SIGNATURE = {ClassLoader.class};

   public void contextInitialized(ServletContextEvent arg0)
   {
      org.jboss.test.classloader.leak.clstore.ClassLoaderStore.getInstance().storeClassLoader("WEBAPP", ClassloaderLeakContextListener.class.getClassLoader());
   }
   
   public void contextDestroyed(ServletContextEvent arg0)
   {
//      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
//
//      Object[] params = new Object[1];
//      params[0] = tccl;
//
      // Walk up the tree of classloaders, finding all the available
      // LogFactory classes and releasing any objects associated with
      // the tccl (ie the webapp).
      //
      // When there is only one LogFactory in the classpath, and it
      // is within the webapp being undeployed then there is no problem;
      // garbage collection works fine.
      //
      // When there are multiple LogFactory classes in the classpath but
      // parent-first classloading is used everywhere, this loop is really
      // short. The first instance of LogFactory found will
      // be the highest in the classpath, and then no more will be found.
      // This is ok, as with this setup this will be the only LogFactory
      // holding any data associated with the tccl being released.
      //
      // When there are multiple LogFactory classes in the classpath and
      // child-first classloading is used in any classloader, then multiple
      // LogFactory instances may hold info about this TCCL; whenever the
      // webapp makes a call into a class loaded via an ancestor classloader
      // and that class calls LogFactory the tccl gets registered in
      // the LogFactory instance that is visible from the ancestor
      // classloader. However the concrete logging library it points
      // to is expected to have been loaded via the TCCL, so the 
      // underlying logging lib is only initialised/configured once.
      // These references from ancestor LogFactory classes down to
      // TCCL classloaders are held via weak references and so should
      // be released but there are circumstances where they may not.
      // Walking up the classloader ancestry ladder releasing
      // the current tccl at each level tree, though, will definitely
      // clear any problem references.
//      ClassLoader loader = tccl;
//      while (loader != null) {
//          // Load via the current loader. Note that if the class is not accessable
//          // via this loader, but is accessable via some ancestor then that class
//          // will be returned.
//          try {
//              System.out.println("Calling LogFactory.release() for " + loader);
//              Class logFactoryClass = loader.loadClass("org.apache.commons.logging.LogFactory");
//              Method releaseMethod = logFactoryClass.getMethod("release", RELEASE_SIGNATURE);
//              releaseMethod.invoke(null, params);
//              loader = logFactoryClass.getClassLoader().getParent();
//          } catch(ClassNotFoundException ex) {
//              // Neither the current classloader nor any of its ancestors could find
//              // the LogFactory class, so we can stop now.
//              loader = null;
//          } catch(NoSuchMethodException ex) {
//              // This is not expected; every version of JCL has this method
//              System.err.println("LogFactory instance found which does not support release method!");
//              loader = null;
//          } catch(IllegalAccessException ex) {
//              // This is not expected; every ancestor class should be accessable
//              System.err.println("LogFactory instance found which is not accessable!");
//              loader = null;
//          } catch(InvocationTargetException ex) {
//              // This is not expected
//              System.err.println("LogFactory instance release method failed!");
//              loader = null;
//          }
//      }
//      
//      // Just to be sure, invoke release on the LogFactory that is visible from
//      // this ServletContextCleaner class too. This should already have been caught
//      // by the above loop but just in case...
//      LogFactory.release(tccl);
   }

}
