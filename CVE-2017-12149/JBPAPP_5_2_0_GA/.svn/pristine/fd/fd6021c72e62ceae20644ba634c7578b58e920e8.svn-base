<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>
<% 
   Person person = (Person)session.getAttribute("TEST_PERSON");
   person.setAge(41);
   session.setAttribute("TEST_PERSON", person);
%>
<%= person.getAge() %>
