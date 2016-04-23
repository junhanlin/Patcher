package edu.nccu.mis.Service;



import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



public class Validation
{
	protected HttpServletRequest httpRequest;
	
    protected HttpSession getSession()
    {
    	if (httpRequest != null)
        {
            return httpRequest.getSession();
        }
        return null;
    }
    
    
	public Validation(HttpServletRequest httpRequest)
    {
        this.httpRequest = httpRequest;
        
        
    }
	
	
}
