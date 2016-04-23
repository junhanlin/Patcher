package edu.nccu.soslab.Patcher;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

public class Utility
{
	public static ServletContext context;

	public static Connection getSqlConn()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection(PatcherConfig.config.connStr);
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static String hash(String str, String algorithm) throws NoSuchAlgorithmException
	{
		MessageDigest digest;
		digest = MessageDigest.getInstance(algorithm);
		byte[] hashBytes = null;
		String result = null;
		try
		{
			hashBytes = digest.digest(str.getBytes("utf-8"));
			result = byte2Hex(hashBytes);
		} catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	private static String byte2Hex(byte[] byteArray)
	{
		StringBuffer strBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++)
		{
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
			{
				strBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			} else
			{
				strBuff.append(Integer.toHexString(0xFF & byteArray[i]));
			}

		}
		return strBuff.toString();
	}

	public static String getExceptionString(Exception e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();

	}

	public static <T> String genSqlParamDirectivies(List<T> params)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < params.size(); i++)
		{
			sb.append("?");
			if (i != params.size() - 1)
			{
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	public static int getCurrUserId(HttpServletRequest request)
	{
		int result = -1;

		try
		{

			if (request.getSession().getAttribute("userId") != null)
			{
				result = Integer.parseInt(request.getSession().getAttribute("userId").toString());
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static byte[] getBytes(InputStream in)
	{

		int READ_SIZE = 16384;
		byte[] resBytes = new byte[READ_SIZE];
		try
		{

			int offset = 0;
			int result = 0;
			int bufLeft = resBytes.length;
			outer: while (true)
			{
				while (bufLeft > 0)
				{
					result = in.read(resBytes, offset, bufLeft);
					if (result < 0)
					{
						break outer;
					}
					offset += result;
					bufLeft -= result;
				}

				// resize byte array
				bufLeft = READ_SIZE;
				byte[] newResBytes = new byte[resBytes.length + READ_SIZE];
				System.arraycopy(resBytes, 0, newResBytes, 0, resBytes.length);
				resBytes = newResBytes;
			}
			// resize final byte array
			byte[] newResBytes = new byte[offset];
			System.arraycopy(resBytes, 0, newResBytes, 0, offset);
			resBytes = newResBytes;

			// dispose all the resources after using them.
			in.close();

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return resBytes;
	}

	public static byte[] getBytes(InputStream in, int size)
	{

		byte[] resBytes = new byte[size];
		try
		{

			int offset = 0;
			int result = 0;
			int bufLeft = resBytes.length;
			while (bufLeft > 0)
			{

				result = in.read(resBytes, offset, bufLeft);
				if (result < 0)
				{
					break;
				}
				offset += result;
				bufLeft -= result;
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return resBytes;
	}

	public static String getFileName(Part part)
	{
		String contentDisp = part.getHeader("content-disposition");
		System.out.println("content-disposition header= " + contentDisp);
		String[] tokens = contentDisp.split(";");
		for (String token : tokens)
		{
			if (token.trim().startsWith("filename"))
			{
				return token.substring(token.indexOf("=") + 2, token.length() - 1);
			}
		}
		return "";
	}
	
	public static boolean extractArchive(ArchiveInputStream archiveIn,File destDir)
	{

		try
		{
		
			ArchiveEntry archiveEntry =null;
			
			while ((archiveEntry=archiveIn.getNextEntry())!=null)
			{
				
				
				String entryFullPath = archiveEntry.getName().replace('\\', '/');
				if(entryFullPath.endsWith("/"))
				{
					//means this is a dir
					continue;
				}
				File entryFile = new File(destDir,entryFullPath);
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
			return true;
		} catch (IOException e)
		{
			e.printStackTrace();

		}
		return false;
	}
	public static void createZip(String directoryPath, String zipPath) throws IOException {
        FileOutputStream fOut = null;
        BufferedOutputStream bOut = null;
        ZipArchiveOutputStream tOut = null;
 
        try {
            fOut = new FileOutputStream(new File(zipPath));
            bOut = new BufferedOutputStream(fOut);
            tOut = new ZipArchiveOutputStream(bOut);
            addFileToZip(tOut, directoryPath, "");
        } finally {
            tOut.finish();
            tOut.close();
            bOut.close();
            fOut.close();
        }
 
    }
	private static void addFileToZip(ZipArchiveOutputStream zOut, String path, String base) throws IOException {
        File f = new File(path);
        String entryName = base + f.getName();
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(f, entryName);
 
        zOut.putArchiveEntry(zipEntry);
 
        if (f.isFile()) {
            FileInputStream fInputStream = null;
            try {
                fInputStream = new FileInputStream(f);
                IOUtils.copy(fInputStream, zOut);
                zOut.closeArchiveEntry();
            } finally {
                IOUtils.closeQuietly(fInputStream);
            }
 
        } else {
            zOut.closeArchiveEntry();
            File[] children = f.listFiles();
 
            if (children != null) {
                for (File child : children) {
                    addFileToZip(zOut, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }
	public static String getRealPath(String path)
	{
		return context.getRealPath(path);
	}
	public static String escapLatex(String str)
	{
		return str.replace("\\", "\\\\").replace("{","\\{").replace("}","\\}").replace("_","\\_").replace("^","\\^").replace("#","\\#").replace("&","\\&").replace("$","\\$").replace("%","\\%").replace("~","\\~");
		
		
	}
}
