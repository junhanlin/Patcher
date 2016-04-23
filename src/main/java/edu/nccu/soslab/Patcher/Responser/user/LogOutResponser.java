package edu.nccu.soslab.Patcher.Responser.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;









import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Json.user.ReqLogOut;
import edu.nccu.soslab.Patcher.Json.user.ResLogOut;



public class LogOutResponser extends JsonResponser<ReqLogOut, ResLogOut>
{

	public LogOutResponser(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, Class<ReqLogOut> classOfRequest,
			Class<ResLogOut> classOfResponse)
	{
		super(httpRequest, httpResponse, classOfRequest, classOfResponse);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processRequest()
	{
		response.userId = Integer.parseInt(httpRequest.getSession()
		.getAttribute("userId").toString());
		response.userName =httpRequest.getSession()
				.getAttribute("userName").toString();
		httpRequest.getSession()
				.setAttribute("userName",null);
		httpRequest.getSession()
		.setAttribute("userId",null);
response.isSuccess=true;
		response.isSuccess=true;
	}

	@Override
	protected String validateRequest()
	{
		PatcherValidation val = new PatcherValidation(httpRequest);
		if(!val.validateLogin())
		{
			return "Hasn't Login Yet";
		}
		return null;
	}

}
