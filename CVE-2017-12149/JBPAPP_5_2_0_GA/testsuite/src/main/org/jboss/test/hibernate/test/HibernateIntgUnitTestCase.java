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
package org.jboss.test.hibernate.test;

import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.hibernate.ejb.interfaces.ProfileService;
import org.jboss.test.hibernate.ejb.interfaces.ProfileServiceHome;
import org.jboss.test.hibernate.ejb.interfaces.ProfileServiceUtil;
import org.jboss.test.hibernate.model.Name;
import org.jboss.test.hibernate.model.User;

/**
 * Implementation of HibernateIntgUnitTestCase.
 *
 * @author Steve Ebersole
 */
public class HibernateIntgUnitTestCase extends JBossTestCase
{
   public HibernateIntgUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   /** Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(HibernateIntgUnitTestCase.class, "hib-test.ear");
   }

   public void testRedeployment() throws Throwable
   {
      Throwable initialThrowable = null;

      // Do some work
      try
      {
         ProfileServiceHome home = ProfileServiceUtil.getHome();
         ProfileService service = null;
         try
         {
            service = home.create();

            User user = new User();
            user.setEmail("nobody@nowhere.com");
            user.setName( new Name() );
            user.getName().setFirstName("John");
            user.getName().setInitial( new Character('Q') );
            user.getName().setLastName("Public");
            user.setPassword("password");
            user.setTimeOfCreation( new GregorianCalendar() );
            user.setHandle("myHandle");

            Long savedUserId = service.storeUser( user ).getId();
            getLog().info("User created with id = " + savedUserId );

            // make *sure* it gets loaded into cache.  This is to check
            // that JBossCache as 2nd-level cache is properly releasing
            // resources on SF shutdown; I have manually verified this is
            // the case w/o JBossCache as the 2nd-level cache (i.e. this
            // test case passes w/o JBossCache in the mix).
            List users = service.listUsers();
            assertNotNull( users );
            assertEquals( "Incorrect result size", 1, users.size() );
         }
         finally
         {
            if ( service != null )
            {
               try
               {
                  service.remove();
               }
               catch( Throwable t )
               {
               }
            }
         }
      }
      catch( Throwable t )
      {
         // ignore; does not really matter if this stuff fails/succeeds
         // simply store the original failure so that we can use it later
         initialThrowable = t;
      }

      // force a redeploy
      delegate.redeploy( "hib-test.ear" );

      // then, do some more work...
      ProfileServiceHome home = ProfileServiceUtil.getHome();
      ProfileService service = null;
      try
      {
         service = home.create();

         User user = new User();
         user.setEmail("nobody@nowhere.com");
         user.setName( new Name() );
         user.getName().setFirstName("John");
         user.getName().setInitial( new Character('Q') );
         user.getName().setLastName("Public");
         user.setPassword("password");
         user.setTimeOfCreation( new GregorianCalendar() );
         user.setHandle("myHandle");

         Long savedUserId = service.storeUser( user ).getId();
         try
         {
            getLog().info("User created with id = " + savedUserId );

            List users = service.listUsers();
            assertNotNull( users );
            assertEquals( "Incorrect result size", 1, users.size() );
         }
         finally
         {
            getLog().info("About to delete user with id = " + savedUserId);
            service.deleteUser(savedUserId);
            getLog().info("User with id = " + savedUserId + " successfully deleted.");
         }
      }
      catch( Throwable t )
      {
         // it is possible for the initial code block (b4 the redeploy) and this
         // (after redeploy) to fail for the same reason, which would not indicate
         // a redeployment issue per-se; but how to detect that?
         if ( initialThrowable == null )
         {
            fail( "Getting new exceptions after redeploy [" + t + "]" );
         }

         if ( !t.getClass().getName().equals( initialThrowable.getClass().getName() ) )
         {
            fail( "After redploy failing for different cause [" + t + "]" );
         }
      }
      finally
      {
         if ( service != null )
         {
            try
            {
               service.remove();
            }
            catch( Throwable t )
            {
            }
         }
      }

   }

   public void testCurrentSession() throws Throwable {

      ProfileServiceHome home = ProfileServiceUtil.getHome();
      ProfileService service = null;

      try
      {
         service = home.create();

         User user = new User();
         user.setEmail("nobody@nowhere.com");
         user.setName( new Name() );
         user.getName().setFirstName("John");
         user.getName().setInitial( new Character('Q') );
         user.getName().setLastName("Public");
         user.setPassword("password");
         user.setTimeOfCreation( new GregorianCalendar() );
         user.setHandle("myHandle");

         Long savedUserId = service.storeUser( user ).getId();
         getLog().info("User created with id = " + savedUserId );

         List users = service.listUsers();
         assertNotNull( users );
         assertEquals( "Incorrect result size", 1, users.size() );

         Long userId = ( ( User ) users.get( 0 ) ).getId();
         assertEquals( "Saved used not returned", savedUserId, userId );

         user = service.loadUser( savedUserId );
         assertNotNull( user );
      }
      finally
      {
         if ( service != null )
         {
            try
            {
               service.remove();
            }
            catch( Throwable t )
            {
               // ignore
            }
         }
      }
   }

}
