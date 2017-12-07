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
package org.jboss.test.cmp2.readonly;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;

public class ReadonlyUnitTestCase extends EJBTestCase {

       static org.jboss.logging.Logger log =
       org.jboss.logging.Logger.getLogger(ReadonlyUnitTestCase.class);

	public static Test suite() throws Exception {
		return JBossTestCase.getDeploySetup(
            ReadonlyUnitTestCase.class, "cmp2-readonly.jar");
   }

	public ReadonlyUnitTestCase(String name) {
		super(name);
	}

	private PublisherHome getPublisherHome() {
		try {
			InitialContext jndiContext = new InitialContext();
			
			return (PublisherHome) jndiContext.lookup("cmp2/readonly/Publisher"); 
		} catch(Exception e) {
			log.debug("failed", e);
			fail("Exception in getPublisherHome: " + e.getMessage());
		}
		return null;
	}

	private BookHome getBookHome() {
		try {
			InitialContext jndiContext = new InitialContext();
			
			return (BookHome) jndiContext.lookup("cmp2/readonly/Book");
		} catch(Exception e) {
			log.debug("failed", e);
			fail("Exception in getBookHome: " + e.getMessage());
		}
		return null;
	}
 
	private AuthorHome getAuthorHome() {
		try {
			InitialContext jndiContext = new InitialContext();
			
			return (AuthorHome) jndiContext.lookup("cmp2/readonly/Author");
		} catch(Exception e) {
			log.debug("failed", e);
			fail("Exception in getAuthorHome: " + e.getMessage());
		}
		return null;
	}
   
   private Connection getConnection() {
      try {
			InitialContext jndiContext = new InitialContext();
         DataSource ds = (DataSource) jndiContext.lookup("java:/DefaultDS");
         return ds.getConnection();
		} catch(Exception e) {
			log.debug("failed", e);
			fail("Exception in getConnection: " + e.getMessage());
		}
		return null;
   }
      
   private Publisher oreilly;
   private Publisher sams;
   private Book ejb;
   private Book jms;
   private Book jmx;
   private Book jboss;
   private Author dain;

   protected void setUp() throws Exception {
      PublisherHome publisherHome = getPublisherHome();
      BookHome bookHome = getBookHome();
      AuthorHome authorHome = getAuthorHome();

      oreilly = publisherHome.findByName("O'Reilly & Associates");
      sams = publisherHome.findByName("Sams");
      ejb = bookHome.findByName("Enterprise Java Beans (3rd Edition)");
      jms = bookHome.findByName("Java Message Service");
      jmx = bookHome.findByName(
            "JMX: Managing J2EE with Java Management Extensions");
      jboss = bookHome.findByName("JBOSS Administration and Development");
      dain = authorHome.findByName("Dain Sundstrom");
   }

   protected void tearDown() {
      oreilly = null;
      sams = null;
      ejb = null;
      jms = null;
      jmx = null;
      jboss = null;
   }

   public void testSetUp() throws Exception {
      Collection oreillyBooks = oreilly.getBooks();
      assertEquals(2, oreillyBooks.size());
      assertTrue(oreillyBooks.contains(ejb));
      assertTrue(oreillyBooks.contains(jms));
      assertTrue(ejb.getPublisher().isIdentical(oreilly));
      assertTrue(jms.getPublisher().isIdentical(oreilly));

      Collection samsBooks = sams.getBooks();
      assertEquals(2, samsBooks.size());
      assertTrue(samsBooks.contains(jmx));
      assertTrue(samsBooks.contains(jboss));
      assertTrue(jmx.getPublisher().isIdentical(sams));
      assertTrue(jboss.getPublisher().isIdentical(sams));
   }

   public void testReadonlyCMPField() throws Exception {
      try {
         oreilly.setName("Stuff");
         fail("Should have gotten exception from Publisher.setName");
      } catch(Exception e) {
      }
   }

   public void testReadonlyEntityCMPFieldChange() throws Exception {
      try {
         dain.setName("Stuff");
         fail("Should have gotten exception from Author.setName");
      } catch(Exception e) {
      }
   }

   public void testReadonlyEntityCreate() throws Exception {
      try {
         AuthorHome authorHome = getAuthorHome();
         authorHome.create(new Integer(44));
         fail("Should have gotten exception from AuthorHome.create");
      } catch(Exception e) {
      }
   }

