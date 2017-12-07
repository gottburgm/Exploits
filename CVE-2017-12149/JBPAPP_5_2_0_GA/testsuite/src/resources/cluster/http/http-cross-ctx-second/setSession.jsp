<%@page contentType="text/html"
   import="java.util.*"
   import="javax.servlet.*"
   import="org.jboss.test.cluster.web.Person"
%>

<% 
   String id=request.getSession().getId();
   session.setAttribute("TEST_ID",id); 
   Person person=new Person("Mary", 55);
   session.setAttribute("TEST_PERSON", person);  
   
   response.setHeader("SECOND", person.getName());
   
   ServletContext ctx = application.getContext("/http-cross-ctx-third");
   RequestDispatcher disp = ctx.getRequestDispatcher("/setSession.jsp");
   disp.include(request, response);
%>
