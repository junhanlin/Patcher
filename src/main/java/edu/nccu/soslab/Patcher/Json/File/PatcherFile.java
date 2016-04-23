package edu.nccu.soslab.Patcher.Json.File;

import edu.nccu.soslab.Patcher.PatcherConfig;


public class PatcherFile
{
	public int fileId;
	public int projectId;
	public String fileName;
	public String path;
	public String content;
	
	
	public PatcherFile()
	{
		// TODO Auto-generated constructor stub
	}
	public java.io.File getRealFile()
	{
		return new java.io.File(PatcherConfig.config.getProjectDirPath()+"/"+projectId+path,fileName);
	}
	public java.io.File getCTraceFile()
	{
		java.io.File result = new java.io.File(getRealFile().getParent(),"c_trace/"+fileName+".c");
		if(!result.getParentFile().exists())
		{
			result.getParentFile().mkdirs();
		}
		return result;
	}
	

}
