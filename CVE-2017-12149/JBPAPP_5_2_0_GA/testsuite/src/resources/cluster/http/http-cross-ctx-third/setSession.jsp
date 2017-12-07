<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>

<% 
   String id=request.getSession().getId();
   session.setAttribute("TEST_ID",id); 
   Person person=new Person("Gary", 32);
   session.setAttribute("TEST_PERSON", person);
%>
<%= person.getAge() %>