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

import java.beans.PropertyEditor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.jboss.common.beans.property.BeanUtils;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.util.Strings;

/**
 * Sets one or more attributes in an MBean
 *
 * @author <a href="mailto:bela@jboss.com">Bela Ban</a>
 * @version $Revision: 113110 $
 */
public class SetAttrsCommand
   extends MBeanServerCommand
{
   private ObjectName objectName;

   /** List<String>. Contains names and values */
   private List attributeNames = new ArrayList(5);


   public SetAttrsCommand()
   {
      super("setattrs", "Set the values of one or more MBean attributes");
   }

   public void displayHelp()
   {
      PrintWriter out = context.getWriter();

      out.println(desc);
      out.println();
      out.println("usage: " + name + " [options] <name> [<attr value>+]");
      out.println("options:");
      out.println("    --noprefix    Do not display attribute name prefixes");
      out.println("    --            Stop processing options");

      out.flush();
   }

   private boolean processArguments(final String[] args)
      throws CommandException
   {
      log.debug("processing arguments: " + Strings.join(args, ","));

      if (args.length == 0)
      {
         throw new CommandException("Command requires arguments");
      }

      String sopts = "-:";
      LongOpt[] lopts =
         {
            new LongOpt("noprefix", LongOpt.NO_ARGUMENT, null, 0x1000),
         };

      Getopt getopt = new Getopt(null, args, sopts, lopts);
      getopt.setOpterr(false);

      int code;
      int argidx = 0;

      while ((code = getopt.getopt()) != -1)
      {
         switch (code)
         {
            case ':':
               throw new CommandException
                  ("Option requires an argument: " + args[getopt.getOptind() - 1]);

            case '?':
               throw new CommandException
                  ("Invalid (or ambiguous) option: " + args[getopt.getOptind() - 1]);

            case 0x1000:
               break;
                  
               // non-option arguments
            case 1:
               {
                  String arg = getopt.getOptarg();

                  switch (argidx++)
                  {
                     case 0:
                        objectName = createObjectName(arg);
                        log.debug("mbean name: " + objectName);
                        break;

                     default:
                        log.debug("adding attribute name: " + arg);
                        attributeNames.add(arg);
                        break;
                  }
                  break;
               }
         }
      }

      return true;
   }

   public void execute(String[] args) throws Exception
   {
      processArguments(args);

      if (objectName == null)
         throw new CommandException("Missing object name");

      log.debug("attribute names: " + attributeNames);

      MBeanServerConnection server = getMBeanServer();
      if (attributeNames.size() == 0)
      {
         throw new CommandException("at least 1 attribute and value needs to be defined");
      }

      MBeanInfo info = server.getMBeanInfo(objectName);
      MBeanAttributeInfo[] attribute_info = info.getAttributes();
      String type;
      AttributeList attrs = new AttributeList(attributeNames.size());
      Attribute attr;
      String attr_name;
      Object attr_value, real_value;
      MBeanAttributeInfo attr_info;


      for (Iterator it = attributeNames.iterator(); it.hasNext();)
      {
         attr_name = (String) it.next();
         attr_value = it.next();

         attr_info = findAttribute(attr_name, attribute_info);
         if (attr_info == null)
            throw new CommandException("attribute " + attr_name + " not found");
         type = attr_info.getType();

         PropertyEditor editor = PropertyEditorFinder.getInstance().find(BeanUtils.findClass(type));
         editor.setAsText((String) attr_value);
         real_value = editor.getValue();

         attr = new Attribute(attr_name, real_value);
         attrs.add(attr);
      }

      AttributeList ret = server.setAttributes(objectName, attrs);
      System.out.println("The following attributes were set successfuly:");
      if (ret.size() > 0)
      {
         for (Iterator it = ret.iterator(); it.hasNext();)
         {
            Attribute a = (Attribute) it.next();
            System.out.println(a.getName() + "=" + a.getValue());
         }
      }
   }

   private MBeanAttributeInfo findAttribute(String attr_name,
      MBeanAttributeInfo[] attribute_info)
   {
      for (int i = 0; i < attribute_info.length; i++)
      {
         MBeanAttributeInfo mBeanAttributeInfo = attribute_info[i];
         if (mBeanAttributeInfo.getName().equals(attr_name))
            return mBeanAttributeInfo;
      }
      return null;
   }
}
