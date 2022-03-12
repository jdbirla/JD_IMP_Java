package util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import main.Client;

import org.slf4j.LoggerFactory;

import consts.ConfigConsts;
import excphndl.SQLExcpHandler;

public class Helper {
	public static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(Helper.class);

	static int batsz = 0;
	static int estc= 0;
	
	public static int getBatchSizeForUpdate() {
		if (batsz != 0)
			return batsz;

		try {

			batsz = 100;

			String v = System.getProperty(ConfigConsts.WRITEBATCHSZ_PROPKEYSTR).trim();
			logger.debug(ConfigConsts.WRITEBATCHSZ_PROPKEYSTR+"="+v);
			batsz = Integer.parseInt(v);

		} catch (Throwable e) {
			logger.warn("Exception in getting write batch size, it will use default");
		}
		
		logger.debug("FetchSizeForWrite={}",batsz);
		return batsz;
	}

	public static void setDbConSessParallelDml(Connection con) throws SQLExcpHandler{
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("ALTER SESSION ENABLE PARALLEL DML");
			logger.debug(" session altered to enable parallel DML! ");
		} catch (SQLException e) {
			throw new SQLExcpHandler(e, "setting parallel session for DML failed.");
		}
	}
	
	
	public static boolean isCommitAfterBatchSize() {
		boolean isCmtPerBatSz= true; //needed to commit after each batch execute and parallel execution. 
		try {
			String v = System.getProperty(ConfigConsts.CMTATBATCHSZ_PROPKEYSTR).trim();
			logger.debug(ConfigConsts.CMTATBATCHSZ_PROPKEYSTR+"="+v);
			isCmtPerBatSz = v.equalsIgnoreCase("No")?false:true;
		} catch (Throwable e) {
			logger.warn("Exception in getting whether commit at batchsize, it will use default=true(commit at batch size)");
		}
		logger.debug("isCmtPerBatSz={}",isCmtPerBatSz);
		return isCmtPerBatSz;
	}

	public static int getThreadCount(){
		if(estc !=0) return estc; // this method expected to be called multiple times. So if it is already calculated the thread count one per JVM start then no need to do again.
		try {
			estc = Integer.parseInt(System.getProperty(ConfigConsts.EXCSRVTHDCNT_PROPKEYSTR).trim());
		} catch (Throwable e) {
			logger.warn("Exception in getting executor service thread count, it will use default");
		}
		
		logger.debug("estc={}",estc);
		return estc;
	}
	
	/**
	 * A method to split the list into chunks.
	 * cpratihast 2021/08/18 11:16:41
	 * @param list
	 * @param chunk
	 * @return
	 */
	public static List<String>[] getChunkedList(HashSet<String> list, int chunk){
		List<String> inlist = list.stream().collect(Collectors.toList()); // create a copy of incoming list to avoid any issue.
		
		List<String>[] res = null;
		int sz = inlist.size(); 
		if (chunk > sz){
			res = (ArrayList<String>[])new ArrayList[1];
			res[0] = new ArrayList<String>(inlist);
			return res;
		}
		
		int n 	= sz / chunk;
		n = n*chunk < sz ? n+1 : n;
		res 	= (ArrayList<String>[])	new ArrayList[n]; //make, return list, of right size.
		
		int st	= 0;
		int end = chunk;
		for(int i = 0; i<n;i++){
			st = i * chunk;
			end = Math.min(st + chunk, sz);
			res[i] = new ArrayList<String>(inlist.subList(st, end));
		}
				
		return res;
	}
}
