/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.jmx.annotated;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.system.ServiceMBeanSupport;

/**
 * AnnotatedJMXPojo.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
@JMX(name="test:name=AnnotatedJMXPojo", exposedInterface=AnnotatedJMXPojoMBean.class, registerDirectly=true)
public class AnnotatedJMXPojo extends ServiceMBeanSupport implements AnnotatedJMXPojoMBean
{
   private boolean createInvoked;
   
   private boolean destroyInvoked;
   
   private boolean startInvoked;
   
   private boolean stopInvoked;

   public boolean isCreateInvoked()
   {
      return createInvoked;
   }

   public boolean isDestroyInvoked()
   {
      return destroyInvoked;
   }

   public boolean isStartInvoked()
   {
      return startInvoked;
   }

   public boolean isStopInvoked()
   {
      return stopInvoked;
   }

   public void reset()
   {
      createInvoked = false;
      destroyInvoked = false;
      startInvoked = false;
      stopInvoked = false;
   }
   
   protected void createService() throws Exception
   {
      log.info("create");
      createInvoked = true;
   }

   protected void destroyService() throws Exception
   {
      log.info("destroy");
      destroyInvoked = true;
   }

   protected void startService() throws Exception
   {
      log.info("start");
      startInvoked = true;
   }

   protected void stopService() throws Exception
   {
      log.info("stop");
      stopInvoked = true;
   }
}
