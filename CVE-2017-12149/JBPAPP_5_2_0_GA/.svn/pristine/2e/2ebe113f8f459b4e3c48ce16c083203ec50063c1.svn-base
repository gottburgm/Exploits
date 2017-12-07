<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>

<% 
   String name  = ((Person)session.getAttribute("TEST_PERSON")).getName();
   response.setHeader("SECOND", name);
   
   session.removeAttribute("TEST_PERSON");
   
   ServletContext ctx = application.getContext("/http-cross-ctx-third");
   RequestDispatcher disp = ctx.getRequestDispatcher("/removeAttribute.jsp");
   
   disp.include(request, response);
%>
