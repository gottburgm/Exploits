<html>
<title>JBoss and Spring integration</title>

<body>
<p/>
Number:
<form action="number.htm" method="POST">
   Radius: <input type="text" name="radius" value=""/>
   <%
      if (request.getAttribute("number") != null)
      {
   %>
   Random number: <%=request.getAttribute("number")%><p/>
   <%
      }
   %>
   <input type="submit" value="Submit"/>
</form>
<p/>
Word:
<form action="word.htm" method="POST">
   <%
      if (request.getAttribute("word") != null)
      {
   %>
   Random word:  <%=request.getAttribute("word")%><p/>
   <%
      }
   %>
   <input type="submit" value="Submit"/>
</form>
<p/>
Horoscope:
<form action="horoscope.htm" method="POST">
   Month: <input type="text" name="month" value=""/>
   <%
      if (request.getAttribute("horoscope") != null)
      {
   %>
   Random horoscope: <%=request.getAttribute("horoscope")%><p/>
   <%
      }
   %>
   <input type="submit" value="Submit"/>
</form>
<p/>
<a href="horoscope.htm?clear=true">Clear horoscope</a>
</body>
</html>