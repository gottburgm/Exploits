<%@page contentType="text/html"
   import="java.util.*"
   import="org.jboss.test.cluster.web.Person"
%>
<% 
   String name = ((Person)session.getAttribute("TEST_PERSON")).getName();
   response.setHeader("FIRST", name); 
   session.invalidate();  
   
   ServletContext ctx = application.getContext("/http-cross-ctx-second");
   RequestDispatcher disp = ctx.getRequestDispatcher("/invalidateSession.jsp");
   
   disp.forward(request, response);
%>
