package de.htwg_konstanz.ebus.wholesaler.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.OutputKeys;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSalesPrice;

public class ExportBean {

	// parse productList to XML (DOM)
	public static Document toXML(List<BOProduct> productList) throws ParserConfigurationException {

		// DocumentBuilder
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();

		// ROOT element
		Element root = doc.createElement("BMECAT");
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("version", "1.2");
		
		// HEADER
		Element header = doc.createElement("HEADER");
		Element catalog = doc.createElement("CATALOG");
		Element language = (Element) doc.createElement("LANGUAGE").appendChild(doc.createTextNode("eng")).getParentNode();
		Element catalog_id = (Element) doc.createElement("CATALOG_ID").appendChild(doc.createTextNode("2015-EBUT-XX01")).getParentNode();
		Element catalog_version = (Element) doc.createElement("CATALOG_VERSION").appendChild(doc.createTextNode("1.0")).getParentNode();
		Element catalog_name = (Element) doc.createElement("CATALOG_NAME").appendChild(doc.createTextNode("ebut catalog 2015")).getParentNode();
		Element supplier = doc.createElement("SUPPLIER");
		Element supplier_name = (Element) doc.createElement("SUPPLIER_NAME").appendChild(doc.createTextNode("Ebut JD&JD")).getParentNode();
		
		catalog.appendChild(language);
		catalog.appendChild(catalog_id);
		catalog.appendChild(catalog_version);
		catalog.appendChild(catalog_name);
		
		supplier.appendChild(supplier_name);
		
		header.appendChild(catalog);
		header.appendChild(supplier);

		// T_NEW_CATALOG
		Element t_new_catalog = doc.createElement("T_NEW_CATALOG");
		Element article;
		Element supplier_aid;
		Element article_details;
		Element article_order_deatils;
		Element order_unit;
		Element article_price_details;
		Element price;
		Attr price_type;

		BOProduct p;
		List<BOSalesPrice> prices;
		BOSalesPrice curr_price;

		int size = productList.size();
		
		// iterate over ARTICLES
		for (int i = 0; i < size; ++i) {
			p = productList.get(i);
			article = doc.createElement("ARTICLE");

			// set SUPPLIER_AID
			supplier_aid = doc.createElement("SUPPLIER_AID");
			supplier_aid.appendChild(doc.createTextNode(p.getOrderNumberCustomer()));
			article.appendChild(supplier_aid);
			
			// set ARTICLE_DETAILS
			article_details = doc.createElement("ARTICLE_DETAILS");
			article_details.appendChild(doc.createElement("DESCRIPTION_SHORT").appendChild(doc.createTextNode(p.getShortDescriptionCustomer())).getParentNode());
			article_details.appendChild(doc.createElement("DESCRIPTION_LONG").appendChild(doc.createTextNode(p.getLongDescriptionCustomer())).getParentNode());
			article.appendChild(article_details);

			// set ARTICLE_ORDER_DETAILS
			order_unit = (Element) doc.createElement("ORDER_UNIT").appendChild(doc.createTextNode("PK")).getParentNode();
			article_order_deatils = (Element) doc.createElement("ARTICLE_ORDER_DETAILS").appendChild(order_unit).getParentNode();
			article.appendChild(article_order_deatils);

			// set ARTICLE_PRICE_DETAILS
			article_price_details = doc.createElement("ARTICLE_PRICE_DETAILS");
			article.appendChild(article_price_details);

			// iterate over ARTICLE_PRICE
			prices = p.getSalesPrices();
			for (int k = 0; k < prices.size(); ++k) {
				curr_price = prices.get(k);
				price = doc.createElement("ARTICLE_PRICE");

				price.appendChild(doc.createElement("PRICE_AMOUNT").appendChild(doc.createTextNode(curr_price.getAmount().toString())).getParentNode());
				price.appendChild(doc.createElement("PRICE_CURRENCY").appendChild(doc.createTextNode(curr_price.getCountry().getCurrency().getCode())).getParentNode());
				price.appendChild(doc.createElement("TAX").appendChild(doc.createTextNode(curr_price.getTaxrate().toString())).getParentNode());
				price.appendChild(doc.createElement("TERRITORY").appendChild(doc.createTextNode(curr_price.getCountry().getIsocode())).getParentNode());

				price_type = doc.createAttribute("price_type");
				price_type.setValue("net_list");

				price.setAttributeNode(price_type);
				article_price_details.appendChild(price);

			}

			t_new_catalog.appendChild(article);

		}
		
		doc.appendChild(root);
		root.appendChild(header);
		root.appendChild(t_new_catalog);

		return doc;
	}

	// transform DOM doc to PrintWiter (download XML)
	public static void doc2Writer(Document doc, PrintWriter writer) throws TransformerException, IOException {
		
		// Transformer
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");

		StringWriter str = new StringWriter();
		StreamResult res = new StreamResult(str);

		DOMSource source = new DOMSource(doc);

		transformer.transform(source, res);
		writer.print(str.toString());

	}
	
	// transform DOM doc to XHTML
	public static void doc2XHTML(Document doc, PrintWriter writer) throws TransformerException{
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(new StreamSource("http://localhost:8080/WholesalerWebDemo/bmecat2xhtml.xsl"));
		
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		
		StringWriter str = new StringWriter();
		StreamResult res = new StreamResult(str);

		DOMSource source = new DOMSource(doc);

		transformer.transform(source, res);
		writer.print(str.toString());
	}

}
