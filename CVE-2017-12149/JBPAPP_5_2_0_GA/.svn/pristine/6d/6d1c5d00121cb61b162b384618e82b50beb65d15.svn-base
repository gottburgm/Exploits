<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>

<% 
   // Note: The name are hard-coded in the test case as well!!!
   Person ben = (Person)session.getAttribute("TEST_PERSON");
   ben.setName("Joe");
   session.setAttribute("TEST_PERSON", ben);
%>
