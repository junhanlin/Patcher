function initPage()
{
	initAlert($("#divAlert"), function()
	{
		initNavBar_LogOut($("#navBar"),function()
		{
			initAddUser($("#divAddUser"),function()
			{
				processUrl();
			});

		});
	});
	



	

	$('#progress_modal').nsProgress();
	$('#progress_modal').nsProgress('showWithImageAndStatus', 'nsLoader.gif', 'Loading&hellip;');
			








}

function processUrl()
{
	var addedProjectState= getParameter("addedProjectState");
	var addedProjectName= getParameter("addedProjectName");
	if(addedProjectName!=null && addedProjectState!=null)
	{
		if(addedProjectState=="true")
		{
			showAlert("Upload Successfully", "Your project \""+addedProjectName+"\" has been uploaded.", -1, null);
		}
		else
		{
			showAlert("Upload Fail", "Fail to upload project \""+addedProjectName+"\"", -1, null);
		}

	}
	var projectIds = getParameter("projectIds", this);
	fillProjectTable($.parseJSON(projectIds));
}


function reload()
{
	location.replace("../Index/Index.html");
}
function fillProjectTable(projectIds)
{
	var reqJson = {
		projectIds : projectIds
	};

	doJsonRequest("GetProject", reqJson, {
		validationError : function(resJson)
		{
			if (resJson.responseMsg[0] == "Validation Error: Invalid Login Session")
			{
				showAlert(resJson.responseMsg[0], "We are going to redirect you page.", -1, function()
				{
					reload();
				});
			}

		},
		success : function(resJson)
		{
			$("#divProjectTable").html("");
			var projectTableMaker = tableMaker({
				colNames : [ "Project ID", "Name", /* "Description", */ "Files", "Sinks"/* ,"Status" */,"Report","Downloads","Attack Pattern"],
				onCreateCellHead : null,
				onCreateCellBody : function(td, userState)
				{
					switch (td.colName)
					{
						case "Project ID":
							td.innerHTML = userState.projectId;
							break;
						case "Name":
							td.innerHTML = userState.projectName;
							break;
						case "Description":
							td.innerHTML = userState.projectDesc;
							break;
						case "Files":
							td.innerHTML = userState.numOfTotalFiles;

							break;
						case "Sinks":
							td.innerHTML = userState.numOfTotalSinks;

							break;
						case "Status":
							td.innerHTML = userState.statusName;
							break;
						case "Attack Pattern":
							if(userState.statusCode==100)
							{
								var atkPtnList = $("<ul></ul>");

								if(userState.taintTypes["SQLI"])
								{
									var txtBox = $("<input type='text'/>");
									txtBox.val(userState.atkPatterns["SQLI"]);
									var li = $("<li><span>SQLI:</span></li>");
									li.append(txtBox);
									atkPtnList.append(li);
								}
								if(userState.taintTypes["XSS"])
								{
									var txtBox = $("<input type='text'/>");
									txtBox.val(userState.atkPatterns["XSS"]);
									var li = $("<li><span>XSS:</span></li>");
									li.append(txtBox);
									atkPtnList.append(li);
								}
								if(userState.taintTypes["MFE"])
								{
									var txtBox = $("<input type='text'/>");
									txtBox.val(userState.atkPatterns["MFE"]);
									var li = $("<li><span>MFE:</span></li>");
									li.append(txtBox);
									atkPtnList.append(li);
								}
								$(td).append(atkPtnList);
							}
							else
							{
								td.innerHTML = "N/A";
							}
							break;
						case "Report":
							if(userState.statusCode==100)
							{
								var btnGroup = $("<div class=\"btn-group\"></div>");
								if(userState.taintTypes["SQLI"])
								{
									btnGroup.append($("<a class='btn "+(userState.sinkNum['SQLI']>0? 'btn-danger':'btn-success')+"' href='../Project/ProjectDetail.html?projectId=" + userState.projectId + "&taintType=SQLI'>SQLI</a>"));
								}
								if(userState.taintTypes["XSS"])
								{
									btnGroup.append($("<a class='btn "+(userState.sinkNum['XSS']>0? 'btn-danger':'btn-success')+"' href='../Project/ProjectDetail.html?projectId=" + userState.projectId + "&taintType=XSS'>XSS</a>"));
								}
								if(userState.taintTypes["MFE"])
								{
									btnGroup.append($("<a class='btn "+(userState.sinkNum['MFE']>0? 'btn-danger':'btn-success')+"' href='../Project/ProjectDetail.html?projectId=" + userState.projectId + "&taintType=MFE'>MFE</a>"));
								}
								$(td).append(btnGroup);
							}
							else
							{
								td.innerHTML = "N/A";
							}

							break;
						case "Downloads":
							if(userState.statusCode==100)
							{
								var btnGroup = $("<div class=\"btn-group\"></div>");
								btnGroup.append($("<a class='btn btn-primary' href='../../GetProjectReport?projectId=" + userState.projectId+"'>Code & Signature</a>"));
								btnGroup.append($("<a class='btn btn-primary' href='../../GetOptPatch?projectId=" + userState.projectId+"'>Patch</a>"));

								$(td).append(btnGroup);
							}
							else
							{
								td.innerHTML = "N/A";
							}

							break;

					}
				}
			});

			if (resJson.projects.length > 0)
			{
				var readme = "";
				for ( var i = 0; i < resJson.projects.length; i++)
				{
					
					var project = resJson.projects[i];
					readme += "###### optPatch"+project.projectId+".zip ######\n";
					readme += "Application: "+project.projectName+"\n";
					if(project.statusCode==100)
					{
						

						if(project.taintTypes["SQLI"])
						{
							readme += "SQLI: "+project.atkPatterns["SQLI"]+"\n";

						}
						if(project.taintTypes["XSS"])
						{
							readme += "XSS: "+project.atkPatterns["XSS"]+"\n";
						}
						if(project.taintTypes["MFE"])
						{
							readme += "MFE: "+project.atkPatterns["MFE"]+"\n";
						}
						
					}
					readme += "#############################\n\n";
					projectTableMaker.createRow(project);

				}
				console.log(readme);
			}
			else
			{

			}

			$("#divProjectTable").append(projectTableMaker.table);
			$("#divProjectTable > table").addClass("table").addClass("table-striped");
			$('#progress_modal').nsProgress('dismiss');
			
		}
	});
}