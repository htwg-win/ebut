<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml">
	<xsl:output method="html" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" 
   doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" indent="yes"/>
	
	<xsl:template match="/">
		<html>
			<head>
				<meta http-equiv="Content-Type" content="text/html;charset=iso-8859-1" />
				<title><xsl:value-of select="/BMECAT/HEADER/CATALOG/CATALOG_NAME" /></title>
			</head>
			<body>
				<h1><xsl:value-of select="/BMECAT/HEADER/CATALOG/CATALOG_NAME" /></h1>
				<p>SUPPLIER: <xsl:value-of select="/BMECAT/HEADER/SUPPLIER/SUPPLIER_NAME" /></p>
				<p>CATALOG: <xsl:value-of select="/BMECAT/HEADER/CATALOG/CATALOG_ID" /> | <xsl:value-of select="/BMECAT/HEADER/CATALOG/CATALOG_VERSION" /> | <xsl:value-of select="/BMECAT/HEADER/CATALOG/LANGUAGE" /></p>

				<table border="1">
					<tr><th>Order No.</th><th>Title</th><th>Description</th><th>Price</th><th>Tax</th></tr>
					<xsl:apply-templates select="/BMECAT/T_NEW_CATALOG/ARTICLE"/>
				</table>

			</body>

		</html>
	</xsl:template>

	<xsl:template match="/BMECAT/T_NEW_CATALOG/ARTICLE">
		<tr>
			<td><xsl:value-of select="SUPPLIER_AID" /></td>
			<td><xsl:value-of select="ARTICLE_DETAILS/DESCRIPTION_SHORT" /></td>
			<td><xsl:value-of select="ARTICLE_DETAILS/DESCRIPTION_LONG" /></td>
			<td>
				<xsl:value-of select="ARTICLE_PRICE_DETAILS/ARTICLE_PRICE/PRICE_AMOUNT"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="ARTICLE_PRICE_DETAILS/ARTICLE_PRICE/PRICE_CURRENCY"/>
			</td>
			<td><xsl:value-of select="ARTICLE_PRICE_DETAILS/ARTICLE_PRICE/TAX"/></td>
		</tr>
	</xsl:template>

</xsl:stylesheet>
