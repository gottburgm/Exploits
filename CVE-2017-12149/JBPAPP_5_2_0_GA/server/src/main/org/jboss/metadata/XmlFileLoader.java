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
package org.jboss.metadata;

import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.util.xml.JBossEntityResolver;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/** XmlFileLoader class is used to read ejb-jar.xml, standardjboss.xml, jboss.xml
 * files, process them using DTDs and create ApplicationMetaData object for
 * future use. It also provides the local entity resolver for the JBoss
 * specific DTDs.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:WolfgangWerner@gmx.net">Wolfgang Werner</a>
 * @author <a href="mailto:Darius.D@jbees.com">Darius Davidavicius</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>.
 * @version $Revision: 81030 $
 */
public class XmlFileLoader
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private static boolean defaultValidateDTDs = false;
   private static Logger log = Logger.getLogger(XmlFileLoader.class);
   private URLClassLoader classLoader;
   private ApplicationMetaData metaData;
   private boolean validateDTDs;
   
   // Static --------------------------------------------------------
   public static boolean getDefaultValidateDTDs()
   {
      return defaultValidateDTDs;
   }
   
   public static void setDefaultValidateDTDs(boolean validate)
   {
      defaultValidateDTDs = validate;
   }
   
   
   // Constructors --------------------------------------------------
   public XmlFileLoader()
   {
      this(defaultValidateDTDs);
   }
   
   public XmlFileLoader(boolean validateDTDs)
   {
      this.validateDTDs = validateDTDs;
   }
   
   // Public --------------------------------------------------------
   public ApplicationMetaData getMetaData()
   {
      return metaData;
   }
   
   /**
    * Set the class loader
    */
   public void setClassLoader(URLClassLoader cl)
   {
      classLoader = cl;
   }
   
   /**
    * Gets the class loader
    *
    * @return ClassLoader - the class loader
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }
   
   
   /** Get the flag indicating that ejb-jar.dtd, jboss.dtd &
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    */
   public boolean getValidateDTDs()
   {
      return validateDTDs;
   }
   
   /** Set the flag indicating that ejb-jar.dtd, jboss.dtd &
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    */
   public void setValidateDTDs(boolean validate)
   {
      this.validateDTDs = validate;
   }
   
   /**
    * Creates the ApplicationMetaData.
    * The configuration files are found in the classLoader when not explicitly given as
    * the alternativeDD.
    *
    * The default jboss.xml and jaws.xml files are always read first, then we override
    * the defaults if the user provides them
    *
    * @param alternativeDD a URL to the alternative DD given in application.xml
    */
   public ApplicationMetaData load(URL alternativeDD) throws Exception
   {
      URL ejbjarUrl = null;
      if (alternativeDD != null)
      {
         log.debug("Using alternativeDD: " + alternativeDD);
         ejbjarUrl = alternativeDD;
      }
      else
      {
         ejbjarUrl = getClassLoader().getResource("META-INF/ejb-jar.xml");
      }

      if (ejbjarUrl == null)
      {
         throw new DeploymentException("no ejb-jar.xml found");
      }

      // create the metadata
      JBossMetaData realMetaData = new JBossMetaData();
      metaData = new ApplicationMetaData(realMetaData);

      Document ejbjarDocument = getDocumentFromURL(ejbjarUrl);
      
      // the url may be used to report errors
      metaData.setUrl(ejbjarUrl);
      metaData.importEjbJarXml(ejbjarDocument.getDocumentElement());
      
      // Load jbossdefault.xml from the default classLoader
      // we always load defaults first
      // we use the context classloader, because this guy has to know where
      // this file is
      URL defaultJbossUrl = Thread.currentThread().getContextClassLoader().getResource("standardjboss.xml");
      if (defaultJbossUrl == null)
      {
         throw new DeploymentException("no standardjboss.xml found");
      }

      Document defaultJbossDocument = null;
      try
      {
         defaultJbossDocument = getDocumentFromURL(defaultJbossUrl);
         metaData.setUrl(defaultJbossUrl);
         metaData.importJbossXml(defaultJbossDocument.getDocumentElement());
      }
      catch (Exception ex)
      {
         log.error("failed to load standardjboss.xml.  There could be a syntax error.", ex);
         throw ex;
      }
      
      // Load jboss.xml
      // if this file is provided, then we override the defaults
      try
      {
         URL jbossUrl = getClassLoader().getResource("META-INF/jboss.xml");
         if (jbossUrl != null)
         {
            Document jbossDocument = getDocumentFromURL(jbossUrl);
            metaData.setUrl(jbossUrl);
            metaData.importJbossXml(jbossDocument.getDocumentElement());
         }
      }
      catch (Exception ex)
      {
         log.error("failed to load jboss.xml.  There could be a syntax error.", ex);
         throw ex;
      }
      
      return metaData;
   }

   /** Invokes getDocument(url, defaultValidateDTDs)
    *
    */
   public static Document getDocument(URL url) throws DeploymentException
   {
      return getDocument(url, defaultValidateDTDs);
   }
   
   /** Get the xml file from the URL and parse it into a Document object.
    * Calls new XmlFileLoader(validateDTDs).getDocumentFromURL(url);
    * @param url the URL from which the xml doc is to be obtained.
    * @return Document
    */
   public static Document getDocument(URL url, boolean validateDTDs) throws DeploymentException
   {
      XmlFileLoader loader = new XmlFileLoader(validateDTDs);
      return loader.getDocumentFromURL(url);
   }
   
   /** Get the xml file from the URL and parse it into a Document object.
    * Calls getDocument(new InputSource(url.openStream()), url.getPath())
    * with the InputSource.SystemId set to url.toExternalForm().
    *
    * @param url the URL from which the xml doc is to be obtained.
    * @return Document
    */
   public Document getDocumentFromURL(URL url) throws DeploymentException
   {
      InputStream is = null;
      try
      {
         is = url.openStream();
         return getDocument(is, url.toExternalForm());
      }
      catch (IOException e)
      {
         throw new DeploymentException("Failed to obtain xml doc from URL", e);
      }
   }

   /** Parses the xml document in is to create a DOM Document. DTD validation
    * is enabled if validateDTDs is true and we install an EntityResolver and
    * ErrorHandler to resolve J2EE DTDs and handle errors. We also create an
    * InputSource for the InputStream and set the SystemId URI to the inPath
    * value. This allows relative entity references to be resolved against the
    * inPath URI. The is argument will be closed.
    *
    * @param is the InputStream containing the xml descriptor to parse
    * @param inPath the path information for the xml doc. This is used as the
    * InputSource SystemId URI for resolving relative entity references.
    * @return Document
    */
   public Document getDocument(InputStream is, String inPath)
      throws DeploymentException
   {
      InputSource is2 = new InputSource(is);
      is2.setSystemId(inPath);
      Document doc = null;
      try
      {
         doc = getDocument(is2, inPath);
      }
      finally
      {
         // close the InputStream to get around "too many open files" errors
         // with large heaps
         try
         {
            if( is != null )
              is.close();
         }
         catch (Exception e)
         {
            // ignore
         }
      }
      return doc;
   }

   /** Parses the xml document in is to create a DOM Document. DTD validation
    * is enabled if validateDTDs is true and we install an EntityResolver and
    * ErrorHandler to resolve J2EE DTDs and handle errors. We also create an
    * InputSource for the InputStream and set the SystemId URI to the inPath
    * value. This allows relative entity references to be resolved against the
    * inPath URI.
    *
    * @param is the InputSource containing the xml descriptor to parse
    * @param inPath the path information for the xml doc. This is used for
    * only for error reporting.
    * @return Document
    */
   public Document getDocument(InputSource is, String inPath)
      throws DeploymentException
   {
      try
      {
         DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

         // Enable DTD validation based on our validateDTDs flag
         docBuilderFactory.setValidating(validateDTDs);
         // make the parser namespace-aware in case we deal 
         // with ejb2.1 descriptors, will not break dtd parsing in any way
         // in which case there would be just a default namespace
         docBuilderFactory.setNamespaceAware(true);
         // this will (along JAXP in conjunction with 
         // validation+namespace-awareness) enable xml schema checking. 
         // Will currently fail because some J2EE1.4/W3C schemas 
         // are still lacking.
         //docBuilderFactory.setAttribute
         //   ("http://java.sun.com/xml/jaxp/properties/schemaLanguage","http://www.w3.org/2001/XMLSchema");
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         JBossEntityResolver lr = new JBossEntityResolver();
         LocalErrorHandler eh = new LocalErrorHandler( inPath, lr );
         docBuilder.setEntityResolver(lr);
         docBuilder.setErrorHandler(eh );

         Document doc = docBuilder.parse(is);
         if(validateDTDs && eh.hadError())
         {
            throw new DeploymentException("Invalid XML: file=" + inPath, eh.getException());
         }
         return doc;
      }
      catch (DeploymentException e) 
      {
         throw e;
      }
      catch (SAXParseException e)
      {
         String msg = "Invalid XML: file=" + inPath+"@"+e.getColumnNumber()+":"+e.getLineNumber();
         throw new DeploymentException(msg, e);
      }
      catch (SAXException e)
      {
         throw new DeploymentException("Invalid XML: file=" + inPath, e);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Invalid XML: file=" + inPath, e);
      }
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

   /** Local error handler for entity resolver to DocumentBuilder parser.
    * Error is printed to output just if DTD was detected in the XML file.
    * If DTD was not found in XML file it is assumed that the EJB builder
    * doesn't want to use DTD validation. Validation may have been enabled via
    * validateDTDs flag so we look to the isEntityResolved() function in the LocalResolver
    * and reject errors if DTD not used.
    **/
   private static class LocalErrorHandler implements ErrorHandler
   {
      // The xml file being parsed
      private String theFileName;
      private JBossEntityResolver localResolver;
      private boolean error;
      private SAXParseException exception;
      
      public LocalErrorHandler( String inFileName, JBossEntityResolver localResolver )
      {
         this.theFileName = inFileName;
         this.localResolver = localResolver;
         this.error = false;
      }
      
      public void error(SAXParseException exception)
      {
         this.exception = exception;
         if ( localResolver.isEntityResolved() )
         {
            this.error = true;
            log.error("XmlFileLoader: File "
            + theFileName
            + " process error. Line: "
            + String.valueOf(exception.getLineNumber())
            + ". Error message: "
            + exception.getMessage()
            );
         }//end if
      }

      public void fatalError(SAXParseException exception)
      {
         this.exception = exception;
         if ( localResolver.isEntityResolved() )
         {
            this.error = true;
            log.error("XmlFileLoader: File "
            + theFileName
            + " process fatal error. Line: "
            + String.valueOf(exception.getLineNumber())
            + ". Error message: "
            + exception.getMessage()
            );
         }//end if
      }
      
      public void warning(SAXParseException exception)
      {
         this.exception = exception;
         if ( localResolver.isEntityResolved() )
         {
            this.error = true;
            log.error("XmlFileLoader: File "
            + theFileName
            + " process warning. Line: "
            + String.valueOf(exception.getLineNumber())
            + ". Error message: "
            + exception.getMessage()
            );
         }//end if
      }

      public SAXParseException getException()
      {
         return exception;
      }

      public boolean hadError()
      {
         return error;
      }
   }// end class LocalErrorHandler
}
