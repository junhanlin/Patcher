package edu.nccu.soslab.Patcher;



public class StatusCode
{
	public final static int NO_STATUS=0;
	public final static int EXTRACT_COMPLETE=100;
	public final static int EXTRACT_FAIL=101;
	public final static int PARSE=200;
	public final static int PARSE_FAIL=201;
	public final static int TAINT=300;
	public final static int TAINT_COMPLETE=301;
	public final static int TAINT_FAIL=302;
	public final static int FORWARD=400;
	public final static int FORWARD_FAIL=401;
	public final static int BACKWARD=500;
	public final static int BACKWARD_FAIL=501;
	public final static int PATCH=600;
	public final static int PATCH_FAIL=601;
	public final static int SANIT_COMPLETE=700;

}
