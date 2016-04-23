package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.soslab.Patcher.Json.Project.ReqGetOptPatch;
import edu.nccu.soslab.Patcher.Json.Project.ReqGetProjectReport;
import edu.nccu.soslab.Patcher.Json.Project.ResGetOptPatch;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProjectReport;
import edu.nccu.soslab.Patcher.Responser.Project.GetOptPatchResponser;
import edu.nccu.soslab.Patcher.Responser.Project.GetProjectReportResponser;

/**
 * Servlet implementation class GetProjectReport
 */

@WebServlet("/GetProjectReport")
public class GetProjectReport extends HttpServlet{
	private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetProjectReport() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReqGetProjectReport reqJson = new ReqGetProjectReport();
		reqJson.projectId = Integer.parseInt(request.getParameter("projectId"));
		GetProjectReportResponser responser = new GetProjectReportResponser(request, response, reqJson, ResGetProjectReport.class);
		responser.complete();
	}

	

	
	
	
    

}
