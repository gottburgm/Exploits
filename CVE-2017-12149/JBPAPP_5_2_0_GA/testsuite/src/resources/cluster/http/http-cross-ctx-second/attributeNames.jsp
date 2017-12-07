<%@page contentType="text/html"
   import="java.util.*"
%>
<% 
   Enumeration names = session.getAttributeNames();
   String list = "";
   while (names.hasMoreElements())
   {
   	  list += names.nextElement();
   }
   
   response.setHeader("SECOND", list); 
   
   ServletContext ctx = application.getContext("/http-cross-ctx-third");
   RequestDispatcher disp = ctx.getRequestDispatcher("/attributeNames.jsp");
   
   disp.include(request, response);
%>

