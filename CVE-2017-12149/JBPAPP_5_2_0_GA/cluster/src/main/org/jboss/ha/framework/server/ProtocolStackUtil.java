/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.ha.framework.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.logging.Logger;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolData;
import org.jgroups.conf.ProtocolParameter;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.conf.XmlConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utilities related to JGroups protocol stack manipulation.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public final class ProtocolStackUtil
{  
   private static final Logger log = Logger.getLogger(ProtocolStackUtil.class);
   
   private static final String PROTOCOL_STACKS="protocol_stacks";
   private static final String STACK="stack";
   private static final String NAME="name";
   private static final String DESCR="description";
   private static final String CONFIG="config";

   /**
    * Parses the contents of <code>input</code> into a map of the
    * protocol stack configurations contained in the XML.
    * 
    * @param input stream which must contain XML content in the JGroups 
    *              <code>stacks.xml</code> format
    *              
    * @return a map of the protocol stack configurations contained in the XML
    * 
    * @throws IllegalArgumentException if <code>input</code> is <code>null</code>
    * @throws Exception
    */
   public static Map<String, ProtocolStackConfigInfo> parse(InputStream input) throws Exception 
   {
      if (input == null)
      {
         throw new IllegalArgumentException("null input");
      }
      
      DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
      factory.setValidating(false); //for now
      DocumentBuilder builder=factory.newDocumentBuilder();
      Document document=builder.parse(input);

      // The root element of the document should be the "config" element,
      // but the parser(Element) method checks this so a check is not
      // needed here.
      Element configElement = document.getDocumentElement();
      return parse(configElement);
   }
   
   /**
    * Parses the contents of <code>root</code> into a map of the
    * protocol stack configurations contained in the XML.
    * 
    * @param root document root node for XML content in the JGroups 
    *              <code>stacks.xml</code> format
    *              
    * @return a map of the protocol stack configurations contained in the XML
    * 
    * @throws IllegalArgumentException if <code>input</code> is <code>null</code>
    * @throws Exception
    */
   public static Map<String, ProtocolStackConfigInfo> parse(Element root) throws Exception 
   {
      if (root == null)
      {
         throw new IllegalArgumentException("null root");
      }
      
      String root_name = root.getNodeName();
      if (!PROTOCOL_STACKS.equals(root_name.trim().toLowerCase()))
      {
         throw new IOException("Invalid XML configuration: configuration does not start with a '" + 
                        PROTOCOL_STACKS + "' element");
      }

      Map<String, ProtocolStackConfigInfo> result = new HashMap<String, ProtocolStackConfigInfo>();

      NodeList tmp_stacks = root.getChildNodes();
      for (int i = 0; i < tmp_stacks.getLength(); i++)
      {
         Node node = tmp_stacks.item(i);
         if (node.getNodeType() != Node.ELEMENT_NODE)
            continue;

         Element stack = (Element) node;
         String tmp = stack.getNodeName();
         if (!STACK.equals(tmp.trim().toLowerCase()))
         {
            throw new IOException("Invalid configuration: didn't find a \"" + STACK + "\" element under \""
                  + PROTOCOL_STACKS + "\"");
         }

         NamedNodeMap attrs = stack.getAttributes();
         Node name = attrs.getNamedItem(NAME);
         String st_name = name.getNodeValue();
         Node descr=attrs.getNamedItem(DESCR);
         String stack_descr=descr.getNodeValue();
         if (log.isTraceEnabled())
         {
            log.trace("Parsing \"" + st_name + "\" (" + stack_descr + ")");
         }
         NodeList configs = stack.getChildNodes();
         for (int j = 0; j < configs.getLength(); j++)
         {
            Node tmp_config = configs.item(j);
            if (tmp_config.getNodeType() != Node.ELEMENT_NODE)
               continue;
            Element cfg = (Element) tmp_config;
            tmp = cfg.getNodeName();
            if (!CONFIG.equals(tmp))
            {
               throw new IOException("Invalid configuration: didn't find a \"" + 
                     CONFIG + "\" element under \"" + STACK + "\"");
            }

            XmlConfigurator conf = XmlConfigurator.getInstance(cfg);
            // fixes http://jira.jboss.com/jira/browse/JGRP-290
            ConfiguratorFactory.substituteVariables(conf); // replace vars with system props

            result.put(st_name, new ProtocolStackConfigInfo(st_name, stack_descr, conf));
         }
      }

      return result;
   }
   
   public static ProtocolData[] getProtocolData(ProtocolStackConfigurator config)
   {
      ProtocolData[] result = null;
      try
      {
         result = config.getProtocolStack();
      }
      catch (UnsupportedOperationException e)
      {
         String s = config.getProtocolStackString();
         String[] prots = s.split(":");
         result = new ProtocolData[prots.length];
         for (int i = 0; i < prots.length; i++)
         {
            ProtocolParameter[] params = null;
            int paren = prots[i].indexOf('(');
            String name = paren > - 1 ? prots[i].substring(0, paren) : prots[1];
            if (paren > -1 && paren < prots[1].length() - 2)
            {
               String unsplit = prots[i].substring(paren + 1, prots[i].length() -1);
               String[] split = unsplit.split(";");
                params = new ProtocolParameter[split.length];
               for (int j = 0; j < split.length; j++)
               {
                  String[] keyVal = split[j].split("=");
                  params[j] = new ProtocolParameter(keyVal[0], keyVal[1]);
               }
            }
            else
            {
               params = new ProtocolParameter[0];
            }
            
            result[i] = new ProtocolData(name, null, name, params);
         }
      }
      
      return result == null ? new ProtocolData[0] : result;
   }   
   
   /**
    * Prevent instantiation.
    */
   private ProtocolStackUtil()
   {
   }

}
