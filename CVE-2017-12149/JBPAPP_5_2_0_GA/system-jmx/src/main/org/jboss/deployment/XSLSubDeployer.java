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
package org.jboss.deployment;

import java.io.IOException;
import java.io.InputStream;

import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.jboss.bootstrap.spi.util.ServerConfigUtil;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.util.xml.DOMWriter;
import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.util.xml.JBossErrorHandler;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * XSLSubDeployer
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version <tt>$Revision: 81033 $</tt>
 */
public class XSLSubDeployer extends SubDeployerSupport implements XSLSubDeployerMBean
{
   protected String xslUrl;

   protected String packageSuffix;

   protected String ddSuffix;

   protected DocumentBuilderFactory dbf;

   private Templates templates;

   protected ObjectName delegateName = SARDeployerMBean.OBJECT_NAME;

   protected SubDeployer delegate;

   /** A flag indicating if deployment descriptors should be validated */
   private boolean validateDTDs;

   public XSLSubDeployer()
   {

   }

   public void setXslUrl(final String xslUrl)
   {
      this.xslUrl = xslUrl;
   }

   public String getXslUrl()
   {
      return xslUrl;
   }

   public void setPackageSuffix(final String packageSuffix)
   {
      this.packageSuffix = packageSuffix;
   }

   public String getPackageSuffix()
   {
      return packageSuffix;
   }

   public void setDdSuffix(final String ddSuffix)
   {
      this.ddSuffix = ddSuffix;
   }

   public String getDdSuffix()
   {
      return ddSuffix;
   }

   public void setDelegateName(final ObjectName delegateName)
   {
      this.delegateName = delegateName;
   }

   public ObjectName getDelegateName()
   {
      return delegateName;
   }
   public boolean getValidateDTDs()
   {
      return validateDTDs;
   }

   public void setValidateDTDs(boolean validate)
   {
      this.validateDTDs = validate;
   }

   protected void createService() throws Exception
   {
      super.createService();
      delegate = (SubDeployer) MBeanProxyExt.create(SubDeployer.class, delegateName, server);

      TransformerFactory tf = TransformerFactory.newInstance();
      dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      dbf.setValidating(validateDTDs);
      
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xslUrl);
      StreamSource ss = new StreamSource(is);
      templates = tf.newTemplates(ss);
      log.debug("Created templates: " + templates);
   }

   protected void destroyService() throws Exception
   {
      templates = null;
      super.destroyService();
   }

   public boolean accepts(DeploymentInfo di)
   {
      String urlStr = di.url.toString();
      return (packageSuffix != null && (urlStr.endsWith(packageSuffix) || urlStr.endsWith(packageSuffix + "/")))
          || (ddSuffix != null && urlStr.endsWith(ddSuffix));
   }

   public void init(DeploymentInfo di) throws DeploymentException
   {
      if (di.document == null)
         findDd(di);

      try
      {
         Transformer trans = templates.newTransformer();
         String urlStr = di.url.toString();
         String shortURL = ServerConfigUtil.shortUrlFromServerHome(urlStr);
         trans.setErrorListener(new JBossErrorHandler(shortURL, null));
         Source s = new DOMSource(di.document);
         DOMResult r = new DOMResult();
         setParameters(trans);
         
         trans.transform(s, r);
         
         di.document = (Document) r.getNode();
         if (log.isDebugEnabled())
         {
            log.debug("transformed into doc: " + di.document);
            String docStr = DOMWriter.printNode(di.document, true);
            int index = docStr.toLowerCase().indexOf("password"); 
            if (index != -1)
            {
               docStr = maskPasswords(docStr, index);
            }
            log.debug("transformed into doc: " + docStr);
         }
      }
      catch (TransformerException ce)
      {
         throw new DeploymentException("Problem with xsl transformation", ce);
      }
      delegate.init(di);
   }

   public void create(DeploymentInfo di) throws DeploymentException
   {
      delegate.create(di);
   }

   public void start(DeploymentInfo di) throws DeploymentException
   {
      delegate.start(di);
   }

   public void stop(DeploymentInfo di) throws DeploymentException
   {
      delegate.stop(di);
   }

   public void destroy(DeploymentInfo di) throws DeploymentException
   {
      delegate.destroy(di);
   }

   protected void setParameters(Transformer trans) throws TransformerException
   {
      //override to set document names etc.
   }

   protected void findDd(DeploymentInfo di) throws DeploymentException
   {
      try
      {
         DocumentBuilder db = dbf.newDocumentBuilder();
         String urlStr = di.url.toString();
         String shortURL = ServerConfigUtil.shortUrlFromServerHome(urlStr);
         JBossEntityResolver resolver = new JBossEntityResolver();
         db.setEntityResolver(resolver);
         db.setErrorHandler(new JBossErrorHandler(shortURL, resolver));

         if (ddSuffix != null && urlStr.endsWith(ddSuffix))
            di.document = db.parse(di.url.openStream());
      }
      catch (SAXException se)
      {
         throw new DeploymentException("Could not parse dd", se);
      }
      catch (IOException ioe)
      {
         throw new DeploymentException("Could not read dd", ioe);
      }
      catch (ParserConfigurationException pce)
      {
         throw new DeploymentException("Could not create document builder for dd", pce);
      }
   }
   
   /**
    * Masks passwords so they are not visible in the log.
    * 
    * @param original <code>String</code> plain-text passwords
    * @param index index where the password keyword was found
    * @return modified <code>String</code> with masked passwords
    */
   private String maskPasswords(String original, int index)
   {
      StringBuilder sb = new StringBuilder(original);
      String modified = null;
      int startPasswdStringIndex = sb.indexOf(">", index);
      if (startPasswdStringIndex != -1)
      {
         // checks if the keyword 'password' was not in a comment
         if (sb.charAt(startPasswdStringIndex - 1) != '-')
         {
            int endPasswdStringIndex = sb.indexOf("<", startPasswdStringIndex);
            if (endPasswdStringIndex != -1) // shouldn't happen, but check anyway
            {
               sb.replace(startPasswdStringIndex + 1, endPasswdStringIndex, "****");
            }
         }
         modified = sb.toString();
         // unlikely event of more than one password
         index = modified.toLowerCase().indexOf("password", startPasswdStringIndex);
         if (index != -1)
            return maskPasswords(modified, index);
         return modified;
      }
      return original;
   }
}
