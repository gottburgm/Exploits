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
package org.jboss.resource.metadata.mcf;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ManagementPropertyFactory;
import org.jboss.resource.deployers.management.ConnectionFactoryProperty;

/**
 * A DataSourceDeploymentMetaData.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 113162 $
 */
public class DataSourceDeploymentMetaData extends ManagedConnectionFactoryDeploymentMetaData implements JDBCProviderSupport
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1440129014410015366L;
   
   private static final String CONNECTION_DEFINITION = "javax.sql.DataSource";
   
   private static final String RAR_NAME = "jboss-local-jdbc.rar";

   @XmlElement(name="transaction-isolation")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String transactionIsolation;
   
   @XmlElement(name="user-name")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String userName;
   
   @XmlElement(name="password")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String passWord;
   
   @XmlElement(name="new-connection-sql")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String newConnectionSQL;
   
   @XmlElement(name="check-valid-connection-sql")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String checkValidConnectionSQL;
   
   @XmlElement(name="valid-connection-checker-class-name")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String validConnectionCheckerClassName;
   
   @XmlElement(name="exception-sorter-class-name")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String exceptionSorterClassName;
   
   @XmlElement(name="stale-connection-checker-class-name")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String staleConnectionCheckerClassName;
   
   @XmlElement(name="track-statements")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String trackStatements;
   
   @XmlElement(name="prepared-statement-cache-size")
   @XmlJavaTypeAdapter(IntegerSystemPropertyXmlJavaTypeAdapter.class)
   private Integer preparedStatementCacheSize = 0;
   
   @XmlElement(name="share-prepared-statements")
   @XmlJavaTypeAdapter(BooleanSystemPropertyXmlJavaTypeAdapter.class)
   private Boolean sharePreparedStatements = Boolean.FALSE;
   
   @XmlElement(name="set-tx-query-timeout")
   @XmlJavaTypeAdapter(BooleanSystemPropertyXmlJavaTypeAdapter.class)
   private Boolean useQueryTimeout = Boolean.FALSE;
   
   @XmlElement(name="query-timeout")
   @XmlJavaTypeAdapter(IntegerSystemPropertyXmlJavaTypeAdapter.class)
   private Integer queryTimeout = 0;
   
   @XmlElement(name="use-try-lock")
   @XmlJavaTypeAdapter(IntegerSystemPropertyXmlJavaTypeAdapter.class)
   private Integer useTryLock = 0;
   
   @XmlElement(name="url-delimiter")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String urlDelimiter;
   
   @XmlElement(name="url-selector-strategy-class-name")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String urlSelectorStrategyClassName;
   
   public DataSourceDeploymentMetaData()
   {
      setConnectionDefinition(CONNECTION_DEFINITION);
      setRarName(RAR_NAME);
      
   }

   @ManagementProperty(name="transaction-isolation",
         description="The DataSource transaction isolation level",
         includeInTemplate=true)
   public String getTransactionIsolation()
   {
      return transactionIsolation;
   }

   public void setTransactionIsolation(String transactionIsolation)
   {
      this.transactionIsolation = transactionIsolation;
   }

   @ManagementProperty(name="password", description="The DataSource password",
         includeInTemplate=true)
   public String getPassWord()
   {
      return passWord;
   }

   public void setPassWord(String passWord)
   {
      this.passWord = passWord;
   }

   @ManagementProperty(name="user-name", description="The DataSource username",
         includeInTemplate=true)
   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String userName)
   {
      this.userName = userName;
   }
   
      
   @ManagementProperty(name="check-valid-connection-sql",
         description="The SQL statement to validate a connection",
         includeInTemplate=true)
   public String getCheckValidConnectionSQL()
   {
      return checkValidConnectionSQL;
   }

   public void setCheckValidConnectionSQL(String checkValidConnectionSQL)
   {
      this.checkValidConnectionSQL = checkValidConnectionSQL;
   }

   @ManagementProperty(name="exception-sorter-class-name",
         description="The exception sorter class name",
         includeInTemplate=true)
   public String getExceptionSorterClassName()
   {
      return exceptionSorterClassName;
   }

   public void setExceptionSorterClassName(String exceptionSorterClassName)
   {
      this.exceptionSorterClassName = exceptionSorterClassName;
   }

   @ManagementProperty(name="new-connection-sql", description="The new connection SQL",
         includeInTemplate=true)
   public String getNewConnectionSQL()
   {
      return newConnectionSQL;
   }

   public void setNewConnectionSQL(String newConnectionSQL)
   {
      this.newConnectionSQL = newConnectionSQL;
   }

   @ManagementProperty(name="valid-connection-checker-class-name",
         description="The DataSource connection checker class name",
         includeInTemplate=true)
   public String getValidConnectionCheckerClassName()
   {
      return validConnectionCheckerClassName;
   }

   public void setValidConnectionCheckerClassName(String validConnectionCheckerClassName)
   {
      this.validConnectionCheckerClassName = validConnectionCheckerClassName;
   }

   @ManagementProperty(name="stale-connection-checker-class-name",
         description="The DataSource stale connection checker class name",
         includeInTemplate=true)
   public String getStaleConnectionCheckerClassName()
   {
      return staleConnectionCheckerClassName;
   }

   public void setStaleConnectionCheckerClassName(String staleConnectionCheckerClassName)
   {
      this.staleConnectionCheckerClassName = staleConnectionCheckerClassName;
   }

   @ManagementProperty(name="url-delimiter", description="The DataSource url delimiter",
         includeInTemplate=true)
   public String getURLDelimiter()
   {
      return urlDelimiter;
   }

   public void setURLDelimiter(String urlDelimiter)
   {
      this.urlDelimiter = urlDelimiter;
   }

   @ManagementProperty(name="url-selector-strategy-class-name",
         description="The DataSource url selector strategy class name",
         includeInTemplate=true)
   public String getURLSelectorStrategyClassName()
   {
      return urlSelectorStrategyClassName;
   }

   public void setURLSelectorStrategyClassName(String urlSelectorStrategyClassName)
   {
      this.urlSelectorStrategyClassName = urlSelectorStrategyClassName;
   }
      
   @ManagementProperty(name="prepared-statement-cache-size",
         description="The DataSource prepared statement cache size",
         includeInTemplate=true)
   public int getPreparedStatementCacheSize()
   {
      return preparedStatementCacheSize;
   }

   public void setPreparedStatementCacheSize(int preparedStatementCacheSize)
   {
      this.preparedStatementCacheSize = preparedStatementCacheSize;
   }

   @ManagementProperty(name="query-timeout", description="The query timeout",
         includeInTemplate=true)
   public int getQueryTimeout()
   {
      return queryTimeout;
   }

   public void setQueryTimeout(int queryTimeout)
   {
      this.queryTimeout = queryTimeout;
   }

   @ManagementProperty(name="use-try-lock", description="The internal lock timeout",
         includeInTemplate=true)
   public Integer getUseTryLock()
   {
      return useTryLock;
   }

   public void setUseTryLock(Integer useTryLock)
   {
      this.useTryLock = useTryLock;
   }

   @ManagementProperty(name="share-prepared-statements",
         description="Should prepared statements be shared",
         includeInTemplate=true)
   public boolean isSharePreparedStatements()
   {
      return sharePreparedStatements;
   }

   public void setSharePreparedStatements(boolean sharePreparedStatements)
   {
      this.sharePreparedStatements = sharePreparedStatements;
   }

   @ManagementProperty(name="track-statements", description="The track statements method",
         includeInTemplate=true)
   public String getTrackStatements()
   {
      return trackStatements;
   }

   public void setTrackStatements(String trackStatements)
   {
      this.trackStatements = trackStatements;
   }

   @ManagementProperty(name="set-tx-query-timeout", description="Should query timeout be enabled",
         includeInTemplate=true)
   public boolean isUseQueryTimeout()
   {
      return useQueryTimeout;
   }

   public void setUseQueryTimeout(boolean useQueryTimeout)
   {
      this.useQueryTimeout = useQueryTimeout;
   }

   @Override
   @ManagementProperty(name="config-property",
         description="The connection factory property info",
         managed=true, readOnly = true)
   @ManagementPropertyFactory(ConnectionFactoryProperty.class)
   public List<ManagedConnectionFactoryPropertyMetaData> getManagedConnectionFactoryProperties()
   {
      List<ManagedConnectionFactoryPropertyMetaData> properties = new ArrayList<ManagedConnectionFactoryPropertyMetaData>();
      ManagedConnectionFactoryPropertyMetaData property = null;
            
      if(getUserName() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("UserName");
         property.setValue(getUserName());
         properties.add(property);
      }
      
      if(getPassWord() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("Password");
         property.setValue(getPassWord());
         properties.add(property);
         
      }
      
      if(getTransactionIsolation() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("TransactionIsolation");
         property.setValue(getTransactionIsolation());
         properties.add(property);
         
      }
      
      if(getNewConnectionSQL() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("NewConnectionSQL");
         property.setValue(getNewConnectionSQL());         
         properties.add(property);

      }
      
      if(getCheckValidConnectionSQL() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("CheckValidConnectionSQL");
         property.setValue(getCheckValidConnectionSQL());                  
         properties.add(property);

      }
      
      if(getValidConnectionCheckerClassName() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("ValidConnectionCheckerClassName");
         property.setValue(getValidConnectionCheckerClassName());                           
         properties.add(property);

      }
      
      if(getExceptionSorterClassName() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("ExceptionSorterClassName");
         property.setValue(getExceptionSorterClassName());                           
         properties.add(property);         
      }
      
      if(getStaleConnectionCheckerClassName() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("StaleConnectionCheckerClassName");
         property.setValue(getStaleConnectionCheckerClassName());                           
         properties.add(property);         
      }
      
      if(getURLSelectorStrategyClassName() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("UrlSelectorStrategyClassName");
         property.setValue(getURLSelectorStrategyClassName());                           
         properties.add(property);         
      }
      
      if(getURLDelimiter() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("URLDelimiter");
         property.setValue(getURLDelimiter());                           
         properties.add(property);         
      }
      
      property = new ManagedConnectionFactoryPropertyMetaData();
      property.setName("PreparedStatementCacheSize");
      property.setType("int");
      property.setValue(String.valueOf(getPreparedStatementCacheSize()));
      properties.add(property);
      
      property = new ManagedConnectionFactoryPropertyMetaData();
      property.setName("SharePreparedStatements");
      property.setType("boolean");
      property.setValue(String.valueOf(isSharePreparedStatements()));
      properties.add(property);
      
      property = new ManagedConnectionFactoryPropertyMetaData();
      property.setName("QueryTimeout");
      property.setType("int");
      property.setValue(String.valueOf(getQueryTimeout()));
      properties.add(property);
      
      property = new ManagedConnectionFactoryPropertyMetaData();
      property.setName("UseTryLock");
      property.setType("java.lang.Integer");
      property.setValue(String.valueOf(getUseTryLock()));
      properties.add(property);
      
      property = new ManagedConnectionFactoryPropertyMetaData();
      property.setName("TransactionQueryTimeout");
      property.setType("boolean");
      property.setValue(String.valueOf(isUseQueryTimeout()));
      properties.add(property);
      
      property = new ManagedConnectionFactoryPropertyMetaData();
      property.setName("ValidateOnMatch");
      property.setType("boolean");
      property.setValue(String.valueOf(isValidateOnMatch()));
      properties.add(property);
      
      if (getTrackStatements() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("TrackStatements");
         property.setType("java.lang.String");
         property.setValue(String.valueOf(getTrackStatements()));
         properties.add(property);
      }
      
      return properties;
   }
}
