var fileListHtml = "";
var fileRootNode = null;

function initPage() {
	$('#progress_modal').nsProgress();
	$('#progress_modal').nsProgress('showWithImageAndStatus', 'nsLoader.gif',
			'Loading&hellip;');
	initAlert($("#divAlert"), function() {
		initNavBar_LogOut($("#navBar"), function() {
			initAddUser($("#divAddUser"), function() {
				initViewSinkList($("#divViewSinkList"), function() {
					processUrl();
				});
			});
		});
	});

}

function processUrl() {
	var projectId = getParameter("projectId", this);
	var taintType = getParameter("taintType", this);
	// fillProjectDetailTreeGrid(projectId, taintType);
	fillProjectDetailTree(projectId, taintType);
}

function reload() {
	location.replace("../Index/Index.html");
}

function fillProjectDetailTree(projectId, taintType) {
	fileListHtml = "";
	fileRootNode = null;
	var reqJson = {
		projectId : projectId,
		taintType : taintType
	};

	doJsonRequest(
			"GetProjectDetail",
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
					$("#titleProject").text(
							resJson.projectName + " - Project Details");
					$("#titleProject").append(
							$("<small>&nbsp;&nbsp;&nbsp;&nbsp;" + taintType
									+ "</small>"));
					$("#wellProjectDesc").text(resJson.projectDesc);

					var rawRootNode = resJson.fileRootNode["0x1"];
					var rootNode = {
						id : "0x1",
						text : "Project",
						type : "root",
						state : {
							opened : true,
							selected : true
						},
						rawData : rawRootNode,
						pNode : null
					};
					extendFileNode(rootNode, resJson.fileRootNode);
					$('#divProjectDetailTree')
							.jstree(
									{
										core : {
											animation : 0,
											check_callback : true,
											themes : {
												"stripes" : true
											},
											data : rootNode
										},
										"types" : {
											"#" : {
												"max_children" : 1,
												"max_depth" : -1,
												"valid_children" : [ "root" ]
											},
											"root" : {
												"icon" : "../../Images/package.png",
												"valid_children" : [ "dir",
														"vuln_dir",
														"busy_code", "code",
														"vuln_code" ],
												"max_depth" : -1,
											},
											"dir" : {
												"icon" : "../../Images/folder_secured.png",
												"valid_children" : [ "dir",
														"vuln_dir",
														"busy_code", "code",
														"vuln_code" ]
											},
											"vuln_dir" : {
												"icon" : "../../Images/folder.png",
												"valid_children" : [
														"vuln_dir",
														"busy_code", "code",
														"vuln_code" ]
											},
											"busy_code" : {
												"icon" : "../../Images/scan.png",
												"valid_children" : [
														"vuln_sink",
														"busy_sink",
														"secured_sink" ]
											},
											"code" : {
												"icon" : "../../Images/php_secured.png",
												"valid_children" : [
														"vuln_sink",
														"busy_sink",
														"secured_sink" ]
											},
											"vuln_code" : {
												"icon" : "../../Images/php.png",
												"valid_children" : [
														"vuln_sink",
														"busy_sink",
														"secured_sink" ]
											},
											"vuln_sink" : {
												"icon" : "../../Images/red.png",
												"valid_children" : []
											},
											"secured_sink" : {
												"icon" : "../../Images/green.png",
												"valid_children" : []
											},
											"busy_sink" : {
												"icon" : "../../Images/gray.png",
												"valid_children" : []
											}

										},
										plugins : [ "types", "wholerow" ],
									})
							.on(
									"select_node.jstree",
									function(e, eArgs) {
										var node = eArgs.node;
										if (node.type == "vuln_sink") {
											location.href = "SinkDetail.html?sinkId="
													+ node.data.sinkId;
										}
									});

					drawTaintSummaryPie(resJson);

					drawSinkSummaryPie(resJson);
					$('#progress_modal').nsProgress('dismiss');
					// setTimeout(function()
					// {
					// fillProjectDetailTree(projectId,taintType);
					// },5000);
				}
			});
}

