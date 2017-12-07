/*
 * Generated file - Do not edit!
 */
package org.jboss.test.cmp2.cmrtree.ejb;

/**
 * Utility class for B.
 */
public class BUtil
{

   /** Cached local home (EJBLocalHome). Uses lazy loading to obtain its value (loaded by getLocalHome() methods). */
   private static org.jboss.test.cmp2.cmrtree.ejb.BLocalHome cachedLocalHome = null;

   // Home interface lookup methods

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for B. Lookup using JNDI_NAME
    */
   public static org.jboss.test.cmp2.cmrtree.ejb.BLocalHome getLocalHome() throws javax.naming.NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      if (cachedLocalHome == null) {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try {
            cachedLocalHome = (org.jboss.test.cmp2.cmrtree.ejb.BLocalHome) initialContext.lookup(org.jboss.test.cmp2.cmrtree.ejb.BLocalHome.JNDI_NAME);
         } finally {
            initialContext.close();
         }
      }
      return cachedLocalHome;
   }

}