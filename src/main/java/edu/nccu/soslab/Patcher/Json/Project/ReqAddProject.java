package edu.nccu.soslab.Patcher.Json.Project;

import java.io.InputStream;

import edu.nccu.mis.Service.Request;

public class ReqAddProject extends Request
{
	public String projectName;
	public String projectDesc;
	public InputStream projectFile;
	public String projectFileName;
	public ReqAddProject()
	{
		// TODO Auto-generated constructor stub
	}

}
