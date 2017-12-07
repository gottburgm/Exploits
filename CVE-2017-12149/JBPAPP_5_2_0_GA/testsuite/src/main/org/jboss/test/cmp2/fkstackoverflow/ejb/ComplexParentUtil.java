/*
 * Generated file - Do not edit!
 */
package org.jboss.test.cmp2.fkstackoverflow.ejb;

/**
 * Utility class for ComplexParent.
 */
public class ComplexParentUtil
{

   /** Cached local home (EJBLocalHome). Uses lazy loading to obtain its value (loaded by getLocalHome() methods). */
   private static org.jboss.test.cmp2.fkstackoverflow.ejb.ComplexParentLocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for ComplexParent. Lookup using JNDI_NAME
    */
   public static org.jboss.test.cmp2.fkstackoverflow.ejb.ComplexParentLocalHome getLocalHome() throws javax.naming.NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if (cachedLocalHome == null) {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try {
            cachedLocalHome = (org.jboss.test.cmp2.fkstackoverflow.ejb.ComplexParentLocalHome) initialContext.lookup(org.jboss.test.cmp2.fkstackoverflow.ejb.ComplexParentLocalHome.JNDI_NAME);
         } finally {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

}