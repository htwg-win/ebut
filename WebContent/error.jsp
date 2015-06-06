<%@ page session="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<br>
<c:forEach var="error" items="${sessionScope.errorList}">
<jsp:useBean id="error" type="java.lang.String" />

<% if(error.matches(".*successful.*")){%>
<b><font color="Green"><%= error %></font></b><br><br>
<% } else {%>
<b><font color="Red"><%= error %></font></b><br><br>
<% } %>


</c:forEach>