function extendFileNode(node, rawNodeMap) {
	var isVuln = false;
	if (!node.children) {
		node.children = [];
	}
	if (rawNodeMap[node.id].children) {
		for (var i = 0; i < rawNodeMap[node.id].children.length; i++) {
			var rawNodeId = rawNodeMap[node.id].children[i];
			var rawChildNode = rawNodeMap[rawNodeId];
			var childNode = {
				id : rawNodeId,
				text : rawChildNode.nodeName,
				data : rawChildNode,
			};

			if (rawChildNode.isDir) {
				childNode.type = "dir";
			} else {
				if (rawChildNode.taintTask) {
					if (rawChildNode.taintTask.statusCode == "300") {
						childNode.type = "busy_code";
					} else {
						childNode.type = "code";
					}

					if (rawChildNode.taintTask.sinks) {

						childNode.children = [];
						for (var j = 0; j < rawChildNode.taintTask.sinks.length; j++) {
							var sink = rawChildNode.taintTask.sinks[j];
							if (sink.sinkId == 92415) {
								console.log(sink);
							}
							var sinkNode = {
								id : "sink_" + sink.sinkId,
								text : "sink-" + sink.sinkId,
								type : "busy_sink",
								data : sink
							};
							if (sink.isVuln === null) {
								sinkNode.type = "busy_sink";
							} else if (sink.isVuln) {
								if (sink.statusCode != 700 && sink.statusCode != 601) { //min-cut fail can count as success here
									sinkNode.type = "busy_sink";
								} else {
									sinkNode.type = "vuln_sink";
								}

								childNode.type = "vuln_code";

								isVuln = true;

							} else {
								sinkNode.type = "secured_sink";
							}
							childNode.children.push(sinkNode);
						}
					}
				}

			}

			node.children.push(childNode);

			var isChildVuln = extendFileNode(childNode, rawNodeMap);
			isVuln = isVuln || isChildVuln;

		}
	}

	if (isVuln) {
		if (node.type == "dir") {
			node.type = "vuln_dir";
		}
	}
	console.log(node.type, isVuln);
	return isVuln;
}

function fillProjectDetailTreeGrid(projectId, taintType) {
	fileListHtml = "";
	fileRootNode = null;
	var reqJson = {
		projectId : projectId,
		taintType : taintType
	};

	doJsonRequest(
			"GetProjectDetail",
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
					$("#titleProject").text(
							resJson.projectName + " - Project Details");
					$("#titleProject").append(
							$("<small>&nbsp;&nbsp;&nbsp;&nbsp;" + taintType
									+ "</small>"));
					$("#wellProjectDesc").text(resJson.projectDesc);
					setupFileTreeFunc(resJson.fileRootNode);
					fileRootNode = resJson.fileRootNode;
					fileListHtml = "";
					genFileListHtml(resJson.fileRootNode,
							resJson.fileRootNode["0x1"]);
					fileListHtml = "<div class='well'><div><ul class='nav'>"
							+ fileListHtml + "</ul></div></div>";

					$("#divProjectDetailTreeGrid").html(fileListHtml);
					$('.tree-toggle').click(function() {
						$(this).parent().children('ul.tree').toggle(200);
					});

					// $("#divProjectTable >
					// table").addClass("table").addClass("table-striped");

					// drawFileSummaryPie(resJson);
					drawTaintSummaryPie(resJson);

					drawSinkSummaryPie(resJson);

					setTimeout(function() {
						fillProjectDetailTreeGrid(projectId, taintType);
					}, 5000);
				}
			});
}

function drawTaintSummaryPie(resJson) {
	var data = google.visualization.arrayToDataTable([
			[ 'Status Name', '# of Files' ],
			[ 'Pending', resJson.numOfUntaintedTask ],
			[ 'Parsing', resJson.numOfParseTask ],
			[ 'Parse Fail', resJson.numOfParseFailTask ],
			[ 'In Taint Analysis', resJson.numOfTaintTask ],
			[ 'Complete Taint Analysis', resJson.numOfTaintCompleteTask ] ]);

	var options = {
		title : "Taint Analysis Summary (Total= " + resJson.numOfTotalTask
				+ ")",
		slices : {
			4 : {
				color : '#66aa00'
			}

		}
	};

	var chart = new google.visualization.PieChart($("#pieTaintSummary")[0]);
	chart.draw(data, options);

}

function drawSinkSummaryPie(resJson) {
	var data = google.visualization.arrayToDataTable([
			[ 'Status Name', '# of Sinks' ],
			[ 'Pending', resJson.numOfUnsanitSinks ],
			[ 'In Forward Analysis', resJson.numOfForwardSinks ],
			[ 'Forward Analysis Fail', resJson.numOfForwardFailSinks ],
			[ 'In Backward Analysis', resJson.numOfBackwardSinks ],
			[ 'Backward Analysis Fail', resJson.numOfBackwardFailSinks ],
			[ 'Patching', resJson.numOfPatchSinks ],
			[ 'Patch Fail', resJson.numOfPatchFailSinks ],
			[ 'Complete Sanit Analysis', resJson.numOfSanitCompleteSinks ], ]);

	var options = {
		title : "Tainted Sinks Summary (Total= " + resJson.numOfTotalSinks
				+ ")"

	};

	var chart = new google.visualization.PieChart($("#pieSinkSummary")[0]);
	chart.draw(data, options);

}

