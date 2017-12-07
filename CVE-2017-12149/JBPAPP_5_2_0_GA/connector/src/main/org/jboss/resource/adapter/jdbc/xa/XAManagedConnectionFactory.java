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
package org.jboss.resource.adapter.jdbc.xa;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.resource.adapter.jdbc.BaseWrapperManagedConnectionFactory;
import org.jboss.resource.adapter.jdbc.URLSelectorStrategy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.beans.PropertyEditor;
import javax.sql.XADataSource;
import javax.sql.XAConnection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;
import org.jboss.resource.JBossResourceException;

/**
 * XAManagedConnectionFactory
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 113233 $
 */
public class XAManagedConnectionFactory extends BaseWrapperManagedConnectionFactory
{
   private static final long serialVersionUID = 1647927657609573729L;

   private String xaDataSourceClass;

   private String xaDataSourceProperties;

   protected final Map<String, String> xaProps = Collections.synchronizedMap(new HashMap<String , String>());

   private Boolean isSameRMOverrideValue;

   private XADataSource xads;
   
   private String urlProperty;

   private URLSelectorStrategy xadsSelector;

   public String getURLProperty()
   {
      return urlProperty;
   }

   public void setURLProperty(String urlProperty) throws ResourceException
   {
      this.urlProperty = urlProperty;
      initSelector();
   }

   public void setURLDelimiter(String urlDelimiter) throws ResourceException
   {
      this.urlDelimiter = urlDelimiter;
      initSelector();
   }

   private void initSelector() throws JBossResourceException
   {
      if(urlProperty != null && urlProperty.length() > 0)
      {
         String urlsStr = xaProps.get(urlProperty);
         if(urlsStr != null && urlsStr.trim().length() > 0 && urlDelimiter != null && urlDelimiter.trim().length() > 0)
         {
            List xaDataList = new ArrayList();

            // copy xaProps
            // ctor doesn't work because iteration won't include defaults
            // Properties xaPropsCopy = new Properties(xaProps);
            Properties xaPropsCopy = new Properties();
            for(Map.Entry<String, String> entry : xaProps.entrySet())
            {
               xaPropsCopy.put(entry.getKey(), entry.getValue());
            }

            int urlStart = 0;
            int urlEnd = urlsStr.indexOf(urlDelimiter);
            while(urlEnd > 0)
            {
               String url = urlsStr.substring(urlStart, urlEnd);
               xaPropsCopy.setProperty(urlProperty, url);
               XADataSource xads = createXaDataSource(xaPropsCopy);
               xaDataList.add(new XAData(xads, url));
               urlStart = ++urlEnd;
               urlEnd = urlsStr.indexOf(urlDelimiter, urlEnd);
               log.debug("added XA HA connection url: " + url);
            }

            if(urlStart != urlsStr.length())
            {
               String url = urlsStr.substring(urlStart, urlsStr.length());
               xaPropsCopy.setProperty(urlProperty, url);
               XADataSource xads = createXaDataSource(xaPropsCopy);
               xaDataList.add(new XAData(xads, url));
               log.debug("added XA HA connection url: " + url);
            }
			if(getUrlSelectorStrategyClassName()==null)
			{
	            xadsSelector = new XADataSelector(xaDataList);
				log.debug("Default URLSelectorStrategy is being used : "+xadsSelector);
			}
			else
			{
				xadsSelector = (URLSelectorStrategy)loadClass(getUrlSelectorStrategyClassName(),xaDataList);
				log.debug("Customized URLSelectorStrategy is being used : "+xadsSelector);
			}
         }
      }
   }

