package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.soslab.Patcher.Json.user.ReqLogin;
import edu.nccu.soslab.Patcher.Json.user.ResLogin;
import edu.nccu.soslab.Patcher.Responser.user.LoginResponser;

/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class Login extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	
	public Login()
	{
		super();
		// TODO Auto-generated constructor stub
	}

		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		LoginResponser responser = new LoginResponser(request, response, ReqLogin.class, ResLogin.class);
		responser.complete();
	}

}
