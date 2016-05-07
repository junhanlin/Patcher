var WEB_VIR_DIR = "patcher";

var clientBrowser = new detectBrowser();
String.prototype.endsWith = function(suffix)
{
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};
function getUrl(virPath)
{
	if (location != null)
		return location.protocol + "//" + location.host + "/" + WEB_VIR_DIR + "/" + virPath;
//		return location.protocol + "//" + location.host   + "/" + virPath;
}

function getParameter(parameterName, _window)
{
	if (typeof (_window) == "undefined")
	{
		_window = window.top;
	}

	if (_window == "ifrmMain")
	{
		_window = window.top.ifrmMain;
	}

	var urlQueryString = _window.location.href;
	//remove # symbol
	if(urlQueryString.match(/#$/))
	{

		urlQueryString = urlQueryString.substr(0,urlQueryString.length-1);
	}
	// 將parameterName後加入'='
	parameterName = parameterName + "=";

	if (urlQueryString.length > 0)
	{

		begin = urlQueryString.indexOf(parameterName);

		if (begin != -1)
		{
			begin += parameterName.length;
			end = urlQueryString.indexOf("&", begin);
			if (end == -1)
			{
				end = urlQueryString.length;
			}
			// 利用unescape解碼並傳回結果
			return unescape(urlQueryString.substring(begin, end));
		}
		// 如果沒有該參數,則傳回null
		return null;
	}
}

function doJsonRequest(virPath, reqJson, callbacks)
{
	$.ajax({
		url : getUrl(virPath),
		type : "POST",
		contentType : "application/json",
		datatype : "json",
		data : JSON.stringify(reqJson),
		error : function(xhr)
		{
			console.error(xhr.status + ": " + xhr.statusText);

		},
		success : function(response)
		{
			var resJson = null;
			try
			{
				resJson = response;
			}
			catch (e)
			{
				console.error(responseText);
			}
			if (callbacks != null)
			{
				// fatal error
				if (resJson.exitValue > 0)
				{
					if (callbacks.innerError != null)
					{
						callbacks.innerError(reqJson);
					}
					else
					{
						var innerErrorMsg = "Encounter inner error (Exit value= " + resJson.exitValue + " ):";
						for ( var i = 0; i < resJson.responseMsg.length; i++)
						{
							innerErrorMsg += "\r\n" + (i + 1) + ":\t" + resJson.responseMsg[i];
						}
						console.error(innerErrorMsg);
					}

				}
				// validation error
				if (resJson.exitValue == -1)
				{
					if (callbacks.validationError != null)
					{
						callbacks.validationError(resJson);
					}
					else
					{
						var validationErrorMsg = "Encounter validation error (Exit value= " + resJson.exitValue + " ):";
						for ( var i = 0; i < resJson.responseMsg.length; i++)
						{
							validationErrorMsg += "\r\n" + (i + 1) + ":\t" + resJson.responseMsg[i];
						}
						console.error(validationErrorMsg);
					}
				}

				if (resJson.exitValue == 0)
				{
					if (callbacks.success != null)
					{
						callbacks.success(resJson);
					}
				}
			}

		}
	});
}

function detectBrowser()
{
	var sAgent = navigator.userAgent.toLowerCase();
	this.isIE = (sAgent.indexOf("msie") != -1);
	// IE
	this.isFF = (sAgent.indexOf("firefox") != -1);
	// firefox
	this.isSa = (sAgent.indexOf("safari") != -1);
	// safari
	this.isOp = (sAgent.indexOf("opera") != -1);
	// opera
	this.isNN = (sAgent.indexOf("netscape") != -1);
	// netscape
	this.isMa = this.isIE;
	// marthon
	this.isOther = (!this.isIE && !this.isFF && !this.isSa && !this.isOp && !this.isNN && !this.isSa);
	// unknown Browser
}

function fillHTMLSrc(virPath, container, callback)
{
	$.ajax({
		url : getUrl(virPath),
		type : "GET",
		contentType : "application/text",
		datatype : "html",
		data : "",
		error : function(xhr)
		{
			console.error(xhr.status + ": " + xhr.statusText);

		},
		success : function(response)
		{

			if (typeof (container.outerHTML) == "undefined")
			{
				container = container[0];
			}
			container.outerHTML = response;
			container.templateHTML = response;
			if (callback != null)
			{
				callback();
			}

		}
	});

}

function tableMaker(options)
{
	var result = {
		options : options,
		table : document.createElement("TABLE"),
		createRow : null

	};
	var createRow = function(userState, pos)
	{
		if (typeof (pos) == "undefined")
		{
			pos = -1;
		}
		var tr = result.table.insertRow(pos);
		for ( var i = 0; i < result.options.colNames.length; i++)
		{
			var td = tr.insertCell(-1);
			td.colName = result.options.colNames[i];
			if (result.options.onCreateCellBody != null)
			{
				result.options.onCreateCellBody(td, userState);
			}
		}
	};
	result.createRow = createRow;
	var trHead = result.table.insertRow(-1);
	for ( var i = 0; i < result.options.colNames.length; i++)
	{
		var tdHead = trHead.insertCell(-1);
		tdHead.colName = result.options.colNames[i];
		tdHead.innerHTML = result.options.colNames[i];
		if (result.options.onCreateCellHead != null)
		{
			result.options.onCreateCellHead(tdHead);
		}

	}

	return result;

}

function redirectPage(page, idString, id)
{
	var result = null;
	var redirectUrl = "";
	if (page == "Property")
	{
		result = function()
		{
			if (idString == "propertyId")
				redirectUrl = "../property/Property.html?propertyId=" + id;

			location.replace(redirectUrl);
		};
	}
	else if (page == "Input")
	{
		result = function()
		{
			if (idString == "projectId")
				redirectUrl = "../input/Input.html?projectId=" + id;
			else if (idString == "sesssionId")
				redirectUrl = "../input/Input.html?inputId=" + id;
			location.replace(redirectUrl);
		};
	}
	else if (page == "Session")
	{
		result = function()
		{

			if (idString == "projectId")
				redirectUrl = "../session/Session.html?projectId=" + id;
			else if (idString == "propertyId")
				redirectUrl = "../session/Session.html?propertyId=" + id;
			else if (idString == "inputId")
				redirectUrl = "../session/Session.html?inputId=" + id;
			else if (idString == "sesssionId")
				redirectUrl = "../session/Session.html?sessionId=" + id;

			location.replace(redirectUrl);
		};
	}
	else if (page == "Task")
	{
		result = function()
		{
			if (idString == "projectId")
				redirectUrl = "../task/Task.html?projectId=" + id;
			else if (idString == "propertyId")
				redirectUrl = "../task/Task.html?propertyId=" + id;
			else if (idString == "inputId")
				redirectUrl = "../task/Task.html?inputId=" + id;
			else if (idString == "sessionId")
				redirectUrl = "../task/Task.html?sessionId=" + id;

			location.replace(redirectUrl);
		};
	}

	return result;
}