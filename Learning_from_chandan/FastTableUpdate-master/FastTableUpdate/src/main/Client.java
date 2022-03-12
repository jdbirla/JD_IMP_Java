package main;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import update.TableUpdater;
import update.TableUpdaterInExecutor;
import util.Helper;
import lookup.LookupLoader;
import conmgr.DBConnectionManager;
import excphndl.GenericCustomExcpHandler;
import excphndl.SQLExcpHandler;

public class Client {
	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(Client.class);  
	public static void main(String[] args)  {
		//command line parameters for the connection. java main.Client localhost IGPA1 1522 VM1DTA vm1dta12#$ ZCLNPF_TRY UNIQUE_NUMBER LSURNAME
		String hn 	= args[0]; // hostname ex: localhost
		String sid 	= args[1]; // sid	ex: IGPA1
		String port = args[2]; // port ex: 1521
		String uid 	= args[3]; // uid ex: VM1DTA
		String pwd 	= args[4];
		
		String tbl 		= args[5]; // the table which shall be updated
		String keycol 	= args[6]; // its key column of the table used in update.
		String updcol 	= args[7]; // the column which will be changed.
		String q 		= args[8]; // the lookup query.
		
		Instant st = Instant.now(),lookupCompleteLoadOrgStart = null,loadOrgCompleteUpdateStart = null;
		
		Connection con 		= null;
		Connection conarr[] = null;
		
		try {
			
			//Now get the connection.
			con 	= DBConnectionManager.getConnection(hn, sid, port, uid, pwd);
			
			//When going to use parallel update using executor have to build that many connections.
			int degree 	= Helper.getThreadCount();
			conarr		= new Connection[degree];
			for(int i =0;i<degree;i++){
				conarr[i] = DBConnectionManager.getConnection(hn, sid, port, uid, pwd);
				conarr[i].setClientInfo("OCSID.ACTION","FTU_ConMgr_Act_UpdInThd_"+i);
			}
			
			logger.info("TimeTakenForBuilding all connections={} milisec",Duration.between(st, Instant.now()).toMillis());
			
			
			
			con.setClientInfo("OCSID.ACTION","FTU_ConMgr_Action_Lookup");
			//Next load the lookup data, using the query as we wish.
			LookupLoader.loadLookup(q, con);
			
			lookupCompleteLoadOrgStart = Instant.now();
			
			// load original value from table in memory so that insert/update statement can be done by doing the lookup for the original values and get the update values.
//			TableUpdater tu = new TableUpdater(tbl, keycol, updcol); // the table name, its key column name and then the column to be updated with lookup values.
			TableUpdaterInExecutor tu = new TableUpdaterInExecutor(tbl, keycol, updcol,conarr); // the table name, its key column name and then the column to be updated with lookup values.
			
			tu.loadOrgValuesInMemory(con);	
				
			loadOrgCompleteUpdateStart = Instant.now();
			
			
			HashMap<String, String> lookupMap = LookupLoader.getLookupMap(); //to be used in update.
			tu.updatedValuesInTableByColumnValues(lookupMap);
//			con.setClientInfo("OCSID.ACTION","FTU_ConMgr_Action_Update");
//			Helper.setDbConSessParallelDml(con);// setting connection session to be parallel enabled.
//			tu.updatedValuesInTableByColumnValues(lookupMap, con );
//			tu.updatedValuesInTableByKey(lookupMap, con,ConfigConsts.DEFAULT_JDBC_BATCH_WRITE_SIZE );
		
		}catch	(SQLExcpHandler e)	{
			logger.error("SQLException!"+e.getMsg());	
		}catch 	(SQLException 	e) 	{
			logger.error("Issue with connection!");e.printStackTrace();
		} catch (GenericCustomExcpHandler e) {
			logger.error("Problem in parallel update!"+e.getMsg());
		}finally{
			if (null != con) {
				try {
					con.close();
					for(Connection c : conarr){
						c.close();
					}
				} catch (SQLException e) {
					System.out.println("Failed to close the connection! Connection leak can cause issue!");e.printStackTrace();
				}
			}
		}
		
		logger.info("Total Time(ms): LookupLoader ={} LoadOrgValues = {} Update = {}",
				Duration.between(st, lookupCompleteLoadOrgStart).toMillis(),
				Duration.between(lookupCompleteLoadOrgStart, loadOrgCompleteUpdateStart).toMillis(),
				Duration.between(loadOrgCompleteUpdateStart, Instant.now()).toMillis());
	}

}


