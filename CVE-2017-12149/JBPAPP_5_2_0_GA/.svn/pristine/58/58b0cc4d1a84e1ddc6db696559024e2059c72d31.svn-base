/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.timer.ejb;

import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.management.ObjectName;

import org.jboss.ejb.Container;
import org.jboss.ejb.txtimer.EJBTimerService;
import org.jboss.ejb.txtimer.EJBTimerServiceLocator;
import org.jboss.mx.util.ObjectNameConverter;

/**
 * TimerEntityExtBean.
 * 
 * @author Galder Zamarreño
 */
public class TimerEntityExtBean extends TimerEntityBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7428841333866106621L;
   
   private EntityContext ctx;
   
   public boolean hasTimerService(String jndi)
   {
      try
      {
         EJBTimerService service = EJBTimerServiceLocator.getEjbTimerService();
         String name = Container.BASE_EJB_CONTAINER_NAME + ",jndiName=" + jndi;
         ObjectName containerId = ObjectNameConverter.convert(name);
         return service.getTimerService(containerId, ctx.getPrimaryKey()) != null;         
      }
      catch(Exception e)
      {
         throw new EJBException("Unable to verify whether entity bean has a timer service associated", e);
      }
   }
   
   public void setEntityContext(EntityContext ctx)
   {
      super.setEntityContext(ctx);
      this.ctx = ctx;
   }
}
