/*
 * Generated file - Do not edit!
 */
package org.jboss.test.entity.interfaces;

/**
 * Utility class for TestEntity.
 */
public class TestEntityUtil
{

   // Home interface lookup methods

   /**
    * Obtain remote home interface from default initial context
    * @return Home interface for TestEntity. Lookup using JNDI_NAME
    */
   public static org.jboss.test.entity.interfaces.TestEntityHome getHome() throws javax.naming.NamingException
   {
      // Obtain initial context
      javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
      try {
         java.lang.Object objRef = initialContext.lookup(org.jboss.test.entity.interfaces.TestEntityHome.JNDI_NAME);
         return (org.jboss.test.entity.interfaces.TestEntityHome) javax.rmi.PortableRemoteObject.narrow(objRef, org.jboss.test.entity.interfaces.TestEntityHome.class);
      } finally {
         initialContext.close();
      }
   }

   /**
    * Obtain remote home interface from parameterised initial context
    * @param environment Parameters to use for creating initial context
    * @return Home interface for TestEntity. Lookup using JNDI_NAME
    */
   public static org.jboss.test.entity.interfaces.TestEntityHome getHome( java.util.Hashtable environment ) throws javax.naming.NamingException
   {
      // Obtain initial context
      javax.naming.InitialContext initialContext = new javax.naming.InitialContext(environment);
      try {
         java.lang.Object objRef = initialContext.lookup(org.jboss.test.entity.interfaces.TestEntityHome.JNDI_NAME);
         return (org.jboss.test.entity.interfaces.TestEntityHome) javax.rmi.PortableRemoteObject.narrow(objRef, org.jboss.test.entity.interfaces.TestEntityHome.class);
      } finally {
         initialContext.close();
      }
   }

   /**
    * Obtain local home interface from default initial context
    * @return Local home interface for TestEntity. Lookup using JNDI_NAME
    */
   public static org.jboss.test.entity.interfaces.TestEntityLocalHome getLocalHome() throws javax.naming.NamingException
   {
      // Local homes shouldn't be narrowed, as there is no RMI involved.
      // Obtain initial context
      javax.naming.InitialContext initialContext = new javax.naming.InitialContext();
      try {
         return (org.jboss.test.entity.interfaces.TestEntityLocalHome) initialContext.lookup(org.jboss.test.entity.interfaces.TestEntityLocalHome.JNDI_NAME);
      } finally {
         initialContext.close();
      }
   }

}