function genFileListHtml(rootNode, fileNode) {
	if (fileNode.isDir) {
		fileListHtml += "<li><label label-default='' class='tree-toggle nav-header'>"
				+ fileNode.nodeName + "</label><ul class='nav tree'>";
	} else {
		var className = null;
		if (fileNode.taintTask.isAllTaskFinish()) {
			if (fileNode.taintTask.isTaintFail()
					|| fileNode.taintTask.hasSanitFail()) {
				className = "failFile";
			} else if (fileNode.taintTask.hasVuln()) {
				className = "vulnFile";
			} else {
				className = "safeFile";
			}

		} else {
			className = "unfinishFile";
		}

		// find ref of this fileNode
		for (var i = 0; i < rootNode[fileNode.parent].children.length; i++) {
			var ref = rootNode[fileNode.parent].children[i];
			if (rootNode[ref].nodeId == fileNode.nodeId) {
				fileListHtml += "<li><a class=\"" + className
						+ "\" href=\"javascript: aViewSinkList_onclick('" + ref
						+ "')\">" + fileNode.nodeName;
			}
		}

	}

	if (fileNode.children.length != 0) {
		for (var i = 0; i < fileNode.children.length; i++) {
			var refChild = fileNode.children[i];
			var childNode = rootNode[refChild];
			genFileListHtml(rootNode, childNode);
		}
	}
	if (fileNode.isDir) {
		fileListHtml += "</ul></li>";
	} else {
		fileListHtml += "</a></li>";
	}

}

function setupFileTreeFunc(rootNode) {
	for ( var ref in rootNode) {

		var node = rootNode[ref];

		/* getChildNode */
		node.getChildNodes = function() {
			var result = [];
			for (var j = 0; j < node.children; j++) {
				result.push(rootNode[node.children[j]]);
			}
			return result;
		};

		/* isRoot */
		node.isRoot = function() {
			return node.nodeId == "1";
		};
		/* getParent */
		node.getParent = function() {
			if (typeof (node.parent) == "undefined") {
				return null;
			} else {
				return rootNode[node.parent];
			}
		};

		/* Funct for TaintTask */
		if (!node.isDir) {
			/* Funct for TaintTask */
			var taintTask = node.taintTask;

			/* isTaintFail */
			taintTask.isTaintFail = function() {
				return this.statusCode == 201 || this.statusCode == 302;
			};
			/* isTaintComplete */
			taintTask.isTaintComplete = function() {
				return this.statusCode == 301;
			};
			/* isTaintFinish */
			taintTask.isTaintFinish = function() {
				return this.isTaintFail() || this.isTaintComplete();
			};

			/* hasSanitFail */
			taintTask.hasSanitFail = function() {
				for (var j = 0; j < this.sinks.length; j++) {
					if (this.sinks[j].isSanitFail()) {
						return true;
					}
				}
				return false;
			};

			/* hasVuln */
			taintTask.hasVuln = function() {

				for (var i = 0; i < this.sinks.length; i++) {
					if (this.sinks[i].isVuln) {
						return true;
					}
				}
				return false;
			};

			/* isAllTaskFinish */
			taintTask.isAllTaskFinish = function() {

				if (!this.isTaintFinish()) {
					return false;
				}
				for (var j = 0; j < this.sinks.length; j++) {
					if (!this.sinks[j].isSanitFinish()) {
						return false;
					}
				}
				return true;
			};

			/* Function for sinks */
			for (var j = 0; j < taintTask.sinks.length; j++) {
				var sink = taintTask.sinks[j];

				/* isSanitFail */
				sink.isSanitFail = function() {
					return this.statusCode == 401 || this.statusCode == 501
							|| this.statusCode == 601;
				};

				/* isSinkFinish */
				sink.isSanitFinish = function() {
					return this.isSanitFail() || sink.isSanitComplete();
				};

				/* isSinkComplete */
				sink.isSanitComplete = function() {
					return this.statusCode == 700;
				};
			}
		}

	}
}