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
package org.jboss.test.util.test;

import java.beans.PropertyEditor;
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.common.beans.property.DateEditor;
import org.jboss.common.beans.property.DocumentEditor;
import org.jboss.common.beans.property.ElementEditor;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit tests for the custom JBoss property editors
 *
 * @see PropertyEditorFinder
 * 
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 113110 $
 */
public class PropertyEditorsUnitTestCase extends JBossTestCase
{
   static class StringArrayComparator implements Comparator
   {
      public int compare(Object o1, Object o2)
      {
         String[] a1 = (String[]) o1;
         String[] a2 = (String[]) o2;
         int compare = a1.length - a2.length;
         for(int n = 0; n < a1.length; n ++)
            compare += a1[n].compareTo(a2[n]);
         return compare;
      }
   }
   static class ClassArrayComparator implements Comparator
   {
      public int compare(Object o1, Object o2)
      {
         Class[] a1 = (Class[]) o1;
         Class[] a2 = (Class[]) o2;
         int compare = a1.length - a2.length;
         for(int n = 0; n < a1.length; n ++)
         {
            int hash1 = a1[n].hashCode();
            int hash2 = a2[n].hashCode();
            compare += hash1 - hash2;
         }
         return compare;
      }
   }
   static class IntArrayComparator implements Comparator
   {
      public int compare(Object o1, Object o2)
      {
         int[] a1 = (int[]) o1;
         int[] a2 = (int[]) o2;
         int compare = a1.length - a2.length;
         for(int n = 0; n < a1.length; n ++)
            compare += a1[n] - a2[n];
         return compare;
      }
   }

   public static Test suite() throws Exception
   {
      // JBAS-3617 - the execution order of tests in this test case is important
      // so it must be defined explicitly when running under some JVMs
      TestSuite suite = new TestSuite();
      suite.addTest(new PropertyEditorsUnitTestCase("testEditorSearchPath"));
      suite.addTest(new PropertyEditorsUnitTestCase("testDateEditor"));
      suite.addTest(new PropertyEditorsUnitTestCase("testDocumentElementEditors"));

      addJavaLangEditors(suite);
      addJBossEditors(suite);

      return suite;
   }
   
   public PropertyEditorsUnitTestCase(String name)
   {
      super(name);
   }

   private static void addTests(final TestSuite suite, final Class[] types, final String[][] inputData, final Object[][] expectedData, final String[][] expectedStringData, final Comparator[] comparators)
   {
      for (int t = 0; t < types.length; t++)
      {
         suite.addTest(new PropertyEditorTest(types[t], inputData[t], expectedData[t], expectedStringData[t], comparators[t]));
      }
   }

   public void testEditorSearchPath()
      throws Exception
   {
      getLog().debug("+++ testEditorSearchPath");
      String[] searchPath = PropertyEditorFinder.getInstance().getEditorSearchPackages();
      boolean foundJBossPath = false;
      for(int p = 0; p < searchPath.length; p ++)
      {
         String path = searchPath[p];
         getLog().debug("path["+p+"]="+path);
         foundJBossPath |= path.equals("org.jboss.common.beans.property");
      }
      assertTrue("Found 'org.jboss.common.beans.property' in search path", foundJBossPath);
   }

   /** The mechanism for mapping java.lang.* variants of the primative types
    misses editors for java.lang.Boolean and java.lang.Integer. Here we test
    the java.lang.* variants we expect editors for.
    **/
   private static void addJavaLangEditors(final TestSuite suite)
      throws Exception
   {
      // The supported java.lang.* types
      Class[] types = {
         Boolean.class,
         Byte.class,
         Short.class,
         Integer.class,
         Long.class,
         Float.class,
         Double.class,
         Byte.class,
         Character.class,
      };
      // The input string data for each type
      String[][] inputData = {
         {"true", "false", "TRUE", "FALSE", "tRuE", "FaLsE", null},
         {"1", "-1", "0", "0x1A"},
         {"1", "-1", "0", "0xA0"},
         {"1", "-1", "0", "0xA0"},
         {"1", "-1", "0", "1000"},
         {"1", "-1", "0", "1000.1"},
         {"1", "-1", "0", "1000.1"},
         {"0x1", "-#1", "0"},
         {"A", "a", "Z", "z"},
      };
      // The expected java.lang.* instance for each inputData value
      Object[][] expectedData = {
         {Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, null},
         {Byte.valueOf("1"), Byte.valueOf("-1"), Byte.valueOf("0"), Byte.decode("0x1A")},
         {Short.valueOf("1"), Short.valueOf("-1"), Short.valueOf("0"), Short.decode("0xA0")},
         {Integer.valueOf("1"), Integer.valueOf("-1"), Integer.valueOf("0"), Integer.decode("0xA0")},
         {Long.valueOf("1"), Long.valueOf("-1"), Long.valueOf("0"), Long.valueOf("1000")},
         {Float.valueOf("1"), Float.valueOf("-1"), Float.valueOf("0"), Float.valueOf("1000.1")},
         {Double.valueOf("1"), Double.valueOf("-1"), Double.valueOf("0"), Double.valueOf("1000.1")},
         {Byte.valueOf("1"), Byte.valueOf("-1"), Byte.valueOf("0")},
         {new Character('A'), new Character('a'), new Character('Z'), new Character('z')},
      };
      // The expected string output from getAsText()
      String[][] expectedStringData = {
         {"true", "false", "true", "false", "true", "false", null},
         {"1", "-1", "0", "26"},
         {"1", "-1", "0", "160"},
         {"1", "-1", "0", "160"},
         {"1", "-1", "0", "1000"},
         {"1.0", "-1.0", "0.0", "1000.1"},
         {"1.0", "-1.0", "0.0", "1000.1"},
         {"1", "-1", "0"},
         {"A", "a", "Z", "z"},
      };
      Comparator[] comparators = new Comparator[types.length];

      addTests(suite, types, inputData, expectedData, expectedStringData, comparators);
   }

