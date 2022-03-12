package update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import main.Client;

import org.slf4j.LoggerFactory;

import util.Helper;
import consts.ConfigConsts;
import excphndl.SQLExcpHandler;

public class TableUpdater {
	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(TableUpdater.class); 

	protected String tn 			= null;
	protected String kcolnm 		= null;
	protected String updtcolnm 	= null;

	protected String[] kcolv 		= null; // hold original key's value
	protected String[] targetcolv = null; // hold original value from the target column.
	protected int countOfRows = 0;
	
	protected String createdIndexName = null;
	
	protected HashSet<String> targetColDistinctVal= null; // to hold distinct original values in the target table-column. to be used in update by values. 
	
	
	private TableUpdater(){}; // do not let this constructor to be used.
	
	/**
	 * @param tableName : The table name which needs to be updated.
	 * @param keyCol : The column which is the key of the table.
	 * @param colNameToUpdate: The column for which the values will be updated.
	 */
	public TableUpdater(String tableName, String keyCol, String colNameToUpdate){
		tn = tableName;
		kcolnm = keyCol;
		updtcolnm = colNameToUpdate;
	}
		
	
	/**
	 * While loading original values trim the values in the target column.
	 * cpratihast 2021/08/09 6:04:47
	 * @param conn
	 * @throws SQLExcpHandler 
	 */
	public void loadOrgValuesInMemory(Connection conn) throws SQLExcpHandler {
		try{	
			Statement stmt = conn.createStatement();
			ResultSet rs = null;
			String q =" select count(1) from "+tn;
			rs = stmt.executeQuery(q);
			while(rs.next()){
				countOfRows = rs.getInt(1);
				logger.info("count of rows in original table: "+tn+" : "+countOfRows);
			}
			stmt.close();
			rs.close();
			
			kcolv 		= new String[countOfRows];
			targetcolv 	= new String[countOfRows];
			targetColDistinctVal = new HashSet<String>(countOfRows);
			Instant st = Instant.now();
			int fs = countOfRows/ConfigConsts.FACTOR_FETCH_SIZE ; fs = fs < 100 ? 100 : fs; 
//			int fs = ConfigConsts.DEFAULT_JDBC_STMT_FETCH_SIZE; 
			q =" select "+ kcolnm+","+updtcolnm+" from "+tn;
			stmt = conn.createStatement();
			stmt.setFetchSize(fs);
			rs = stmt.executeQuery(q);
			int i = 0;
			int j=0,k=0; //for debug purpose. when it is long running then to print message about the progress.
			String v = null;
			while(rs.next()){
				
				v 			= rs.getString(2);
				if(null!=v){ // if we have valid values add in the array with the same index.
					kcolv[i]	= rs.getString(1);
					targetcolv[i]= v.trim(); 
					targetColDistinctVal.add(v); // in the distinct values do not trim and use as is. 
					i++;
				}
				
				//Below line is just to show the progress.
				if(j==ConfigConsts.MAP_BUILD_PROGRESS_PRINT_SIZE){j=0;logger.debug("Loading OriginalValues: "+k+"/"+countOfRows+" fetchsz:"+fs);
				}j++;k++;
			}
			logger.info("Loading OriginalValues In Memory Done!:"+k+" Time(ms):"+Duration.between(st, Instant.now()).toMillis());
			stmt.close();
			rs.close();
		}catch(SQLException e){
			throw new SQLExcpHandler(e, "Loading OriginalValues in memory has failed!");
		}
	}
	
