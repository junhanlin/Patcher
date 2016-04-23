package edu.nccu.soslab.Patcher.Servlet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.RowSet;

import com.google.gson.Gson;

import edu.nccu.soslab.Patcher.StatusCode;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.File.PatcherFile;
import edu.nccu.soslab.Patcher.Json.Project.PatcherProject;
import edu.nccu.soslab.Patcher.Json.Sink.PatcherSink;
import edu.nccu.soslab.Patcher.Json.TaintTask.PatcherTaintTask;
import edu.nccu.soslab.Patcher.Json.auto.PatcherAuto;

/**
 * Servlet implementation class GenWorkspaceSummary
 */
@WebServlet("/GenWorkspaceSummary")
public class GenWorkspaceSummary extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GenWorkspaceSummary() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		int userId = Utility.getCurrUserId(request);
		if(userId == -1)
		{
			response.sendRedirect("/Views/Index/Index.html");
		}
		else
		{
			Connection conn = Utility.getSqlConn();
			
			
			OutputStream out = response.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition",
				"attachment; filename=\"workspaceSummary_"+System.currentTimeMillis()+"_.tex\"");
			Summary summary = getSummary(conn, userId);
			Map<String, TaintProgressRow> taintProgressTable = getTaintProgressTable(summary);
			Map<String, SanitProgressRow> sanitProgressTable = getSanitProgressTable(summary);
			
			appendFileHeader(writer,getTaintProgressCSVContent(taintProgressTable),getSanitProgressCSVContent(sanitProgressTable));
			appendAnalysisSummaryTable(writer, getAnalysisSummaryTable(summary));
			appendAutoSummaryTable(writer, getBlackAutoSummaryTable(summary),getWhiteAutoSummaryTable(summary));
			appendTaintProgressTable(writer, getTaintProgressTable(summary));
			appendSanitProgressTable(writer, getSanitProgressTable(summary));
			appendTaintProgressPie(writer);
			appendSanitProgressPie(writer);
			appendFailReasonTable(writer,getReasonOfForwardFail(summary) , getReasonOfBackwardFail(summary), getReasonOfPatchFail(summary));
			appendFileFooter(writer);
			
			writer.flush();
			writer.close();
		}
		
		
	}
	
	private Summary getSummary(Connection conn,int userId)
	{
		Summary retVal = new Summary();
		PreparedStatement selectStat = null;
		ResultSet rs = null;
		
		try
		{

			
			String selectSql = "SELECT tblProject.projectId,tblProject.projectName,tblProject.projectDesc,tblProject.statusCode FROM tblProject INNER JOIN tblUser ON tblUser.userId=tblProject.userId WHERE tblUser.userId=?;";
			
			selectStat = conn.prepareStatement(selectSql);
			int p = 1;
			selectStat.setInt(p++, userId);
			
			rs= selectStat.executeQuery();
			while(rs.next())
			{
				Summary.Project project = retVal.new Project();
				retVal.projects.add(project);
				project.projectId=rs.getInt("projectId");
				project.projectName=rs.getString("projectName");
				project.projectDesc = rs.getString("projectDesc");
				project.statusCode=rs.getInt("statusCode");
				
				project.files.addAll(getFile(conn, retVal, project));
				
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
	private List<Summary.File> getFile(Connection conn,Summary summary,Summary.Project project)
	{
		List<Summary.File> retVal = new ArrayList<>();
		PreparedStatement selectStat = null;
		ResultSet rs = null;
		
		try
		{

			
			String selectSql = "SELECT tblFile.fileId,tblFile.projectId,tblFile.fileName,tblFile.path,tblFile.content FROM tblFile WHERE tblFile.projectId=?";
			
			selectStat = conn.prepareStatement(selectSql);
			int p = 1;
			selectStat.setInt(p++, project.projectId);
			
			rs= selectStat.executeQuery();
			while(rs.next())
			{
				Summary.File file = summary.new File();
				retVal.add(file);
				file.fileId = rs.getInt("fileId");
				file.projectId=rs.getInt("projectId");
				file.fileName=rs.getString("fileName");
				file.path = rs.getString("path");
				file.lines=rs.getString("content").split("\n").length;
				file.taintTasks.addAll(getTaintTasks(conn, summary, file));
				
				
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
				
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retVal;
	}
	private List<Summary.TaintTask> getTaintTasks(Connection conn,Summary summary,Summary.File file)
	{
		List<Summary.TaintTask> retVal = new ArrayList<>();
		PreparedStatement selectStat = null;
		ResultSet rs = null;
		
		try
		{

			
			String selectSql = "SELECT tblTaintTask.taintTaskId,tblTaintTask.fileId,tblTaintTask.taintType,tblTaintTask.statusCode,tblTaintTask.taintTime,tblTaintTask.terminateType,tblTaintTask.assigned,tblTaintTask.atkPattern FROM tblTaintTask WHERE tblTaintTask.fileId=?";
			
			selectStat = conn.prepareStatement(selectSql);
			int p = 1;
			selectStat.setInt(p++,file.fileId);
			
			rs= selectStat.executeQuery();
			while(rs.next())
			{
				Summary.TaintTask taintTask = summary.new TaintTask();
				retVal.add(taintTask);
				taintTask.taintTaskId = rs.getInt("taintTaskId");
				taintTask.fileId = rs.getInt("fileId");
				taintTask.taintType=rs.getString("taintType");
				taintTask.statusCode=rs.getInt("statusCode");
				taintTask.taintTime = rs.getLong("taintTime");
				taintTask.terminateType = rs.getString("terminateType");
				taintTask.assigned = rs.getBoolean("assigned");
				taintTask.atkPattern = rs.getString("atkPattern");
				taintTask.sinks.addAll(getSink(conn, summary, taintTask));
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
				
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retVal;
	}
	private List<Summary.Sink> getSink(Connection conn,Summary summary,Summary.TaintTask taintTask)
	{
		List<Summary.Sink> retVal = new ArrayList<>();
		PreparedStatement selectStat = null;
		ResultSet rs = null;
		
		try
		{

			
			String selectSql = "SELECT tblSink.sinkId,tblSink.taintTaskId,tblSink.inputs,tblSink.isVuln,tblSink.sanitTime,statusCode,tblSink.terminateType,tblSink.assigned,tblSink.blackAutoId,tblSink.whiteAutoId FROM tblSink WHERE tblSink.taintTaskId=?";
			
			selectStat = conn.prepareStatement(selectSql);
			int p = 1;
			selectStat.setInt(p++,taintTask.taintTaskId);
			rs= selectStat.executeQuery();
			while(rs.next())
			{
				Summary.Sink sink  = summary.new Sink();
				retVal.add(sink);
				sink.sinkId = rs.getInt("sinkId");
				sink.taintTaskId = rs.getInt("taintTaskId");
				sink.inputs=rs.getInt("inputs");
				sink.isVuln=rs.getBoolean("isVuln");
				sink.sanitTime = rs.getLong("sanitTime");
				sink.statusCode = rs.getInt("statusCode");
				sink.terminateType = rs.getString("terminateType");
				sink.assigned= rs.getBoolean("assigned");
				sink.blackAutoId = rs.getInt("blackAutoId");
				sink.whiteAutoId = rs.getInt("whiteAutoId");
				sink.blackAuto = getAuto(conn, sink.blackAutoId);
				sink.whiteAuto = getAuto(conn, sink.whiteAutoId);
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
				
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retVal;
	}
	private PatcherAuto getAuto(Connection conn,int autoId)
	{
		PatcherAuto retVal = new PatcherAuto();
		PreparedStatement selectStat = null;
		ResultSet rs = null;
		
		try
		{

			
			String selectSql = "SELECT tblAuto.autoId,tblAuto.bdd,tblAuto.state FROM tblAuto WHERE tblAuto.autoId=?";
			
			selectStat = conn.prepareStatement(selectSql);
			int p = 1;
			selectStat.setInt(p++,autoId);
			rs= selectStat.executeQuery();
			if(rs.next())
			{	
				retVal.autoId = rs.getInt("autoId");
				retVal.bdd = rs.getInt("bdd");
				retVal.state=rs.getInt("state");
				
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
				
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retVal;
	}
	private void appendFileHeader(BufferedWriter writer,String taintProgressCSVContent,String sanitProgressCSVContent) throws IOException
	{

		writer.write("\\documentclass{report}\n");
		writer.write("\\usepackage{multirow}\n");
		writer.write("\\usepackage[margin=1.5cm]{geometry}\n");
		writer.write("\\usepackage{booktabs}\n");
		writer.write("\\newcommand{\\tabincell}[2]{\\begin{tabular}{@{}#1@{}}#2\\end{tabular}}\n");
		writer.write("\\usepackage{datapie}\n");
		writer.write(taintProgressCSVContent);
		writer.write(sanitProgressCSVContent);
		
		writer.write("\\begin{document}\n");
		
	}
	private void appendFileFooter(BufferedWriter writer) throws IOException
	{
		writer.write("\\end{document}\n");
	}
	private LinkedHashMap<String, AnalysisSummaryRow> getAnalysisSummaryTable(Summary summary)
	{
		LinkedHashMap<String, AnalysisSummaryRow> retVal = new LinkedHashMap<>();
		for(Summary.Project proj : summary.projects)
		{
			if(!retVal.containsKey(proj.projectName))
			{
				retVal.put(proj.projectName, new AnalysisSummaryRow());
				
			}
			AnalysisSummaryRow row = retVal.get(proj.projectName);
			row.projectName = proj.projectName;
			if(row.numOfFile==0)
			{
				row.numOfFile = proj.files.size();
			}
			
			
			if(row.numOfLines==0)
			{
				row.numOfLines=proj.getNumOfLines();
			}
			
			
			Map<String,Integer> numOfSinks = proj.getNumOfSinks();
			if(row.numOfSqliSinks ==0)
			{
				row.numOfSqliSinks = numOfSinks.get("SQLI");
			}
			if(row.numOfXssSinks == 0)
			{
				row.numOfXssSinks = numOfSinks.get("XSS");
			}
			if(row.numOfMfeSinks == 0)
			{
				row.numOfMfeSinks = numOfSinks.get("MFE");
			}
			
			
			Map<String,Integer> numOfVulns = proj.getNumOfVulns();
			row.numOfSqliVulns += numOfVulns.get("SQLI");
			row.numOfXssVulns += numOfVulns.get("XSS");
			row.numOfMfeVulns += numOfVulns.get("MFE");
			
			if(row.taintTime == 0)
			{
				row.taintTime = proj.getTaintTime();
			}
			row.sanitTime+=proj.getSanitTime();
			
			
		}
		return retVal;
		
	}
	private void appendAnalysisSummaryTable(BufferedWriter writer,Map<String,AnalysisSummaryRow> table) throws IOException
	{
		/*
		 \begin{table}[htbp]
		  \centering
		  \caption{Analysis Summary}
		    \begin{tabular}{ccccccccccccc}
		    \toprule
		    Application & \# of Files & \multicolumn{4}{c}{\# of Tainted Sinks} & \multicolumn{4}{c}{\# of Vulnerabilities} & \multicolumn{3}{c}{Analysis Time} \\
		    \midrule
		          &       & SQLI  & XSS   & MFE   & Total & SQLI  & XSS   & MFE   & Total & Taint & Sanitize & Total \\
		    benchmarks & 10    & 0     & 8     & 0     & 8     & 0     & 4     & 0     & 4     & 6161  & 12315 & 18476 \\
		    e107  & 218   & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 132035 & 0     & 132035 \\
		    examples & 5     & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 3038  & 0     & 3038 \\
		    market & 22    & 0     & 0     & 8     & 8     & 0     & 0     & 2     & 2     & 14295 & 717   & 15012 \\
		    moodle1\_6 & 1319  & 0     & 1927  & 767   & 2694  & 0     & 1489  & 132   & 1621  & 990190 & 1262022 & 2252212 \\
		    nucleus3.64 & 67    & 0     & 27    & 32    & 59    & 0     & 7     & 2     & 9     & 48356 & 9523  & 57879 \\
		    PBLGuestbook & 3     & 8     & 0     & 2     & 10    & 7     & 0     & 2     & 9     & 3873  & 9324  & 13197 \\
		    php-fusion-6-01-18 & 1156  & 77    & 373   & 597   & 1047  & 72    & 140   & 419   & 631   & 842751 & 2621392 & 3464143 \\
		    schoolmate & 63    & 308   & 58    & 0     & 366   & 305   & 58    & 0     & 363   & 39477 & 2413034 & 2452511 \\
		    sendcard\_3-4-1 & 72    & 1     & 12    & 40    & 53    & 1     & 12    & 9     & 22    & 48070 & 34649 & 82719 \\
		    servoo & 26    & 3     & 1     & 37    & 41    & 1     & 0     & 1     & 2     & 19904 & 4864  & 24768 \\
		    \bottomrule
		    \end{tabular}%
		  \label{tab:addlabel}%
		\end{table}%
		 * */
		AnalysisSummaryRow totalRow = new AnalysisSummaryRow();
		
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%Analysis Summary START\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\\begin{table}[htbp]\n");
		writer.write("\\centering\n");
		writer.write("\\caption{Analysis Summary}\n");
		writer.write("\\label{tab:AnalysisSummary}\n");
		writer.write("\\begin{tabular}{cccccccccccccc}\n");
		writer.write("\\toprule\n");
		writer.write("Application & \\# of Files & \\# of Lines & \\multicolumn{4}{c}{\\# of Tainted Sinks} & \\multicolumn{4}{c}{\\# of Vulnerabilities} & Analysis Time \\\\\n");
		writer.write("\\midrule\n");
		writer.write(" & & & SQLI & XSS & MFE & Total & SQLI & XSS & MFE & Total \\\\\n");
		
		
		for(Entry<String, AnalysisSummaryRow> entry:table.entrySet())
		{
			AnalysisSummaryRow row = entry.getValue();
			totalRow.numOfFile+=row.numOfFile;
			totalRow.numOfLines += row.numOfLines;
			totalRow.numOfSqliSinks += row.numOfSqliSinks;
			totalRow.numOfXssSinks += row.numOfXssSinks;
			totalRow.numOfMfeSinks += row.numOfMfeSinks;
			totalRow.numOfSqliVulns += row.numOfSqliVulns;
			totalRow.numOfXssVulns += row.numOfXssVulns;
			totalRow.numOfMfeVulns += row.numOfMfeVulns;
			totalRow.taintTime += row.taintTime;
			totalRow.sanitTime += row.sanitTime;
			
			
			writer.write(Utility.escapLatex(row.projectName)+" & "+row.numOfFile+" & "+row.numOfLines+" & "+row.numOfSqliSinks+" & "+row.numOfXssSinks+" & "+row.numOfMfeSinks+" & "+row.getTotalSinks()+" & "+row.numOfSqliVulns+" & "+row.numOfXssVulns+" & "+row.numOfMfeVulns+" & "+row.getTotalVulns()+" & "+row.getTotalTime()+" \\\\\n");
		}
		
		writer.write("\\bottomrule\n");
		writer.write("Total & "+totalRow.numOfFile+" & "+totalRow.numOfLines+" & "+totalRow.numOfSqliSinks+" & "+totalRow.numOfXssSinks+" & "+totalRow.numOfMfeSinks+" & "+totalRow.getTotalSinks()+" & "+totalRow.numOfSqliVulns+" & "+totalRow.numOfXssVulns+" & "+totalRow.numOfMfeVulns+" & "+totalRow.getTotalVulns()+" & "+totalRow.getTotalTime()+" \\\\\n");
		writer.write("\\hline");
		writer.write("\\end{tabular}%\n");
		
		writer.write("\\end{table}%\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%Analysis Summary END\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\n\n\n\n");
		
		
	}
	
	private LinkedHashMap<String, AutoSummaryRow> getBlackAutoSummaryTable(Summary summary)
	{
		LinkedHashMap<String, AutoSummaryRow> retVal = new LinkedHashMap<>();
		for(Summary.Project proj : summary.projects)
		{
			if(!retVal.containsKey(proj.projectName))
			{
				retVal.put(proj.projectName, new AutoSummaryRow());
				
			}
			AutoSummaryRow row = retVal.get(proj.projectName);
			row.projectName = proj.projectName;
			Map<String,Integer> nums = proj.getNumOfBlackAuto();
			Map<String,Integer> bdds = proj.getBlackAutoBdd();
			Map<String,Integer> states = proj.getBlackAutoState();
			row.numOfSqliAuto+=nums.get("SQLI");
			row.numOfXssAuto+=nums.get("XSS");
			row.numOfMfeAuto+=nums.get("MFE");
			row.sqliBdd+=bdds.get("SQLI");
			row.xssBdd+=bdds.get("XSS");
			row.mfeBdd+=bdds.get("MFE");
			row.sqliState+=states.get("SQLI");
			row.xssState+=states.get("XSS");
			row.mfeState+=states.get("MFE");
					
			
		}
		return retVal;
		
	}
	private LinkedHashMap<String, AutoSummaryRow> getWhiteAutoSummaryTable(Summary summary)
	{
		LinkedHashMap<String, AutoSummaryRow> retVal = new LinkedHashMap<>();
		for(Summary.Project proj : summary.projects)
		{
			if(!retVal.containsKey(proj.projectName))
			{
				retVal.put(proj.projectName, new AutoSummaryRow());
				
			}
			AutoSummaryRow row = retVal.get(proj.projectName);
			row.projectName = proj.projectName;
			Map<String,Integer> nums = proj.getNumOfWhiteAuto();
			Map<String,Integer> bdds = proj.getWhiteAutoBdd();
			Map<String,Integer> states = proj.getWhiteAutoState();
			row.numOfSqliAuto+=nums.get("SQLI");
			row.numOfXssAuto+=nums.get("XSS");
			row.numOfMfeAuto+=nums.get("MFE");
			row.sqliBdd+=bdds.get("SQLI");
			row.xssBdd+=bdds.get("XSS");
			row.mfeBdd+=bdds.get("MFE");
			row.sqliState+=states.get("SQLI");
			row.xssState+=states.get("XSS");
			row.mfeState+=states.get("MFE");
					
			
		}
		return retVal;
		
	}
	
	private void appendAutoSummaryTable(BufferedWriter writer,Map<String,AutoSummaryRow> blackTable,Map<String,AutoSummaryRow> whiteTable) throws IOException
	{
		/*
		\begin{table}[htbp]
		  \centering
		  \caption{Black Automata}
		    \begin{tabular}{cccccccccc}
		    \toprule
		    Application & \multicolumn{9}{c}{Black Automata} \\
		    \midrule
		          & \multicolumn{3}{c}{SQLI} & \multicolumn{3}{c}{XSS} & \multicolumn{3}{c}{MFE} \\
		          & \#    & BDD   & State & \#    & BDD   & State & \#    & BDD   & State \\
		    benchmarks & 0     & 0     & 0     & 3     & 34531 & 1418  & 0     & 0     & 0 \\
		    e107  & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 0 \\
		    examples & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 0 \\
		    market & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 0 \\
		    moodle1\_6 & 0     & 0     & 0     & 26    & 29529 & 1339  & 18    & 832   & 73 \\
		    nucleus3.64 & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 0     & 0 \\
		    PBLGuestbook & 6     & 624   & 76    & 0     & 0     & 0     & 2     & 120   & 16 \\
		    php-fusion-6-01-18 & 38    & 122459 & 4918  & 18    & 91334 & 5450  & 66    & 3610  & 322 \\
		    schoolmate & 260   & 290977 & 13113 & 50    & 616798 & 28937 & 0     & 0     & 0 \\
		    sendcard\_3-4-1 & 1     & 423   & 44    & 5     & 684   & 64    & 2     & 802   & 52 \\
		    servoo & 1     & 123   & 14    & 0     & 0     & 0     & 1     & 60    & 7 \\
		    \bottomrule
		    \end{tabular}%
		  \label{tab:addlabel}%
		\end{table}%
		 * */
		AutoSummaryRow blackTotal = new AutoSummaryRow();
		AutoSummaryRow whiteTotal = new AutoSummaryRow();
		
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%Black Automata Summary START\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\\begin{table}[htbp]\n");
		writer.write("\\centering\n");
		writer.write("\\caption{Black Automata}\n");
		writer.write("\\label{tab:BlackAutomata}\n");
		writer.write("\\begin{tabular}{cccccccccc}\n");
		writer.write("\\toprule\n");
		writer.write("Application & \\multicolumn{9}{c}{Black Automata} \\\\\n");
		writer.write("\\midrule\n");
		writer.write(" & \\multicolumn{3}{c}{SQLI} & \\multicolumn{3}{c}{XSS} & \\multicolumn{3}{c}{MFE} \\\\\n");
		writer.write(" & \\# & BDD & State & \\# & BDD & State & \\# & BDD & State \\\\\n");
		
	
		for(Entry<String, AutoSummaryRow> entry:blackTable.entrySet())
		{
			AutoSummaryRow row = entry.getValue();
			writer.write(Utility.escapLatex(row.projectName)+" & "+row.numOfSqliAuto+" & "+row.sqliBdd+" & "+row.sqliState+" & "+row.numOfXssAuto+" & "+row.xssBdd+" & "+row.xssState+" & "+row.numOfMfeAuto+" & "+row.mfeBdd+" & "+row.mfeState+" \\\\\n");
			blackTotal.numOfSqliAuto+=row.numOfSqliAuto;
			blackTotal.sqliBdd+=row.sqliBdd;
			blackTotal.sqliState+=row.sqliState;
			
			blackTotal.numOfXssAuto+=row.numOfXssAuto;
			blackTotal.xssBdd+=row.xssBdd;
			blackTotal.xssState+=row.xssState;
			
			blackTotal.numOfMfeAuto+=row.numOfMfeAuto;
			blackTotal.mfeBdd+=row.mfeBdd;
			blackTotal.mfeState+=row.mfeState;
			
			
			
		}
		
		
		writer.write("\\bottomrule\n");
		writer.write("Total & "+blackTotal.numOfSqliAuto+" & "+blackTotal.sqliBdd+" & "+blackTotal.sqliState+" & "+blackTotal.numOfXssAuto+" & "+blackTotal.xssBdd+" & "+blackTotal.xssState+" & "+blackTotal.numOfMfeAuto+" & "+blackTotal.mfeBdd+" & "+blackTotal.mfeState+" \\\\\n");
		writer.write("\\hline");
		writer.write("\\end{tabular}%\n");
		
		
		writer.write("\\end{table}%\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%Black Automata Summary END\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\n\n\n\n");
		
		
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%White Automata Summary START\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\\begin{table}[htbp]\n");
		writer.write("\\centering\n");
		writer.write("\\caption{White Automata}\n");
		writer.write("\\label{tab:WhiteAutomata}\n");
		writer.write("\\begin{tabular}{cccccccccc}\n");
		writer.write("\\toprule\n");
		writer.write("Application & \\multicolumn{9}{c}{White Automata} \\\\\n");
		writer.write("\\midrule\n");
		writer.write(" & \\multicolumn{3}{c}{SQLI} & \\multicolumn{3}{c}{XSS} & \\multicolumn{3}{c}{MFE} \\\\\n");
		writer.write(" & \\# & BDD & State & \\# & BDD & State & \\# & BDD & State \\\\\n");
		
	
		for(Entry<String, AutoSummaryRow> entry:whiteTable.entrySet())
		{
			AutoSummaryRow row = entry.getValue();
			writer.write(Utility.escapLatex(row.projectName)+" & "+row.numOfSqliAuto+" & "+row.sqliBdd+" & "+row.sqliState+" & "+row.numOfXssAuto+" & "+row.xssBdd+" & "+row.xssState+" & "+row.numOfMfeAuto+" & "+row.mfeBdd+" & "+row.mfeState+" \\\\\n");
			
			whiteTotal.numOfSqliAuto+=row.numOfSqliAuto;
			whiteTotal.sqliBdd+=row.sqliBdd;
			whiteTotal.sqliState+=row.sqliState;
			
			whiteTotal.numOfXssAuto+=row.numOfXssAuto;
			whiteTotal.xssBdd+=row.xssBdd;
			whiteTotal.xssState+=row.xssState;
			
			whiteTotal.numOfMfeAuto+=row.numOfMfeAuto;
			whiteTotal.mfeBdd+=row.mfeBdd;
			whiteTotal.mfeState+=row.mfeState;
			
		}
		
		
		writer.write("\\bottomrule\n");
		writer.write("Total & "+whiteTotal.numOfSqliAuto+" & "+whiteTotal.sqliBdd+" & "+whiteTotal.sqliState+" & "+whiteTotal.numOfXssAuto+" & "+whiteTotal.xssBdd+" & "+whiteTotal.xssState+" & "+whiteTotal.numOfMfeAuto+" & "+whiteTotal.mfeBdd+" & "+whiteTotal.mfeState+" \\\\\n");
		writer.write("\\hline");
		writer.write("\\end{tabular}%\n");
		
		writer.write("\\end{table}%\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%White Automata Summary END\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\n\n\n\n");
		
		
	}
	private LinkedHashMap<String, TaintProgressRow> getTaintProgressTable(Summary summary)
	{
		LinkedHashMap<String, TaintProgressRow> retVal = new LinkedHashMap<>();
		for(Summary.Project proj : summary.projects)
		{
			if(!retVal.containsKey(proj.projectName))
			{
				retVal.put(proj.projectName, new TaintProgressRow());
				
			}
			TaintProgressRow row = retVal.get(proj.projectName);
			row.projectName = proj.projectName;
			Map<String, Integer> numOfTaintTask = proj.getNumOfTaintTask();
			row.numOfSqliTaintTask += numOfTaintTask.get("SQLI");
			row.numOfXssTaintTask += numOfTaintTask.get("XSS");
			row.numOfMfeTaintTask += numOfTaintTask.get("MFE");
			
			Map<String,Integer> numOfParseFail = proj.getNumOfParseFail();
			Map<String,Integer> numOfTaintComplete = proj.getNumOfTaintComplete();
			
			row.sqliParseFail+=numOfParseFail.get("SQLI");
			row.xssParseFail+=numOfParseFail.get("XSS");
			row.mfeParseFail+=numOfParseFail.get("MFE");
					
			row.sqliComplete+=numOfTaintComplete.get("SQLI");
			row.xssComplete+=numOfTaintComplete.get("XSS");
			row.mfeComplete+=numOfTaintComplete.get("MFE");
			
			
		}
		return retVal;
	}
	private String getTaintProgressCSVContent(Map<String, TaintProgressRow> table)
	{
		StringBuilder retVal = new StringBuilder();
		TaintProgressRow total = new TaintProgressRow();
		
		for(Entry<String,TaintProgressRow> row : table.entrySet())
		{
			total.numOfSqliTaintTask+=row.getValue().numOfSqliTaintTask;
			total.numOfXssTaintTask+=row.getValue().numOfXssTaintTask;
			total.numOfMfeTaintTask+=row.getValue().numOfMfeTaintTask;
			total.sqliParseFail+=row.getValue().sqliParseFail;
			total.sqliComplete+=row.getValue().sqliComplete;
			total.xssParseFail+=row.getValue().xssParseFail;
			total.xssComplete+=row.getValue().xssComplete;
			total.mfeParseFail+=row.getValue().mfeParseFail;
			total.mfeComplete+=row.getValue().mfeComplete;
		}
		
		retVal.append("\\begin{filecontents}{sqliTaintProgress.csv}\n");
		retVal.append("Name,Quantity\n");
		retVal.append("Parse Fail ("+Math.round(((float)total.sqliParseFail/total.numOfSqliTaintTask)*100)+" \\% ),"+total.sqliParseFail+"\n");
		retVal.append("Complete ("+Math.round(((float)total.sqliComplete/total.numOfSqliTaintTask)*100)+" \\% ),"+total.sqliComplete+"\n");
		retVal.append("\\end{filecontents}\n");
		retVal.append("\\DTLloaddb{sqliTaintProgress}{sqliTaintProgress.csv}\n");
		
		retVal.append("\\begin{filecontents}{xssTaintProgress.csv}\n");
		retVal.append("Name,Quantity\n");
		retVal.append("Parse Fail ("+Math.round(((float)total.xssParseFail/total.numOfXssTaintTask)*100)+" \\% ),"+total.xssParseFail+"\n");
		retVal.append("Complete ("+Math.round(((float)total.xssComplete/total.numOfXssTaintTask)*100)+" \\% ),"+total.xssComplete+"\n");
		retVal.append("\\end{filecontents}\n");
		retVal.append("\\DTLloaddb{xssTaintProgress}{xssTaintProgress.csv}\n");
		
		retVal.append("\\begin{filecontents}{mfeTaintProgress.csv}\n");
		retVal.append("Name,Quantity\n");
		retVal.append("Parse Fail ("+Math.round(((float)total.mfeParseFail/total.numOfMfeTaintTask)*100)+" \\% ),"+total.mfeParseFail+"\n");
		retVal.append("Complete ("+Math.round(((float)total.mfeComplete/total.numOfMfeTaintTask)*100)+" \\% ),"+total.mfeComplete+"\n");
		retVal.append("\\end{filecontents}\n");
		retVal.append("\\DTLloaddb{mfeTaintProgress}{mfeTaintProgress.csv}\n");
		
		return retVal.toString();
	}
	private String getSanitProgressCSVContent(Map<String, SanitProgressRow> table)
	{
		StringBuilder retVal = new StringBuilder();
		SanitProgressRow total = new SanitProgressRow();
		for(Entry<String,SanitProgressRow> row : table.entrySet())
		{
			total.numOfSqliSink+=row.getValue().numOfSqliSink;
			total.numOfXssSink+=row.getValue().numOfXssSink;
			total.numOfMfeSink+=row.getValue().numOfMfeSink;
			total.numOfForwardFail+=row.getValue().numOfForwardFail;
			total.numOfBackwardFail+=row.getValue().numOfBackwardFail;
			total.numOfPatchFail+=row.getValue().numOfPatchFail;
			total.numOfSanitComplete+=row.getValue().numOfSanitComplete;
		}
		
		retVal.append("\\begin{filecontents}{sanitProgress.csv}\n");
		retVal.append("Name,Quantity\n");
		retVal.append("Forward Fail ("+Math.round(((float)total.numOfForwardFail/total.getAggregateSinks())*100)+" \\% ),"+total.numOfForwardFail+"\n");
		retVal.append("Backward Fail ("+Math.round(((float)total.numOfBackwardFail/total.getAggregateSinks())*100)+" \\% ),"+total.numOfBackwardFail+"\n");
		retVal.append("Patch Fail ("+Math.round(((float)total.numOfPatchFail/total.getAggregateSinks())*100)+" \\% ),"+total.numOfPatchFail+"\n");
		retVal.append("Complete ("+Math.round(((float)total.numOfSanitComplete/total.getAggregateSinks())*100)+" \\% ),"+total.numOfSanitComplete+"\n");
		
		retVal.append("\\end{filecontents}\n");
		retVal.append("\\DTLloaddb{sanitProgress}{sanitProgress.csv}\n");
		
		
		return retVal.toString();
	}
	private LinkedHashMap<String, SanitProgressRow> getSanitProgressTable(Summary summary)
	{
		LinkedHashMap<String, SanitProgressRow> retVal = new LinkedHashMap<>();
		for(Summary.Project proj : summary.projects)
		{
			if(!retVal.containsKey(proj.projectName))
			{
				retVal.put(proj.projectName, new SanitProgressRow());
				
			}
			SanitProgressRow row = retVal.get(proj.projectName);
			row.projectName = proj.projectName;
			Map<String, Integer> numOfSinks = proj.getNumOfSinks();
			if(row.numOfSqliSink==0)
			{
				row.numOfSqliSink += numOfSinks.get("SQLI");
			}
			if(row.numOfXssSink==0)
			{
				row.numOfXssSink += numOfSinks.get("XSS");
			}
			if(row.numOfMfeSink==0)
			{
				row.numOfMfeSink += numOfSinks.get("MFE");
			}
			
			Map<String,Integer> numOfForwardFail = proj.getNumOfForwardFail();
			Map<String,Integer> numOfBackwardFail = proj.getNumOfBackwardFail();
			Map<String,Integer> numOfPatchFail = proj.getNumOfPatchFail();
			Map<String,Integer> numOfSanitComplete = proj.getNumOfSanitComplete();
			
			row.numOfForwardFail+=numOfForwardFail.get("SQLI")+numOfForwardFail.get("XSS")+numOfForwardFail.get("MFE");
			row.numOfBackwardFail+=numOfBackwardFail.get("SQLI")+numOfBackwardFail.get("XSS")+numOfBackwardFail.get("MFE");
			row.numOfPatchFail+=numOfPatchFail.get("SQLI")+numOfPatchFail.get("XSS")+numOfPatchFail.get("MFE");
			row.numOfSanitComplete+=numOfSanitComplete.get("SQLI")+numOfSanitComplete.get("XSS")+numOfSanitComplete.get("MFE");
		}
		return retVal;
	}
	private Map<String, Integer> getReasonOfForwardFail(Summary summary)
	{
		Map<String, Integer> retVal = new HashMap<>();
		for(Summary.Project proj : summary.projects)
		{
			Map<String, Integer> reasons = proj.getReasonOfForwardFail();
			for(Entry<String, Integer> reason : reasons.entrySet())
			{
				if(!retVal.containsKey(reason.getKey()))
				{
					retVal.put(reason.getKey(), 0);
					
				}
				
				retVal.put(reason.getKey(),retVal.get(reason.getKey())+reason.getValue());
			}
			
		}
		return retVal;
	}
	private Map<String, Integer> getReasonOfBackwardFail(Summary summary)
	{
		Map<String, Integer> retVal = new HashMap<>();
		for(Summary.Project proj : summary.projects)
		{
			Map<String, Integer> reasons = proj.getReasonOfBackwardFail();
			for(Entry<String, Integer> reason : reasons.entrySet())
			{
				if(!retVal.containsKey(reason.getKey()))
				{
					retVal.put(reason.getKey(), 0);
					
				}
				
				retVal.put(reason.getKey(),retVal.get(reason.getKey())+reason.getValue());
			}
			
		}
		return retVal;
	}
	private Map<String, Integer> getReasonOfPatchFail(Summary summary)
	{
		Map<String, Integer> retVal = new HashMap<>();
		for(Summary.Project proj : summary.projects)
		{
			Map<String, Integer> reasons = proj.getReasonOfPatchFail();
			for(Entry<String, Integer> reason : reasons.entrySet())
			{
				if(!retVal.containsKey(reason.getKey()))
				{
					retVal.put(reason.getKey(), 0);
					
				}
				
				retVal.put(reason.getKey(),retVal.get(reason.getKey())+reason.getValue());
			}
			
		}
		return retVal;
	}
	private void appendTaintProgressTable(BufferedWriter writer,Map<String,TaintProgressRow> table) throws IOException
	{
		/*
		\begin{table}[htbp]
		  \centering
		  \caption{Taint Analysis Progress}
		    \begin{tabular}{ccccccc}
		    \toprule
		    Application & \multicolumn{6}{c}{Taint Analysis} \\
		    \midrule
		          & \multicolumn{2}{c}{SQLI} & \multicolumn{2}{c}{XSS} & \multicolumn{2}{c}{MFE} \\
		          & Parse Fail & Complete & Parse Fail & Complete & Parse Fail & Complete \\
		    benchmarks & 0     & 10    & 0     & 10    & 0     & 10 \\
		    e107  & 6     & 212   & 6     & 212   & 6     & 212 \\
		    examples & 0     & 5     & 0     & 5     & 0     & 5 \\
		    market & 0     & 22    & 0     & 22    & 0     & 22 \\
		    moodle1\_6 & 27    & 1292  & 27    & 1292  & 27    & 1292 \\
		    nucleus3.64 & 1     & 66    & 1     & 66    & 1     & 66 \\
		    PBLGuestbook & 0     & 3     & 0     & 3     & 0     & 3 \\
		    php-fusion-6-01-18 & 2     & 1154  & 2     & 1154  & 2     & 1154 \\
		    schoolmate & 0     & 63    & 2     & 61    & 2     & 61 \\
		    sendcard\_3-4-1 & 0     & 72    & 0     & 72    & 0     & 72 \\
		    servoo & 0     & 26    & 0     & 26    & 0     & 26 \\
		    TOTAL & 36    & 2925  & 38    & 2923  & 38    & 2923 \\
		    \bottomrule
		    \end{tabular}%
		  \label{tab:addlabel}%
		\end{table}%
		 * */
		TaintProgressRow totalRow = new TaintProgressRow();
		
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%Taint Analysis Progress START\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\\begin{table}[htbp]\n");
		writer.write("\\centering\n");
		writer.write("\\caption{Taint Analysis Progress Summary}\n");
		writer.write("\\label{tab:TaintAnalysisProgressSummary}\n");
		writer.write("\\begin{tabular}{cccccccc}\n");
		writer.write("\\toprule\n");
		writer.write("Application & \\# of Task  & \\multicolumn{6}{c}{Taint Analysis} \\\\\n");
		writer.write("\\midrule\n");
		writer.write(" & & \\multicolumn{2}{c}{SQLI} & \\multicolumn{2}{c}{XSS} & \\multicolumn{2}{c}{MFE} \\\\\n");
		writer.write(" & & Parse Fail & Complete & Parse Fail & Complete & Parse Fail & Complete \\\\\n");
		
	
		for(Entry<String, TaintProgressRow> entry:table.entrySet())
		{
			TaintProgressRow row = entry.getValue();
			writer.write(Utility.escapLatex(row.projectName)+" & "+(row.getTotalTaintTasks())+" & "+row.sqliParseFail+" & "+row.sqliComplete+" & "+row.xssParseFail+" & "+row.xssComplete+" & "+row.mfeParseFail+" & "+row.mfeComplete+" \\\\\n");
			totalRow.numOfSqliTaintTask+=row.numOfSqliTaintTask;
			totalRow.numOfXssTaintTask+=row.numOfXssTaintTask;
			totalRow.numOfMfeTaintTask+=row.numOfMfeTaintTask;
			
			totalRow.sqliParseFail += row.sqliParseFail;
			totalRow.xssParseFail += row.xssParseFail;
			totalRow.mfeParseFail+=row.mfeParseFail;
			
			totalRow.sqliComplete += row.sqliComplete;
			totalRow.xssComplete += row.xssComplete;
			totalRow.mfeComplete+=row.mfeComplete;
			
			
			
		}
		
		
		
		writer.write("\\bottomrule\n");
		writer.write("Total & "+(totalRow.getTotalTaintTasks())+" & "+totalRow.sqliParseFail+" & "+totalRow.sqliComplete+" & "+totalRow.xssParseFail+" & "+totalRow.xssComplete+" & "+totalRow.mfeParseFail+" & "+totalRow.mfeComplete+" \\\\\n");
		writer.write("\\hline");
		writer.write("\\end{tabular}%\n");
		
		writer.write("\\end{table}%\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%White Automata Summary END\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\n\n\n\n");
		
		
	}
	private void appendSanitProgressTable(BufferedWriter writer,Map<String,SanitProgressRow> table) throws IOException
	{
		/*
		\begin{table}[htbp]
		  \centering
		  \caption{Sanitize Analysis Progress}
		    \begin{tabular}{cccccc}
		    \toprule
		    Application & \\# of Sinks & \multicolumn{4}{c}{Sanitize Analysis} \\
		    \midrule
		           & & Forward Fail & Backward Fail & Patch Fail & Complete \\
		    benchmarks & 0     & 1     & 1     & 6 \\
		    e107  & 0     & 0     & 0     & 0 \\
		    examples & 0     & 0     & 0     & 0 \\
		    market & 4     & 0     & 0     & 4 \\
		    moodle1\_6 & 533   & 0     & 21    & 2138 \\
		    nucleus3.64 & 37    & 0     & 0     & 22 \\
		    PBLGuestbook & 0     & 1     & 1     & 8 \\
		    php-fusion-6-01-18 & 43    & 124   & 40    & 840 \\
		    schoolmate & 3     & 52    & 23    & 268 \\
		    sendcard\_3-4-1 & 0     & 0     & 0     & 53 \\
		    servoo & 18    & 0     & 0     & 23 \\
		    TOTAL & 638   & 178   & 86    & 3362 \\
		    \bottomrule
		    \end{tabular}%
		  \label{tab:addlabel}%
		
		
		\end{table}%
		 * */
		SanitProgressRow totalRow = new SanitProgressRow();
		
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%Sanitize Analysis Progress START\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\\begin{table}[htbp]\n");
		writer.write("\\centering\n");
		writer.write("\\caption{Sanitize Analysis Progress Summary}\n");
		writer.write("\\label{tab:SanitizeAnalysisProgressSummary}\n");
		writer.write("\\begin{tabular}{cccccc}\n");
		writer.write("\\toprule\n");
		writer.write("Application & \\# of Sinks & \\multicolumn{4}{c}{Sanitize Analysis} \\\\\n");
		writer.write("\\midrule\n");
		
		writer.write(" & & Forward Fail & Backward Fail & Patch Fail & Complete \\\\\n");
		
	
		for(Entry<String, SanitProgressRow> entry:table.entrySet())
		{
			SanitProgressRow row = entry.getValue();
			writer.write(Utility.escapLatex(row.projectName)+" & "+row.getTotalSinks()+" & "+row.numOfForwardFail+" & "+row.numOfBackwardFail+" & "+row.numOfPatchFail+" & "+row.numOfSanitComplete+" \\\\\n");
			
			totalRow.numOfSqliSink += row.numOfSqliSink;
			totalRow.numOfXssSink += row.numOfXssSink;
			totalRow.numOfMfeSink += row.numOfMfeSink;
			
			totalRow.numOfForwardFail += row.numOfForwardFail;
			totalRow.numOfBackwardFail += row.numOfBackwardFail;
			totalRow.numOfPatchFail += row.numOfPatchFail;
			totalRow.numOfSanitComplete += row.numOfSanitComplete;
			
			
		}
		
		
		
		writer.write("\\bottomrule\n");
		writer.write("Total & "+totalRow.getTotalSinks()+" & "+totalRow.numOfForwardFail+" & "+totalRow.numOfBackwardFail+" & "+totalRow.numOfPatchFail+" & "+totalRow.numOfSanitComplete+" \\\\\n");
		writer.write("\\hline");
		writer.write("\\end{tabular}%\n");
		writer.write("\\end{table}%\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%Sanitize Ayalysis Progress  END\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\n\n\n\n");
		
		
	}
	private void appendTaintProgressPie(Writer writer) throws IOException
	{
		/*
		\begin{figure}[h!]
		\DTLpiechart{variable=\quantity,outerlabel=\name}{fruit}{\name=Name,\quantity=Quantity}
		\caption{A pie chart}
		\end{figure}
		 */
		
		writer.write("\\begin{figure}[h]\n");
		writer.write("\\DTLpiechart{variable=\\quantity,outerlabel=\\name}{sqliTaintProgress}{\\name=Name,\\quantity=Quantity}\n");
		writer.write("\\caption{\\label{fig:SqliTaintAnalysisProgress}SQLI Taint Analysis Progress (\\# of Task)}\n");
		
		writer.write("\\end{figure}\n");
		
		writer.write("\\begin{figure}[h]\n");
		writer.write("\\DTLpiechart{variable=\\quantity,outerlabel=\\name}{xssTaintProgress}{\\name=Name,\\quantity=Quantity}\n");
		writer.write("\\caption{\\label{fig:XssTaintAnalysisProgress}XSS Taint Analysis Progress (\\# of Task)}\n");
		writer.write("\\end{figure}\n");
		
		writer.write("\\begin{figure}[h]\n");
		writer.write("\\DTLpiechart{variable=\\quantity,outerlabel=\\name}{mfeTaintProgress}{\\name=Name,\\quantity=Quantity}\n");
		writer.write("\\caption{\\label{fig:MfeTaintAnalysisProgress}MFE Taint Analysis Progress (\\# of Task)}\n");
		writer.write("\\end{figure}\n");
		
		
	}
	private void appendSanitProgressPie(Writer writer) throws IOException
	{
		/*
		\begin{figure}[h!]
		\DTLpiechart{variable=\quantity,outerlabel=\name}{fruit}{\name=Name,\quantity=Quantity}
		\caption{A pie chart}
		\end{figure}
		 */
		
		writer.write("\\begin{figure}[h]\n");
		writer.write("\\DTLpiechart{variable=\\quantity,outerlabel=\\name}{sanitProgress}{\\name=Name,\\quantity=Quantity}\n");
		writer.write("\\caption{\\label{fig:SanitAnalysisProgress}Sanit Analysis Progress}\n");
		writer.write("\\end{figure}\n");
		
		
	}
	private void appendFailReasonTable(BufferedWriter writer,Map<String,Integer> forwardFailReason,Map<String,Integer> backwardFailReason,Map<String,Integer> patchFailReason) throws IOException
	{
		
		AnalysisSummaryRow totalRow = new AnalysisSummaryRow();
		
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%Fail Reasons START\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\\begin{table}[htbp]\n");
		writer.write("\\centering\n");
		writer.write("\\caption{Fail Reasons}\n");
		writer.write("\\label{tab:FailReasons}\n");
		writer.write("\\begin{tabular}{ccccccc}\n");
		writer.write("\\toprule\n");
		writer.write("Reason & \\multicolumn{2}{c}{Forward Fail} & \\multicolumn{2}{c}{Backward Fail} & \\multicolumn{2}{c}{Patch Fail} \\\\\n");
		writer.write("\\midrule\n");
		writer.write(" & \\# & Ratio & \\# & Ratio & \\# & Ratio \\\\\n");
		
		String[] terminateTypes = new String[]{"OUT_OF_MEM","SELF_TERMINATE","TIMEOUT"};
		
		int totalForwardFail = 0;
		int totalBackwardFail =0;
		int totalPatchFail = 0;
		
		for(Entry<String, Integer> reason : forwardFailReason.entrySet())
		{
			totalForwardFail+= reason.getValue();
		}
		for(Entry<String, Integer> reason : backwardFailReason.entrySet())
		{
			totalBackwardFail+= reason.getValue();
		}
		for(Entry<String, Integer> reason : patchFailReason.entrySet())
		{
			totalPatchFail+= reason.getValue();
		}
		
//		for(String terminateType : terminateTypes)
//		{
//			//forward
//			for(Entry<String, Integer> reason : forwardFailReason)
//			{
//				if(!total.containsKey(reason.getKey()))
//				{
//					total.put(reason.getKey(), 0);
//				}
//				total.put(reason.getKey(), total.get(reason.getKey())+reason.getValue());
//			}
//			//backward
//			for(Entry<String, Integer> reason : backwardFailReason)
//			{
//				if(!total.containsKey(reason.getKey()))
//				{
//					total.put(reason.getKey(), 0);
//				}
//				total.put(reason.getKey(), total.get(reason.getKey())+reason.getValue());
//			}
//			//patch
//			for(Entry<String, Integer> reason : patchFailReason)
//			{
//				if(!total.containsKey(reason.getKey()))
//				{
//					total.put(reason.getKey(), 0);
//				}
//				total.put(reason.getKey(), total.get(reason.getKey())+reason.getValue());
//			}
//			
//		}
		
		for(String terminateType : terminateTypes)
		{
			int forwardNum = forwardFailReason.containsKey(terminateType)?forwardFailReason.get(terminateType):0;
			int backwardNum = backwardFailReason.containsKey(terminateType)?backwardFailReason.get(terminateType):0;
			int patchNum = patchFailReason.containsKey(terminateType)?patchFailReason.get(terminateType):0;
			float forwardRatio = Math.round((totalForwardFail!=0?(float)forwardNum/totalForwardFail:0)*100);
			float backwardRatio = Math.round(( totalBackwardFail!=0?(float)backwardNum/totalBackwardFail:0)*100);
			float patcherRatio = Math.round((totalPatchFail!=0?(float)patchNum/totalPatchFail:0)*100);
			writer.write(Utility.escapLatex(terminateType)+" & "+ forwardNum +" & "+forwardRatio + "\\% & "+backwardNum+" & "+backwardRatio + "\\% & "+patchNum+" & "+patcherRatio +"\\% \\\\\n");
			
		}
		
		
		
		writer.write("\\hline");
		writer.write("\\end{tabular}%\n");
		
		writer.write("\\end{table}%\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("%Fail Reasons END\n");
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		writer.write("\n\n\n\n");
		
		
	}
	
	public class Summary
	{
		
		List<Project> projects;
		public Summary()
		{
			this.projects = new ArrayList<>();
		}
		public class Project extends PatcherProject
		{
			public List<File> files;
			
			public Project()
			{
				this.files = new ArrayList<>();
			}
			public Map<String,Integer> getNumOfTaintTask()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.File file : files)
				{
					for(Entry<String, Integer> numOfTaintTask : file.getNumOfTaintTask().entrySet())
					{
						retVal.put(numOfTaintTask.getKey(), retVal.get(numOfTaintTask.getKey())+numOfTaintTask.getValue());
					}
				}
				return retVal;
			}
			public int getNumOfLines()
			{
				int retVal =0;
				for(Summary.File file : files)
				{
					retVal+=file.lines;
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfSinks()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.File file : files)
				{
					for(Entry<String, Integer> numOfSink : file.getNumOfSinks().entrySet())
					{
						retVal.put(numOfSink.getKey(), retVal.get(numOfSink.getKey())+numOfSink.getValue());
					}
					
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfVulns()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.File file : files)
				{
					for(Entry<String, Integer> numOfSink : file.getNumOfVulns().entrySet())
					{
						retVal.put(numOfSink.getKey(), retVal.get(numOfSink.getKey())+numOfSink.getValue());
					}
				}
				return retVal;
			}
			public long getSanitTime()
			{
				int retVal = 0;
				for(Summary.File file : files)
				{
					retVal+=file.getSanitTime();
					
				}
				return retVal;
			}
			public long getTaintTime()
			{
				int retVal = 0;
				for(Summary.File file : files)
				{
					retVal+=file.getTaintTime();
					
				}
				return retVal;
			}
			public long getAnalysisTime()
			{
				int retVal = 0;
				for(Summary.File file : files)
				{
					retVal+=file.getAnalysisTime();
					
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfBlackAuto()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.File file : files)
				{
					for(Entry<String, Integer> entry : file.getNumOfBlackAuto().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
					
					
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfWhiteAuto()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.File file : files)
				{
					for(Entry<String, Integer> entry : file.getNumOfWhiteAuto().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
						
				}
				return retVal;
			}
			public Map<String,Integer> getBlackAutoBdd()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.File file : files)
				{
					for(Entry<String, Integer> entry : file.getBlackAutoBdd().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
					
					
				}
				return retVal;
			}
			public Map<String,Integer> getWhiteAutoBdd()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.File file : files)
				{
					for(Entry<String, Integer> entry : file.getWhiteAutoBdd().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
						
				}
				return retVal;
			}
			public Map<String,Integer> getBlackAutoState()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.File file : files)
				{
					for(Entry<String, Integer> entry : file.getBlackAutoState().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
					
					
				}
				return retVal;
			}
			public Map<String,Integer> getWhiteAutoState()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.File file : files)
				{
					
					for(Entry<String, Integer> entry : file.getWhiteAutoState().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
					
				}
				return retVal;
			}
			
			public Map<String,Integer> getNumOfParseFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.File file : files)
				{
					
					for(Entry<String, Integer> entry : file.getNumOfParseFail().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
				}
				return retVal;
			}
			
			public Map<String,Integer> getNumOfTaintComplete()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.File file : files)
				{
					
					for(Entry<String, Integer> entry : file.getNumOfTaintComplete().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfForwardFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.File file : files)
				{
					
					for(Entry<String, Integer> entry : file.getNumOfForwardFail().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
				}
				return retVal;
			}
			public Map<String, Integer> getReasonOfForwardFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				for(Summary.File file : files)
				{
					Map<String, Integer> reasons = file.getReasonOfForwardFail();
					for(Entry<String,Integer> reason : reasons.entrySet())
					{
						if(!retVal.containsKey(reason.getKey()))
						{
							retVal.put(reason.getKey(), 0);
						}
						retVal.put(reason.getKey(), retVal.get(reason.getKey())+reason.getValue());
						
					}
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfBackwardFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.File file : files)
				{
					
					for(Entry<String, Integer> entry : file.getNumOfBackwardFail().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
				}
				return retVal;
			}
			public Map<String, Integer> getReasonOfBackwardFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				for(Summary.File file : files)
				{
					Map<String, Integer> reasons = file.getReasonOfBackwardFail();
					for(Entry<String,Integer> reason : reasons.entrySet())
					{
						if(!retVal.containsKey(reason.getKey()))
						{
							retVal.put(reason.getKey(), 0);
						}
						retVal.put(reason.getKey(), retVal.get(reason.getKey())+reason.getValue());
						
					}
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfPatchFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.File file : files)
				{
					
					for(Entry<String, Integer> entry : file.getNumOfPatchFail().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
				}
				return retVal;
			}
			public Map<String, Integer> getReasonOfPatchFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				for(Summary.File file : files)
				{
					Map<String, Integer> reasons = file.getReasonOfPatchFail();
					for(Entry<String,Integer> reason : reasons.entrySet())
					{
						if(!retVal.containsKey(reason.getKey()))
						{
							retVal.put(reason.getKey(), 0);
						}
						retVal.put(reason.getKey(), retVal.get(reason.getKey())+reason.getValue());
						
					}
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfSanitComplete()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.File file : files)
				{
					
					for(Entry<String, Integer> entry : file.getNumOfSanitComplete().entrySet())
					{
						retVal.put(entry.getKey(), retVal.get(entry.getKey())+entry.getValue());
					}
				}
				return retVal;
			}
		}
		public class File extends PatcherFile
		{
			public int lines;
			public List<TaintTask> taintTasks;
			public File()
			{
				taintTasks = new ArrayList<>();
			}
			public Map<String,Integer> getNumOfTaintTask()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+1);
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfSinks()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.sinks.size());
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfVulns()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getNumOfVulns());
				}
				return retVal;
			}
			public long getSanitTime()
			{
				int retVal = 0;
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal+=taintTask.getSanitTime();
					
				}
				return retVal;
			}
			public long getTaintTime()
			{
				int retVal = 0;
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal+=taintTask.taintTime;
					
				}
				return retVal;
			}
			public long getAnalysisTime()
			{
				int retVal = 0;
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal+=taintTask.getAnalysisTime();
					
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfBlackAuto()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getNumOfBlackAuto());
					
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfWhiteAuto()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getNumOfWhiteAuto());
						
				}
				return retVal;
			}
			public Map<String,Integer> getBlackAutoBdd()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getBlackAutoBdd());
					
					
				}
				return retVal;
			}
			public Map<String,Integer> getWhiteAutoBdd()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getWhiteAutoBdd());
						
				}
				return retVal;
			}
			public Map<String,Integer> getBlackAutoState()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getBlackAutoState());
					
					
				}
				return retVal;
			}
			public Map<String,Integer> getWhiteAutoState()
			{
				Map<String,Integer> retVal =new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				for(Summary.TaintTask taintTask : taintTasks)
				{
					
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getWhiteAutoState());
					
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfParseFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.TaintTask taintTask : taintTasks)
				{
					if(!taintTask.assigned)
					{
						continue;
					}
					if(taintTask.statusCode!=StatusCode.TAINT_COMPLETE)
					{
						retVal.put(taintTask.taintType,retVal.get(taintTask.taintType)+1);
					}
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfTaintComplete()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.TaintTask taintTask : taintTasks)
				{
					if(!taintTask.assigned)
					{
						continue;
					}
					if(taintTask.statusCode==StatusCode.TAINT_COMPLETE)
					{
						retVal.put(taintTask.taintType,retVal.get(taintTask.taintType)+1);
					}
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfForwardFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getNumOfForwardFail());
				}
				return retVal;
			}
			public Map<String, Integer> getReasonOfForwardFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				for(Summary.TaintTask taintTask : taintTasks)
				{
					Map<String, Integer> reasons = taintTask.getReasonOfForwardFail();
					for(Entry<String,Integer> reason : reasons.entrySet())
					{
						if(!retVal.containsKey(reason.getKey()))
						{
							retVal.put(reason.getKey(), 0);
						}
						retVal.put(reason.getKey(), retVal.get(reason.getKey())+reason.getValue());
						
					}
				
				}
				return retVal;
			}
			
			
			
			public Map<String,Integer> getNumOfBackwardFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getNumOfBackwardFail());
				}
				return retVal;
			}
			
			public Map<String, Integer> getReasonOfBackwardFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				for(Summary.TaintTask taintTask : taintTasks)
				{
					Map<String, Integer> reasons = taintTask.getReasonOfBackwardFail();
					for(Entry<String,Integer> reason : reasons.entrySet())
					{
						if(!retVal.containsKey(reason.getKey()))
						{
							retVal.put(reason.getKey(), 0);
						}
						retVal.put(reason.getKey(), retVal.get(reason.getKey())+reason.getValue());
						
					}
				
				}
				return retVal;
			}
			
			public Map<String,Integer> getNumOfPatchFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getNumOfPatchFail());
				}
				return retVal;
			}
			public Map<String, Integer> getReasonOfPatchFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				for(Summary.TaintTask taintTask : taintTasks)
				{
					Map<String, Integer> reasons = taintTask.getReasonOfPatchFail();
					for(Entry<String,Integer> reason : reasons.entrySet())
					{
						if(!retVal.containsKey(reason.getKey()))
						{
							retVal.put(reason.getKey(), 0);
						}
						retVal.put(reason.getKey(), retVal.get(reason.getKey())+reason.getValue());
						
					}
				
				}
				return retVal;
			}
			public Map<String,Integer> getNumOfSanitComplete()
			{
				Map<String, Integer> retVal = new HashMap<>();
				retVal.put("SQLI", 0);
				retVal.put("XSS", 0);
				retVal.put("MFE", 0);
				
				for(Summary.TaintTask taintTask : taintTasks)
				{
					retVal.put(taintTask.taintType, retVal.get(taintTask.taintType)+taintTask.getNumOfSanitComplete());
					
				}
				return retVal;
			}
			
			
		}
		public class TaintTask extends PatcherTaintTask
		{
			public List<Sink> sinks;
			public TaintTask()
			{
				sinks = new ArrayList<>();
			}
			
			public int getNumOfVulns()
			{
				int retVal = 0;
				
				
				for(Summary.Sink sink : sinks)
				{
					if(!sink.isVuln)
					{
						continue;
					}
					retVal+=1;
					
				}
				return retVal;
			}
			public long getSanitTime()
			{
				int retVal = 0;
				for(Summary.Sink sink : sinks)
				{
					retVal+=sink.sanitTime;
					
				}
				return retVal;
			}
			public long getAnalysisTime()
			{
				return taintTime+getSanitTime();
			}
			
			public int getNumOfBlackAuto()
			{
				int retVal =0;
				
				for(Summary.Sink sink : sinks)
				{
					if(sink.isVuln && sink.blackAuto!=null)
					{
						retVal+=1;
					}
					
					
				}
				return retVal;
			}
			public int getNumOfWhiteAuto()
			{
				int retVal =0;
				for(Summary.Sink sink : sinks)
				{
					if(sink.isVuln && sink.whiteAuto!=null)
					{
						retVal+=1;
					}
					
				}
				return retVal;
			}
			public int getBlackAutoBdd()
			{
				int retVal =0;
				for(Summary.Sink sink : sinks)
				{
					if(sink.isVuln && sink.blackAuto!=null)
					{
						retVal+=sink.blackAuto.bdd;
					}
					
					
				}
				return retVal;
			}
			public int getWhiteAutoBdd()
			{
				int retVal =0;
				for(Summary.Sink sink : sinks)
				{
					if(sink.isVuln && sink.whiteAuto!=null)
					{
						retVal+=sink.whiteAuto.bdd;
						
					}
					
					
				}
				return retVal;
			}
			public int getBlackAutoState()
			{
				int retVal =0;
				for(Summary.Sink sink : sinks)
				{
					if(sink.isVuln && sink.blackAuto!=null)
					{
						retVal+=sink.blackAuto.state;
					}
					
					
				}
				return retVal;
			}
			public int getWhiteAutoState()
			{
				int retVal =0;
				for(Summary.Sink sink : sinks)
				{
					if(sink.isVuln && sink.whiteAuto!=null)
					{
						retVal+=sink.whiteAuto.state;
					}
					
					
				}
				return retVal;
			}
			public int getNumOfForwardFail()
			{
				int retVal = 0;
				
				
				for(Summary.Sink sink : sinks)
				{
					if(!sink.assigned)
					{
						continue;
					}
					if(sink.statusCode <= StatusCode.FORWARD_FAIL)
					{
						
						retVal+=1;
					}
				}
				return retVal;
			}
			public Map<String, Integer> getReasonOfForwardFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				
				for(Summary.Sink sink : sinks)
				{
					if(!sink.assigned)
					{
						continue;
					}
					
					if(sink.statusCode <= StatusCode.FORWARD_FAIL)
					{
						if(sink.terminateType==null)
						{
							sink.terminateType ="SELF_TERMINATE";
						}
						if(!retVal.containsKey(sink.terminateType))
						{
							retVal.put(sink.terminateType, 0);
						}
						retVal.put(sink.terminateType, retVal.get(sink.terminateType)+1);
					}
				}
				return retVal;
				
			}
			public int getNumOfBackwardFail()
			{
				int retVal = 0;
				
				
				for(Summary.Sink sink : sinks)
				{
					if(!sink.assigned)
					{
						continue;
					}
					if(sink.statusCode > StatusCode.FORWARD_FAIL && sink.statusCode <= StatusCode.BACKWARD_FAIL)
					{
						retVal+=1;
					}
				}
				return retVal;
			}
			public Map<String, Integer> getReasonOfBackwardFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				
				for(Summary.Sink sink : sinks)
				{
					if(!sink.assigned)
					{
						continue;
					}
					if(sink.statusCode > StatusCode.FORWARD_FAIL && sink.statusCode <= StatusCode.BACKWARD_FAIL)
					{
						if(sink.terminateType==null)
						{
							sink.terminateType ="SELF_TERMINATE";
						}
						if(!retVal.containsKey(sink.terminateType))
						{
							retVal.put(sink.terminateType, 0);
						}
						retVal.put(sink.terminateType, retVal.get(sink.terminateType)+1);
					}
				}
				return retVal;
				
			}
			public int getNumOfPatchFail()
			{
				int retVal = 0;
				
				
				for(Summary.Sink sink : sinks)
				{
					if(!sink.assigned)
					{
						continue;
					}
					if(sink.statusCode > StatusCode.BACKWARD_FAIL && sink.statusCode <= StatusCode.PATCH_FAIL)
					{
						retVal+=1;
					}
				}
				return retVal;
			}
			public Map<String, Integer> getReasonOfPatchFail()
			{
				Map<String, Integer> retVal = new HashMap<>();
				
				for(Summary.Sink sink : sinks)
				{
					if(!sink.assigned)
					{
						continue;
					}
					if(sink.statusCode > StatusCode.BACKWARD_FAIL && sink.statusCode <= StatusCode.PATCH_FAIL)
					{
						if(sink.terminateType==null)
						{
							sink.terminateType ="SELF_TERMINATE";
						}
						if(!retVal.containsKey(sink.terminateType))
						{
							retVal.put(sink.terminateType, 0);
						}
						retVal.put(sink.terminateType, retVal.get(sink.terminateType)+1);
					}
				}
				return retVal;
				
			}
			public int getNumOfSanitComplete()
			{
				int retVal = 0;
				
				
				for(Summary.Sink sink : sinks)
				{
					if(!sink.assigned)
					{
						continue;
					}
					if(sink.statusCode == StatusCode.SANIT_COMPLETE)
					{
						retVal+=1;
					}
				}
				return retVal;
			}
			
		}
		public class Sink extends PatcherSink
		{
			public PatcherAuto blackAuto;
			public PatcherAuto whiteAuto;
			
			
		}
		
		
		
	}
	public class AnalysisSummaryRow
	{
		public String projectName;
		public int numOfFile;
		public int numOfLines;
		public int numOfSqliSinks;
		public int numOfXssSinks;
		public int numOfMfeSinks;
		public int numOfSqliVulns;
		public int numOfXssVulns;
		public int numOfMfeVulns;
		public long taintTime;
		public long sanitTime;
		
		public int getTotalSinks()
		{
			return numOfSqliSinks+numOfXssSinks+numOfMfeSinks;
		}
		public int getTotalVulns()
		{
			return numOfSqliVulns+numOfXssVulns+numOfMfeVulns;
		}
		
		public long getTotalTime()
		{
			return taintTime+sanitTime;
		}
	}
	
	public class AutoSummaryRow
	{
		public String projectName;
		public int numOfSqliAuto;
		public int numOfXssAuto;
		public int numOfMfeAuto;
		public int sqliBdd;
		public int xssBdd;
		public int mfeBdd;
		public int sqliState;
		public int xssState;
		public int mfeState;
		
	}
	public class TaintProgressRow
	{
		public String projectName;
		//public int numOfFile;
		public int numOfSqliTaintTask;
		public int numOfXssTaintTask;
		public int numOfMfeTaintTask;
		public int sqliParseFail;
		public int xssParseFail;
		public int mfeParseFail;
		public int sqliComplete;
		public int xssComplete;
		public int mfeComplete;
		
		public int getTotalTaintTasks()
		{
			return numOfSqliTaintTask+numOfXssTaintTask+numOfMfeTaintTask;
		}
		
		
	}
	public class SanitProgressRow
	{
		public String projectName;
		public int numOfSqliSink;
		public int numOfXssSink;
		public int numOfMfeSink;
		public int getAggregateSinks()
		{
			// an aggregate sinks is calculate by re-counting a single sink because of analyzing with different atkPattern, while getTotalSinks() won't recount
			return numOfForwardFail+numOfBackwardFail+numOfPatchFail+numOfSanitComplete;
		}
		public int numOfForwardFail;
		public int numOfBackwardFail;
		public int numOfPatchFail;
		public int numOfSanitComplete;
		
		public int getTotalSinks()
		{
			return numOfSqliSink+numOfXssSink+numOfMfeSink;
		}
		
	}
	

}
