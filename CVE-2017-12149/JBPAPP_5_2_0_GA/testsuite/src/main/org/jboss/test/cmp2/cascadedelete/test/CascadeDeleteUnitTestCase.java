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
package org.jboss.test.cmp2.cascadedelete.test;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.cmp2.cascadedelete.ejb.Facade;
import org.jboss.test.cmp2.cascadedelete.ejb.FacadeHome;


/**
 * A CascadeDeleteUnitTestCase.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 85945 $
 */
public class CascadeDeleteUnitTestCase extends JBossTestCase
{
   public CascadeDeleteUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(CascadeDeleteUnitTestCase.class, "cmp2-cascadedelete.jar");
   }

   public void testScenario1() throws Exception
   {      
      InitialContext ic = new InitialContext();
      FacadeHome fh = (FacadeHome) PortableRemoteObject.narrow(ic.lookup("Facade"), FacadeHome.class);
      Facade facade = fh.create();
      facade.createScenario1();
      facade.deleteBeans();
   }

   public void testScenario2() throws Exception
   {      
      InitialContext ic = new InitialContext();
      FacadeHome fh = (FacadeHome) PortableRemoteObject.narrow(ic.lookup("Facade"), FacadeHome.class);
      Facade facade = fh.create();
      facade.createScenario2();
      facade.deleteBeans();
   }
}
