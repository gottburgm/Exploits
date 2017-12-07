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
package org.jboss.test.jca.test;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.resource.statistic.JBossStatistics;
import org.jboss.resource.statistic.formatter.StatisticsFormatter;
import org.jboss.resource.statistic.pool.JBossDefaultSubPoolStatisticFormatter;
import org.jboss.resource.statistic.pool.JBossXmlSubPoolStatisticFormatter;
import org.jboss.resource.statistic.pool.ManagedConnectionPoolStatistics;
import org.jboss.test.JBossTestCase;
import org.jboss.test.jca.statistics.StatisticsHelper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * A StatisticsReporterUnitTestCase.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 113159 $
 */
public class StatisticsFormatterUnitTestCase extends JBossTestCase
{
   private static final Logger log = Logger.getLogger(StatisticsFormatterUnitTestCase.class);
   
   private static final String DEFAULT_FORMATTER = "org.jboss.resource.statistic.pool.JBossDefaultSubPoolStatisticFormatter";
   private static final String XML_FORMATTER = "org.jboss.resource.statistic.pool.JBossXmlSubPoolStatisticFormatter";
   
   private static final ObjectName POOL_NAME = ObjectNameFactory.create("jboss.jca:service=ManagedConnectionPool,name=StatsDS");
   private static final String ATTRIBUTE_NAME = "StatisticsFormatter";
   private static final String RAW_STATS_METHOD = "listStatistics";
   private static final String FORMATTED_STATS_METHOD = "listFormattedSubPoolStatistics";
   private static final String FLUSH_METHOD = "flush";
   
   public StatisticsFormatterUnitTestCase(String name){
    
      super(name);
      
   }
   
   /**
    * Test basic formatter MBean.
    * 
    * @throws Exception
    */
   public void testDefaultFormatterSetting() throws Exception{

      String formatter = StatisticsHelper.getStatisticsFormatter(getServer());
      log.debug("Found default statistics formatter " + formatter);
      super.assertEquals(formatter, DEFAULT_FORMATTER);
      
   }
   
   /**
    * FIXME Comment this
    * 
    * @throws Exception
    */
   public void testRawStatistics() throws Exception{
      
      Object result = StatisticsHelper.listRawStatistics(getServer());
      
      assertTrue(result instanceof Serializable);
      assertTrue(result instanceof ManagedConnectionPoolStatistics);      
            
   }

   public void testDefaultFormattedStatistics() throws Exception{
      
      InitialContext initCtx = super.getInitialContext();
      DataSource ds = (DataSource)initCtx.lookup("StatsDS");
      Connection conn = ds.getConnection("sa", "");
      
      Object formattedStats = StatisticsHelper.listFormattedStatistics(getServer());

      assertTrue(formattedStats instanceof String);
      
      //Do a diff
      Object rawStatistics = StatisticsHelper.listRawStatistics(getServer());
      
      StatisticsFormatter defaultFormatter = StatisticsHelper.getDefaultFormatter();
      
      ManagedConnectionPoolStatistics stats = (ManagedConnectionPoolStatistics)rawStatistics;
      
      String rawFormat = (String)defaultFormatter.formatStatistics((JBossStatistics)stats);
      
      assertEquals(formattedStats, rawFormat);
      
      conn.close();
   }

