package base;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;



import com.csc.fsu.general.procedures.Sbmbchonl;
import com.csc.groupasia.runtime.variables.GroupAsiaBatchAppVars;
import com.quipoz.framework.util.AppConfig;
import com.quipoz.framework.util.IntegralDBProperties;
import com.zaxxer.hikari.HikariDataSource;

import static CustomLogger.CustomLogger.getLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The abstract class to do the execute of the action. It also loads the properties of IG batch servers like dbconfig system properties and ApplicationContext (if not set already.) .
 * @author Chandan K Pratihast
 * @since Mar 2, 2020 10:28:21 AM
 *
* **************************************************************************************|
* | Mod Date			| Mod Desc                                  				  	|
* **************************************************************************************|
* 	20200529          	|BugFix:20200509:Chandan										|
*  						|The batch submit was not getting into IGTables. In the 		|
*  						|spring application context we need to set quartz and then 		|	
*  						|other xmls.													|
* 	20200614          	|BugFix:20200614:Chandan	v2.4.6								|
*  						|buildResponseStringToClient: simplified by using hashmap 		|	
*                       |            													|
* **************************************************************************************/
public abstract class BaseAction {
	protected static final ReentrantLock lock = new ReentrantLock();
	protected GroupAsiaBatchAppVars appVars;
	protected Sbmbchonl 			submitBatchOnline;
	protected Properties 			dbProperties;
	protected ApplicationContext 	appCtx; 					//20200509:Added
	protected String[] 				cmdBatchArgs;				//20200509:Added
	
	//The order of xmls as needed to build the application context.
	private static final String[] APPLICATION_CONTEXT = new String[] { //BugFix:20200509
			"classpath*:mandatory/common.xml",
			"classpath*:scenarios/scheduling/quartz/quartz-common.xml",
			"classpath*:scenarios/scheduling/quartz/quartz-jobs.xml",
			"classpath*:scenarios/scheduling/quartz/quartz-listeners.xml",
			"classpath*:com/csc/groupasia/context/GroupContext.xml",
			"classpath:com/csc/groupasia/context/dao/Group-DAO-0.xml",
			"classpath:com/zurich/customerZurJpn-services.xml"
	};//BugFix:20200509
	
	//The order of xmls as needed to build the AppVars.
	private static final String[] APPVARS_CONTEXT = new String[] { //BugFix:20200509
			"classpath*:database.xml",
			"classpath*:com/csc/groupasia/context/GroupContext.xml",
			"classpath:com/csc/groupasia/context/dao/Group-DAO-0.xml"
	};//BugFix:20200509
		
	/**
	 * The method to execute the processing.
	 * @param batchName : The batch name of IG which we want to have action on.
	 * @param bizDate : The business date. In AIX server it is supposed to be in the format YYYY-MM-DD. Generally we need to give YYYYMMDD.
	 * @param userid : The userid who has the sufficient rights to perform the action in the system.
	 * @param tout : The timeout counter. Based on this value it will multiply to 10 seconds and for those many 10x seconds it will wait before looping to check the status again.
	 * @return : The response after completion of the action. For getting status the return will be useful. For submit it will return OK. Please refer its implementation class.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @since Mar 2, 2020 10:29:44 AM
	 */
	public abstract String execute(String batchName, String bizDate, String userid, String tout)  throws FileNotFoundException, IOException;
	
	/**
	 * The class to set the system properties and ApplicationContext. 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @since Mar 2, 2020 10:29:44 AM
	 */
	protected void setIGBatchSystemProperties() throws FileNotFoundException, IOException{
		if (null == dbProperties) {
			getLogger().info(" Setting DB Properties ");
			dbProperties = new Properties();
			dbProperties.load(new FileReader(System.getProperty("dbconfig")));
		}
		
		if (null == System.getProperty("org.quartz.dataSource.myDS.URL." )) {
			getLogger().info(" Setting org.quartz.dataSource. ");
			System.setProperty("org.quartz.dataSource.myDS.URL", dbProperties.getProperty("database.url"));
			System.setProperty("org.quartz.dataSource.myDS.user", dbProperties.getProperty("database.username"));
			System.setProperty("org.quartz.dataSource.myDS.driver",	dbProperties.getProperty("database.driverClassName"));
			System.setProperty("org.quartz.dataSource.myDS.password", dbProperties.getProperty("database.password"));
			
			getLogger().info(" Setting ClassPathXmlApplicationContext. ");
			
			//BugFix:20200509 START ***/
			//Build the context with the order of xml as needed for AppVars. And hold the reference.
			appCtx 	= new ClassPathXmlApplicationContext(APPVARS_CONTEXT); 
			appVars = new GroupAsiaBatchAppVars("CSCGroupAsiaWeb"); 
			//Now next add the application context with the order of xmls as defined above.APPLICATION_CONTEXT. 
			//And then add the appVars context at the end.
			appCtx 	= new ClassPathXmlApplicationContext(APPLICATION_CONTEXT,appCtx);
			//BugFix:20200509 END ***/

			getLogger().info(" Setting ClassPathXmlApplicationContext. Completed!");
		}
	}
	
