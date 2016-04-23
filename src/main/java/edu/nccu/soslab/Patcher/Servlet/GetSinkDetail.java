package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.soslab.Patcher.Json.Project.ReqGetSinkDetail;
import edu.nccu.soslab.Patcher.Json.Project.ResGetSinkDetail;
import edu.nccu.soslab.Patcher.Responser.Sink.GetSInkDetailResponser;

/**
 * Servlet implementation class GetSinkDetail
 */
@WebServlet("/GetSinkDetail")
public class GetSinkDetail extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSinkDetail() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		GetSInkDetailResponser responser = new GetSInkDetailResponser(request, response, ReqGetSinkDetail.class, ResGetSinkDetail.class);
		responser.complete();
	}

}
