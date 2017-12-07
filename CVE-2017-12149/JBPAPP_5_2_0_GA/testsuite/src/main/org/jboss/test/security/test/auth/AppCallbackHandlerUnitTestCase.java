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
package org.jboss.test.security.test.auth; 

import java.util.HashMap;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback; 

import org.jboss.security.auth.callback.AppCallbackHandler;
import org.jboss.security.auth.callback.ByteArrayCallback;
import org.jboss.security.auth.callback.MapCallback;
import org.jboss.test.JBossTestCase;

//$Id: AppCallbackHandlerUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  Unit Tests the AppCallbackHandler 
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 13, 2006 
 *  @version $Revision: 81036 $
 */
public class AppCallbackHandlerUnitTestCase extends JBossTestCase
{ 
   private NameCallback ncb = null;
   private PasswordCallback pcb = null;
   private MapCallback mcb = null;
   private ByteArrayCallback bacb = null;
   
   public AppCallbackHandlerUnitTestCase(String name)
   {
      super(name); 
   } 
   
   protected void setUp() throws Exception
   {
      ncb = new NameCallback("Enter Username:");
      pcb = new PasswordCallback("Enter Password:", false);
      mcb = new MapCallback();
      bacb = new ByteArrayCallback("Enter data");
   }
   
   protected void tearDown() throws Exception
   {
      ncb = null; 
      pcb = null;
      mcb = null;
      bacb = null;
   }
   
   public void testUserNamePassword() throws Exception
   {
      AppCallbackHandler apc = new AppCallbackHandler("jduke","theduke".toCharArray());
      //Create the Callbacks
      Callback[] cb = new Callback[]{ncb,pcb};
      apc.handle(cb);
      assertTrue("jduke", "jduke".equals(ncb.getName()));
      assertTrue("theduke","theduke".equals(new String(pcb.getPassword())) );
   }
   
   public void testMapCallback() throws Exception
   {
      HashMap hm = new HashMap();
      hm.put("jduke","theduke");
      hm.put("scott","echoman");
      AppCallbackHandler apc = new AppCallbackHandler(hm);
      Callback[] cb = new Callback[]{mcb}; 
      apc.handle(cb);
      assertTrue("jduke=theduke", "theduke".equals(mcb.getInfo("jduke")));
      assertTrue("scott=echoman", "echoman".equals(mcb.getInfo("scott")));
   } 
   
   public void testByteArrayCallback() throws Exception
   {
      AppCallbackHandler apc = new AppCallbackHandler("scott",
                      "echoman".toCharArray(), "Loves Skiing!!!".getBytes());
      //Create the Callbacks
      Callback[] cb = new Callback[]{ncb,pcb,bacb};
      apc.handle(cb);
      assertTrue("scott", "scott".equals(ncb.getName()));
      assertTrue("echoman","echoman".equals(new String(pcb.getPassword())) );
      assertTrue("Loves Skiing!!!", 
            "Loves Skiing!!!".equals(new String(bacb.getByteArray())));
   }
}
