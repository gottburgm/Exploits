<html>
<head>
   <title>Web1 (X=true, Y=false)</title>
</head>
<body>
   <h1>Web1 (X=true, Y=false)</h1>
   Is user in role X ? = <%= request.isUserInRole("X") %>(true)<br/>
   Is user in role Y ? = <%= request.isUserInRole("Y") %>(false)<br/>
   Is user in role Z ? = <%= request.isUserInRole("Z") %>(true)<br/>
   <%
      response.addHeader("X-isUserInRole-X", ""+request.isUserInRole("X"));
      response.addHeader("X-isUserInRole-Y", ""+request.isUserInRole("Y"));
      response.addHeader("X-isUserInRole-Z", ""+request.isUserInRole("Z"));
   %>
</body>
</html>
