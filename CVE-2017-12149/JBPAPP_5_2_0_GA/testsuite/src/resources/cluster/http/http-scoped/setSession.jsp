<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
   import="org.jboss.test.cluster.web.DeserializationSensor"
%>

<html>
<center>
<% 
   String id=request.getSession().getId();
   session.setAttribute("TEST_ID",id); 
   Person ben=new Person("Ben", 55);
   session.setAttribute("TEST_PERSON", ben);
   session.setAttribute("TEST_DESER", new DeserializationSensor());
%>
<%=id%>

<h1><%=application.getServerInfo()%>:<%=request.getServerPort()%></h1>
</body>
</html>
