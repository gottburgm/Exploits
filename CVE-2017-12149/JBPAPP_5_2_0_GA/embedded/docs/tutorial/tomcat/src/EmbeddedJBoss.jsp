<%@ page import="javax.naming.InitialContext" %>
<%@ page import="org.jboss.embedded.tutorial.war.beans.Customer" %>
<%@ page import="org.jboss.embedded.tutorial.war.beans.CustomerDAOLocal" %>
<%@ page import="org.jboss.embedded.tutorial.war.beans.CustomerDAORemote" %>

<html>
<body>
<%
   InitialContext ctx = new InitialContext();
   CustomerDAOLocal local = (CustomerDAOLocal) ctx.lookup("CustomerDAOBean/local");
   CustomerDAORemote remote = (CustomerDAORemote) ctx.lookup("CustomerDAOBean/remote");

   int id = local.createCustomer("Gavin");
   Customer cust = local.findCustomer(id);
%>
<p>
   Successfully created and found Gavin from @Local interface: <%=cust.getName()%>
</p>
<%
   id = remote.createCustomer("Emmanuel");
   cust = remote.findCustomer(id);
%>

<p>
   Successfully created and found Emmanuel from @Remote interface
</p>

DONE!
</body>
</html>