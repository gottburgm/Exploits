<%@ page import="javax.naming.InitialContext" %>
<%@ page import="org.jboss.embedded.test.ejb.DAO" %>
<%@ page import="org.jboss.embedded.test.ejb.Customer" %>
<%@ page import="javax.naming.NamingException" %>

<%
   Customer cust = null;
   try
   {
      InitialContext ctx = new InitialContext();
      DAO dao = (DAO) ctx.lookup("DAOBean/local");
      cust = dao.createCustomer("Bill");
      cust = dao.findCustomer("Bill");
   }
   catch (NamingException e)
   {
      throw new RuntimeException(e);
   }
   assert cust != null;
   assert cust.getName().equals("Bill");

%>

<html>
<body>
<h1>SUCCESS</h1>
</body>
</html>
