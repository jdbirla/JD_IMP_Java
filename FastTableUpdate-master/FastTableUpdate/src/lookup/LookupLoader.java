package lookup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

import main.Client;

import org.slf4j.LoggerFactory;

import conmgr.DBConnectionManager;
import consts.ConfigConsts;
import excphndl.SQLExcpHandler;

/**
 * This class will load the lookup data.
 * @author cpratihast
 *
 * 2021/08/09 0:09:13
 */
public class LookupLoader {
	
	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(LookupLoader.class);  
	
	
	private static HashMap<String,String> kvLookupMap = null;;
	private static int countInLookUp = 100_000;
	
	/**
	 * While saving the lookup value the original value is trimmed.
	 * cpratihast 2021/08/09 6:07:29
	 * @param usingQry
	 * @param conn
	 * @param fs
	 * @throws SQLExcpHandler 
	 * @throws SQLException
	 */
	private static void loadLookup(String usingQry, Connection conn, int fs) throws SQLExcpHandler {
		try {
			kvLookupMap = new HashMap<String, String>(countInLookUp);
			Statement stmt = conn.createStatement();
			stmt.setFetchSize(fs);

			Instant st = Instant.now();

			ResultSet rs = stmt.executeQuery(usingQry);

			logger.info("loadLookup:executequery time(ms): "	+ Duration.between(st, Instant.now()).toMillis() + " fetchSize:" + fs + " Next it will loop the result set and store in map.");
			st = Instant.now();
			int i = 0;
			int j = 0;
			String temp = null;
			while (rs.next()) {
				temp = rs.getString(1);
				if (null != temp)
					temp = temp.trim();
				kvLookupMap.put(temp, rs.getString(2));
				// for debugging purpose print information when this is taking long time.
				if (i == ConfigConsts.MAP_BUILD_PROGRESS_PRINT_SIZE) {
					i = 0;
					logger.debug("Loading LookupMap:" + j + "/" + countInLookUp + " fetchsz:" + fs);
				}
				i++;
				j++;
			}
			logger.info("Loading completed. hashmap size:"	+ kvLookupMap.size() + " filling time(ms):"	+ Duration.between(st, Instant.now()).toMillis());

			if (null != stmt)
				stmt.close();
			if (null != rs)
				rs.close();
		} catch (SQLException e) {
			throw new SQLExcpHandler(e, "loadLookup has failed.");
		}
	}
	
	public static void loadLookup(String usingQry, Connection conn) throws SQLExcpHandler{
		Statement stmt = null;
		try{
			stmt = conn.createStatement();
			String cq = " with T as ( "+usingQry+" ) select count(1) from T ";
			logger.debug(cq);
			ResultSet rs = stmt.executeQuery(cq);
		
			while(rs.next()){		countInLookUp = rs.getInt(1); logger.info("count of rows in lookup data: "+countInLookUp);		}
		
			stmt.close();rs.close();
			 	
		}catch(SQLException e){
			throw new SQLExcpHandler(e, "Getting the count from the supplied lookup table failed!");
		}
		int fs = countInLookUp/ConfigConsts.FACTOR_FETCH_SIZE ; fs = fs < 100 ? 100 : fs;
//		int fs = ConfigConsts.DEFAULT_JDBC_STMT_FETCH_SIZE; 
		loadLookup( usingQry,  conn,  fs);
	}
		
	public static HashMap<String,String> getLookupMap(){
		return kvLookupMap;
	}

/****************************************************************************************************************************	
/****************************************** TESTING TESTING
 * This is for testing purpose.
 * cpratihast 2021/08/09 2:35:39
 * @param args
 */
	public static void main(String[] args) {
		String q = " select TRIM(a.surname) as org_surname, b.surname as masked_surname from clntpf a, clntpf_masked b "+ 
					" where a.unique_number = b.unique_number ";
		Connection conn = DBConnectionManager.getConnection("localhost", "IGPA1","1522", "VM1DTA", "something");
		try {
			LookupLoader.loadLookup(q, conn, 5000);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LookupLoader.getLookupMap().entrySet().stream().limit(5).forEach(e->System.out.println(e.getKey()+":"+e.getValue()));
		
	}
}
