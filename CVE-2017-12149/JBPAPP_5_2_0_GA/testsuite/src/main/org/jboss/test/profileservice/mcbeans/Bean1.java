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
package org.jboss.test.profileservice.mcbeans;

import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105321 $
 */
@ManagementObject(componentType=@ManagementComponent(type="MCBean",subtype=""),
      description="A managed mc bean")
public class Bean1
{
   private String prop1;
   private float prop2;
   private Double prop3;
   private long calls;

   @ManagementProperty
   public String getProp1()
   {
      return prop1;
   }
   public void setProp1(String prop1)
   {
      this.prop1 = prop1;
   }
   @ManagementProperty
   public float getProp2()
   {
      return prop2;
   }
   public void setProp2(float prop2)
   {
      this.prop2 = prop2;
   }
   @ManagementProperty
   public Double getProp3()
   {
      return prop3;
   }
   public void setProp3(Double prop3)
   {
      this.prop3 = prop3;
   }
   @ManagementProperty(use={ViewUse.STATISTIC})
   public long getCalls()
   {
      return calls;
   }

   @ManagementOperation
   public synchronized void op1()
   {
      calls ++;
   }
}
