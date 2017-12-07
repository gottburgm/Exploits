<%
   // Use a custom header to pass back the session id
   response.setHeader("X-TestJSessionID", session.getId());
   
   int count = 1;
   Integer attr = (Integer) session.getAttribute("Count");
   if (attr != null)
   {
      count = attr.intValue();
      count++;
   }
   session.setAttribute("Count", new Integer(count));
   // Use a custom header to pass back the count
   response.setHeader("X-TestRequestCount", Integer.toString(count));
   
   // Last, encode a url to ensure that is working
%>
<%= response.encodeURL("accessSession.jsp") %>