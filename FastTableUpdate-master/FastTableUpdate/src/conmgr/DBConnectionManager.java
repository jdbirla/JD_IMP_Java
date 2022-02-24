package conmgr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;

import main.Client;

import org.slf4j.LoggerFactory;

public class DBConnectionManager {
	
	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(DBConnectionManager.class);  

	public static Connection getConnection(String hostnm, String sid, String port,
			String uid, String pwd) {
		Connection conn = null;
		
		try {
			Instant st = Instant.now();
			conn = DriverManager.getConnection("jdbc:oracle:thin:@" + hostnm
					+ ":"+port+":" + sid,uid, pwd);
			String conTime = "Time(ms) in getConnection:"+Duration.between(st, Instant.now()).toMillis();
			conn.setClientInfo("OCSID.CLIENTID","FTU_ConMgr_CI");
			conn.setClientInfo("OCSID.MODULE","FTU_ConMgr_Module");
			conn.setClientInfo("OCSID.ACTION","FTU_ConMgr_Action");
			
			if(testConnection(conn)) logger.info("Connection and test query okay."+conTime);

		} catch (SQLException e) {
			logger.error("SQL State: %s\n%s", e.getSQLState(),
					e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return conn;
	}
	
	private static void sleep(int sec){
		try {
			Thread.sleep(1000*sec);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static boolean testConnection(Connection conn){
		String qry =  "select count(1) from vm1dta.bsscpf  ";
		boolean res = false;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute(qry);
			res = true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}finally{
			if(null!=stmt)
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		return res;
	}
	
	

}
