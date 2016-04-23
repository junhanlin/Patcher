package edu.nccu.soslab.Patcher.Responser.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.mis.Service.Responser;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.user.ReqLogin;
import edu.nccu.soslab.Patcher.Json.user.ResLogin;


public class LoginResponser extends JsonResponser<ReqLogin,ResLogin>
{

	public LoginResponser(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Class classOfRequest, Class classOfResponse)
	{
		super(httpRequest, httpResponse, classOfRequest, classOfResponse);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processRequest()
	{
		PatcherValidation val = new PatcherValidation(httpRequest);

		if (val.validateLogin())
		{
			
			response.isValid = true;
			response.userName = httpRequest.getSession()
					.getAttribute("userName").toString();
			response.userId = Integer.parseInt(httpRequest.getSession()
					.getAttribute("userId").toString());

		} else
		{
			response.isValid = isUserExist(request.userName, request.userPassword);
			if (response.isValid)
			{
				
				
				this.httpRequest.getSession().setAttribute("userName",
						response.userName);
				this.httpRequest.getSession().setAttribute("userId",
						response.userId);
				

			} else
			{
				this.httpRequest.getSession().setAttribute("userName",
						null);
				this.httpRequest.getSession().setAttribute("userId",
						null);

			}
		}
		
	}
	
	private boolean isUserExist(String userName, String userPassword)
	{
		Connection conn = null;

		PreparedStatement selectStat = null;
		ResultSet rs = null;
		boolean result = false;
		try
		{

			conn = Utility.getSqlConn();
			String selectSql = "SELECT * FROM tblUser WHERE userName=? AND userPass=?;";

			selectStat = conn.prepareStatement(selectSql);
			int p = 1;
			selectStat.setString(p++, userName);
			selectStat.setString(p++,
					PatcherValidation.getUserPassHash(userPassword));

			rs = selectStat.executeQuery();
			if(rs.next())
			{
				result = true;
				response.userId = rs.getInt("userId");
				response.userName = rs.getString("userName");
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
	
	@Override
	protected String validateRequest()
	{
		// TODO Auto-generated method stub
		return null;
	}

	

}
