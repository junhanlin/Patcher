package edu.nccu.soslab.Patcher.Json.Project;

import java.util.ArrayList;
import java.util.List;

import edu.nccu.mis.Service.Response;
import edu.nccu.soslab.Patcher.Json.File.PatcherFile;
import edu.nccu.soslab.Patcher.Json.Sink.PatcherSink;
import edu.nccu.soslab.Patcher.Json.TaintTask.PatcherTaintTask;


public class ResGetProjectDetail extends Response
{
	
	public String projectName;
	public String projectDesc;
	private int fileNodeIdCounter;
	public int numOfTotalTask;
	public int numOfUnassignedTask;
	public int numOfUntaintedTask;
	
	public int numOfParseTask;
	
	public int numOfParseFailTask;
	
	public int numOfTaintTask;

	public int numOfTaintCompleteTask;
	
	public int numOfTaintFailTask;
	
	
	public int numOfTotalSinks;
	public int numOfUnsanitSinks;
	public int numOfForwardSinks;
	public int numOfForwardFailSinks;
	public int numOfBackwardSinks;
	public int numOfBackwardFailSinks;
	public int numOfPatchSinks;
	public int numOfPatchFailSinks;
	public int numOfSanitCompleteSinks;
	
	public FileNode fileRootNode;
	
	public class FileNode
	{
		private int nodeId;
		public boolean isDir;
		
		public ResTaintTask taintTask;
		public PatcherFile file;
		public String nodeName;
		public String nodePath;
		public FileNode parent;
		public List<FileNode> children;
		
		public FileNode()
		{
			file = new PatcherFile();
			taintTask = new ResTaintTask();
			this.nodeId=++fileNodeIdCounter;
			children = new ArrayList<>();
		}
		
	}
	
	
	public class ResTaintTask extends PatcherTaintTask
	{
		
		public String statusName;
		public List<ResSink> sinks;
		public ResTaintTask()
		{
			super();
			sinks= new ArrayList<>();
			
		}
	}
	public class ResSink extends PatcherSink
	{
		public String statusName;
		public ResSink()
		{
			super();
		}
	}
	public ResGetProjectDetail()
	{
		this.fileNodeIdCounter=0;
	}

}
