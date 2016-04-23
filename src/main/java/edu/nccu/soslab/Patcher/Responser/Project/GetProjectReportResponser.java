package edu.nccu.soslab.Patcher.Responser.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;

import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.soslab.Patcher.PatcherConfig;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.File.PatcherFile;
import edu.nccu.soslab.Patcher.Json.Project.ReqGetOptPatch;
import edu.nccu.soslab.Patcher.Json.Project.ReqGetProjectReport;
import edu.nccu.soslab.Patcher.Json.Project.ResGetOptPatch;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProjectReport;
import edu.nccu.soslab.Patcher.Servlet.GenWorkspaceSummary.Summary.TaintTask;

public class GetProjectReportResponser extends JsonResponser<ReqGetProjectReport, ResGetProjectReport>
{
	private File reportDir;
	private File reportZipFile;
	
	public GetProjectReportResponser(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse,
			Class<ReqGetProjectReport> classOfRequest,
			Class<ResGetProjectReport> classOfResponse)
	{
		super(httpRequest, httpResponse, classOfRequest, classOfResponse);
		// TODO Auto-generated constructor stub
	}
	public GetProjectReportResponser(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, ReqGetProjectReport request,
			Class<ResGetProjectReport> classOfResponse)
	{
		super(httpRequest, httpResponse, request, classOfResponse);
	}
	
