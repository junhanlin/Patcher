package edu.nccu.soslab.Patcher.Responser.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipUtil;

import edu.nccu.mis.Service.JsonResponser;
import edu.nccu.soslab.Patcher.PatcherConfig;
import edu.nccu.soslab.Patcher.PatcherValidation;
import edu.nccu.soslab.Patcher.Utility;
import edu.nccu.soslab.Patcher.Json.Project.ReqAddProject;
import edu.nccu.soslab.Patcher.Json.Project.ResAddProject;

public class AddProjectResponser extends JsonResponser<ReqAddProject, ResAddProject>
{

	
	public AddProjectResponser(HttpServletRequest httpRequest, HttpServletResponse httpResponse, ReqAddProject request, Class<ResAddProject> classOfResponse)
	{
		super(httpRequest, httpResponse, request, classOfResponse);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processRequest()
	{
		response.isSuccess = insertProject();
		if(response.isSuccess)
		{
			if(request.projectFileName.endsWith(".php"))
			{
				
				response.isSuccess &= insertSingleFile(response.projectId);
				response.isSuccess &= extractSingleFile(response.projectId);
				
				
			}
			else 
			{
				response.isSuccess &= insertArchivedFiles(response.projectId);
				response.isSuccess &= extractArchivedFiles(response.projectId);
				
			}
			//update db
			if(response.isSuccess)
			{
				markExtractComplete(response.projectId);
			}
			else 
			{
				markExtractFail(response.projectId);
			}
		}
		
	}
//	private boolean isProjectNameUsed()
//	{
//		Connection conn = null;
//		PreparedStatement selectStat = null;
//		ResultSet rs = null;
//		boolean result = false;
//
//		String selectInputSql = "SELECT projectName FROM tblProject WHERE projectName=? AND userId=?";
//
//		conn = Utility.getSqlConn();
//		try
//		{
//			selectStat = (PreparedStatement) conn.prepareStatement(
//					selectInputSql);
//			int p = 1;
//			selectStat.setString(p++, request.projectName);
//			selectStat.setInt(p++, Utility.getCurrUserId(httpRequest));
//			rs = selectStat.executeQuery();
//			if (rs.next())
//			{
//				result = true;
//				response.exitValue = -1;
//				response.responseMsg
//						.add("Project Name Already Exist");
//			}
//
//		} catch (SQLException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return result;
//	}
	@Override
	protected String validateRequest()
	{
		PatcherValidation val = new PatcherValidation(httpRequest);
		if (!val.validateLogin())
		{
			return "Invalid Login Session";
		}
		return null;
	}
	protected boolean insertProject()
	{
		
		Connection conn = null;
		PreparedStatement insertStat = null;
		
		ResultSet rs = null;
		// int insertedInputId = -1;
		boolean result = false;

		try
		{

			conn = Utility.getSqlConn();
			String insertSql = "INSERT INTO tblProject (projectName,projectFile,projectFileName,projectDesc,isArchivedFile,userId) VALUES (?,?,?,?,?,?);";
			
			insertStat = conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			
			insertStat.setString(p++, request.projectName);
			insertStat.setBinaryStream(p++, request.projectFile);
			insertStat.setString(p++, request.projectFileName);
			insertStat.setString(p++, request.projectDesc);
			insertStat.setBoolean(p++, !request.projectFileName.endsWith(".php"));
			insertStat.setInt(p++, Utility.getCurrUserId(httpRequest));
			
			int insertedRow = insertStat.executeUpdate();
			if (insertedRow > 0)
			{
				rs=insertStat.getGeneratedKeys();
				
				if(rs.next())
				{
					response.projectId=rs.getInt(1);
					response.projectName = request.projectName;
					result = true;
				}
				
			}

		} catch (Exception e)
		{
			addResponseMsg(e);
			e.printStackTrace();
		} finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
				if (insertStat != null)
				{
					insertStat.close();
					insertStat = null;
				}
				
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				addResponseMsg(e);
				e.printStackTrace();
			}
		}
		return result;

	}
	
	private boolean insertSingleFile(int projectId)
	{
		Connection conn = null;
		PreparedStatement insertStat = null;
		
		ResultSet rs = null;
		// int insertedInputId = -1;
		boolean result = false;

		try
		{
			
			conn = Utility.getSqlConn();
			String insertSql = "INSERT INTO tblFile (projectId,fileName,path,content) VALUES (?,?,?,?);";
			
			insertStat = conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
			
			int p = 1;
			
			insertStat.setInt(p++, projectId);
			insertStat.setString(p++, request.projectFileName);
			insertStat.setString(p++, "/");
			request.projectFile.reset();
			insertStat.setBinaryStream(p++, request.projectFile);
			
			
			int insertedRow = insertStat.executeUpdate();
			if (insertedRow > 0)
			{
				ResultSet genKeyRs = insertStat.getGeneratedKeys();
				genKeyRs.next();
				int fileId = genKeyRs.getInt(1);
				insertTaintTask(conn, fileId);
				result = true;
			}

		} catch (Exception e)
		{
			addResponseMsg(e);
			e.printStackTrace();
		} finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
				if (insertStat != null)
				{
					insertStat.close();
					insertStat = null;
				}
				
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				addResponseMsg(e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	public void insertTaintTask(Connection conn,int fileId)
	{
		
		PreparedStatement insertStat = null;
		
		
		// int insertedInputId = -1;
		boolean result = false;

		try
		{
			
			
			if(httpRequest.getParameter("chkAddProjectShouldSqli")!=null)
			{
				String insertSql = "INSERT INTO tblTaintTask (fileId,taintType,atkPattern) VALUES (?,?,?);";
				
				insertStat = conn.prepareStatement(insertSql);
				
				int p = 1;
				
				insertStat.setInt(p++, fileId);
				insertStat.setString(p++, "SQLI");
				insertStat.setString(p++, httpRequest.getParameter("txtAddProjectAtkPatternSqli"));
				insertStat.executeUpdate();
				insertStat.close();
			}
			
			if(httpRequest.getParameter("chkAddProjectShouldXss")!=null)
			{
				String insertSql = "INSERT INTO tblTaintTask (fileId,taintType,atkPattern) VALUES (?,?,?);";
				
				insertStat = conn.prepareStatement(insertSql);
				
				int p = 1;
				
				insertStat.setInt(p++, fileId);
				insertStat.setString(p++, "XSS");
				insertStat.setString(p++, httpRequest.getParameter("txtAddProjectAtkPatternXss"));
				insertStat.executeUpdate();
				insertStat.close();
			}
			
			if(httpRequest.getParameter("chkAddProjectShouldMfe")!=null)
			{
				String insertSql = "INSERT INTO tblTaintTask (fileId,taintType,atkPattern) VALUES (?,?,?);";
				
				insertStat = conn.prepareStatement(insertSql);
				
				int p = 1;
				
				insertStat.setInt(p++, fileId);
				insertStat.setString(p++, "MFE");
				insertStat.setString(p++, httpRequest.getParameter("txtAddProjectAtkPatternMfe"));
				insertStat.executeUpdate();
			}
			

		} catch (Exception e)
		{
			addResponseMsg(e);
			e.printStackTrace();
		} finally
		{
			try
			{
				
				if (insertStat != null)
				{
					insertStat.close();
					insertStat = null;
				}
				
				
			} catch (Exception e)
			{
				addResponseMsg(e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	}
	public boolean extractSingleFile(int projectId)
	{
		boolean result=false;
		File projectRootDir;
		
		try
		{
			projectRootDir= new File(PatcherConfig.config.getProjectDirPath(),String.valueOf(projectId));
			if(!projectRootDir.exists())
			{
				projectRootDir.mkdir();
				projectRootDir.setReadable(true, false);
				projectRootDir.setWritable(true, false);
//				Runtime.getRuntime().exec("chmod -R 777 " + projectRootDir.getAbsolutePath());
//				System.out.println("CHMOD");
			}
			
			if(projectRootDir.exists())
			{
				FileOutputStream fileOut = new FileOutputStream(new File(projectRootDir,request.projectFileName));
				request.projectFile.reset();
				fileOut.write(Utility.getBytes(request.projectFile));
				fileOut.close();
				result=true;
			}
			else 
			{
				System.err.printf("Fail to extract php file to %s\n",projectRootDir.getAbsolutePath());
			}
		}
		catch(Exception e)
		{
			addResponseMsg(e);
			e.printStackTrace();
		}
		return result;
		
		
	}
	private boolean insertArchivedFiles(int projectId)
	{
		boolean result = false;
		try
		{
			request.projectFile.reset();
			ArchiveInputStream archiveIn = new ArchiveStreamFactory()
			    .createArchiveInputStream(request.projectFile);
			ArchiveEntry archiveEntry=null;
			
			while((archiveEntry=archiveIn.getNextEntry())!=null)
			{
				if(!archiveEntry.isDirectory() && archiveEntry.getName().endsWith(".php"))
				{
					String entryFullPath = archiveEntry.getName().replace('\\', '/');
					File entryFile = new File(entryFullPath);
					
					Connection conn = null;
					PreparedStatement insertStat = null;
					
					ResultSet rs = null;
					// int insertedInputId = -1;
					

					try
					{

						conn = Utility.getSqlConn();
						String insertSql = "INSERT INTO tblFile (projectId,fileName,path,content) VALUES (?,?,?,?);";
						
						insertStat = conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
						
						int p = 1;
						
						insertStat.setInt(p++, projectId);
						insertStat.setString(p++, entryFile.getName());
						insertStat.setString(p++, "/"+(entryFile.getParent()==null?"":entryFile.getParent()));
						
						insertStat.setBinaryStream(p++, archiveIn);
						
						
						int insertedRow = insertStat.executeUpdate();
						if (insertedRow > 0)
						{
							ResultSet genKeyRs = insertStat.getGeneratedKeys();
							genKeyRs.next();
							int fileId = genKeyRs.getInt(1);
							insertTaintTask(conn, fileId);
							result = true;
						}

					} catch (Exception e)
					{
						addResponseMsg(e);
						e.printStackTrace();
					} finally
					{
						try
						{
							
							if (insertStat != null)
							{
								insertStat.close();
								insertStat = null;
							}
							
							if (conn != null)
							{
								conn.close();
								conn = null;
							}
						} catch (Exception e)
						{
							// TODO Auto-generated catch block
							addResponseMsg(e);
							e.printStackTrace();
						}
					}
				}
			}
		} 
		 catch (Exception e)
		{
			// TODO Auto-generated catch block
			addResponseMsg(e);
			e.printStackTrace();
		}

		
		return result;
	}
	
	public boolean extractArchivedFiles(int projectId)
	{
		boolean result=false;
		File projectRootDir;
		
		try
		{
			projectRootDir= new File(PatcherConfig.config.getProjectDirPath(),String.valueOf(projectId));
			if(!projectRootDir.exists())
			{
				projectRootDir.mkdir();
				projectRootDir.setReadable(true, false);
				projectRootDir.setWritable(true, false);
//				Files.setPosixFilePermissions(projectRootDir.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ,PosixFilePermission.OWNER_WRITE,PosixFilePermission.OWNER_EXECUTE,PosixFilePermission.GROUP_READ,PosixFilePermission.GROUP_WRITE,PosixFilePermission.GROUP_EXECUTE,PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_WRITE));
				
			}
			
			if(projectRootDir.exists())
			{
				request.projectFile.reset();
				ArchiveInputStream archiveIn = new ArchiveStreamFactory()
				    .createArchiveInputStream(request.projectFile);
				ArchiveEntry archiveEntry =null;
				while ((archiveEntry=archiveIn.getNextEntry())!=null)
				{
					if(archiveEntry.isDirectory() || !archiveEntry.getName().endsWith(".php"))
					{
						//means this is a dir
						continue;
					}
					
					String entryFullPath = archiveEntry.getName().replace('\\', '/');
					File entryFile = new File(projectRootDir,entryFullPath);
					File entryParentDir = entryFile.getParentFile();
					if(!entryParentDir.exists())
					{
						entryParentDir.mkdirs();
					}
					if(entryParentDir.exists())
					{
						FileOutputStream fileOut=null;
						try
						{
							fileOut = new FileOutputStream(entryFile);
							fileOut.write(Utility.getBytes(archiveIn,(int)archiveEntry.getSize()));
						
						} 
						catch (IOException e1)
						{
							e1.printStackTrace();
						}
						finally
						{
							if(fileOut!=null)
							{
								fileOut.close();
							}
						}
					}
					
					

				}
				result=true;
			}
			else 
			{
				System.err.printf("Fail to extract php files to %s\n",projectRootDir.getAbsolutePath());
			}
		}
		catch(Exception e)
		{
			addResponseMsg(e);
			e.printStackTrace();
		}
		return result;
		
		
	}
	
	private boolean markExtractComplete(int projectId)
	{
		boolean result=false;
		Connection conn = null;
		
		PreparedStatement updateStat = null;

		try
		{

			conn = Utility.getSqlConn();

			updateStat = conn.prepareStatement("UPDATE tblProject SET statusCode=100 WHERE projectId=?");
			int p = 1;
			updateStat.setInt(p++, projectId);
			int updatedRow = updateStat.executeUpdate();
			if(updatedRow>0)
			{
				result=true;
			}

		} catch (Exception e)
		{
			addResponseMsg(e);
			e.printStackTrace();
		} finally
		{
			try
			{
				
				if (updateStat != null)
				{
					updateStat.close();
					updateStat = null;
				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				addResponseMsg(e);
				e.printStackTrace();
			}
		}
		return result;
	}
	private boolean markExtractFail(int projectId)
	{
		boolean result = false;
		Connection conn = null;
		PreparedStatement updateStat = null;

		try
		{

			conn = Utility.getSqlConn();

			updateStat = conn.prepareStatement("UPDATE tblProject SET statusCode=101 WHERE projectId=?");
			int p = 1;
			updateStat.setInt(p++, projectId);
			int updatedRow = updateStat.executeUpdate();
			if(updatedRow>0)
			{
				result=true;
			}

		} catch (Exception e)
		{
			addResponseMsg(e);
			e.printStackTrace();
		} finally
		{
			try
			{
				
				if (updateStat != null)
				{
					updateStat.close();
					updateStat = null;
				}
				if (conn != null)
				{
					conn.close();
					conn = null;
				}
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				addResponseMsg(e);
				e.printStackTrace();
			}
		}
		return result;
	}

}
