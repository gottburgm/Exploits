/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.refs.client;

import java.util.Properties;
import javax.ejb.EJB;

import org.jboss.logging.Logger;
import org.jboss.test.refs.ejbs.StatefulIF;
import org.jboss.test.refs.ejbs.StatelessIF;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class Client
{
   private static Logger log = Logger.getLogger(Client.class);
   @EJB(name = "ejb/StatefulBean", beanInterface = StatefulIF.class)
   private static StatefulIF statefulBean;

   @EJB(name = "ejb/StatelessBean", beanInterface = StatelessIF.class)
   private static StatelessIF statelessBean;

   private Properties props;

   public static StatefulIF getStatefulBean()
   {
      return statefulBean;
   }
   public static StatelessIF getStatelessBean()
   {
      return statelessBean;
   }

   public static void main(String[] args)
      throws Exception
   {
      Client c = new Client();
      int test = 1;
      if(args.length > 0) {
         test = 2;
      }
      switch(test)
      {
         case 1:
            c.test1();
            break;
         case 2:
         default:
            c.test2();
            break;
      }
   }

   public void test1() throws Exception
   {
      log.info("Begin test1");
      statefulBean.init(props);
      statefulBean.test1();
   }
   public void test2() throws Exception
   {
      log.info("Begin test2");
      try
      {
         statefulBean.test2();
      }
      catch (Exception e)
      {
         log.info("", e);
      }
   }

}
