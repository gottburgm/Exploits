/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.jdbc;

import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;
import org.jboss.resource.JBossResourceException;

/**
 * BaseWrapperManagedConnectionFactory
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:weston.price@jboss.com">Weston Price</a>
 * 
 * @version $Revision: 113230 $
 */

public abstract class BaseWrapperManagedConnectionFactory implements ManagedConnectionFactory, ValidatingManagedConnectionFactory, Serializable
{
   /** @since 4.0.1 */
   static final long serialVersionUID = -84923705377702088L;

   public static final int TRACK_STATEMENTS_FALSE_INT = 0;
   public static final int TRACK_STATEMENTS_TRUE_INT = 1;
   public static final int TRACK_STATEMENTS_NOWARN_INT = 2;

   public static final String TRACK_STATEMENTS_FALSE = "false";
   public static final String TRACK_STATEMENTS_TRUE = "true";
   public static final String TRACK_STATEMENTS_NOWARN = "nowarn";

   protected final Logger log = Logger.getLogger(getClass());

   protected String userName;
   protected String password;

   //This is used by Local wrapper for all properties, and is left
   //in this class for ease of writing getConnectionProperties,
   //which always holds the user/pw.
   protected final Properties connectionProps = new Properties();

   protected int transactionIsolation = -1;

   protected int preparedStatementCacheSize = 0;

   protected boolean doQueryTimeout = false;

   /**
    * The variable <code>newConnectionSQL</code> holds an SQL
    * statement which if not null is executed when a new Connection is
    * obtained for a new ManagedConnection.
    */
   protected String newConnectionSQL;

   /**
    * The variable <code>checkValidConnectionSQL</code> holds an sql
    * statement that may be executed whenever a managed connection is
    * removed from the pool, to check that it is still valid.  This
    * requires setting up an mbean to execute it when notified by the
    * ConnectionManager.
    */
   protected String checkValidConnectionSQL;

   /**
    * The classname used to check whether a connection is valid
    */
   protected String validConnectionCheckerClassName;

   /**
    * The instance of the valid connection checker
    */
   protected ValidConnectionChecker connectionChecker;

   /** The instance of the stale connection checker */
   protected StaleConnectionChecker staleConnectionChecker;
   
   /** The staleConnectionCheckerClassName */
   private String staleConnectionCheckerClassName;

   private String exceptionSorterClassName;

   private ExceptionSorter exceptionSorter;

   protected int trackStatements = TRACK_STATEMENTS_NOWARN_INT;

   /** Whether to share cached prepared statements */
   protected boolean sharePS = false;
   
   protected boolean isTransactionQueryTimeout = false;
   
   protected int queryTimeout = 0;
   
   /** 
    * The variable <code>urlDelimiter</code> holds the url delimiter 
    * information to be used for HA DS configuration .
	*/
   protected String urlDelimiter;
   
   protected String urlSelectorStrategyClassName;

   private URLSelectorStrategy urlSelectorStrategy;

   private boolean validateOnMatch = true;

   /** Whether to use a try lock */
   private Integer useTryLock = Integer.valueOf(60);

