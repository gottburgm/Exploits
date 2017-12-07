<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>

<% 
   int age = ((Person)session.getAttribute("TEST_PERSON")).getAge();
   session.removeAttribute("TEST_PERSON");
%>
<%= age %>
