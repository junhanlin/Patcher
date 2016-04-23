package edu.nccu.mis.Service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.graph.GraphAdapterBuilder;

import edu.nccu.soslab.Patcher.Json.Project.ResGetProjectDetail.FileNode;

public abstract class JsonResponser<T extends Request, U extends Response>
		extends Responser<T, U>
{

	public JsonResponser(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, Class<T> classOfRequest,
			Class<U> classOfResponse)
	{
		super(httpRequest, httpResponse, classOfRequest, classOfResponse);
		Gson gson = new Gson();
		StringBuilder sb = new StringBuilder();
		String line;
		try
		{
			while ((line = httpRequest.getReader().readLine()) != null)
			{
				sb.append(line);
			}

			System.out.println("[Request:" + classOfRequest.getName() + "]");

			System.out.println(sb.toString());
			this.request = gson.fromJson(sb.toString(), classOfRequest);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JsonResponser(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, T request,
			Class<U> classOfResponse)
	{
		super(httpRequest, httpResponse, request, classOfResponse);
	}

	public void complete()
	{
		Gson gson = new Gson();		
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
	
	public void completeWithoutJson()
	{	
		Gson gson = new Gson();
		String responseStr = gson.toJson(getResponse());
		System.out.println("[Reponse:" + classOfResponse.getName() + "]");
		System.out.println(responseStr);
	}

}
