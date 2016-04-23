package edu.nccu.soslab.Patcher.Responser.Sink;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;




import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.soslab.Patcher.PatcherConfig;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.Project.ReqGetSinkDetail;
import edu.nccu.soslab.Patcher.Json.Project.ResGetSinkDetail;
import edu.nccu.soslab.Patcher.Json.Project.ResGetSinkDetail.ResPatcherInput;
import edu.nccu.soslab.Patcher.Json.auto.PatcherAuto;
import edu.nccu.soslab.Patcher.Json.input.PatcherInput;


public class GetSInkDetailResponser extends JsonResponser<ReqGetSinkDetail,ResGetSinkDetail>
{

	
	public GetSInkDetailResponser(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Class<ReqGetSinkDetail> classOfRequest,
			Class<ResGetSinkDetail> classOfResponse)
	{
		super(httpRequest, httpResponse, classOfRequest, classOfResponse);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processRequest()
	{
		
		querySink();
		if(response.sink.isVuln)
		{
			response.blackAuto = queryAuto(response.sink.blackAutoId);
			response.whiteAuto = queryAuto(response.sink.whiteAutoId);
			queryInput();
		}
		
	}
	
	public boolean querySink()
	{
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		// int insertedInputId = -1;
		boolean result = false;

		try
		{

			conn = Utility.getSqlConn();
			String selectSql = "SELECT tblFile.projectId,tblFile.fileId,tblFile.fileName,tblFile.path,tblTaintTask.taintTaskId,tblTaintTask.assigned,tblTaintTask.statusCode,tblTaintTask.taintTime,tblTaintTask.taintType,tblSink.sinkId,tblSink.depGraphDot,tblSink.inputs,tblSink.isVuln,tblSink.sanitTime,tblSink.blackAutoId,tblSink.whiteAutoId,tblSink.statusCode,tblSink.terminateType FROM tblSink INNER JOIN tblTaintTask ON tblTaintTask.taintTaskId = tblSink.taintTaskId INNER JOIN tblFile ON tblFile.fileId=tblTaintTask.fileId INNER JOIN tblProject ON tblProject.projectId=tblFile.projectId INNER JOIN tblUser ON tblUser.userId=tblProject.userId WHERE tblSink.sinkId=? AND tblUser.userId=?;";
			
			selectStat = conn.prepareStatement(selectSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			selectStat.setInt(p++, request.sinkId);
			selectStat.setInt(p++, Utility.getCurrUserId(httpRequest));
			
			rs= selectStat.executeQuery();
			if(rs.next())
			{
				response.file.projectId = rs.getInt("projectId");
				response.file.fileId = rs.getInt("fileId");
				response.file.fileName = rs.getString("fileName");
				response.file.path = rs.getString("path");
				response.taintTask.taintTime = rs.getLong("taintTime");
				if(rs.wasNull())
				{
					//if taintTime was null
					response.taintTask.taintTime=0;
				}
				
				
				response.taintTask.statusCode = rs.getInt("statusCode");
				
				
				response.sink.sinkId=rs.getInt("sinkId");
				response.taintTask.taintTaskId= rs.getInt("taintTaskId");
				response.taintTask.taintType=rs.getString("TaintType");
				response.sink.taintTaskId=response.taintTask.taintTaskId;
				response.sink.inputs=rs.getInt("inputs");
				response.sink.isVuln = rs.getBoolean("isVuln");
				response.sink.sanitTime = rs.getLong("sanitTime");
				
				response.sink.statusCode = rs.getInt("statusCode");
				response.sink.blackAutoId = rs.getInt("blackAutoId");
				response.sink.whiteAutoId = rs.getInt("whiteAutoId");
				response.depGraphPlainTxt = convertDotToPlainText(rs.getString("depGraphDot"));
				
				
				
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
	public PatcherAuto queryAuto(int autoId)
	{
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		// int insertedInputId = -1;
		PatcherAuto result = null;

		try
		{

			conn = Utility.getSqlConn();
			String selectSql = "SELECT tblAuto.autoId,tblAuto.autoDot,tblAuto.bdd,tblAuto.state FROM tblAuto INNER JOIN tblSink ON (tblSink.blackAutoId=? OR tblSink.whiteAutoId=?) INNER JOIN tblTaintTask ON tblTaintTask.taintTaskId=tblSink.taintTaskId INNER JOIN tblFile ON tblFile.fileId=tblTaintTask.fileId INNER JOIN tblProject ON tblProject.projectId=tblFile.projectId INNER JOIN tblUser ON tblUser.userId=tblProject.userId WHERE tblAuto.autoId=? AND tblUser.userId=?;";
			
			selectStat = conn.prepareStatement(selectSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			selectStat.setInt(p++, autoId);
			selectStat.setInt(p++, autoId);
			selectStat.setInt(p++, autoId);
			selectStat.setInt(p++, Utility.getCurrUserId(httpRequest));
			
			rs= selectStat.executeQuery();
			if(rs.next())
			{
				result = new PatcherAuto();
				result.autoId = rs.getInt("autoId");
				result.bdd = rs.getInt("bdd");
				result.state = rs.getInt("state");
				result.autoDot = rs.getString("autoDot");
				
			}
			

		} catch (Exception e)
		{
			e.printStackTrace();
			addResponseMsg(e);
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
	public boolean queryInput()
	{
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		// int insertedInputId = -1;
		boolean result = false;

		try
		{

			conn = Utility.getSqlConn();
			String selectSql = "SELECT tblInput.*  FROM tblInput INNER JOIN tblSink ON tblSink.sinkId=tblInput.sinkId INNER JOIN tblTaintTask ON tblTaintTask.taintTaskId=tblSink.taintTaskId  INNER JOIN tblFile ON tblFile.fileId=tblTaintTask.fileId INNER JOIN tblProject ON tblProject.projectId=tblFile.projectId INNER JOIN tblUser ON tblUser.userId=tblProject.userId WHERE tblSink.sinkId=? AND tblProject.userId=?";
			
			selectStat = conn.prepareStatement(selectSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			selectStat.setInt(p++, request.sinkId);
			selectStat.setInt(p++, Utility.getCurrUserId(httpRequest));
			
			rs= selectStat.executeQuery();
			while(rs.next())
			{
				ResPatcherInput input = response.new ResPatcherInput();
				input.sinkId = rs.getInt("sinkId");
				input.inputId = rs.getInt("inputId");
				input.inputNo = rs.getInt("inputNo");
				input.varName = rs.getString("varName");
				input.lineNo = rs.getInt("lineNo");
				String patch = rs.getString("patch"); 
				input.patch = patch;
				StringBuilder patchFunctBuilder = new StringBuilder();
				patchFunctBuilder.append("preg_replace(\"/");
				
				for(int i=0;i<patch.length();i++)
				{
					String c= StringEscapeUtils.escapeJava(String.valueOf(patch.charAt(i)));
					if(c.length()==1)
					{
						if(c.equals("/") 
								|| c.equals("\\") 
								|| c.equals("?")
								|| c.equals("*")
								|| c.equals("$")
								|| c.equals("^")
								|| c.equals(".")
								|| c.equals("+")
								|| c.equals("|")
								|| c.equals("+")
								|| c.equals("[")
								|| c.equals("]")
								|| c.equals("(")
								|| c.equals(")")
								|| c.equals("{")
								|| c.equals("}")
								)
						{
							//escape these symbol
							patchFunctBuilder.append("\\");
							
						}
						patchFunctBuilder.append(c);
					}
					else
					{
						//we get a unicode such as \u00ff
						patchFunctBuilder.append("\\x{"+c.substring(2)+"}"); //append: \x{00ff}
						
					}
					
					
					if(i!=patch.length()-1)
					{
						patchFunctBuilder.append("|");
					}
				}
				patchFunctBuilder.append("/u\",\"\","+input.varName+")");
				input.patchFunct = patchFunctBuilder.toString();
				response.inputs.add(input);
			}
			

		} catch (Exception e)
		{
			e.printStackTrace();
			addResponseMsg(e);
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
	public String convertDotToPlainText(String dot)
	{
		String result = null;
		String randUUIDString = UUID.randomUUID().toString();
		try
		{
			File plainTxtFile = new File(PatcherConfig.config.getTmpDirPath()+"/"+UUID.randomUUID().toString()+".txt");
			File dotFile = new File(PatcherConfig.config.getTmpDirPath()+"/"+UUID.randomUUID().toString()+".dot");
			
			/*write dot to tmp file*/
			FileWriter dotWriter = new FileWriter(dotFile);
			dotWriter.write(dot);
			dotWriter.close();
					
			/*convert dot to plain-ext*/
			String[] command = { "dot","-Tplain-ext",dotFile.getAbsolutePath(),"-o",plainTxtFile.getAbsolutePath()}; 
			
			Runtime runtime = Runtime.getRuntime();
			Process process = null;
			
			
			process = runtime.exec(command);
			BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
			
			// Read and print the output
			String line = null;
			while ((line = stdOutReader.readLine()) != null)
			{
				System.out.println(line);
			}
			while ((line = stdErrReader.readLine()) != null)
			{
				System.out.println(line);
			}
			process.waitFor();
			System.out.print("Exit Value of dot: "+process.exitValue());
			
			result=new String(Utility.getBytes(new FileInputStream(plainTxtFile)),"utf-8");
			
		} catch (Exception e)
		{
			e.printStackTrace();
			addResponseMsg(e);
		}
		return result;
		
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
