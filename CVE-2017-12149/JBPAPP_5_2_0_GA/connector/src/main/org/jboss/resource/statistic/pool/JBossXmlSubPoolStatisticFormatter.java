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
package org.jboss.resource.statistic.pool;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.resource.statistic.JBossStatistics;
import org.jboss.resource.statistic.formatter.StatisticsFormatter;
import org.jboss.resource.statistic.formatter.StatisticsFormatterException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * A XmlStatisticsFormatter.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 92075 $
 */
public class JBossXmlSubPoolStatisticFormatter implements StatisticsFormatter
{

   public Object formatSubPoolStatistics(Collection subPoolStatistics)
   {
      final Document doc = createDocument();
      final Element root = doc.createElement("subpool-statistics");
      doc.appendChild(root);
      
      for(Iterator iter = subPoolStatistics.iterator(); iter.hasNext();){
         
         JBossSubPoolStatistics stat = (JBossSubPoolStatistics)iter.next();
         createChildNode(doc, root, stat);
       
      }
      
      return getDocumentAsString(doc);
   }

   public Object formatSubPoolStatistics(ManagedConnectionPoolStatistics stats)
   {
      return formatSubPoolStatistics(stats.getSubPools());
   }
   
   private void createChildNode(Document doc, Element root, JBossSubPoolStatistics stat){
    
      root.appendChild(createTextNode(doc, "max-connections-in-use", String.valueOf(stat.getMaxConnectionsInUse())));
      root.appendChild(createTextNode(doc, "connections-in-use", String.valueOf(stat.getConnectionsInUse())));
      root.appendChild(createTextNode(doc, "connections-destroyed", String.valueOf(stat.getConnectionsDestroyed())));
      root.appendChild(createTextNode(doc, "available-connections", String.valueOf(stat.getAvailableConnections())));
      
   }
   
   private Element createTextNode(Document doc, String name, String value){
      
      Text node = doc.createTextNode(name);
      node.setNodeValue(String.valueOf(value));
      Element child = doc.createElement(name);
      child.appendChild(node);
      return child;
      
      
   }
  
   private Document createDocument(){
      
      try
      {
         Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
         return doc; 
      }
      catch (ParserConfigurationException e)
      {
         throw new StatisticsFormatterException(e.getMessage());
         
      }
      
   }
   
   private String getDocumentAsString(Document doc){
      
      try
      {
         final Source source = new DOMSource(doc);
         final StringWriter writer = new StringWriter();
         final StreamResult result = new StreamResult(writer);
         TransformerFactory.newInstance().newTransformer().transform(source, result);
         return writer.toString();
      }
      catch (TransformerException e)
      {
         throw new StatisticsFormatterException(e.getMessage());
         
      }      
            
   }

   public Object formatStatistics(JBossStatistics stats)
   {
      if(!(stats instanceof ManagedConnectionPoolStatistics)){

         throw new IllegalArgumentException("Error: invalid statistics implementaiton for formatter.");
         
      }
      
      final ManagedConnectionPoolStatistics poolStats = (ManagedConnectionPoolStatistics)stats;
      return formatSubPoolStatistics(poolStats);
   }

}