	@Override
	public void complete()
	{
		Gson gson = new Gson();
		
		try
		{
			ResGetProjectReport resJson = getResponse();
			String responseStr = gson.toJson(resJson);
			
			if(resJson.isSuccess )
			{
				
				httpResponse.setContentType("application/octet-stream");
				httpResponse.setHeader("Content-Disposition",
					"attachment; filename=\""+reportZipFile.getName()+"\"");
				OutputStream out= httpResponse.getOutputStream();
				FileInputStream in = new FileInputStream(reportZipFile);
				byte[] buffer = new byte[4096];
				int length;
				while ((length = in.read(buffer)) > 0){
				    out.write(buffer, 0, length);
				}
				in.close();
				out.flush();
				out.close();
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
	public void cleanUp()
	{
		if(reportZipFile!=null && reportZipFile.exists())
		{
			reportZipFile.delete();
		}
		if(reportDir!=null && reportDir.exists() && reportDir.isDirectory())
		{
			try
			{
				FileUtils.deleteDirectory(reportDir);
				FileUtils.deleteDirectory(reportDir.getParentFile());
				//FileUtils.deleteDirectory(reportDir.getParentFile().getParentFile());
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
	protected void processRequest()
	{
		if(request.projectId==0)
		{
			genWorkspaceReport();
		}
		else
		{
			genProjectReport();
			
		}
		
	}
	private void genWorkspaceReport()
	{
		try
		{
			//gen work dir
			UUID uuid = UUID.randomUUID();
			reportDir = new File(PatcherConfig.config.getProjectDirPath()
					+ "/" + request.projectId+"/report_" + uuid,"Workspace-Report");
			System.out.println("output: "+reportDir.getAbsolutePath());
			if(!reportDir.exists())
			{
				reportDir.mkdirs();
			}
			List<PatcherFile> files = getWorkspaceFiles(reportDir);
			for(PatcherFile file : files)
			{
				File fileDir = new File(reportDir,file.path+"/"+file.fileName);
				genFileReport(file,fileDir);
			}
			
			
			//gen archive zip file
			reportZipFile = new File(reportDir.getParent(),reportDir.getName()+".zip");
			Utility.createZip(reportDir.getAbsolutePath(),reportZipFile.getAbsolutePath());
			response.isSuccess=true;
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void genProjectReport()
	{
		
		try
		{
			//gen work dir
			UUID uuid = UUID.randomUUID();
			File reportDir = new File(PatcherConfig.config.getProjectDirPath()
					+ "/" + request.projectId+"/report_" + uuid,request.projectId+"-Report");
			reportDir.mkdirs();
			
			List<PatcherFile> files = getProjectFiles(request.projectId,reportDir);
			for(PatcherFile file : files)
			{
				File fileDir = new File(reportDir,file.path+"/"+file.fileName);
				genFileReport(file,fileDir);
			}
			
			
			//gen archive zip file
			reportZipFile = new File(reportDir.getParent(),reportDir.getName()+".zip");
			Utility.createZip(reportDir.getAbsolutePath(),reportZipFile.getAbsolutePath());
			response.isSuccess=true;
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private List<PatcherFile> getProjectFiles(int projectId,File reportDir)
	{
		List<PatcherFile> retVal = new ArrayList<>();
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		

		try
		{

			conn = Utility.getSqlConn();
			String selectSql = "SELECT tblFile.*,tblProject.projectName FROM tblSink "+
"INNER JOIN tblTaintTask ON tblTaintTask.taintTaskId = tblSink.taintTaskId "+
"INNER JOIN tblFile ON tblFile.fileId = tblTaintTask.fileId "+
"INNER JOIN tblProject ON tblProject.projectId = tblFile.projectId "+
"WHERE tblProject.projectId=? AND tblSink.isVuln=1 AND tblSink.statusCode=700 "+
"GROUP BY tblFile.fileId ";
			
			selectStat = conn.prepareStatement(selectSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			
			selectStat.setInt(p++, request.projectId);
			
			rs= selectStat.executeQuery();
			while(rs.next())
			{
				PatcherFile file = new PatcherFile();
				file.fileId = rs.getInt("fileId");
				file.projectId = rs.getInt("projectId");
				file.fileName = rs.getString("fileName");
				file.path = rs.getString("path");
				retVal.add(file);
				File fileDir = new File(reportDir,rs.getString("path")+"/"+file.fileName);
				if(!fileDir.exists())
				{
					fileDir.mkdirs();
				}
				
				File phpSrcFile = new File(fileDir,file.fileName);
				
				copy(rs.getBinaryStream("content"), phpSrcFile);
			}
			

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
		
		return retVal;
		
	}
	private List<PatcherFile> getWorkspaceFiles(File reportDir)
	{
		List<PatcherFile> retVal = new ArrayList<>();
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		

		try
		{

			conn = Utility.getSqlConn();
			String selectSql = "SELECT tblFile.*,tblProject.projectName FROM tblSink "+
"INNER JOIN tblTaintTask ON tblTaintTask.taintTaskId = tblSink.taintTaskId "+
"INNER JOIN tblFile ON tblFile.fileId = tblTaintTask.fileId "+
"INNER JOIN tblProject ON tblProject.projectId = tblFile.projectId "+
"WHERE tblProject.userId=? AND tblSink.isVuln=1 AND tblSink.statusCode=700 "+
"GROUP BY tblFile.fileId ";
			
			selectStat = conn.prepareStatement(selectSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			
			selectStat.setInt(p++, Utility.getCurrUserId(httpRequest));
			
			rs= selectStat.executeQuery();
			while(rs.next())
			{
				PatcherFile file = new PatcherFile();
				file.fileId = rs.getInt("fileId");
				file.projectId = rs.getInt("projectId");
				file.fileName = rs.getString("fileName");
				file.path = rs.getString("path");
				retVal.add(file);
				File fileDir = new File(reportDir,rs.getString("path")+"/"+file.fileName);
				if(!fileDir.exists())
				{
					fileDir.mkdirs();
				}
				
				File phpSrcFile = new File(fileDir,file.fileName);
				
				copy(rs.getBinaryStream("content"), phpSrcFile);
			}
			

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
		
		return retVal;
		
	}
	public void genFileReport(PatcherFile file,File fileDir)
	{
		
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		

		try
		{

			conn = Utility.getSqlConn();
			String selectSql = "SELECT tblSink.sinkId,tblTaintTask.taintType,tblTaintTask.atkPattern,tblSink.depGraphDot,tblSink.inputs,tblSink.isVuln,tblSink.sanitTime,"+
					"tblBlackAuto.autoId AS blackAutoId,tblBlackAuto.autoDot AS blackAutoDot,tblBlackAuto.bdd AS blackAutoBdd,tblBlackAuto.state AS blackAutoState,"+
					"tblWhiteAuto.autoId AS whiteAutoId,tblWhiteAuto.autoDot AS whiteAutoDot,tblWhiteAuto.bdd AS whiteAutoBdd,tblWhiteAuto.state AS whiteAutoState "+
					"FROM tblSink "+
					"INNER JOIN tblAuto tblBlackAuto ON tblBlackAuto.autoId=tblSink.blackAutoId "+
					"INNER JOIN tblAuto tblWhiteAuto ON tblWhiteAuto.autoId=tblSink.whiteAutoId "+
					"INNER JOIN tblTaintTask ON tblTaintTask.taintTaskId = tblSink.taintTaskId "+
					"INNER JOIN tblFile ON tblFile.fileId = tblTaintTask.fileId "+
					"WHERE tblFile.fileId=? AND tblSink.isVuln=1 AND tblSink.statusCode=700 ";
			
			selectStat = conn.prepareStatement(selectSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			
			selectStat.setInt(p++, file.fileId);
			
			rs= selectStat.executeQuery();
			while(rs.next())
			{
				int sinkId = rs.getInt("sinkId");
				String taintType = rs.getString("taintType");
				File sinkDir = new File(fileDir,taintType+"_"+sinkId);
				if(!sinkDir.exists())
				{
					sinkDir.mkdirs();
				}
				File depGraphFile = new File(sinkDir,"depGraph.dot");
				File blackAutoFile = new File(sinkDir,"black.auto.dot");
				File whiteAutoFile = new File(sinkDir,"white.auto.dot");
				copy(rs.getBinaryStream("depGraphDot"), depGraphFile);
				copy(rs.getBinaryStream("blackAutoDot"), blackAutoFile);
				copy(rs.getBinaryStream("whiteAutoDot"), whiteAutoFile);
				
				FileWriter infoWriter = new FileWriter(new File(sinkDir,"sink.info"));
				infoWriter.append("sinkId:"+sinkId+"\n");
				infoWriter.append("fileId:"+file.fileId+"\n");
				infoWriter.append("fileName:"+file.fileName+"\n");
				infoWriter.append("taintType:"+taintType+"\n");
				infoWriter.append("attackPattern:"+rs.getString("atkPattern")+"\n");
				infoWriter.append("inputs:"+rs.getInt("inputs")+"\n");
				infoWriter.append("isVuln:"+rs.getBoolean("isVuln")+"\n");
				infoWriter.append("sanitTime:"+rs.getString("sanitTime")+"\n");
				infoWriter.append("blackAutoId:"+rs.getInt("blackAutoId")+"\n");
				infoWriter.append("blackAutoBdd:"+rs.getInt("blackAutoBdd")+"\n");
				infoWriter.append("blackAutoState:"+rs.getInt("blackAutoState")+"\n");
				infoWriter.append("whiteAutoId:"+rs.getInt("whiteAutoId")+"\n");
				infoWriter.append("whiteAutoBdd:"+rs.getInt("whiteAutoBdd")+"\n");
				infoWriter.append("whiteAutoState:"+rs.getInt("whiteAutoState"));
				
				infoWriter.flush();
				infoWriter.close();
				
			}
			

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
		
		
	}
	@Override
	protected String validateRequest()
	{
		PatcherValidation val = new PatcherValidation(httpRequest);
		if (!val.validateLogin())
		{
			return "Invalid Login Session";
		}
		if(request.projectId!=0)
		{
			if(!val.validateProject(request.projectId))
			{
				return "Invalid Project";
			}
		}
		
		return null;
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
}
