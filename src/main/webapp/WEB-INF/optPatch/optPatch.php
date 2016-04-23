<?PHP
include("getInputVars.php");
$__PATCHER__JAVA_BIN = "/usr/bin/java";
$__PATCHER__optPatchs = array();
$__PATCHER__SUCCESS_EXIT_VALUE = 0;



function initOptPatch($sinkId,$inputVars)
{
	global $__PATCHER__JAVA_BIN;
	global $__PATCHER__optPatchs;
	global $__PATCHER__SUCCESS_EXIT_VALUE;

	$dir = realpath(dirname(__FILE__));
	$whiteAutoPath = $dir.DIRECTORY_SEPARATOR."whiteAutos".DIRECTORY_SEPARATOR."sink_".$sinkId."_WHITE.auto";
	$optPatchJarPath = $dir.DIRECTORY_SEPARATOR."OptPatch.jar";
	
	$exitValue =-1;
	$__PATCHER__optPatchs["$sinkId"]=array();
	
	$cmdLine = $__PATCHER__JAVA_BIN." -jar ".$optPatchJarPath." ".$whiteAutoPath." ";
	
	for($i =0;$i<count($inputVars);$i++)
	{
		$cmdLine.="\"".$inputVars[$i]."\" ";	
	}
	
	$lastLog = exec($cmdLine,$__PATCHER__optPatchs["$sinkId"],$exitValue);
	

	if($exitValue != $__PATCHER__SUCCESS_EXIT_VALUE)
	{
		
		return false;
	}
	
	
	return true;

}

function optPatch(&$inputVar,$sinkId,$inputNo)
{
	global $__PATCHER__optPatchs;
	if(!isset($__PATCHER__optPatchs["$sinkId"]))
	{
		$inputVars = getInputVars($sinkId);
		
		 if(!initOptPatch($sinkId,$inputVars))
		{
			//fail to patch
			return;
		}
	
	}
	
	$result =  $__PATCHER__optPatchs["$sinkId"][$inputNo];
	return $result;
}

?>
