function initNavBar_Login(container, callback)
{
	fillHTMLSrc("Views/User/NavBar_Login.html", container, function()
	{
		
		$("#aLogOut").click(function()
		{
			logOut();
		});
		
		
		if(callback!=null)
		{
			callback();
		}
	});
}

function logOut()
{
	
	var reqJson = {
		
	};
	doJsonRequest("LogOut", reqJson, {
		validationError : function(resJson)
		{
			
		},
		success : function(resJson)
		{

			if (resJson.isSuccess)
			{
				onLogOutSuccess(resJson);

			}
			else
			{
				
			}

		}
	});
	

}

function onLogOutSuccess(resJson)
{
	showAlert("Goodbye", resJson.userName, null,null);
	initNavBar_LogOut($("#navBar"),null);

}