	/**
	 * Updated the response string to be more informative. And the return format is JSON. 
	 * @param status
	 * @param batchName
	 * @param bizDate
	 * @param userid
	 * @param tout
	 * @return {"status":"00", "batchname":"bname","bizdate":"20190101 ", "userid":"jpaxx","tout":"1"}
	 * @since Mar 6, 2020 8:23:39 PM
	 * @version 2.3.1 
	 */
	public String buildResponseStringToClient(String status, String batchName, String bizDate, String userid, String tout){
		StringBuilder sb = new StringBuilder(300);
		sb.append(Msg.JSON_CURLY_START.getValue());
		buildJson(sb,Msg.JSON_RET_STATUS_KEY.getValue(),status);sb.append(Msg.JSON_COMMA.getValue());
		buildJson(sb,Msg.JSON_RET_BATCHNAME_KEY.getValue(),batchName);sb.append(Msg.JSON_COMMA.getValue());
		buildJson(sb,Msg.JSON_RET_BIZDATE_KEY.getValue(),bizDate);sb.append(Msg.JSON_COMMA.getValue());	
		buildJson(sb,Msg.JSON_RET_UID_KEY.getValue(),userid);sb.append(Msg.JSON_COMMA.getValue());	
		buildJson(sb,Msg.JSON_RET_TIMEOUT_KEY.getValue(),tout);
		sb.append(Msg.JSON_CURLY_END.getValue());	
		return sb.toString();
	}
	
	private void buildJson(StringBuilder sb, String k, String v){
		sb.append(Msg.JSON_QUOTE.getValue());sb.append(k);sb.append(Msg.JSON_QUOTE.getValue());sb.append(Msg.JSON_SEPARATOR.getValue());sb.append(Msg.JSON_QUOTE.getValue());
		sb.append(v);sb.append(Msg.JSON_QUOTE.getValue());
	}
	
	/**
	 * To build the response json using map. 
	 * @author Chandan K Pratihast
	 * @Jun 14, 2020 12:57:17 AM. v2.4.6
	 * @param keyValMap
	 * @return
	 */
	public String buildResponseStringToClient(LinkedHashMap<String, String> keyValMap){
		StringBuilder sb = new StringBuilder(300);
		sb.append(Msg.JSON_CURLY_START.getValue());
		keyValMap.forEach((k,v)->{ buildJson(sb,k,v);sb.append(Msg.JSON_COMMA.getValue()); });
		sb.append(Msg.JSON_CURLY_END.getValue());	
		return sb.toString();
	}
}


/**
 * All the possible status of the IG Batches.
 * @author Chandan K Pratihast
 * @since Mar 2, 2020 10:27:22 AM
 */
enum RET_TYPE{
	SUBMITTED("50"),RUNNING("10"),
	COMPLETED_SUCCESSFULY("90"),
	ABORTED("01"),FAILED("03"),
	CANCELLED("05");
	private String statusStr;
	private RET_TYPE(String s){statusStr = s;}
	public String getStatus(){return statusStr;}
}

/**
 * The sleep time in seconds.
 * @author Chandan K Pratihast
 * @since Mar 2, 2020 10:26:37 AM
 */
enum Const{
	SLEEP_TIME("10000");
	private String val;
	private Const(String s){val = s;}
	public String getValue(){return val;}
}


/**
 * The recognized actions. 
 * @author Chandan K Pratihast
 * @since Mar 2, 2020 10:27:49 AM
 */
enum Action{
	submit,status,wait4EndThenStatus;
}

enum Msg{
	STATUS_CHK_TIMEOUT(" Status Check Timed Out!"),
	START_MSG			(" *** START    ***"),
	END_MSG				(" *** END      ***"),
	EXCEPTION_MSG		(" *** EXCEPTION***"),
	//The below values has to be same as in the Msg enum of IGSocketService.Msg
	JSON_RET_STATUS_KEY		("status"),
	JSON_RET_ACTION_KEY		("action"),	
	JSON_RET_BATCHNAME_KEY	("bn"),
	JSON_RET_BIZDATE_KEY	("bd"),
	JSON_RET_UID_KEY		("uid"),
	JSON_RET_TIMEOUT_KEY	("tout"),
	JSON_CURLY_START 		("{"),
	JSON_SEPARATOR 			(":"),
	JSON_QUOTE				("\""),
	JSON_COMMA				(","),
	JSON_CURLY_END			("}");
	
	
	private String msg;
	private Msg(String s){msg = s;}
	public String getValue(){return msg;}
}
