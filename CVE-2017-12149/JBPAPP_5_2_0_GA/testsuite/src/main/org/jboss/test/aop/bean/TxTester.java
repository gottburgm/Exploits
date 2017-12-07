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
package org.jboss.test.aop.bean;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;
/**
 *
 * @see Monitorable
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 */
public class TxTester
   extends ServiceMBeanSupport
   implements TxTesterMBean, MBeanRegistration
{
   // Constants ----------------------------------------------------
   // Attributes ---------------------------------------------------
   static Logger log = Logger.getLogger(TxTester.class);
   MBeanServer m_mbeanServer;

   // Static -------------------------------------------------------
   
   // Constructors -------------------------------------------------
   public TxTester()
   {}
   
   // Public -------------------------------------------------------
   
   // MBeanRegistration implementation -----------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   throws Exception
   {
      m_mbeanServer = server;
      return name;
   }
   
   public void postRegister(Boolean registrationDone)
   {}
   public void preDeregister() throws Exception
   {}
   public void postDeregister()
   {}

   protected void startService()
      throws Exception
   {
   }

   protected void stopService() {
   }

   public void testXml()
   {
      try{
         log.info("TESTING Tx XML");
         TxPOJO pojo = new TxPOJO();
         log.info("TESTING Never");
         pojo.callNever();
         log.info("TESTING NotSupprted");
         pojo.callNotSupported();
         log.info("TESTING Supports");
         pojo.callSupportsWithTx();
         pojo.callSupportsWithoutTx();

         log.info("TESTING Required");
         pojo.required();

         log.info("TESTING RequiresNew");
         pojo.callRequiresNew();

         log.info("TESTING Mandatory");
         pojo.callMandatoryNoTx();
         pojo.callMandatoryWithTx();
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex.getMessage());
      }
   }

   public void testAnnotated()
   {
      try{
         log.info("TESTING Tx Annotated");
         AnnotatedTxPOJO pojo = new AnnotatedTxPOJO();
         log.info("TESTING Never");
         pojo.callNever();
         log.info("TESTING NotSupprted");
         pojo.callNotSupported();
         log.info("TESTING Supports");
         pojo.callSupportsWithTx();
         pojo.callSupportsWithoutTx();

         log.info("TESTING Required");
         pojo.required();

         log.info("TESTING RequiresNew");
         pojo.callRequiresNew();

         log.info("TESTING Mandatory");
         pojo.callMandatoryNoTx();
         pojo.callMandatoryWithTx();
      }
      catch (Throwable ex)
      {
         log.error("failed", ex);
         throw new RuntimeException(ex);
      }
   }

}

