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
package org.jboss.ejb.txtimer;

import java.security.PrivilegedAction;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.jboss.security.RunAs; 
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SecurityContextAssociation;

/** 
 * A collection of privileged actions for this package
 * 
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@redhat.com
 * @version $Revision: 81030 $
 */
public class SecurityActions
{
   interface RunAsIdentityActions
   {
      RunAsIdentityActions PRIVILEGED = new RunAsIdentityActions()
      {
         private final PrivilegedAction peekAction = new PrivilegedAction()
         {
            public Object run()
            {
               SecurityContext sc = getSecurityContext();
               if(sc == null)
                  throw new IllegalStateException("Security Context is null");
               return sc.getOutgoingRunAs();
               //return SecurityAssociation.peekRunAsIdentity();
            }
         };

         private final PrivilegedAction popAction = new PrivilegedAction()
         {
            public Object run()
            {
               //return SecurityAssociation.popRunAsIdentity();
               SecurityContext sc = getSecurityContext();
               if(sc == null)
                  throw new IllegalStateException("Security Context is null");
               RunAs ra = sc.getOutgoingRunAs();
               sc.setOutgoingRunAs(null);
               return ra;
            }
         };

         public RunAs peek()
         {
            return (RunAs)AccessController.doPrivileged(peekAction);
         }

         public void push(final RunAs id)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     //SecurityAssociation.pushRunAsIdentity(id);
                     SecurityContext sc = getSecurityContext();
                     if(sc == null)
                        throw new IllegalStateException("Security Context is null"); 
                     sc.setOutgoingRunAs(id);
                     return null;
                  }
               }
            );
         }

         public RunAs pop()
         {
            return (RunAs)AccessController.doPrivileged(popAction);
         }
      };

      RunAsIdentityActions NON_PRIVILEGED = new RunAsIdentityActions()
      {
         public RunAs peek()
         {
            SecurityContext sc = getSecurityContext();
            if(sc == null)
               throw new IllegalStateException("Security Context is null");
            return sc.getOutgoingRunAs();
            //return SecurityAssociation.peekRunAsIdentity();
         }

         public void push(RunAs id)
         {
          //SecurityAssociation.pushRunAsIdentity(id);
            SecurityContext sc = getSecurityContext();
            if(sc == null)
               throw new IllegalStateException("Security Context is null"); 
            sc.setOutgoingRunAs(id); 
         }

         public RunAs pop()
         {
          //return SecurityAssociation.popRunAsIdentity();
            SecurityContext sc = getSecurityContext();
            if(sc == null)
               throw new IllegalStateException("Security Context is null");
            RunAs ra = sc.getOutgoingRunAs();
            sc.setOutgoingRunAs(null);
            return ra;
         }
      };

      RunAs peek();

      void push(RunAs id);

      RunAs pop();
   }

   static ClassLoader getContextClassLoader()
   {
      return TCLAction.UTIL.getContextClassLoader();
   }

   static ClassLoader getContextClassLoader(Thread thread)
   {
      return TCLAction.UTIL.getContextClassLoader(thread);
   }
   
   static SecurityContext createSecurityContext(final String securityDomain) 
   throws PrivilegedActionException
   {
      return (SecurityContext)AccessController.doPrivileged(new PrivilegedExceptionAction()
      { 
         public Object run() throws Exception
         {
            SecurityContext sc = SecurityContextFactory.createSecurityContext(securityDomain); 
            SecurityContextAssociation.setSecurityContext(sc);
            return sc;
         }
      }); 
   } 
   
   static SecurityContext getSecurityContext()
   {
     return (SecurityContext) AccessController.doPrivileged(new PrivilegedAction()
     {

      public Object run()
      {
         return SecurityContextAssociation.getSecurityContext();
      }}); 
   }
   
   static void setSecurityContext(final SecurityContext sc)
   {
      AccessController.doPrivileged(new PrivilegedAction()
     {

      public Object run()
      {
         SecurityContextAssociation.setSecurityContext(sc);
         return null;
      }}); 
   }

   static void setContextClassLoader(ClassLoader loader)
   {
      TCLAction.UTIL.setContextClassLoader(loader);
   }

   static void setContextClassLoader(Thread thread, ClassLoader loader)
   {
      TCLAction.UTIL.setContextClassLoader(thread, loader);
   }

   static void pushRunAsIdentity(RunAs principal)
   {
      if(System.getSecurityManager() == null)
      {
         RunAsIdentityActions.NON_PRIVILEGED.push(principal);
      }
      else
      {
         RunAsIdentityActions.PRIVILEGED.push(principal);
      }
   }

   static RunAs popRunAsIdentity()
   {
      if(System.getSecurityManager() == null)
      {
         return RunAsIdentityActions.NON_PRIVILEGED.pop();
      }
      else
      {
         return RunAsIdentityActions.PRIVILEGED.pop();
      }
   }

   interface TCLAction
   {
      class UTIL
      {
         static TCLAction getTCLAction()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }

         static ClassLoader getContextClassLoader()
         {
            return getTCLAction().getContextClassLoader();
         }

         static ClassLoader getContextClassLoader(Thread thread)
         {
            return getTCLAction().getContextClassLoader(thread);
         }

         static void setContextClassLoader(ClassLoader cl)
         {
            getTCLAction().setContextClassLoader(cl);
         }

         static void setContextClassLoader(Thread thread, ClassLoader cl)
         {
            getTCLAction().setContextClassLoader(thread, cl);
         }
      }

      TCLAction NON_PRIVILEGED = new TCLAction()
      {
         public ClassLoader getContextClassLoader()
         {
            return Thread.currentThread().getContextClassLoader();
         }

         public ClassLoader getContextClassLoader(Thread thread)
         {
            return thread.getContextClassLoader();
         }

         public void setContextClassLoader(ClassLoader cl)
         {
            Thread.currentThread().setContextClassLoader(cl);
         }

         public void setContextClassLoader(Thread thread, ClassLoader cl)
         {
            thread.setContextClassLoader(cl);
         }
      };

      TCLAction PRIVILEGED = new TCLAction()
      {
         private final PrivilegedAction getTCLPrivilegedAction = new PrivilegedAction()
         {
            public Object run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         };

         public ClassLoader getContextClassLoader()
         {
            return (ClassLoader)AccessController.doPrivileged(getTCLPrivilegedAction);
         }

         public ClassLoader getContextClassLoader(final Thread thread)
         {
            return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  return thread.getContextClassLoader();
               }
            });
         }

         public void setContextClassLoader(final ClassLoader cl)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     Thread.currentThread().setContextClassLoader(cl);
                     return null;
                  }
               }
            );
         }

         public void setContextClassLoader(final Thread thread, final ClassLoader cl)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     thread.setContextClassLoader(cl);
                     return null;
                  }
               }
            );
         }
      };

      ClassLoader getContextClassLoader();

      ClassLoader getContextClassLoader(Thread thread);

      void setContextClassLoader(ClassLoader cl);

      void setContextClassLoader(Thread thread, ClassLoader cl);
   }
}
