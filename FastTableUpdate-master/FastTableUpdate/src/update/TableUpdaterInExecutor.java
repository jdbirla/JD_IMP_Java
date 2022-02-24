package update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import conmgr.DBConnectionManager;
import consts.ConfigConsts;
import util.Helper;
import excphndl.GenericCustomExcpHandler;
import excphndl.SQLExcpHandler;

public class TableUpdaterInExecutor extends TableUpdater {
	
	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(TableUpdaterInExecutor.class);
	
	ExecutorService es = null;
	Connection conarr[] = null;
	private int parallel_degree = 2;
	
	
	/*
	 * @param tableName : The table name which needs to be updated.
	 * @param keyCol : The column which is the key of the table.
	 * @param colNameToUpdate: The column for which the values will be updated.
	 */
	public TableUpdaterInExecutor(String tableName, String keyCol, String colNameToUpdate,  Connection ca[]){
		super(tableName, keyCol, colNameToUpdate);
		parallel_degree = ca.length;
		es = Executors.newFixedThreadPool(parallel_degree);
		this.conarr = ca;
	}
	
	/*
	 * Split the laod into number of parallel_degree and the perform below for each load in executor.
	 * Lookup the original values in the lookup map and set it in the prepared statement and do the batch update.	
	 * cpratihast 2021/08/09 14:31:27
	 * @param kvLookupMap : the map created externally.
	 * @param conn
	 * @param batchSize : auto calculated. Total count / 8.
	 * @throws SQLExcpHandler 
	 * @throws SQLException
	 */
	public void updatedValuesInTableByColumnValues(HashMap<String,String> kvLookupMap) throws GenericCustomExcpHandler {
		// the parent class has the method to build the distinct value so use that.
		int inloadSz = targetColDistinctVal.size();
		int chunk = inloadSz / parallel_degree;
		chunk = chunk * parallel_degree  < inloadSz ? chunk + 1 : chunk; // to do Math.ciel kind of operation. so that each parallel will get load.
		
		List<String>[] arrOfList = Helper.getChunkedList(targetColDistinctVal, chunk);
		
		int bs = Helper.getBatchSizeForUpdate();
		String q =" update "+tn +" set "+updtcolnm +" = ? where "+updtcolnm+"=?";
		int i = 0;
		List<CompletableFuture> cflist = new ArrayList<CompletableFuture>();
		
		logger.info("Submitting update task in executors: parallel_degree={},load in each thread(chunk)={} batchSize={}",parallel_degree,chunk, bs);
		for (List<String> load : arrOfList) {
			ExecutorUpdateTask eut = new ExecutorUpdateTask();
			Connection con = conarr[i];
			CompletableFuture<Object> cf = CompletableFuture.supplyAsync(()->eut.doUpdate(load, con, q, kvLookupMap, bs));
			cflist.add(cf);
			i++;
		}
		
		logger.info("Submitted update task. Now it will wait for its completion.");
		
		i=0;
		for(CompletableFuture cf : cflist){
			try {
				Boolean o = (Boolean)cf.get(ConfigConsts.TIMEOUT, TimeUnit.MINUTES);
				logger.debug("executor update completed for {} / {}, isSuccesfullCompletion={}",++i,parallel_degree,o.booleanValue());
			} catch (InterruptedException e) {
				throw new GenericCustomExcpHandler(e, "completable future parallel work interruped.");
			} catch (ExecutionException e) {
				throw new GenericCustomExcpHandler(e, "completable future parallel work executionException.");
			} catch (TimeoutException e) {
				throw new GenericCustomExcpHandler(e, "completable future parallel work took more time than "+ConfigConsts.TIMEOUT+" mins!");
			}
		}
	}

}


//----------------------------------------- Task to be executed by ExecutorService------------------------
/**
* This task is supposed to run in the executors which will be executed in worker threads.
* @author cpratihast
*
* 2021/08/18 8:39:51
*/
class ExecutorUpdateTask {
	public static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(ExecutorUpdateTask.class);

	// hold local variables
	private boolean didAutoCommitSetToFalseHere = false;


	// update
	public Object doUpdate(List<String> load, Connection con, String pstmq,
			HashMap<String, String> kvLookupMap, int bs)  {
		try {
			int bszcontrol = 0;
			int complCount = 0;
			int totSize = load.size();
			PreparedStatement ps = con.prepareStatement(pstmq);
			int batchSize = bs;

			if (con.getAutoCommit()) {
				con.setAutoCommit(false);// do the update in trx to avoid
											// multiple commit
				didAutoCommitSetToFalseHere = true;
			}

			for (String item : load) {

				ps.setString(1, kvLookupMap.get(item.trim()));
				ps.setString(2, item);

				ps.addBatch();
				if (bszcontrol == batchSize) { // if it has reached the batch
												// size then execute the query
					ps.executeBatch();
					ps.clearBatch();
					bszcontrol = 0; // reset the counter.
					logger.debug(" batch update (by values) going on!"
							+ (++complCount * batchSize) + "/" + totSize
							+ " batchsize:" + batchSize);
				}
				bszcontrol++; // counting the number of updates enqueued.
			}

			// when there is leftover those will be executed below.
			ps.executeBatch();
			ps.clearBatch();
			con.commit();
			logger.debug(" batch update (by values) done. distinct value count:"
					+ totSize);

			
		} catch (Exception e) {
//			throw new SQLExcpHandler(e,	"Updating (by values) the column in target table failed!");
			logger.error("Updating (by values) the column in target table failed!");
			return Boolean.FALSE;
		} finally {

			if (didAutoCommitSetToFalseHere)
				try {
					con.setAutoCommit(true);// revert the state of connection
											// auto commit if we have changed in
											// this method.
				} catch (SQLException e) {
//					throw new SQLExcpHandler(e,	"Setting connection back to autoCommit true failed!");
					logger.error("Setting connection back to autoCommit true failed!");
					return Boolean.FALSE;
				}

		}
		
		return Boolean.TRUE;
	}
}

