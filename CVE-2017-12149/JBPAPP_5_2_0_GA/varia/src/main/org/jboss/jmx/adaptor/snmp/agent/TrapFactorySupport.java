/*
 * Copyright (c) 2003,  Intracom S.A. - www.intracom.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * This package and its source code is available at www.jboss.org
**/
package org.jboss.jmx.adaptor.snmp.agent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.management.Notification;

import org.jboss.jmx.adaptor.snmp.config.notification.Mapping;
import org.jboss.jmx.adaptor.snmp.config.notification.VarBind;
import org.jboss.jmx.adaptor.snmp.config.notification.VarBindList;
import org.jboss.logging.Logger;
import org.jboss.xb.binding.GenericObjectModelFactory;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.xml.sax.Attributes;

/**
 * <tt>TrapFactorySupport</tt> takes care of translation of Notifications
 * into SNMP V1 and V2 traps
 *
 * Data Structure Guide
 *
 * It looks complicated but it ain't. The mappings are read into a structure
 * that follows the outline defined in the Notification.xsd. Have a look
 * there and in the example notificationMap.xml and you should get the picture. 
 * As an optimization, 2 things are done:
 *
 * 1.   The "NotificationType" fields of all the mappings are 
 *      read, interpreted and compiled as regular expressions. All the 
 *      instances are placed in an array and made accessible in their compiled 
 *      form
 * 2.   The "wrapperClass" attribute is interpreted as a class name that 
 *      implements interface NotificationWrapper. An instance of each class is 
 *      created and similarly placed in an array 
 *
 * This results in 2 collections one of regular expressions and one of 
 * NotificationWrapper instances. The two collections have exactly the same
 * size as the collection of mappings. Obviously each read mapping has a "1-1"
 * correspondence with exactly 1 compiled regular expression and exactly 1
 * NotificationWrapper instance. The key for the correspondence is the index: 
 * regular expression i corresponds to mapping i that coresponds to 
 * NotificationWrapper instance i. The loading of the 2 collections is 
 * performed in method startService.
 * Checking for which mapping to apply (implemented in method findMapping) on a 
 * notification is simple: traverse the cached regular expressions and attempt 
 * to match the notification type against them. The FIRST match short circuits 
 * the search and the coresponding mapping index is returned.
 *
 * @version $Revision: 44604 $
 *
 * @author  <a href="mailto:spol@intracom.gr">Spyros Pollatos</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
**/
public class TrapFactorySupport
   implements TrapFactory
{
   /** The logger object */
   private static final Logger log = Logger.getLogger(TrapFactorySupport.class);

   /** Reference to SNMP variable binding factory */
   private SnmpVarBindFactory snmpVBFactory = null;
   
   /** File that contains notification mappings */
   private String notificationMapResName = null;
   
   /** Uptime clock */
   private Clock clock = null;
   
   /** Trap counter */
   private Counter trapCount = null;
   
   /** Contains the read in mappings */
   private ArrayList notificationMapList = null;
    
   /** Contains the compiled regular expression type specifications */
   private ArrayList mappingRegExpCache = null;
   
   /** Contains instances of the notification wrappers */
   private ArrayList notificationWrapperCache = null;
   
   /**
    * Create TrapFactorySupport
   **/
   public TrapFactorySupport()
   {
      this.snmpVBFactory = new SnmpVarBindFactory();
   }

   /**
    * Sets the name of the file containing the notification/trap mappings,
    * the uptime clock and the trap counter
   **/ 
   public void set(String notificationMapResName, Clock clock, Counter count)
   {
      this.notificationMapResName = notificationMapResName;
      this.clock = clock;
      this.trapCount = count;
   }
   
   /**
    * Populates the regular expression and wrapper instance collections. Note 
    * that a failure (e.g. to compile a regular expression or to instantiate a 
    * wrapper) generates an error message. Furthermore, the offending 
    * expression or class are skipped and the corresponding collection entry 
    * is null. It is the user's responsibility to track the reported errors in 
    * the logs and act accordingly (i.e. correct them and restart). If not the 
    * corresponding mappings are effectively void and will NOT have effect. 
   **/    
   public void start()
      throws Exception
   {
      log.debug("Reading resource: '" + notificationMapResName + "'");
      
      ObjectModelFactory omf = new NotificationBinding();
      InputStream is = null;
      try
      {
         // locate notifications.xml
         is = this.getClass().getResourceAsStream(notificationMapResName);
         
         // create unmarshaller
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();

         // let JBossXB do it's magic using the MappingObjectModelFactory
         this.notificationMapList = (ArrayList)unmarshaller.unmarshal(is, omf, null);         
      }
      catch (Exception e)
      {
         log.error("Accessing resource '" + notificationMapResName + "'");
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
      log.debug("Found " + notificationMapList.size() + " notification mappings");   
      
      // Initialise the cache with the compiled regular expressions denoting 
      // notification type specifications
      this.mappingRegExpCache = 
         new ArrayList(notificationMapList.size());
        
      // Initialise the cache with the instantiated notification wrappers
      this.notificationWrapperCache =
         new ArrayList(notificationMapList.size());
        
      for (Iterator i = notificationMapList.iterator(); i.hasNext(); )
      {
         Mapping mapping = (Mapping)i.next();
         
         // Compile and add the regular expression
         String notificationType = mapping.getNotificationType();
         
         try
         {
            Pattern re = Pattern.compile(notificationType);
            this.mappingRegExpCache.add(re);
         }
         catch (PatternSyntaxException e)
         {
            // Fill the slot to keep index count correct
            this.mappingRegExpCache.add(null);
                
            log.warn("Error compiling notification mapping for type: " + notificationType, e); 
         }
            
         // Instantiate and add the wrapper
         // Read wrapper class name 
         String wrapperClassName = mapping.getVarBindList().getWrapperClass();
                
         log.debug("notification wrapper class: " + wrapperClassName);
         
         try
         {
            NotificationWrapper wrapper =
               (NotificationWrapper)Class.forName(wrapperClassName, true, this.getClass().getClassLoader()).newInstance();
                
            // Initialise it
            wrapper.set(this.clock, this.trapCount);
            
            // Add the wrapper to the cache
            this.notificationWrapperCache.add(wrapper);
         }
         catch (Exception e)
         {
            // Fill the slot to keep index count correct
            this.notificationWrapperCache.add(null);
                
            log.warn("Error compiling notification mapping for type: " + notificationType, e);  
         }
      }
      log.debug("Trap factory going active");                                                       
   }
    
   /**
    * Locate mapping applicable for the incoming notification. Key is the
    * notification's type
    *
    * @param n the notification to be examined
    * @return the index of the mapping
    * @throws IndexOutOfBoundsException if no mapping found
   **/ 
   private int findMappingIndex(Notification n)
      throws IndexOutOfBoundsException
   {
      // Sequentially check the notification type against the compiled 
      // regular expressions. On first match return the coresponding mapping
      // index
      for (int i = 0; i < notificationMapList.size(); i++)
      {
         Pattern p = (Pattern) this.mappingRegExpCache.get(i);
            
         if (p != null)
         {
            Matcher m = p.matcher(n.getType());
            
            if (m.matches())
            {
               if (log.isTraceEnabled())
                  log.trace("Match for '" + n.getType() + "' on mapping " + i);
               return i;
            }
         }
      }
      // Signal "no mapping found"
      throw new IndexOutOfBoundsException();
   }
    
   /**
    * Traslates a Notification to an SNMP V1 trap.
   **/
   public SnmpPduTrap generateV1Trap(Notification n) 
      throws MappingFailedException
   {
      if (log.isTraceEnabled())
         log.trace("generateV1Trap");
        
      // Locate mapping for incomming event
      int index = -1;
        
      try
      {
         index = findMappingIndex(n);
      }
      catch (IndexOutOfBoundsException e)
      {
         throw new MappingFailedException("No mapping found for notification type: '" + 
                    n.getType() + "'");
      }
        
      Mapping m = (Mapping)this.notificationMapList.get(index);
        
      // Create trap
      SnmpPduTrap trapPdu = new SnmpPduTrap();
        
      trapPdu.setTimeStamp(this.clock.uptime());
        
      // Organise the 'variable' payload 
      trapPdu.setGeneric(m.getGeneric());
      trapPdu.setSpecific(m.getSpecific());
      trapPdu.setEnterprise(m.getEnterprise());
        
      // Append the specified varbinds. Get varbinds from mapping and for
      // each one of the former use the wrapper to get the corresponding
      // values

      // Get the coresponding wrapper to get access to notification payload
      NotificationWrapper wrapper =
         (NotificationWrapper)this.notificationWrapperCache.get(index);
        
      if(wrapper != null)
      {
         // Prime the wrapper with the notification contents
         wrapper.prime(n);
            
         // Iterate through mapping specified varbinds and organise values
         // for each
         List vbList = m.getVarBindList().getVarBindList();
         
         for (int i = 0; i < vbList.size(); i++)
         {
            VarBind vb = (VarBind)vbList.get(i);
                
            // Append the var bind. Interrogate read vb for OID and 
            // variable tag. The later is used as the key passed to the 
            // wrapper in order for it to locate the required value. That 
            // value and the aforementioned OID are used to generate the 
            // variable binding
            trapPdu.addVarBind(
               this.snmpVBFactory.make(vb.getOid(), wrapper.get(vb.getTag())));
         }
      }
      else
      {
         throw new MappingFailedException(
            "Varbind mapping failure: null wrapper defined for " +
            " notification type '" + m.getNotificationType() + "'" );
      }
      return trapPdu;        
   }
    
   /**
    * Traslates a Notification to an SNMP V2 trap.
    *
    * TODO: how do you get timestamp, generic, and specific stuff in the trap
   **/
   public SnmpPduPacket generateV2Trap(Notification n) 
      throws MappingFailedException
   {
      if (log.isTraceEnabled())
         log.trace("generateV2Trap");
        
      // Locate mapping for incomming event
      int index = -1;
        
      try
      {
         index = findMappingIndex(n);
      }
      catch (IndexOutOfBoundsException e)
      {
         throw new MappingFailedException(
            "No mapping found for notification type: '" + n.getType() + "'");
      }
        
      Mapping m = (Mapping)this.notificationMapList.get(index);
      
      // Create trap
      SnmpPduRequest trapPdu = new SnmpPduRequest(SnmpPduPacket.V2TRAP);
        
      // Append the specified varbinds. Get varbinds from mapping and for
      // each one of the former use the wrapper to get data from the 
      // notification

      // Get the coresponding wrapper
      NotificationWrapper wrapper =
         (NotificationWrapper)this.notificationWrapperCache.get(index);
        
      if (wrapper != null)
      {
         // Prime the wrapper with the notification contents
         wrapper.prime(n);
            
         List vbList = m.getVarBindList().getVarBindList();
         
         for (int i = 0; i < vbList.size(); i++)
         {
            VarBind vb = (VarBind)vbList.get(i);
                
            // Append the var bind. Interrogate read vb for OID and 
            // variable tag. The later is used as the key passed to the 
            // wrapper in order for it to locate the required value. That 
            // value and the aforementioned OID are used to generate the 
            // variable binding
            trapPdu.addVarBind(
               this.snmpVBFactory.make(vb.getOid(), wrapper.get(vb.getTag())));
         }
      }
      else
      {
         log.warn("Varbind mapping failure: null wrapper defined for " +
                  " notification type '" + m.getNotificationType() + "'" );
      }
      return trapPdu;
   }
   
   /**
    * Utility class used by JBossXB to help parse notifications.xml 
    */
   private static class NotificationBinding implements GenericObjectModelFactory
   {
      // GenericObjectModelFactory implementation ----------------------

      public Object completeRoot(Object root, UnmarshallingContext ctx,
            String uri, String name)
      {
         return root;
      }

      public Object newRoot(Object root, UnmarshallingContext navigator, String namespaceURI,
                            String localName, Attributes attrs)
      {
         ArrayList notifList;
         
         if (root == null)
         {
            root = notifList = new ArrayList();
         }
         else
         {
            notifList = (ArrayList) root;
         }
         return root;
      }
      
      public Object newChild(Object parent, UnmarshallingContext navigator, String namespaceURI,
                             String localName, Attributes attrs)
      {
         Object child = null;

         if ("mapping".equals(localName))
         {
            Mapping m = new Mapping();
            child = m;
         }
         else if ("var-bind-list".equals(localName))
         {
            VarBindList vblist = new VarBindList();
            child = vblist;
            if (attrs.getLength() > 0)
            {
               for (int i = 0; i < attrs.getLength(); i++)
               {
                  if ("wrapper-class".equals(attrs.getLocalName(i)))
                  {
                     vblist.setWrapperClass(attrs.getValue(i));
                  }
               }
            }
            // check that wrapper-class is set
            if (vblist.getWrapperClass() == null)
            {
               throw new RuntimeException("'wrapper-class' must be set at 'var-bind-list' element");
            }
         }
         else if ("var-bind".equals(localName))
         {
            VarBind vb = new VarBind();
            child = vb;
         }
         return child;
      }

      public void addChild(Object parent, Object child, UnmarshallingContext navigator,
                           String namespaceURI, String localName)
      {
         if (parent instanceof ArrayList)
         {
            ArrayList notifList = (ArrayList)parent;
            
            if (child instanceof Mapping)
            {
               notifList.add(child);
            }
         }
         else if (parent instanceof Mapping)
         {
            Mapping m = (Mapping)parent;
            
            if (child instanceof VarBindList)
            {
               m.setVarBindList((VarBindList)child);
            }
         }
         else if (parent instanceof VarBindList)
         {
            VarBindList vblist = (VarBindList)parent;
            
            if (child instanceof VarBind)
            {
               vblist.addVarBind((VarBind)child);
            }
         }
      }
      
      public void setValue(Object o, UnmarshallingContext navigator, String namespaceURI,
                           String localName, String value)
      {
         if (o instanceof Mapping)
         {
            Mapping m = (Mapping)o;
            
            if ("notification-type".equals(localName))
            {
               m.setNotificationType(value);
            }
            else if ("generic".equals(localName))
            {
               m.setGeneric(Integer.parseInt(value));
            }
            else if ("specific".equals(localName))
            {
               m.setSpecific(Integer.parseInt(value));
            }
            else if ("enterprise".equals(localName))
            {
               m.setEnterprise(value);
            }
         }
         else if (o instanceof VarBind)
         {
            VarBind vb = (VarBind)o;
            
            if ("tag".equals(localName))
            {
               vb.setTag(value);
            }
            else if ("oid".equals(localName))
            {
               vb.setOid(value);
            }
         }
      }

      public Object completedRoot(Object root, UnmarshallingContext navigator, String namespaceURI, String localName)
      {
         return root;
      }      
   }
   
} // class TrapFactorySupport
