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
package org.jboss.system;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.management.ObjectName;

/**
 * ServiceContext holds information for the Service
 *
 * @see Service
 * @see ServiceMBeanSupport
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">marc fleury</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81485 $
 */
public class ServiceContext implements Serializable
{
   /** @since 4.0.2 */
   private static final long serialVersionUID = 7461263042948325633L;

   /** The possible string-fied states */
   private static final String[] stateNames = {
         "INSTALLED",
         "CONFIGURED",
         "CREATED",
         "RUNNING",
         "FAILED",
         "STOPPED",
         "DESTROYED",
         "NOTYETINSTALLED"
      };
   
   /** Valid service states */
   public static final int INSTALLED = 0;
   public static final int CONFIGURED = 1;
   public static final int CREATED = 2;
   public static final int RUNNING = 3;
   public static final int FAILED = 4;
   public static final int STOPPED = 5;
   public static final int DESTROYED = 6;
   public static final int NOTYETINSTALLED = 7;
   
   /** The name of the service **/
   public ObjectName objectName;
   
   /** State of the service **/
   public int state = NOTYETINSTALLED;
   
   /** Dependent beans **/
   public List<ServiceContext> iDependOn = new LinkedList<ServiceContext>();
   
   /** Beans that depend on me **/
   public List<ServiceContext> dependsOnMe = new LinkedList<ServiceContext>();
   
   /** The fancy proxy to my service calls **/
   public transient Service proxy;

   /** Cause for failure */
   public Throwable problem;
   
   public String getStateString()
   {
      return getStateString(state);
   }
   
   public static String getStateString(int stateInt)
   {
      return stateNames[stateInt];
   }

   public Throwable getProblem()
   {
      return problem;
   }

   public void setProblem(Throwable problem)
   {
      this.problem = problem;
   }

   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(512);
      
      sbuf.append("ObjectName: ").append(objectName);
      sbuf.append("\n  State: ").append(stateNames[state]);

      if (state == FAILED && problem != null) 
      {
         sbuf.append("\n  Reason: ").append(problem.toString());
      }
      printList(sbuf, "\n  I Depend On:", iDependOn);
      printList(sbuf, "\n  Depends On Me:", dependsOnMe);

      // this magically makes *all* jmx-console outputs look better :)
      sbuf.append("\n");
      
      return sbuf.toString();
   }

   @SuppressWarnings("unchecked")
   private void printList(StringBuffer sbuf, String msg, List ctxs)
   {
      if (ctxs.size() > 0)
      {
         // only out stuff, if there are indeed dependencies
         sbuf.append(msg);
         for (Iterator i = ctxs.iterator(); i.hasNext(); )
         {
            ServiceContext sc = (ServiceContext) i.next();
            sbuf.append("\n    ");
            sbuf.append(sc.objectName);
         }
      }
   }
}
