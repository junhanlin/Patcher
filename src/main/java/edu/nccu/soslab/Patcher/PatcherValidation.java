package edu.nccu.soslab.Patcher;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;

import edu.nccu.mis.Service.Request;
import edu.nccu.mis.Service.Validation;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProject;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProject.Project;

public class PatcherValidation extends Validation
{
	public PatcherValidation(HttpServletRequest httpRequest)
	{
		super(httpRequest);
		// TODO Auto-generated constructor stub
	}

	private static String hashKey = "Patcher@Soslab";

	public static String getUserPassHash(String pass)
	{
		try
		{
			return Utility.hash(pass + hashKey, "md5");
		} catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public boolean validateLogin()
	{

		return (httpRequest.getSession().getAttribute("userName") != null) 
				&& (httpRequest.getSession().getAttribute("userId")!=null);
	}
	public boolean validateProject(int projectId)
	{
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		// int insertedInputId = -1;
		boolean result = false;

		try
		{

			conn = Utility.getSqlConn();
			String insertSql = "SELECT tblProject.projectId FROM tblProject WHERE tblProject.userId=? AND tblProject.projectId=?;";
			
			selectStat = conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			
			selectStat.setInt(p++, Utility.getCurrUserId(httpRequest));
			selectStat.setInt(p++,projectId);
			rs= selectStat.executeQuery();
			if(rs.next())
			{
				result=true;
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
}
