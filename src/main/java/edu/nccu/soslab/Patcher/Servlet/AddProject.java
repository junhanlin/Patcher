package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.Project.ReqAddProject;
import edu.nccu.soslab.Patcher.Json.Project.ResAddProject;
import edu.nccu.soslab.Patcher.Responser.Project.AddProjectResponser;

/**
 * Servlet implementation class AddProject
 */
@WebServlet("/AddProject")
@MultipartConfig(fileSizeThreshold=1024*1024*200,    // 200 MB
maxFileSize=1024*1024*200,          // 200 MB
maxRequestSize=1024*1024*300)      // 300 MB
public class AddProject extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AddProject()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			/* request */
			PatcherValidation val = new PatcherValidation(request);
			if (val.validateLogin())// valid login
			{
				// step1-text,Insert Sql
				ReqAddProject reqAddProject = new ReqAddProject();
				
				reqAddProject.projectName = request
						.getParameter("txtAddProjectName");
				
				reqAddProject.projectDesc = request
						.getParameter("txtAddProjectDesc");
				
				Part projectFilePart = request.getPart("fileAddProjectFile");
				
				reqAddProject.projectFile = projectFilePart.getInputStream();
				reqAddProject.projectFileName = Utility.getFileName(projectFilePart);

				AddProjectResponser responser = new AddProjectResponser(request,
						response, reqAddProject, ResAddProject.class);
				ResAddProject resJson = responser.getResponseWithoutValidation();
				
				/* reponse */
				response.setContentType("text/html");
				response.sendRedirect("Views/Project/Project.html?addedProjectState="
						+ resJson.isSuccess + "&addedProjectName="
						+ reqAddProject.projectName);

			} else
			{
				response.setContentType("text/html");
				response.sendRedirect("Views/Index/Index.html");
				System.out.println("Invalid request.");
			}
		} catch (Exception e)
		{

			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
					"Please check format or required feilds!");
		}
	}

}
