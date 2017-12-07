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
   
   response.setHeader("FIRST", list); 
   
   ServletContext ctx = application.getContext("/http-cross-ctx-second");
   RequestDispatcher disp = ctx.getRequestDispatcher("/attributeNames.jsp");
   
   disp.forward(request, response);
%>