   public void testConnectionInUse() throws Exception
   {
      String POOL_SEPERATOR = "------------------------------------------------------";

      StringBuffer expectedBeforeBuffer = new StringBuffer();
      expectedBeforeBuffer.append("Sub Pool Statistics: \n");
      expectedBeforeBuffer.append("Sub Pool Count: 0\n");
      expectedBeforeBuffer.append(POOL_SEPERATOR);
      expectedBeforeBuffer.append("\n\n");

      StringBuffer expectedAfterBuffer = new StringBuffer();
      expectedAfterBuffer.append("Sub Pool Statistics: \n");
      expectedAfterBuffer.append("Sub Pool Count: 0\n");
      expectedAfterBuffer.append(POOL_SEPERATOR);
      expectedAfterBuffer.append("\n\n");

      StringBuffer expectedConn1Buffer = new StringBuffer();
      expectedConn1Buffer.append("Sub Pool Statistics: \n");
      expectedConn1Buffer.append("Sub Pool Count: 1\n");
      expectedConn1Buffer.append(POOL_SEPERATOR);
      expectedConn1Buffer.append("\n\n");
      expectedConn1Buffer.append("Available Connections Count: 9\n");
      expectedConn1Buffer.append("Max Connections In Use Count:1\n");
      expectedConn1Buffer.append("Connections Destroyed Count:0\n");
      expectedConn1Buffer.append("Connections In Use Count:1\n");
      expectedConn1Buffer.append("Total Block Time:\\d+\n");
      expectedConn1Buffer.append("Average Block Time For Sub Pool:\\d+\n");
      expectedConn1Buffer.append("Maximum Wait Time For Sub Pool:\\d+\n");
      expectedConn1Buffer.append("Total Timed Out:\\d+\n");
      expectedConn1Buffer.append("\n");
      expectedConn1Buffer.append(POOL_SEPERATOR);
      expectedConn1Buffer.append("\n\n");

      StringBuffer expectedConn2Buffer = new StringBuffer();
      expectedConn2Buffer.append("Sub Pool Statistics: \n");
      expectedConn2Buffer.append("Sub Pool Count: 1\n");
      expectedConn2Buffer.append(POOL_SEPERATOR);
      expectedConn2Buffer.append("\n\n");
      expectedConn2Buffer.append("Available Connections Count: 8\n");
      expectedConn2Buffer.append("Max Connections In Use Count:2\n");
      expectedConn2Buffer.append("Connections Destroyed Count:0\n");
      expectedConn2Buffer.append("Connections In Use Count:2\n");
      expectedConn2Buffer.append("Total Block Time:\\d+\n");
      expectedConn2Buffer.append("Average Block Time For Sub Pool:\\d+\n");
      expectedConn2Buffer.append("Maximum Wait Time For Sub Pool:\\d+\n");
      expectedConn2Buffer.append("Total Timed Out:\\d+\n");
      expectedConn2Buffer.append("\n");
      expectedConn2Buffer.append(POOL_SEPERATOR);
      expectedConn2Buffer.append("\n\n");

      InitialContext initCtx = super.getInitialContext();

      // Flush
      Object o = getServer().invoke(POOL_NAME, FLUSH_METHOD, new Object[0], new String[0]);

      // Before obtaining connections
      Object beforeStats = StatisticsHelper.listFormattedStatistics(getServer());

      assertTrue(beforeStats instanceof String);
      assertEquals(beforeStats, expectedBeforeBuffer.toString());

      // Obtaining connections
      DataSource ds = (DataSource)initCtx.lookup("StatsDS");
      Connection conn1 = ds.getConnection("sa", "");
      
      Object conn1Stats = StatisticsHelper.listFormattedStatistics(getServer());

      assertTrue(conn1Stats instanceof String);

      Pattern p1 = Pattern.compile(expectedConn1Buffer.toString());
      Matcher m1 = p1.matcher((String)conn1Stats);

      assertTrue(m1.matches());

      Connection conn2 = ds.getConnection("sa", "");
      
      Object conn2Stats = StatisticsHelper.listFormattedStatistics(getServer());

      assertTrue(conn2Stats instanceof String);

      Pattern p2 = Pattern.compile(expectedConn2Buffer.toString());
      Matcher m2 = p2.matcher((String)conn2Stats);

      assertTrue(m2.matches());

      conn1.close();
      conn2.close();

      // Flush
      o = getServer().invoke(POOL_NAME, FLUSH_METHOD, new Object[0], new String[0]);

      // After obtaining connections
      Object afterStats = StatisticsHelper.listFormattedStatistics(getServer());

      assertTrue(afterStats instanceof String);
      assertEquals(afterStats, expectedAfterBuffer.toString());
   }
   

