package de.htwg_konstanz.ebus.wholesaler.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hibernate.PersistentObjectException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSupplier;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.SupplierBOA;
import de.htwg_konstanz.ebus.wholesaler.Exceptions.InvalidXMLException;
import de.htwg_konstanz.ebus.wholesaler.Exceptions.XMLImportException;

public class ImportBean {

	private Document doc;
	
	public void importXMLFromStream(InputStream input) throws XMLImportException {
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringElementContentWhitespace(true);
			DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
				Document document = db.parse(input);
				this.doc = document;
			} catch (ParserConfigurationException e) {
				throw new XMLImportException();
			} catch (SAXException e) {
				throw new XMLImportException();
			} catch (IOException e) {
				throw new XMLImportException();
			}	
	}
	
	
	public void isValidDOM() throws InvalidXMLException{
		
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try {
			schema = factory.newSchema(new URL("http://localhost:8080/WholesalerWebDemo/wsdl/bmecat_new_catalog_1_2_simple_eps_V0.96.xsd"));
			Validator validator = schema.newValidator();	
			validator.validate(new DOMSource(this.doc));
			
		} catch (SAXException e) {
			throw new InvalidXMLException("SAX" + e.getMessage());
		} catch (MalformedURLException e) {
			throw new InvalidXMLException("MALFORMED_URL");
		} catch (IOException e) {
			throw new InvalidXMLException("IO");
		} 

	}
	
	
	public void persist() throws PersistentObjectException {
		
		Element root = this.doc.getDocumentElement();
		NodeList supplier_name = root.getElementsByTagName("SUPPLIER_NAME");
		
		String str = supplier_name.item(0).getTextContent();
		
		List<BOSupplier> sup = SupplierBOA.getInstance().findByCompanyName(str);
						
		if(sup.isEmpty()) {
			throw new PersistentObjectException("Supplier "+str+" does not exist");
		}
		
		
		NodeList products = root.getElementsByTagName("ARTICLE");
		
		Element el;
		BOProduct product;
		ProductBOA pboa = ProductBOA.getInstance();
		
		
		for (int i=0; i<products.getLength(); i++) {
			el = (Element)products.item(i);

			product = new BOProduct();
			product.setSupplier(sup.get(0));
			product.setLongDescription(el.getElementsByTagName("DESCRIPTION_LONG").item(0).getTextContent());
			product.setShortDescription(el.getElementsByTagName("DESCRIPTION_SHORT").item(0).getTextContent());
			product.setOrderNumberSupplier(el.getElementsByTagName("SUPPLIER_AID").item(0).getTextContent());
			product.setOrderNumberCustomer(el.getElementsByTagName("SUPPLIER_AID").item(0).getTextContent());
			pboa.saveOrUpdate(product);
			
			
			
			System.out.println("Artikel : " + el.getElementsByTagName("SUPPLIER_AID").item(0).getTextContent());
		}
		
		
		
	}
	
	
	public boolean importDom(org.w3c.dom.Document document) {
		
		return false;
	}
}
