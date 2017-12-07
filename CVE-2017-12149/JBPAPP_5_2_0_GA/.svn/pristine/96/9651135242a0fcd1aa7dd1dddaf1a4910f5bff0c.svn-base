<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>
<%
   String isNew = session.isNew() ? "true" : "false";
   response.setHeader("X-SessionIsNew", isNew);
	
   String name = ((Person)session.getAttribute("TEST_PERSON")).getName();
   response.setHeader("FIRST", name); 
   
   ServletContext ctx = application.getContext("/http-cross-ctx-second");
   RequestDispatcher disp = ctx.getRequestDispatcher("/getAttribute.jsp");
   
   disp.forward(request, response);
%>
