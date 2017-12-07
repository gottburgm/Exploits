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
package org.jboss.test.security.test;

import java.rmi.RemoteException;
import java.lang.reflect.Method;
import javax.security.jacc.EJBMethodPermission;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import junit.framework.TestCase;

/** Tests of the JAAC EJB*Permissions
 *
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 81036 $
 */
public class EJBPermissionUnitTestCase
   extends TestCase
{
   static interface AHome extends EJBHome
   {
      public void create() throws CreateException, RemoteException;
   }
   static interface ARemote extends EJBObject
   {
      public void methodX() throws RemoteException;
      public void methodX(int x) throws RemoteException;
   }

   public EJBPermissionUnitTestCase(String name)
   {
      super(name);
   }

   /** Tests of the EJBMethodPermission(String name, String actions)
    * @throws Exception
    */ 
   public void testCtor1() throws Exception
   {
      EJBMethodPermission p = new EJBMethodPermission("someEJB", null);
      String actions = p.getActions();
      assertTrue("actions("+actions+") == null", actions == null);

      p = new EJBMethodPermission("someEJB", "methodX");
      actions = p.getActions();
      assertTrue("actions("+actions+") == methodX", actions.equals("methodX"));

      p = new EJBMethodPermission("someEJB", "methodX,,int");
      actions = p.getActions();
      assertTrue("actions("+actions+") == methodX,,int",
         actions.equals("methodX,,int"));

      p = new EJBMethodPermission("someEJB", "methodX,ServiceEndpoint,int");
      actions = p.getActions();
      assertTrue("actions("+actions+") == methodX,ServiceEndpoint,int",
         actions.equals("methodX,ServiceEndpoint,int"));

      p = new EJBMethodPermission("someEJB", "methodX,ServiceEndpoint,");
      actions = p.getActions();
      assertTrue("actions("+actions+") == methodX,ServiceEndpoint,",
         actions.equals("methodX,ServiceEndpoint,"));
   }

   /** Tests of EJBMethodPermission(String ejbName, String methodInterface, Method method)
    * 
    * @throws Exception
    */ 
   public void testCtor2() throws Exception
   {
      Class[] createSig = {};
      Method method = AHome.class.getMethod("create", createSig);
      EJBMethodPermission p = new EJBMethodPermission("someEJB", "Home", method);
      String actions = p.getActions();
      assertTrue("actions("+actions+") == create,Home,",
         actions.equals("create,Home,"));

      Class[] methodXSig = {int.class};
      Method methodX = ARemote.class.getMethod("methodX", methodXSig);
      p = new EJBMethodPermission("someEJB", "Remote", methodX);
      actions = p.getActions();
      assertTrue("actions("+actions+") == create,Home,",
         actions.equals("methodX,Remote,int"));      
   }

   /** Tests of EJBMethodPermission(String ejbName, String methodName,
      String methodInterface, String[] methodParams)
    @throws Exception
    */ 
   public void testCtor3() throws Exception
   {
      String methodName = null;
      String methodInterface = null;
      String[] methodParams = null;
      EJBMethodPermission p = new EJBMethodPermission("someEJB", methodName,
         methodInterface, methodParams);
      String actions = p.getActions();
      assertTrue("actions("+actions+") == null", actions == null);

      methodName = "methodX";
      methodInterface = null;
      methodParams = null;
      p = new EJBMethodPermission("someEJB", methodName,
         methodInterface, methodParams);
      actions = p.getActions();
      assertTrue("actions("+actions+") == methodX", actions.equals("methodX"));

      methodName = "methodX";
      methodInterface = null;
      methodParams = new String[0];
      p = new EJBMethodPermission("someEJB", methodName,
         methodInterface, methodParams);
      actions = p.getActions();
      assertTrue("actions("+actions+") == methodX,,", actions.equals("methodX,,"));

      methodName = "methodX";
      methodInterface = null;
      methodParams = new String[]{"int"};
      p = new EJBMethodPermission("someEJB", methodName,
         methodInterface, methodParams);
      actions = p.getActions();
      assertTrue("actions("+actions+") == methodX,,int",
         actions.equals("methodX,,int"));

      methodName = "methodX";
      methodInterface = "ServiceEndpoint";
      methodParams = new String[]{"int"};
      p = new EJBMethodPermission("someEJB", methodName,
         methodInterface, methodParams);
      actions = p.getActions();
      assertTrue("actions("+actions+") == methodX,ServiceEndpoint,int",
         actions.equals("methodX,ServiceEndpoint,int"));
   }

   public void testImpliesPermission() throws Exception
   {
      EJBMethodPermission p0 = new EJBMethodPermission("someEJB", null);
      EJBMethodPermission p1 = new EJBMethodPermission("someEJB", "methodX");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new EJBMethodPermission("someEJB", "methodX");
      assertTrue("p0.implies(p1)", p0.implies(p1));
      
      p0 = new EJBMethodPermission("someEJB", null);
      p1 = new EJBMethodPermission("someEJB", "methodX,Remote");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p1 = new EJBMethodPermission("someEJB", "methodX,Remote,");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new EJBMethodPermission("someEJB", "methodX");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new EJBMethodPermission("someEJB", "methodX,Remote");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new EJBMethodPermission("someEJB", null);
      p1 = new EJBMethodPermission("someEJB", "methodX,Local,int,java.lang.String");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new EJBMethodPermission("someEJB", "methodX");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new EJBMethodPermission("someEJB", "methodX,,int,java.lang.String");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new EJBMethodPermission("someEJB", "methodX,Local");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new EJBMethodPermission("someEJB", ",,int,java.lang.String");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new EJBMethodPermission("someEJB", "methodX,Local,int,java.lang.String");
      assertTrue("p0.implies(p1)", p0.implies(p1));

      p0 = new EJBMethodPermission("someEJB", "methodX,Local,int,java.lang.String");
      assertTrue("p0.implies(p1)", p0.implies(p1));
      
      p0 = new EJBMethodPermission("someEJB", "methodX");
      p1 = new EJBMethodPermission("someEJB", "methodX,,");
      assertTrue("p0.implies(p1)", p0.implies(p1));
      
      p0 = new EJBMethodPermission("ejbName", null, null, new String[0]);        
      p1 = new EJBMethodPermission("ejbName", "create", dummyInterface.class.getMethods()[0]); 
      assertTrue("p0.implies(p1)", p0.implies(p1));
   }

   public void testNotImpliesPermission() throws Exception
   {
      EJBMethodPermission p0 = new EJBMethodPermission("someEJB", "methodX");
      EJBMethodPermission p1 = new EJBMethodPermission("someEJB", null);
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new EJBMethodPermission("someEJB", "methodX");
      p1 = new EJBMethodPermission("someEJB", "methodY");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new EJBMethodPermission("someEJB", "methodX,,");
      p1 = new EJBMethodPermission("someEJB", "methodX");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new EJBMethodPermission("someEJB", "methodX,Local");
      p1 = new EJBMethodPermission("someEJB", "methodX,Remote");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new EJBMethodPermission("someEJB", "methodX,,int");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new EJBMethodPermission("someEJB", "methodX,Remote");
      p1 = new EJBMethodPermission("someEJB", "methodX,Local,int,java.lang.String");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new EJBMethodPermission("someEJB", "methodX,Local,int");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new EJBMethodPermission("someEJB", "methodX,,float,java.lang.String");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);

      p0 = new EJBMethodPermission("someEJB", ",,int,java.lang.String2");
      assertTrue("! p0.implies(p1)", p0.implies(p1) == false);
      
      p0 = new EJBMethodPermission("ejbName", null, null, new String[0]);
      p1 = new EJBMethodPermission("ejbName", "a", "LocalHome", 
              new String[]{"java.lang.String"});
      assertTrue("!p0.implies(p1)", p0.implies(p1) == false);
   }
   
   public interface dummyInterface {  void noop(); }
}
