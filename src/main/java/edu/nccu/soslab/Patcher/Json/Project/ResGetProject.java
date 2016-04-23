package edu.nccu.soslab.Patcher.Json.Project;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.nccu.mis.Service.Response;

public class ResGetProject extends Response
{
	
	public List<Project> projects;
	public ResGetProject()
	{
		this.projects = new ArrayList<>();
		
	}
	
	public class Project
	{
	
		public int projectId;
		public String projectName;
		public String projectDesc;
		public int statusCode;
		public String statusName;
		public int numOfTotalFiles;
		public int numOfTotalSinks;
		public Map<String,Boolean> taintTypes;
		public Map<String, String> atkPatterns;
		public Map<String, Integer> sinkNum;
		public Project()
		{
			this.taintTypes = new HashMap<>();
			this.atkPatterns = new HashMap<String, String>();
			this.sinkNum = new HashMap<>();
		}
	}
	
	

}
