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
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.StatefulSessionEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.invocation.Invocation;

/**
 * This interceptor synchronizes a HA SFSB instance with its underlying persistent manager.
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @version $Revision: 81001 $
 *
 * <p><b>Revisions:</b>
  * <p><b>2002/07/28: <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a></b>
 * <ol>
 *   <li>Added isModified check before replication (TaskId 57031)
 * </ol>
*/
public class StatefulHASessionSynchronisationInterceptor
   extends AbstractInterceptor
{
   protected StatefulSessionContainer container;
   
   // optional isModified method used to avoid unecessary state replication
   //
   protected Method isModified = null;

   public void start () throws Exception
   {
      // Lookup the isModified method if it exists
      //
      try
      {
         isModified = this.container.getBeanClass().getMethod("isModified", new Class[0]);
         if (!isModified.getReturnType().equals(Boolean.TYPE)) {
            isModified = null; // Has to have "boolean" as return type!
            log.warn("Found isModified method, but return type is not boolean; ignoring");
         }
         else 
         {
            log.debug("Using isModified method: " + isModified);
         }
      }
      catch (NoSuchMethodException ignored) {}
   }
   
   public Object invokeHome (Invocation mi)
      throws Exception
   {
      EnterpriseContext ctx = (EnterpriseContext)mi.getEnterpriseContext ();
      
      try
      {
         // Invoke through interceptors
         return getNext ().invokeHome (mi);
      }
      finally
      {
         if ( (ctx != null) && (ctx.getId () != null) )
            // Still a valid instance and instance not removed
            //
         {
            // Everything went ok (at least no J2EE problem) and the instance will most probably be called
            // many more times. Consequently, we need to synchronize the state of our bean instance with
            // our persistant store (which will forward this to its HASessionState implementation) for clustering
            // behaviour. This is only necessary for "create" calls (which is the case because ctx.getId() != null)
            //
            synchronizeState (ctx);
         }
      }
   }
   
   public Object invoke (Invocation mi)
      throws Exception
   {
      EnterpriseContext ctx = (EnterpriseContext)mi.getEnterpriseContext ();
      
      try
      {
         // Invoke through interceptors
         return getNext ().invoke (mi);
      }
      catch (RemoteException e)
      {
         ctx = null;
         throw e;
      }
      catch (RuntimeException e)
      {
         ctx = null;
         throw e;
      }
      catch (Error e)
      {
         ctx = null;
         throw e;
      }
      finally
      {
         if ( (ctx != null) && (ctx.getId () != null) )
            // Still a valid instance and instance not removed
            //
         {
            // Everything went ok (at least no J2EE problem) and the instance will most probably be called
            // many more times. Consequently, we need to synchronize the state of our bean instance with
            // our persistant store (which will forward this to its HASessionState implementation) for clustering
            // behaviour.
            //
            
            if(isModified == null)
            {
               synchronizeState (ctx);
            }
            else
            {
               Boolean modified = (Boolean) isModified.invoke (ctx.getInstance (), new Object[0]);
               if (modified.booleanValue ())
                  synchronizeState (ctx);
            }
         }
      }
   }
   
   protected void synchronizeState (EnterpriseContext ctx) throws Exception
   {
      ((HAPersistentManager)container.getPersistenceManager ()).synchroSession ((StatefulSessionEnterpriseContext)ctx);
   }
   
   /**
    * This callback is set by the container so that the plugin may access it
    *
    * @param container    The container using this plugin.
    */
   public void setContainer (Container container)
   {
      this.container = (StatefulSessionContainer)container;
   }
   
   public  Container getContainer ()
   {
      return container;
   }
   
}

