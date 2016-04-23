package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.soslab.Patcher.Json.Project.ReqGetOptPatch;
import edu.nccu.soslab.Patcher.Json.Project.ResGetOptPatch;
import edu.nccu.soslab.Patcher.Responser.Project.GetOptPatchResponser;

/**
 * Servlet implementation class GetOptPatch
 */
@WebServlet("/GetOptPatch")
public class GetOptPatch extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetOptPatch() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReqGetOptPatch reqJson = new ReqGetOptPatch();
		reqJson.projectId = Integer.parseInt(request.getParameter("projectId"));
		GetOptPatchResponser responser = new GetOptPatchResponser(request, response, reqJson, ResGetOptPatch.class);
		responser.complete();
	}

}
