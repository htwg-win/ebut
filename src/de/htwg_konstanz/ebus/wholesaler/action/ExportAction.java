package de.htwg_konstanz.ebus.wholesaler.action;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.htwg_konstanz.ebus.wholesaler.demo.ControllerServlet;
import de.htwg_konstanz.ebus.wholesaler.demo.IAction;
import de.htwg_konstanz.ebus.wholesaler.demo.util.Constants;

/**
* The ImportAction processes an import request.<p>
*
* @author jd
*/
public class ExportAction implements IAction
{
	public ExportAction()
	{
		super();
	}

   /**
   * The execute method is automatically called by the dispatching sequence of the {@link ControllerServlet}. 
   * 
   * @param request the HttpServletRequest-Object provided by the servlet engine
   * @param response the HttpServletResponse-Object provided by the servlet engine
   * @param errorList a Stringlist for possible error messages occured in the corresponding action
   * @return the redirection URL
   */
	public String execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> errorList)
	{
		return "export.jsp";
	}

   /**
   * Each action itself decides if it is responsible to process the corrensponding request or not.
   * This means that the {@link ControllerServlet} will ask each action by calling this method if it
   * is able to process the incoming action request, or not.
   * 
   * @param actionName the name of the incoming action which should be processed
   * @return true if the action is responsible, else false
   */
	public boolean accepts(String actionName)
	{
		return actionName.equalsIgnoreCase(Constants.ACTION_SHOW_EXPORT);
	}
}
