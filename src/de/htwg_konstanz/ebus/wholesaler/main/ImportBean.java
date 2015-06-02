package de.htwg_konstanz.ebus.wholesaler.main;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.hibernate.PersistentObjectException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOCountry;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOPurchasePrice;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSalesPrice;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSupplier;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.CountryBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.PriceBOA;
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
	
	
	public int persist() throws PersistentObjectException {
		
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
				
		int _new = 0;
		
		for (int i=0; i<products.getLength(); i++) {
			el = (Element)products.item(i);
			
			String supplierNumber = el.getElementsByTagName("SUPPLIER_AID").item(0).getTextContent();
			BOProduct prod = pboa.findByOrderNumberSupplier(supplierNumber);
			
			if(prod != null) { 
				product = prod;
			} else {
				product = new BOProduct();	
			}
			
			++_new;
			
			String description_short = el.getElementsByTagName("DESCRIPTION_SHORT").item(0).getTextContent();
			String description_long = el.getElementsByTagName("DESCRIPTION_LONG").item(0).getTextContent();
			
			product.setSupplier(sup.get(0));
			product.setLongDescription(description_long);
			product.setLongDescriptionCustomer(description_long);
			product.setShortDescription(description_short);
			product.setShortDescriptionCustomer(description_short);
			product.setManufacturer(str);
			
			product.setOrderNumberSupplier(el.getElementsByTagName("SUPPLIER_AID").item(0).getTextContent());
			product.setOrderNumberCustomer(el.getElementsByTagName("SUPPLIER_AID").item(0).getTextContent());
			
			pboa.saveOrUpdate(product);
			
			this.persistPrices(product, el.getElementsByTagName("ARTICLE_PRICE"));
			
			
			System.out.println("Artikel : " + el.getElementsByTagName("SUPPLIER_AID").item(0).getTextContent());
		}
		
		return  _new;
	}
	

	
	private void persistPrices(BOProduct product, NodeList prices) {
		CountryBOA cboa = CountryBOA.getInstance();
		BOCountry country;
		
		PriceBOA pboa = PriceBOA.getInstance();
		
		BOPurchasePrice pp;
		BOSalesPrice sp;
		
		Element el;
		String priceType;
		NodeList terris;
		
		//delete all old prices
		//this.deleteAllPrices(pboa, product);
						
		for (int i=0; i<prices.getLength(); i++) {
			el = (Element)prices.item(i);
			priceType = el.getAttribute("price_type");
			
			terris = el.getElementsByTagName("TERRITORY");
			for (int k=0; k<terris.getLength(); k++) {
				 country = cboa.findCountry(terris.item(k).getTextContent());
				 pp = new BOPurchasePrice();
				 pp.setCountry(country);
				 pp.setLowerboundScaledprice(1);
				 pp.setAmount(new BigDecimal(el.getElementsByTagName("PRICE_AMOUNT").item(0).getTextContent()));
				 pp.setProduct(product);
				 pp.setTaxrate(new BigDecimal(el.getElementsByTagName("TAX").item(0).getTextContent()));
				 pp.setPricetype(priceType);
				 pboa.saveOrUpdatePurchasePrice(pp);
				 
				 sp = new BOSalesPrice();
				 sp.setCountry(country);
				 sp.setLowerboundScaledprice(1);
				 BigDecimal pr = new BigDecimal(Double.parseDouble(el.getElementsByTagName("PRICE_AMOUNT").item(0).getTextContent()) * 1.5);
				 sp.setAmount(pr);
				 sp.setProduct(product);
				 sp.setTaxrate(new BigDecimal(el.getElementsByTagName("TAX").item(0).getTextContent()));
				 sp.setPricetype(priceType);
				 pboa.saveOrUpdateSalesPrice(sp);  
			}
			
		}
		
	}
	
	private void deleteAllPrices(PriceBOA pboa, BOProduct product) {
		//delete all prices
		List<BOPurchasePrice> purchase_prices = pboa.findPurchasePrices(product);
		List<BOSalesPrice> sales_prices = pboa.findSalesPrices(product);
		
		int s = purchase_prices.size();
		for( int k = 0;k < s;k++ ) {
			pboa.deletePurchasePrice(purchase_prices.get(k));
		}
		
		s = sales_prices.size();
		for( int k = 0;k < s;k++ ) {
			pboa.deleteSalesPrice(sales_prices.get(k));
		}
		
		
	}
		
	public boolean importDom(org.w3c.dom.Document document) {
		
		return false;
	}
}
