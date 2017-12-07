/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.refs.ejbs2;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.test.refs.ejbs.StatelessIF;
import org.jboss.test.refs.ejbs.StatelessIFExt;
import org.jboss.test.util.Debug;

/**
 * A stateless bean that references beans in other deployments
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
@Stateless(mappedName="refs/ejbs2/StatelessBean")
@Remote(StatelessTest.class)
public class StatelessBean2
{
   @Resource SessionContext ctx;
   @EJB
   StatelessIF bean1;
   @EJB(beanName="StatelessBean")
   StatelessIF bean2;
   @EJB(beanInterface=StatelessIFExt.class)
   StatelessIF bean3;
   @EJB(beanInterface=StatelessIFExt.class, beanName="StatelessBean")
   StatelessIF bean4;
   @EJB(beanInterface=StatelessIFExt.class, beanName="refs-ejb.jar#StatelessBean")
   StatelessIF bean5;

   public void validate()
      throws Exception
   {
      if(bean1 == null)
         throw new IllegalStateException("bean1 is null");
      if(bean2 == null)
         throw new IllegalStateException("bean2 is null");
      if(bean3 == null)
         throw new IllegalStateException("bean3 is null");
      if((bean3 instanceof StatelessIFExt) == false)
         throw new IllegalStateException("bean3 is not a StatelessIFExt");
      if(bean4 == null)
         throw new IllegalStateException("bean4 is null");
      if(bean5 == null)
         throw new IllegalStateException("bean5 is null");
      validateENCLookup();
      validateSessionContextLookup();
   }
   private void validateSessionContextLookup()
   {
      if(ctx == null)
         throw new IllegalStateException("SessionContext is null");
      for(int n = 1; n <= 5; n ++)
      {
         StatelessIF bean = (StatelessIF) ctx.lookup(getClass().getName()+"/bean"+n);
         if(bean == null)
            throw new IllegalStateException("SessionContext.lookup(bean"+n+") is null");
      }
   }
   private void validateENCLookup()
      throws Exception
   {
      InitialContext ic = new InitialContext();
      Context enc = (Context) ic.lookup("java:comp/env");
      StatelessIF bean1enc = null;
      StatelessIF bean2enc = null;
      StatelessIF bean3enc = null;
      StatelessIF bean4enc = null;
      StatelessIF bean5enc = null;
      try
      {
         bean1enc = (StatelessIF) enc.lookup(getClass().getName()+"/bean1");
         bean2enc = (StatelessIF) enc.lookup(getClass().getName()+"/bean2");
         bean3enc = (StatelessIF) enc.lookup(getClass().getName()+"/bean3");
         bean4enc = (StatelessIF) enc.lookup(getClass().getName()+"/bean4");
         bean5enc = (StatelessIF) enc.lookup(getClass().getName()+"/bean5");
      }
      catch(NameNotFoundException e)
      {
      }
      if(bean1enc == null)
      {
         StringBuffer tmp = new StringBuffer("java:comp/env/StatelessBean2/bean1 is null\n");
         Debug.list(enc, " ", tmp, true);
         throw new IllegalStateException(tmp.toString());
      }
      if(bean2enc == null)
      {
         StringBuffer tmp = new StringBuffer("java:comp/env/StatelessBean2/bean2 is null\n");
         Debug.list(enc, " ", tmp, true);
         throw new IllegalStateException(tmp.toString());
      }
      if(bean3enc == null)
      {
         StringBuffer tmp = new StringBuffer("java:comp/env/StatelessBean2/bean3 is null\n");
         Debug.list(enc, " ", tmp, true);
         throw new IllegalStateException(tmp.toString());
      }
      if(bean4enc == null)
      {
         StringBuffer tmp = new StringBuffer("java:comp/env/StatelessBean2/bean4 is null\n");
         Debug.list(enc, " ", tmp, true);
         throw new IllegalStateException(tmp.toString());
      }
      if(bean5enc == null)
      {
         StringBuffer tmp = new StringBuffer("java:comp/env/StatelessBean2/bean5 is null\n");
         Debug.list(enc, " ", tmp, true);
         throw new IllegalStateException(tmp.toString());
      }
   }
}
