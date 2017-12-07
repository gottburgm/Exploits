<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>
<% 
	String name = "Joe";
    Person person = (Person)session.getAttribute("TEST_PERSON");
    person.setName(name);
    session.setAttribute("TEST_PERSON", person);
	response.setHeader("FIRST", name);
   
   ServletContext ctx = application.getContext("/http-cross-ctx-second");
   RequestDispatcher disp = ctx.getRequestDispatcher("/modifyAttribute.jsp");
   
   disp.forward(request, response);
%>
