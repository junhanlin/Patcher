
function initAddUser(container, callback)
{
	fillHTMLSrc("Views/User/AddUser.html", container, function()
	{
		$("#frmAddUser").submit(function()
		{
			return false;
		});
		$("#aAddUser").click(aAddUser_onclick);
		$("#btnSubmitAddUser").click(btnSubmitAddUser_onclick);
		$("#btnCancelAddUser").click(btnCancelAddUser_onclick);
		if (callback != null)
		{
			callback();
		}
	});

}
function showAddUser()
{
	$("#txtAddUserEmail").val("");
	$("#txtAddUserName").val("");
	$("#txtAddUserPass").val("");
	$("#txtAddUserConfirmPass").val("");
	
	
	$('#mdlAddUser').modal('show');

}
function submitAddUser()
{
	$("#mdlAddUser .form-group").removeClass("has-error");
	
	var reqJson = {
		email: $("#txtAddUserEmail").val(),
		userName : $("#txtAddUserName").val(),
		userPass : $("#txtAddUserPass").val(),
		userConfirmPass : $("#txtAddUserConfirmPass").val(),
	};
	
	var ladda = Ladda.create($("#btnSubmitAddUser")[0]);
 	ladda.start();

	doJsonRequest("AddUser", reqJson, {
		validationError : function(resJson)
		{
			if(resJson.responseMsg[0]=="Validation Error: Email Is Required")
			{
				$("#txtAddUserEmail").parent().parent().addClass("has-error");
			}
			if(resJson.responseMsg[0]=="Validation Error: Email Already Exist")
			{
				$("#txtAddUserEmail").parent().parent().addClass("has-error");
			}
			if(resJson.responseMsg[0]=="Validation Error: User Name Is Required")
			{
				$("#txtAddUserName").parent().parent().addClass("has-error");
			}
			if(resJson.responseMsg[0]=="Validation Error: User Name Already Exist")
			{
				$("#txtAddUserName").parent().parent().addClass("has-error");
			}
			if(resJson.responseMsg[0]=="Validation Error: Password Is Required")
			{
				$("#txtAddUserPass").parent().parent().addClass("has-error");
			}
			if (resJson.responseMsg[0] == "Validation Error: Password Is Inconsistent")
			{
				$("#txtAddUserConfirmPass").parent().parent().addClass("has-error");	
			}
			ladda.stop();
			showAlert("<font color='red'>Error</font>",resJson.responseMsg[0], -1, null);
		},
		success : function(resJson)
		{
			
			if (resJson.isSuccess)
			{

				showAlert("<font color='green'>Congratulation</font>", "Successfully Sign Up !", 1500,
						function()
						{
							closeAddUser();
							

						});

			}
			else
			{
				showAlert("<font color='red'>Error</font>", "Fail to sign up user " + reqJson.userName + "&nbsp;!", -1, function()
				{
					closeAddUser();
				});
			}
			ladda.stop();
		}
	});
}
function cancelAddUser()
{

	closeAddUser();
}
function closeAddUser()
{
	$('#mdlAddUser').modal('hide');
}
function aAddUser_onclick()
{
	showAddUser();
}
function btnSubmitAddUser_onclick()
{
	submitAddUser();
}
function btnCancelAddUser_onclick()
{
	cancelAddUser();
}