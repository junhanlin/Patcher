var visualPatcherApp = null;
var currDepGraphPlainTxt;
var currSrcCodePath = null;
var srcCodes = new Array();
function initPage() {
	$('#progress_modal').nsProgress();
	$('#progress_modal').nsProgress('showWithImageAndStatus', 'nsLoader.gif',
			'Loading&hellip;');
	initAlert($("#divAlert"), function() {
		initNavBar_LogOut($("#navBar"), function() {
			initAddUser($("#divAddUser"), function() {
				processUrl();
			});

		});
	});

}

function processUrl() {
	var sinkId = getParameter("sinkId");
	fillSinkDetail(sinkId);
}

function reload() {
	location.replace("../Index/Index.html");
}
function fillSinkDetail(sinkId) {

	var reqJson = {
		sinkId : sinkId
	};

	doJsonRequest(
			"GetSinkDetail",
			reqJson,
			{
				validationError : function(resJson) {
					if (resJson.responseMsg[0] == "Validation Error: Invalid Login Session") {
						showAlert(resJson.responseMsg[0],
								"We are going to redirect you page.", -1,
								function() {
									reload();
								});
					}

				},
				success : function(resJson) {

					$("#aDepGraphDot").attr('href',
							'../../GetDepGraphDot?sinkId=' + reqJson.sinkId);

					$("#divMinCutPatch").html("");
					$("#divOptPatch").html("");
					$("#divOptPatch")
							.append(
									"<div class='alert alert-info'>Download <strong>optimal patch package</strong> <a style='color:#ff0000;' href='"
											+ getUrl("GetOptPatch?projectId="
													+ resJson.file.projectId)
											+ "'>here</a>!</div>");
					$("#divOptPatch")
							.append(
									"<div class='alert alert-info'>Extract the <strong>optPatch</strong> directory into your <span class='glyphicon glyphicon glyphicon-folder-open'>&nbsp;</span><strong>Root</strong> directory</div>");

					// optPatch inclusion path
					var numOfParent = resJson.file.path == "/" ? 0
							: resJson.file.path.split("/").length - 1;
					var includePath = "";
					for (var i = 0; i < numOfParent; i++) {
						includePath += "../";
					}
					includePath += "optPatch/optPatch.php";
					$("#divOptPatch")
							.append(
									"<div class='alert alert-info'>At the beginning of <strong>"
											+ resJson.file.fileName
											+ "</strong>, do the fallowing inclusion:<br/><strong>include(\""
											+ includePath + "\");</strong>");
					if (resJson.sink.isVuln) {

						for (var i = 0; i < resJson.inputs.length; i++) {
							var input = resJson.inputs[i];
							$("#divMinCutPatch")
									.append(
											$("<div class='well'>In line <strong>"
													+ input.lineNo
													+ "</strong>, change variable <strong>"
													+ input.varName
													+ "</strong> to <strong>"
													+ escapeHtml(input.patchFunct)
													+ "</strong></div>"));
							$("#divOptPatch")
									.append(
											$("<div class='well'>In line <strong>"
													+ input.lineNo
													+ "</strong>, change variable <strong>"
													+ input.varName
													+ "</strong> to <strong>optPatch("
													+ input.varName
													+ ","
													+ resJson.sink.sinkId
													+ ","
													+ i + ")</strong></div>"));
						}

						$("#txtBlackAuto").html(
								resJson.blackAuto.autoDot.replace("\r", "")
										.replace("\n", "\r\n"));
						$("#txtWhiteAuto").html(
								resJson.whiteAuto.autoDot.replace("\r", "")
										.replace("\n", "\r\n"));
					}
					currDepGraphPlainTxt = resJson.depGraphPlainTxt;
					embedVisualPatcherApp();
					$('#progress_modal').nsProgress('dismiss');
				}
			});
}

function escapeHtml(str) {
	return $("<div></div>").text(str).html();
}
function getUnity(playerId) {
	if (typeof unityObject != "undefined") {
		return unityObject.getObjectById(playerId);
	}
	return null;
}

