package de.htwg_konstanz.ebus.wholesaler.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.wholesaler.demo.ControllerServlet;
import de.htwg_konstanz.ebus.wholesaler.demo.IAction;
import de.htwg_konstanz.ebus.wholesaler.demo.util.Constants;
import de.htwg_konstanz.ebus.wholesaler.main.ExportBean;

/**
 * The ImportAction processes an import request.
 * <p>
 *
 * @author jd
 */
public class ExportAction implements IAction {

	private static final String PARAM_PRODUCT_LIST = "productList";

	public ExportAction() {
		super();
	}

	/**
	 * The execute method is automatically called by the dispatching sequence of
	 * the {@link ControllerServlet}.
	 * 
	 * @param request
	 *            the HttpServletRequest-Object provided by the servlet engine
	 * @param response
	 *            the HttpServletResponse-Object provided by the servlet engine
	 * @param errorList
	 *            a Stringlist for possible error messages occured in the
	 *            corresponding action
	 * @return the redirection URL
	 */
	public String execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> errorList) {

		// get search query
		String query = request.getParameter("q");
		// get export format
		String mode = request.getParameter("exportFormat");

		List<BOProduct> productList;

		// SEARCH short description
		if (query != null && !query.isEmpty()) {
			productList = ProductBOA.getInstance().findByShortdescription("%" + query + "%");
			request.getSession(true).setAttribute(PARAM_PRODUCT_LIST, productList);
			request.getSession(true).setAttribute("query", query.toString());

		} else {
			// empty search query
			request.getSession(true).setAttribute("query", "");
			productList = ProductBOA.getInstance().findAll();
			request.getSession(true).setAttribute(PARAM_PRODUCT_LIST, productList);
		}
		
		//list size
		request.getSession(true).setAttribute("listSize", productList.size());


		// EXPORT BMECAT
		if (mode != null && mode.equals("bmecat")) {

			try {
				// generate XML
				Document doc = ExportBean.toXML(productList);

				// set Header and Content Type
				response.setContentType("application/octet-stream");
				response.setHeader("Content-Transfer-Encoding", "binary");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + "export.xml\"");

				// download XML-file
				ExportBean.doc2Writer(doc, response.getWriter());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		// EXPORT XHTML
		if (mode != null && mode.equals("xhtml")) {
			
			try {
				// generate XML
				Document doc = ExportBean.toXML(productList);
				
				// set Header and Content Type
				response.setContentType("application/octet-stream");
				response.setHeader("Content-Transfer-Encoding", "binary");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + "export.html\"");
				
				// download xhtml
				ExportBean.doc2XHTML(doc, response.getWriter());
				
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		return "export.jsp";
	}

	/**
	 * Each action itself decides if it is responsible to process the
	 * corrensponding request or not. This means that the
	 * {@link ControllerServlet} will ask each action by calling this method if
	 * it is able to process the incoming action request, or not.
	 * 
	 * @param actionName
	 *            the name of the incoming action which should be processed
	 * @return true if the action is responsible, else false
	 */
	public boolean accepts(String actionName) {
		return actionName.equalsIgnoreCase(Constants.ACTION_SHOW_EXPORT);
	}
}
