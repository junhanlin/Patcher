package edu.nccu.soslab.Patcher.Json.Project;

import java.util.ArrayList;
import java.util.List;

import edu.nccu.mis.Service.Response;
import edu.nccu.soslab.Patcher.Json.File.PatcherFile;
import edu.nccu.soslab.Patcher.Json.Sink.PatcherSink;
import edu.nccu.soslab.Patcher.Json.TaintTask.PatcherTaintTask;
import edu.nccu.soslab.Patcher.Json.auto.PatcherAuto;
import edu.nccu.soslab.Patcher.Json.input.PatcherInput;

public class ResGetSinkDetail extends Response
{
	public int projectId;
	public PatcherSink sink;
	public PatcherTaintTask taintTask;
	public PatcherFile file;
	public List<ResPatcherInput> inputs;
	public PatcherAuto blackAuto;
	public PatcherAuto whiteAuto;
	public String depGraphPlainTxt;
	
	
	public class ResPatcherInput extends PatcherInput
	{
		public String patchFunct;
	}
	public ResGetSinkDetail()
	{
		taintTask = new PatcherTaintTask();
		file = new PatcherFile();
		sink = new PatcherSink();
		inputs = new ArrayList<>();
		
		
	}


}
