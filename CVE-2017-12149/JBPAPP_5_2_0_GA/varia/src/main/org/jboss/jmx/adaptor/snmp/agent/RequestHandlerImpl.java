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

import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.jmx.adaptor.snmp.config.attribute.AttributeMappings;
import org.jboss.jmx.adaptor.snmp.config.attribute.ManagedBean;
import org.jboss.jmx.adaptor.snmp.config.attribute.MappedAttribute;
import org.jboss.logging.Logger;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.opennms.protocols.snmp.*;

/**
 * Implement RequestHandler with mapping of snmp get/set requests
 * to JMX mbean attribute gets/sets
 *
 * @author <a href="mailto:hwr@pilhuhn.de>">Heiko W. Rupp</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 112202 $
 */
public class RequestHandlerImpl extends RequestHandlerSupport
   implements Reconfigurable
{
	// Protected Data ------------------------------------------------

	private static final String NO_ENTRY_FOUND_FOR_OID = "No entry found for oid ";
	private static final String SKIP_ENTRY = " - skipping entry";
	private static final String USE_TYPE_64 = "jboss.snmp.use64bit";


	/** Bindings from oid to mbean */
	protected SortedMap bindings = new TreeMap();

	private SortedSet oidKeys = null;

	/** Has this RequestHandler instance been initialized? */
	private boolean initialized = false;

	// Constructors --------------------------------------------------

	/**
	 * Default CTOR
	 */
	public RequestHandlerImpl() 
	{
		bindings = new TreeMap();
		oidKeys = new TreeSet();
	}

	// RequestHandler Implementation ---------------------------------

	/**
	 * Initialize
	 * 
	 * @param resourceName A file containing get/set mappings
	 * @param server Our MBean-Server
	 * @param log The logger we use
	 * @param uptime The uptime of the snmp-agent subsystem.
	 */
	public void initialize(String resourceName, MBeanServer server, Logger log, Clock uptime)
      throws Exception
   {
      log.debug("initialize() with res=" + resourceName);
	   super.initialize(resourceName, server, log, uptime);
		if (resourceName != null)
			initializeBindings();
		else
			log.warn("No RequestHandlerResName configured, disabling snmp-get");

		initialized = true;
	}

   // Reconfigurable Implementation ---------------------------------
   /**
    * Reconfigures the RequestHandler
    */
   public void reconfigure(String resName) throws Exception
   {
      if (resName == null || resName.equals(""))
         throw new IllegalArgumentException("Null or empty resName, cannot reconfigure");

      if (initialized == false)
         throw new IllegalStateException("Cannot reconfigure, not initialized yet");
      
      this.resourceName = resName;
   
      // Wipe out old entries
      bindings.clear();
      
      // Fetch them again
      initializeBindings();
   }
   
	// SnmpAgentHandler Implementation -------------------------------

	/**
	 * <P>
	 * This method is defined to handle SNMP Get requests that are received by
	 * the session. The request has already been validated by the system. This
	 * routine will build a response and pass it back to the caller.
	 * </P>
	 * 
	 * @param pdu
	 *            The SNMP pdu
	 * @param getNext
	 *            The agent is requesting the lexically NEXT item after each
	 *            item in the pdu.
	 * 
	 * @return SnmpPduRequest filled in with the proper response, or null if
	 *         cannot process NOTE: this might be changed to throw an exception.
	 */
	public SnmpPduRequest snmpReceivedGet(SnmpPduPacket pdu, boolean getNext)
	{
		try
		{
			SnmpPduRequest response = null;
			int pduLength = pdu.getLength();
			final boolean trace = log.isTraceEnabled();

			if (trace)
				log.trace("requestId=" + pdu.getRequestId() + ", pduLength="
						+ pduLength + ", getNext=" + getNext);

			SnmpVarBind[] vblist = new SnmpVarBind[pduLength];
			int errorStatus = SnmpPduPacket.ErrNoError;
			int errorIndex = 0;

			// Process for each varibind in the request
			for (int i = 0; i < pduLength; i++)
			{
				boolean good = true;
				SnmpVarBind vb = pdu.getVarBindAt(i);
				SnmpObjectId oid = vb.getName();
				if (getNext)
				{
					/*
					 * We call getNextOid() to find out what is the next valid OID
					 * instance in the supported MIB (sub-)tree. Assign that OID to the
					 * VB List and then proceed same as that of get request. If the
					 * passed oid is already the last, we flag it.
					 */
					ComparableSnmpObjectId coid = new ComparableSnmpObjectId(oid);
					oid = getNextOid(coid, true);
					if (oid == null)
					{
						good = false;
					}
					else
					{
						pdu.setVarBindAt(i, new SnmpVarBind(oid));
					}
				}
				if (oid!=null)
					vblist[i] = new SnmpVarBind(oid);
				else
					vblist[i] = new SnmpVarBind(vb.getName()); // oid passed in
				

				if (trace)
					log.trace("oid=" + oid);

				SnmpSyntax result = null;
				if (good && bindings != null)
					result = getValueFor(oid);

				if (trace)
					log.trace("got result of " + result);

				if (result == null || !good)
				{
					errorStatus = SnmpPduPacket.ErrNoSuchName;
					errorIndex = i + 1;
					log.debug("Error Occured " + vb.getName().toString());
				} 
				else
				{
					vblist[i].setValue(result);
					log.debug("Varbind[" + i + "] := "
									+ vblist[i].getName().toString());
					log.debug(" --> " + vblist[i].getValue().toString());
				}
			} // for ...
			response = new SnmpPduRequest(SnmpPduPacket.RESPONSE, vblist);
			response.setErrorStatus(errorStatus);
			response.setErrorIndex(errorIndex);
			return response;
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <P>
	 * This method is defined to handle SNMP Set requests that are received by
	 * the session. The request has already been validated by the system. This
	 * routine will build a response and pass it back to the caller.
	 * </P>
	 * 
	 * @param pdu
	 *           The SNMP pdu
	 * 
	 * @return SnmpPduRequest filled in with the proper response, or null if
	 *         cannot process NOTE: this might be changed to throw an exception.
	 */
	public SnmpPduRequest snmpReceivedSet(SnmpPduPacket pdu)
   {
		final boolean trace = log.isTraceEnabled();
		SnmpPduRequest response = null;
		int errorStatus = SnmpPduPacket.ErrNoError;
		int errorIndex = 0;
		int k = pdu.getLength();
		SnmpVarBind[] vblist = new SnmpVarBind[k];

		for (int i = 0; i < k; i++)
      {
			SnmpVarBind vb = pdu.getVarBindAt(i);
			vblist[i] = new SnmpVarBind(vb);
			SnmpObjectId oid = vb.getName();
			SnmpSyntax newVal = vb.getValue();
			if (trace)
				log.trace("set: received oid " + oid.toString() + " with value " + newVal.toString());
			SnmpSyntax result = null;
			try
         {
				result = setValueFor(oid,newVal);
			}
         catch (ReadOnlyException e)
         {
				errorStatus = SnmpPduPacket.ErrReadOnly;
				errorIndex = i + 1;
			}

			 if (result != null)
			 {
				errorStatus = SnmpPduPacket.ErrReadOnly;
				errorIndex = i + 1;
				log.debug("Error occured " + vb.getName().toString());
			 }

			 if (trace)
          {
				 log.trace("Varbind[" + i + "] := " + vb.getName().toString());
				 log.trace(" --> " + vb.getValue().toString());
			 }
		}
		response = new SnmpPduRequest(SnmpPduPacket.RESPONSE, vblist);
		response.setErrorStatus(errorStatus);
		response.setErrorIndex(errorIndex);

		return response;
	}

	/**
	 * <P>
	 * This method is defined to handle SNMP requests that are received by the
	 * session. The parameters allow the handler to determine the host, port,
	 * and community string of the received PDU
	 * </P>
	 * 
	 * @param session
	 *            The SNMP session
	 * @param manager
	 *            The remote sender
	 * @param port
	 *            The remote senders port
	 * @param community
	 *            The community string
	 * @param pdu
	 *            The SNMP pdu
	 * 
	 */
	public void snmpReceivedPdu(SnmpAgentSession session, InetAddress manager,
			int port, SnmpOctetString community, SnmpPduPacket pdu)
   {
		log.error("Message from manager " + manager.toString() + " on port " + port);
		int cmd = pdu.getCommand();
		log.error("Unsupported PDU command......... " + cmd);
	}

	/**
	 * <P>
	 * This method is invoked if an error occurs in the session. The error code
	 * that represents the failure will be passed in the second parameter,
	 * 'error'. The error codes can be found in the class SnmpAgentSession
	 * class.
	 * </P>
	 * 
	 * <P>
	 * If a particular PDU is part of the error condition it will be passed in
	 * the third parameter, 'pdu'. The pdu will be of the type SnmpPduRequest or
	 * SnmpPduTrap object. The handler should use the "instanceof" operator to
	 * determine which type the object is. Also, the object may be null if the
	 * error condition is not associated with a particular PDU.
	 * </P>
	 * 
	 * @param session
	 *            The SNMP Session
	 * @param error
	 *            The error condition value.
	 * @param ref
	 *            The PDU reference, or potentially null. It may also be an
	 *            exception.
	 */
	public void SnmpAgentSessionError(SnmpAgentSession session, int error, Object ref)
   {
		log.error("An error occured in the trap session");
		log.error("Session error code = " + error);
		if (ref != null)
      {
			log.error("Session error reference: " + ref.toString());
		}

		if (error == SnmpAgentSession.ERROR_EXCEPTION)
      {
			synchronized (session)
         {
				session.notify(); // close the session
			}
		}
	}

   // Private -------------------------------------------------------
   
	/**
	 * Initialize the bindings from the file given in resourceName
	 */
	private void initializeBindings() throws Exception
   {
      log.debug("Reading resource: '" + resourceName + "'");
      
      ObjectModelFactory omf = new AttributeMappingsBinding();
      InputStream is = null;
      AttributeMappings mappings = null;
      try
      {
         // locate resource
         is = getClass().getResourceAsStream(resourceName);
         
         // create unmarshaller
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();

         // let JBossXB do it's magic using the AttributeMappingsBinding
         mappings = (AttributeMappings)unmarshaller.unmarshal(is, omf, null);         
      }
      catch (Exception e)
      {
         log.error("Accessing resource '" + resourceName + "'");
         throw e;
      }
      finally
      {
         if (is != null)
         {
            // close the XML stream
            is.close();            
         }
      }
      if (mappings == null)
      {
         log.warn("No bindings found in " + resourceName);
         return;         
      }
      log.debug("Found " + mappings.size() + " attribute mappings"); 		
		/**
		 * We have the MBeans now. Put them into the bindungs.
		 */

		Iterator it = mappings.iterator();
		while (it.hasNext())
      {
		   ManagedBean mmb = (ManagedBean)it.next();
		   String oidPrefix = mmb.getOidPrefix();
		   List attrs = mmb.getAttributes();
		   Iterator aIt = attrs.iterator();
		   while (aIt.hasNext())
		   {
		      MappedAttribute ma = (MappedAttribute)aIt.next();
		      String oid;
		      if (oidPrefix != null)
		         oid = oidPrefix + ma.getOid();
		      else
		         oid = ma.getOid();
           
		      BindEntry be = new BindEntry(oid, mmb.getName(), ma.getName());
		      be.isReadWrite = ma.isReadWrite();
  			  
  			  ComparableSnmpObjectId coid = new ComparableSnmpObjectId(oid);
  			  
		      if (log.isTraceEnabled())
		         log.trace("New bind entry   " + be);
  			  if (bindings.containsKey(coid)) {
		         log.info("Duplicate oid " + oid + SKIP_ENTRY);
		         continue;
		      }
		      if (mmb.getName() == null || mmb.getName().equals(""))
		      {
		         log.info("Invalid mbean name for oid " + oid + SKIP_ENTRY);
		         continue;
		      }
		      if (ma.getName() == null || ma.getName().equals(""))
		      {
		         log.info("Invalid attribute name " + ma.getName() + " for oid " + oid + SKIP_ENTRY);
		         continue;
		      }
  			  bindings.put(coid, be);
  			  oidKeys.add(coid);
  			  
		   }
      }
   }

	/**
	 * Return the current value for the given oid
	 * 
	 * @param oid
	 *            The oid we want a value for
	 * @return SnmpNull if no value present
	 */
	private SnmpSyntax getValueFor(final SnmpObjectId oid) {

		BindEntry be = findBindEntryForOid(oid);
		SnmpSyntax ssy = null;
		if (be != null)
      {
			if (log.isTraceEnabled())
				log.trace("Found entry " + be.toString() + " for oid " + oid);
         
			try
         {
			   Object val = server.getAttribute(be.mbean, be.attr.getName());

				if (val instanceof Long)
            {
				   String return64bitType = System.getProperty(USE_TYPE_64, "false");
					Long uin = (Long) val;
					if(return64bitType.equals("true")) {
					   ssy = new SnmpCounter64(uin);
					}else { //default return 32bit
					ssy = new SnmpUInt32(uin);
				}
				}
            else if (val instanceof String)
            {
					String in = (String) val;
					ssy = new SnmpOctetString(in.getBytes());
				}
            else if (val instanceof Integer)
            {
					Integer in = (Integer) val;
					ssy = new SnmpInt32(in);
				}
            else if (val instanceof SnmpObjectId)
            {
					ssy = (SnmpObjectId)val;
				}
            else if (val instanceof SnmpTimeTicks)
            {
               ssy = (SnmpTimeTicks)val;
            }
            else
					log.info("Unknown type for " + be);
			}
         catch (Exception e)
         {
				log.warn("getValueFor (" + be.mbean.toString() + ", "
						+ be.attr.getName() + ": " + e.toString());
         }
      }
      else
      {
			ssy = new SnmpNull();
			log.info(NO_ENTRY_FOUND_FOR_OID + oid);
		}
		return ssy;
	}
	
	/**
	 * Set a jmx attribute
	 * @param oid The oid to set. This is translated into a mbean / attribute pair
	 * @param newVal The new value to set
	 * @return null on success, non-null on failure
	 * @throws ReadOnlyException If the referred entry is read only.
	 */
	private SnmpSyntax setValueFor(final SnmpObjectId oid, final SnmpSyntax newVal) throws ReadOnlyException
   {
		final boolean trace = log.isTraceEnabled();
		
		BindEntry be = findBindEntryForOid(oid);
		
		if (trace)
			log.trace("setValueFor: found bind entry for " + oid);
		
		SnmpSyntax ssy = null;
		if (be != null)
      {
			if (trace)
				log.trace("setValueFor: " + be.toString());
         
			if (be.isReadWrite == false)
         {
				if (trace)
					log.trace("setValueFor: this is marked read only");
            
				throw new ReadOnlyException(oid);
			}
			try
         {
				Object val = null;
				if (newVal instanceof SnmpOctetString)
            {
					val = newVal.toString();
				}
				else if (newVal instanceof SnmpInt32)
            {
					val = new Integer(((SnmpInt32)newVal).getValue());
				}
				else if (newVal instanceof SnmpUInt32)
            {
					val = new Long(((SnmpUInt32)newVal).getValue());
				}
            else if (newVal instanceof SnmpCounter64)
            {
               BigInteger bi = BigInteger.ZERO;
               val = bi.add(((SnmpCounter64)newVal).getValue()); //add newVal to ZERO
				}
				// TODO do more mumbo jumbo for type casting / changing
				
				if (val != null)
            { 
					Attribute at = new Attribute(be.attr.getName(), val);
					server.setAttribute(be.mbean, at);
					if (trace)
						log.trace("setValueFor: set attribute in mbean-Server");
				}
				else
            {
					log.debug("Did not find a suitable data type for newVal " + newVal);
					ssy = new SnmpNull();
				}
				// TODO
			}
			catch (Exception e )
         {
				log.debug("setValueFor: exception " + e.getMessage());
				ssy = new SnmpNull();
			}
		}
		else
      {
			ssy = new SnmpNull();
			log.info(NO_ENTRY_FOUND_FOR_OID + oid);
		}
		return ssy;
	}


	/**
	 * Lookup a BinEntry on the given oid. If the oid ends in .0,
	 * then the .0 will be stripped of before the search.
	 * @param oid The oid look up.
	 * @return a bind entry or null.
	 */
	private BindEntry findBindEntryForOid(final SnmpObjectId oid) {
		
		ComparableSnmpObjectId coid= new ComparableSnmpObjectId(oid);
		
		if (coid.isLeaf())
		{
			coid = coid.removeLastPart();
		}
		BindEntry be = (BindEntry)bindings.get(coid);

		return be;
	}

	/**
	 * Return the next oid that is larger than ours.
	 * @param oid the starting oid
	 * @param stayInSubtree if true, the next oid will not have a different prefix than the one of oid.
	 * @return the next oid or null if none found.
	 */
	private ComparableSnmpObjectId getNextOid(final ComparableSnmpObjectId oid, boolean stayInSubtree) {
		ComparableSnmpObjectId coid = new ComparableSnmpObjectId(oid);


		if (coid.isLeaf())
			coid = coid.removeLastPart();

		SortedSet ret;
		ret= oidKeys.tailSet(oid);  // get oids >= oid
		Iterator it = ret.iterator();
		ComparableSnmpObjectId roid=null;
		
		/*
		 * If there are elements in the tail set, then
		 * - get first one.
		 * - if first is input (which it is supposed to be according to the contract of
		 *   SortedSet.tailSet() , then get next, which is the 
		 *   one we look for.
		 */
		if (it.hasNext()) 
		{
			roid = (ComparableSnmpObjectId)it.next(); // oid
		}
		
		if (roid == null)
		{
			return null; // roid is null, 
		}
		
		if (roid.compareTo(coid)==0) // input elment
		{
			// if there is a next element, then it is ours.
			if (it.hasNext()) 
			{
				roid = (ComparableSnmpObjectId)it.next();
			}
			else
			{
				roid = null; // end of list
			}
		}
      
		/*
		 * Check if still in subtree if requested to stay within
		 */
		if (stayInSubtree && roid != null)
		{
			ComparableSnmpObjectId parent = coid.removeLastPart();
			if (!parent.isRootOf(roid))
				roid = null;
		}

		return roid;
	}


   // Inner Class ---------------------------------------------------
   
	/**
	 * An entry containing the mapping between oid and mbean/attribute
	 * 
	 * @author <a href="mailto:pilhuhn@user.sf.net>">Heiko W. Rupp</a>
	 */
	private class BindEntry implements Comparable {
		private final ComparableSnmpObjectId oid;

		private ObjectName mbean;
      private Attribute attr;
		private String mName;
      private String aName;      
		private boolean isReadWrite = false;

		/**
		 * Constructs a new BindEntry
		 * 
		 * @param oid
		 *            The SNMP-oid, this entry will use.
		 * @param mbName
		 *            The name of an MBean with attribute to query
		 * @param attrName
		 *            The name of the attribute to query
		 */
		BindEntry(final String oidString, final String mbName, final String attrName) {
			this(new ComparableSnmpObjectId(oidString), 
					mbName,
					attrName);
		}
		
		/**
		 * Constructs a new BindEntry.
		 * @param coid The SNMP-oid, this entry will use.
		 * @param mbName The name of an MBean with attribute to query
		 * @param attrName The name of the attribute to query
		 */
		BindEntry(final ComparableSnmpObjectId coid, final String mbName, final String attrName) {
			oid = coid;
			this.mName = mbName;
			this.aName = attrName;
			try
         {
			   mbean = new ObjectName(mbName);
				attr = new Attribute(attrName, null);

			}
         catch (Exception e)
         {
            log.warn(e.toString());
				mName = "-unset-";
				aName = "-unset-";
			}
		}

		/**
		 * A string representation of this BindEntry
		 */
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("[oid=");
			buf.append(oid).append(", mbean=");
			buf.append(mName).append(", attr=");
			buf.append(aName).append(", rw=");
			buf.append(isReadWrite).append("]");

			return buf.toString();
		}

		public Attribute getAttr() {
			return attr;
		}

		public ObjectName getMbean()
      {
			return mbean;
		}

		public ComparableSnmpObjectId getOid()
      {
			return oid;
		}


		/**
		 * Compare two BindEntries. Ordering is defined at oid-level.
		 * 
		 * @param other
		 *            The BindEntry to compare to.
		 * @return 0 on equals, 1 if this is bigger than other
		 */
		public int compareTo(Object other)
      {
			if (other == null)
				throw new NullPointerException("Can't compare to NULL");

			if (!(other instanceof BindEntry))
				throw new ClassCastException("Parameter is no BindEntry");

			// trivial case
			if (this.equals(other))
				return 0;
         
			BindEntry obe = (BindEntry) other;
			if (getOid().equals(obe.getOid()))
				return 0;

			int res =oid.compare(obe.getOid());
			return res;
		}

	}

}
