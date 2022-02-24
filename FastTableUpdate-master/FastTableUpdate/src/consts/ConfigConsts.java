package consts;

public interface ConfigConsts {
	
//	public static int DEFAULT_JDBC_BATCH_WRITE_SIZE = 	10_000;
//	public static int DEFAULT_JDBC_STMT_FETCH_SIZE 	= 	10_000;
	public static int MAP_BUILD_PROGRESS_PRINT_SIZE =  250_000;
	public static int FACTOR_FETCH_SIZE =  8;
	
	// Command line system parameters
	public static String WRITEBATCHSZ_PROPKEYSTR = "tufbsfw";
	public static String CMTATBATCHSZ_PROPKEYSTR = "tufbsdc";
	public static String EXCSRVTHDCNT_PROPKEYSTR = "tufestc";
	
	
	public static String IDX_SUFFIX =  "ftuidx";
	public static String NEXT_LINE =  "\r\n";
	
	public static int ORA_IDX_ALREADY_PRESENT = 1408;
	public static int ORA_IDX_NAME_CONFLICT = 955;
	
	public static int TIMEOUT = 60; //mins.

}