   public BaseWrapperManagedConnectionFactory ()
   {

   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }

   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
   }

   public Object createConnectionFactory(ConnectionManager cm) throws ResourceException
   {
      return new WrapperDataSource(this, cm);
   }

   public Object createConnectionFactory() throws ResourceException
   {
      throw new JBossResourceException("Resource Adapter does not currently support running in a non-managed environment.");
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(final String userName)
   {
      this.userName = userName;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(final String password)
   {
      this.password = password;
   }

   public int getPreparedStatementCacheSize()
   {
      return preparedStatementCacheSize;
   }

   public void setPreparedStatementCacheSize(int size)
   {
      preparedStatementCacheSize = size;
   }

   public boolean getSharePreparedStatements()
   {
      return sharePS;
   }

   public void setSharePreparedStatements(boolean sharePS)
   {
      this.sharePS = sharePS;
   }

   public String getTransactionIsolation()
   {
      switch (this.transactionIsolation)
      {
         case Connection.TRANSACTION_NONE:
            return "TRANSACTION_NONE";
         case Connection.TRANSACTION_READ_COMMITTED:
            return "TRANSACTION_READ_COMMITTED";
         case Connection.TRANSACTION_READ_UNCOMMITTED:
            return "TRANSACTION_READ_UNCOMMITTED";
         case Connection.TRANSACTION_REPEATABLE_READ:
            return "TRANSACTION_REPEATABLE_READ";
         case Connection.TRANSACTION_SERIALIZABLE:
            return "TRANSACTION_SERIALIZABLE";
         case -1:
            return "DEFAULT";
         default:
            return Integer.toString(transactionIsolation);
      }
   }

   public void setTransactionIsolation(String transactionIsolation)
   {
      if (transactionIsolation.equals("TRANSACTION_NONE"))
         this.transactionIsolation = Connection.TRANSACTION_NONE;
      else if (transactionIsolation.equals("TRANSACTION_READ_COMMITTED"))
         this.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
      else if (transactionIsolation.equals("TRANSACTION_READ_UNCOMMITTED"))
         this.transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED;
      else if (transactionIsolation.equals("TRANSACTION_REPEATABLE_READ"))
         this.transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ;
      else if (transactionIsolation.equals("TRANSACTION_SERIALIZABLE"))
         this.transactionIsolation = Connection.TRANSACTION_SERIALIZABLE;
      else
      {
         try
         {
            this.transactionIsolation = Integer.parseInt(transactionIsolation);
         }
         catch (NumberFormatException nfe)
         {
            throw new IllegalArgumentException("Setting Isolation level to unknown state: " + transactionIsolation);
         }
      }
   }

   public String getNewConnectionSQL()
   {
      return newConnectionSQL;
   }

   public void setNewConnectionSQL(String newConnectionSQL)
   {
      this.newConnectionSQL = newConnectionSQL;
   }

   public String getCheckValidConnectionSQL()
   {
      return checkValidConnectionSQL;
   }

   public void setCheckValidConnectionSQL(String checkValidConnectionSQL)
   {
      this.checkValidConnectionSQL = checkValidConnectionSQL;
   }

   public void setStaleConnectionCheckerClassName(String value)
   {
      staleConnectionCheckerClassName = value;
      
   }
   
   public String getStaleConnectionCheckerClassName()
   {
      return staleConnectionCheckerClassName;
      
   }

   public String getTrackStatements()
   {
      if (trackStatements == TRACK_STATEMENTS_FALSE_INT)
         return TRACK_STATEMENTS_FALSE;
      else if (trackStatements == TRACK_STATEMENTS_TRUE_INT)
         return TRACK_STATEMENTS_TRUE;
      return TRACK_STATEMENTS_NOWARN;
   }
   
   public boolean getValidateOnMatch()
   {
      return this.validateOnMatch;
   }

   public void setValidateOnMatch(boolean validateOnMatch)
   {
      this.validateOnMatch = validateOnMatch;
   }
   
   public void setTrackStatements(String value)
   {
      if (value == null)
         throw new IllegalArgumentException("Null value for trackStatements"); 
      String trimmed = value.trim();
      if (trimmed.equalsIgnoreCase(TRACK_STATEMENTS_FALSE))
         trackStatements = TRACK_STATEMENTS_FALSE_INT;
      else if (trimmed.equalsIgnoreCase(TRACK_STATEMENTS_TRUE))
         trackStatements = TRACK_STATEMENTS_TRUE_INT;
      else
         trackStatements = TRACK_STATEMENTS_NOWARN_INT;
   }

   public String getExceptionSorterClassName()
   {
      return exceptionSorterClassName;
   }

   public void setExceptionSorterClassName(String exceptionSorterClassName)
   {
      this.exceptionSorterClassName = exceptionSorterClassName;
   }

   public String getValidConnectionCheckerClassName()
   {
      return validConnectionCheckerClassName;
   }

   public void setValidConnectionCheckerClassName(String value)
   {
      validConnectionCheckerClassName = value;
   }

   public boolean isTransactionQueryTimeout()
   {
      return isTransactionQueryTimeout;
   }

   public void setTransactionQueryTimeout(boolean value)
   {
      isTransactionQueryTimeout = value;
   }

   public int getQueryTimeout()
   {
      return queryTimeout;
   }

   public void setQueryTimeout(int timeout)
   {
      queryTimeout = timeout;
   }
   
   /**
    * Get the useTryLock.
    * 
    * @return the useTryLock.
    */
   public Integer getUseTryLock()
   {
      return useTryLock;
   }

   /**
    * Set the useTryLock.
    * 
    * @param useTryLock the useTryLock.
    */
   public void setUseTryLock(Integer useTryLock)
   {
      if( useTryLock != null)
         this.useTryLock = useTryLock;
   }
   
   /**
    * Get the urlDelimiter.
    * 
    * @return the urlDelimiter.
    */
   public String getURLDelimiter()
   {
      return urlDelimiter;
   }

   public void setURLDelimiter(String urlDelimiter)
	   throws ResourceException
   {
	   this.urlDelimiter = urlDelimiter;
   }   

   public void setUrlSelectorStrategyClassName(String urlSelectorStrategyClassName)
   {
		this.urlSelectorStrategyClassName = urlSelectorStrategyClassName;
   }
   
   public String getUrlSelectorStrategyClassName()
   {
	   return urlSelectorStrategyClassName;
   }

   public URLSelectorStrategy getUrlSelectorStrategy()
   {
	    return urlSelectorStrategy;
   }

   //This load the URLSelectStrategy class.
   public Object loadClass(String className,Object constructorParameter) 
		//throws JBossResourceException	
   {
		Object result = null;
		try
		{
			Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
			Class param[] = {java.util.List.class};
			Constructor cnstructor = clazz.getDeclaredConstructor(param);
			Object consParameter[] = {constructorParameter};
			result = cnstructor.newInstance(consParameter);
		}
		catch (ClassNotFoundException cnfe)
		{
			log.error("Class not found for URLSelectStrategy :"+className);
			//throw new JBossResourceException("Class not found for URLSelectStrategy: " + className, cnfe);
		}
		catch (InstantiationException ie)
		{	
			log.error("Could not instantiate URLSelectorStrategy type :"+className);
			//throw new JBossResourceException("Could not instantiate an URLSelectStrategy: ", ie);
		}
		catch (IllegalAccessException iae)
		{	
			log.error("Check for the constructor with List parameter for URLSelectStrategy class as "+className);
			//throw new JBossResourceException("Constructor object enforces Java language access control and the //underlying constructor is inaccessible: ", iae);
		
		}
		catch (InvocationTargetException ite)
		{	
			log.error("Constructor Invocation failing for URLSelectorStrategy "+className);
			//throw new JBossResourceException("The underlying constructor of URLSelecStrategy throws an exception: //", ite);
		}
		catch (NoSuchMethodException nsme)
		{	
			log.error("Constructor or Method mismatch in URLSelectorStrategy :"+className);
			//throw new JBossResourceException("Could not a matching method for URLSelecStrategy: ", nsme);
		}
		
		return result;
	}
   
   public Set getInvalidConnections(final Set connectionSet) throws ResourceException
   {
      final Set invalid = new HashSet();

      for (Iterator iter = connectionSet.iterator(); iter.hasNext();)
      {
         final Object anonymous = iter.next();

         if (anonymous instanceof BaseWrapperManagedConnection)
         {
            BaseWrapperManagedConnection mc = (BaseWrapperManagedConnection) anonymous;

            if (!mc.checkValid())
            {
               invalid.add(mc);

            }

         }

      }

      return invalid;
   }
   
   
   /**
    * Gets full set of connection properties, i.e. whatever is provided
    * in config plus "user" and "password" from subject/cri.
    *
    * <p>Note that the set is used to match connections to datasources as well
    * as to create new managed connections.
    *
    * <p>In fact, we have a problem here. Theoretically, there is a possible
    * name collision between config properties and "user"/"password".
    */
   protected synchronized Properties getConnectionProperties(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      if (cri != null && cri.getClass() != WrappedConnectionRequestInfo.class)
         throw new JBossResourceException("Wrong kind of ConnectionRequestInfo: " + cri.getClass());

      Properties props = new Properties();
      props.putAll(connectionProps);
      if (subject != null)
      {
         if (SubjectActions.addMatchingProperties(subject, props, this) == true)
            return props;
         throw new JBossResourceException("No matching credentials in Subject!");
      }
      WrappedConnectionRequestInfo lcri = (WrappedConnectionRequestInfo)cri;
      if (lcri != null)
      {
         props.setProperty("user", (lcri.getUserName() == null)? "": lcri.getUserName());
         props.setProperty("password", (lcri.getPassword() == null)? "": lcri.getPassword());
         return props;
      }
      if (userName != null)
      {
         props.setProperty("user", userName);
         props.setProperty("password", (password == null) ? "" : password);
      }
      return props;
   }

   boolean isExceptionFatal(SQLException e)
   {
      try
      {
         if (exceptionSorter != null)
            return exceptionSorter.isExceptionFatal(e);

         if (exceptionSorterClassName != null)
         {
            try
            {
               ClassLoader cl = Thread.currentThread().getContextClassLoader();
               Class clazz = cl.loadClass(exceptionSorterClassName);
               exceptionSorter = (ExceptionSorter)clazz.newInstance();
               return exceptionSorter.isExceptionFatal(e);
            }
            catch (Exception e2)
            {
               log.warn("exception trying to create exception sorter (disabling):", e2);
               exceptionSorter = new NullExceptionSorter();
            }
         }
      }
      catch (Throwable t)
      {
         log.warn("Error checking exception fatality: ", t);
      }
      return false;
   }

   /**
    * Checks whether a connection is valid
    */
   SQLException isValidConnection(Connection c)
   {
      // Already got a checker
      if (connectionChecker != null)
         return connectionChecker.isValidConnection(c);

      // Class specified
      if (validConnectionCheckerClassName != null)
      {
         try
         {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class clazz = cl.loadClass(validConnectionCheckerClassName);
            connectionChecker = (ValidConnectionChecker) clazz.newInstance();
            return connectionChecker.isValidConnection(c);
         }
         catch (Exception e)
         {
            log.warn("Exception trying to create connection checker (disabling):", e);
            connectionChecker = new NullValidConnectionChecker();
         }
      }

      // SQL statement specified
      if (checkValidConnectionSQL != null)
      {
         connectionChecker = new CheckValidConnectionSQL(checkValidConnectionSQL);
         return connectionChecker.isValidConnection(c);
      }

      // No Check
      return null;
   }

   boolean isStaleConnection(SQLException e)
   {
      if (staleConnectionChecker != null)
         return staleConnectionChecker.isStaleConnection(e); 
      
      if (staleConnectionCheckerClassName != null)
      {
         try
         {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class clazz = cl.loadClass(staleConnectionCheckerClassName);
            staleConnectionChecker = (StaleConnectionChecker)clazz.newInstance();
            return staleConnectionChecker.isStaleConnection(e);
         }
         catch (Exception ex2)
         {
            log.warn("exception trying to create stale connection checker (disabling)" + staleConnectionCheckerClassName, ex2);
            staleConnectionChecker = new NullStaleConnectionChecker();
         }
      }
            
      return false;
   }
   

   
   static class SubjectActions implements PrivilegedAction
   {
      Subject subject;

      Properties props;

      ManagedConnectionFactory mcf;

      SubjectActions(Subject subject, Properties props, ManagedConnectionFactory mcf)
      {
         this.subject = subject;
         this.props = props;
         this.mcf = mcf;
      }

      public Object run()
      {
         Iterator i = subject.getPrivateCredentials().iterator();
         while (i.hasNext())
         {
            Object o = i.next();
            if (o instanceof PasswordCredential)
            {
               PasswordCredential cred = (PasswordCredential) o;
               if (cred.getManagedConnectionFactory().equals(mcf))
               {
                  props.setProperty("user", (cred.getUserName() == null) ? "" : cred.getUserName());
                  if( cred.getPassword() != null )
                     props.setProperty("password", new String(cred.getPassword()));
                  return Boolean.TRUE;
               }
            }
         }
         return Boolean.FALSE;
      }

      static boolean addMatchingProperties(Subject subject, Properties props, ManagedConnectionFactory mcf)
      {
         SubjectActions action = new SubjectActions(subject, props, mcf);
         Boolean matched = (Boolean) AccessController.doPrivileged(action);
         return matched.booleanValue();
      }
   }
}
