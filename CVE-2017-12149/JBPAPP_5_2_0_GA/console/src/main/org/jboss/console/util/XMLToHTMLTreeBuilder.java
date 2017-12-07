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
package org.jboss.console.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;

/**
 * Utility to take xml string and convert it to a javascript based tree within html.
 *
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 */
public class XMLToHTMLTreeBuilder
{
   /**
    * Expects the xml to string to be in same format as calling JNDIView.listXML().
    * Will then build a tree in html that uses javascript.  Depends on dtree.js script
    * being available.
    *
    * @param xml
    * @return
    * @throws DocumentException
    */
   public static String convertJNDIXML(String xml) throws DocumentException
   {
      Document jndiXml = DocumentHelper.parseText(xml);

      String scriptHead = "<div class=\"dtree\"><script type=\"text/javascript\" src=\"dtree.js\"></script>\n";

      String ejbTree = convertEjbModule(jndiXml);

      String contextTree = convertContext(jndiXml);

      return scriptHead + ejbTree + "<p>" + contextTree;
   }

   private static String convertContext(Document jndiXml)
   {
      StringBuffer html = new StringBuffer();

      Element jndiRoot = jndiXml.getRootElement();

      List contextRoot = jndiRoot.elements("context");
      int globalCounter = 0;
      if (contextRoot.size() > 0)
      {
         html.append(createTreeCommandLinks("contextTree"));

         // create tree and add root node
         html.append("<script type=\"text/javascript\">\n<!--\n");
         html.append("contextTree = new dTree('contextTree');\n");
         html.append("contextTree.add(" + globalCounter++ + ",-1,'JNDI Root Context');\n");


         int parentId = 0;

         Iterator itr = contextRoot.iterator();
         while (itr.hasNext())
         {
            Element contextElm = (Element) itr.next();
            String contextElmName = contextElm.getName();
            Element nameSub = contextElm.element("name");
            if (nameSub != null)
            {
               contextElmName = nameSub.getText();
            }
            //html.append("contextTree.add(" + globalCounter++ + ", " + parentId + "," + contextElmName + ");");
            html.append(add("contextTree", globalCounter++, parentId, contextElmName));
            //parentId++;

            String[] searchNames = new String[]{"context", "leaf"};

            globalCounter = buildTree(contextElm, searchNames, html, globalCounter, globalCounter - 1, "contextTree");

         }

         html.append("document.write(contextTree);");
         html.append("\n//-->\n</script>");
         html.append("\n</div>\n");
      }


      return html.toString();
   }

   private static String createTreeCommandLinks(String treeName)
   {
      return "<p><a href=\"javascript: " + treeName + ".openAll();\">Expands all</a> | <a href=\"javascript: " + treeName + ".closeAll();\">Collapse all</a></p>";
   }

   private static String convertEjbModule(Document jndiXml)
   {
      StringBuffer html = new StringBuffer();

      Element jndiRoot = jndiXml.getRootElement();

      List ejbModules = jndiRoot.elements("ejbmodule");
      int globalCounter = 0;
      if (ejbModules.size() > 0)
      {
         html.append(createTreeCommandLinks("ejbTree"));

         // create tree and add root node
         html.append("<script type=\"text/javascript\">\n<!--\n");
         html.append("ejbTree = new dTree('ejbTree');\n");
         html.append("ejbTree.add(" + globalCounter++ + ",-1,'EJB Modules');\n");


         int parentId = 0;

         Iterator itr = ejbModules.iterator();
         while (itr.hasNext())
         {
            Element ejbElm = (Element) itr.next();
            String ejbElmName = ejbElm.getName();
            //html.append("ejbTree.add(" + globalCounter++ + ", " + parentId + "," + ejbElmName + ");");
            html.append(add("ejbTree", globalCounter++, parentId, ejbElmName));
            parentId++;

            String[] searchNames = new String[]{"context", "leaf"};

            globalCounter = buildTree(ejbElm, searchNames, html, globalCounter, parentId, "ejbTree");


         }

         html.append("document.write(ejbTree);");
         html.append("\n//-->\n</script>");
      }


      return html.toString();
   }

