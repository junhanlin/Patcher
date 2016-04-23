package edu.nccu.mis.Service;

import java.lang.reflect.Constructor;

public class Request
{
	public Request()
	{
		
	}
	
	protected String validateContent()
	{
		return null;
	}
	
	public static Constructor<Request> getDefaultConstructor()
	{
		try
		{
			return Request.class.getConstructor();
		} 
		catch (NoSuchMethodException | SecurityException e)
		{
			
			e.printStackTrace();
		}
		return null;
	}
	
}
