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
package org.jboss.hibernate.session;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Maintains and exposes, for app usage, the current context bound Hibernate Session.
 * Application code need only deal with the {@link #getSession(java.lang.String)}
 * as the means to retreive the {@link org.hibernate.Session} associated with
 * the current context.
 *
 * @author <a href="mailto:steve@hibernate.org">Steve Ebersole</a>
 * @version $Revision: 81017 $
 *
 * @deprecated Direct use of the new {@link org.hibernate.SessionFactory#getCurrentSession()}
 * method is the preferred approach to managing "transactionally contextual sessions".
 */
public class HibernateContext
{
   /**
    * Retreives an "unmanaged" session against the same underlying jdbc
    * connnection as the session currently bound to the current context for
    * the given JNDI name.  This is simply a convenience method for
    * SessionFactory.openSession({@link #getSession}.connection()).  Unmanaged
    * here means that the returned session is not controlled by the code
    * managing the actually bound session; callers are required to cleanup
    * these sessions manually using {@link #releaseUnmanagedSession}.
    *
    * @param name The "name" of the {@link org.hibernate.SessionFactory}
    *       for which an unmanaged session is requested.
    * @return An unmanaged session.
    * @throws HibernateException If an error occurs opening the new Session.
    * @throws IllegalStateException If unable to locate a managed Session for the current context.
    *
    * @deprecated Given a SessionFactory, sf, obtained from JNDI, this method is equivalent to
    * <pre>sf.openSession( sf.getCurrentSession().connection() )</pre>
    */
   public static Session getUnmanagedSession(String name) throws HibernateException, IllegalStateException
   {
      SessionFactory sf = locateSessionFactory( name );
      return sf.openSession( sf.getCurrentSession().connection() );
   }

   /**
    * Method to release a previously obtained unmanaged session.
    *
    * @param unmanagedSession The unmanaged Session to release.
    * @throws HibernateException If an error occurs releasing the unmanaged Session.
    *
    * @deprecated See {@link #getUnmanagedSession(String)}
    */
   public static void releaseUnmanagedSession(Session unmanagedSession) throws HibernateException
   {
      unmanagedSession.close();
   }

   /**
    * Retreives the session currently bound to the current context.
    *
    * @param name The "name" of the {@link org.hibernate.SessionFactory}
    *       for which a session is requested.
    * @return The current session.
    *
    * @deprecated This call is equivalent to <pre>
    * ( ( SessionFactory ) new InitialContext().lookup( name ) ).getCurrentSession()
    * </pre>.
    * @see org.hibernate.SessionFactory#getCurrentSession()
    */
   public static Session getSession(String name)
   {
      return locateSessionFactory( name ).getCurrentSession();
   }

   private static SessionFactory locateSessionFactory(String name) throws HibernateException
   {
      InitialContext context = null;
      try
      {
         context = new InitialContext();
         return ( SessionFactory ) context.lookup(name);
      }
      catch( NamingException e )
      {
         throw new HibernateException( "Unable to locate SessionFactory in JNDI under name [" + name + "]", e );
      }
      finally
      {
         release( context );
      }
   }

   private static void release(InitialContext ctx)
   {
      if (ctx != null)
      {
         try
         {
            ctx.close();
         }
         catch( Throwable ignore )
         {
            // ignore
         }
      }
   }
}
