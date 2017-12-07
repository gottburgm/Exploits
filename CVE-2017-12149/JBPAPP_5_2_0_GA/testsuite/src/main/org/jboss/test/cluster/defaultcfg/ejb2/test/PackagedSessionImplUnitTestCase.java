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

package org.jboss.test.cluster.defaultcfg.ejb2.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jboss.ha.hasessionstate.server.PackagedSessionImpl;
import org.jboss.test.JBossTestCase;

public class PackagedSessionImplUnitTestCase extends JBossTestCase
{

   public PackagedSessionImplUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testUnmodifiedExistenceInVM() throws Exception
   {
      long oldTimestamp = System.currentTimeMillis();
      
      tick(); // make sure the system clock changes
      
      byte[] state = new byte[1];
      PackagedSessionImpl psi = new PackagedSessionImpl("Test", state, "Test");
      
      long newTimestamp = psi.unmodifiedExistenceInVM();
      
      assertTrue("Valid initial timestamp", newTimestamp > oldTimestamp);
      
      
      tick(); // make sure the system clock changes
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(psi);
      oos.close();
      
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      psi = (PackagedSessionImpl) ois.readObject();
      ois.close();
      
      oldTimestamp = newTimestamp;
      newTimestamp = psi.unmodifiedExistenceInVM();
      
      assertTrue("Valid timestamp after deserialization", newTimestamp > oldTimestamp);
      
      tick(); // make sure the system clock changes
      
      psi.setState(state);  // use the same state to confirm that the timestamp updates anyway
      
      oldTimestamp = newTimestamp;
      newTimestamp = psi.unmodifiedExistenceInVM();
      
      assertTrue("Valid timestamp after setState()", newTimestamp > oldTimestamp);
      
      tick(); // make sure the system clock changes
      
      PackagedSessionImpl psi2 = new PackagedSessionImpl("Test", state, "Test");
      
      psi.update(psi2);
      
      oldTimestamp = newTimestamp;
      newTimestamp = psi.unmodifiedExistenceInVM();
      
      assertTrue("Valid timestamp after update()", newTimestamp > oldTimestamp);
   }
   
   private void tick()
   {
      final long now = System.currentTimeMillis();
      while (System.currentTimeMillis() <= now)
        try {
            sleep(1);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
   }

}
