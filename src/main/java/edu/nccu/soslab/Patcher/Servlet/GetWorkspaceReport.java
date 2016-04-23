package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.soslab.Patcher.Json.Project.ReqGetProjectReport;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProjectReport;
import edu.nccu.soslab.Patcher.Responser.Project.GetProjectReportResponser;

/**
 * Servlet implementation class GetWorkspaceReport
 */
@WebServlet("/GetWorkspaceReport")
public class GetWorkspaceReport extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetWorkspaceReport() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReqGetProjectReport reqJson = new ReqGetProjectReport();
		GetProjectReportResponser responser = new GetProjectReportResponser(request, response, reqJson, ResGetProjectReport.class);
		responser.complete();
	}

}
