<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>
<% 
	String name = "Tina";
    Person person = (Person)session.getAttribute("TEST_PERSON");
    person.setName(name);
    session.setAttribute("TEST_PERSON", person);
	response.setHeader("SECOND", name);
   
   ServletContext ctx = application.getContext("/http-cross-ctx-third");
   RequestDispatcher disp = ctx.getRequestDispatcher("/modifyAttribute.jsp");
   
   disp.include(request, response);
%>
