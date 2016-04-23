package edu.nccu.soslab.Patcher.Json.user;

import edu.nccu.mis.Service.Response;

public class ResLogOut extends Response
{
	public boolean isSuccess;
	public String userName;
	public int userId;
	
	public ResLogOut()
	{
		isSuccess=false;
	}
}