	/**
	 * Lookup the original values in the lookup map and set it in the prepared statement and do the batch update.	
	 * cpratihast 2021/08/09 14:31:27
	 * @param kvLookupMap : the map created externally.
	 * @param conn
	 * @param batchSize : auto calculated. Total count / 8.
	 * @throws SQLExcpHandler 
	 * @throws SQLException
	 */
	public void updatedValuesInTableByColumnValues(HashMap<String,String> kvLookupMap, Connection conn) throws SQLExcpHandler{
		
		createIndexOnUpdateColumn(conn);
		
		try{
			int bszcontrol = 0; int complCount = 0; int totSize = targetColDistinctVal.size();
			logger.info("Starting Update (by values) distinct values:"+totSize);
			
			String q =" update /*+ parallel("+tn+") */"+tn +" set "+updtcolnm +" = ? where "+updtcolnm+"=?";
			PreparedStatement ps = conn.prepareStatement(q);
			Iterator<String> iter = targetColDistinctVal.iterator();
			String tmpV = null;
									
			boolean didAutoCommitSetToFalseHere = false;
			if(conn.getAutoCommit()){
				conn.setAutoCommit(false);// do the update in trx to avoid multiple commit
				didAutoCommitSetToFalseHere = true;
			}
			
			
			int batchSize = Helper.getBatchSizeForUpdate();
			boolean shallCommitToAvoidORA12838 = Helper.isCommitAfterBatchSize();
			
			while(iter.hasNext()){
				tmpV = iter.next();
				
				ps.setString(1, kvLookupMap.get(tmpV.trim()));
				ps.setString(2, tmpV);
				ps.addBatch();
				
				if(bszcontrol == batchSize){ // if it has reached the batch size then execute the query
					bszcontrol = 0; //reset the counter.
					ps.executeBatch();
					ps.clearBatch();
					if(shallCommitToAvoidORA12838){
						conn.commit();	
					}
					logger.debug(" batch update (by values) going on!"+ (++complCount*batchSize) +"/"+totSize+" batchsize:"+batchSize);
				}
				bszcontrol ++; //counting the number of updates enqueued.
			}
			
			
			//when there is leftover those will be executed below.
			ps.executeBatch();
			ps.clearBatch();
			conn.commit();	
			if(didAutoCommitSetToFalseHere)	conn.setAutoCommit(true);// revert the state of connection auto commit if we have changed in this method.
			logger.debug(" batch update (by values) done: row count "+countOfRows+" distinct value count:"+totSize);
		}catch(Exception e){
			throw new SQLExcpHandler(e, "Updating (by values) the column in target table failed!");
		}finally{
			dropIndexOnUpdateColumn(conn);
		}
		
	}
	

	/**
	 * Lookup the original values in the lookup map and set it in the prepared statement and do the batch update.	
	 * cpratihast 2021/08/09 14:31:27
	 * @param kvLookupMap : the map created externally.
	 * @param conn
	 * @param batchSize : auto calculated. Total count / 8.
	 * @throws SQLExcpHandler 
	 * @throws SQLException
	 */
	public void updatedValuesInTableByKey(HashMap<String,String> kvLookupMap, Connection conn, int batchSize) throws SQLExcpHandler {
		try{
			String q =" update "+tn +" set "+updtcolnm +" = ? where "+kcolnm+"=?";
			PreparedStatement ps = conn.prepareStatement(q);
			int bszcontrol = 0;
			int sz = kcolv.length;
			for(int i=0;i<sz;i++){
				ps.setString(1, kvLookupMap.get(targetcolv[i]));
				ps.setString(2, kcolv[i]);
				ps.addBatch();
				
				if(bszcontrol == batchSize){ // if it has reached the batch size then execute the query
					bszcontrol = 0; //reset the counter.
					ps.executeBatch();
					ps.clearBatch();
					logger.debug(" batch update done: i:"+i+"/"+countOfRows);
				}	
				bszcontrol ++; //counting the number of updates enqueued.
			}
			
			//when there is leftover those will be executed below.
			ps.executeBatch();
			ps.clearBatch();
			conn.commit();	
			logger.info(" batch update done: "+countOfRows);
		}catch(SQLException e){
			throw new SQLExcpHandler(e, "Updating (by values) the column in target table failed!");
		}
	}

	private void createIndexOnUpdateColumn(Connection conn) throws SQLExcpHandler {
		try {
			logger.info("Creating index..."); Instant st = Instant.now();
			createdIndexName = tn + updtcolnm +ConfigConsts.IDX_SUFFIX;
			String ddl_crt_idx = "create index " + createdIndexName + " on "
					+ tn + "(" + updtcolnm + ") parallel nologging";
			conn.createStatement().execute(ddl_crt_idx);
			logger.info("Creating index Done! Time taken(ms)"+Duration.between(st, Instant.now()).toMillis());
		} catch (SQLException e) {
			if(e.getErrorCode() == ConfigConsts.ORA_IDX_ALREADY_PRESENT){
				logger.info("Index already present!");
			} else throw new SQLExcpHandler(e, " Create Index Failed."+tn+"."+updtcolnm);
		}
	}
	
	private void dropIndexOnUpdateColumn(Connection conn) throws SQLExcpHandler {
		try {
			logger.info("Dropping index...");
			String ddl_crt_idx = "drop index " + createdIndexName;
			conn.createStatement().execute(ddl_crt_idx);
			logger.info("Dropping index Done!");
		} catch (SQLException e) {
			throw new SQLExcpHandler(e, " Drop Index Failed. Please drop it *manually*!"+createdIndexName);
		}
	}
}
// Prospect: This can update any table fast. Only dependency is the external provided lookup map.

