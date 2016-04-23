package edu.nccu.soslab.Patcher.Listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


import edu.nccu.soslab.Patcher.PatcherConfig;

/**
 * Application Lifecycle Listener implementation class WorkerControllerListener
 * 
 */
@WebListener
public class LoadConfigListener implements ServletContextListener,Runnable
{
	private Thread thread;
	private ServletContext context;
	
	/**
	 * Default constructor.
	 */
	public LoadConfigListener()
	{
		this.thread = new Thread(this);
		this.thread.setName("ConfigLoader");
		
		
	}
	
	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0)
	{
		this.context = arg0.getServletContext();
		this.thread.start();
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0)
	{
		
		this.thread.interrupt();
		
		
	}

	@Override
	public void run()
	{
		try
		{
			
			PatcherConfig.loadPatcherConfig(context);
			
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