   private XADataSource createXaDataSource(Properties p)
   throws JBossResourceException
   {
   if(getXADataSourceClass() == null)
   {
      throw new JBossResourceException("No XADataSourceClass supplied!");
   }

   XADataSource xads;
   try
   {
      Class clazz = Thread.currentThread().getContextClassLoader().loadClass(getXADataSourceClass());
      xads = (XADataSource)clazz.newInstance();
      Class[] NOCLASSES = new Class[]{};
      for(Iterator i = p.keySet().iterator(); i.hasNext();)
      {
         String name = (String)i.next();
         String value = p.getProperty(name);
         char firstCharName = Character.toUpperCase(name.charAt(0));
     	 if (name.length() > 1)
     		 name = firstCharName+name.substring(1);
     	 else
      		 name = ""+firstCharName;            		              	                              		              	                          
         //This is a bad solution.  On the other hand the only known example
         // of a setter with no getter is for Oracle with password.
         //Anyway, each xadatasource implementation should get its
         //own subclass of this that explicitly sets the
         //properties individually.
         Class type = null;
         try
         {
            Method getter = clazz.getMethod("get" + name, NOCLASSES);
            type = getter.getReturnType();
         }
         catch(NoSuchMethodException e)
         {
            try
            {
               //HACK for now until we can rethink the XADataSourceProperties variable and pass type information
               Method isMethod = clazz.getMethod("is" + name, NOCLASSES);
               type = isMethod.getReturnType();
            }
            catch(NoSuchMethodException nsme)
            {
               type = String.class;                     
            }
         } // end of try-catch

         Method setter = clazz.getMethod("set" + name, new Class[]{type});
         PropertyEditor editor = PropertyEditorFinder.getInstance().find(type);
         if(editor == null)
         {
            throw new JBossResourceException("No property editor found for type: " + type);
         } // end of if ()
         editor.setAsText(value);
         setter.invoke(xads, new Object[]{editor.getValue()});

      } // end of for ()
   }
   catch(ClassNotFoundException cnfe)
   {
      throw new JBossResourceException("Class not found for XADataSource " + getXADataSourceClass(), cnfe);
   } // end of try-catch
   catch(InstantiationException ie)
   {
      throw new JBossResourceException("Could not create an XADataSource: ", ie);
   } // end of catch
   catch(IllegalAccessException iae)
   {
      throw new JBossResourceException("Could not set a property: ", iae);
   } // end of catch

   catch(IllegalArgumentException iae)
   {
      throw new JBossResourceException("Could not set a property: ", iae);
   } // end of catch

   catch(InvocationTargetException ite)
   {
      throw new JBossResourceException("Could not invoke setter on XADataSource: ", ite);
   } // end of catch
   catch(NoSuchMethodException nsme)
   {
      throw new JBossResourceException("Could not find accessor on XADataSource: ", nsme);
   } // end of catch

   return xads;
   }
   
   // Default Implementaion of the URLSelectorStrategy
   public static class XADataSelector implements URLSelectorStrategy
   {
      private final List xaDataList;
      private int xaDataIndex;
      private XAData xaData;
	  
      public XADataSelector(List xaDataList)
      {
         if(xaDataList == null || xaDataList.size() == 0)
         {
            throw new IllegalStateException("Expected non-empty list of XADataSource/URL pairs but got: " + xaDataList);
         }

         this.xaDataList = xaDataList;
      }

      public synchronized XAData getXAData()
      {
         if(xaData == null)
         {
            if(xaDataIndex == xaDataList.size())
            {
               xaDataIndex = 0;
            }
            xaData = (XAData)xaDataList.get(xaDataIndex++);
         }
         return xaData;
      }

      public synchronized void failedXAData(XAData xads)
      {
         if(xads.equals(this.xaData))
         {
            this.xaData = null;
         }
      }

	  /* URLSelectorStrategy Implementation goes here*/	  
	  public List getCustomSortedUrls()
	  {
		 return xaDataList;
	  }
	  public void failedUrlObject(Object urlObject)
	  {
		 failedXAData((XAData)urlObject);
	  }
	  public List getAllUrlObjects()
	  {
		 return xaDataList;
      }
	  public Object getUrlObject()
	  {
		 return getXAData();
	  }	  
   }
   
   public static class XAData
   {
      public final XADataSource xads;
      public final String url;

      public XAData(XADataSource xads, String url)
      {
         this.xads = xads;
         this.url = url;
      }

      public boolean equals(Object o)
      {
         if(this == o)
         {
            return true;
         }
         if(!(o instanceof XAData))
         {
            return false;
         }

         final XAData xaData = (XAData)o;

         if(!url.equals(xaData.url))
         {
            return false;
         }

         return true;
      }

      public int hashCode()
      {
         return url.hashCode();
      }

      public String toString()
      {
         return "[XA URL=" + url + "]";
      }
   }

   public XAManagedConnectionFactory()
   {
   }

   /**
    * Get the XaDataSourceClass value.
    * 
    * @return the XaDataSourceClass value.
    */
   public String getXADataSourceClass()
   {
      return xaDataSourceClass;
   }

