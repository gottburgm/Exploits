<% 
   String type = "NULL";
   Object subject = session.getAttribute("javax.security.auth.subject");
   if (subject != null)
   {
   	type = subject.getClass().getName();
   }
   else
   {
      Object test = session.getAttribute("TEST");
      if (test != null)
         type = test.getClass().getName();
   }
%>
<%=type%>
