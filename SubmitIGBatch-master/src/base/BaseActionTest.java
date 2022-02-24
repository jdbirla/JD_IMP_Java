package base;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;



import com.csc.fsu.general.procedures.Sbmbchonl;
import com.csc.fsu.general.recordstructures.Sbmbonlrec;
import com.csc.groupasia.runtime.variables.GroupAsiaBatchAppVars;
import com.quipoz.COBOLFramework.job.JobInfo;
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
 */
public class BaseActionTest {
	protected static final ReentrantLock lock = new ReentrantLock();
	protected static GroupAsiaBatchAppVars appVars;
	protected Sbmbchonl 			submitBatchOnline;
	protected Properties 			dbProperties;
	protected static ApplicationContext 	appCtx; 					//20200509:Added
	
	private static final String[] APPLICATION_CONTEXT = new String[] { //20200509:Added
			"classpath*:mandatory/common.xml",
			"classpath*:scenarios/scheduling/quartz/quartz-common.xml",
			"classpath*:scenarios/scheduling/quartz/quartz-jobs.xml",
			"classpath*:scenarios/scheduling/quartz/quartz-listeners.xml",
			"classpath*:com/csc/groupasia/context/GroupContext.xml",
			"classpath:com/csc/groupasia/context/dao/Group-DAO-0.xml",
			"classpath:com/zurich/customerZurJpn-services.xml"
	};//20200509
	
	private static final String[] APPVARS_CONTEXT = new String[] { //BugFix:20200509
			"classpath*:database.xml",
			"classpath*:com/csc/groupasia/context/GroupContext.xml",
			"classpath:com/csc/groupasia/context/dao/Group-DAO-0.xml"
	};//BugFix:20200509

	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		System.setProperty("Quipoz.CSCGroupAsiaWeb.XMLPath", args[0]);
		Properties dbProperties = new Properties();
		try {
			dbProperties.load(new FileReader(System.getProperty("dbconfig")));
		} catch (IOException e) { e.printStackTrace();
			return;
		}
		
		getLogger().info(" Setting org.quartz.dataSource. ");
		System.setProperty("org.quartz.dataSource.myDS.URL", dbProperties.getProperty("database.url"));
		System.setProperty("org.quartz.dataSource.myDS.user", dbProperties.getProperty("database.username"));
		System.setProperty("org.quartz.dataSource.myDS.driver",	dbProperties.getProperty("database.driverClassName"));
		System.setProperty("org.quartz.dataSource.myDS.password", dbProperties.getProperty("database.password"));
		
		/*appCtx = new ClassPathXmlApplicationContext(
				new String[] { "classpath*:database.xml", "classpath*:com/csc/groupasia/context/GroupContext.xml",
						"classpath:com/csc/groupasia/context/dao/Group-DAO-" + IntegralDBProperties.getType()
								+ ".xml" });//IJTI-449
*/		appCtx = new ClassPathXmlApplicationContext(APPVARS_CONTEXT);
		appVars = new GroupAsiaBatchAppVars("CSCGroupAsiaWeb");
		
		/*List<String> appContextLocations = new ArrayList<>();
		for(String contextLocation : APPLICATION_CONTEXT){
			appContextLocations.add(contextLocation);
		}
						
		String [] appContext = new String[appContextLocations.size()];
		appContextLocations.toArray(appContext);
		*/
		//IJTI-401 starts
		appCtx = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT, appCtx);
		//IJTI-401 ends
		//IGROUP-2507 ENDS
		
		System.out.println("Configuration done!");
		String bn = "F9AUTOALOC", bd="20190301", uid = "JPAAJS", tout="1";
		execute(bn, bd, uid, tout);

	}
	
	public static void execute(String batchName, String bizDate, String userid, String tout)
			throws FileNotFoundException, IOException {
		JobInfo ji = new JobInfo("001",batchName,new Date(),userid,"TEST/TEST",batchName,"TEST");
		appVars.setJobinfo(ji);
		
		Sbmbonlrec sbmbonlrec = new Sbmbonlrec();
		sbmbonlrec.company.set("1");sbmbonlrec.branch.set("31");
		sbmbonlrec.acctmonth.set(Integer.parseInt(bizDate.substring(4, 6)));
		sbmbonlrec.acctyear.set(Integer.parseInt(bizDate.substring(0, 4)));
		sbmbonlrec.user.set(userid);sbmbonlrec.userid.set(userid);
		sbmbonlrec.fsuco.set("9");sbmbonlrec.language.set("J");
		sbmbonlrec.jobname.set(batchName);
		
		//Adding the functionality that some batches needs extra parameter setting. //BugFix:20200329:001
		ConfigBatchFactory.setBatchParam(sbmbonlrec, batchName);					//BugFix:20200329:001	
//		sbmbonlrec.parmRecord[1].set("");											//BugFix:20200329:001
//		sbmbonlrec.parmProg[1].set("");												//BugFix:20200329:001
		
		Sbmbchonl submitBatchOnline = new Sbmbchonl();
		submitBatchOnline.setGlobalVars(appVars);
		getLogger().debug(" Creating Sbmbchonl. " );
				
		getLogger().debug(" startCommitControl. " );
		appVars.startCommitControl();
		getLogger().debug(" startCommitControl. " );
		
		getLogger().debug(" submitBatchOnline.mainline(sbmbonlrec.sbmbchonlRec). " );
		submitBatchOnline.mainline(sbmbonlrec.sbmbchonlRec);
		getLogger().debug(" submitBatchOnline.mainline(sbmbonlrec.sbmbchonlRec). " );
		
		appVars.commit();
	}
	
	
}


