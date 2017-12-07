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
package org.jboss.test.hibernate.model.v2;

import java.util.List;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.cfg.Configuration;

/**
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $ */
public class PersonBean implements SessionBean
{
   private SessionContext context;

   public void ejbCreate()
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext context)
   {
      this.context = context;
   }

   public void init( )
      throws HibernateException
   {
      Configuration cfg = new Configuration().configure();
      SchemaExport se = new SchemaExport(cfg);
      se.create(true, true);
   }
   public void sessionInit( )
      throws HibernateException
   {
      Configuration cfg = new Configuration().configure();
      SessionFactory sf = cfg.buildSessionFactory();
      System.out.println("Initialized session: "+sf);
   }

   public Person loadUser(long id) throws HibernateException
   {
      return loadUser( new Long(id) );
   }

   public Person loadUser(Long id) throws HibernateException
   {
      return (Person) getSession().load(Person.class, id);
   }

   public List listPeople() throws HibernateException
   {
      return getSession()
            .createQuery("from Person")
            .list();
   }

   public Person loadUser(String name) throws HibernateException
   {
      return (Person) getSession()
            .createQuery("from Person as p where p.name = :name")
            .setString("name", name)
            .uniqueResult();
   }

   public Person storeUser(Person user) throws HibernateException
   {
      getSession().saveOrUpdate(user);
      getSession().flush();
      return user;
   }

   private Session getSession()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         String sessionName = (String) ctx.lookup("java:/comp/env/HibernateFactory");
         SessionFactory sf = (SessionFactory) ctx.lookup(sessionName);
         return sf.getCurrentSession();
      }
      catch( HibernateException e )
      {
         throw e;
      }
      catch( NamingException e )
      {
         throw new HibernateException(e);
      }
   }

}