function embedVisualPatcherApp() {
	if (typeof unityObject != "undefined") {

		var appUrl = getUrl("Bins/WebPlayer.unity3d");
		unityObject.embedUnity("visualPatcherApp", appUrl, "600", "337", null,
				null, completeLoadVisualPatcherApp);
	}
}

function completeLoadVisualPatcherApp(result) {

	if (result.success) {
		visualPatcherApp = getUnity("visualPatcherApp");
		visualPatcherApp.SendMessage("Starter", "setDepGraph",
				currDepGraphPlainTxt);
	} else {

	}

}

function onVisualPatcherAppReady(str) {
	visualPatcherApp
			.SendMessage("Starter", "setDepGraph", currDepGraphPlainTxt);
}

//
// function gameAppLoginComplete(loginRes)
// {
// var slCmrsArea = document.getElementById("slCmrsArea");
// var gameAppArea = document.getElementById("gameAppArea");
// slCmrs.Content.MP.gameAppLoginComplete(loginRes);
//
// }

function removeVisualPatcherApp() {
	/*
	 * unityObject.removeUnity("gameApp"); var newDiv =
	 * document.createElement("DIV"); newDiv.id = "visualPatcherApp";
	 * newDiv.innerHTML = "<div class='missing'><a
	 * href='http://unity3d.com/webplayer/' title='Please install Unity Web
	 * Player !'><img alt='Please Install Unity Web Player !'
	 * src='http://webplayer.unity3d.com/installation/getunity.png'/></a></div>";
	 * document.getElementById("divVisualPatcher").innerHTML="";
	 * document.getElementById("divVisualPatcher").appendChild(newDiv);
	 */
}

function focusLine(srcCodePath, lineNo, objNodeName) {
	if (srcCodePath == currSrcCodePath
			|| typeof (srcCodes[srcCodePath]) != "undefined") {

		executeFocusLine(srcCodePath, lineNo, objNodeName);
	} else {
		var reqJson = {
			fileAbsPath : srcCodePath,

		};

		doJsonRequest("GetSrcCode", reqJson, {
			httpError : function(resJson) {
				visualPatcherApp.SendMessage(objNodeName,
						"loadSrcCodeCallback", "fail");
			},
			fatalError : function(resJson) {
				visualPatcherApp.SendMessage(objNodeName,
						"loadSrcCodeCallback", "fail");
			},
			validationError : function(resJson) {
				visualPatcherApp.SendMessage(objNodeName,
						"loadSrcCodeCallback", "fail");
			},
			success : function(resJson) {

				srcCodes[srcCodePath] = resJson.srcCode;
				executeFocusLine(srcCodePath, lineNo, objNodeName);

			}
		});
	}

}

function executeFocusLine(srcCodePath, lineNo, objNodeName) {
	$(".highlightLine").css("background-color", "")
			.removeClass("highlightLine");
	var srcCode = srcCodes[srcCodePath];
	if (currSrcCodePath != srcCodePath) {
		fillSrcCode(srcCode);
		currSrcCodePath = srcCodePath;
	}

	if (lineNo != -1) {
		var li = $("ol li:nth-child(" + (lineNo) + ")");
		li.addClass("highlightLine").css("background-color", "#FFFF00");
		$("#divSrcCode").scrollTop(
				$("#divSrcCode").scrollTop() + li.position().top
						- $("#divSrcCode").height() / 2 + li.height() / 2);
	}

	visualPatcherApp.SendMessage(objNodeName, "loadSrcCodeCallback", "success");
}

function fillSrcCode(srcCode) {

	$("#codeSrcCode").removeClass("prettyprinted");
	$("#codeSrcCode").html(
			"<code class = 'language-php'>" + srcCode + "</code>");
	$("#codeSrcCode").addClass("prettyprint").addClass("linenums");
	prettyPrint();

	// var srcCodeLines = srcCode.split("\n");
	/*
	 * for (var i = 0; i < srcCodeLines.length; i++) { var divLine = $("<div></div>").text((i +
	 * 1) + " : " + srcCodeLines[i]).addClass("tintBlue14"); divLine.append($("<a
	 * name='srcCodeLine_" + (i + 1) + "'></a>"));
	 * $("#divSrcCode").append(divLine); }
	 */
}

function log(msg) {
	console.log(msg);
}