package edu.nccu.soslab.Patcher.Responser.Sink;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProjectReport;
import edu.nccu.soslab.Patcher.Json.Sink.ReqGetDepGraphDot;
import edu.nccu.soslab.Patcher.Json.Sink.ResGetDepGraphDot;

public class GetDepGraphDotResponser extends JsonResponser<ReqGetDepGraphDot, ResGetDepGraphDot>
{
	
	public GetDepGraphDotResponser(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, ReqGetDepGraphDot request,
			Class<ResGetDepGraphDot> classOfResponse)
	{
		super(httpRequest, httpResponse, request, classOfResponse);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void complete()
	{
		try
		{
			getResponse();
			
			if(response.isSuccess)
			{
				
				httpResponse.setContentType("application/octet-stream");
				httpResponse.setHeader("Content-Disposition",
					"attachment; filename=\"depGraph_"+request.sinkId+".dot\"");
				OutputStream out= httpResponse.getOutputStream();
				IOUtils.write(response.depGraphDot, out);
				out.flush();
				out.close();
			}
			else 
			{
				//validation error
				httpResponse.sendRedirect("/Views/Index/Index.html");
			}
			

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	protected void processRequest()
	{
		response.isSuccess = querySink();
	}

	public boolean querySink()
	{
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		// int insertedInputId = -1;
		boolean result = false;

		try
		{

			conn = Utility.getSqlConn();
			String selectSql = "SELECT tblSink.depGraphDot FROM tblSink INNER JOIN tblTaintTask ON tblTaintTask.taintTaskId = tblSink.taintTaskId INNER JOIN tblFile ON tblFile.fileId=tblTaintTask.fileId INNER JOIN tblProject ON tblProject.projectId=tblFile.projectId INNER JOIN tblUser ON tblUser.userId=tblProject.userId WHERE tblSink.sinkId=? AND tblUser.userId=?;";
			
			selectStat = conn.prepareStatement(selectSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			selectStat.setInt(p++, request.sinkId);
			selectStat.setInt(p++, Utility.getCurrUserId(httpRequest));
			
			rs= selectStat.executeQuery();
			if(rs.next())
			{
				response.depGraphDot = rs.getString("depGraphDot");
			}
			result=true;

		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
				if (selectStat != null)
				{
					selectStat.close();
					selectStat = null;
				}
				
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	@Override
	protected String validateRequest()
	{
		PatcherValidation val = new PatcherValidation(httpRequest);
		if(!val.validateLogin())
		{
			return "Invalid Login Session";
		}
		return null;
	}
	
}
