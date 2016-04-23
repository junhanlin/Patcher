var sinkPanelTemplate=null;
function initViewSinkList(container, callback)
{
	fillHTMLSrc("Views/Project/ViewSinkList.html", container, function()
	{
		$("#aAddProject").click(aAddProject_onclick);
		sinkPanelTemplate =  $("#divSinkPanel");
		fillHTMLSrc("Views/Project/SinkPanel.html",sinkPanelTemplate, function()
		{
			if (callback != null)
			{
				sinkPanelTemplate.addClass("hide");
				callback();
			}	
		});
	});

}
function showViewSinkList(fileNode)
{
	$("#accSinkList").html("");
	$("#sinkListAlert").html("");
	if(fileNode.taintTask.sinks.length>0)
	{
		for(var i=0;i<fileNode.taintTask.sinks.length;i++)
		{
			var sink = fileNode.taintTask.sinks[i];
			$("#accSinkList").append(gen_SinkPanel(i+1,sink));
		}
	}
	else
	{
		$("#sinkListAlert").append($("<div class='alert alert-info'><strong>Notice!</strong>No tainted sink was found</div>"));
	}
	$('#mdlViewSinkList').modal('show');
	
}

function gen_SinkPanel(num,sink)
{
	var result = $(sinkPanelTemplate[0].templateHTML);
	
	if(sink.isSanitFinish())
	{
		if(sink.isSanitComplete())
		{
			result.find(".sinkTitle").html("SINK#"+num +" (COMPLETE)");
			if(sink.isVuln)
			{
				result.removeClass("panel-default").addClass("panel-danger");
				result.find(".btnViewDepGraph").removeAttr('disabled');
				result.find(".btnViewDepGraph").addClass('btn-danger');
			}	
			else
			{
				result.removeClass("panel-default").addClass("panel-success");
				result.find(".btnViewDepGraph").removeAttr('disabled');
				result.find(".btnViewDepGraph").addClass('btn-success');
			}
		}
		else
		{
			result.find(".sinkTitle").html("SINK#"+num+" (FAIL)");
			result.find(".panel-default").removeClass("panel-default").addClass("panel-warning");
			result.find(".btnViewDepGraph").addClass('btn-warning');
			
		}
			
	}
	else
	{
		
		result.find(".sinkTitle").html("SINK#"+num +"(IN QUEUE)");
		
	}
	
	result.find(".txtInputs").val(sink.inputs);
	result.find(".chkIsVuln").attr('checked', sink.isVuln);
	result.find(".btnViewDepGraph").click(gen_btnViewDepGraph(sink));
	
	return result;
	
}
function gen_btnViewDepGraph(sink)
{
	return function(){
		location.href="SinkDetail.html?sinkId="+sink.sinkId;
	};
}
function closeAddProject()
{
	$('#mdlViewSinkList').modal('hide');
}
function aViewSinkList_onclick(refFileNode)
{
	showViewSinkList(fileRootNode[refFileNode]);
}

function btnSubmitAddProject_onclick()
{
	submitAddProject();
}
function btnCancelAddProject_onclick()
{
	cancelAddProject();
}