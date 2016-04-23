function initPage()
{
	initAlert($("#divAlert"), null);
	initNavBar_LogOut($("#navBar"),function()
	{
		initAddUser($("#divAddUser"),null);
		
	});
	
	
	$('.carousel').carousel({
      interval: 2000
    });
	$('.carousel-control.right').trigger('click');
	
	processUrl();
	
}

function processUrl()
{

}


function reload()
{
	location.replace("Index.html");
}
