/*
 * Generated file - Do not edit!
 */
package org.jboss.test.cmp2.cmrstress.interfaces;

/**
 * Utility class for Parent.
 */
public class ParentUtil
{
   /** Cached remote home (EJBHome). Uses lazy loading to obtain its value (loaded by getHome() methods). */
   private static org.jboss.test.cmp2.cmrstress.interfaces.ParentHome cachedRemoteHome = null;

   /** Cached local home (EJBLocalHome). Uses lazy loading to obtain its value (loaded by getLocalHome() methods). */
   private static org.jboss.test.cmp2.cmrstress.interfaces.ParentLocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain remote home interface from default initial context
    * @return Home interface for Parent. Lookup using JNDI_NAME
    */
   public static org.jboss.test.cmp2.cmrstress.interfaces.ParentHome getHome() throws javax.naming.NamingException
   {
      if (cachedRemoteHome == null) {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try {
            java.lang.Object objRef = initialContext.lookup(org.jboss.test.cmp2.cmrstress.interfaces.ParentHome.JNDI_NAME);
            cachedRemoteHome = (org.jboss.test.cmp2.cmrstress.interfaces.ParentHome) javax.rmi.PortableRemoteObject.narrow(objRef, org.jboss.test.cmp2.cmrstress.interfaces.ParentHome.class);
         } finally {
            initialContext.close();
         }
      }
      return cachedRemoteHome;
   }

   /**
    * Obtain remote home interface from parameterised initial context
    * @param environment Parameters to use for creating initial context
    * @return Home interface for Parent. Lookup using JNDI_NAME
    */
   public static org.jboss.test.cmp2.cmrstress.interfaces.ParentHome getHome( java.util.Hashtable environment ) throws javax.naming.NamingException
   {
      // Obtain initial context
      javax.naming.InitialContext initialContext = new javax.naming.InitialContext(environment);
      try {
         java.lang.Object objRef = initialContext.lookup(org.jboss.test.cmp2.cmrstress.interfaces.ParentHome.JNDI_NAME);
         return (org.jboss.test.cmp2.cmrstress.interfaces.ParentHome) javax.rmi.PortableRemoteObject.narrow(objRef, org.jboss.test.cmp2.cmrstress.interfaces.ParentHome.class);
      } finally {
         initialContext.close();
      }
   }

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for Parent. Lookup using JNDI_NAME
    */
   public static org.jboss.test.cmp2.cmrstress.interfaces.ParentLocalHome getLocalHome() throws javax.naming.NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if (cachedLocalHome == null) {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try {
            cachedLocalHome = (org.jboss.test.cmp2.cmrstress.interfaces.ParentLocalHome) initialContext.lookup(org.jboss.test.cmp2.cmrstress.interfaces.ParentLocalHome.JNDI_NAME);
         } finally {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

}