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
package org.jboss.console.twiddle.command;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.management.j2ee.J2EETypeConstants;

/** 
 * Command to print out jsr77 related information.
 *
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81010 $
 */
public class JSR77Command  extends MBeanServerCommand
{
   private static final String INDENT = "  ";
   
   private static final Map j2eeTypeMap = new HashMap();
   static
   {
      j2eeTypeMap.put(J2EETypeConstants.J2EEDomain,
         new J2EEType(J2EETypeConstants.J2EEDomain, null, new String[] { "servers" }, null));
      j2eeTypeMap.put(J2EETypeConstants.J2EEServer,
         new J2EEType(J2EETypeConstants.J2EEServer, new String[] { "serverVendor", "serverVersion" }, new String[] { "deployedObjects", "resources", "javaVMs" }, null));
      j2eeTypeMap.put(J2EETypeConstants.JVM,
         new J2EEType(J2EETypeConstants.JVM, new String[] { "javaVersion", "javaVendor", "node" }, null, null));
      j2eeTypeMap.put(J2EETypeConstants.J2EEApplication,
         new J2EEType(J2EETypeConstants.J2EEApplication, new String[] { /*"deploymentDescriptor"*/ }, null, new String[] { "modules" }));
      j2eeTypeMap.put(J2EETypeConstants.AppClientModule,
         new J2EEType(J2EETypeConstants.AppClientModule, null, null, null));
      j2eeTypeMap.put(J2EETypeConstants.EJBModule,
         new J2EEType(J2EETypeConstants.EJBModule, null, new String[] { "ejbs" }, null));
      j2eeTypeMap.put(J2EETypeConstants.EntityBean,
         new J2EEType(J2EETypeConstants.EntityBean, new String[] { "JndiName" }, null, null));
      j2eeTypeMap.put(J2EETypeConstants.MessageDrivenBean,
         new J2EEType(J2EETypeConstants.MessageDrivenBean, new String[] { "JndiName" }, null, null));
      j2eeTypeMap.put(J2EETypeConstants.StatelessSessionBean,
         new J2EEType(J2EETypeConstants.StatelessSessionBean, new String[] { "JndiName" }, null, null));
      j2eeTypeMap.put(J2EETypeConstants.StatefulSessionBean,
         new J2EEType(J2EETypeConstants.StatefulSessionBean, new String[] { "JndiName" }, null, null));       
      j2eeTypeMap.put(J2EETypeConstants.WebModule,
         new J2EEType(J2EETypeConstants.WebModule, new String[] { /*"deploymentDescriptor"*/ }, new String[] { "servlets" }, null));
      j2eeTypeMap.put(J2EETypeConstants.Servlet,
         new J2EEType(J2EETypeConstants.Servlet, null, null, null));
      j2eeTypeMap.put(J2EETypeConstants.ServiceModule,
         new J2EEType(J2EETypeConstants.ServiceModule, new String[] { /*"deploymentDescriptor"*/ }, new String[] { "MBeans" }, null));
      j2eeTypeMap.put(J2EETypeConstants.MBean,
         new J2EEType(J2EETypeConstants.MBean, new String[] { "stateMonitored", "StateString" }, null, null));
      j2eeTypeMap.put(J2EETypeConstants.ResourceAdapterModule,
         new J2EEType(J2EETypeConstants.ResourceAdapterModule, new String[] { /*"deploymentDescriptor"*/ }, new String[] { "resourceAdapters" }, null));
      j2eeTypeMap.put(J2EETypeConstants.ResourceAdapter,
         new J2EEType(J2EETypeConstants.ResourceAdapter, null, new String[] { "jcaResource" }, null));
      j2eeTypeMap.put(J2EETypeConstants.JCAResource,
         new J2EEType(J2EETypeConstants.JCAResource, null, new String[] { "connectionFactories" }, null));
      j2eeTypeMap.put(J2EETypeConstants.JCAConnectionFactory,
         new J2EEType(J2EETypeConstants.JCAConnectionFactory, null, new String[] { "managedConnectionFactory" }, null));
      j2eeTypeMap.put(J2EETypeConstants.JCAManagedConnectionFactory,
         new J2EEType(J2EETypeConstants.JCAManagedConnectionFactory, null, null, null));
      j2eeTypeMap.put(J2EETypeConstants.JNDIResource,
         new J2EEType(J2EETypeConstants.JNDIResource, new String[] { "StateString" }, null, null));      
      j2eeTypeMap.put(J2EETypeConstants.JTAResource,
         new J2EEType(J2EETypeConstants.JTAResource, null, null, null));
      j2eeTypeMap.put(J2EETypeConstants.RMI_IIOPResource,
         new J2EEType(J2EETypeConstants.RMI_IIOPResource, null, null, null));      
      j2eeTypeMap.put(J2EETypeConstants.JavaMailResource,
         new J2EEType(J2EETypeConstants.JavaMailResource, new String[] { "StateString" }, null, null));
      j2eeTypeMap.put(J2EETypeConstants.JMSResource,
         new J2EEType(J2EETypeConstants.JMSResource, null, null, null));
   }
   
