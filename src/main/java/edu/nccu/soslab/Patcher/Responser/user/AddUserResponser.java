package edu.nccu.soslab.Patcher.Responser.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.user.ReqAddUser;
import edu.nccu.soslab.Patcher.Json.user.ResAddUser;

public class AddUserResponser extends JsonResponser<ReqAddUser,ResAddUser>
{

	

	public AddUserResponser(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Class<ReqAddUser> classOfRequest,
			Class<ResAddUser> classOfResponse)
	{
		super(httpRequest, httpResponse, classOfRequest, classOfResponse);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processRequest()
	{
		if(response.isSuccess=insertUser())
		{
			this.addResponseMsg("Successfully add user: "+ request.userName);
		}
		
	}
	private boolean insertUser()
	{
		Connection conn = null;

		PreparedStatement insertStat = null;
		ResultSet rs = null;
		boolean result = false;
		try
		{

			conn = Utility.getSqlConn();
			String insertSql = "INSERT INTO tblUser (email,userName,userPass) VALUES (?,?,?);";

			insertStat = conn.prepareStatement(insertSql);
			int p = 1;
			insertStat.setString(p++, request.email);
			insertStat.setString(p++, request.userName);
			insertStat.setString(p++,
					PatcherValidation.getUserPassHash(request.userPass));

			int affectedRows = insertStat.executeUpdate();
			result = affectedRows>0;
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

				if (insertStat != null)
				{
					insertStat.close();
					insertStat = null;
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
		if(request.email==null ||  request.email.isEmpty())
		{
			return "Email Is Required";
		}
		if(isEmailExist())
		{
			return "Email Already Exist";
		}
		if(request.userName==null ||  request.userName.isEmpty())
		{
			return "User Name Is Required";
		}
		if(isUserNameExist())
		{
			return "User Name Already Exist";
		}
		if(request.userPass==null ||  request.userPass.isEmpty())
		{
			return "Password Is Required";
		}
		if(request.userConfirmPass==null ||  !request.userPass.equals(request.userConfirmPass))
		{
			return "Passwords Are Inconsistent";
		}
		
		return null;
	}
	
	private boolean isEmailExist()
	{
		Connection conn = null;

		PreparedStatement insertStat = null;
		ResultSet rs = null;
		boolean result = false;
		try
		{

			conn = Utility.getSqlConn();
			String insertSql = "SELECT * FROM tblUser WHERE email=?";

			insertStat = conn.prepareStatement(insertSql);
			int p = 1;
			insertStat.setString(p++, request.email);
			
			rs = insertStat.executeQuery();
			if(rs.next())
			{
				result = true;
				
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

				if (insertStat != null)
				{
					insertStat.close();
					insertStat = null;
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
	private boolean isUserNameExist()
	{
		Connection conn = null;

		PreparedStatement insertStat = null;
		ResultSet rs = null;
		boolean result = false;
		try
		{

			conn = Utility.getSqlConn();
			String insertSql = "SELECT * FROM tblUser WHERE userName=?";

			insertStat = conn.prepareStatement(insertSql);
			int p = 1;
			insertStat.setString(p++, request.userName);
			
			rs = insertStat.executeQuery();
			if(rs.next())
			{
				result = true;
				
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

				if (insertStat != null)
				{
					insertStat.close();
					insertStat = null;
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
