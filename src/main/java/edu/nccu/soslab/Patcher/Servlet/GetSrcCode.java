package edu.nccu.soslab.Patcher.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.soslab.Patcher.Json.File.ReqGetSrcCode;
import edu.nccu.soslab.Patcher.Json.File.ResGetSrcCode;
import edu.nccu.soslab.Patcher.Responser.File.GetSrcCodeResponser;

/**
 * Servlet implementation class GetSrcCode
 */
@WebServlet("/GetSrcCode")
public class GetSrcCode extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSrcCode() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		GetSrcCodeResponser responser = new GetSrcCodeResponser(request, response, ReqGetSrcCode.class, ResGetSrcCode.class);
		responser.complete();
	}

}
