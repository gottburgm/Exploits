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
package org.jboss.test.util.test;

import java.io.File;
import java.util.Properties;

import org.jboss.test.JBossTestCase;
import org.jboss.util.StringPropertyReplacer;

/** Unit tests for the StringPropertyReplacer utility class
 *
 * @see org.jboss.util.StringPropertyReplacer
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * 
 * @version $Revision: 81036 $
 */
public class StringPropertyReplacerUnitTestCase extends JBossTestCase
{
   public StringPropertyReplacerUnitTestCase(String name)
   {
      super(name);
   }

   /** Tests of the ${x} property replacement
    * 
    * @throws Exception
    */ 
   public void testPropReplacement()
      throws Exception
   {
      getLog().debug("+++ testPropReplacement");
      String xref = "${x}";
      String xval = StringPropertyReplacer.replaceProperties(xref);
      assertTrue("xval == xref", xval.equals(xref));
      
      System.setProperty("x", "testPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref);
      assertTrue("xval == 'xval'", xval.equals("testPropReplacement"));
   }

   /** Tests of the ${x} property replacement with a non-System Properties
    * 
    * @throws Exception
    */ 
   public void testNonSystemPropReplacement()
      throws Exception
   {
      getLog().debug("+++ testNonSystemPropReplacement");
      String xref = "${xx}";

      Properties props = new Properties();
      props.setProperty("xx", "testNonSystemPropReplacement");
      String xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xval'", xval.equals("testNonSystemPropReplacement"));
   }

   /** Test the specified default value is used
    *  when the system property could not be replaced,
    *  and ignored when the system property is replaced.
    */
   public void testDefaultValueSystemPropReplacement()
      throws Exception
   {
      getLog().debug("+++ testDefaultValueSystemPropReplacement");

      String xref = "${xxx:d}";
      String xdef = "d";
      String xval = StringPropertyReplacer.replaceProperties(xref);
      assertTrue("xval == xdef", xval.equals(xdef));
      
      System.setProperty("xxx", "testPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref);
      assertTrue("xval == 'xval'", xval.equals("testPropReplacement"));
   }
   

   /** Test the specified default value is used
    *  when the non-system property could not be replaced
    *  and ignored when the non-property is replaced.
    */
   public void testDefaultValueNonSystemPropReplacement()
      throws Exception
   {
      getLog().debug("+++ testDefaultValueNonSystemPropReplacement");

      Properties props = new Properties();
      
      String xref = "${xxx:d}";
      String xdef = "d";
      String xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == xdef", xval.equals(xdef));
      
      props.setProperty("xxx", "testNonSystemPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xval'", xval.equals("testNonSystemPropReplacement"));
   }
   
   /**
    * Test the scenario where a primary and a secondary
    * system property is specified.
    */
   public void testSecondarySystemPropReplacement()
      throws Exception
   {
      getLog().debug("+++ testSecondarySystemPropReplacement");
      
      String xref = "${x1,x2}";
      String xval = StringPropertyReplacer.replaceProperties(xref);
      assertTrue("xval == 'xref'", xval.equals(xref));
      
      System.setProperty("x2", "secondaryPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref);
      assertTrue("xval == 'xval'", xval.equals("secondaryPropReplacement"));
      
      System.setProperty("x1", "primaryPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref);
      assertTrue("xval == 'xval'", xval.equals("primaryPropReplacement"));
   }
   
   /**
    * Test the scenario where a primary and a secondary
    * non-system property, plus a default value are specified
    */
   public void testSecondaryNonSystemPropReplacementWithDefault()
      throws Exception
   {
      getLog().debug("+++ testSecondaryNonSystemPropReplacementWithDefault");
      
      Properties props = new Properties();
      
      String xref = "${x1,x2:d}";
      String xdef = "d";
      String xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xdef'", xval.equals(xdef));
      
      props.setProperty("x2", "secondaryPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xval'", xval.equals("secondaryPropReplacement"));
      
      props.setProperty("x1", "primaryPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xval'", xval.equals("primaryPropReplacement"));
   }

   /**
    * Test that we first check if the property is set before
    * trying to apply a default or a resolving secondary property
    */
   public void testPathologicalNonSystemPropReplacement()
      throws Exception
   {
      getLog().debug("+++ testPathologicalNonSystemPropReplacement");
      
      Properties props = new Properties();
      
      String xref = "${x1,x2:d}";
      props.setProperty("x1,x2:d", "pathologicalPropReplacement");
      String xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xval'", xval.equals("pathologicalPropReplacement"));
   }
   
   /**
    * Test that a composite property gets resolved
    * when the secondary property is missing.
    */
   public void testPathologicalMissingSecondaryProperty()
      throws Exception
   {
      getLog().debug("+++ testPathologicalMissingSecondaryProperty");
      
      Properties props = new Properties();
      
      String xref = "${x1,}";
      String xval = StringPropertyReplacer.replaceProperties(xref, props);      
      assertTrue("xval == 'xref'", xval.equals(xref));
      
      props.setProperty("x1", "primaryPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xval'", xval.equals("primaryPropReplacement"));      
   }
   
   /**
    * Test that a composite property with a default gets resolved
    * when the secondary property is missing.
    */
   public void testPathologicalMissingSecondaryPropertyWithDefault()
      throws Exception
   {
      getLog().debug("+++ testPathologicalMissingSecondaryPropertyWithDefault");
      
      Properties props = new Properties();
      
      String xref = "${x1,:d}";
      String xdef = "d";      
      String xval = StringPropertyReplacer.replaceProperties(xref, props);      
      assertTrue("xval == 'xdef'", xval.equals(xdef));
      
      props.setProperty("x1", "primaryPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xval'", xval.equals("primaryPropReplacement"));      
   }
   
   /**
    * Test that a composite property gets resolved
    * when the primary property is missing.
    */
   public void testPathologicalMissingPrimaryProperty()
      throws Exception
   {
      getLog().debug("+++ testPathologicalMissingPrimaryProperty");
      
      Properties props = new Properties();
      
      String xref = "${,x2}";
      String xval = StringPropertyReplacer.replaceProperties(xref, props);      
      assertTrue("xval == 'xref'", xval.equals(xref));
      
      props.setProperty("x2", "secondaryPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xval'", xval.equals("secondaryPropReplacement"));      
   }
   
   /**
    * Test that a composite property with a default gets resolved
    * when the primary property is missing.
    */
   public void testPathologicalMissingPrimaryPropertyWithDefault()
      throws Exception
   {
      getLog().debug("+++ testPathologicalMissingPrimaryPropertyWithDefault");
      
      Properties props = new Properties();
      
      String xref = "${,x2:d}";
      String xdef = "d";      
      String xval = StringPropertyReplacer.replaceProperties(xref, props);      
      assertTrue("xval == 'xdef'", xval.equals(xdef));
      
      props.setProperty("x2", "secondaryPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xval'", xval.equals("secondaryPropReplacement"));      
   }
   
   /**
    * Test that with an empty default value we get the
    * property evaluating to an empty string "", when
    * the property is undefined
    */
   public void testEmptyDefaultNonSystemPropReplacement()
      throws Exception
   {
      getLog().debug("+++ testEmptyDefaultNonSystemPropReplacement");
      
      Properties props = new Properties();
      
      String xref="${x1:}";
      String xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == ''", xval.equals(""));
      
      props.setProperty("x1", "primaryPropReplacement");
      xval = StringPropertyReplacer.replaceProperties(xref, props);
      assertTrue("xval == 'xval'", xval.equals("primaryPropReplacement"));
   }
   
   /** Test that ${/} and ${:} refs are replaced with  
    * 
    * @throws Exception
    */ 
   public void testFilePropReplacement()
      throws Exception
   {
      getLog().debug("+++ testFilePropReplacement");
      String pathSeparatorRef = "${:}";
      String pathSeparator = StringPropertyReplacer.replaceProperties(pathSeparatorRef);
      String separatorRef = "${/}";
      String separator = StringPropertyReplacer.replaceProperties(separatorRef);
      getLog().debug("File.pathSeparator='"+File.pathSeparator+"'");
      getLog().debug("File.separator='"+File.separator+"'");
      assertTrue("${:}("+pathSeparator+") == File.pathSeparator", pathSeparator.equals(File.pathSeparator));
      assertTrue("${/}("+separator+") == File.separator", separator.equals(File.separator));
   }

}
