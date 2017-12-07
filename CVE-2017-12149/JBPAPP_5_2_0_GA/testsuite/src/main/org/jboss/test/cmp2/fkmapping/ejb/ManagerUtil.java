/*
 * Generated file - Do not edit!
 */
package org.jboss.test.cmp2.fkmapping.ejb;

/**
 * Utility class for Manager.
 */
public class ManagerUtil
{
   /** Cached remote home (EJBHome). Uses lazy loading to obtain its value (loaded by getHome() methods). */
   private static org.jboss.test.cmp2.fkmapping.ejb.ManagerHome cachedRemoteHome = null;

   // Home interface lookup methods

   /**
    * Obtain remote home interface from default initial context
    * @return Home interface for Manager. Lookup using JNDI_NAME
    */
   public static org.jboss.test.cmp2.fkmapping.ejb.ManagerHome getHome() throws javax.naming.NamingException
   {
      if (cachedRemoteHome == null) {
         // Obtain initial context
         javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
         try {
            java.lang.Object objRef = initialContext.lookup(org.jboss.test.cmp2.fkmapping.ejb.ManagerHome.JNDI_NAME);
            cachedRemoteHome = (org.jboss.test.cmp2.fkmapping.ejb.ManagerHome) javax.rmi.PortableRemoteObject.narrow(objRef, org.jboss.test.cmp2.fkmapping.ejb.ManagerHome.class);
         } finally {
            initialContext.close();
         }
      }
      return cachedRemoteHome;
   }

   /**
    * Obtain remote home interface from parameterised initial context
    * @param environment Parameters to use for creating initial context
    * @return Home interface for Manager. Lookup using JNDI_NAME
    */
   public static org.jboss.test.cmp2.fkmapping.ejb.ManagerHome getHome( java.util.Hashtable environment ) throws javax.naming.NamingException
   {
      // Obtain initial context
      javax.naming.InitialContext initialContext = new javax.naming.InitialContext(environment);
      try {
         java.lang.Object objRef = initialContext.lookup(org.jboss.test.cmp2.fkmapping.ejb.ManagerHome.JNDI_NAME);
         return (org.jboss.test.cmp2.fkmapping.ejb.ManagerHome) javax.rmi.PortableRemoteObject.narrow(objRef, org.jboss.test.cmp2.fkmapping.ejb.ManagerHome.class);
      } finally {
         initialContext.close();
      }
   }

}