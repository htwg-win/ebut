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
	
	// parse InputStream to DOM and check if it is a well-formed XML
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
	
	
	// validate DOM (BMEcat)
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
	
	// persist product details
	public int persist() throws PersistentObjectException {
		
		Element root = this.doc.getDocumentElement();
			
		// get SUPPLIER NAME and check if supplier exists in DB
		NodeList supplier_name = root.getElementsByTagName("SUPPLIER_NAME");
		String supplier_name_str = supplier_name.item(0).getTextContent();
		
		List<BOSupplier> cat_supplier = SupplierBOA.getInstance().findByCompanyName(supplier_name_str);		
		if(cat_supplier.isEmpty()) {
			throw new PersistentObjectException("Supplier "+supplier_name_str+" does not exist");
		}
		
		// get all ARTICELS
		NodeList products = root.getElementsByTagName("ARTICLE");
		
		Element el;
		BOProduct product;
		ProductBOA pboa = ProductBOA.getInstance();
				
		int _new = 0;
		
		// iterate over ARTICLES
		for (int i=0; i<products.getLength(); i++) {
			
			el = (Element)products.item(i);
			
			// check if product already exists (SUPPLIER_AID + SUPPLIER_NAME)
			String supplierNumber = el.getElementsByTagName("SUPPLIER_AID").item(0).getTextContent();
			BOProduct prod = pboa.findByOrderNumberSupplier(supplierNumber);
			
			if(prod != null && cat_supplier.get(0).getSupplierNumber().equals(prod.getSupplier().getSupplierNumber())) { 
				// if product exists (same OrderNumberSupplier and supplier_id) -> update product
				product = prod;
			} else {
				// else -> create new product
				product = new BOProduct();	
			}
			
			// get short and long description
			String description_short = el.getElementsByTagName("DESCRIPTION_SHORT").item(0).getTextContent();
			String description_long = el.getElementsByTagName("DESCRIPTION_LONG").item(0).getTextContent();
			
			// set supplier, descriptions and manufacturer
			product.setSupplier(cat_supplier.get(0));
			product.setLongDescription(description_long);
			product.setLongDescriptionCustomer(description_long);
			product.setShortDescription(description_short);
			product.setShortDescriptionCustomer(description_short);
			product.setManufacturer(supplier_name_str);
			
			// set OrderNumberSupplier
			product.setOrderNumberSupplier(el.getElementsByTagName("SUPPLIER_AID").item(0).getTextContent());
			
			// generate and set OrderNumberCustomer (combination of supplier_id and OrderNumberSupplier)
			String orderNumberCustomer = cat_supplier.get(0).getSupplierNumber() + "_" + el.getElementsByTagName("SUPPLIER_AID").item(0).getTextContent();
			product.setOrderNumberCustomer(orderNumberCustomer);
			
			// saveOrUpdate
			pboa.saveOrUpdate(product);
			
			// call method persistPrices
			this.persistPrices(product, el.getElementsByTagName("ARTICLE_PRICE"));
			
			++_new;
			
		}
		
		// return number of updated 
		return  _new;
	}
	

	// persist product prices
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
		
		// iterate over prices
		for (int i=0; i<prices.getLength(); i++) {
			
			el = (Element)prices.item(i);
			
			// set price_type
			priceType = el.getAttribute("price_type");
			
			// iterate over TERRITORIES
			terris = el.getElementsByTagName("TERRITORY");
			
			for (int k=0; k< terris.getLength(); k++) {
				 country = cboa.findCountry(terris.item(k).getTextContent());
				 
				 // set purchase prices
				 pp = new BOPurchasePrice();
				 pp.setCountry(country);
				 pp.setLowerboundScaledprice(1);
				 pp.setAmount(new BigDecimal(el.getElementsByTagName("PRICE_AMOUNT").item(0).getTextContent()));
				 pp.setProduct(product);
				 pp.setTaxrate(new BigDecimal(el.getElementsByTagName("TAX").item(0).getTextContent()));
				 pp.setPricetype(priceType);
				 pboa.saveOrUpdatePurchasePrice(pp);
				 
				 // set sales prices
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
	
	// delete all prices in DB
	private void deleteAllPrices(PriceBOA pboa, BOProduct product) {
		//delete all prices
		List<BOPurchasePrice> purchase_prices = pboa.findPurchasePrices(product);
		List<BOSalesPrice> sales_prices = pboa.findSalesPrices(product);
		
		// delete all purchase prices
		int s = purchase_prices.size();
		for( int k = 0;k < s;k++ ) {
			pboa.deletePurchasePrice(purchase_prices.get(k));
		}
		
		// delete all sales prices
		s = sales_prices.size();
		for( int k = 0;k < s;k++ ) {
			pboa.deleteSalesPrice(sales_prices.get(k));
		}
				
	}

}
