package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.soslab.Patcher.Json.Project.ReqGetProject;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProject;
import edu.nccu.soslab.Patcher.Responser.Project.GetProjectResponser;

/**
 * Servlet implementation class GetProject
 */
@WebServlet("/GetProject")
public class GetProject extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetProject() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		GetProjectResponser responser = new GetProjectResponser(request, response, ReqGetProject.class, ResGetProject.class);
		responser.complete();
	}

}
