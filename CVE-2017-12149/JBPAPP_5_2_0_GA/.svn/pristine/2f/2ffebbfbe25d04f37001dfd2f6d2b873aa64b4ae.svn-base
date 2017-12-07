<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<jsp:useBean id="threadDump"
             class="org.jboss.varia.threaddump.ThreadDumpBean"
             scope="request"/>

<html>
<body>
<h2>Thread Summary</h2>
<table cellpadding="5" cellspacing="5">
  <tr>
    <th>Thread</th>
    <th>State</th>
    <th>Priority</th>
    <th>Daemon</th>
  </tr>
  <c:forEach items="${threadDump.threads}" var="thr">
    <tr>
      <td><c:out value='<a href="#${thr.id}">${thr.name}</a>' escapeXml="false"/></td>
      <td><c:out value="${thr.state}"/></td>
      <td><c:out value="${thr.priority}"/></td>
      <td><c:out value="${thr.daemon}"/></td>
    </tr>
  </c:forEach>
</table>

<h2>Thread Stack Traces</h2>
<c:forEach items="${threadDump.stackTraces}" var="trace">
  <h4><c:out value='<a name="${trace.key.id}">${trace.key}</a>' escapeXml="false"/></h4>
  <pre>
  <c:forEach items="${trace.value}" var="traceline">
      at <c:out value="${traceline}"/></c:forEach>
  </pre>
</c:forEach>

</body>
</html>
