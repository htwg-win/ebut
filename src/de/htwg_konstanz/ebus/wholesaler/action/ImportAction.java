package de.htwg_konstanz.ebus.wholesaler.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hibernate.PersistentObjectException;

import de.htwg_konstanz.ebus.wholesaler.Exceptions.InvalidXMLException;
import de.htwg_konstanz.ebus.wholesaler.Exceptions.XMLImportException;
import de.htwg_konstanz.ebus.wholesaler.demo.ControllerServlet;
import de.htwg_konstanz.ebus.wholesaler.demo.IAction;
import de.htwg_konstanz.ebus.wholesaler.demo.util.Constants;
import de.htwg_konstanz.ebus.wholesaler.main.ImportBean;

/**
 * The ImportAction processes an import request.
 * 
 * @author jd
 */
public class ImportAction implements IAction {
	public ImportAction() {
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

		// get the action request parameter
		String actionName = (String) request.getParameter(Constants.PARAM_NAME_ACTION);

		
		// ACTION_UPLOAD_IMPORT
		if (actionName.equalsIgnoreCase(Constants.ACTION_UPLOAD_IMPORT)) {

			ImportBean im = new ImportBean();
			
			try {
				// start file-upload process
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);

				if (isMultipart) {		
					// FileUploader
					FileItemFactory factory = new DiskFileItemFactory();
					ServletFileUpload upload = new ServletFileUpload(factory);

					List items = upload.parseRequest(request);

					Iterator iterator = items.iterator();
					FileItem item = (FileItem) iterator.next();
					if (!item.isFormField()) {
						InputStream is = item.getInputStream();

						try {
							// parse imported XML
							im.importXMLFromStream(is);
							
							// validate imported XML
							im.isValidDOM();

							// persist imported XML
							int persisted = im.persist();

							// success message
							errorList.add("Catalog import successful. " + persisted + " items imported");

						} catch (XMLImportException e) {
							errorList.add("The File you tried uploading is not a wellformed XML file.");
						} catch (InvalidXMLException e) {
							errorList.add("Invalid XML format. <br/>" + e.getMessage());
						} catch (PersistentObjectException e) {
							errorList.add("persistError :<br/> " + e.getMessage());
						}
					}

				}
			} catch (FileUploadException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		
		// @TODO: check role and logged in status
		return "import.jsp";

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
		return actionName.equalsIgnoreCase(Constants.ACTION_SHOW_IMPORT) || actionName.equalsIgnoreCase(Constants.ACTION_UPLOAD_IMPORT);
	}
}
