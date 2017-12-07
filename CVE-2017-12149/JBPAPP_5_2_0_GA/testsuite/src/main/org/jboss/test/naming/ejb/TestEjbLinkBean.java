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
package org.jboss.test.naming.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.jboss.test.naming.interfaces.TestEjbLinkHome;
import org.jboss.test.naming.interfaces.TestEjbLink;
import org.jboss.test.naming.interfaces.TestEjbLinkLocalHome;
import org.jboss.test.naming.interfaces.TestEjbLinkLocal;
import org.jboss.test.util.Debug;

/** A bean that tests ejb-link works 

@author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian.Brock</a>
@version $Revision: 81036 $
*/
public class TestEjbLinkBean implements SessionBean
{
   org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(getClass());

    public void ejbCreate() throws CreateException
    {
    }

// --- Begin SessionBean interface methods
    public void ejbActivate()
    {
    }
    
    public void ejbPassivate()
    {
    }
    
    public void ejbRemove()
    {
    }

    public void setSessionContext(SessionContext sessionContext) throws EJBException
    {
    }

    public String testEjbLinkCaller(String jndiName)
    {
       try
       {
          InitialContext initial = new InitialContext();
          Object object = initial.lookup(jndiName);
          log.debug("jndiName="+jndiName);

          StringBuffer results = new StringBuffer("testEjbLinkCaller Proxy info\n");
          Debug.displayClassInfo(object.getClass(), results);
          log.debug(results.toString());

          results.setLength(0);
          results.append("testEjbLinkCaller TestEjbLinkLocalHome.class info\n");
          Debug.displayClassInfo(TestEjbLinkLocalHome.class, results);
          log.debug(results.toString());

          TestEjbLinkHome home = 
            (TestEjbLinkHome) PortableRemoteObject.narrow(object, TestEjbLinkHome.class);
          TestEjbLink bean = home.create();
          return bean.testEjbLinkCalled();
       }
       catch (Exception e)
       {
          log.debug("failed", e);
          return "Failed";
       }
    }

    public String testEjbLinkCallerLocal(String jndiName)
    {
       try
       {
          InitialContext initial = new InitialContext();
          Object object = initial.lookup(jndiName);
          log.debug("jndiName="+jndiName);

          StringBuffer results = new StringBuffer("testEjbLinkCallerLocal Proxy info\n");
          Debug.displayClassInfo(object.getClass(), results);
          log.debug(results.toString());

          results.setLength(0);
          results.append("testEjbLinkCallerLocal TestEjbLinkLocalHome.class info\n");
          Debug.displayClassInfo(TestEjbLinkLocalHome.class, results);
          log.debug(results.toString());

          TestEjbLinkLocalHome home = 
            (TestEjbLinkLocalHome) PortableRemoteObject.narrow(object, TestEjbLinkLocalHome.class);
          TestEjbLinkLocal bean = home.create();
          return bean.testEjbLinkCalled();
       }
       catch (Exception e)
       {
          log.debug("failed", e);
          return "Failed";
       }
    }

    public String testEjbLinkCalled()
    {
       return "Works";
    }

}
