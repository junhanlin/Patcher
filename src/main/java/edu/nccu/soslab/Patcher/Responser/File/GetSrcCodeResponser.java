package edu.nccu.soslab.Patcher.Responser.File;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;













import org.apache.commons.lang3.StringEscapeUtils;

import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.soslab.Patcher.PatcherConfig;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.File.ReqGetSrcCode;
import edu.nccu.soslab.Patcher.Json.File.ResGetSrcCode;
import edu.nccu.soslab.Patcher.Json.input.PatcherInput;

public class GetSrcCodeResponser extends JsonResponser<ReqGetSrcCode, ResGetSrcCode>
{

	private Pattern dirRegex;
	public GetSrcCodeResponser(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Class<ReqGetSrcCode> classOfRequest,
			Class<ResGetSrcCode> classOfResponse)
	{
		super(httpRequest, httpResponse, classOfRequest, classOfResponse);
		String projectDirPath = PatcherConfig.config.getProjectDirPath();
		File projectDir;
		try {
		    projectDir = new File(projectDirPath).getCanonicalFile();
		    dirRegex =  Pattern.compile(Pattern.quote(projectDir.getAbsolutePath())+"/(?<projectId>[0-9]*)(?<path>/.*\\.php)");
			
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		
		
	}

	@Override
	protected void processRequest()
	{
		
		
		Matcher matcher=null;
		try {
		    matcher = dirRegex.matcher(new File(request.fileAbsPath).getCanonicalPath());
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		String fileVirPath =null;
		if(matcher.find())
		{
			fileVirPath = matcher.group("path");
			int lastIndexOfSlash = fileVirPath.lastIndexOf("/");
			String fileNameString = fileVirPath.substring(lastIndexOfSlash+1);
			String path = fileVirPath.substring(0,lastIndexOfSlash);
			if(path.isEmpty())
			{
				path+="/";
			}
			querySrcCode(Integer.parseInt(matcher.group("projectId")),path,fileNameString);
		}
		
		 
		
	}
	private boolean querySrcCode(int projectId,String path,String fileName)
	{
		Connection conn = null;
		PreparedStatement selectStat = null;
		
		ResultSet rs = null;
		// int insertedInputId = -1;
		boolean result = false;

		try
		{

			conn = Utility.getSqlConn();
			String selectSql = "SELECT tblFile.fileId,tblFile.content FROM tblFile INNER JOIN tblProject ON tblProject.projectId=tblFile.projectId WHERE tblFile.projectId=? AND tblFile.path=? AND tblFile.fileName=? AND tblProject.userId=?";
			
			selectStat = conn.prepareStatement(selectSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			selectStat.setInt(p++, projectId);
			selectStat.setString(p++, path);
			selectStat.setString(p++, fileName);
			selectStat.setInt(p++, Utility.getCurrUserId(httpRequest));
			
			rs= selectStat.executeQuery();
			if(rs.next())
			{
				response.fileId = rs.getInt("fileId");
				String srcCode = new String(Utility.getBytes(rs.getBinaryStream("content")),"utf-8");
				response.srcCode = StringEscapeUtils.escapeHtml4(srcCode);
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

	@Override
	protected String validateRequest()
	{
		PatcherValidation val = new PatcherValidation(httpRequest);
		if(!val.validateLogin())
		{
			return "Invalid Login Session";
		}
		try {
		    File srcFile = new File(request.fileAbsPath).getCanonicalFile();
		    Matcher matcher = dirRegex.matcher(srcFile.getAbsolutePath());
			if(!matcher.matches())
			{
				return "Invalide File Path";
			}
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		    return "File Validation Error";
		}
		
		
		return null;
	}

}
