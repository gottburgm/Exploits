<%@page contentType="text/html"
   import="java.util.*"
   import="java.security.SecureRandom"
   import="javax.servlet.ServletContext"
   import="org.jboss.test.cluster.web.aop.Person"
   import="org.jboss.test.cluster.web.aop.Address"
%>

<% 
   // Modify the POJO that was bound to the servlet context and
   // to the session as well.  Only access it via the servlet context
   // so we can check whether modifying it causes the session
   // to be replicated.
   ServletContext ctx = getServletConfig().getServletContext();
   Person ben = (Person)ctx.getAttribute("TEST_PERSON");
   try
   {
      ben.setAge(new SecureRandom().nextInt(100));
   }
   catch (org.jboss.cache.pojo.PojoCacheAlreadyDetachedException e)
   {
      // This is OK. We're checking that an operation on a detached
      // pojo doesn't cause session repl. Being unable to do the
      // operation is not a negative outcome.
      System.out.println("TEST_PERSON pojo is detached");
   }   
%>
