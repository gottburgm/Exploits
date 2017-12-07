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
package org.jboss.test.ejbconf.test; // Generated package name

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Collection;
import junit.framework.*;
import org.jboss.test.JBossTestCase;
import org.jboss.test.ejbconf.beans.interfaces.ReadOnly;
import org.jboss.test.ejbconf.beans.interfaces.ReadOnlyHome;
import org.jboss.test.ejbconf.beans.interfaces.ReadOnlyHelper;
import org.jboss.test.ejbconf.beans.interfaces.ReadOnlyHelperHome;


/**
 * ReadOnlyUnitTestCase.java
 *
 *
 * Created: Wed Jan 30 00:16:57 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class ReadOnlyUnitTestCase extends JBossTestCase 
{
   public ReadOnlyUnitTestCase (String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ReadOnlyUnitTestCase.class, "ejbconf-test.jar");
   }

   public void testReadOnly() throws Exception
   {
      ReadOnlyHelperHome rohh = (ReadOnlyHelperHome)getInitialContext().lookup("ReadOnlyHelper");
      ReadOnlyHelper roHelper = rohh.create();
      roHelper.setUp();
      ReadOnlyHome roh = (ReadOnlyHome)getInitialContext().lookup("ReadOnly");
      ReadOnly ro = roh.findByPrimaryKey(new Integer(1));
      assertTrue("ReadOnly didn't get correct initial value", ro.getValue().equals(new Integer(1)));
      try
      {
         ro.setValue(new Integer(2));
         fail("Was able to set read-only field");
      }
      catch(RemoteException e)
      {
         getLog().debug("Failed to set read-only field as expected", e);
      }
   }
   
}// ReadOnlyUnitTestCase
