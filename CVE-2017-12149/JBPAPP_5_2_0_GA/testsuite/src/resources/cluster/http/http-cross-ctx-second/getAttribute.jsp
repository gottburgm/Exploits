<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>
<%
	String name = ((Person)session.getAttribute("TEST_PERSON")).getName();
	response.setHeader("SECOND", name); 
   
   ServletContext ctx = application.getContext("/http-cross-ctx-third");
   RequestDispatcher disp = ctx.getRequestDispatcher("/getAttribute.jsp");
   
   disp.include(request, response);
%>
