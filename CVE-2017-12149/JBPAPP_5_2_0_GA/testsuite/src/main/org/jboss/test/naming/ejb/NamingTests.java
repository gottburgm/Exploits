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

import javax.ejb.DuplicateKeyException;
import javax.naming.InitialContext;

import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.test.JBossTestCase;
import org.jboss.test.naming.interfaces.TestENCHome;
import org.jboss.test.naming.interfaces.TestENC;
import org.jboss.test.cts.interfaces.CtsCmpLocalHome;
import org.jboss.test.cts.interfaces.CtsCmpLocal;
import org.jboss.test.cts.keys.AccountPK;
import org.jboss.test.cts.ejb.LocalEjbTests;
import org.jboss.logging.Logger;
import junit.framework.Test;

/** Tests of JNDI ENC performance. The following properties are used:
 * 
 * <ul>
 *    <li>ejbRunnerJndiName, the JNDI name of the EJBTestRunning. Defaults
 *    to EJBTestRunnerHome
 *    </li>
 *    <li>encBeanJndiName, the JNDI name of the ENCBean. Defaults to ENCBean.
 *    </li>
 * </ul>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class NamingTests extends EJBTestCase
{
   Logger log = Logger.getLogger(NamingTests.class);

   public NamingTests(String methodName)
   {
      super(methodName);
   }

   public String getEJBRunnerJndiName()
   {
      String jndiName = "EJBTestRunnerHome";
      if( props != null )
         jndiName = props.getProperty("ejbRunnerJndiName", "EJBTestRunnerHome");
      return jndiName;
   }

   public void testENCPerf() throws Exception
   {
      InitialContext ctx = new InitialContext();
      String name = "ENCBean";
      if( props != null )
         name = props.getProperty("encBeanJndiName", "ENCBean");
      TestENCHome home = (TestENCHome) ctx.lookup(name);
      TestENC bean = home.create();
      int iterations = Integer.getInteger("encIterations", 1000).intValue();
      long time = bean.stressENC(iterations);
      log.info("testENCPerf, time="+time);
      bean.remove();
   }
}