   private static int buildTree(Element ejbElm, String[] searchNames, StringBuffer html,
                                int globalCounter, int parentId, String treeName)
   {
      if (searchNames != null)
      {
         for (int x = 0; x < searchNames.length; x++)
         {
            String searchName = searchNames[x];

            List contextElms = ejbElm.elements(searchName);
            Iterator elmItr = contextElms.iterator();
            while (elmItr.hasNext())
            {
               Element contextElm = (Element) elmItr.next();

               String name = "";
               String type = "";
               String typeValue = "";

               // check for context name
               Element nameElm = contextElm.element("name");
               if (nameElm != null)
               {
                  name = nameElm.getText();
               }
               Element attrElem = contextElm.element("attribute");
               if (attrElem != null)
               {
                  type = attrElem.attributeValue("name");
                  typeValue = attrElem.getText();
               }

               //html.append("treeName.add(" + globalCounter++ + ", " + parentId + ", '" + name + " -- " + type + "[" + typeValue + "]');");
               html.append(add(treeName, globalCounter++, parentId, name + " -- " + type + "[" + typeValue + "]"));

               // now recurse
               globalCounter = buildTree(contextElm, searchNames, html, globalCounter, globalCounter - 1, treeName);

            }
         }
      }
      return globalCounter;
   }

   private static String add(String tree, int global, int parent, String name)
   {
      return tree + ".add(" + global + ", " + parent + ", '" + name + "');\n";
   }


