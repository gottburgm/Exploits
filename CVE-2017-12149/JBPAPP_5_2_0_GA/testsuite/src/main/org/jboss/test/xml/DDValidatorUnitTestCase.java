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
package org.jboss.test.xml;

import java.io.File;
import java.io.InputStream;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.test.JBossTestCase;


/**
 * Validates all the descriptors in the testsuite resources.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 105945 $</tt>
 */
public class DDValidatorUnitTestCase
    extends JBossTestCase
{
   private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();
   static
   {
      FACTORY.setValidating(true);
      FACTORY.setNamespaceAware(true);
   }

   private static final File RESOURCES = new File("resources");

   private static final Set<String> IGNORE = new HashSet<String>();
   static
   {
      // it's invalid on purpose
      ignore("jmx/undeploy/bad-inf/ejb-jar.xml");
      // contains property variable
      ignore("messagedriven/jmscontainerinvoker/META-INF/ejb-jar.xml");
      ignore("messagedriven/jmscontainerinvoker-hornetq/META-INF/ejb-jar.xml");

      // these don't include any module
      ignore("classloader/concurrentloader/application.xml");
      ignore("classloader/resource/application.xml");
      ignore("deployers/jbas2904/ear/META-INF/application.xml");
      ignore("jmx/loading/ext/application.xml");
      ignore("kernel/deployment/dependspojoear/META-INF/application.xml");
      ignore("util/scheduler/application.xml");
      
      // create-destination: '${test.messagedriven.createDestination}' is not a valid value for 'boolean'.
      ignore("messagedriven/jar/META-INF/jboss.xml");
      ignore("messagedriven/jar-hornetq/META-INF/jboss.xml");
   }

   private static void ignore(String path)
   {
      if('/' != File.separatorChar)
         path = path.replace('/', File.separatorChar);
      path = RESOURCES.getAbsolutePath() + File.separatorChar + path;
      IGNORE.add(path);
   }

   private int total;
   private int invalid;

   public DDValidatorUnitTestCase(String localName)
   {
      super(localName);
   }


   protected void setUp() throws Exception
   {
      super.setUp();
      total = 0;
      invalid = 0;
   }

   public void testEjbJar() throws Exception
   {
      validate("ejb-jar.xml");
   }

   public void testJBossXml() throws Exception
   {
      validate("jboss.xml");
   }

   public void testJBossCmpJdbc() throws Exception
   {
      validate("jbosscmp-jdbc.xml");
   }

   public void testWebXml() throws Exception
   {
      validate("web.xml");
   }

   public void testJBossWeb() throws Exception
   {
      validate("jboss-web.xml");
   }

   public void testApplicationXml() throws Exception
   {
      validate("application.xml");
   }

   public void testJBossApp() throws Exception
   {
      validate("jboss-app.xml");
   }

   // private

   private void validate(String ddName)
   {
      List<String> invalidList = new ArrayList<String>();
      scan(RESOURCES, Collections.singleton(ddName), invalidList, false);
      assertEquals(invalid + " from " + total + " are invalid: " + invalidList, 0, invalid);
   }

   /**
    * @param f  the directory in which to search for files
    * @param names  the files to validate
    * @param invalidList  a list of error messages
    * @param failIfInvalid  whether to fail immediately after the first invalid file
    */
   private void scan(java.io.File f, final Set<String> names, final List<String> invalidList, final boolean failIfInvalid)
   {
      f.listFiles(new FileFilter()
      {
         public boolean accept(File pathname)
         {
            if (pathname.isDirectory())
            {
               scan(pathname, names, invalidList, failIfInvalid);
               return true;
            }

            if (!IGNORE.contains(pathname.getAbsolutePath()) && names.contains(pathname.getName()))
            {
               ++total;
               if (!validate(pathname, invalidList, failIfInvalid))
               {
                  ++invalid;
               }
               return false;
            }

            return false;
         }
      });
   }

   private boolean validate(File file, List<String> invalidList, boolean failIfInvalid)
   {
      InputStream is;
      try
      {
         is = file.toURL().openStream();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to open file: " + file.getAbsolutePath(), e);
      }

      boolean valid;
      try
      {
         parse(is, new JBossEntityResolver());
         valid = true;
      }
      catch (JBossXBRuntimeException e)
      {
         valid = false;
         if (e.getCause() instanceof SAXException)
         {
            SAXException sax = (SAXException) e.getCause();

            StringBuffer msg = new StringBuffer();
            msg.append("Failed to parse: ").append(file.getAbsolutePath()).append(": ").append(sax.getMessage());

            if (sax instanceof SAXParseException)
            {
               SAXParseException parseException = (SAXParseException) sax;
               msg.append(" [").append(parseException.getLineNumber()).append(",").append(
                     parseException.getColumnNumber()).append("]");
            }

            if (failIfInvalid)
            {
               fail(msg.toString());
            }
            else
            {
               getLog().debug(msg.toString());
            }

            invalidList.add(msg.toString());
         }
         else
         {
            throw e;
         }
      }

      return valid;
   }

   private static void parse(InputStream xmlIs, final EntityResolver resolver)
   {
      SAXParser parser;
      try
      {
         parser = FACTORY.newSAXParser();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to instantiate a SAX parser: " + e.getMessage());
      }

      try
      {
         parser.getXMLReader().setFeature("http://apache.org/xml/features/validation/schema", true);
      }
      catch (SAXException e)
      {
         throw new IllegalStateException("Schema validation feature is not supported by the parser: " + e.getMessage());
      }

      try
      {
         parser.parse(xmlIs, new DefaultHandler()
         {
            public void warning(SAXParseException e)
            {
            }

            public void error(SAXParseException e)
            {
               throw new JBossXBRuntimeException("Error", e);
            }

            public void fatalError(SAXParseException e)
            {
               throw new JBossXBRuntimeException("Fatal error", e);
            }

            public InputSource resolveEntity(String publicId, String systemId)
            {
               InputSource is = null;
               if (resolver != null)
               {
                  try
                  {
                     is = resolver.resolveEntity(publicId, systemId);
                  }
                  catch (Exception e)
                  {
                     throw new IllegalStateException("Failed to resolveEntity " + systemId + ": " + systemId);
                  }
               }

               if(is == null)
               {
                  fail("Failed to resolve entity: publicId=" + publicId + " systemId=" + systemId);
               }

               return is;
            }
         });
      }
      catch(JBossXBRuntimeException e)
      {
         throw e;
      }
      catch (SAXException e)
      {
         throw new JBossXBRuntimeException("Parsing failed.", e);
      }
      catch (IOException e)
      {
         throw new JBossXBRuntimeException("Parsing failed.", e);
      }
   }
}
