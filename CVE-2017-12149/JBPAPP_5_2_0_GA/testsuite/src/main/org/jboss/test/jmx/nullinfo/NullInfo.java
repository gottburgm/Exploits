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
package org.jboss.test.jmx.nullinfo;

import javax.management.*;

public class NullInfo implements DynamicMBean{

    static org.jboss.logging.Logger log =
       org.jboss.logging.Logger.getLogger(NullInfo.class);
   
   //This is what kills it, the others aren't even called
   public MBeanInfo getMBeanInfo(){
      log.debug("Returning null from getMBeanInfo");
      return null;
   }
   
   public Object invoke(String action, Object[] params, String[] sig){
      log.debug("Returning null from invoke");
      return null;
   }
   
   public Object getAttribute(String attrName){
      log.debug("Returning null from getAttribute");
      return null;
   }
   public AttributeList getAttributes(String[] attrs){
      log.debug("Returning null from getAttributes");
      return null;
   }
   public void setAttribute(Attribute attr){
      log.debug("setAttribute called");
   }
   public AttributeList setAttributes(AttributeList attrs){
      log.debug("Returning null from setAttributes");
      return null;
   }
}
