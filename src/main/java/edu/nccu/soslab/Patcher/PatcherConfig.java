package edu.nccu.soslab.Patcher;



import java.io.File;
import java.io.InputStream;

import javax.servlet.ServletContext;





import com.google.gson.Gson;



public class PatcherConfig
{
	public static PatcherConfig config;
	public static final String SERVLET_TMP_DIR = "javax.servlet.context.tempdir";
	private static boolean isConfigLoaded=false;
	public String connStr;
	public String patcherHomePath;
	public String optPatchPHPVirPath;
	public String optPatchJarVirPath;
	
	
	
	public String getProjectDirPath()
	{
		return new File(patcherHomePath,"projects").getAbsolutePath();
	}
	public String getOptPatchPHPPath()
	{
		return Utility.getRealPath(optPatchPHPVirPath);
	}
	public String getOptPatchJarPath()
	{
		return Utility.getRealPath(optPatchJarVirPath);
	}
	public String getTmpDirPath()
	{
		return ((File) Utility.context.getAttribute(SERVLET_TMP_DIR)).getAbsolutePath();
	}
	public static void loadPatcherConfig(ServletContext context)
	{
		try
		{
			Utility.context = context;
			System.out.println("Configuring Patcher...");
			System.out.println("Real path of WEB-INF: "+Utility.getRealPath("/WEB-INF/"));
		
			InputStream configIn = context.getResourceAsStream("/WEB-INF/patcher.config");
			Gson gson = new Gson();
			String patcherConfigStr=null;
			
			patcherConfigStr = new String(Utility.getBytes(configIn),"utf-8");
			System.out.println("\n"+patcherConfigStr);
			PatcherConfig.config = gson.fromJson(patcherConfigStr, PatcherConfig.class);
			
		  
		    System.out.println("Configuring Patcher Finish");
			isConfigLoaded=true;
		    
		   
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
	}
	public static boolean isConfigLoaded()
	{
		return isConfigLoaded;
	}
	
	
	
}
