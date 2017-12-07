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
package org.jboss.jmx.adaptor.snmp.agent;

import java.net.InetAddress;

import javax.management.MBeanServer;

import org.jboss.logging.Logger;
import org.opennms.protocols.snmp.SnmpAgentSession;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * Implement RequestHandler with simple tracing of incoming requests.
 * 
 * Derived classes are expected to implement actual behaviour.
 * 
 * @author <a href="mailto:krishnaraj@ieee.org">Krishnaraj S</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class RequestHandlerSupport implements RequestHandler
{
   // Protected Data ------------------------------------------------
   
   /** Logger object */
   protected Logger log;
   
   /** the MBeanServer */
   protected MBeanServer server;
   
   /** the file name to get mapping info from */
   protected String resourceName;
   
   /** the agent clock */
   protected Clock clock;
   
   // Constructors --------------------------------------------------
   
   /**
    * Default CTOR
    */
   public RequestHandlerSupport()
   {
      // empty
   }
   
   // RequestHandler Implementation ---------------------------------
   
   /**
    * Initialize
    */
   public void initialize(String resourceName, MBeanServer server, Logger log, Clock uptime)
      throws Exception
   {
      this.resourceName = resourceName;
      this.server = server;
      this.log = log;
      this.clock = uptime;
   }
   
   // SnmpAgentHandler Implementation -------------------------------
   
   /**
    * <P>This method is defined to handle SNMP Get requests
    * that are received by the session. The request has already
    * been validated by the system.  This routine will build a
    * response and pass it back to the caller.</P>
    *
    * @param pdu     The SNMP pdu
    * @param getNext The agent is requesting the lexically NEXT item after each
    *                    item in the pdu.
    *
    * @return SnmpPduRequest filled in with the proper response, or null if cannot process
    * NOTE: this might be changed to throw an exception.
    */   
   public SnmpPduRequest snmpReceivedGet(SnmpPduPacket pdu, boolean getNext)
   {
      SnmpPduRequest response = null;
      int pduLength = pdu.getLength();
      
      log.debug("requestId=" + pdu.getRequestId() + ", pduLength="  + pduLength);
   
      SnmpVarBind[] vblist  = new SnmpVarBind[pduLength];
      int errorStatus = SnmpPduPacket.ErrNoError;
      int errorIndex = 0;

      //Process for each varibind in the request
      for (int i = 0; i < pduLength ; i++ )
      {
         SnmpVarBind vb = pdu.getVarBindAt(i);
         SnmpObjectId oid = vb.getName();
         if (getNext) 
         {
            log.debug(
               "Should call getNextOid() to find out what is the next valid OID " +
               "instance in the supported MIB tree. Assign that OID to the VB List " +
               "and then proceed same as that of get request" );
         }
         vblist[i] = new SnmpVarBind(oid);
         
         log.debug("oid=" + oid.toString());
    
         log.debug("Should call the respective interface to retrieve current value for this OID" );

         SnmpSyntax result = null;
         
         if (result == null)
         {
            errorStatus = SnmpPduPacket.ErrNoSuchName;
            errorIndex = i + 1;
            //log.debug("Error Occured " + vb.getName().toString());
         }
         else
         {
            vblist[i].setValue(result);
            log.debug("Varbind[" + i + "] := " + vblist[i].getName().toString());
            log.debug(" --> " + vblist[i].getValue().toString());       
         }
      }
      response = new SnmpPduRequest(SnmpPduPacket.RESPONSE, vblist);
      response.setErrorStatus(errorStatus);
      response.setErrorIndex(errorIndex);
      return response;
   }

   /**
    * <P>This method is defined to handle SNMP Set requests
    * that are received by the session. The request has already
    * been validated by the system.  This routine will build a
    * response and pass it back to the caller.</P>
    *
    * @param pdu     The SNMP pdu
    *
    * @return SnmpPduRequest filled in with the proper response, or null if cannot process
    * NOTE: this might be changed to throw an exception.
    */
   public SnmpPduRequest snmpReceivedSet(SnmpPduPacket pdu)
   {
      SnmpPduRequest response = null;
      int errorStatus = SnmpPduPacket.ErrNoError;
      int errorIndex = 0;
      int k = pdu.getLength();
      SnmpVarBind[] vblist  = new SnmpVarBind[k];
     
      for (int i = 0; i < k ; i++ )
      {
         SnmpVarBind vb = pdu.getVarBindAt(i);
         vblist[i] = new SnmpVarBind(vb);
         SnmpObjectId oid = vb.getName();
         
         SnmpSyntax result = null;
         log.debug("Should call the respective interface to assign a value for this OID" );
          
         if (result != null)
         {
            errorStatus = SnmpPduPacket.ErrReadOnly;
            errorIndex = i + 1;
            log.debug("Error occured " + vb.getName().toString());
         }
         
         log.debug("Varbind[" + i + "] := " + vb.getName().toString());
         log.debug(" --> " + vb.getValue().toString());     
      }
     
      response = new SnmpPduRequest(SnmpPduPacket.RESPONSE, vblist);
      response.setErrorStatus(errorStatus);
      response.setErrorIndex(errorIndex);

      return response;
   }
 
   /**
    * <P>This method is defined to handle SNMP requests
    * that are received by the session. The parameters
    * allow the handler to determine the host, port, and
    * community string of the received PDU</P>
    *
    * @param session The SNMP session
    * @param manager The remote sender
    * @param port    The remote senders port
    * @param community  The community string
    * @param pdu     The SNMP pdu
    *
    */
   public void snmpReceivedPdu(SnmpAgentSession session, InetAddress manager, int port,
                               SnmpOctetString community, SnmpPduPacket pdu)
   {
      log.error("Message from manager " + manager.toString() + " on port " + port);
      int cmd = pdu.getCommand();
      log.error("Unsupported PDU command......... " + cmd);
   }
 
   /**
    * <P>This method is invoked if an error occurs in 
    * the session. The error code that represents
    * the failure will be passed in the second parameter,
    * 'error'. The error codes can be found in the class
    * SnmpAgentSession class.</P>
    *
    * <P>If a particular PDU is part of the error condition
    * it will be passed in the third parameter, 'pdu'. The
    * pdu will be of the type SnmpPduRequest or SnmpPduTrap
    * object. The handler should use the "instanceof" operator
    * to determine which type the object is. Also, the object
    * may be null if the error condition is not associated
    * with a particular PDU.</P>
    *
    * @param session The SNMP Session
    * @param error   The error condition value.
    * @param ref     The PDU reference, or potentially null.
    *                It may also be an exception.
    */
   public void SnmpAgentSessionError(SnmpAgentSession session, int error, Object ref)
   {
      log.error("An error occured in the trap session");
      log.error("Session error code = " + error);
      if(ref != null)
      {
         log.error("Session error reference: " + ref.toString());
      }
     
      if(error == SnmpAgentSession.ERROR_EXCEPTION)
      {
         synchronized(session)
         {
            session.notify(); // close the session
         }
      }
   }
}