   public static void main(String[] args)
   {
      String xml = "<jndi>\n" +
            "\t<ejbmodule>\n" +
            "\t\t<file>null</file>\n" +
            "\t\t<context>\n" +
            "\t\t\t<name>java:comp</name>\n" +
            "\t\t\t<attribute name=\"bean\">MEJB</attribute>\n" +
            "\t\t\t<context>\n" +
            "\t\t\t\t<name>env</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t\t<leaf>\n" +
            "\t\t\t\t\t<name>Server-Name</name>\n" +
            "\t\t\t\t\t<attribute name=\"class\">java.lang.String</attribute>\n" +
            "\t\t\t\t</leaf>\n" +
            "\t\t\t</context>\n" +
            "\t\t</context>\n" +
            "\t</ejbmodule>\n" +
            "\t<context>\n" +
            "\t\t<name>java:</name>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>XAConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyXAConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>DefaultDS</name>\n" +
            "\t\t\t<attribute name=\"class\">javax.sql.DataSource</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>SecurityProxyFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.security.SubjectSecurityProxyFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>DefaultJMSProvider</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.jms.jndi.JBossMQProvider</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<context>\n" +
            "\t\t\t<name>comp</name>\n" +
            "\t\t\t<attribute name=\"class\">javax.naming.Context</attribute>\n" +
            "\t\t</context>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>ConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>JmsXA</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.resource.adapter.jms.JmsConnectionFactoryImpl</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<context>\n" +
            "\t\t\t<name>jaas</name>\n" +
            "\t\t\t<attribute name=\"class\">javax.naming.Context</attribute>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>JmsXARealm</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.security.plugins.SecurityDomainContext</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>jbossmq</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.security.plugins.SecurityDomainContext</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>HsqlDbRealm</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.security.plugins.SecurityDomainContext</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t</context>\n" +
            "\t\t<context>\n" +
            "\t\t\t<name>timedCacheFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">javax.naming.Context</attribute>\n" +
            "\t\t\t<error>\n" +
            "\t\t\t\t<message>Failed to list contents of: timedCacheFactory, errmsg=null</message>\n" +
            "\t\t\t</error>\n" +
            "\t\t</context>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>TransactionPropagationContextExporter</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.tm.TransactionPropagationContextFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>Mail</name>\n" +
            "\t\t\t<attribute name=\"class\">javax.mail.Session</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>StdJMSPool</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.jms.asf.StdServerSessionPoolFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>TransactionPropagationContextImporter</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.tm.TransactionPropagationContextImporter</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>TransactionManager</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.tm.TxManager</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t</context>\n" +
            "\t<context>\n" +
            "\t\t<name>Global</name>\n" +
            "\t\t<context>\n" +
            "\t\t\t<name>jmx</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t<context>\n" +
            "\t\t\t\t<name>invoker</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t\t<leaf>\n" +
            "\t\t\t\t\t<name>RMIAdaptor</name>\n" +
            "\t\t\t\t\t<attribute name=\"class\">$Proxy38</attribute>\n" +
            "\t\t\t\t</leaf>\n" +
            "\t\t\t</context>\n" +
            "\t\t\t<context>\n" +
            "\t\t\t\t<name>rmi</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t\t<link-ref>\n" +
            "\t\t\t\t\t<name>RMIAdaptor</name>\n" +
            "\t\t\t\t\t<link>jmx/invoker/RMIAdaptor</link>\n" +
            "\t\t\t\t\t<attribute name=\"class\">javax.naming.LinkRef</attribute>\n" +
            "\t\t\t\t</link-ref>\n" +
            "\t\t\t</context>\n" +
            "\t\t</context>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>OIL2XAConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyXAConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>HTTPXAConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyXAConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>ConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>UserTransactionSessionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">$Proxy13</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>HTTPConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>XAConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyXAConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<context>\n" +
            "\t\t\t<name>invokers</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t<context>\n" +
            "\t\t\t\t<name>TCK3</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t\t<leaf>\n" +
            "\t\t\t\t\t<name>pooled</name>\n" +
            "\t\t\t\t\t<attribute name=\"class\">org.jboss.invocation.pooled.interfaces.PooledInvokerProxy</attribute>\n" +
            "\t\t\t\t</leaf>\n" +
            "\t\t\t\t<leaf>\n" +
            "\t\t\t\t\t<name>jrmp</name>\n" +
            "\t\t\t\t\t<attribute name=\"class\">org.jboss.invocation.jrmp.interfaces.JRMPInvokerProxy</attribute>\n" +
            "\t\t\t\t</leaf>\n" +
            "\t\t\t\t<leaf>\n" +
            "\t\t\t\t\t<name>http</name>\n" +
            "\t\t\t\t\t<attribute name=\"class\">org.jboss.invocation.http.interfaces.HttpInvokerProxy</attribute>\n" +
            "\t\t\t\t</leaf>\n" +
            "\t\t\t</context>\n" +
            "\t\t</context>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>UserTransaction</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.tm.usertx.client.ClientUserTransaction</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>RMIXAConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyXAConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>UIL2XAConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyXAConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<context>\n" +
            "\t\t\t<name>queue</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>A</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.mq.SpyQueue</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>testQueue</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.mq.SpyQueue</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>ex</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.mq.SpyQueue</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>DLQ</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.mq.SpyQueue</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>D</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.mq.SpyQueue</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>C</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.mq.SpyQueue</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>B</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.mq.SpyQueue</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t</context>\n" +
            "\t\t<context>\n" +
            "\t\t\t<name>topic</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>testDurableTopic</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.mq.SpyTopic</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>testTopic</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.mq.SpyTopic</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>securedTopic</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jboss.mq.SpyTopic</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t</context>\n" +
            "\t\t<context>\n" +
            "\t\t\t<name>console</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t<leaf>\n" +
            "\t\t\t\t<name>PluginManager</name>\n" +
            "\t\t\t\t<attribute name=\"class\">$Proxy39</attribute>\n" +
            "\t\t\t</leaf>\n" +
            "\t\t</context>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>UIL2ConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>HiLoKeyGeneratorFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.ejb.plugins.keygenerator.hilo.HiLoKeyGeneratorFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>RMIConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<context>\n" +
            "\t\t\t<name>ejb</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t<context>\n" +
            "\t\t\t\t<name>mgmt</name>\n" +
            "\t\t\t\t<attribute name=\"class\">org.jnp.interfaces.NamingContext</attribute>\n" +
            "\t\t\t\t<leaf>\n" +
            "\t\t\t\t\t<name>MEJB</name>\n" +
            "\t\t\t\t\t<attribute name=\"class\">$Proxy44</attribute>\n" +
            "\t\t\t\t</leaf>\n" +
            "\t\t\t</context>\n" +
            "\t\t</context>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>OIL2ConnectionFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.mq.SpyConnectionFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t\t<leaf>\n" +
            "\t\t\t<name>UUIDKeyGeneratorFactory</name>\n" +
            "\t\t\t<attribute name=\"class\">org.jboss.ejb.plugins.keygenerator.uuid.UUIDKeyGeneratorFactory</attribute>\n" +
            "\t\t</leaf>\n" +
            "\t</context>\n" +
            "</jndi>";

      String html = null;
      try
      {
         html = XMLToHTMLTreeBuilder.convertJNDIXML(xml);
      }
      catch (DocumentException e)
      {
         e.printStackTrace();
      }
      System.out.println("HTML output:\n\n" + html);
   }

}