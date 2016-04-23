var DEFAULT_ALERT_DURATION = 1500;
function initAlert(container, callback)
{
	fillHTMLSrc("Views/Shared/Alert.html", container, callback);
}

function showAlert(title, content, duration, callback)
{
	$("#divAlertTitle").html(title);
	$("#divAlertContent").html(content);
	$("#mdlAlert").modal('show');

	$("#mdlAlert").on("hidden", function()
	{
		callback();
		$("#mdlAlert").on("hidden", null);
	});
	duration = duration == -1 ? DEFAULT_ALERT_DURATION : duration;
	$("#mdlAlert").on('hidden.bs.modal', function()
	{
		if (callback != null)
		{
			callback();
		}
	});
	setTimeout(function()
	{
		$("#mdlAlert").modal('hide');
	}, duration);
}