   /**
    * Default CTOR
    */
   public JSR77Command()
   {
      super("jsr77", "Print out JSR77 related information");
   }

   public void displayHelp()
   {
      PrintWriter out = context.getWriter();

      out.println(desc);
      out.println();      
      out.println("Print out JSR77 related information");
      out.println();
      out.println("Usage: " + name);
      out.println();

      out.flush();
   }

   public void execute(String[] args) throws Exception
   {
      //if (args.length != 1)
      //{
      //   throw new CommandException("Missing object name");
      //}
      //ObjectName target = super.createObjectName(args[0]);

      MBeanServerConnection server = super.getMBeanServer();
      PrintWriter out = context.getWriter();
      
      Set jsr77Domains = locateJSR77Domains(server);
      for (Iterator i = jsr77Domains.iterator(); i.hasNext(); )
      {
         printJsr77Node(server, (ObjectName)i.next(), out, 0);
      }      
      out.flush();
   }

   /**
    * Locate the ObjectNames of any JSR77 registered domains
    * 
    * @param server the mbean server to query
    * @return a set of JSR77 ObjectNames (can be empty)
    * @throws Exception in case of error
    */
   private static Set locateJSR77Domains(MBeanServerConnection server) throws Exception
   {
      // The potential list of domain starting points. According to the
      // spec we should be looking for *:j2eeType=J2EEDomain,* where
      // the value of the "name" attribute matches the domain name.
      ObjectName domainNameQuery = new ObjectName("*:j2eeType=J2EEDomain,*");
      Set domainNames = server.queryNames(domainNameQuery, null);
      
      for (Iterator i = domainNames.iterator(); i.hasNext(); )
      {
         ObjectName objectName = (ObjectName)i.next();

         if (objectName.getDomain().equals(objectName.getKeyProperty("name")) == false)
         {
            // value of "name" attribute doesn't match the domain name
            // remove from the domain set
            i.remove();
         }
      }
      return domainNames;
   } 

   /**
    * Generic traversal of a JSR77 node
    */
   private static void printJsr77Node(MBeanServerConnection server, ObjectName node, PrintWriter out, int depth) throws Exception
   {
      String j2eeType = node.getKeyProperty("j2eeType");
      String name = node.getKeyProperty("name");
      
      J2EEType type = (J2EEType)j2eeTypeMap.get(j2eeType);
      if (type == null)
      {
         println(out, depth, "Unknown j2eeType=" + j2eeType);
      }
      else
      {
         // print the node's j2eeType
         println(out, depth, j2eeType + "=" + name);
         
         // print attributes, if any
         if (type.attributes != null)
         {
            AttributeList attrs = server.getAttributes(node, type.attributes);
            for (int i = 0; i < attrs.size(); i++)
            {
               Attribute attr = (Attribute)attrs.get(i);
               println(out, depth + 1, attr.getName() + "=" + attr.getValue());
            }
         }
         
         // print out associations, if any
         if (type.associations != null)
         {
            for (int i = 0; i < type.associations.length; i++)
            {
               String association = type.associations[i];
               String[] children = (String[])server.getAttribute(node, association);
               for (int j = 0; j < children.length; j++)
               {
                  String child = children[j];
                  ObjectName subnode = new ObjectName(child);
                  String subnodeType = subnode.getKeyProperty("j2eeType");
                  String subnodeName = subnode.getKeyProperty("name");
                  
                  println(out, depth + 1, subnodeType + "=" + subnodeName);
               }
            }
         }
         
         // recurse into subnodes, if any
         if (type.containment != null)
         {
            for (int i = 0; i < type.containment.length; i++)
            {
               String containment = type.containment[i];
               Object result = server.getAttribute(node, containment);
               // this is probably a bug, it should be String
               if (result instanceof ObjectName)
               {
                  printJsr77Node(server, (ObjectName)result, out, depth + 1);
               }
               else if (result instanceof String)
               {
                  printJsr77Node(server, new ObjectName((String)result), out, depth + 1);                  
               }
               else if (result instanceof String[])
               {
                  String[] children = (String[])result;
                  for (int j = 0; j < children.length; j++)
                  {
                     String child = children[j];
                     ObjectName subnode = new ObjectName(child);
                     if (depth < 2)
                        out.println();
                     printJsr77Node(server, subnode, out, depth + 1);
                  }
               }
            }
         }         
      }
   }
   
   /**
    * Println with indentation
    */
   private static void println(PrintWriter out, int depth, String msg)
   {
      for (int i = 0; i < depth; i++)
      {
         out.print(INDENT);
      }
      out.println(msg);
   }
   
   /**
    * Simple data holder class to record information
    * for the various JSR77 j2eeType(s)
    */
   private static class J2EEType
   {
      public String type;
      public String[] attributes;
      public String[] containment;
      public String[] associations;
      
      public J2EEType(String type, String[] attributes, String[] containment, String[] associations)
      {
         this.type = type;
         this.attributes = attributes;
         this.containment = containment;
         this.associations = associations;
      }
   }
}
