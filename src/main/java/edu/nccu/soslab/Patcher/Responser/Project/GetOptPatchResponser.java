package edu.nccu.soslab.Patcher.Responser.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.soslab.Patcher.PatcherConfig;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.Project.ReqGetOptPatch;
import edu.nccu.soslab.Patcher.Json.Project.ResGetOptPatch;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProject;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProject.Project;

public class GetOptPatchResponser extends
		JsonResponser<ReqGetOptPatch, ResGetOptPatch>
{
	private File optPatchZipFile;
	private Map<String, List<PatchPlace>> patches = new LinkedHashMap<String, List<PatchPlace>>();
	public GetOptPatchResponser(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse,
			Class<ReqGetOptPatch> classOfRequest,
			Class<ResGetOptPatch> classOfResponse)
	{
		super(httpRequest, httpResponse, classOfRequest, classOfResponse);
		// TODO Auto-generated constructor stub
	}

	public GetOptPatchResponser(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, ReqGetOptPatch request,
			Class<ResGetOptPatch> classOfResponse)
	{
		super(httpRequest, httpResponse, request, classOfResponse);
	}

	@Override
	public void complete()
	{
		Gson gson = new Gson();
		try
		{
			ResGetOptPatch resJson = getResponse();
			String responseStr = gson.toJson(resJson);
			if(resJson.isSuccess )
			{
				
				httpResponse.setContentType("application/octet-stream");
				httpResponse.setHeader("Content-Disposition",
					"attachment; filename=\"optPatch"+request.projectId+".zip\"");
				OutputStream out = httpResponse.getOutputStream();
				FileInputStream in = new FileInputStream(optPatchZipFile);
				byte[] buffer = new byte[4096];
				int length;
				while ((length = in.read(buffer)) > 0){
				    out.write(buffer, 0, length);
				}
				in.close();
				out.flush();
				httpResponse.getOutputStream().close();
			}
			else 
			{
				//validation error
				httpResponse.sendRedirect("/Views/Index/Index.html");
			}
			

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void processRequest()
	{
		genOptPatch();
	}

	private void genOptPatch()
	{
		
		try
		{
			//gen work dir
			UUID uuid = UUID.randomUUID();
			File optPatchDir = new File(PatcherConfig.config.getProjectDirPath()
					+ "/" + request.projectId+"/optPatch_" + uuid,"optPatch");
			optPatchDir.mkdirs();
			File optPatchAutoDir = new File(optPatchDir,"whiteAutos");
			optPatchAutoDir.mkdirs();

			// copy optPatch jar and PHP
			File  optPatchJar = new File(PatcherConfig.config.getOptPatchJarPath());
			File optPatchPHP = new File(PatcherConfig.config.getOptPatchPHPPath());
			copy(optPatchJar, new File(optPatchDir,optPatchJar.getName()));
			copy(optPatchPHP, new File(optPatchDir,optPatchPHP.getName()));
			
			//write white autos into work dir
			genWhiteAutoFile(optPatchAutoDir);
			
			//write getInputVars.php into work dir
			genGetInputVarsPHPFile(optPatchDir);
			
			//write README
			genReadMeFile(optPatchDir);
			
			
			
			//gen archive zip file
			optPatchZipFile = new File(optPatchDir.getParent(),"optPatch.zip");
			Utility.createZip(optPatchDir.getAbsolutePath(),optPatchZipFile.getAbsolutePath());
			response.isSuccess=true;
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private boolean genWhiteAutoFile(File optPatchAutoDir)
	{
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		boolean result = false;

		try
		{

			conn = Utility.getSqlConn();
			String insertSql = "SELECT tblSink.sinkId,tblFile.path,tblFile.fileName FROM tblSink INNER JOIN tblAuto ON tblAuto.autoId=tblSink.whiteAutoId INNER JOIN tblTaintTask ON tblTaintTask.taintTaskId = tblSink.taintTaskId  INNER JOIN tblFile ON tblFile.fileId = tblTaintTask.fileId WHERE tblFile.projectId = ? AND tblAuto.autoDot IS NOT NULL";
			
			selectStat = conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			
			selectStat.setInt(p++, request.projectId);
			
			rs= selectStat.executeQuery();
			while(rs.next())
			{
				File fileDir = new File(PatcherConfig.config.getProjectDirPath()+"/"+request.projectId,rs.getString("path"));
				File whiteAutoFile = new File(fileDir.getAbsoluteFile()+"/_auto",rs.getString("fileName")+"_sink_"+rs.getInt("sinkId")+"_WHITE.auto");
				copy(whiteAutoFile, new File(optPatchAutoDir,"sink_"+rs.getInt("sinkId")+"_WHITE.auto"));
			}
			result=true;

		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
				if (selectStat != null)
				{
					selectStat.close();
					selectStat = null;
				}
				
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	public boolean genGetInputVarsPHPFile(File optPatchDir)
	{
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		boolean result = false;

		try
		{

			conn = Utility.getSqlConn();
			String insertSql = "SELECT tblSink.sinkId,tblFile.path,tblFile.fileName,tblInput.inputId,tblInput.inputNo,tblInput.varName,tblInput.lineNo FROM tblInput INNER JOIN tblSink ON tblSink.sinkId=tblInput.sinkId INNER JOIN tblTaintTask ON tblTaintTask.taintTaskId = tblSink.taintTaskId  INNER JOIN tblFile ON tblFile.fileId = tblTaintTask.fileId INNER JOIN tblProject ON tblProject.projectId=tblFile.projectId WHERE tblProject.projectId = ? ORDER BY tblSink.sinkId,tblInput.inputNo ASC";
			
			selectStat = conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			
			selectStat.setInt(p++, request.projectId);
			
			rs= selectStat.executeQuery();
			
			FileWriter getInputVarsPHPWriter = new FileWriter(new File(optPatchDir,"getInputVars.php"));
			getInputVarsPHPWriter.append("<?PHP\n");
			getInputVarsPHPWriter.append("function getInputVars($sinkId)\n");
			getInputVarsPHPWriter.append("{\n");
			getInputVarsPHPWriter.append("\t$result = array();\n");
			getInputVarsPHPWriter.append("\tswitch($sinkId)\n");
			getInputVarsPHPWriter.append("\t{\n");
			
			
			
			
			int currSinkId =-1;
			while(rs.next())
			{
				if(currSinkId!=rs.getInt("sinkId"))
				{
					if(currSinkId!=-1)
					{
						getInputVarsPHPWriter.append("\t\t\tbreak;\n");
					}
					getInputVarsPHPWriter.append("\t\tcase "+rs.getInt("sinkId")+":\n");
				}
				getInputVarsPHPWriter.append("\t\t\t$result[] = "+rs.getString("varName")+";\n");
				currSinkId=rs.getInt("sinkId");
				
				String filePath = rs.getString("path")+"/"+rs.getString("fileName");
				if(!patches.containsKey(filePath))
				{
				    patches.put(filePath, new ArrayList<GetOptPatchResponser.PatchPlace>());
				}
				patches.get(filePath).add(new PatchPlace(rs.getInt("lineNo"), rs.getString("varName"), "optPatch("+rs.getString("varName")+","+rs.getInt("sinkId")+","+rs.getInt("inputNo")+")"));
			}
			getInputVarsPHPWriter.append("\t\tdefault:\n");
			getInputVarsPHPWriter.append("\t}\n");
			getInputVarsPHPWriter.append("\treturn $result;\n");
			getInputVarsPHPWriter.append("}\n");
			getInputVarsPHPWriter.append("?>\n");
			getInputVarsPHPWriter.close();
			result=true;

		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
				if (selectStat != null)
				{
					selectStat.close();
					selectStat = null;
				}
				
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	private void copy(File src, File dest)
	{
		try
		{
			FileInputStream srcIn = new FileInputStream(src);
			FileOutputStream destOut = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while ((len = srcIn.read(buf)) > 0)
			{
				destOut.write(buf, 0, len);
			}
			srcIn.close();
			destOut.close();
			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void copy(InputStream srcIn, File dest)
	{
		try
		{
			
			FileOutputStream destOut = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while ((len = srcIn.read(buf)) > 0)
			{
				destOut.write(buf, 0, len);
			}
			srcIn.close();
			destOut.close();
			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void genReadMeFile(File optPatchDir)
	{
	    File readMeFile = new File(optPatchDir,"README");
	    try
	    {
		FileOutputStream fileOut = new FileOutputStream(readMeFile);
		PrintWriter writer = new PrintWriter(fileOut);
		writer.println("======================");
		writer.println("How to Patch the PHP Project");
		writer.println("======================");
		writer.println("Step 1: Copy all the directories and the files in this folder to your PHP project's root folder.");
		writer.println("Step 2: Open the 'optPatch.php' file, and change the default value of '$__PATCHER__JAVA_BIN' to your system's java executable path");
		writer.println("Step 3: Include 'optPatch.php' into the following "+patches.size()+" PHP files and apply the patches by following the given instructions:");
		writer.println("");
		writer.println("");
		for(Entry<String, List<PatchPlace>>  entry : patches.entrySet())
		{
		    writer.println("--------------------------------------------------------");
		    String filePath = entry.getKey();
		    List<PatchPlace> patchPlaces = entry.getValue();
		    writer.println("#FILE: "+filePath);
		    writer.println("");
		    int patchCount=1;
		    for(PatchPlace patchPlace :patchPlaces)
		    {
			writer.println(patchCount+". On line "+patchPlace.line+", change the variable "+patchPlace.var+" to "+patchPlace.patchResult);
			patchCount++;
		    }
		    
		    writer.println("--------------------------------------------------------");
		    writer.println("");
		    writer.println("");
		}
		writer.flush();
		writer.close();
		
		
		
	    } catch (FileNotFoundException e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    
	}

	@Override
	protected String validateRequest()
	{
		PatcherValidation val = new PatcherValidation(httpRequest);
		if (!val.validateLogin())
		{
			return "Invalid Login Session";
		}
		if(!val.validateProject(request.projectId))
		{
			return "Invalid Project";
		}
		return null;
	}
	public class PatchPlace
	{
	    public int line;
	    public String var;
	    public String patchResult;
	    public PatchPlace(int line, String var, String patchResult)
	    {
		super();
		this.line = line;
		this.var = var;
		this.patchResult = patchResult;
	    }
	    
	    
	}
}