   public void testReadonlySetFK() throws Exception {
      try {
         jboss.setPublisher(sams);
         fail("Should have gotten exception from Book.setPublisher");
      } catch(Exception e) {
      }
   }

   public void testReadonlySetCollection() throws Exception {
      try {
         sams.setBooks(new HashSet());
         fail("Should have gotten exception from Publisher.setBooks");
      } catch(Exception e) {
      }
   }
   
   public void testReadonlyCollectionAdd() throws Exception {
      try {
         sams.getBooks().add(jboss);
         fail("Should have gotten exception from Book.setPublisher");
      } catch(Exception e) {
      }
   }
   
   public void testReadonlyCollectionRemove() throws Exception {
      try {
         sams.getBooks().remove(ejb);
         fail("Should have gotten exception from Book.setPublisher");
      } catch(Exception e) {
      }
   }
   
	public void setUpEJB(Properties props) throws Exception {
      cleanDB();

      Connection con = null;
      PreparedStatement ps = null;
      try {
         con = getConnection();
         
         ps = con.prepareStatement(
               "INSERT INTO PublisherEJB (id, name) " +
               "VALUES (?,?)");
         
         // O'Reilly
         ps.setInt(1, 1);
         ps.setString(2, "O'Reilly & Associates");
         if(ps.executeUpdate() != 1) {
            fail("Failed to add Publisher to database");
         }

         // Sams
         ps.setInt(1, 2);
         ps.setString(2, "Sams");
         if(ps.executeUpdate() != 1) {
            fail("Failed to add Publisher to database");
         }

         ps.close();

         ps = con.prepareStatement(
               "INSERT INTO Book (id, name, isbn, publisher) " +
               "VALUES (?,?,?,?)");

         ps.setInt(1, -1);
         ps.setString(2, "Enterprise Java Beans (3rd Edition)");
         ps.setString(3, "0596002262");
         ps.setInt(4, 1);
         if(ps.executeUpdate() != 1) {
            fail("Failed to add Book to database");
         }

         ps.setInt(1, -2);
         ps.setString(2, "Java Message Service");
         ps.setString(3, "0596000685 ");
         ps.setInt(4, 1);
         if(ps.executeUpdate() != 1) {
            fail("Failed to add Book to database");
         }

         ps.setInt(1, -3);
         ps.setString(2, "JMX: Managing J2EE with Java Management Extensions");
         ps.setString(3, "0672322889");
         ps.setInt(4, 2);
         if(ps.executeUpdate() != 1) {
            fail("Failed to add Book to database");
         }

         ps.setInt(1, -4);
         ps.setString(2, "JBOSS Administration and Development");
         ps.setString(3, "0672323478");
         ps.setInt(4, 2);
         if(ps.executeUpdate() != 1) {
            fail("Failed to add Book to database");
         }

         ps = con.prepareStatement(
               "INSERT INTO Author (id, name) " +
               "VALUES (?,?)");
         
         // O'Reilly
         ps.setInt(1, 1);
         ps.setString(2, "Dain Sundstrom");
         if(ps.executeUpdate() != 1) {
            fail("Failed to add Author to database");
         }
      } finally {
         if(ps != null) {
            try {
               ps.close();
            } catch(SQLException e) {
               log.debug("failed", e);
            }
         }
         if(con != null) {
            try {
               con.close();
            } catch(SQLException e) {
               log.debug("failed", e);
            }
         }
      }
	}
	
	public void tearDownEJB(Properties props) throws Exception {
      cleanDB();
	}
	
	public void cleanDB() throws Exception {
	   Connection con = null;
      Statement statement = null;
      try {
         con = getConnection();
         
         statement = con.createStatement();

         statement.executeUpdate("DELETE FROM Book");
         statement.executeUpdate("DELETE FROM PublisherEJB");
         statement.executeUpdate("DELETE FROM Author");
      } finally {
         if(statement != null) {
            try {
               statement.close();
            } catch(SQLException e) {
               log.debug("failed", e);
            }
         }
         if(con != null) {
            try {
               con.close();
            } catch(SQLException e) {
               log.debug("failed", e);
            }
         }
      }
   }
}



