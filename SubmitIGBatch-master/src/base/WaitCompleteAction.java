package base;

import static CustomLogger.CustomLogger.getLogger;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.quipoz.COBOLFramework.messaging.SysOperatorMessageFormatter;

/**
 * This action will wait for the batch to complete and then return the status.
 * @author Chandan K Pratihast
 * @Mar 30, 2020 3:37:05 PM 
 * CKPBugFix:20200330:
 * 
* **************************************************************************************|
* | Mod Date			| Mod Desc                                  				  	|
* **************************************************************************************|
* 	20200614          	|BugFix:20200614:Chandan	v2.4.6								|
*  						|since StatusChkAction is changed this is modified accordingly	|	
*                       |																|
* **************************************************************************************/
public class WaitCompleteAction extends BaseAction {
	private static WaitCompleteAction wca = null;
	/**
	 * To make singleton, made the constructor as static.
	 */
	private WaitCompleteAction(){}
	/**
	 * Singleton approach to get only one instance of this Action.
	 * @return
	 */
	public static WaitCompleteAction getInstance() {
		lock.lock();
		if (null == wca) {
			wca = new WaitCompleteAction();
		}
		lock.unlock();
		return wca;
	}
	
	
	/**
	 * It uses the check status and loop till the batch completes or abort or cancelled or fails.
	 */
	@Override
	public String execute(String batchName, String bizDate, String userid, String tout)
			throws FileNotFoundException, IOException {
		getLogger().info(" BatchName: {}| BizDate: {} | UserId:{} | TimeOut:{} | {}",  batchName,bizDate,userid,tout, Msg.START_MSG.getValue() );
		
		StatusChkAction statusCheckAction =  StatusChkAction.getInstance();
		String ret = statusCheckAction.execute(batchName, bizDate, userid, tout);
		
		// CKPBugFix:20200330: Since return is now a json type so, needed to parse and get the 2 digit status value.
		// The return will be like: {"status":"00", "batchname":"bname","bizdate":"20190101 ", "userid":"jpaxx","tout":"1"}
		ret = ret.split(":")[1];   //CKPBugFix:20200330: this will give=> "00", "batchname":"bname","bizdate":"20190101 ", "userid":"jpaxx","tout":"1"}
		ret =  ret.substring(1,3); //CKPBugFix:20200330: trim to get 00 from above string: 00
		int countSleepLoop = 0;
		int timeOutInteger = Integer.parseInt(tout);
		System.out.println("Status:"+ret+" tout by user: " + tout);
		long sleepTime = Long.parseLong(Const.SLEEP_TIME.getValue());
		while(	!	(ret.equals(RET_TYPE.COMPLETED_SUCCESSFULY.getStatus()) || ret.equals(RET_TYPE.ABORTED.getStatus()) || ret.equals(RET_TYPE.CANCELLED.getStatus())) || ret.equals(RET_TYPE.FAILED.getStatus())	){
			try {
				Thread.sleep(sleepTime);
				getLogger().info("Status:{} | CheckStatusRepCount:{} X SleepTime {} (millisecs) | BatchName:{} | BizDate:{}.", ret, countSleepLoop,Const.SLEEP_TIME.getValue(),batchName,bizDate );
			} catch (Exception e) {
				getLogger().info(" BatchName: {}| BizDate: {} | UserId:{} | TimeOut:{} | {}",  batchName,bizDate,userid,tout, Msg.EXCEPTION_MSG.getValue() );
				e.printStackTrace();
			}
			if(++ countSleepLoop > timeOutInteger){ ret = Msg.STATUS_CHK_TIMEOUT.getValue(); break;}
			String retMsg = statusCheckAction.execute(batchName, bizDate, userid, tout); //v2.4.6: to get information of running process.
			ret = retMsg.split(":")[1].substring(1,3);  //CKPBugFix:20200510:2.4.5, the fix of CKPBugFix:20200330 needed here also.
			System.out.println(countSleepLoop+"/"+timeOutInteger+" | "+(countSleepLoop*sleepTime/1000)+"secs elapsed |"+ retMsg);	//v2.4.6: to get information of running process.		
		}
		getLogger().info(" BatchName: {}| BizDate: {} | UserId:{} | TimeOut:{} | {}",  batchName,bizDate,userid,tout, Msg.END_MSG.getValue() );
		return buildResponseStringToClient(ret, batchName,bizDate,userid,tout);
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		WaitCompleteAction sa = new WaitCompleteAction();
		String r = sa.execute("G1AUTOALOC", "20190301", "UNDERWR1", "1");// ("?action=submit&bn=G1AUTOALOC&bizD=20250901&uid=BATCHUSER&tout=1");
	}
}
