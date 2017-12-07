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
package org.jboss.ant;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jboss.common.beans.property.BeanUtils;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;

/**
 * JMX.java. An ant plugin to call managed operations and set attributes
 * on mbeans in a jboss jmx mbean server.
 * To use this plugin with Ant, place the jbossjmx-ant.jar together with the
 * jboss jars jboss-j2ee.jar and jboss-common-client.jar, and the sun jnet.jar in the
 * ant/lib directory you wish to use.
 * If the JMX invoker is secured, set the username and password attributes in the task.
 *
 * Here is an example from an ant build file.
 *
 * <target name="jmx">
 *   <taskdef name="jmx"
 *	classname="org.jboss.ant.JMX"/>
 *   <jmx adapterName="jmx:HP.home.home:rmi">
 *
 *     <propertyEditor type="java.math.BigDecimal" editor="org.jboss.common.beans.property.BigDecimalEditor"/>
 *     <propertyEditor type="java.util.Date" editor="org.jboss.common.beans.property.DateEditor"/>
 *
 *
 *      <!-- define classes -->
 *     <invoke target="fgm.sysadmin:service=DefineClasses"
 *             operation="defineClasses">
 *       <parameter type="java.lang.String" arg="defineclasses.xml"/>
 *     </invoke>
 *   </jmx>
 *
 *
 * Created: Tue Jun 11 20:17:44 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:dsnyder_lion@users.sourceforge.net">David Snyder</a>
 * @version
 */
public class JMX extends Task
{
   private String serverURL;

   private String adapterName = "jmx/invoker/RMIAdaptor";

   private String username;

   private String password;

   private List<Operation> ops = new ArrayList<Operation>();

   private List<PropertyEditorHolder> editors = new ArrayList<PropertyEditorHolder>();

   /**
    * Creates a new <code>JMX</code> instance.
    * Provides a default adapterName for the current server, so you only need to set it to
    * talk to a remote server.
    *
    * @exception Exception if an error occurs
    */
   public JMX() throws Exception
   {
   }

   /**
    * Use the <code>setServerURL</code> method to set the URL of the server
    * you wish to connect to.
    *
    * @param serverURL a <code>String</code> value
    */
   public void setServerURL(String serverURL)
   {
      this.serverURL = serverURL;
   }

   /**
    * Use the <code>setAdapterName</code> method to set the name the
    * adapter mbean is bound under in jndi.
    *
    * @param adapterName a <code>String</code> value
    */
   public void setAdapterName(String adapterName)
   {
      this.adapterName = adapterName;
   }

   /**
    * Use the <code>setUsername</code> method to set the username for 
    * the JMX invoker (if it's secured).
    * 
    * @param username a <code>String</code> value
    */
   public void setUsername(String username)
   {
      this.username = username;
   }

   /**
    * Use the <code>setPassword</code> method to set the password for 
    * the JMX invoker (if it's secured).
    * 
    * @param password a <code>String</code> value
    */
   public void setPassword(String password)
   {
      this.password = password;
   }

   /**
    * Use the <code>addInvoke</code> method to add an <invoke> operation.
    * Include as attributes the target ObjectName and operation name.
    * Include as sub-elements parameters: see addParameter in the Invoke class.
    *
    * @param invoke an <code>Invoke</code> value
    */
   public void addInvoke(Invoke invoke)
   {
      ops.add(invoke);
   }

   /**
    * Use the  <code>addSetAttribute</code> method to add a set-attribute
    * operation. Include as attributes the target ObjectName and the
    * the attribute name.  Include the value as a nested value tag
    * following the parameter syntax.
    *
    * @param setter a <code>Setter</code> value
    */
   public void addSetAttribute(Setter setter)
   {
      ops.add(setter);
   }

   /**
    * Use the  <code>addGetAttribute</code> method to add a get-attribute
    * operation. Include as attributes the target ObjectName, the
    * the attribute name, and a property name to hold the result of the
    * get-attribute operation.
    *
    * @param getter a <code>Getter</code> value
    */
   public void addGetAttribute(Getter getter)
   {
      ops.add(getter);
   }

   /**
    * Use the <code>addPropertyEditor</code> method to make a PropertyEditor
    * available for values.  Include attributes for the type and editor fully
    * qualified class name.
    *
    * @param peh a <code>PropertyEditorHolder</code> value
    */
   public void addPropertyEditor(PropertyEditorHolder peh)
   {
      editors.add(peh);
   }

