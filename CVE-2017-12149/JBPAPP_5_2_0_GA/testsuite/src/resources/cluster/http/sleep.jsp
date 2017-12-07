<%
   String sleep = request.getParameter("sleep");

   if (sleep != null)
   {
      long ms = Long.parseLong(sleep);
      
      if (ms > 0)
      {
         Thread.sleep(ms);
      }
      
      session.setAttribute("sleep", sleep);
   }
%>