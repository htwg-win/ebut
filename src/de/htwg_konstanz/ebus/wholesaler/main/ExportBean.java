package de.htwg_konstanz.ebus.wholesaler.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSalesPrice;

public class ExportBean {

	public static Document toXML(List<BOProduct> productList)
			throws ParserConfigurationException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		
		Element root = doc.createElement("BMECAT");
		Element header = doc.createElement("HEADER");
		Element t_new_catalog = doc.createElement("T_NEW_CATALOG");
		Element article;
		Element supplier_aid;
		Element article_details;
		Element article_order_deatils;
		Element order_unit;
		Element article_price_details;
		Element price;
		Element catalog = doc.createElement("CATALOG");
		Element language = (Element)doc.createElement("LANGUAGE").appendChild(doc.createTextNode("eng")).getParentNode();
		Element catalog_id = (Element)doc.createElement("CATALOG_ID").appendChild(doc.createTextNode("2015-EBUT-XX01")).getParentNode();
		Element catalog_version = (Element)doc.createElement("CATALOG_VERSION").appendChild(doc.createTextNode("1.0")).getParentNode();;
		Element catalog_name = (Element)doc.createElement("CATALOG_NAME").appendChild(doc.createTextNode("ebut catalog 2015")).getParentNode();
		Element supplier = doc.createElement("SUPPLIER");;
		Element supplier_name = (Element)doc.createElement("SUPPLIER_NAME").appendChild(doc.createTextNode("Ebut JD&JD")).getParentNode();;
		Attr price_type;
		
		catalog.appendChild(language);
		catalog.appendChild(catalog_id);
		catalog.appendChild(catalog_version);
		catalog.appendChild(catalog_name);
		
		supplier.appendChild(supplier_name);
		header.appendChild(catalog);
		header.appendChild(supplier);

		
		root.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("version", "1.2");
		

		doc.appendChild(root);
		root.appendChild(header);
		root.appendChild(t_new_catalog);


		BOProduct p;
		List<BOSalesPrice> prices;
		BOSalesPrice curr_price;

		int size = productList.size();
		
		for (int i = 0; i < size; ++i) {
			p = productList.get(i);
			article = doc.createElement("ARTICLE");

			supplier_aid = doc.createElement("SUPPLIER_AID");
			supplier_aid.appendChild(doc.createTextNode(p
					.getOrderNumberCustomer()));
			article.appendChild(supplier_aid);

			order_unit = (Element)doc.createElement("ORDER_UNIT").appendChild(doc.createTextNode("PK")).getParentNode();
			article_order_deatils = (Element)doc.createElement("ARTICLE_ORDER_DETAILS").appendChild(order_unit).getParentNode();

			article_details = doc.createElement("ARTICLE_DETAILS");
			article_details
					.appendChild(doc
							.createElement("DESCRIPTION_SHORT")
							.appendChild(
									doc.createTextNode(p
											.getShortDescriptionCustomer()))
							.getParentNode());
			article_details.appendChild(doc
					.createElement("DESCRIPTION_LONG")
					.appendChild(
							doc.createTextNode(p.getLongDescriptionCustomer()))
					.getParentNode());

			article_price_details = doc.createElement("ARTICLE_PRICE_DETAILS");

			article.appendChild(article_details);
			article.appendChild(article_order_deatils);
			article.appendChild(article_price_details);


			prices = p.getSalesPrices();

			for (int k = 0; k < prices.size(); ++k) {
				curr_price = prices.get(k);
				price = doc.createElement("ARTICLE_PRICE");

				price.appendChild(doc
						.createElement("PRICE_AMOUNT")
						.appendChild(
								doc.createTextNode(curr_price.getAmount()
										.toString())).getParentNode());
				price.appendChild(doc
						.createElement("PRICE_CURRENCY")
						.appendChild(
								doc.createTextNode(curr_price.getCountry()
										.getCurrency().getCode()))
						.getParentNode());
				price.appendChild(doc
						.createElement("TAX")
						.appendChild(
								doc.createTextNode(curr_price.getTaxrate()
										.toString())).getParentNode());
				price.appendChild(doc
						.createElement("TERRITORY")
						.appendChild(
								doc.createTextNode(curr_price.getCountry()
										.getIsocode())).getParentNode());

				price_type = doc.createAttribute("price_type");
				price_type.setValue("net_list");

				price.setAttributeNode(price_type);
				article_price_details.appendChild(price);

			}

			t_new_catalog.appendChild(article);

		}

		return doc;
	}

	public static void doc2file(Document doc, String path)
			throws TransformerException {
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(path));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);

		System.out.println("File saved!");

	}

	public static void doc2Writer(Document doc, PrintWriter writer)
			throws TransformerException, IOException {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		transformer.setOutputProperty("encoding", "iso-8859-1");
		
		

		StringWriter str = new StringWriter();
		StreamResult res = new StreamResult(str);

		DOMSource source = new DOMSource(doc);
		// StreamResult result=new StreamResult(writer);

		transformer.transform(source, res);
		writer.print(str.toString());

		// StreamResult result = new StreamResult(System.out);
		// transformer.transform(source,result);

	}

}
