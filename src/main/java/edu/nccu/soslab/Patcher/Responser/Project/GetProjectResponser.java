package edu.nccu.soslab.Patcher.Responser.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.Project.ReqGetProject;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProject;


public class GetProjectResponser extends JsonResponser<ReqGetProject, ResGetProject>
{
    	public GetProjectResponser(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Class<ReqGetProject> classOfRequest,
			Class<ResGetProject> classOfResponse)
	{
		super(httpRequest, httpResponse, classOfRequest, classOfResponse);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processRequest()
	{
	    queryProjects();
	}
	
	private boolean queryProjects()
	{
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		// int insertedInputId = -1;
		boolean result = false;

		try
		{

			conn = Utility.getSqlConn();
			String insertSql = "SELECT tblProject.projectId,tblProject.projectName,tblProject.projectDesc,tblStatusCode.statusCode,tblStatusCode.statusName,numOfTotalFilesInProject(tblProject.projectId) AS numOfTotalFiles, numOfTotalSinksInProject(tblProject.projectId,'SQLI') AS numOfSQLISinks, numOfTotalSinksInProject(tblProject.projectId,'XSS') AS numOfXSSSinks, numOfTotalSinksInProject(tblProject.projectId,'MFE') AS numOfMFESinks FROM tblProject INNER JOIN tblStatusCode ON tblStatusCode.statusCode = tblProject.statusCode INNER JOIN tblUser ON tblUser.userId=tblProject.userId WHERE tblUser.userId=? ORDER BY (numOfSQLISinks+numOfXSSSinks+numOfMFESinks) DESC , tblProject.projectId ASC;";
			
			selectStat = conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			
			selectStat.setInt(p++, Utility.getCurrUserId(httpRequest));
			
			rs= selectStat.executeQuery();
			while(rs.next())
			{
				ResGetProject.Project project = response.new Project();
				project.projectId=rs.getInt("projectId");
				project.projectName=rs.getString("projectName");
				project.projectDesc = rs.getString("projectDesc");
				project.statusCode=rs.getInt("statusCode");
				project.statusName=rs.getString("statusName");
				project.numOfTotalFiles=rs.getInt("numOfTotalFiles");
				project.numOfTotalSinks=rs.getInt("numOfSQLISinks") + rs.getInt("numOfXSSSinks") +rs.getInt("numOfMFESinks");
				
				project.sinkNum.put("SQLI", rs.getInt("numOfSQLISinks"));
				project.sinkNum.put("XSS", rs.getInt("numOfXSSSinks"));
				project.sinkNum.put("MFE", rs.getInt("numOfMFESinks"));
				
				project.atkPatterns.put("SQLI", StringEscapeUtils.escapeHtml4(getAtkPattern(conn, project.projectId, "SQLI")));
				project.atkPatterns.put("XSS", StringEscapeUtils.escapeHtml4(getAtkPattern(conn, project.projectId, "XSS")));
				project.atkPatterns.put("MFE", StringEscapeUtils.escapeHtml4(getAtkPattern(conn, project.projectId, "MFE")));
				
				project.taintTypes.put("SQLI",  project.atkPatterns.get("SQLI")!=null);
				project.taintTypes.put("XSS", project.atkPatterns.get("XSS")!=null);
				project.taintTypes.put("MFE", project.atkPatterns.get("MFE")!=null);
				
				response.projects.add(project);
				
				
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
	private String getAtkPattern(Connection conn,int projectId,String taintType)
	{
		PreparedStatement selectStat = null;
		ResultSet rs = null;
		String result = null;

		try
		{

			
			String insertSql = "SELECT atkPattern FROM tblTaintTask INNER JOIN tblFile ON tblFile.fileId = tblTaintTask.fileId WHERE tblFile.projectId = ? AND tblTaintTask.taintType=?;";
			selectStat = conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			
			selectStat.setInt(p++, projectId);
			selectStat.setString(p++,taintType);
			
			rs= selectStat.executeQuery();
			if(rs.next())
			{
				result=rs.getString("atkPattern");
			}

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
