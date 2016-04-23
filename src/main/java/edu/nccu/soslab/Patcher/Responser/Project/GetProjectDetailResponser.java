package edu.nccu.soslab.Patcher.Responser.Project;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.graph.GraphAdapterBuilder;

import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.StatusCode;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.File.PatcherFile;
import edu.nccu.soslab.Patcher.Json.Project.ReqGetProjectDetail;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProject;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProjectDetail;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProject.Project;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProjectDetail.FileNode;
import edu.nccu.soslab.Patcher.Json.Project.ResGetProjectDetail.ResSink;

public class GetProjectDetailResponser extends JsonResponser<ReqGetProjectDetail, ResGetProjectDetail>
{

	public GetProjectDetailResponser(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Class<ReqGetProjectDetail> classOfRequest,
			Class<ResGetProjectDetail> classOfResponse)
	{
		super(httpRequest, httpResponse, classOfRequest, classOfResponse);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void complete()
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		new GraphAdapterBuilder()
		    .addType(FileNode.class)
		    .registerOn(gsonBuilder);
		Gson gson = gsonBuilder.create();
		try
		{

			httpResponse.setContentType("application/json");
			PrintWriter resWriter;

			resWriter = httpResponse.getWriter();

			String responseStr = gson.toJson(getResponse());
			System.out.println("[Reponse:" + classOfResponse.getName() + "]");
			System.out.println(responseStr);
			resWriter.write(responseStr);
			resWriter.close();

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void processRequest()
	{
		queryProjectDetail();
	}

	public boolean queryProjectDetail()
	{
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		
		boolean result = false;

		try
		{

			conn = Utility.getSqlConn();
			String selectSql = "SELECT tblProject.projectName,tblProject.projectDesc,tblTaintTask.taintTaskId,tblTaintTask.assigned,tblTaintTask.statusCode,tblTaintTask.taintTime,tblTaintTask.taintType, tblFile.projectId,tblFile.fileId,tblFile.fileName,tblFile.path,tblSink.sinkId,tblSink.inputs,tblSink.isVuln,tblSink.sanitTime,tblSink.StatusCode AS sinkStatusCode FROM tblSink RIGHT JOIN tblTaintTask ON tblTaintTask.taintTaskId = tblSink.taintTaskId INNER JOIN tblFile ON tblFile.fileId = tblTaintTask.fileId INNER JOIN tblProject ON tblProject.projectId = tblFile.projectId WHERE tblTaintTask.taintType=? AND tblFile.projectId= ? AND tblProject.userId=?  ORDER BY tblFile.path ASC,tblFile.fileName ASC,tblTaintTask.taintType ASC, tblSink.sinkId ASC";			
			selectStat = conn.prepareStatement(selectSql);
			int p = 1;
			selectStat.setString(p++, request.taintType);
			selectStat.setInt(p++, request.projectId);
			selectStat.setInt(p++, Utility.getCurrUserId(httpRequest));
			
			rs= selectStat.executeQuery();
			HashMap<String,FileNode> pathNodeMap = new HashMap<>();
			//setup root node
			response.fileRootNode = response.new FileNode();
			response.fileRootNode.isDir=true;
			response.fileRootNode.parent=null;
			response.fileRootNode.nodePath="/";
			response.fileRootNode.nodeName="_ROOT";
			pathNodeMap.put("/", response.fileRootNode);
			
			
			ResGetProjectDetail.ResTaintTask currTaintTask = null;
			PatcherFile currFile =null;
			while(rs.next())
			{
				if(response.projectName==null)
				{
					response.projectName = rs.getString("projectName");
				}
				if(response.projectDesc==null)
				{
					response.projectDesc = rs.getString("projectDesc");
				}
				int taintTaskId = rs.getInt("taintTaskId");
				if(currTaintTask == null || currTaintTask.taintTaskId!=taintTaskId)
				{
					
					currFile = new PatcherFile();
					currFile.projectId = rs.getInt("projectId");
					currFile.fileId = rs.getInt("fileId");
					currFile.fileName=rs.getString("fileName");
					currFile.path =rs.getString("path");
					
					
					currTaintTask =response.new ResTaintTask();
					currTaintTask.taintTaskId = taintTaskId;
				
					currTaintTask.assigned=rs.getBoolean("assigned");
					currTaintTask.taintTime = rs.getLong("taintTime");
					currTaintTask.taintType = rs.getString("taintType");
					if(rs.wasNull())
					{
						//if taintTime was null
						currTaintTask.taintTime=0;
					}
					
					
					currTaintTask.statusCode = rs.getInt("statusCode");
					
					//build dir node (in not exist) and find the parent node of this file
					String[] pathParts = currFile.path.split("/");
					StringBuilder pathBuilder = new StringBuilder();
					pathBuilder.append("/");
					FileNode parent = response.fileRootNode;
					for(int i=1;i<pathParts.length;i++)// we skip the first part
					{
						pathBuilder.append(pathParts[i]);
						
						if(!pathNodeMap.containsKey(pathBuilder.toString()))
						{
							FileNode dirNode = response.new FileNode();
							dirNode.isDir=true;
							dirNode.parent=parent;
							dirNode.nodePath=pathBuilder.toString();
							dirNode.nodeName=pathParts[i];
							parent.children.add(dirNode);
							pathNodeMap.put(pathBuilder.toString(), dirNode);
							parent = dirNode;
						}
						else 
						{
							parent = pathNodeMap.get(pathBuilder.toString());
						}
						pathBuilder.append("/");
						
					}
					
					//create the file node of this file
					
					FileNode fileNode = response.new FileNode();
					fileNode.isDir=false;
					fileNode.taintTask = currTaintTask;
					fileNode.file = currFile;
					fileNode.parent=parent;
					fileNode.nodePath = currFile.path;
					fileNode.nodeName = currFile.fileName;
					parent.children.add(fileNode);
					increaseFileStatusStatics(currTaintTask.assigned,currTaintTask.statusCode);
					
				}
				
				//sink
				int sinkId = rs.getInt("sinkId");
				
				if(rs.wasNull())
				{
					//this taint task has no sink result row
					continue;
				}
				
				ResSink sink = response.new ResSink();
				sink.taintTaskId = currTaintTask.taintTaskId;
				sink.sinkId = sinkId;
				sink.inputs = rs.getInt("inputs");
				sink.isVuln = rs.getBoolean("isVuln");
				
				sink.sanitTime = rs.getLong("sanitTime");
				
				sink.statusCode = rs.getInt("sinkStatusCode");
				increaseSinkStatusStatics(sink.statusCode);
				currTaintTask.sinks.add(sink);
				
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
	
	private void increaseFileStatusStatics(boolean assigned,int statusCode)
	{
		response.numOfTotalTask++;
		if(!assigned)
		{
			response.numOfUnassignedTask++;
		}
		
		if(statusCode==StatusCode.NO_STATUS)
		{
			response.numOfUntaintedTask++;
		}
		else if(statusCode==StatusCode.PARSE)
		{
			response.numOfParseTask++;
		}
		else if(statusCode==StatusCode.PARSE_FAIL)
		{
			response.numOfParseFailTask++;
		}
		else if(statusCode==StatusCode.TAINT)
		{
			response.numOfTaintTask++;
		}
		else if(statusCode==StatusCode.TAINT_FAIL)
		{
			response.numOfTaintFailTask++;
		}
		else if(statusCode==StatusCode.TAINT_COMPLETE)
		{
			response.numOfTaintCompleteTask++;
		}
		
		
	}
	private void increaseSinkStatusStatics(int sinkStatusCode)
	{
		response.numOfTotalSinks++;
		if(sinkStatusCode==StatusCode.NO_STATUS)
		{
			response.numOfUnsanitSinks++;
		}
		else if(sinkStatusCode==StatusCode.FORWARD)
		{
			response.numOfForwardSinks++;
		}
		else if(sinkStatusCode==StatusCode.FORWARD_FAIL)
		{
			response.numOfForwardFailSinks++;
		}
		else if(sinkStatusCode==StatusCode.BACKWARD)
		{
			response.numOfBackwardSinks++;
		}
		else if(sinkStatusCode==StatusCode.BACKWARD_FAIL)
		{
			response.numOfBackwardFailSinks++;
		}
		else if(sinkStatusCode==StatusCode.PATCH)
		{
			response.numOfPatchSinks++;
		}
		else if(sinkStatusCode==StatusCode.PATCH_FAIL)
		{
			response.numOfPatchFailSinks++;
		}
		else if(sinkStatusCode==StatusCode.SANIT_COMPLETE)
		{
			response.numOfSanitCompleteSinks++;
		}
		
	}
	
	@Override
	protected String validateRequest()
	{
		PatcherValidation val = new PatcherValidation(httpRequest);
		if(!val.validateLogin())
		{
			return "Invalid Login Session";
		}
		return null;
	}
	
	

}