   /**
    * The <code>execute</code> method is called by ant to execute the task.
    *
    * @exception BuildException if an error occurs
    */
   public void execute() throws BuildException
   {
      final ClassLoader origCL = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         try
         {
            for (int i = 0; i < editors.size(); i++)
            {
               editors.get(i).execute();
            } // end of for ()

         }
         catch (Exception e)
         {
            e.printStackTrace();
            throw new BuildException("Could not register property editors: " + e);
         } // end of try-catch

         try
         {
            Properties props = new Properties();
            String factory = "org.jnp.interfaces.NamingContextFactory";
            if (username != null && password != null)
            {
               factory = "org.jboss.security.jndi.JndiLoginInitialContextFactory";
               props.put(Context.SECURITY_PRINCIPAL, username);
               props.put(Context.SECURITY_CREDENTIALS, password);
            }
            props.put(Context.INITIAL_CONTEXT_FACTORY, factory);
            props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

            if (serverURL == null || "".equals(serverURL))
            {
               props.put(Context.PROVIDER_URL, "jnp://localhost:1099");
            }
            else
            {
               props.put(Context.PROVIDER_URL, serverURL);
            }
            InitialContext ctx = new InitialContext(props);

            // if adapter is null, the use the default
            if (adapterName == null)
            {
               adapterName = "jmx/rmi/RMIAdaptor";//org.jboss.jmx.adaptor.rmi.RMIAdaptorService.DEFAULT_JNDI_NAME;
            }

            Object obj = ctx.lookup(adapterName);
            ctx.close();

            if (!(obj instanceof MBeanServerConnection))
            {
               throw new ClassCastException("Object not of type: MBeanServerConnection, but: "
                     + (obj == null ? "not found" : obj.getClass().getName()));
            }

            MBeanServerConnection server = (MBeanServerConnection) obj;

            for (int i = 0; i < ops.size(); i++)
            {
               Operation op = ops.get(i);
               op.execute(server, this);
            } // end of for ()

         }
         catch (Exception e)
         {
            e.printStackTrace();
            throw new BuildException("problem: " + e);
         } // end of try-catch
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(origCL);
      }

   }

   /**
    * The interface <code>Operation</code> provides a common interface
    * for the sub-tasks..
    *
    */
   public static interface Operation
   {
      void execute(MBeanServerConnection server, Task parent) throws Exception;
   }

   /**
    * The class <code>Invoke</code> specifies the invocation of a
    * managed operation.
    *
    */
   public static class Invoke implements Operation
   {
      private ObjectName target;

      private String property;

      private String operation;

      private List<Param> params = new ArrayList<Param>();

      /**
       * The <code>setProperty</code> method sets the name of the property
       * that will contain the result of the operation.
       *
       * @param property a <code>String</code> value
       */
      public void setProperty(String property)
      {
         this.property = property;
      }

      /**
       * The <code>setTarget</code> method sets the ObjectName
       * of the target mbean.
       *
       * @param target an <code>ObjectName</code> value
       */
      public void setTarget(ObjectName target)
      {
         this.target = target;
      }

      /**
       * The <code>setOperation</code> method specifies the operation to
       * be performed.
       *
       * @param operation a <code>String</code> value
       */
      public void setOperation(String operation)
      {
         this.operation = operation;
      }

      /**
       * The <code>addParameter</code> method adds a parameter for
       * the operation. You must specify type and value.
       *
       * @param param a <code>Param</code> value
       */
      public void addParameter(Param param)
      {
         params.add(param);
      }

      public void execute(MBeanServerConnection server, Task parent) throws Exception
      {
         int paramCount = params.size();
         Object[] args = new Object[paramCount];
         String[] types = new String[paramCount];
         int pos = 0;
         for (int i = 0; i < params.size(); i++)
         {
            Param p = params.get(i);
            args[pos] = p.getValue();
            types[pos] = p.getType();
            pos++;
         } // end of for ()
         Object result = server.invoke(target, operation, args, types);
         if ((property != null) && (result != null))
         {
            parent.getProject().setProperty(property, result.toString());
         }
      }
   }

   /**
    * The class <code>Setter</code> specifies setting an attribute
    * value on an mbean.
    *
    */
   public static class Setter implements Operation
   {
      private ObjectName target;

      private String attribute;

      private Param value;

      /**
       * The <code>setTarget</code> method sets the ObjectName
       * of the target mbean.
       *
       * @param target an <code>ObjectName</code> value
       */
      public void setTarget(ObjectName target)
      {
         this.target = target;
      }

      /**
       * The <code>setAttribute</code> method specifies the attribute to be set.
       *
       * @param attribute a <code>String</code> value
       */
      public void setAttribute(String attribute)
      {
         this.attribute = attribute;
      }

      /**
       * The <code>setValue</code> method specifies the value to be used.
       * The type is used to convert the value to the correct type.
       *
       * @param value a <code>Param</code> value
       */
      public void setValue(Param value)
      {
         this.value = value;
      }

      public void execute(MBeanServerConnection server, Task parent) throws Exception
      {
         Attribute att = new Attribute(attribute, value.getValue());
         server.setAttribute(target, att);
      }
   }

   /**
    * The class <code>Getter</code> specifies getting an attribute
    * value of an mbean.
    *
    */
   public static class Getter implements Operation
   {
      private ObjectName target;

      private String attribute;

      private String property;

      /**
       * The <code>setTarget</code> method sets the ObjectName
       * of the target mbean.
       *
       * @param target an <code>ObjectName</code> value
       */
      public void setTarget(ObjectName target)
      {
         this.target = target;
      }

      /**
       * The <code>setAttribute</code> method specifies the attribute to be
       * retrieved.
       *
       * @param attribute a <code>String</code> value
       */
      public void setAttribute(String attribute)
      {
         this.attribute = attribute;
      }

      /**
       * The <code>setProperty</code> method specifies the name of the property
       * to be set with the attribute value.
       *
       * @param property a <code>String</code> value
       */
      public void setProperty(String property)
      {
         this.property = property;
      }

      public void execute(MBeanServerConnection server, Task parent) throws Exception
      {
         Object result = server.getAttribute(target, attribute);
         if ((property != null) && (result != null))
         {
            parent.getProject().setProperty(property, result.toString());
         }
      }
   }

   /**
    * The class <code>Param</code> is used to represent a object by
    * means of a string representation of its value and its type.
    *
    */
   public static class Param
   {
      private String arg;

      private String type;

      /**
       * The <code>setArg</code> method sets the string representation
       * of the parameters value.
       *
       * @param arg a <code>String</code> value
       */
      public void setArg(String arg)
      {
         this.arg = arg;
      }

      public String getArg()
      {
         return arg;
      }

      /**
       * The <code>setType</code> method sets the fully qualified class
       * name of the type represented by the param object.
       *
       * @param type a <code>String</code> value
       */
      public void setType(String type)
      {
         this.type = type;
      }

      public String getType()
      {
         return type;
      }

      /**
       * The <code>getValue</code> method uses PropertyEditorFinder to convert
       * the string representation of the value to an object, which it returns.
       * The PropertyEditor to use is determined by the type specified.
       *
       * @return an <code>Object</code> value
       * @exception Exception if an error occurs
       */
      public Object getValue() throws Exception
      {
         PropertyEditor editor = PropertyEditorFinder.getInstance().find(BeanUtils.findClass(type));
         editor.setAsText(arg);
         return editor.getValue();
      }
   }

   /**
    * The class <code>PropertyEditorHolder</code> allows you to add a
    * PropertyEditor to the default set.
    *
    */
   public static class PropertyEditorHolder
   {
      private String type;

      private String editor;

      /**
       * The <code>setType</code> method specifies the return type from the
       * property editor.
       *
       * @param type a <code>String</code> value
       */
      public void setType(final String type)
      {
         this.type = type;
      }

      public String getType()
      {
         return type;
      }

      private Class<?> getTypeClass() throws ClassNotFoundException
      {
         //with a little luck, one of these will work with Ant's classloaders
         try
         {
            return Class.forName(type);
         }
         catch (ClassNotFoundException e)
         {
         } // end of try-catch
         try
         {
            return getClass().getClassLoader().loadClass(type);
         }
         catch (ClassNotFoundException e)
         {
         } // end of try-catch
         return Thread.currentThread().getContextClassLoader().loadClass(type);
      }

      /**
       * The <code>setEditor</code> method specifies the fully qualified
       * class name of the PropertyEditor for the type specified in the type field.
       *
       * @param editor a <code>String</code> value
       */
      public void setEditor(final String editor)
      {
         this.editor = editor;
      }

      public String getEditor()
      {
         return editor;
      }

      private Class<? extends PropertyEditor> getEditorClass() throws ClassNotFoundException
      {
         //with a little luck, one of these will work with Ant's classloaders
         try
         {
            return (Class<? extends PropertyEditor>) Class.forName(editor);
         }
         catch (ClassNotFoundException e)
         {
         } // end of try-catch
         try
         {
            return (Class<? extends PropertyEditor>)getClass().getClassLoader().loadClass(editor);
         }
         catch (ClassNotFoundException e)
         {
         } // end of try-catch
         return (Class<? extends PropertyEditor>)Thread.currentThread().getContextClassLoader().loadClass(editor);
      }

      public void execute() throws ClassNotFoundException
      {
         PropertyEditorFinder.getInstance().register(getTypeClass(), getEditorClass());
      }
   }

}// JMX
