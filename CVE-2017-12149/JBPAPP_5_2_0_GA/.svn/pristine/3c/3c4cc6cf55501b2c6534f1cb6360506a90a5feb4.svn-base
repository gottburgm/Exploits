<html>
<head>
   <title>Web2 (X=false, Y=true)</title>
</head>
<body>
   <h1>Web2 (X=false, Y=true)</h1>
   Is user in role X ? = <%= request.isUserInRole("X") %>(false)<br/>
   Is user in role Y ? = <%= request.isUserInRole("Y") %>(true)<br/>
   Is user in role Z ? = <%= request.isUserInRole("Z") %>(true)<br/>
   <%
      response.addHeader("X-isUserInRole-X", ""+request.isUserInRole("X"));
      response.addHeader("X-isUserInRole-Y", ""+request.isUserInRole("Y"));
      response.addHeader("X-isUserInRole-Z", ""+request.isUserInRole("Z"));
   %>
</body>
</html>
