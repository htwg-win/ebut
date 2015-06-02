<%@ page session="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>eBusiness Framework Demo - Welcome</title>
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" type="text/css" href="default.css">
</head>
<body>

<%@ include file="header.jsp" %>
<%@ include file="error.jsp" %>
<%@ include file="authentication.jsp" %>
<%@ include file="navigation.jspfragment" %>

<h1>Import</h1>
<div>
	<form action="<%= response.encodeURL("controllerservlet?action="+Constants.ACTION_UPLOAD_IMPORT) %>" enctype="multipart/form-data" method="post">  
	  <input type="file" name="file" accept=".xml"> 
	  <button type="submit">Import Now!</button> 
	</form>
</div>
</body>
</html>