   /**
    * Set the XaDataSourceClass value.
    * 
    * @param xaDataSourceClass The new XaDataSourceClass value.
    */
   public void setXADataSourceClass(String xaDataSourceClass)
   {
      this.xaDataSourceClass = xaDataSourceClass;
   }

   /**
    * Get the XADataSourceProperties value.
    * 
    * @return the XADataSourceProperties value.
    */
   public String getXADataSourceProperties()
   {
      return xaDataSourceProperties;
   }

   /**
    * Set the XADataSourceProperties value.
    * 
    * @param xaDataSourceProperties The new XADataSourceProperties value.
    */
   public void setXADataSourceProperties(String xaDataSourceProperties) throws ResourceException
   {
      this.xaDataSourceProperties = xaDataSourceProperties;
      xaProps.clear();
      if (xaDataSourceProperties != null)
      {
         // Map any \ to \\
         xaDataSourceProperties = xaDataSourceProperties.replaceAll("\\\\", "\\\\\\\\");

         InputStream is = new ByteArrayInputStream(xaDataSourceProperties.getBytes());
         try
         {
            Properties p = new Properties();
            p.load(is);

            for (Map.Entry<Object, Object> entry: p.entrySet())
            {
               xaProps.put((String)entry.getKey(), (String)entry.getValue());
            }
         }
         catch (IOException ioe)
         {
            throw new JBossResourceException("Could not load connection properties", ioe);
         }
      }
      initSelector();
   }

   /**
    * Get the IsSameRMOverrideValue value.
    * 
    * @return the IsSameRMOverrideValue value.
    */
   public Boolean getIsSameRMOverrideValue()
   {
      return isSameRMOverrideValue;
   }

   /**
    * Set the IsSameRMOverrideValue value.
    * 
    * @param isSameRMOverrideValue The new IsSameRMOverrideValue value.
    */
   public void setIsSameRMOverrideValue(Boolean isSameRMOverrideValue)
   {
      this.isSameRMOverrideValue = isSameRMOverrideValue;
   }

