package ig;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import util.DBUtil;

import com.csc.groupasia.runtime.variables.GroupAsiaBatchAppVars;
import com.csc.integral.batch.util.BatchServerLauncher;
import com.quipoz.framework.util.AppConfig;
import com.quipoz.framework.util.AppVars;
import com.quipoz.framework.util.IntegralDBProperties;

public class IGLoader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IGLoader.class);
	
	public static GroupAsiaBatchAppVars appVars = null;
	private static final String[] APPLICATION_CONTEXT = new String[] {
		"classpath*:mandatory/common.xml"};
	
	public static void load(String[] args){
		// Verifies prerequisites.
		if (ArrayUtils.getLength(args) < 1) {
			throw new IllegalArgumentException(
					"QuipozCfg.xml is expected.");
		}

		System.setProperty("Quipoz.CSCGroupAsiaWeb.XMLPath", args[0]);
		Properties dbProperties = new Properties();
		try {
			dbProperties.load(new FileReader(System.getProperty("dbconfig")));
		} catch (IOException e) {
			LOGGER.error("Exception occurred while loading database.properties file : ", e);//IJTI-1495
			return;
		}

		System.setProperty("org.quartz.dataSource.myDS.URL", IntegralDBProperties.getUrl());//IJTI-449
		System.setProperty("org.quartz.dataSource.myDS.user", IntegralDBProperties.getUser());//IJTI-449
		System.setProperty("org.quartz.dataSource.myDS.driver", IntegralDBProperties.getDriver());//IJTI-449
		System.setProperty("org.quartz.dataSource.myDS.password", IntegralDBProperties.getPassword());//IJTI-449
		
		ApplicationContext appCtx = new ClassPathXmlApplicationContext(
				new String[] { "classpath*:database.xml", "classpath*:com/csc/groupasia/context/GroupContext.xml",
						"classpath:com/csc/groupasia/context/dao/Group-DAO-" + IntegralDBProperties.getType()
								+ ".xml" });//IJTI-449
		appVars = new GroupAsiaBatchAppVars("CSCGroupAsiaWeb");
		AppConfig.setPrintlng(getFOPPrintLng(appVars));
		if (StringUtils.isEmpty(AppConfig.getPrintlng())) {
			throw new RuntimeException("AppConfig.printlng must NOT be empty.");
		}
		
		
		List<String> appContextLocations = new ArrayList<>();
		for(String contextLocation : APPLICATION_CONTEXT){
			appContextLocations.add(contextLocation);
		}
		
		if(args.length > 1){
			for(int i=1; i<args.length; i++){
				appContextLocations.add(args[i]);
			}
		}
		
		String [] appContext = new String[appContextLocations.size()];
		appContextLocations.toArray(appContext);
		
		appCtx = new ClassPathXmlApplicationContext(appContext, appCtx);
		
	}
	
	private static String getFOPPrintLng(AppVars appVars) {
		Connection con = null;
		ResultSet rs = null;
		try {
			con = appVars.getTempDBConnection("DB");
			rs = con.createStatement()
					.executeQuery(
							"select dataarea_data from printlng where dataarea_id='PRINTLNG'");
			rs.next();
			return rs.getString(1);
		} catch (Exception e) {
			throw new RuntimeException(
					"Error when querying printlng from database.", e);
		} finally {
			DBUtil.close(null, null, con);
		}
	}

}
