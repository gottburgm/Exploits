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
package org.jboss.test.marshal.test;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.marshal.TestPayload;
import org.jboss.test.marshal.interfaces.MarshalSession;
import org.jboss.test.marshal.interfaces.MarshalSessionHome;

import java.rmi.MarshalException;

/**
 * Test that unmarshall exception is thrown.
 *
 * @author  <a href="mailto:tom@jboss.org">Tom Elrod</a>
 * @version $Revision: 81083 $
 */
public class EjbUnMarshalUnitTestCase extends JBossTestCase
{
   public EjbUnMarshalUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite()
      throws Exception
   {
      return getDeploySetup(EjbUnMarshalUnitTestCase.class, "test-ejb-unmarshal.jar");
   }

   /**
    * This is a simple test which calls on the MarshalSession bean, passing a TestPayload
    * object as the parameter.  The deployed jar with the MarshalSession should NOT contain
    * the TestPayload class, thus cause there to be a UnmarshalException to be thrown.
    * @throws Exception
    */
   public void testUnMarshalException() throws Exception
   {
      MarshalSessionHome marshalHome = (MarshalSessionHome)getInitialContext().lookup("marshal/MarshallSession");
      MarshalSession marshalSession = marshalHome.create();

      TestPayload payload = new TestPayload();
      try
      {
         marshalSession.testMethod(payload);
         assertTrue("Call on MarshalSession.testMethod() should have thrown UnmarshalException, but did not", false);
      }
      catch (MarshalException e)
      {
         assertTrue(true);
      }
      catch (Throwable thr)
      {
         thr.printStackTrace();
         throw new Exception(thr);
      }

   }

}
