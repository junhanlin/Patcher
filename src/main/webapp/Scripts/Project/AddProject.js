
function initAddProject(container, callback)
{
	fillHTMLSrc("Views/Project/AddProject.html", container, function()
	{
		
		$("#aAddProject").click(aAddProject_onclick);
		$("#btnSubmitAddProject").click(btnSubmitAddProject_onclick);
		$("#btnCancelAddProject").click(btnCancelAddProject_onclick);
		$("#chkAddProjectShouldSqli").change(gen_chkAddProjectShouldTaint_onchange($("#txtAddProjectAtkPatternSqli")));
		$("#chkAddProjectShouldXss").change(gen_chkAddProjectShouldTaint_onchange($("#txtAddProjectAtkPatternXss")));
		$("#chkAddProjectShouldMfe").change(gen_chkAddProjectShouldTaint_onchange($("#txtAddProjectAtkPatternMfe")));
		
		
		if (callback != null)
		{
			callback();
		}
	});

}
function showAddProject()
{
	
	$("#txtAddProjectName").val("");
	$('#mdlAddProject').modal('show');

}
function submitAddProject()
{
	
	$("#mdlAddProject .form-group").removeClass("has-error");
	
	if($("#txtAddProjectName").val().trim()=="")
	{
		$("#txtAddProjectName").parent().parent().addClass("has-error");
		showAlert("<font color='red'>Error</font>","Project Name Is Required", -1, null);
		return;
	}
	$('#frmAddProject').attr("action", getUrl("AddProject"));
	$('#frmAddProject').attr("enctype", "multipart/form-data");

	$('#frmAddProject').attr("method", "post");
	$('#frmAddProject').submit();

	
}
function cancelAddProject()
{

	closeAddProject();
}
function closeAddProject()
{
	$('#mdlAddProject').modal('hide');
}
function aAddProject_onclick()
{
	showAddProject();
}
function btnSubmitAddProject_onclick()
{
	submitAddProject();
}
function btnCancelAddProject_onclick()
{
	cancelAddProject();
}
function gen_chkAddProjectShouldTaint_onchange(txtAtkPattern)
{
	var retVal = function()
	{
		txtAtkPattern.disabled = this.checked;
	};
	return retVal;
	
}
