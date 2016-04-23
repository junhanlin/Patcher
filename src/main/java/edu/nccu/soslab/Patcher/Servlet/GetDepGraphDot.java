package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.soslab.Patcher.Json.Project.ReqGetProjectReport;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProjectReport;
import edu.nccu.soslab.Patcher.Json.Sink.ReqGetDepGraphDot;
import edu.nccu.soslab.Patcher.Json.Sink.ResGetDepGraphDot;
import edu.nccu.soslab.Patcher.Responser.Project.GetProjectReportResponser;
import edu.nccu.soslab.Patcher.Responser.Sink.GetDepGraphDotResponser;

/**
 * Servlet implementation class GetDepGraphDot
 */
@WebServlet("/GetDepGraphDot")
public class GetDepGraphDot extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetDepGraphDot() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReqGetDepGraphDot reqJson = new ReqGetDepGraphDot();
		reqJson.sinkId = Integer.parseInt(request.getParameter("sinkId"));
		GetDepGraphDotResponser responser = new GetDepGraphDotResponser(request, response, reqJson, ResGetDepGraphDot.class);
		responser.complete();
	}

}
