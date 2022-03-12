package base;

import static CustomLogger.CustomLogger.getLogger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.csc.fsu.general.procedures.Sbmbchonl;
import com.csc.fsu.general.recordstructures.Sbmbonlrec;
import com.csc.groupasia.runtime.variables.GroupAsiaBatchAppVars;
import com.quipoz.COBOLFramework.job.JobInfo;
import com.quipoz.COBOLFramework.messaging.SysOperatorMessageFormatter;
import com.zaxxer.hikari.HikariDataSource;

import CustomLogger.CustomLogger;
import ch.qos.logback.core.util.Duration;

/**
 * This action does the submit of IG Batch.
 * In 2.3.1 Updated the response string to be more informative.
 * In 2.4.6 GroupAsiaBatchAppVars  appVars needs to be initialized in execute section.
 * @author Chandan K Pratihast
 * @since Mar 4, 2020 11:01:56 AM
 * @version 2.4.6
 * 
* **************************************************************************************|
* | Mod Date			| Mod Desc                                  				  	|
* **************************************************************************************|
* 	20200614          	|BugFix:20200614:Chandan	v2.4.6								|
*  						|appvars in execute needs to be called every time, as the end	|	
*                       |clean this and make it null.									|
* **************************************************************************************/
public class SubmitAction extends BaseAction{
	private static final Logger LOGGER = CustomLogger.getLogger();
	private static Sbmbonlrec sbmbonlrec;
	private static SubmitAction sa = null;
	/**
	 * To make singleton, made the constructor as static.
	 */
	private SubmitAction() throws FileNotFoundException, IOException{
		setIGBatchSystemProperties();
//		sbmbonlrec = new Sbmbonlrec();
	}
	/**
	 * Singleton approach to get only one instance of this Action.
	 * @return
	 */
	public static SubmitAction getInstance() throws FileNotFoundException, IOException{
		lock.lock();
		if (null == sa) {
			sa = new SubmitAction();
		}
		lock.unlock();
		return sa;
	}	
	
		
	/**
	 * THis just submit the batch and return is OK as string appended with batch names etc.
	 */
	public String execute(String batchName, String bizDate, String userid, String tout) throws FileNotFoundException, IOException {
		Instant start = Instant.now();
//		getLogger().debug(" BatchName: {}| BizDate: {} | UserId:{} | TimeOut:{} | {}",  batchName,bizDate,userid,tout, Msg.START_MSG.getValue() );
		
		getLogger().debug(" Creating GroupAsiaBatchAppVars. " + Msg.START_MSG.getValue()); 
		GroupAsiaBatchAppVars  appVars = new GroupAsiaBatchAppVars("CSCGroupAsiaWeb"); 
		getLogger().debug(" Creating GroupAsiaBatchAppVars. " + Msg.END_MSG.getValue());
		
		JobInfo ji = new JobInfo("001",batchName,new Date(),userid,"TEST/TEST",batchName,"TEST");
		appVars.setJobinfo(ji);
		
		Sbmbonlrec sbmbonlrec = new Sbmbonlrec();
		sbmbonlrec.company.set("1");sbmbonlrec.branch.set("31");
		sbmbonlrec.acctmonth.set(Integer.parseInt(bizDate.substring(4, 6)));
		sbmbonlrec.acctyear.set(Integer.parseInt(bizDate.substring(0, 4)));
		sbmbonlrec.user.set(userid);sbmbonlrec.userid.set(userid);
		sbmbonlrec.fsuco.set("9");sbmbonlrec.language.set("E");
		sbmbonlrec.jobname.set(batchName);
		
		//Adding the functionality that some batches needs extra parameter setting. //BugFix:20200329:001
		ConfigBatchFactory.setBatchParam(sbmbonlrec, batchName);					//BugFix:20200329:001	
//		sbmbonlrec.parmRecord[1].set("");											//BugFix:20200329:001
//		sbmbonlrec.parmProg[1].set("");												//BugFix:20200329:001
		
		getLogger().debug(" Creating Sbmbchonl. " + Msg.START_MSG.getValue());
		submitBatchOnline = new Sbmbchonl();
		submitBatchOnline.setGlobalVars(appVars);
		getLogger().debug(" Creating Sbmbchonl. " + Msg.END_MSG.getValue());
				
		getLogger().debug(" startCommitControl. " + Msg.START_MSG.getValue());
		appVars.startCommitControl();
		getLogger().debug(" startCommitControl. " + Msg.END_MSG.getValue());
		
		getLogger().debug(" submitBatchOnline.mainline(sbmbonlrec.sbmbchonlRec). " + Msg.START_MSG.getValue());
		submitBatchOnline.mainline(sbmbonlrec.sbmbchonlRec);
		getLogger().debug(" submitBatchOnline.mainline(sbmbonlrec.sbmbchonlRec). " + Msg.END_MSG.getValue());
		
		appVars.commit(); 
		appVars.finallyFreeAllAppVarsConnections();appVars=null; //v2.4.6
		
		Instant end = Instant.now();
		
//		getLogger().info(" BatchName: {}| BizDate: {} | UserId:{} | TimeOut:{} | {} | TimeTaken(secs) {} ",  batchName,bizDate,userid,tout, Msg.START_MSG.getValue(), java.time.Duration.between(start, end).toMillis()/1000 );
		return buildResponseStringToClient("OK", batchName,bizDate,userid,tout);
	  }
	
	/**
	 * For testing purpose this class can be called directly, with the command like:
	 * java G1AUTOALOC 20191209 JPANRY 1
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @since Mar 2, 2020 10:57:55 PM
	 */
		public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
			Instant start = Instant.now();
			String bn = args[0], bd = args[1], uid = args[2], tout= args[3];
			SubmitAction sa = SubmitAction.getInstance();

//			String bn = "G1ZVALDTEX", bd="20200423", uid = "JPAVTJ", tout="1";
//			String bn = "F9AUTOALOC", bd="20190301", uid = "JPAAJS", tout="1";
			sa.execute(bn, bd, uid, tout);
			sa.execute(bn, bd, uid, tout);
			

			WaitCompleteAction wca =  WaitCompleteAction.getInstance();
			String r2= wca.execute(bn, bd, uid, tout); 
			System.out.println(r2);
			
			Instant end = Instant.now();
			System.out.printf(" Total time taken: %s \n", java.time.Duration.between(start, end).toMillis()/1000);
		}
}

/**
 * Added class which will be responsible for setting the extra parameters for the batches like G1ZVALDTEX.
 * @author Chandan K Pratihast
 * @since Mar 4, 2020 11:01:56 AM
 * @version 2.3.2
 */
//BugFix:20200329:001
class ConfigBatchFactory{
	public static void setBatchParam(Sbmbonlrec sbmbonlrec, String batchName) {
		switch (batchName) {
		case "G1ZVALDTEX":
			sbmbonlrec.parmRecord[1].set("");
			sbmbonlrec.parmProg[1].set("PQ9M4");
			break;
		default:
			sbmbonlrec.parmRecord[1].set("");
			sbmbonlrec.parmProg[1].set("");
		}
	}
}
