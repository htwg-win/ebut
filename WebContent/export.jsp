<%@ page session="true" import="de.htwg_konstanz.ebus.framework.wholesaler.api.bo.*,de.htwg_konstanz.ebus.framework.wholesaler.api.boa.*,java.math.BigDecimal,de.htwg_konstanz.ebus.framework.wholesaler.vo.util.PriceUtil" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>eBusiness Framework Demo - Export</title>
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" type="text/css" href="default.css">
</head>
<body>

<%@ include file="header.jsp" %>
<%@ include file="error.jsp" %>
<%@ include file="authentication.jsp" %>
<%@ include file="navigation.jspfragment" %>

<h1>Export</h1>
<div>
<form method="POST" action="<%= response.encodeURL("controllerservlet?action="+Constants.ACTION_SHOW_EXPORT) %>" style="display:inline-block">
	<input type="search" name="q" placeholder="What are you looking for?.." value="<%=session.getAttribute("query") %>" />
	<button type="submit">Go</button>
</form>

<div style="float:right;">
	<a href="<%= response.encodeURL("controllerservlet?action="+Constants.ACTION_SHOW_EXPORT+"&exportFormat=bmecat&q="+session.getAttribute("query")) %>">Export as BMEcat</a> | <a href="<%= response.encodeURL("controllerservlet?action="+Constants.ACTION_SHOW_EXPORT+"&exportFormat=xhtml&q="+session.getAttribute("query")) %>">Export as XHTML</a>
</div>
</div>
<div>
<% if(!session.getAttribute("listSize").equals(0)) { %>

<table class="dataTable">
<thead>
<tr>
<th><b>Order No.</b></th>
<th><b>Title</b></th>
<th><b>Description</b></th>
<th><b>Price net</b></th>
<th><b>Price gross</b></th>
<th><b>Inventory</b></th>
</tr>
</thead>
<tbody>
<c:forEach var="product" items="${sessionScope.productList}">
<jsp:useBean id="product" type="de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct" />
<tr valign="top">
<td><a href="<%= response.encodeURL("controllerservlet?action=showProductDetail&orderNumberCustomer="+product.getOrderNumberCustomer()) %>"><%= product.getOrderNumberCustomer() %></a></td>
<td><%= product.getShortDescriptionCustomer() %></td> 
<td width="400px"><%= product.getLongDescriptionCustomer() %></td> 

<% BigDecimal netPrice = product.getDefaultSalesPrice().getAmount(); %>
<% BigDecimal grossPrice = product.getDefaultSalesPrice().getAmountGross(); %>
<td align="right"><fmt:formatNumber value="<%= netPrice %>" type="currency" currencySymbol="<%= product.getDefaultSalesPrice().getCountry().getCurrency().getCode() %>"/></td>
<td align="right"><fmt:formatNumber value="<%= grossPrice %>" type="currency" currencySymbol="<%= product.getDefaultSalesPrice().getCountry().getCurrency().getCode() %>"/></td>

<% BOInventory inventory = InventoryBOA.getInstance().findByProduct(product); %>
<td align="right"><% if (inventory != null) 
{ %>
<%= inventory.getInventoryNumber() %> 
<% } %>
</td>
</tr>
</c:forEach>
</tbody>
</table>
<% } else { %>
<h3>No matches found in product database.</h3>
<% } %>
</div>
</body>
</html>