   public void testXmlFormatterStatistics() throws Exception{

      InitialContext initCtx = super.getInitialContext();
      DataSource ds = (DataSource)initCtx.lookup("StatsDS");
      Connection conn = ds.getConnection("sa", "");
      final String originalFormatter = getStatisticsFormatter();
      try{
          setStatisticsFormatter(XML_FORMATTER);
          Object formattedStats = (String)getServer().invoke(POOL_NAME, FORMATTED_STATS_METHOD, new Object[0], new String[0]);
          
          assertTrue(formattedStats instanceof String);
          
          String xml = (String)formattedStats;
         
          DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          Document doc = builder.parse(new InputSource(new StringReader(xml)));   
          
          TransformerFactory transformerFactory = TransformerFactory.newInstance();
          Transformer transformer = transformerFactory.newTransformer();
          
          StringWriter xmlout = new StringWriter();
          StreamResult result = new StreamResult(xmlout);
    
          transformer.transform(new DOMSource(doc.getFirstChild()), result);
          
          ManagedConnectionPoolStatistics rawStatistics = (ManagedConnectionPoolStatistics)getServer().invoke(POOL_NAME, RAW_STATS_METHOD, new Object[0], new String[0]);
          JBossXmlSubPoolStatisticFormatter xmlFormatter = new JBossXmlSubPoolStatisticFormatter();
          String xml2 = (String)xmlFormatter.formatSubPoolStatistics(rawStatistics);
          
          Document doc2 = builder.parse(new InputSource(new StringReader(xml2)));
    
    
          StringWriter xmlout2 = new StringWriter();
          StreamResult result2 = new StreamResult(xmlout2);
    
          transformer.transform(new DOMSource(doc2.getFirstChild()), result2);
          
          //only compare xml content, ignore standalone="no"
          assertEquals(xmlout.toString(), xmlout2.toString());
    
          conn.close();
      }finally
      {
          setStatisticsFormatter(originalFormatter);
      }
      
   }
   
   public void testInvalidFormatter() throws Exception{

      ObjectName name = new ObjectName("jboss.jca:service=ManagedConnectionPool,name=StatsDS");      
      
      InitialContext initCtx = super.getInitialContext();
      DataSource ds = (DataSource)initCtx.lookup("StatsDS");
      Connection conn = ds.getConnection("sa", "");
      final String originalFormatter = getStatisticsFormatter();
      try{
          setStatisticsFormatter("Invalid");
          
          Object formattedStats = (String)getServer().invoke(POOL_NAME, FORMATTED_STATS_METHOD, new Object[0], new String[0]);
          
          assertTrue(formattedStats instanceof String);
          
          JBossDefaultSubPoolStatisticFormatter defaultFormatter = new JBossDefaultSubPoolStatisticFormatter();
          
          Object rawStatistics = listRawStatistics();
          ManagedConnectionPoolStatistics stats = (ManagedConnectionPoolStatistics)rawStatistics;
          
          String rawFormat = (String)defaultFormatter.formatSubPoolStatistics(stats);
          
          assertEquals(formattedStats, rawFormat);
      }finally{
          setStatisticsFormatter(originalFormatter);
      }
      
   }
  
   
   private Object listRawStatistics() throws Exception{
      
      return getServer().invoke(POOL_NAME, RAW_STATS_METHOD, new Object[0], new String[0]);
            
   }
   
   private void setStatisticsFormatter(String formatter) throws Exception{
      
      getServer().setAttribute(POOL_NAME, new Attribute(ATTRIBUTE_NAME, formatter));
      
   }

   private String getStatisticsFormatter() throws Exception{
   
      return (String)getServer().getAttribute(POOL_NAME, ATTRIBUTE_NAME);
      
   }

   public static Test suite() throws Exception
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL resURL = loader.getResource("jca/stats/default-stats-ds.xml");
      return getDeploySetup(StatisticsFormatterUnitTestCase.class, resURL.toString());
   }
}
