package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.soslab.Patcher.Json.Project.ReqGetProjectDetail;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProjectDetail;
import edu.nccu.soslab.Patcher.Responser.Project.GetProjectDetailResponser;

/**
 * Servlet implementation class GetProjectDetail
 */
@WebServlet("/GetProjectDetail")
public class GetProjectDetail extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetProjectDetail() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		GetProjectDetailResponser responser = new GetProjectDetailResponser(request, response, ReqGetProjectDetail.class, ResGetProjectDetail.class);
		responser.complete();
	}

}
