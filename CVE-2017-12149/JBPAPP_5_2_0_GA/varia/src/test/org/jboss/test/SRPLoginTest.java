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
package org.jboss.test;

import java.net.URL;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.callback.UsernamePasswordHandler;

/** A test of the SRPLogin module

@see org.jboss.security.srp.jaas.SRPLoginModule

@author Scott.Stark@jboss.org
@version $Revision: 81038 $
*/
public class SRPLoginTest extends junit.framework.TestCase
{
    public SRPLoginTest(String name)
    {
        super(name);
    }

    /** Create a SecurityPolicy from a xml policy file and install it as the
        JAAS Policy and Configuration implementations.
    */
    protected void setUp() throws Exception
    {
        // Create a subject security policy
        String policyName = "tst-policy.xml";
        URL policyURL = getClass().getClassLoader().getResource(policyName);
        if( policyURL == null )
            throw new IllegalStateException("Failed to find "+policyName+" in classpath");
       /*
        SecurityPolicyParser policyStore = new SecurityPolicyParser(policyURL);
        SecurityPolicy policy = new SecurityPolicy(policyStore);
        policy.refresh();
        Policy.setPolicy(policy);
        Configuration.setConfiguration(policy.getLoginConfiguration());
        */
    }

    public void testLogin()
    {
        CallbackHandler handler = new UsernamePasswordHandler("scott", "stark".toCharArray());
        try
        {
            LoginContext lc = new LoginContext("srp-login", handler);
            lc.login();
            Subject subject = lc.getSubject();
            System.out.println("Subject="+subject);
        }
        catch(LoginException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public static void main(String args[])
    {
        try
        {
            SRPLoginTest tst = new SRPLoginTest("main");
            tst.setUp();
        }
        catch(Exception e)
        {
            e.printStackTrace(System.out);
        }
    }
}