   public synchronized ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri)
   		throws javax.resource.ResourceException
   {	   
	   if(xadsSelector == null)
	   {
		  return getXAManagedConnection(subject,cri);
	   }
	
	   // try to get a connection as many times as many urls we have in the list
	   for(int i = 0; i < xadsSelector.getCustomSortedUrls().size(); ++i)
	   {
		  XAData xaData = (XAData)xadsSelector.getUrlObject();
	
	      if(log.isTraceEnabled())
	      {
	         log.trace("Trying to create an XA connection to " + xaData.url);
	      }
	
	      try
	      {
	    	  return getXAManagedConnection(subject,cri);
	      }
	      catch(ResourceException e)
	      {
	         log.warn("Failed to create an XA connection to " + xaData.url + ": " + e.getMessage());
			 xadsSelector.failedUrlObject(xaData);
	      }
	   }
	
	   // we have supposedly tried all the urls
	   throw new JBossResourceException(
	      "Could not create connection using any of the URLs: " + xadsSelector.getAllUrlObjects()
	   );	   
   }
   
   public ManagedConnection getXAManagedConnection(Subject subject, ConnectionRequestInfo cri)
   		throws javax.resource.ResourceException
	{
        XAConnection xaConnection = null;
		Properties props = getConnectionProperties(subject, cri);
		try
		{
		   final String user = props.getProperty("user");
		   final String password = props.getProperty("password");
		
		   xaConnection = (user != null)
				 ? getXADataSource().getXAConnection(user, password)
				 : getXADataSource().getXAConnection();
		
		   return newXAManagedConnection(props, xaConnection);
		}
		catch (Throwable e)
		{
		   try
		   {
		      if (xaConnection != null)
		         xaConnection.close();
		   }
		   catch (Throwable ignored)
		   {
		   }
		   throw new JBossResourceException("Could not create connection", e);
		}
	}

   /**
    * This method can be overwritten by sublcasses to provide rm specific
    * implementation of XAManagedConnection
    */
   protected ManagedConnection newXAManagedConnection(Properties props, XAConnection xaConnection) throws SQLException
   {
      return new XAManagedConnection(this, xaConnection, props, transactionIsolation, preparedStatementCacheSize);
   }

   public ManagedConnection matchManagedConnections(Set mcs, Subject subject, ConnectionRequestInfo cri)
         throws ResourceException
   {
      Properties newProps = getConnectionProperties(subject, cri);
      for (Iterator i = mcs.iterator(); i.hasNext();)
      {
         Object o = i.next();
         if (o instanceof XAManagedConnection)
         {
            XAManagedConnection mc = (XAManagedConnection) o;

            if (mc.getProperties().equals(newProps))
            {
               //Next check to see if we are validating on matchManagedConnections
               if ((getValidateOnMatch() && mc.checkValid()) || !getValidateOnMatch())
               {

                  return mc;

               }

            }

         }
      }
      return null;
   }

   public int hashCode()
   {
      int result = 17;
      result = result * 37 + ((xaDataSourceClass == null) ? 0 : xaDataSourceClass.hashCode());
      result = result * 37 + xaProps.hashCode();
      result = result * 37 + ((userName == null) ? 0 : userName.hashCode());
      result = result * 37 + ((password == null) ? 0 : password.hashCode());
      result = result * 37 + transactionIsolation;
      return result;
   }

   public boolean equals(Object other)
   {
      if (this == other)
         return true;
      if (getClass() != other.getClass())
         return false;
      XAManagedConnectionFactory otherMcf = (XAManagedConnectionFactory) other;
      return this.xaDataSourceClass.equals(otherMcf.xaDataSourceClass) && this.xaProps.equals(otherMcf.xaProps)
            && ((this.userName == null) ? otherMcf.userName == null : this.userName.equals(otherMcf.userName))
            && ((this.password == null) ? otherMcf.password == null : this.password.equals(otherMcf.password))
            && this.transactionIsolation == otherMcf.transactionIsolation;

   }

   protected synchronized XADataSource getXADataSource() throws ResourceException
   {	  
	  if(xadsSelector != null)
	  {
		   XAData xada = (XAData)xadsSelector.getUrlObject();
		   return xada.xads;
	  }
      if (xads == null)
      {
         if (xaDataSourceClass == null)
            throw new JBossResourceException("No XADataSourceClass supplied!");
         try
         {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(xaDataSourceClass);
            xads = (XADataSource) clazz.newInstance();
            Class[] NOCLASSES = new Class[] {};
            for (Map.Entry<String, String> entry : xaProps.entrySet())
            {
               String name = entry.getKey();
               String value = entry.getValue();      
               char firstCharName = Character.toUpperCase(name.charAt(0));
           	   if (name.length() > 1)
            	       name = firstCharName+name.substring(1);
           	   else
            		   name = ""+firstCharName;            		              	                              		              	                 
               //This is a bad solution.  On the other hand the only known example
               // of a setter with no getter is for Oracle with password.
               //Anyway, each xadatasource implementation should get its
               //own subclass of this that explicitly sets the
               //properties individually.
               Class type = null;
               try
               {
                  Method getter = clazz.getMethod("get" + name, NOCLASSES);
                  type = getter.getReturnType();
               }
               catch (NoSuchMethodException e)
               {
                  try
                  {
                     //HACK for now until we can rethink the XADataSourceProperties variable and pass type information
                     Method isMethod = clazz.getMethod("is" + name, NOCLASSES);
                     type = isMethod.getReturnType();
                     
                  }
                  catch(NoSuchMethodException nsme)
                  {
                     type = String.class;                     
                  }
               }

               Method setter = clazz.getMethod("set" + name, new Class[] { type });
               PropertyEditor editor = PropertyEditorFinder.getInstance().find(type);
               if (editor == null)
                  throw new JBossResourceException("No property editor found for type: " + type);
               editor.setAsText(value);
               setter.invoke(xads, new Object[] { editor.getValue() });
            }
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new JBossResourceException("Class not found for XADataSource " + xaDataSourceClass, cnfe);
         }
         catch (InstantiationException ie)
         {
            throw new JBossResourceException("Could not create an XADataSource: ", ie);
         }
         catch (IllegalAccessException iae)
         {
            throw new JBossResourceException("Could not set a property: ", iae);
         }
         catch (IllegalArgumentException iae)
         {
            throw new JBossResourceException("Could not set a property: ", iae);
         }
         catch (InvocationTargetException ite)
         {
            throw new JBossResourceException("Could not invoke setter on XADataSource: ", ite);
         }
         catch (NoSuchMethodException nsme)
         {
            throw new JBossResourceException("Could not find accessor on XADataSource: ", nsme);
         }
      }
      return xads;
   }
}
