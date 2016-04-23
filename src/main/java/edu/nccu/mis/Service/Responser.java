package edu.nccu.mis.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



public abstract class Responser<T extends Request,U extends Response>
{
	protected T request;
	protected U response;
	protected Class<T> classOfRequest;
	protected Class<U> classOfResponse;
	protected HttpServletRequest httpRequest;
	protected HttpServletResponse httpResponse;
    protected HttpSession getSession()
    {
    	if (httpRequest != null)
        {
            return httpRequest.getSession();
        }
        return null;
    }
    protected Cookie[] getCookies()
    {
    	if (httpRequest != null)
        {
            return httpRequest.getCookies();
        }
        return null;
    }
    
    
	public Responser(HttpServletRequest httpRequest,HttpServletResponse httpResponse,Class<T> classOfRequest,Class<U> classOfResponse)
    {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.classOfRequest = classOfRequest;
        this.classOfResponse = classOfResponse;
        try
		{
			this.response = classOfResponse.newInstance();
		} 
        catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    @SuppressWarnings("unchecked")
	public Responser(HttpServletRequest httpRequest,HttpServletResponse httpResponse,T request,Class<U> classOfResponse)
    {
        this(httpRequest, httpResponse,(Class<T>)request.getClass(),classOfResponse);
    	this.request = request;
    }
    public U getResponse()
    {
        
        String validateResult =null;
        if (((validateResult = validateRequest()) == null))
        {
            processRequest();
        }
        else
        {
            response.exitValue = -1;
            this.addResponseMsg(String.format("Validation Error: %1$s",validateResult));
        }
        return response;
    }
    public U getResponseWithoutValidation()
    {   
        processRequest();
        return response;
    }
    public void addResponseMsg(String msgStr)
    {
        response.responseMsg.add(msgStr);
        
    }
    public void addResponseMsg(Exception e)
    {
        this.addResponseMsg(e.toString());
        
    }
    protected abstract void processRequest();
    protected abstract String validateRequest();
}