   /** Test custom JBoss property editors.
    **/
   private static void addJBossEditors(final TestSuite suite)
      throws Exception
   {
      Class[] types = {
         javax.management.ObjectName.class,
//         java.util.Properties.class,
         java.io.File.class,
         java.net.URL.class,
         java.lang.String.class,         
         java.lang.Class.class,
         InetAddress.class,
         String[].class,
         Class[].class,
         int[].class,
         Date.class
      };
      // The input string data for each type
      String[][] inputData = {
         // javax.management.ObjectName.class
         {"jboss.test:test=1"},
//         // java.util.Properties.class
//         {"prop1=value1\nprop2=value2\nprop3=value3\nprop32=${prop3}\nprop4=${user.home}\nprop5=${some.win32.path}"},
         // java.io.File.class
         {"/tmp/test1", "/tmp/subdir/../test2"},
         // java.net.URL.class
         {"http://www.jboss.org"},
         // java.lang.String.class
         {"JBoss, Home of Professional Open Source"},
         // java.lang.Class.class
         {"java.util.Arrays"},
         // InetAddress.class, localhost must be defined for this to work
         {"127.0.0.1", "localhost"},
         // String[].class
         {"1,2,3", "a,b,c", "", "#,%,\\,,.,_$,\\,v"},
         // Class[].class
         {"java.lang.Integer,java.lang.Float"},
         // int[].class
         {"0,#123,-123"},
         // Date.class
         {"Jan 4, 2005", "Tue Jan  4 23:38:21 PST 2005", "Tue, 04 Jan 2005 23:38:48 -0800"}
      };
      final Calendar calendar = Calendar.getInstance();
      // The expected instance for each inputData value
      calendar.set(2005, 0, 4, 0, 0, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      Date date1 = calendar.getTime();
      calendar.setTimeZone(TimeZone.getTimeZone("PST"));      
      calendar.set(2005, 0, 4, 23, 38, 21);
      Date date2 = calendar.getTime();
      calendar.set(2005, 0, 4, 23, 38, 48);
      Date date3 = calendar.getTime();
//      Properties props = new Properties();
//      props.setProperty("prop1", "value1");
//      props.setProperty("prop2", "value2");
//      props.setProperty("prop3", "value3");
//      props.setProperty("prop32", "value3");
//      props.setProperty("prop4", System.getProperty("user.home"));
//      System.setProperty("some.win32.path", "C:\\disk1\\root\\");
//      props.setProperty("prop5", "C:\\disk1\\root\\");
      Object[][] expectedData = {
         {new ObjectName("jboss.test:test=1")},
//         {props},
         {new File("/tmp/test1").getCanonicalFile(), new File("/tmp/test2").getCanonicalFile()},
         {new URL("http://www.jboss.org")},
         {new String("JBoss, Home of Professional Open Source")},
         {java.util.Arrays.class},
         {InetAddress.getByName("127.0.0.1"), InetAddress.getByName("localhost")},
         {new String[]{"1", "2", "3"}, new String[] {"a", "b", "c"},
            new String[]{}, new String[]{"#","%",",",".","_$", ",v"}},
         {new Class[]{Integer.class, Float.class}},
         {new int[]{0, 0x123, -123}},
         {date1, date2, date3}
      };
      // The expected string output from getAsText()
      String[][] expectedStringData = {
         // javax.management.ObjectName.class
         {"jboss.test:test=1"},
         // java.util.Properties.class
//         {"prop1=value1\nprop2=value2\nprop3=value3\nprop32=${prop3}\nprop4=${user.home}\nprop5=${some.win32.path}"},
         // java.io.File.class
         {"/tmp/test1", "/tmp/test2"},
         // java.net.URL.class
         {"http://www.jboss.org"},
         // java.lang.String.class
         {"JBoss, Home of Professional Open Source"},
         // java.lang.Class.class
         {"java.util.Arrays"},
         // InetAddress.class, localhost must be defined for this to work
         {"127.0.0.1", "localhost"},
         // String[].class
         {"1,2,3", "a,b,c", "", "#,%,\\,,.,_$,\\,v"},
         // Class[].class
         {"java.lang.Integer,java.lang.Float"},
         // int[].class
         {"0,291,-123"},
         // Date.class
         {"Jan 4, 2005", "Tue Jan  4 23:38:21 PST 2005", "Tue, 04 Jan 2005 23:38:48 -0800"}            
      };
      // The Comparator for non-trival types
      Comparator[] comparators = {
         null, // ObjectName
//         null, // Properties
         null, // File
         null, // URL
         null, // String
         null, // Class
         null, // InetAddress
         new StringArrayComparator(), // String[]
         new ClassArrayComparator(), // Class[]
         new IntArrayComparator(), // int[]
         null // Date
      };

      addTests(suite, types, inputData, expectedData, expectedStringData, comparators);
   }
   
   public void testDateEditor() throws Exception
   {
      getLog().debug("+++ testDateEditor");
      
      Locale locale = Locale.getDefault();
      
      try
      {
         // Use the default locale
         getLog().debug("Current Locale: " + Locale.getDefault());
      
         // An important date
         String text = "Fri, 25 Jun 1971 00:30:00 +0200";
         DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");      
         Date date = format.parse(text);      
         
         PropertyEditor editor = new DateEditor();
         editor.setAsText(text);
         getLog().debug("setAsText('" + text + "') --> getValue() = '" + editor.getValue() + "'");
         assertTrue("Compare date1: " + date + ", date2: " + editor.getValue(),
               date.compareTo((Date)editor.getValue()) == 0);
         
         editor.setValue(date);
         getLog().debug("setValue('" + date + "') --> getAsText() - '" + editor.getAsText() + "'");
         Date date2 = format.parse(editor.getAsText());
         assertTrue("Compare date1: " + date + ", date2: " + date2,
               date.compareTo(date2) == 0);
         
         // Try in French
         Locale.setDefault(Locale.FRENCH);
         getLog().debug("Current Locale: " + Locale.getDefault());
         DateEditor.initialize();
         
         // An important date
         text = "ven., 25 juin 1971 00:30:00 +0200";
         format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
         date = format.parse(text);      
         
         editor = new DateEditor();
         editor.setAsText(text);
         getLog().debug("setAsText('" + text + "') --> getValue() = '" + editor.getValue() + "'");
         assertTrue("Compare date1: " + date + ", date2: " + editor.getValue(),
               date.compareTo((Date)editor.getValue()) == 0);
         
         editor.setValue(date);
         getLog().debug("setValue('" + date + "') --> getAsText() = '" + editor.getAsText() + "'");
         date2 = format.parse(editor.getAsText());
         assertTrue("Compare date1: " + date + ", date2: " + date2,
               date.compareTo(date2) == 0);        
      }
      finally
      {
         // reset locale
         Locale.setDefault(locale);
         DateEditor.initialize();
      }
   }
   
   /** 
    * Tests the DOM Document and Element editors.
    */
   public void testDocumentElementEditors()
   {
      getLog().debug("+++ testDocumentElementEditors");
      DocumentEditor de = new DocumentEditor();
      // Comments can appear outside of a document
      String s = "<!-- header comment --><doc name='whatever'/><!-- footer comment -->";
      getLog().debug("setAsText '" + s + "'");
      de.setAsText(s);
      getLog().debug("Parsed XML document:");
      log((Node)de.getValue(), "  ");
      getLog().debug("getAsText '" + de.getAsText() + "'");
      assertTrue("Document :\n" + de.getAsText(), de.getAsText().trim().endsWith(s));
      assertTrue(de.getValue() instanceof org.w3c.dom.Document);
      // Test whitespace preservation
      s = "<element>\n\n<e2/> testing\n\n</element>";
      de.setAsText(s);
      assertTrue("Document :\n" + de.getAsText() + "\nvs\n" + s, de.getAsText().trim().endsWith(s));

      ElementEditor ee = new ElementEditor();
      s = "<element>text</element>";
      ee.setAsText(s);
      assertEquals(s, ee.getAsText());
      assertTrue(ee.getValue() instanceof org.w3c.dom.Element);
   }
   
   private void doTests(Class[] types, String[][] inputData, Object[][] expectedData,
         String[][] expectedStringData, Comparator[] comparators)
   {
      for(int t = 0; t < types.length; t ++)
      {
         Class type = types[t];
         getLog().debug("Checking property editor for: "+type);
         PropertyEditor editor = PropertyEditorFinder.getInstance().find(type);
         assertTrue("Found property editor for: "+type, editor != null);
         getLog().debug("Found property editor for: "+type+", editor="+editor.getClass().getName());
         assertTrue("inputData.length == expectedData.length", inputData[t].length == expectedData[t].length);
         for(int i = 0; i < inputData[t].length; i ++)
         {
            String input = inputData[t][i];
            editor.setAsText(input);
            Object expected = expectedData[t][i];
            Object output = editor.getValue();
            Comparator c = comparators[t];
            boolean equals = false;
            if (c == null)
            {
               equals = output != null ? output.equals(expected) : expected == null;
            }
            else
            {
               equals = c.compare(output, expected) == 0;
            }
            assertTrue("Transform of "+input+" equals "+expected+", output="+output, equals);
               
            String expectedStringOutput = expectedStringData[t][i];
            String stringOutput = editor.getAsText();
            getLog().debug("editor " + editor.getClass());
            getLog().debug("setAsText '" + logString(input) + "'");
            getLog().debug("getAsText '" + logString(stringOutput) + "'");

            boolean stringOutputEquals = stringOutput != null ?
                  stringOutput.equals(expectedStringOutput) : expectedStringOutput == null;
            assertTrue("PropertyEditor: " + editor.getClass().getName() + ", getAsText() '" + logString(stringOutput) + "' == expectedStringOutput '" +
                  logString(expectedStringOutput) + "'", stringOutputEquals);           
         }
      }
   }
   
   /**
    * Log a Node hierarchy
    */
   private void log(Node node, String indent)
   {
      String name = node.getNodeName();
      String value = node.getNodeValue();
      getLog().debug(indent + "Name=" + name + ", Value=" + value);
      NodeList list = node.getChildNodes();
      for (int i = 0; i < list.getLength(); i++)
         log(list.item(i), indent + indent);
   }
   
   private static String logString(String s)
   {
      return s != null ? s : "<null>";
   }

   public static class PropertyEditorTest extends TestCase
   {
      private static final Logger log = Logger.getLogger(PropertyEditorTest.class);

      private final Class<?> type;
      private final String[] inputData;
      private final Object[] expectedData;
      private final String[] expectedStringData;
      private final Comparator<Object> c;

      private PropertyEditorTest(final Class<?> type, final String[] inputData, final Object[] expectedData, final String[] expectedStringData, final Comparator<Object> c)
      {
         super("testPropertyEditor " + type);
         this.type = type;
         this.inputData = inputData;
         this.expectedData = expectedData;
         this.expectedStringData = expectedStringData;
         this.c = c;
      }

      @Override
      protected void runTest()
      {
         log.debug("Checking property editor for: "+type);
         PropertyEditor editor = PropertyEditorFinder.getInstance().find(type);
         assertTrue("Found property editor for: "+type, editor != null);
         log.debug("Found property editor for: "+type+", editor="+editor.getClass().getName());
         assertTrue("inputData.length == expectedData.length", inputData.length == expectedData.length);
         for(int i = 0; i < inputData.length; i ++)
         {
            String input = inputData[i];
            editor.setAsText(input);
            Object expected = expectedData[i];
            Object output = editor.getValue();
            boolean equals = false;
            if (c == null)
            {
               equals = output != null ? output.equals(expected) : expected == null;
            }
            else
            {
               equals = c.compare(output, expected) == 0;
            }
            assertTrue("Transform of "+input+" equals "+expected+", output="+output, equals);

            String expectedStringOutput = expectedStringData[i];
            String stringOutput = editor.getAsText();
            log.debug("editor " + editor.getClass());
            log.debug("setAsText '" + logString(input) + "'");
            log.debug("getAsText '" + logString(stringOutput) + "'");

            boolean stringOutputEquals = stringOutput != null ?
                    stringOutput.equals(expectedStringOutput) : expectedStringOutput == null;
            assertTrue("PropertyEditor: " + editor.getClass().getName() + ", getAsText() '" + logString(stringOutput) + "' == expectedStringOutput '" +
                    logString(expectedStringOutput) + "'", stringOutputEquals);
         }
      }
   }
}
