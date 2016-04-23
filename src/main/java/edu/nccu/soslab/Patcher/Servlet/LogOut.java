package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.soslab.Patcher.Json.user.ReqLogOut;
import edu.nccu.soslab.Patcher.Json.user.ResLogOut;
import edu.nccu.soslab.Patcher.Responser.user.LogOutResponser;

/**
 * Servlet implementation class LogOut
 */
@WebServlet("/LogOut")
public class LogOut extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	public LogOut()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		LogOutResponser responser = new LogOutResponser(request,response,ReqLogOut.class,ResLogOut.class);
		responser.complete();
	}

}
