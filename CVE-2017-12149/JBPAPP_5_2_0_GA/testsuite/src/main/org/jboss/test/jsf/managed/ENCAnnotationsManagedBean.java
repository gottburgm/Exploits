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
package org.jboss.test.jsf.managed;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.ejb.EJB;
import javax.faces.FacesException;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.jboss.test.web.mock.EntityHome;
import org.jboss.test.web.mock.StatelessSessionHome;
import org.jboss.test.web.mock.StatelessSessionLocalHome;

/**
 * Tests of a managed bean for java:comp/env jndi settings and resource injection specified via javaee5 annotations.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
@Resources({
   @Resource(name="mail/DefaultMail", type=javax.mail.Session.class, mappedName="java:/Mail"),
   @Resource(name="mdr/ConsumesLink", type=javax.jms.Queue.class, mappedName="MockQueueA"),
   @Resource(name="mdr/ProducesLink", type=javax.jms.Topic.class, mappedName="MockTopicA")
})
public class ENCAnnotationsManagedBean
{
   /** The logger. */
   private static final Logger log = Logger.getLogger(ENCAnnotationsManagedBean.class);

   @Resource(name="jms/QueFactory", mappedName="java:/ConnectionFactory")
   QueueConnectionFactory queueFactory;
   @Resource(name="TestQueue", mappedName="MockQueueB")
   Queue testQueue;
   @Resource(name="mdr/ConsumesProducesJNDIName", mappedName="MockQueueA")
   Queue refQueue;

   @Resource(name="jdbc/DefaultDS", mappedName="java:/MockDS")
   DataSource ds;
   @EJB(name="ejb/bean3", beanInterface=StatelessSessionHome.class, 
         mappedName="jbosstest/ejbs/UnsecuredEJB")
   StatelessSessionHome sshome;
   @EJB(name="ejb/CtsBmp", beanInterface=EntityHome.class, 
         mappedName="jbosstest/ejbs/CtsBmp")
   EntityHome entityHome;
   @EJB(name="ejb/local/bean3", beanInterface=StatelessSessionLocalHome.class, 
         mappedName="jbosstest/ejbs/local/ENCBean1")
   StatelessSessionLocalHome localHome;

   @Resource(name="url/JBossHome", mappedName="http://www.jboss.org")
   java.net.URL url;

   @Resource(name="Ints/i0", mappedName="0")
   Integer i0;
   @Resource(name="Ints/i1", mappedName="1")
   Integer i1;
   @Resource(name="Floats/f0", mappedName="0.0")
   Float f0;
   @Resource(name="Floats/f1", mappedName="1.1")
   Float f1;
   @Resource(name="Strings/s0", mappedName="String0")
   String s0;
   @Resource(name="Strings/s1", mappedName="String1")
   String s1;
   @Resource(name="ejb/catalog/CatalogDAOClass", mappedName="com.sun.model.dao.CatalogDAOImpl")
   String ejbName;
   
   public boolean getTest()
   {
      ENCTester tester = new ENCTester(log);
      tester.testENC();

      if( queueFactory == null )
         throw new FacesException("queueFactory is not injected");
      if( testQueue == null )
         throw new FacesException("testQueue is not injected");
      if( testQueue == null )
         throw new FacesException("testQueue is not injected");
      if(url == null)
         throw new FacesException("url is not injected");
      
      return true;
   }
   
}

