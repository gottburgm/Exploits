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
package org.jboss.test.profileservice.override.restart.test;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.test.profileservice.override.test.AbstractProfileServiceTest;

/**
 * Test if the properties were applied to a connection factory after restart.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 86425 $
 */
public class ConnectionFactoryRestartUnitTestCase extends AbstractProfileServiceTest
{

   public ConnectionFactoryRestartUnitTestCase(String name)
   {
      super(name);
   }
   
   public void test() throws Exception
   {
      ManagementView managementView = getManagementView();
      ComponentType type = new ComponentType("ConnectionFactory", "Tx");
      ManagedComponent component = managementView.getComponent("JmsXA", type);
      assertNotNull(component);
   
      ManagedProperty property = component.getProperty("max-pool-size");
      assertNotNull(property);
      
      assertEquals(21, ((SimpleValue) property.getValue()).getValue());
   }

}
