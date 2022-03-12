package base;

import static CustomLogger.CustomLogger.getLogger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;


import com.csc.fsu.general.procedures.Sbmbchonl;
import com.csc.groupasia.runtime.variables.GroupAsiaBatchAppVars;

/**
 * The class do the processing to check the status of the submitted IG Batch.
 * @author Chandan K Pratihast
 * @since Mar 2, 2020 10:31:27 AM
 
* **************************************************************************************|
* | Mod Date			| Mod Desc                                  				  	|
* **************************************************************************************|
* 	20200614          	|BugFix:20200614:Chandan	v2.4.6								|
*  						|More information for the status is obtained.      		 		|	
*                       |            													|
* **************************************************************************************/
public class StatusChkAction  extends BaseAction{
	private static StatusChkAction sca = null;
	/**
	 * To make singleton, made the constructor as static.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private StatusChkAction() throws FileNotFoundException, IOException{
		setIGBatchSystemProperties();
	}
	
	/**
	 * Singleton approach to get only one instance of this Action.
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static StatusChkAction getInstance() throws FileNotFoundException, IOException {
		lock.lock();
		if (null == sca) {
			sca = new StatusChkAction(){};
		}
		lock.unlock();
		return sca;
	}	
	/**
	 * Return is the status. The 2 digit string as return by IG (BSSCPF table).
	 */
	public String execute(String batchName, String bizDate, String userid, String tout) throws FileNotFoundException, IOException {
		Instant start = Instant.now();
		getLogger().debug(" BatchName: {}| BizDate: {} | UserId:{} | TimeOut:{} | {}",  batchName,bizDate,userid,tout, Msg.START_MSG.getValue() );
		String ret = "00";
		getLogger().debug(" Creating GroupAsiaBatchAppVars. " + Msg.START_MSG.getValue());
		appVars = new GroupAsiaBatchAppVars("CSCGroupAsiaWeb");
		getLogger().debug(" Creating GroupAsiaBatchAppVars. " + Msg.END_MSG.getValue());
		
		//v2.4.6: more information about the running batch is added.
		LinkedHashMap<String,String> resMap = new LinkedHashMap<String,String>();
		try (Connection con = appVars.getDBConnection("CheckStatusFromSubmitBatch");
				PreparedStatement prstmt = con.prepareStatement(buildQry()); // v2.4.6
		) {
			prstmt.setString(1, batchName.trim());
			prstmt.setString(2, userid.trim());
			ResultSet rs = prstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData(); 	// v2.4.6
			int COL_CNT = rsmd.getColumnCount(); 		// v2.4.6
			while (rs.next()) {
				for (int i = 1; i <= COL_CNT; i++) {	//<v2.4.6>
					resMap.put(rsmd.getColumnName(i), rs.getString(i));
				}// <v2.4.6/>
			}
			
			rs.close();
		} catch (Exception e) {
			getLogger().error(" BatchName: {}| BizDate: {} | UserId:{} | TimeOut:{} | {} ", batchName, bizDate, userid,
					tout, Msg.EXCEPTION_MSG.getValue());
			e.printStackTrace();
		}
		appVars.finallyFreeAllAppVarsConnections();appVars=null; //v2.4.6
		Instant end = Instant.now();
		getLogger().info(" BatchName: {}| BizDate: {} | UserId:{} | TimeOut:{} | {} | TimeTaken(secs) {} ",  batchName,bizDate,userid,tout, Msg.START_MSG.getValue(), java.time.Duration.between(start, end).toMillis()/1000 );
		return buildResponseStringToClient(resMap); //v2.4.6: pass map to build the json.
	}
	
	/**
	 * The query to check the status. Note that the status shall be kept as first column. The WaitCompletAction expects first column be status.
	 * @author Chandan K Pratihast
	 * @Jun 14, 2020 1:26:32 AM v2.4.6
	 * @return
	 */
	private String buildQry() {
		String qry=
				"with A as (select * from bsscpf where TRIM(BSCHEDNAM) = ? and TRIM(BSUSERNAME) = ? order by datime  desc fetch first 1 rows only), " + 
				"B as (select * from bsprpf  order by datime desc fetch first 1 rows only) " + 
				"select A.BSHDSTATUS,A.BSCHEDNAM, A.BSCHEDNUM,  A.BSUSERNAME, B.BPROCESNAM, B.BSHDTHRDNO, B.BPCYCLCNT, B.BCONTOT01 from A, B " + 
				"where A.BSCHEDNAM = B.BSCHEDNAM and A.BSCHEDNUM=B.BSCHEDNUM";
		return qry;
	}
	
}
