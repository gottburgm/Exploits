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
package org.jboss.system;

/**
 * An extension of the ServiceMBean interface that provides for
 * declarative JMX notification subscription handling.
 * <p>
 * The SubscriptionList attribute is used to specify the list
 * of MBeans/notifications that the listener service instance
 * will subscribe for.
 * <p>
 * The abstract class ListenerServiceMBeanSupport implements
 * this interface.
 *
 * @see ServiceMBean
 * @see ListenerServiceMBeanSupport
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
**/
public interface ListenerServiceMBean
   extends ServiceMBean
{
   // Constants -----------------------------------------------------
  
   /** The XML subscription-list elements and attributes */
   public static final String SL_ROOT_ELEMENT                = "subscription-list";
   public static final String SL_MBEAN_ELEMENT               = "mbean";
   public static final String SL_FILTER_ELEMENT              = "filter";
   public static final String SL_NOTIFICATION_ELEMENT        = "notification";
   public static final String SL_MBEAN_NAME_ATTRIBUTE        = "name";
   public static final String SL_MBEAN_HANDBACK_ATTRIBUTE    = "handback";
   public static final String SL_FILTER_FACTORY_ATTRIBUTE    = "factory";
   public static final String SL_NOTIFICATION_TYPE_ATTRIBUTE = "type";
   
   // Public --------------------------------------------------------

   /**
    * Used to configure at start-up the JMX notification subscriptions.
    *
    * The configuration is done inline in the mbean descriptor. For example:
    *
    * <code>
    * ...
    * <attribute name="SubscriptionList">
    *   <subscription-list>
    *     <mbean name="jboss.system:*">
    *       <notification type="org.jboss.system.ServiceMBean.start"/>
    *       <notification type="org.jboss.system.ServiceMBean.stop"/>
    *     </mbean>
    *   </subscription-list>
    * </attribute>
    * ...
    * </code>
    *
    * The filter mechanism has been extended to support specification
    * of arbitrary filters, using filter factory plugins:
    * 
    * <code>
    * ...
    * <attribute name="SubscriptionList">
    *   <subscription-list>
    *     <mbean name="jboss.system:*">
    *       <filter factory="NotificationFilterSupportFactory">
    *         <enable type="org.jboss.system.ServiceMBean.start"/>
    *         <enable type="org.jboss.system.ServiceMBean.stop"/>
    *       </filter>
    *     </mbean>
    *     <mbean name="jboss.monitor:service=MemoryMonitor">
    *       <filter factory="AttributeChangeNotificationFilterFactory">
    *         <enable attribute-name="State"/>
    *       </filter>
    *     </mbean>        
    *     <mbean name="JMImplementation:type=MBeanServerDelegate">
    *       <filter factory="MBeanServerNotificationFilterFactory">
    *         <enable type="JMX.mbean"/>
    *         <enable object-name="jboss:type=Service,name=SystemProperties"/>
    *       </filter>
    *     </mbean>
    *   </subscription-list>
    * </attribute>
    * ...
    * </code>
    * 
    * 'factory' is the full class name of a class that implements the
    * org.jboss.system.NotificationFilterFactory interface. If the
    * class cannot be loaded, a second attempt is made to load the
    * class from within the org.jboss.system.filterfactory package.
    * 
    * Three NotificationFilterFactories corresponding to the three
    * "standard" jmx notification filters, have been pre-packaged.
    * 
    * Those are:
    * 
    * @see org.jboss.system.filterfactory.AttributeChangeNotificationFilterFactory
    * @see org.jboss.system.filterfactory.MBeanServerNotificationFilterFactory
    * @see org.jboss.system.filterfactory.NotificationFilterSupportFactory
    *  
    * See also jboss-subscription.dtd
   **/
   public void setSubscriptionList(org.w3c.dom.Element list);
   
}
