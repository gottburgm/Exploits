<%@page contentType="text/html"
   import="java.util.*"
   import="javax.servlet.ServletContext"
   import="org.jboss.test.cluster.web.aop.Person"
   import="org.jboss.test.cluster.web.aop.Address"
   import="org.jboss.test.cluster.web.DeserializationSensor"
%>

<html>
<center>
<% 
   String id=request.getSession().getId();
   session.setAttribute("TEST_ID",id); 
   Person ben=new Person();
   Address addr = new Address();
   addr.setZip(95123);
   addr.setCity("San Jose");
   ben.setAge(100);
   ben.setName("Ben");
   ben.setAddress(addr);
   session.setAttribute("TEST_PERSON", ben);
   session.setAttribute("TEST_DESER", new DeserializationSensor());
   
   // Bind ben to the servlet context as well so it can be
   // accessed without involving the session
   ServletContext ctx = getServletConfig().getServletContext();
   ctx.setAttribute("TEST_PERSON", ben);
%>
<%=id%>

<h1><%=application.getServerInfo()%>:<%=request.getServerPort()%></h1>
</body>
</html>
