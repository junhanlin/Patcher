function initNavBar_LogOut(container, callback)
{
	fillHTMLSrc("Views/User/NavBar_LogOut.html", container, function()
	{
		$("#frmLogin").on("submit", function()
		{
			return false;
		});
		$("#btnSubmitLogin").click(function()
		{
			
			login();
		});
		/*
	 * $("#pw").keypress(function(e) { var key = e.which; if (key == 13)// the
	 * enter key code { login(); return false; } });
	 */

		checkLogin();
		
		if(callback!=null)
		{
			callback();
		}
	});
}
function login()
{
	var userName = $('#txtUserName').val();
	var userPassword = $('#txtUserPassword').val();
	$('#txtUserName').val("");
	$('#txtUserPassword').val("");
	
	var ladda = Ladda.create($("#btnSubmitLogin")[0]);
 	ladda.start();
	
	var reqJson = {
		userName : userName,
		userPassword : userPassword
	};
	
	doJsonRequest("Login", reqJson, {
		validationError : function(resJson)
		{
			
		},
		success : function(resJson)
		{
			ladda.stop();
			if (resJson.isValid)
			{
				onLoginSuccess(resJson);

			}
			else
			{
				onLoginFail(resJson);
			}

		}
	});
}

function checkLogin()
{
	var reqJson = {
		userName : null,
		userPassword : null
	};
	doJsonRequest("Login", reqJson, {
		validationError : function(resJson)
		{
			onLogOutSuccess(resJson);
		},
		success : function(resJson)
		{

			if (resJson.isValid)
			{
				onLoginSuccess(resJson);

			}
			else
			{
				

			}

		}
	});

	return true;

}
function onLoginSuccess(response)
{
	if(location.href.match(/Index.html/g)!=null)
	{
		location.href="../Project/Project.html";
		return;
	}
	initNavBar_Login($("#navBar"),function()
	{
		$("#lblLoginInfo").html("Hello, "+response.userName);
		initAddProject($("#divAddProject"),null);
		
		
	});
	
}

function onLoginFail(response)
{
	showAlert("Fail to login", "Make sure you input the correct user name and password", null,null);
}
